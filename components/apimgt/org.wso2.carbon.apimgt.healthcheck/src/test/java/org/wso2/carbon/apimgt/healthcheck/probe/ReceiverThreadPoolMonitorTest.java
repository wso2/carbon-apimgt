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

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

public class ReceiverThreadPoolMonitorTest {

    @Test
    public void countsOnlyThreadsServingAConnectionAsBusy() {

        ReceiverThreadPoolMonitor monitor = fixedMonitor(100, 100, 3, 1);

        List<ThreadPoolUsage> usages = monitor.snapshot();

        Assert.assertEquals("tcp", usages.get(0).getPool());
        Assert.assertEquals(100, usages.get(0).getSize());
        Assert.assertEquals(3, usages.get(0).getBusy());
        Assert.assertEquals(97, usages.get(0).getFree());
        Assert.assertEquals("ssl", usages.get(1).getPool());
        Assert.assertEquals(99, usages.get(1).getFree());
        Assert.assertEquals("UP", monitor.statusOf(usages));
    }

    @Test
    public void reportsUpWhileEvenOneThreadIsLeft() {

        ReceiverThreadPoolMonitor monitor = fixedMonitor(100, 100, 99, 0);

        List<ThreadPoolUsage> usages = monitor.snapshot();

        Assert.assertEquals(1, usages.get(0).getFree());
        Assert.assertEquals("UP", monitor.statusOf(usages));
    }

    @Test
    public void reportsDownWhenEitherPoolHasNoThreadsLeft() {

        Assert.assertEquals("DOWN", fixedMonitor(100, 100, 100, 0).getStatus());
        Assert.assertEquals("DOWN", fixedMonitor(100, 10, 0, 10).getStatus());
        Assert.assertEquals(0, fixedMonitor(100, 10, 0, 10).getSslFreeThreads());
    }

    @Test
    public void reportsUnknownWhenThePoolSizesCannotBeRead() {

        ReceiverThreadPoolMonitor monitor = new ReceiverThreadPoolMonitor(configName -> null) {

            @Override
            protected List<ReceiverThread> sampleReceiverThreads() {

                return new ArrayList<>();
            }
        };

        Assert.assertEquals("UNKNOWN", monitor.getStatus());
        Assert.assertEquals(-1, monitor.getTcpPoolSize());
        Assert.assertEquals(-1, monitor.getTcpBusyThreads());
        Assert.assertEquals(-1, monitor.getTcpFreeThreads());
    }

    @Test
    public void reportsUnknownInsteadOfFailingWhenThePoolSizeLookupThrows() {

        ReceiverThreadPoolMonitor monitor = new ReceiverThreadPoolMonitor(configName -> {
            throw new IllegalStateException("simulated");
        });

        Assert.assertEquals("UNKNOWN", monitor.getStatus());
    }

    /**
     * Uses real threads named like the receiver's, so the actual JVM thread inspection is what decides busy vs
     * idle: two threads are held by a task (like an open connection), the rest wait for work.
     */
    @Test
    public void tellsBusyFromIdleThreadsOfARealThreadPool() throws InterruptedException {

        AtomicInteger threadNumber = new AtomicInteger();
        ThreadPoolExecutor pool = new ThreadPoolExecutor(5, 5, 0, TimeUnit.SECONDS, new LinkedBlockingQueue<>(),
                runnable -> {
                    Thread thread = new Thread(runnable, ReceiverThreadPoolMonitor.TCP_THREAD_NAME_PREFIX
                            + "99-thread-" + threadNumber.incrementAndGet());
                    thread.setDaemon(true);
                    return thread;
                });
        CountDownLatch started = new CountDownLatch(2);
        CountDownLatch release = new CountDownLatch(1);
        try {
            pool.prestartAllCoreThreads();
            for (int i = 0; i < 2; i++) {
                pool.execute(() -> {
                    started.countDown();
                    try {
                        release.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
            Assert.assertTrue(started.await(5, TimeUnit.SECONDS));
            ReceiverThreadPoolMonitor monitor = new ReceiverThreadPoolMonitor(configName -> 5);

            // Idle threads only reach their wait shortly after starting, so allow a moment to settle.
            long giveUpAt = System.nanoTime() + TimeUnit.SECONDS.toNanos(5);
            while (monitor.getTcpBusyThreads() != 2 && System.nanoTime() < giveUpAt) {
                Thread.sleep(20);
            }
            Assert.assertEquals(2, monitor.getTcpBusyThreads());
            Assert.assertEquals(3, monitor.getTcpFreeThreads());
            Assert.assertEquals(0, monitor.getSslBusyThreads());
        } finally {
            release.countDown();
            pool.shutdownNow();
        }
    }

    @Test
    public void canBeRegisteredAndReadAsAnMBean() throws Exception {

        MBeanServer mBeanServer = MBeanServerFactory.newMBeanServer();
        ObjectName objectName = new ObjectName(ReceiverThreadPoolMonitor.OBJECT_NAME);

        mBeanServer.registerMBean(fixedMonitor(100, 50, 40, 0), objectName);

        Assert.assertEquals(60, mBeanServer.getAttribute(objectName, "TcpFreeThreads"));
        Assert.assertEquals(40, mBeanServer.getAttribute(objectName, "TcpBusyThreads"));
        Assert.assertEquals(50, mBeanServer.getAttribute(objectName, "SslPoolSize"));
        Assert.assertEquals("UP", mBeanServer.getAttribute(objectName, "Status"));
    }

    /**
     * A monitor whose pools have the given sizes and busy-thread counts, plus one idle thread in each pool.
     */
    static ReceiverThreadPoolMonitor fixedMonitor(int tcpSize, int sslSize, int tcpBusy, int sslBusy) {

        Map<String, Integer> sizes = new HashMap<>();
        sizes.put(ReceiverThreadPoolMonitor.TCP_POOL_SIZE_CONFIG, tcpSize);
        sizes.put(ReceiverThreadPoolMonitor.SSL_POOL_SIZE_CONFIG, sslSize);
        return new ReceiverThreadPoolMonitor(sizes::get) {

            @Override
            protected List<ReceiverThread> sampleReceiverThreads() {

                List<ReceiverThread> threads = new ArrayList<>();
                addThreads(threads, TCP_THREAD_NAME_PREFIX, tcpBusy);
                addThreads(threads, SSL_THREAD_NAME_PREFIX, sslBusy);
                threads.add(new ReceiverThread("unrelated-thread", false));
                return threads;
            }
        };
    }

    private static void addThreads(List<ReceiverThreadPoolMonitor.ReceiverThread> threads, String prefix,
                                   int busy) {

        for (int i = 0; i < busy; i++) {
            threads.add(new ReceiverThreadPoolMonitor.ReceiverThread(prefix + "1-thread-" + i, false));
        }
        threads.add(new ReceiverThreadPoolMonitor.ReceiverThread(prefix + "1-thread-idle", true));
    }
}
