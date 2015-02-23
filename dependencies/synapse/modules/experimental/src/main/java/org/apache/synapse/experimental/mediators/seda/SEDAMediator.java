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

import org.apache.synapse.ManagedLifecycle;
import org.apache.synapse.Mediator;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.mediators.AbstractMediator;

/**
 *
 */
public class SEDAMediator extends AbstractMediator implements ManagedLifecycle {

    private SEDAQueueConsumerPolicy sedaQueueConsumerPolicy;
    private SEDAQueueProducerPolicy sedaQueueProducerPolicy;
    private SEDAQueuePolicy sedaQueuePolicy;
    private SEDAQueue sedaQueue;
    private SEDAQueueProducer sedaQueueProducer;
    private boolean initialized = false;
    // A key of the mediator that do work after SEDA component - actual consumer
    private String consumer;

    public boolean mediate(MessageContext synCtx) {
        if (initialized) {
            sedaQueueProducer.produce(synCtx);
        }
        return false;
    }

    public void init(SynapseEnvironment se) {
        Mediator mediator = se.getSynapseConfiguration().getSequence(consumer);
        sedaQueue = new SEDAQueue(sedaQueuePolicy,
                new SEDAQueueConsumerWorkerFactory(sedaQueueConsumerPolicy,
                        new DefaultConsumer(mediator)));
        sedaQueueProducer = new SEDAQueueProducer(sedaQueueProducerPolicy,
                sedaQueue);
        sedaQueue.init();
        initialized = true;
    }

    public void destroy() {
        sedaQueue.destory();
        initialized = false;
    }

    public SEDAQueueConsumerPolicy getSedaQueueConsumerPolicy() {
        return sedaQueueConsumerPolicy;
    }

    public void setSedaQueueConsumerPolicy(SEDAQueueConsumerPolicy sedaQueueConsumerPolicy) {
        this.sedaQueueConsumerPolicy = sedaQueueConsumerPolicy;
    }

    public SEDAQueueProducerPolicy getSedaQueueProducerPolicy() {
        return sedaQueueProducerPolicy;
    }

    public void setSedaQueueProducerPolicy(SEDAQueueProducerPolicy sedaQueueProducerPolicy) {
        this.sedaQueueProducerPolicy = sedaQueueProducerPolicy;
    }

    public SEDAQueuePolicy getSedaQueuePolicy() {
        return sedaQueuePolicy;
    }

    public void setSedaQueuePolicy(SEDAQueuePolicy sedaQueuePolicy) {
        this.sedaQueuePolicy = sedaQueuePolicy;
    }

    public String getConsumer() {
        return consumer;
    }

    public void setConsumer(String consumer) {
        this.consumer = consumer;
    }
}
