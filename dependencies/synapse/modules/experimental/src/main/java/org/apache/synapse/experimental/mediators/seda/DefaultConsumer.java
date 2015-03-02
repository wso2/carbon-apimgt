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

import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 *
 */
public class DefaultConsumer implements SEDAQueueConsumer {

    private static final Executor EXECUTOR
            = Executors.newFixedThreadPool(10);

    private Mediator mediator;

    public DefaultConsumer(Mediator mediator) {
        this.mediator = mediator;
    }

    public void consume(MessageContext messageContext) {
        EXECUTOR.execute(new Worker(mediator, messageContext));
    }

    static class Worker implements Runnable {

        private final Mediator mediator;
        private final MessageContext messageContext;

        private volatile Exception exception;

        public Worker(final Mediator mediator, final MessageContext messageContext) {
            super();
            this.mediator = mediator;
            this.messageContext = messageContext;
        }

        public void run() {
            try {
                mediator.mediate(messageContext);
            } catch (Exception ex) {
                this.exception = ex;
            }
        }

        public Exception getException() {
            return this.exception;
        }

    }
}
