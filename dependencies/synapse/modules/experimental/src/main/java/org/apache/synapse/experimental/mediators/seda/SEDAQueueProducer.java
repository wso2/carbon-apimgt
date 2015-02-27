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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class SEDAQueueProducer {

    private static final Log log = LogFactory.getLog(SEDAQueueProducer.class);
    private final SEDAQueueProducerPolicy queueProducerPolicy;
    private final SEDAQueue sedaQueue;
    private final BlockingQueue<MessageContext> queue;

    public SEDAQueueProducer(SEDAQueueProducerPolicy queueProducerPolicy, SEDAQueue sedaQueue) {
        this.queueProducerPolicy = queueProducerPolicy;
        this.sedaQueue = sedaQueue;
        this.queue = sedaQueue.getQueue();
    }

    public void produce(MessageContext messageContext) {
        String action = queueProducerPolicy.getAction();
        if (SEDAQueueProducerPolicy.ADD.equals(action)) {
            queue.add(messageContext);
        } else if (SEDAQueueProducerPolicy.OFFER.equals(action)) {
            long timeout = queueProducerPolicy.getTimeoutOnInsert();
            if (timeout < 0) {
                if (!queue.offer(messageContext)) {
                    log.warn("Error while offering the message to the queue");    
                }
            } else {
                try {
                    queue.offer(messageContext, timeout, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    log.debug("Ignored InterruptedException when ocuured calling queue.offer");
                }
            }
        } else if (SEDAQueueProducerPolicy.PUT.equals(action)) {
            try {
                queue.put(messageContext);
            } catch (InterruptedException e) {
                log.debug("Ignored InterruptedException when ocuured calling queue.put");
            }
        }
    }
}
