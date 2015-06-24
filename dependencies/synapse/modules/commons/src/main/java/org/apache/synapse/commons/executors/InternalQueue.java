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

import java.util.Queue;
import java.util.Collection;
import java.util.concurrent.locks.Condition;

/**
 * Interface implemented by the internal queues.
 * @param <E>
 */
@SuppressWarnings({"UnusedDeclaration"})
public interface InternalQueue<E> extends Queue<E> {
    /**
     * Get the Priority of the queue
     *
     * @return priority
     */
    public int getPriority();

    /**
     * Set the priority
     *
     * @param p priority
     */
    public void setPriority(int p);

    /**
     * Get not full condition
     *
     * @return not full condition
     */
    public Condition getNotFullCond();

    /**
     * Get not full condition
     *
     * @param condition condition     
     */
    public void setNotFullCond(Condition condition);


    /**
     * Drain items from this queue to the specified collection
     *
     * @param c collection to add item
     * @return number of items added
     */
    public int drainTo(Collection<? super E> c);

    /**
     * Drain items from this queue to the specified collection
     *
     * @param c collection to add item
     * @param maxElements maximum number of element to drain
     * @return number of items added
     */
    public int drainTo(Collection<? super E> c, int maxElements);

    /**
     * Remaining capacity of the queue. Unbounded queues should return
     * Integer.MAX_VALUE
     *
     * @return remaining capacity
     */
    public int remainingCapacity();

    /**
     * Return the capacity of the queue
     * 
     * @return capacity of thr queue
     */
    public int getCapacity();
}
