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

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 *
 */
public class SEDAQueueConsumerWorker implements Runnable {

    private static final Log log = LogFactory.getLog(SEDAQueueConsumerWorker.class);
    private final SEDAQueueConsumerPolicy queueConsumerPolicy;
    private final SEDAQueue sedaQueue;
    private final BlockingQueue<MessageContext> queue;
    private final SEDAQueueConsumer sedaQueueConsumer;

    public SEDAQueueConsumerWorker(SEDAQueue sedaQueue,
                                   SEDAQueueConsumerPolicy queueConsumerPolicy,
                                   SEDAQueueConsumer sedaQueueConsumer) {
        this.sedaQueue = sedaQueue;
        this.queueConsumerPolicy = queueConsumerPolicy;
        this.queue = sedaQueue.getQueue();
        this.sedaQueueConsumer = sedaQueueConsumer;
    }

    public void run() {
        String action = queueConsumerPolicy.getAction();
        if (SEDAQueueConsumerPolicy.TAKE.equals(action)) {
            try {
                consume(queue.take());
            } catch (InterruptedException ignored) {
                log.debug("Ignored InterruptedException when ocuured calling queue.take()");
            }
        } else if (SEDAQueueConsumerPolicy.POLL.equals(action)) {
            long timeout = queueConsumerPolicy.getTimeoutOnPoll();
            if (timeout < 0) {
                consume(queue.poll());
            } else {
                try {
                    consume(queue.poll(timeout, TimeUnit.MILLISECONDS));
                } catch (InterruptedException ignored) {
                    log.debug("Ignored InterruptedException when ocuured calling queue.poll()");
                }
            }
        } else if (SEDAQueueConsumerPolicy.DRAIN.equals(action)) {
            int maxElements = queueConsumerPolicy.getMaxElementsOnPoll();
            Collection<MessageContext> contexts = new ArrayList<MessageContext>(maxElements);
            queue.drainTo(contexts, maxElements);
            consume(contexts);
        } else if (SEDAQueueConsumerPolicy.DRAINALL.equals(action)) {
            Collection<MessageContext> contexts = new ArrayList<MessageContext>();
            queue.drainTo(contexts);
            consume(contexts);
        }
    }

    private void consume(MessageContext context) {
        if (context != null) {
            sedaQueueConsumer.consume(context);
        }
    }

    private void consume(Collection<MessageContext> contexts) {
        for (MessageContext context : contexts) {
            consume(context);
        }
    }
}
