/**
 *  Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.synapse.message.store.impl.memory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.message.MessageConsumer;
import org.apache.synapse.message.MessageProducer;
import org.apache.synapse.message.store.AbstractMessageStore;
import org.apache.synapse.message.store.Constants;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class InMemoryStore extends AbstractMessageStore {
    private static final Log logger = LogFactory.getLog(InMemoryStore.class.getName());

    private Queue<MessageContext> queue = new ConcurrentLinkedQueue<MessageContext>();

    private final Object queueLock = new Object();

    public MessageProducer getProducer() {
        InMemoryProducer producer = new InMemoryProducer(this);
        producer.setId(nextProducerId());
        producer.setDestination(queue);
        if (logger.isDebugEnabled()) {
            logger.debug(nameString() + " created a new In Memory Message Producer.");
        }
        return producer;
    }

    public MessageConsumer getConsumer() {
        InMemoryConsumer consumer = new InMemoryConsumer(this);
        consumer.setId(nextConsumerId());
        consumer.setDestination(queue);
        if (logger.isDebugEnabled()) {
            logger.debug(nameString() + " created a new In Memory Message Consumer.");
        }
        return consumer;
    }

    public int getType() {
        return Constants.INMEMORY_MS;
    }

    public int size() {
        synchronized (queueLock) {
            return queue.size();
        }
    }

    public MessageContext remove() throws NoSuchElementException {
        MessageContext message;
        synchronized (queueLock) {
            message = queue.remove();
        }
        if (message != null) {
            dequeued();
            if (logger.isDebugEnabled()) {
                logger.debug(nameString() + " removed MessageID:" + message.getMessageID() + " => true");
            }
        }
        return message;
    }

    public void clear() {
        synchronized (queueLock) {
            while (!queue.isEmpty()) {
                remove();
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug(nameString() + " cleared InMemoryStore...");
        }
    }

    public MessageContext remove(final String messageID) {
        MessageContext message = null;
        synchronized (queueLock) {
            for (MessageContext m : queue) {
                if (m.getMessageID().equals(messageID)) {
                    message = m;
                    break;
                }
            }
            if (message != null) {
                boolean result = queue.remove(message);
                if (logger.isDebugEnabled()) {
                    logger.debug(nameString() + " removed MessageID:" + message.getMessageID() + " => " + result);
                }
            }
        }
        return message;
    }

    public MessageContext get(final int index) {
        if (index < 0 || index >= queue.size()) {
            return null;
        }
        MessageContext message = null;
        synchronized (queueLock) {
            int i = 0;
            for (MessageContext m : queue) {
                if (i == index) {
                    message = m;
                    break;
                }
                ++i;
            }
        }
        return message;
    }

    public List<MessageContext> getAll() {
        List<MessageContext> list = new ArrayList<MessageContext>();
        synchronized (queueLock) {
            list.addAll(queue);
        }
        return list;
    }

    public MessageContext get(String messageId) {
        MessageContext message = null;
        synchronized (queueLock) {
            for (MessageContext m : queue) {
                if (m.getMessageID().equals(messageId)) {
                    message = m;
                    break;
                }
            }
        }
        return message;
    }

    public void init(SynapseEnvironment se) {
        if (se == null) {
            logger.error("Cannot initialize store.");
            return;
        }
        super.init(se);
        logger.info("Initialized " + nameString() + "...");
    }

    public void destroy() {
        // do whatever...
        if (logger.isDebugEnabled()) {
            logger.debug("Destroying " + nameString() + "...");
        }
        super.destroy();
    }

    public Object getQLock() {
        return queueLock;
    }

    private String nameString() {
        return "Store [" + getName() + "]";
    }
}
