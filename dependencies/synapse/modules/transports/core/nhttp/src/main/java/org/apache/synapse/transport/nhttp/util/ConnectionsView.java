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

package org.apache.synapse.transport.nhttp.util;

import org.apache.synapse.commons.jmx.MBeanRegistrar;

import java.util.*;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * ConnectionsView MBean can be used to collect and monitor statistics on HTTP connections
 * created by the NHTTP transport. Connection statistics can be divided into two categories,
 * namely short term data and long term data. Short term data is related to the last 15
 * minutes of execution and they are updated every minute. Long term data is related to
 * the last 24 hours of execution and they get updated every 5 minutes.  In addition to the
 * connection statistics this MBean also provides information on the request and response
 * sizes received over the HTTP connections. All messages are divided into six categories
 * based on their sizes and the resulting counts are made available as a table. 
 */
public class ConnectionsView implements ConnectionsViewMBean {

    private static final String NHTTP_CONNECTIONS = "NhttpConnections";

    // Bucket definitions
    private static final int LESS_THAN_1K       = 0;
    private static final int LESS_THAN_10K      = 1;
    private static final int LESS_THAN_100K     = 2;
    private static final int LESS_THAN_1M       = 3;
    private static final int LESS_THAN_10M      = 4;
    private static final int GREATER_THAN_10M   = 5;

    private static final int SHORT_DATA_COLLECTION_PERIOD = 60;
    private static final int LONG_DATA_COLLECTION_PERIOD = 60 * 5;

    private static final int SAMPLES_PER_HOUR = (60 * 60)/LONG_DATA_COLLECTION_PERIOD;

    private Queue<Integer> shortTermDataQueue = new LinkedList<Integer>();
    private Queue<Integer> longTermDataQueue = new LinkedList<Integer>();

    private AtomicInteger activeConnections = new AtomicInteger(0);
    private AtomicInteger shortTermOpenedConnections = new AtomicInteger(0);
    private AtomicInteger longTermOpenedConnections = new AtomicInteger(0);
    // The map keeps the key as connection host:port and value as the number of connections for
    // that host
    private Map<String,AtomicInteger> activeConnectionsPerHost = new HashMap<String,AtomicInteger>();
    // The array length must be equal to the number of buckets
    private AtomicInteger[] requestSizeCounters = new AtomicInteger[6];
    private AtomicInteger[] responseSizeCounters = new AtomicInteger[6];

    private Date resetTime = Calendar.getInstance().getTime();

    private ScheduledExecutorService scheduler;

    private String name;

    public ConnectionsView(final String name) {
        this.name = name;

        this.scheduler = Executors.newSingleThreadScheduledExecutor(new ThreadFactory() {
            public Thread newThread(Runnable r) {
                return new Thread(r, name + "-connections-view");
            }
        });

        initCounters(requestSizeCounters);
        initCounters(responseSizeCounters);

        Runnable task = new Runnable() {
            public void run() {
                // We only need historical data for the last 15 minutes
                // Therefore no need to keep data older than that...
                if (shortTermDataQueue.size() == 15) {
                    shortTermDataQueue.remove();
                }
                shortTermDataQueue.offer(shortTermOpenedConnections.getAndSet(0));
            }
        };
        // Delay the timer by 1 minute to prevent the task from starting immediately
        scheduler.scheduleAtFixedRate(task, SHORT_DATA_COLLECTION_PERIOD,
                SHORT_DATA_COLLECTION_PERIOD, TimeUnit.SECONDS);

        Runnable longTermCollector = new Runnable() {
            public void run() {
                // We only need historical data for the last 24 hours
                // Therefore no need to keep data older than that...
                if (longTermDataQueue.size() == 24 * SAMPLES_PER_HOUR) {
                    longTermDataQueue.remove();
                }
                longTermDataQueue.offer(longTermOpenedConnections.getAndSet(0));
            }
        };
        scheduler.scheduleAtFixedRate(longTermCollector, LONG_DATA_COLLECTION_PERIOD,
                LONG_DATA_COLLECTION_PERIOD, TimeUnit.SECONDS);
        boolean registered = false;
        try {
            registered = MBeanRegistrar.getInstance().registerMBean(this, NHTTP_CONNECTIONS, name);
        } finally {
            if (!registered) {
                scheduler.shutdownNow();
            }
        }
    }

    public void destroy() {
        MBeanRegistrar.getInstance().unRegisterMBean(NHTTP_CONNECTIONS, name);
        if (!scheduler.isShutdown()) {
            scheduler.shutdownNow();
        }
    }

    private void initCounters(AtomicInteger[] counters) {
        for (int i = 0; i < counters.length; i++) {
            if (counters[i] == null) {
                counters[i] = new AtomicInteger(0);
            } else {
                counters[i].set(0);
            }
        }
    }

    protected void connected() {
        activeConnections.incrementAndGet();
        shortTermOpenedConnections.incrementAndGet();
        longTermOpenedConnections.incrementAndGet();
    }

    protected void disconnected() {
        activeConnections.decrementAndGet();
    }

    protected void notifyMessageSize(long size, boolean isRequest) {
        // This logic gets executed for each and every transaction. For a typical
        // mediation scenario this method will be called 4 times. Therefore I'm using
        // arrays of integers to keep the overhead down to a minimum. Since the number
        // of buckets is 6, this can be easily managed without using a slow data structure
        // like a HashMap. This approach guarantees O(1) complexity.

        AtomicInteger[] counters = isRequest ? requestSizeCounters : responseSizeCounters;

        if (size < 1024) {
            counters[LESS_THAN_1K].incrementAndGet();
        } else if (size < 10240) {
            counters[LESS_THAN_10K].incrementAndGet();
        } else if (size < 102400) {
            counters[LESS_THAN_100K].incrementAndGet();
        } else if (size < 1048576) {
            counters[LESS_THAN_1M].incrementAndGet();
        } else if (size < 10485760) {
            counters[LESS_THAN_10M].incrementAndGet();
        } else {
            counters[GREATER_THAN_10M].incrementAndGet();
        }
    }

    public int getActiveConnections() {
        return activeConnections.get();
    }

    public int getLastMinuteConnections() {
        return getTotalConnections(1);
    }

    public int getLast5MinuteConnections() {
        return getTotalConnections(5);
    }

    public int getLast15MinuteConnections() {
        return getTotalConnections(15);
    }

    public int getLastHourConnections() {
        return getTotalConnectionsByHour(1);
    }

    public int getLast8HourConnections() {
        return getTotalConnectionsByHour(8);
    }

    public int getLast24HourConnections() {
        return getTotalConnectionsByHour(24);
    }

    public Map getRequestSizesMap() {
        return getCountersMap(requestSizeCounters);
    }

    public Map getResponseSizesMap() {
        return getCountersMap(responseSizeCounters);
    }

    public Date getLastResetTime() {
        return resetTime;
    }

    private Map<String,Integer> getCountersMap(AtomicInteger[] counters) {
        // This ensures that keys are returned in the same order we insert them
        // Provides better readability in the JMX consoles
        Map<String,Integer> map = new LinkedHashMap<String,Integer>();
        map.put("< 1 K", counters[LESS_THAN_1K].get());
        map.put("< 10 K", counters[LESS_THAN_10K].get());
        map.put("< 100 K", counters[LESS_THAN_100K].get());
        map.put("< 1 M", counters[LESS_THAN_1M].get());
        map.put("< 10 M", counters[LESS_THAN_10M].get());
        map.put("> 10 M", counters[GREATER_THAN_10M].get());
        return map;
    }

    public void reset() {
        initCounters(requestSizeCounters);
        initCounters(responseSizeCounters);
        shortTermDataQueue.clear();
        longTermDataQueue.clear();
        resetTime = Calendar.getInstance().getTime();
    }

    /**
     * Return the number of total connections opened during last 'n' munites
     * of execution
     *
     * @param n Number of minutes in the execution history
     * @return The number of connections opened
     */
    private int getTotalConnections(int n) {
        int sum = 0;
        Integer[] array = shortTermDataQueue.toArray(new Integer[shortTermDataQueue.size()]);

        if (n > array.length) {
            for (int i = 0; i < array.length; i++) {
                sum += array[i];
            }
        } else {
            for (int i = 0; i < n; i++) {
                sum += array[array.length - 1 - i];
            }
        }
        return sum;
    }

    /**
     * Return the number of total connections opened during last 'n' hours
     * of execution
     *
     * @param n Number of hours in the execution history
     * @return The number of connections opened
     */
    private int getTotalConnectionsByHour(int n) {
        int samples = n * SAMPLES_PER_HOUR;
        int sum = 0;
        Integer[] array = longTermDataQueue.toArray(new Integer[longTermDataQueue.size()]);

        if (samples > array.length) {
            for (int i = 0; i < array.length; i++) {
                sum += array[i];
            }
        } else {
            for (int i = 0; i < samples; i++) {
                sum += array[array.length - 1 - i];
            }
        }
        return sum;
    }

    /**
     * Setter method for activeConnectionsPerHost, this will get called during connection creation
     * and Connection shutdown operations
     *
     * @param activeConnectionsPerHost
     */
    public void setActiveConnectionPerHostEntry(Map<String,AtomicInteger> activeConnectionsPerHost){
        this.activeConnectionsPerHost = activeConnectionsPerHost;
    }

    public Map getActiveConnectionsPerHosts(){
        return activeConnectionsPerHost;
    }

}
