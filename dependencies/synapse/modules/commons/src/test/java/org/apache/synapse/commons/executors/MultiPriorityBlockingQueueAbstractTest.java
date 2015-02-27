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

import org.apache.synapse.commons.executors.queues.UnboundedQueue;
import org.apache.synapse.commons.executors.queues.FixedSizeQueue;

import java.util.List;
import java.util.ArrayList;

import junit.framework.TestCase;

/**
 * Provde common functionality for Concurrent and Single threaded testing of the
 * MultiPriorityBlockingQueue.
 */
public abstract class MultiPriorityBlockingQueueAbstractTest extends TestCase {
    protected MultiPriorityBlockingQueue<DummyTask> createUnboundedQueue(
            int noQueues, int[] priorities) {

        List<InternalQueue<DummyTask>> internalQueueList =
                new ArrayList<InternalQueue<DummyTask>>();

        for (int i = 0; i < noQueues; i++) {
            InternalQueue<DummyTask> intQueue = new UnboundedQueue<DummyTask>(priorities[i]);

            internalQueueList.add(intQueue);
        }

        return new MultiPriorityBlockingQueue(internalQueueList, true, new PRRNextQueueAlgorithm());
    }

    protected MultiPriorityBlockingQueue<DummyTask> createFixedQueue(
            int noQueues, int[] sizes, int[] priorities) {
        List<InternalQueue<DummyTask>> internalQueueList =
                new ArrayList<InternalQueue<DummyTask>>();

        for (int i = 0; i < noQueues; i++) {
            InternalQueue<DummyTask> intQueue =
                    new FixedSizeQueue<DummyTask>(priorities[i], sizes[i]);

            internalQueueList.add(intQueue);
        }

        return new MultiPriorityBlockingQueue(
                internalQueueList, true, new PRRNextQueueAlgorithm());
    }
}
