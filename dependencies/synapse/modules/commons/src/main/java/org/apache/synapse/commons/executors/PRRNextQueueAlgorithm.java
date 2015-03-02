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

/**
 * This is a priority based round robin algorithm for getting the next queue </p>
 *
 * <p>This algorithm works in cycles. Lets say we have queues with following priorities.
 * 7, 5, 2 and assume we name the queues as 1, 2, 3 in the order. </p>
 * <p>Here is how messages are picked in a single cycle </p>
 * <p> 1, 1, 1, 1, 1, 1, 1 all the messages for the queue with priority 1 are sent for this cycle
 * 2, 2, 2, 2, 2,  all the messages for the queue with priority 2 are sent for this cycle
 * 3, 3 all the messages with priority 2 are sent for this cycle</p>
 *
 * <p>This algorithm choose the queues in the above order if all the queues have messages at the
 * point of selection. If a queue doesn't have messages it will skip the queue and move to the
 * next. If none of the queues have messages it will return null.
 */
public class PRRNextQueueAlgorithm<E> implements NextQueueAlgorithm<E> {
    
    /** Reference to the actual queue */
    private List<InternalQueue<E>> queues;

    /** Number of queues, we keep this to avoid the overhead of calculation this again and again */
    private int size = 0;

    /** Current queue we are operating on */
    private int currentQueue = 0;

    /** Number of messages sent from the current queue */
    private int currentCount = 0;

    public InternalQueue<E> getNextQueue() {
        InternalQueue<E> internalQueue = queues.get(currentQueue);

        int priority = internalQueue.getPriority();

        if (priority == currentCount || internalQueue.size() == 0) {
            currentCount = 0;
            // we need to move to the next queue not empty
            int c = 0;
            do {
                if (currentQueue == queues.size() - 1) {
                    currentQueue = 0;
                } else {
                    currentQueue++;
                }

                internalQueue = queues.get(currentQueue);

                c++;
                // we move forward until we find a non empty queue or everything is empty
            } while (internalQueue.size() == 0 && c < size);

            // if we come to the initial queue, that means all the queues are empty.
            if (internalQueue.size() == 0) {
                currentQueue = 0;
                return null;
            }
        }

        currentCount++;

        return internalQueue;
    }

    public void init(List<InternalQueue<E>> queues) {
        this.queues = queues;
        size = queues.size();
    }
}
