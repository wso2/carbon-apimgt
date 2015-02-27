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

package org.apache.synapse.util.concurrent;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.ThreadFactory;

/**
 * This is the thread factory for Synapse threads which are accessible through the
 * SynapseEnvironment as pooled threads.
 */
public class SynapseThreadFactory implements ThreadFactory {

    /** Holds the ThreadGroup under which this factory creates threads */
    private final ThreadGroup group;

    /** Holds the AtomicInteger class instance for the factory */
    private final AtomicInteger count;

    /** prefix for the thread id, thread number will be followed to construct the id */
    private final String namePrefix;

    /**
     * Constructor for the ThreadFactory to create new threads
     *
     * @param group      - all the threads are created under this group by this factory
     * @param namePrefix - name prefix of the threads created by this factory
     */
    public SynapseThreadFactory(final ThreadGroup group, final String namePrefix) {
        super();
        this.count = new AtomicInteger(1);
        this.group = group;
        this.namePrefix = namePrefix;
    }

    /**
     * This method is the implementation of the the newThread method and will
     * create new threads under the group and with the nameprefix followed by the
     * thread number as the id
     * 
     * @param runnable - Runnable class to run by the created thread
     * @return a Thread executing the given runnable
     */
    public Thread newThread(final Runnable runnable) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(this.namePrefix);
        buffer.append('-');
        buffer.append(this.count.getAndIncrement());
        Thread t = new Thread(group, runnable, buffer.toString(), 0);
        t.setDaemon(false);
        t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }

}
