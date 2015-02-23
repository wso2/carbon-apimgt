/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *   * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.apache.synapse.commons.jmx;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.util.MiscellaneousUtil;

import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.lang.management.ThreadMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;

/**
 * ThreadingView can be used to monitor a named thread group over JMX. Data
 * gathered by this monitor can be classified as short term data and long term data.
 * Short term data is the statistics related to last 15 minutes of execution and they
 * get updated every 2 seconds. Long term data is related to last 24 hours of execution
 * and they get updated every 5 minutes. This monitor can also be configured to log a
 * summary of the thread states periodically. If needed a margin can be set for the blocked
 * thread percentage, upon exceeding which a system alert will be logged as a warning. By
 * default both periodic logs and alerts are turned off.
 */
public class ThreadingView implements ThreadingViewMBean {

    private static final String SYNAPSE_THREADING_VIEW = "Threading";
    private static final int SHORT_SAMPLING_PERIOD = 2;
    private static final int LONG_SAMPLING_PERIOD = 5 * 60;
    private static final int SAMPLES_PER_MINUTE = 60/ SHORT_SAMPLING_PERIOD;
    private static final int SAMPLES_PER_HOUR = 3600/LONG_SAMPLING_PERIOD;

    private String threadNamePrefix = null;
    private boolean periodicLogs = false;
    private double alertMargin = -1;

    private double avgBlockedWorkerPercentage = 0.0;
    private double avgUnblockedWorkerPercentage = 0.0;

    /**
     * The queue of samples taken by the short term data collector task. Maintained as a fixed
     * length queue. Only the data for the last 15 minutes of execution will be stored here.
     * Maximum length = (60/2) * 15 = 450
     */
    private Queue<Double> shortTermDataQueue = new LinkedList<Double>();

    /**
     * The queue of samples taken by the long term data collector task. Maintained as a fixed
     * length queue. Only the data for the last 24 hours of execution will be stored here.
     * Maximum length = (3600/5*60) * 24 = 288
     */
    private Queue<Double> longTermDataQueue = new LinkedList<Double>();

    private int samplesCount = 0;
    private int totalCount = 0;

    private Date resetTime = Calendar.getInstance().getTime();

    private static final Log log = LogFactory.getLog(ThreadingView.class);

    private static final ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();

    private ScheduledExecutorService scheduler;

    private boolean threadJMXViewEnabled = true;

    public ThreadingView(final String threadNamePrefix) {
        this.threadNamePrefix = threadNamePrefix;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                return new Thread(r, threadNamePrefix + "-thread-view");
            }
        });
        initMBean();
    }

    public ThreadingView(String threadNamePrefix, boolean periodicLogs, double alertMargin) {
        this(threadNamePrefix);

        Properties props = MiscellaneousUtil.loadProperties("synapse.properties");
        String jmxEnabled = MiscellaneousUtil.getProperty(props,
                JmxConfigurationConstants.PROP_THREAD_JMX_ENABLE,"true");
        if("false".equals(jmxEnabled)){
            this.threadJMXViewEnabled = false;
        }
        
        this.periodicLogs = periodicLogs;
        if (alertMargin > 0 && alertMargin < 100) {
            this.alertMargin = alertMargin;
        } else {
            log.warn("Invalid alert margin for the thread group: " + threadNamePrefix + " - " +
                    "Using default value");
        }
    }

    public void destroy() {
        if (log.isDebugEnabled()) {
            log.debug("Un-registering the Synapse threading view for the thread group: " +
                    threadNamePrefix);
        }
        MBeanRegistrar.getInstance().unRegisterMBean(SYNAPSE_THREADING_VIEW, threadNamePrefix);
        if (!scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    private void initMBean() {
        if (log.isDebugEnabled()) {
            log.debug("Starting a new Synapse threading view for the thread group: " +
                    threadNamePrefix);
        }
        scheduler.scheduleAtFixedRate(new ThreadingDataCollectorTask(), SHORT_SAMPLING_PERIOD,
                SHORT_SAMPLING_PERIOD, TimeUnit.SECONDS);
        scheduler.scheduleAtFixedRate(new LongTermDataCollectorTask(), LONG_SAMPLING_PERIOD,
                LONG_SAMPLING_PERIOD, TimeUnit.SECONDS);
        boolean registered = false;
        try {
            registered = MBeanRegistrar.getInstance().registerMBean(this, SYNAPSE_THREADING_VIEW,
                        threadNamePrefix);
        } finally {
            if (!registered) {
                scheduler.shutdownNow();
            }
        }
    }

    public int getTotalWorkerCount() {
        int count = 0;
        ThreadInfo[] threadInfo = dumpAllThreads();
        for (ThreadInfo ti : threadInfo) {
            if (ti != null && ti.getThreadName().startsWith(threadNamePrefix)) {
                count++;
            }
        }
        return count;
    }

    private double getBlockedWorkerPercentage() {
        int totalCount = 0;
        int blockedCount = 0;
        ThreadInfo[] threadInfo = dumpAllThreads();
        for (ThreadInfo ti : threadInfo) {
            // see if the thread name matches the prefix
            if (ti != null && ti.getThreadName().startsWith(threadNamePrefix)) {
                totalCount++;
                if (isBlocked(ti)) {
                    blockedCount++;
                }
            }
        }
        if (totalCount == 0) {
            return 0;
        }
        return ((double) blockedCount/(double) totalCount) * 100;
    }

    public String[] getDeadLockedWorkers() {
        String[] workers = null;
        // JDK 1.6 has a better implementation of this method but since we are on JDK 1.5
        // we have to stick with this for now
        long[] threads = threadBean.findMonitorDeadlockedThreads();
        if (threads != null) {
            ThreadInfo[] threadInfo = threadBean.getThreadInfo(threads);
            workers = new String[threadInfo.length];
            for (int i = 0; i < threadInfo.length; i++) {
                if (threadInfo[i] != null) {
                    workers[i] = threadInfo[i].getThreadName();
                } else {
                    workers[i] = null;
                }
            }
        }
        return workers;
    }

    public double getAvgBlockedWorkerPercentage() {
        return avgBlockedWorkerPercentage;
    }

    public double getAvgUnblockedWorkerPercentage() {
        return avgUnblockedWorkerPercentage;
    }

    public double getLastMinuteBlockedWorkerPercentage() {
        return getAverageBlockedThreads(1);
    }

    public double getLast5MinuteBlockedWorkerPercentage() {
        return getAverageBlockedThreads(5);
    }

    public double getLast15MinuteBlockedWorkerPercentage() {
        return getAverageBlockedThreads(15);
    }

    public double getLastHourBlockedWorkerPercentage() {
        return getAverageBlockedThreadsByHour(1);
    }

    public double getLast8HourBlockedWorkerPercentage() {
        return getAverageBlockedThreadsByHour(8);
    }

    public double getLast24HourBlockedWorkerPercentage() {
        return getAverageBlockedThreadsByHour(24);
    }

    public Date getLastResetTime() {
        return resetTime;
    }

    public void reset() {
       avgBlockedWorkerPercentage = 0.0;
       avgUnblockedWorkerPercentage = 0.0;
       shortTermDataQueue.clear();
       longTermDataQueue.clear();
       samplesCount = 0;
       totalCount = 0;
       resetTime = Calendar.getInstance().getTime();
    }

    private boolean isBlocked(ThreadInfo threadInfo) {
        // A thread is considered "Blocked" if it is in the BLOCKED state
        // or if it is in the WAITING state due to some reason other than
        // 'parking'.
        Thread.State state = threadInfo.getThreadState();
        if (state.equals(Thread.State.BLOCKED)) {
            return true;
        } else if (state.equals(Thread.State.WAITING) ||
                state.equals(Thread.State.TIMED_WAITING)) {
            StackTraceElement[] stackTrace = threadInfo.getStackTrace();
            if (stackTrace.length > 0 && !"park".equals(stackTrace[0].getMethodName())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Get a summary of all threads running in the JVM.
     *
     * @return an array of ThreadInfo objects
     */
    private ThreadInfo[] dumpAllThreads() {
        // JDK 1.6 has a built-in method for this
        // But since we are on JDK 1.5 we have to follow this 2-step approach
        if (threadJMXViewEnabled) {
            long[] ids = threadBean.getAllThreadIds();
            return threadBean.getThreadInfo(ids, 1);
        } else {
            return new ThreadInfo[0];
        }
    }

    /**
     * Calculates and returns the average blocked worker percentage during last 'n' minutes
     * of execution
     *
     * @param n Number of minutes in the execution history
     * @return the average blocked percentage as a double value
     */
    private double getAverageBlockedThreads(int n) {
        int samples = n * SAMPLES_PER_MINUTE;
        double sum = 0.0;
        Double[] array = shortTermDataQueue.toArray(new Double[shortTermDataQueue.size()]);

        if (samples > array.length) {
            // If we don't have enough samples in the queue, try to approximate
            // the value using all the available samples
            samples = array.length;
            for (Double anArray : array) {
                sum += anArray;
            }
        } else {
            for (int i = 0; i < samples; i++) {
                sum += array[array.length - 1 - i];
            }
        }

        if (samples == 0) {
            return 0.0;
        }
        return sum/samples;
    }

    /**
     * Calculates and returns the average blocked worker percentage during last 'n' hours
     * of execution
     *
     * @param n Number of hours in the execution history
     * @return the average blocked percentage as a double value
     */
    private double getAverageBlockedThreadsByHour(int n) {
        int samples = n * SAMPLES_PER_HOUR;
        double sum = 0.0;
        Double[] array = longTermDataQueue.toArray(new Double[longTermDataQueue.size()]);

        if (samples > array.length) {
            samples = array.length;
            for (Double anArray : array) {
                sum += anArray;
            }
        } else {
            for (int i = 0; i < samples; i++) {
                sum += array[array.length - 1 - i];
            }
        }

        if (samples == 0) {
            return 0.0;
        }
        return sum/samples;
    }


    private class ThreadingDataCollectorTask implements Runnable {

        public void run() {
            samplesCount++;

            double blocked = getBlockedWorkerPercentage();
            double unblocked = 100 - blocked;

            // calculate all time average values
            avgBlockedWorkerPercentage = (avgBlockedWorkerPercentage * totalCount + blocked)/
                    (double) (totalCount + 1);
            avgUnblockedWorkerPercentage = (avgUnblockedWorkerPercentage * totalCount + unblocked)/
                    (double) (totalCount + 1);

            if (shortTermDataQueue.size() == 15 * SAMPLES_PER_MINUTE) {
                shortTermDataQueue.remove();
            }
            shortTermDataQueue.offer(blocked);

            if (samplesCount == SAMPLES_PER_MINUTE) {
                samplesCount = 0;
                periodicDump();
            }
            totalCount++;
        }

        private void periodicDump() {
            if (periodicLogs && log.isDebugEnabled()) {
                StringBuffer buffer = new StringBuffer();
                buffer.append("Thread state summary for ").append(threadNamePrefix).
                        append(" threads - Blocked: ").append(avgBlockedWorkerPercentage).
                        append("%, Unblocked: ").append(avgUnblockedWorkerPercentage).
                        append("%");
                log.debug(buffer.toString());
            }

            if (alertMargin > 0) {
                double blocked = getAverageBlockedThreads(1);
                if (blocked > alertMargin) {
                    log.warn("SYSTEM ALERT: " + blocked + "% of the " + threadNamePrefix +
                            " threads were in BLOCKED state during last minute!");
                }
            }
        }
    }

    private class LongTermDataCollectorTask implements Runnable {
        public void run() {
            double blocked = getBlockedWorkerPercentage();

            if (longTermDataQueue.size() == 24 * SAMPLES_PER_HOUR) {
                longTermDataQueue.remove();
            }
            longTermDataQueue.offer(blocked);
        }
    }
}

