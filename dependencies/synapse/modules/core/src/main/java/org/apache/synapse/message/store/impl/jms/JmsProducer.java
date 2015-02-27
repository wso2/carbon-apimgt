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

package org.apache.synapse.message.store.impl.jms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.synapse.MessageContext;
import org.apache.synapse.message.MessageProducer;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;
import javax.jms.Session;

public class JmsProducer implements MessageProducer {
    private static final Log logger = LogFactory.getLog(JmsProducer.class.getName());

    private static final String OriginalMessageID = "OrigMessageID";

    private Connection connection;

    private Session session;

    private javax.jms.MessageProducer producer;

    private JmsStore store;

    private String idString;

    private boolean isConnectionError = false;

    private boolean isInitialized = false;

    public JmsProducer(JmsStore store) {
        if (store == null) {
            logger.error("Cannot initialize.");
            return;
        }
        this.store = store;
        isInitialized = true;
    }

    public boolean storeMessage(MessageContext synCtx) {
        if (synCtx == null) {
            return false;
        }
        if (!checkConnection()) {
            logger.warn(getId() + ". Ignored MessageID : " + synCtx.getMessageID());
            return false;
        }
        StorableMessage message = MessageConverter.toStorableMessage(synCtx);
        boolean error = false;
        Throwable throwable = null;
        try {
            ObjectMessage objectMessage = session.createObjectMessage(message);
            objectMessage.setStringProperty(OriginalMessageID, synCtx.getMessageID());
            setPriority(producer, objectMessage, message);
            producer.send(objectMessage);
        } catch (JMSException e) {
            throwable = e;
            error = true;
            isConnectionError = true;
        } catch (Throwable t) {
            throwable = t;
            error = true;
        }
        if (error) {
            String errorMsg = getId() + ". Ignored MessageID : " + synCtx.getMessageID()
                              + ". Could not store message to store ["
                              + store.getName() + "]. Error:" + throwable.getLocalizedMessage();
            logger.error(errorMsg, throwable);
            store.closeWriteConnection();
            connection = null;
            if (logger.isDebugEnabled()) {
                logger.debug(getId() + ". Ignored MessageID : " + synCtx.getMessageID());
            }
            return false;
        } else {
            store.cleanup(null, session, false);
        }
        if (logger.isDebugEnabled()) {
            logger.debug(getId() + ". Stored MessageID : " + synCtx.getMessageID());
        }
        store.enqueued();
        return true;
    }

    public boolean cleanup() {
        return store.cleanup(null, session, false);
    }

    public JmsProducer setConnection(Connection connection) {
        this.connection = connection;
        return this;
    }

    public JmsProducer setSession(Session session) {
        this.session = session;
        return this;
    }

    public JmsProducer setProducer(javax.jms.MessageProducer producer) {
        this.producer = producer;
        return this;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void setId(int id) {
        idString = "[" + store.getName() + "-P-" + id + "]";
    }

    public String getId() {
        return getIdAsString();
    }

    private String getIdAsString() {
        if (idString == null) {
            return "[unknown-producer]";
        }
        return idString;
    }

    private boolean checkConnection() {
        if (producer == null) {
            if (logger.isDebugEnabled()) {
                logger.error(getId() + " cannot proceed. Message producer is null.");
            }
            return false;
        }
        if (session == null) {
            if (logger.isDebugEnabled()) {
                logger.error(getId() + " cannot proceed. JMS Session is null.");
            }
            return false;
        }
        if (connection == null) {
            if (logger.isDebugEnabled()) {
                logger.error(getId() + " cannot proceed. JMS Connection is null.");
            }
            return false;
        }
        return true;
    }

    private void setPriority(javax.jms.MessageProducer producer, ObjectMessage objectMessage,
                             StorableMessage message) {
        if (message.getPriority() != 4) {
            try {
                producer.setPriority(message.getPriority());
            } catch (JMSException e) {
                logger.warn(getId() + " could not set priority ["
                            + message.getPriority() + "]");
            }
        }  else {
            try {
                producer.setPriority(4);
            } catch (JMSException e) {}
        }
    }
}
