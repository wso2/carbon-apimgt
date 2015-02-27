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
import org.apache.synapse.message.MessageProducer;

import java.util.Queue;

public class InMemoryProducer implements MessageProducer {
    private static final Log logger = LogFactory.getLog(InMemoryProducer.class.getName());
    /** */
    private Queue<MessageContext> queue;
    /** */
    private final InMemoryStore store;
    /** */
    private String idString;

    private final Object queueLock;

    public InMemoryProducer(InMemoryStore store) {
        this.store = store;
        this.queueLock = store.getQLock();
    }

    public boolean storeMessage(MessageContext synCtx) {
        boolean result = false;
        if (synCtx != null) {
            synCtx.getEnvelope().build();
            synchronized (queueLock) {
                result = queue.offer(synCtx);
            }
            if (!result) {
                logger.warn(getId() + " ignored MessageID : " + synCtx.getMessageID());
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug(getId() + " stored MessageID: " + synCtx.getMessageID());
        }
        if (result) {
            store.enqueued();
        }
        return result;
    }

    public boolean cleanup() {
        if (logger.isDebugEnabled()) {
            logger.debug(getId() + " cleanup");
        }
        return true;
    }

    public void setId(int id) {
        idString = "[" + store.getName() + "-P-" + id + "]";
    }

    public String getId() {
        return idString;
    }

    public InMemoryProducer setDestination(Queue<MessageContext> queue) {
        this.queue = queue;
        return this;
    }
}
