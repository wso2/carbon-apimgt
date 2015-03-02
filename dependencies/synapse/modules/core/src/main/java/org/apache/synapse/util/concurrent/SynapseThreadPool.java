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

import java.util.concurrent.*;

/**
 * This is the executor service that will be returned by the env
 */
public class SynapseThreadPool extends ThreadPoolExecutor {

    // default values
    public static final int SYNAPSE_CORE_THREADS  = 20;
    public static final int SYNAPSE_MAX_THREADS   = 100;
    public static final int SYNAPSE_KEEP_ALIVE    = 5;
    public static final int SYNAPSE_THREAD_QLEN   = -1;
    public static final String SYNAPSE_THREAD_GROUP     = "synapse-thread-group";
    public static final String SYNAPSE_THREAD_ID_PREFIX = "SynapseWorker";

    // property keys
    public static final String SYN_THREAD_CORE     = "synapse.threads.core";
    public static final String SYN_THREAD_MAX      = "synapse.threads.max";
    public static final String SYN_THREAD_ALIVE    = "synapse.threads.keepalive";
    public static final String SYN_THREAD_QLEN     = "synapse.threads.qlen";
    public static final String SYN_THREAD_GROUP    = "synapse.threads.group";
    public static final String SYN_THREAD_IDPREFIX = "synapse.threads.idprefix";

    /**
     * Constructor for the Synapse thread poll
     * 
     * @param corePoolSize    - number of threads to keep in the pool, even if they are idle
     * @param maximumPoolSize - the maximum number of threads to allow in the pool
     * @param keepAliveTime   - this is the maximum time that excess idle threads will wait
     *  for new tasks before terminating.
     * @param unit            - the time unit for the keepAliveTime argument.
     * @param workQueue       - the queue to use for holding tasks before they are executed.
     */
    public SynapseThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime,
        TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
            new SynapseThreadFactory(
                new ThreadGroup(SYNAPSE_THREAD_GROUP), SYNAPSE_THREAD_ID_PREFIX));
    }

    /**
     * Default Constructor for the thread pool and will use all the values as default
     */
    public SynapseThreadPool() {
        this(SYNAPSE_CORE_THREADS, SYNAPSE_MAX_THREADS, SYNAPSE_KEEP_ALIVE,
            TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }

    /**
     * Constructor for the SynapseThreadPool
     * 
     * @param corePoolSize  - number of threads to keep in the pool, even if they are idle
     * @param maxPoolSize   - the maximum number of threads to allow in the pool
     * @param keepAliveTime - this is the maximum time that excess idle threads will wait
     *  for new tasks before terminating.
     * @param qlen          - Thread Blocking Queue length
     * @param threadGroup    - ThreadGroup name
     * @param threadIdPrefix - Thread id prefix
     */
    public SynapseThreadPool(int corePoolSize, int maxPoolSize, long keepAliveTime, int qlen,
        String threadGroup, String threadIdPrefix) {
        super(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS,
            qlen > 0 ? new LinkedBlockingQueue<Runnable>(qlen) : new LinkedBlockingQueue<Runnable>(),
            new SynapseThreadFactory(new ThreadGroup(threadGroup), threadIdPrefix));
    }
}
