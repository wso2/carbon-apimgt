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

package org.apache.synapse.commons.executors;

import java.util.List;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Stree Test the MultiPriorityBlockingQueue. This test uses threads to insert and remove
 * items concurrently.
 */
public class MultiPriorityBlockingQueueStressTest extends MultiPriorityBlockingQueueAbstractTest {
    private static final int CONCURRENT_ITEMS = 10000;

    private final int[] sizes = {CONCURRENT_ITEMS, CONCURRENT_ITEMS, CONCURRENT_ITEMS};
    private final int[] priorities = {1, 10, 100};
    private final int QUEUES = 3;

    private MultiPriorityBlockingQueue fixedQueue;
    private MultiPriorityBlockingQueue unboundedQueue;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        fixedQueue = createFixedQueue(QUEUES, sizes, priorities);

        unboundedQueue = createUnboundedQueue(QUEUES, priorities);
    }

    public void testOffer() {
        List<InsertItemThread> list = startConcurrentOffer(fixedQueue, QUEUES, priorities, sizes);

        for (InsertItemThread t : list) {
            while (!t.isFinished()) {}
        }

        assertEquals(QUEUES * CONCURRENT_ITEMS, fixedQueue.size());

        fixedQueue.clear();

        assertEquals(0, fixedQueue.size());
    }

    public void testUnboundedQueueOffer() {
        List<InsertItemThread> list  = startConcurrentOffer(unboundedQueue, 3, priorities, sizes);

        for (InsertItemThread t : list) {
            while (!t.isFinished()) {}
        }

        assertEquals(QUEUES * CONCURRENT_ITEMS, unboundedQueue.size());

        unboundedQueue.clear();

        assertEquals(0, unboundedQueue.size());
    }

    public void testTake() {
        List<InsertItemThread> offers = startConcurrentOffer(fixedQueue, QUEUES, priorities, sizes);
        List<RemoveItemsThread> takes = startConcurrentTake(fixedQueue, QUEUES, sizes);

        for (InsertItemThread t : offers) {
            while (!t.isFinished()) {}
        }

        for (RemoveItemsThread t : takes) {
            while (!t.isFinished()) {}
        }

        assertEquals(0, fixedQueue.size());
    }

     public void testUnbounderQueueTake() {
        List<InsertItemThread> offers = startConcurrentOffer(unboundedQueue, QUEUES, priorities, sizes);
        List<RemoveItemsThread> takes = startConcurrentTake(unboundedQueue, QUEUES, sizes);

        for (InsertItemThread t : offers) {
            while (!t.isFinished()) {}
        }

        for (RemoveItemsThread t : takes) {
            while (!t.isFinished()) {}
        }

        assertEquals(0, unboundedQueue.size());
    }

    public void testRemainingCapacity() {
        List<InsertItemThread> offers = startConcurrentOffer(fixedQueue, QUEUES, priorities, sizes);

        int[] s1 = {CONCURRENT_ITEMS - 1000, CONCURRENT_ITEMS - 1000, CONCURRENT_ITEMS - 1000};
        List<RemoveItemsThread> takes = startConcurrentTake(fixedQueue, QUEUES, s1);

        for (InsertItemThread t : offers) {
            while (!t.isFinished()) {}
        }

        for (RemoveItemsThread t : takes) {
            while (!t.isFinished()) {}
        }

        assertEquals(CONCURRENT_ITEMS * QUEUES - 3000, fixedQueue.remainingCapacity());

        int[] s2 = {1000, 1000, 1000};
        takes = startConcurrentTake(fixedQueue, QUEUES, s2);

        for (RemoveItemsThread t : takes) {
            while (!t.isFinished()) {}
        }

        assertEquals(CONCURRENT_ITEMS * QUEUES, fixedQueue.remainingCapacity());
    }

     private List<InsertItemThread> startConcurrentOffer(
             MultiPriorityBlockingQueue queue, int threads, int []priorities, int sizes[]) {
        List<InsertItemThread> threadList = new ArrayList<InsertItemThread>();
        for (int i = 0; i < threads; i++) {
            InsertItemThread t = new InsertItemThread(queue, priorities[i], sizes[i]);
            t.start();
            threadList.add(t);
        }

        return threadList;
    }

    private List<RemoveItemsThread> startConcurrentTake(MultiPriorityBlockingQueue queue,
                                                        int threads, int []sizes) {
        List<RemoveItemsThread> threadList = new ArrayList<RemoveItemsThread>();
        for (int i = 0; i < threads; i++) {
            RemoveItemsThread t = new RemoveItemsThread(queue, sizes[i]);
            t.start();
            threadList.add(t);
        }
        return threadList;
    }

    public static class InsertItemThread extends Thread {
            private MultiPriorityBlockingQueue queue = null;

            private int max = 0;
            private int priority = 0;

            private AtomicBoolean finished = new AtomicBoolean(false);

            public InsertItemThread(MultiPriorityBlockingQueue queue, int priority, int max) {
                this.queue = queue;
                this.max = max;
                this.priority = priority;

            }

            public void run() {
                for (int i = 0; i < max; i++) {
                    boolean offer = queue.offer(new DummyTask(priority));
                    assertTrue("Offer should be successful", offer);
                }
                finished.getAndSet(true);
            }

            public boolean isFinished() {
                return finished.get();
            }
        }

    public static class RemoveItemsThread extends Thread {
        private MultiPriorityBlockingQueue queue = null;

        private int max = 0;

        private AtomicBoolean finished = new AtomicBoolean(false);

        public RemoveItemsThread(MultiPriorityBlockingQueue queue, int max) {
            this.queue = queue;
            this.max = max;
        }

        public void run() {
            for (int i = 0; i < max; i++) {
                try {
                    Object o = queue.take();
                    assertNotNull("Take should return a valid object", o);
                } catch (InterruptedException e) {
                    assertFalse("This exception cannot occur: " + e.getMessage(), true);
                }
            }
            finished.getAndSet(true);
        }

        public boolean isFinished() {
            return finished.get();
        }
    }
}
