/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.healthcheck.probe;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Function;

/**
 * Counts how many worker threads the throttle event receiver has left, for its TCP (publish, 9611 + offset) and
 * SSL (login, 9711 + offset) pools.
 *
 * <p>The binary receiver gives each open connection one thread from a fixed-size pool for as long as the
 * connection stays open. When every thread is taken it stops accepting work: the port still accepts a new TCP
 * connection, but nothing ever reads from it. So a port check alone cannot see this - the thread count can.</p>
 *
 * <p>The pools are private to the receiver, so they are not read directly. Instead their threads are found by name
 * ({@value #TCP_THREAD_NAME_PREFIX}..., {@value #SSL_THREAD_NAME_PREFIX}...) through the JVM's own
 * {@link ThreadMXBean}: a thread waiting for its next task inside {@link ThreadPoolExecutor} is idle, any other is
 * serving a connection. Pool threads are created only when first needed, so a thread that does not exist yet is
 * counted as free. The configured pool sizes come from the receiver's own configuration.</p>
 *
 * <p>Nothing is cached - every call looks at the threads as they are right now.</p>
 */
public class ReceiverThreadPoolMonitor implements ReceiverThreadPoolMXBean {

    private static final Log log = LogFactory.getLog(ReceiverThreadPoolMonitor.class);

    public static final String OBJECT_NAME =
            "org.wso2.carbon.apimgt:type=TrafficManagerHealth,name=ThrottleReceiverThreadPools";

    static final String TCP_POOL = "tcp";
    static final String SSL_POOL = "ssl";

    /**
     * Thread names given by the receiver's {@code DataBridgeThreadFactory}, e.g.
     * {@code DataBridge-Receiver-Binary-TCP-pool-3-thread-7}.
     */
    static final String TCP_THREAD_NAME_PREFIX = "DataBridge-Receiver-Binary-TCP-pool-";
    static final String SSL_THREAD_NAME_PREFIX = "DataBridge-Receiver-Binary-SSL-pool-";

    /**
     * Receiver configuration keys for the pool sizes, in {@code data-bridge-config.xml}.
     */
    public static final String TCP_POOL_SIZE_CONFIG = "tcpReceiverThreadPoolSize";
    public static final String SSL_POOL_SIZE_CONFIG = "sslReceiverThreadPoolSize";

    /**
     * Deep enough to reach {@code ThreadPoolExecutor.getTask} from the parking frames above it.
     */
    private static final int STACK_DEPTH = 12;
    private static final String IDLE_CLASS = ThreadPoolExecutor.class.getName();
    private static final String IDLE_METHOD = "getTask";

    private final Function<String, Integer> poolSizeLookup;

    /**
     * @param poolSizeLookup returns the configured size for {@link #TCP_POOL_SIZE_CONFIG} or
     *                       {@link #SSL_POOL_SIZE_CONFIG}, or {@code null} if it cannot be read
     */
    public ReceiverThreadPoolMonitor(Function<String, Integer> poolSizeLookup) {

        this.poolSizeLookup = poolSizeLookup;
    }

    /**
     * @return the TCP pool's usage, then the SSL pool's
     */
    public List<ThreadPoolUsage> snapshot() {

        int tcpBusy = 0;
        int sslBusy = 0;
        for (ReceiverThread thread : sampleReceiverThreads()) {
            if (thread.isIdle()) {
                continue;
            }
            if (thread.getName().startsWith(TCP_THREAD_NAME_PREFIX)) {
                tcpBusy++;
            } else if (thread.getName().startsWith(SSL_THREAD_NAME_PREFIX)) {
                sslBusy++;
            }
        }
        return Arrays.asList(new ThreadPoolUsage(TCP_POOL, poolSize(TCP_POOL_SIZE_CONFIG), tcpBusy),
                new ThreadPoolUsage(SSL_POOL, poolSize(SSL_POOL_SIZE_CONFIG), sslBusy));
    }

    /**
     * {@code DOWN} if any pool has no free threads left; {@code UP} if every pool still has at least one, however
     * few; {@code UNKNOWN} if no pool size could be read.
     */
    String statusOf(List<ThreadPoolUsage> usages) {

        String status = TMHealthChecker.STATUS_UNKNOWN;
        for (ThreadPoolUsage usage : usages) {
            if (!usage.isKnown()) {
                continue;
            }
            if (usage.getFree() == 0) {
                return TMHealthChecker.STATUS_DOWN;
            }
            status = TMHealthChecker.STATUS_UP;
        }
        return status;
    }

    /**
     * Finds the receiver's pool threads by name, then reads only those threads' stacks. Seam for tests.
     */
    protected List<ReceiverThread> sampleReceiverThreads() {

        List<ReceiverThread> threads = new ArrayList<>();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        List<Long> poolThreadIds = new ArrayList<>();
        for (ThreadInfo info : threadMXBean.getThreadInfo(threadMXBean.getAllThreadIds(), 0)) {
            if (info != null && isReceiverThread(info.getThreadName())) {
                poolThreadIds.add(info.getThreadId());
            }
        }
        if (poolThreadIds.isEmpty()) {
            return threads;
        }
        long[] ids = new long[poolThreadIds.size()];
        for (int i = 0; i < ids.length; i++) {
            ids[i] = poolThreadIds.get(i);
        }
        // A thread that ended between the two calls comes back as null and is simply not counted.
        for (ThreadInfo info : threadMXBean.getThreadInfo(ids, STACK_DEPTH)) {
            if (info != null) {
                threads.add(new ReceiverThread(info.getThreadName(), isWaitingForTask(info.getStackTrace())));
            }
        }
        return threads;
    }

    private int poolSize(String configName) {

        try {
            Integer size = poolSizeLookup.apply(configName);
            return size == null || size <= 0 ? ThreadPoolUsage.UNKNOWN_COUNT : size;
        } catch (RuntimeException e) {
            log.warn("Could not read the throttle receiver's " + configName, e);
            return ThreadPoolUsage.UNKNOWN_COUNT;
        }
    }

    private static boolean isReceiverThread(String name) {

        return name != null && (name.startsWith(TCP_THREAD_NAME_PREFIX) || name.startsWith(SSL_THREAD_NAME_PREFIX));
    }

    private static boolean isWaitingForTask(StackTraceElement[] stack) {

        for (StackTraceElement frame : stack) {
            if (IDLE_CLASS.equals(frame.getClassName()) && IDLE_METHOD.equals(frame.getMethodName())) {
                return true;
            }
        }
        return false;
    }

    private ThreadPoolUsage usage(String pool) {

        for (ThreadPoolUsage usage : snapshot()) {
            if (usage.getPool().equals(pool)) {
                return usage;
            }
        }
        return new ThreadPoolUsage(pool, ThreadPoolUsage.UNKNOWN_COUNT, 0);
    }

    @Override
    public String getStatus() {

        return statusOf(snapshot());
    }

    @Override
    public int getTcpPoolSize() {

        return usage(TCP_POOL).getSize();
    }

    @Override
    public int getTcpBusyThreads() {

        return usage(TCP_POOL).getBusy();
    }

    @Override
    public int getTcpFreeThreads() {

        return usage(TCP_POOL).getFree();
    }

    @Override
    public int getSslPoolSize() {

        return usage(SSL_POOL).getSize();
    }

    @Override
    public int getSslBusyThreads() {

        return usage(SSL_POOL).getBusy();
    }

    @Override
    public int getSslFreeThreads() {

        return usage(SSL_POOL).getFree();
    }

    /**
     * One receiver pool thread: its name, and whether it is waiting for work.
     */
    static final class ReceiverThread {

        private final String name;
        private final boolean idle;

        ReceiverThread(String name, boolean idle) {

            this.name = name;
            this.idle = idle;
        }

        String getName() {

            return name;
        }

        boolean isIdle() {

            return idle;
        }
    }
}
