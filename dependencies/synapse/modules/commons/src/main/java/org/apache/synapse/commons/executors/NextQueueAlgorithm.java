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
 * This interface abstracts the algorithm for determining the next internal
 * queue for picking up the message. This class is created once and initialized.
 * This class should capture any runtime information about the queues since the
 * MultiPriorityBlockingQueue doesn't hold any runtime state information about
 * the queues.
 *
 * @param <E>
 */
public interface NextQueueAlgorithm<E> {

    /**
     * Initialized with the queues sorted according to the priority.
     *
     * @param queues list of queues
     */
    void init(List<InternalQueue<E>> queues);

    /**
     * Should return a queue based on some selection criteria and current
     * state of the queues.
     *
     * @return the queue
     */
    InternalQueue<E> getNextQueue();    
}
