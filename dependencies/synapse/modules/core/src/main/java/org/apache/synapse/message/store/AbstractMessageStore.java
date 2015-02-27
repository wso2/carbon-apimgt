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

package org.apache.synapse.message.store;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.commons.jmx.MBeanRegistrar;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.SynapseEnvironment;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public abstract class AbstractMessageStore implements MessageStore {
    private static final Log logger = LogFactory.getLog(AbstractMessageStore.class.getName());

    /**
     * message store name
     */
    protected String name;

    /**
     * name of the sequence to be executed before storing the message
     */
    protected String sequence;

    /**
     * Message store JMX view
     */
    protected MessageStoreView messageStoreMBean;

    /**
     * synapse configuration reference
     */
    protected SynapseConfiguration synapseConfiguration;

    /**
     * synapse environment reference
     */
    protected SynapseEnvironment synapseEnvironment;

    /**
     * Message store parameters
     */
    protected Map<String,Object> parameters;

    /**
     * Message Store description
     */
    protected String description;

    /**
     * Name of the file where this message store is defined
     */
    protected String fileName;

    /**
     * List that holds the MessageStore observers registered with the Message Store
     */
    protected List<MessageStoreObserver> messageStoreObservers =
            new ArrayList<MessageStoreObserver>();

    protected Lock lock = new ReentrantLock();

    /** */
    private MessageStore store;
    /** Message producer id */
    private AtomicInteger producerId = new AtomicInteger(0);
    /** Message consumer id */
    private AtomicInteger consumerId = new AtomicInteger(0);

    private int maxProducerId = Integer.MAX_VALUE;

    private AtomicLong enqueued = new AtomicLong(0);

    private AtomicLong dequeued = new AtomicLong(0);

    private static final long maxEnDequeuable = Long.MAX_VALUE;

    private final Object messageCountLock = new Object();

    public void init(SynapseEnvironment se) {
        this.synapseEnvironment = se;
        this.synapseConfiguration = synapseEnvironment.getSynapseConfiguration();
        if (logger.isDebugEnabled()) {
            logger.debug("Initialized store [" + getName() + "]...");
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name == null || "".equals(name)) {
            logger.warn("Invalid name.");
            return;
        }
        this.name = name;
        messageStoreMBean = new MessageStoreView(name, this);
        MBeanRegistrar.getInstance().registerMBean(messageStoreMBean,
                "MessageStore", this.name);
    }

    public void registerObserver(MessageStoreObserver observer) {
        if(observer != null && !messageStoreObservers.contains(observer)) {
            messageStoreObservers.add(observer);
        }
    }

    public void unregisterObserver(MessageStoreObserver observer) {
        if(observer != null && messageStoreObservers.contains(observer)) {
            messageStoreObservers.remove(observer);
        }
    }

    /**
     * Notify Message Addition to the observers
     * @param messageId of the Message added.
     */
    protected void notifyMessageAddition(String messageId) {
        for(MessageStoreObserver o : messageStoreObservers) {
            o.messageAdded(messageId);
        }
    }

    /**
     * Notify Message removal to the observers
     * @param messageId of the Message added
     */
    protected void notifyMessageRemoval(String messageId) {
        for(MessageStoreObserver o : messageStoreObservers) {
            o.messageRemoved(messageId);
        }
    }

    public int size() {
        return -1;
    }

    public Map<String, Object> getParameters() {
        return Collections.unmodifiableMap(parameters);
    }

    public void setParameters(Map<String, Object> parameters) {
        if (parameters == null) {
            this.parameters = new HashMap<String, Object>();
            return;
        }
        this.parameters = parameters;
    }

    public void destroy() {
        if (logger.isDebugEnabled()) {
            logger.debug("Destroyed store [" + getName() + "]...");
        }
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return this.description;
    }

    public void setFileName(String filename) {
        this.fileName = filename;
    }

    public String getFileName() {
        return this.fileName;
    }

    public int getType() {
        return 4;
    }

    public Lock getLock() {
        return lock;
    }

    public int nextProducerId() {
        int id = producerId.incrementAndGet();
        if (id == maxProducerId) {
            logger.info("Setting producer ID generator to 0...");
            producerId.set(0);
            id = producerId.incrementAndGet();
        }
        return id;
    }

    public int nextConsumerId() {
        int id = consumerId.incrementAndGet();
        return id;
    }


    public void enqueued() {
        synchronized (messageCountLock) {
            enqueued.compareAndSet(maxEnDequeuable, 0);
            enqueued.incrementAndGet();
        }
    }

    public void dequeued() {
        synchronized (messageCountLock) {
            dequeued.compareAndSet(maxEnDequeuable, 0);
            dequeued.incrementAndGet();
        }
    }

    public long difference() {
        synchronized (messageCountLock) {
            long diff = enqueued.get() - dequeued.get();
            return diff;
        }
    }
}
