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

package org.apache.synapse.endpoints;

import org.apache.axis2.transport.base.MessageLevelMetricsCollector;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class is the metrics collector and JMX control point for Endpoints
 */
public class EndpointView implements EndpointViewMBean, MessageLevelMetricsCollector {

    private static final Log log = LogFactory.getLog(EndpointView.class);
    private static final Long ONE = 1L;
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(20,
        new ThreadFactory() {
            public Thread newThread(Runnable r) {
                return new Thread(r, "endpoint-jmx-stat-collector");
            }
        }
    );

    /** The name of the endpoint */
    private String endpointName = null;
    /** The actual Endpoint implementation we manage */
    private Endpoint endpoint = null;

    // metrics collected / maintained
    private long messagesReceived;
    private long faultsReceiving;
    private long timeoutsReceiving;
    private long bytesReceived;
    private long minSizeReceived;
    private long maxSizeReceived;
    private double avgSizeReceived;
    private final Map<Integer, Long> receivingFaultTable =
        Collections.synchronizedMap(new HashMap<Integer, Long>());

    private long messagesSent;
    private long faultsSending;
    private long timeoutsSending;
    private long bytesSent;
    private long minSizeSent;
    private long maxSizeSent;
    private double avgSizeSent;

    private int consecutiveSuspensions;
    private int consecutiveTimeouts;
    private int totalSuspensions;
    private int totalTimeouts;
    private AtomicInteger suspensions = new AtomicInteger(0);
    private AtomicInteger timeouts = new AtomicInteger(0);
    private Date suspendedAt;
    private Date timedoutAt;

    private final Map<Integer, Long> sendingFaultTable =
        Collections.synchronizedMap(new HashMap<Integer, Long>());

    private final Map<Integer, Long> responseCodeTable =
        Collections.synchronizedMap(new HashMap<Integer, Long>());

    private long lastResetTime = System.currentTimeMillis();

    private ScheduledFuture future;

    private Queue<Integer> suspensionCounts = new LinkedList<Integer>();
    private Queue<Integer> timeoutCounts = new LinkedList<Integer>();

    /**
     * Create a new MBean to manage the given endpoint
     * @param endpointName the name of the endpoint
     * @param endpoint the actual endpoint
     */
    public EndpointView(final String endpointName, Endpoint endpoint) {
        this.endpointName = endpointName;
        this.endpoint = endpoint;

        this.future = scheduler.scheduleAtFixedRate(new Runnable() {
            public void run() {
                if (suspensionCounts.size() == 15) {
                    suspensionCounts.remove();
                }
                suspensionCounts.offer(suspensions.getAndSet(0));

                if (timeoutCounts.size() == 15) {
                    timeoutCounts.remove();
                }
                timeoutCounts.offer(timeouts.getAndSet(0));
            }
        }, 60, 60, TimeUnit.SECONDS);
    }

    public void destroy() {
        future.cancel(true);
        suspensionCounts.clear();
        timeoutCounts.clear();
    }

    // --- endpoint control ---
    /**
     * Switch on a leaf endpoint, or all endpoints on a group - from maintenance
     * @throws Exception
     */
    public void switchOn() throws Exception {
        if (endpoint.getChildren() != null) {
            for (Endpoint e : endpoint.getChildren()) {
                if (e.getMetricsMBean() != null) {
                    e.getMetricsMBean().switchOn();
                }
            }
        } else {
            if (endpoint.getContext() != null) {
                endpoint.getContext().switchOn();
            }
        }
    }

    /**
     * Switch off a leaf endpoint, or all endpoints of a group - for maintenance
     *
     * @throws Exception
     */
    public void switchOff() throws Exception {
        if (endpoint.getChildren() != null) {
            for (Endpoint e : endpoint.getChildren()) {
                if (e.getMetricsMBean() != null) {
                    e.getMetricsMBean().switchOff();
                }
            }
        } else {
            if (endpoint.getContext() != null) {
                endpoint.getContext().switchOff();
            }
        }
    }

    // --- endpoint status check ---
    /**
     * Is a leaf level endpoint active? For a group endpoint this means at least one is active
     * @return true if at least one is active in a group endpoint; for a leaf - if it is currently active
     * @throws Exception
     */
    public boolean isActive() throws Exception {
        if (endpoint.getChildren() != null) {
            return getActiveChildren() > 0;
        } else if (endpoint.getContext() != null) {
            return endpoint.getContext().isState(EndpointContext.ST_ACTIVE);
        }
        return false;
    }

    /**
     * Is this leaf level endpoint in timeout state? For a group, has all endpoints timed out?
     * @return true if a leaf level endpoint has timed out, For a group, has all endpoints timed out?
     * @throws Exception
     */
    public boolean isTimedout() throws Exception {
        return isEndpointInState(EndpointContext.ST_TIMEOUT);
    }

    /**
     * Is this leaf level endpoint in suspend state?
     * @return true if a leaf level endpoint is suspended, false for group endpoints and non-suspend
     * @throws Exception
     */
    public boolean isSuspended() throws Exception {
        return isEndpointInState(EndpointContext.ST_SUSPENDED);
    }

    /**
     * Is this leaf level endpoint switched off?
     * @return true if a leaf level endpoint is off, false for group endpoints and non-off
     * @throws Exception
     */
    public boolean isSwitchedOff() throws Exception {
        return isEndpointInState(EndpointContext.ST_OFF);
    }

    /**
     * Return number of children for this endpoint
     * @return the number of children for this endpoint
     * @throws Exception
     */
    public int getTotalChildren() throws Exception {
        return (endpoint.getChildren() == null ? 0 : endpoint.getChildren().size());
    }

    /**
     * Return the number of active children for this endpoint
     * @return the number of active children for this endpoint
     * @throws Exception
     */
    public int getActiveChildren() throws Exception {
        if (endpoint.getChildren() == null) {
            return 0;
        } else {
            int activeCount = 0;
            for (Endpoint e : endpoint.getChildren()) {
                if (e.getContext().isState(EndpointContext.ST_ACTIVE)) {
                    activeCount++;
                }
            }
            return activeCount;
        }
    }

    /**
     * Return the number of ready children for this endpoint
     * @return the number of ready children for this endpoint
     * @throws Exception
     */
    public int getReadyChildren() throws Exception {
        if (endpoint.getChildren() == null) {
            return 0;
        } else {
            int readyCount = 0;
            for (Endpoint e : endpoint.getChildren()) {
                if (e.getContext().readyToSend()) {
                    readyCount++;
                }
            }
            return readyCount;
        }
    }

    // --- endpoint metrics ---
    /**
     * Time when statistics was last reset for this leaf endpoint
     * @return the time when statistics was last reset for this leaf endpoint, or -1 for group endpoints
     */
    public long getLastResetTime() {
        return (endpoint.getChildren() != null ? -1 : lastResetTime);
    }

    /**
     * Time since statistics was last reset for this leaf endpoint
     * @return the time since statistics was last reset for this leaf endpoint, or -1 for group endpoints
     */
    public long getMetricsWindow() {
        return (endpoint.getChildren() != null ? -1 : System.currentTimeMillis() - lastResetTime);
    }

    /**
     * A Map of receive faults with the error code and count
     * @return a Map of receive faults
     */
    public Map<Integer, Long> getReceivingFaultTable() {
        if (endpoint.getChildren() != null) {
            Map<Integer, Long> receivingFaultTable = new HashMap<Integer, Long>();
            for (Endpoint e : endpoint.getChildren()) {
                if (e.getMetricsMBean() != null) {
                    addTableMaps(receivingFaultTable, e.getMetricsMBean().getReceivingFaultTable());
                }
            }
            addTableMaps(receivingFaultTable, this.receivingFaultTable);
            return receivingFaultTable;
        } else {
            return receivingFaultTable;
        }
    }

    /**
     * A Map of send faults with the error code and count
     * @return a Map of send faults
     */
    public Map<Integer, Long> getSendingFaultTable() {
        if (endpoint.getChildren() != null) {
            Map<Integer, Long> sendingFaultTable = new HashMap<Integer, Long>();
            for (Endpoint e : endpoint.getChildren()) {
                if (e.getMetricsMBean() != null) {
                    addTableMaps(sendingFaultTable, e.getMetricsMBean().getSendingFaultTable());
                }
            }
            addTableMaps(sendingFaultTable, this.sendingFaultTable);
            return sendingFaultTable;
        } else {
            return sendingFaultTable;
        }
    }

    /**
     * A Map of response codes and counts
     * @return a Map of response codes and counts
     */
    public Map<Integer, Long> getResponseCodeTable() {
        if (endpoint.getChildren() != null) {
            Map<Integer, Long> responseCodeTable = new HashMap<Integer, Long>();
            for (Endpoint e : endpoint.getChildren()) {
                if (e.getMetricsMBean() != null) {
                    addTableMaps(responseCodeTable, e.getMetricsMBean().getResponseCodeTable());
                }
            }
            return responseCodeTable;
        } else {
            return responseCodeTable;
        }
    }

    public Date getSuspendedAt() {
        return suspendedAt;
    }

    public void setSuspendedAt(Date suspendedAt) {
        this.suspendedAt = suspendedAt;
    }

    public Date getTimedoutAt() {
        return timedoutAt;
    }

    public void setTimedoutAt(Date timedoutAt) {
        this.timedoutAt = timedoutAt;
    }

    public int getConsecutiveEndpointSuspensions() {
        return consecutiveSuspensions;
    }

    public void incrementSuspensions() {
        consecutiveSuspensions++;
        totalSuspensions++;
        suspensions.incrementAndGet();
    }

    public void resetConsecutiveSuspensions() {
        consecutiveSuspensions = 0;
    }

    public int getConsecutiveEndpointTimeouts() {
        return consecutiveTimeouts;
    }

    public void incrementTimeouts() {
        consecutiveTimeouts++;
        totalTimeouts++;
        timeouts.incrementAndGet();
    }

    public void resetConsecutiveTimeouts() {
        consecutiveTimeouts = 0;
    }

    public int getTotalEndpointSuspensions() {
        return totalSuspensions;
    }

    public int getTotalEndpointTimeouts() {
        return totalTimeouts;
    }

    public int getLastMinuteEndpointSuspensions() {
        return getTotal(suspensionCounts, 1);
    }

    public int getLast5MinuteEndpointSuspensions() {
        return getTotal(suspensionCounts, 5);
    }

    public int getLast15MinuteEndpointSuspensions() {
        return getTotal(suspensionCounts, 15);
    }

    public int getLastMinuteEndpointTimeouts() {
        return getTotal(timeoutCounts, 1);
    }

    public int getLast5MinuteEndpointTimeouts() {
        return getTotal(timeoutCounts, 5);
    }

    public int getLast15MinuteEndpointTimeouts() {
        return getTotal(timeoutCounts, 15);
    }

    private int getTotal(Queue<Integer> queue, int count) {
        int sum = 0;
        Integer[] array = queue.toArray(new Integer[queue.size()]);

        if (count > array.length) {
            for (int i = 0; i < array.length; i++) {
                sum +=array[i];
            }
        } else {
            for (int i = 0; i < count; i++) {
                sum += array[array.length - 1 - i];
            }
        }
        return sum;
    }

    /**
     * Number of messages (ie replies) received
     * @return # of messages (replies) received
     */
    public long getMessagesReceived() {
        if (endpoint.getChildren() != null) {
            long messagesReceived = 0;
            for (Endpoint e : endpoint.getChildren()) {
                if (e.getMetricsMBean() != null) {
                    messagesReceived += e.getMetricsMBean().getMessagesReceived();
                }
            }
            return messagesReceived;
        } else {
            return messagesReceived;
        }
    }

    /**
     * Number of faults, receiving replies
     * @return # of faults, receiving replies
     */
    public long getFaultsReceiving() {
        if (endpoint.getChildren() != null) {
            long faultsReceiving = 0;
            for (Endpoint e : endpoint.getChildren()) {
                if (e.getMetricsMBean() != null) {
                    faultsReceiving += e.getMetricsMBean().getFaultsReceiving();
                }
            }
            return faultsReceiving;
        } else {
            return faultsReceiving;
        }
    }

    /**
     * Number of timeouts, receiving replies
     * @return # of timeouts, receiving replies
     */
    public long getTimeoutsReceiving() {
        if (endpoint.getChildren() != null) {
            long timeoutsReceiving = 0;
            for (Endpoint e : endpoint.getChildren()) {
                if (e.getMetricsMBean() != null) {
                    timeoutsReceiving += e.getMetricsMBean().getTimeoutsReceiving();
                }
            }
            return timeoutsReceiving;
        } else {
            return timeoutsReceiving;
        }
    }

    /**
     * Number of bytes received, receiving replies
     * @return # of bytes received, receiving replies
     */
    public long getBytesReceived() {
        if (endpoint.getChildren() != null) {
            long bytesReceived = 0;
            for (Endpoint e : endpoint.getChildren()) {
                if (e.getMetricsMBean() != null) {
                    bytesReceived += e.getMetricsMBean().getBytesReceived();
                }
            }
            return bytesReceived;
        } else {
            return bytesReceived;
        }
    }

    /**
     * Number of messages sent
     * @return # of messages sent
     */
    public long getMessagesSent() {
        if (endpoint.getChildren() != null) {
            long messagesSent = 0;
            for (Endpoint e : endpoint.getChildren()) {
                if (e.getMetricsMBean() != null) {
                    messagesSent += e.getMetricsMBean().getMessagesSent();
                }
            }
            return messagesSent;
        } else {
            return messagesSent;
        }
    }

    /**
     * Number of faults sending
     * @return # of faults sending
     */
    public long getFaultsSending() {
        if (endpoint.getChildren() != null) {
            long faultsSending = 0;
            for (Endpoint e : endpoint.getChildren()) {
                if (e.getMetricsMBean() != null) {
                    faultsSending += e.getMetricsMBean().getFaultsSending();
                }
            }
            return faultsSending;
        } else {
            return faultsSending;
        }
    }

    /**
     * Number of timeouts, sending
     * @return # of timeouts, sending
     */
    public long getTimeoutsSending() {
        if (endpoint.getChildren() != null) {
            long timeoutsSending = 0;
            for (Endpoint e : endpoint.getChildren()) {
                if (e.getMetricsMBean() != null) {
                    timeoutsSending += e.getMetricsMBean().getTimeoutsSending();
                }
            }
            return timeoutsSending;
        } else {
            return timeoutsSending;
        }
    }

    /**
     * Number of bytes sent
     * @return # of bytes sent
     */
    public long getBytesSent() {
        if (endpoint.getChildren() != null) {
            long bytesSent = 0;
            for (Endpoint e : endpoint.getChildren()) {
                if (e.getMetricsMBean() != null) {
                    bytesSent += e.getMetricsMBean().getBytesSent();
                }
            }
            return bytesSent;
        } else {
            return bytesSent;
        }
    }

    public long getMinSizeReceived() {
        if (endpoint.getChildren() != null) {
            long minSizeReceived = 0;
            for (Endpoint e : endpoint.getChildren()) {
                if (e.getMetricsMBean() != null) {
                    if (minSizeReceived == 0) {
                        minSizeReceived = e.getMetricsMBean().getMinSizeReceived();
                    } else if (e.getMetricsMBean().getMinSizeReceived() < minSizeReceived) {
                        minSizeReceived = e.getMetricsMBean().getMinSizeReceived();
                    }
                }
            }
            return minSizeReceived;
        } else {
            return minSizeReceived;
        }
    }

    public long getMaxSizeReceived() {
        if (endpoint.getChildren() != null) {
            long maxSizeReceived = 0;
            for (Endpoint e : endpoint.getChildren()) {
                if (e.getMetricsMBean() != null) {
                    if (maxSizeReceived == 0) {
                        maxSizeReceived = e.getMetricsMBean().getMaxSizeReceived();
                    } else if (e.getMetricsMBean().getMaxSizeReceived() > maxSizeReceived) {
                        maxSizeReceived = e.getMetricsMBean().getMaxSizeReceived();
                    }
                }
            }
            return maxSizeReceived;
        } else {
            return maxSizeReceived;
        }
    }

    public long getMinSizeSent() {
        if (endpoint.getChildren() != null) {
            long minSizeSent = 0;
            for (Endpoint e : endpoint.getChildren()) {
                if (e.getMetricsMBean() != null) {
                    if (minSizeSent == 0) {
                        minSizeSent = e.getMetricsMBean().getMinSizeSent();
                    } else if (e.getMetricsMBean().getMinSizeSent() < minSizeSent) {
                        minSizeSent = e.getMetricsMBean().getMinSizeSent();
                    }
                }
            }
            return minSizeSent;
        } else {
            return minSizeSent;
        }
    }

    public long getMaxSizeSent() {
        if (endpoint.getChildren() != null) {
            long maxSizeSent = 0;
            for (Endpoint e : endpoint.getChildren()) {
                if (e.getMetricsMBean() != null) {
                    if (maxSizeSent == 0) {
                        maxSizeSent = e.getMetricsMBean().getMaxSizeSent();
                    } else if (e.getMetricsMBean().getMaxSizeSent() > maxSizeSent) {
                        maxSizeSent = e.getMetricsMBean().getMaxSizeSent();
                    }
                }
            }
            return maxSizeSent;
        } else {
            return maxSizeSent;
        }
    }

    public double getAvgSizeReceived() {
        if (endpoint.getChildren() != null) {
            double avgSizeReceived = 0;
            for (Endpoint e : endpoint.getChildren()) {
                double epValue =
                    e.getMetricsMBean() == null ? 0 : e.getMetricsMBean().getAvgSizeReceived();
                if (epValue > 0) {
                    avgSizeReceived =
                        (avgSizeReceived == 0 ? epValue : (avgSizeReceived + epValue) / 2);
                }
            }
            return avgSizeReceived;
        } else {
            return avgSizeReceived;
        }
    }

    public double getAvgSizeSent() {
        if (endpoint.getChildren() != null) {
            double avgSizeSent = 0;
            for (Endpoint e : endpoint.getChildren()) {
                double epValue =
                    e.getMetricsMBean() == null ? 0 : e.getMetricsMBean().getAvgSizeSent();
                if (epValue > 0) {
                    avgSizeSent = (avgSizeSent == 0 ? epValue : (avgSizeSent + epValue) / 2);
                }
            }
            return avgSizeSent;
        } else {
            return avgSizeSent;
        }
    }

    // --- MessageLevelMetricsCollector methods ---
    public void resetStatistics() {

        messagesReceived  = 0;
        faultsReceiving   = 0;
        timeoutsReceiving = 0;
        bytesReceived     = 0;
        minSizeReceived   = 0;
        maxSizeReceived   = 0;
        avgSizeReceived   = 0;
        receivingFaultTable.clear();

        messagesSent      = 0;
        faultsSending     = 0;
        timeoutsSending   = 0;
        bytesSent         = 0;
        minSizeSent       = 0;
        maxSizeSent       = 0;
        avgSizeSent       = 0;
        sendingFaultTable.clear();

        responseCodeTable.clear();
        lastResetTime = System.currentTimeMillis();

        if (endpoint.getChildren() != null) {
            for (Endpoint e : endpoint.getChildren()) {
                if (e.getMetricsMBean() != null) {
                    e.getMetricsMBean().resetStatistics();
                }
            }
        }
        log.info("Endpoint statistics reset for : " + endpointName + " (and/or its children)");
    }

    public synchronized void incrementMessagesReceived() {
        messagesReceived++;
    }

    public synchronized void incrementFaultsReceiving(int errorCode) {
        faultsReceiving++;
        Object o = receivingFaultTable.get(errorCode);
        if (o == null) {
            receivingFaultTable.put(errorCode, ONE);
        } else {
            receivingFaultTable.put(errorCode, (Long) o + 1);
        }
    }

    public synchronized void incrementTimeoutsReceiving() {
        timeoutsReceiving++;
    }

    public synchronized void incrementBytesReceived(long size) {
        bytesReceived += size;
    }

    public synchronized void incrementMessagesSent() {
        messagesSent++;
    }

    public synchronized void incrementFaultsSending(int errorCode) {
        faultsSending++;
        Object o = sendingFaultTable.get(errorCode);
        if (o == null) {
            sendingFaultTable.put(errorCode, ONE);
        } else {
            sendingFaultTable.put(errorCode, (Long) o + 1);
        }
    }

    public synchronized void incrementTimeoutsSending() {
        timeoutsSending++;
    }

    public synchronized void incrementBytesSent(long size) {
        bytesSent += size;
    }

    public synchronized void notifyReceivedMessageSize(long size) {
        if (minSizeReceived == 0 || size < minSizeReceived) {
            minSizeReceived = size;
        }
        if (size > maxSizeReceived) {
            maxSizeReceived = size;
        }
        avgSizeReceived = (avgSizeReceived == 0 ? size : (avgSizeReceived + size) / 2);
    }

    public synchronized void notifySentMessageSize(long size) {
        if (minSizeSent == 0 || size < minSizeSent) {
            minSizeSent = size;
        }
        if (size > maxSizeSent) {
            maxSizeSent = size;
        }
        avgSizeSent = (avgSizeSent == 0 ? size : (avgSizeSent + size) / 2);
    }

    /**
     * Report a/an [typically non-fatal] error to the sending fault table, without incrementing
     * the sendingFault count e.g. to report a successful fail-over etc
     * @param errorCode the code to report
     */
    public void reportSendingFault(int errorCode) {
        synchronized(sendingFaultTable) {
            Object o = sendingFaultTable.get(errorCode);
            if (o == null) {
                sendingFaultTable.put(errorCode, ONE);
            } else {
                sendingFaultTable.put(errorCode, (Long) o + 1);
            }
        }
    }

    /**
     * Report a/an [typically non-fatal] error to the receiving fault table, without incrementing
     * the receivingFault count
     * @param errorCode the code to report
     */
    public void reportReceivingFault(int errorCode) {
        synchronized(receivingFaultTable) {
            Object o = receivingFaultTable.get(errorCode);
            if (o == null) {
                receivingFaultTable.put(errorCode, ONE);
            } else {
                receivingFaultTable.put(errorCode, (Long) o + 1);
            }
        }
    }

    /**
     * Collect response code statistics
     * @param respCode response code
     */
    public void reportResponseCode(int respCode) {
        synchronized(responseCodeTable) {
            Object o = responseCodeTable.get(respCode);
            if (o == null) {
                responseCodeTable.put(respCode, ONE);
            } else {
                responseCodeTable.put(respCode, (Long) o + 1);
            }
        }
    }

    //---------- utility methods ---------------
    private static void addTableMaps(Map<Integer, Long> t, Map<Integer, Long> s) {
        for (Map.Entry<Integer, Long> o : s.entrySet()) {
            if (t.containsKey(o.getKey())) {
                t.put(o.getKey(), o.getValue() + s.get(o.getKey()));
            } else {
                t.put(o.getKey(), s.get(o.getKey()));
            }
        }
    }

    /**
     * Is the endpoint considered to be in the given state?
     * @param state the state to consider
     * @return true if all endpoints in a group are of the given state, or if a leaf endpoint is in the given state
     */
    public boolean isEndpointInState(int state) {
        if (endpoint.getChildren() != null) {
            int count = 0, total = 0;
            for (Endpoint e : endpoint.getChildren()) {
                if (e.getContext().isState(state)) {
                    count++;
                }
                total++;
            }
            return count == total;

        } else if (endpoint.getContext() != null) {
            return endpoint.getContext().isState(state);
        }
        return false;
    }

}
