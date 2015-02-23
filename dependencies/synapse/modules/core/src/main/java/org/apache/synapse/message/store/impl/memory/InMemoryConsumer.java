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
import org.apache.synapse.message.MessageConsumer;

import java.util.Queue;

public class InMemoryConsumer implements MessageConsumer {
    private static final Log logger = LogFactory.getLog(InMemoryConsumer.class.getName());
    /** */
    private Queue<MessageContext> queue;
    /** */
    private final InMemoryStore store;
    /** ID of this message consumer instance */
    private String idString;

    private MessageContext lastMessage;

    private final Object queueLock;

    public InMemoryConsumer(InMemoryStore store) {
        this.store = store;
        this.queueLock = store.getQLock();
    }

    public MessageContext receive() {
        MessageContext message;
        synchronized (queueLock) {
            message = queue.peek();
            if (logger.isDebugEnabled()) {
                if (message != null) {
                    logger.debug(getId() + " received MessageID : " + message.getMessageID());
                }
            }
            lastMessage = message;

        }
        return message;
    }

    public boolean ack() {
        if (logger.isDebugEnabled() && lastMessage != null) {
            logger.debug(getId() + " ack");
        }
        synchronized (queueLock) {
            Object o = queue.poll();
            if (o != null) {
                store.dequeued();
            }
            lastMessage = null;
        }
        return true;
    }

    public boolean cleanup() {
        if (logger.isDebugEnabled()) {
            logger.debug(getId() + " cleanup");
        }
        return true;
    }

    public void setId(int id) {
        idString = "[" + store.getName() + "-C-" + id + "]";
    }

    public String getId() {
        return idString;
    }

    public InMemoryConsumer setDestination(Queue<MessageContext> queue) {
        this.queue = queue;
        return this;
    }
}
