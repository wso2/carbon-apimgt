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
package org.apache.synapse.experimental.mediators.seda;

import org.apache.synapse.MessageContext;

import java.util.concurrent.*;

/**
 *
 */
public class SEDAQueue {

    private SEDAQueuePolicy queuePolicy;
    private final BlockingQueue<MessageContext> queue;
    private SEDAQueueConsumerWorkerFactory workerFactory;
    private boolean initialized;
    private static final Executor EXECUTOR
            = Executors.newFixedThreadPool(10);

    public SEDAQueue(SEDAQueuePolicy queuePolicy, SEDAQueueConsumerWorkerFactory workerFactory) {
        this.queuePolicy = queuePolicy;
        this.queue = createBlockingQueue(queuePolicy);
        this.workerFactory = workerFactory;
    }

    private BlockingQueue<MessageContext> createBlockingQueue(SEDAQueuePolicy queuePolicy) {
        BlockingQueue<MessageContext> queue;
        String queueType = queuePolicy.getQueueType();
        int capacity = queuePolicy.getQueueSize();
        if (SEDAQueuePolicy.QUEUE_TYPE_PRIORITY_BLOCKING.equals(queueType)) {
            queue = new PriorityBlockingQueue<MessageContext>(capacity);
        } else if (SEDAQueuePolicy.QUEUE_TYPE_SYNCHRONOUS.equals(queueType)) {
            queue = new SynchronousQueue<MessageContext>();
        } else {
            queue = new LinkedBlockingQueue<MessageContext>(capacity);
        }
        return queue;
    }

    public void init() {
        EXECUTOR.execute(workerFactory.createSEDAQueueConsumerWorker(this));
    }

    public void destory() {
    }

    public BlockingQueue<MessageContext> getQueue() {
        return queue;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public SEDAQueuePolicy getQueuePolicy() {
        return queuePolicy;
    }
}
