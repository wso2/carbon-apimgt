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

package org.apache.synapse.commons.executors.queues;

import junit.framework.TestCase;
import org.apache.synapse.commons.executors.DummyTask;

import java.util.List;
import java.util.ArrayList;
import java.util.NoSuchElementException;


public class FixedQueueTest extends TestCase {

    private FixedSizeQueue<DummyTask> queue;

    private final int SIZE = 10;
    private final int PRIORITY = 10;

    @Override
    protected void setUp() throws Exception {
        super.setUp();

        queue = new FixedSizeQueue<DummyTask>(PRIORITY, SIZE);
    }

    public void testOffer() {
        queue.offer(new DummyTask(PRIORITY));

        assertEquals(queue.size(), 1);

        for (int i = 0; i < SIZE; i++) {
            queue.offer(new DummyTask(SIZE));
        }

        assertEquals(queue.size(), SIZE);

        queue.clear();

        assertEquals(queue.size(), 0);
    }

    public void testPeek() {
        DummyTask task = new DummyTask(PRIORITY);
        queue.offer(task);

        assertEquals(queue.peek(), task);

        for (int i = 0; i < SIZE/2; i++) {
            queue.offer(new DummyTask(SIZE));
        }
        
        queue.offer(task);

        assertEquals(queue.peek(), task);
        queue.clear();

        assertTrue(null == queue.peek());
    }

    public void testAdd() {
        DummyTask task = new DummyTask(PRIORITY);
        queue.add(task);

        assertEquals(queue.peek(), task);

        for (int i = 0; i < SIZE/2; i++) {
            queue.add(new DummyTask(SIZE));
        }

        queue.add(task);

        assertEquals(queue.peek(), task);

        boolean exceptionOccurred = false;
        try {
            for (int i = 0; i < SIZE; i++) {
                queue.add(new DummyTask(SIZE));
            }
        } catch (IllegalStateException e) {
            exceptionOccurred = true;
        }

        assertTrue(exceptionOccurred);
        queue.clear();
    }

    public void testRemove() {
        DummyTask task = new DummyTask(PRIORITY);
        queue.offer(task);

        assertEquals(queue.peek(), task);
        assertEquals(queue.remove(), task);
        assertEquals(queue.size(), 0);

        for (int i = 0; i < SIZE; i++) {
            queue.offer(new DummyTask(SIZE));
        }
        
        queue.remove();

        assertEquals(queue.size(), SIZE - 1);

        queue.clear();
    }

    public void testRemoveObject() {
        DummyTask task = new DummyTask(PRIORITY);
        queue.offer(task);

        assertEquals(queue.peek(), task);
        assertTrue(queue.remove(task));
        assertEquals(queue.size(), 0);

        List<DummyTask> tasks = new ArrayList<DummyTask>();
        for (int i = 0; i < SIZE; i++) {
            DummyTask t = new DummyTask(10);
            tasks.add(t);
            queue.offer(t);
        }

        assertEquals(queue.size(), SIZE);

        queue.remove(tasks.get(6));
        queue.remove(tasks.get(7));
        queue.remove(tasks.get(4));
        queue.remove(tasks.get(5));

        assertEquals(queue.size(), SIZE - 4);

        queue.offer(tasks.get(4));
        queue.offer(tasks.get(5));
        queue.offer(tasks.get(6));
        queue.offer(tasks.get(7));

        assertEquals(queue.size(), SIZE);

        queue.clear();
    }

    public void testElement() {
        DummyTask task = new DummyTask(PRIORITY);
        queue.offer(task);

        assertEquals(queue.element(), task);

        for (int i = 0; i < SIZE/2; i++) {
            queue.offer(new DummyTask(SIZE));
        }

        queue.offer(task);

        assertEquals(queue.element(), task);
        queue.clear();

        boolean exceptionOccurred = false;
        try {
            queue.element();
        } catch (NoSuchElementException e) {
            exceptionOccurred = true;
        }

        assertTrue(exceptionOccurred);
    }
}
