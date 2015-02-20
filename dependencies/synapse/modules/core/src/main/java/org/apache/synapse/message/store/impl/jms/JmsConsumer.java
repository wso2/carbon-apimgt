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
import org.apache.synapse.message.MessageConsumer;
import org.apache.synapse.message.store.Constants;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.ObjectMessage;
import javax.jms.Session;

public class JmsConsumer implements MessageConsumer {
    private static final Log logger = LogFactory.getLog(JmsConsumer.class.getName());

    private Connection connection;

    private Session session;

    private javax.jms.MessageConsumer consumer;

    private JmsStore store;

    private String idString;

    private boolean isInitialized;
    /** Holds the last message read from the message store. */
    private CachedMessage cachedMessage;
    /** Did last receive() call cause an error? */
    private boolean isReceiveError;

    public JmsConsumer(JmsStore store) {
        if (store == null) {
            logger.error("Cannot initialize.");
            return;
        }
        this.store = store;
        cachedMessage = new CachedMessage();
        isReceiveError = false;
        isInitialized = true;
    }

    public MessageContext receive() {
        boolean error;
        JMSException exception;
        if (!checkConnection()) {
            if (!reconnect()) {
                if (logger.isDebugEnabled()) {
                    logger.debug(getId()
                            + " cannot receive message from store. Cannot reconnect.");
                }
                return null;
            } else {
                logger.info(getId() + " reconnected to store.");
                isReceiveError = false;
            }

        }
        if (!checkConnection()) {
            if (logger.isDebugEnabled()) {
                logger.debug(getId() + " cannot receive message from store.");
            }
            return null;
        }
        try {
            Message message = consumer.receive(3000);
            if (message == null) {
                return null;
            }
            if (!(message instanceof ObjectMessage)) {
                logger.warn(getId() + ". Did not receive a javax.jms.ObjectMessage");
                message.acknowledge(); // TODO:
                return null;
            }
            ObjectMessage msg = (ObjectMessage) message;
            String messageId = msg.getStringProperty(Constants.OriginalMessageID);
            if (!(msg.getObject() instanceof StorableMessage)) {
                logger.warn(getId() + ". Did not receive a valid message.");
                message.acknowledge();
                return null;
            }
            StorableMessage storableMessage = (StorableMessage) msg.getObject();
            org.apache.axis2.context.MessageContext axis2Mc = store.newAxis2Mc();
            MessageContext synapseMc = store.newSynapseMc(axis2Mc);
            synapseMc = MessageConverter.toMessageContext(storableMessage, axis2Mc, synapseMc);
            updateCache(message, synapseMc, messageId, false);
            if (logger.isDebugEnabled()) {
                logger.debug(getId() + " Received MessageId:" + messageId + " priority:" + message.getJMSPriority());
            }
            return synapseMc;
        } catch (JMSException e) {
            error = true;
            exception = e;
        }
        if (error) {
            if (!isReceiveError) {
                logger.error(getId() + " cannot receive message from store. Error:"
                             + exception.getLocalizedMessage());//, exception);
            }
            updateCache(null, null, "", true);
            cleanup();
            return null;
        }
        return null;
    }

    public boolean ack() {
        boolean result = cachedMessage.ack();
        if (result) {
            store.dequeued();
        }
        return result;
    }

    public boolean cleanup() {
        if (logger.isDebugEnabled()) {
            logger.debug(getId() + " cleaning up...");
        }
        boolean result =  store.cleanup(connection, session, true);
        if (result) {
            connection = null;
            session = null;
            consumer = null;
            return true;
        }
        return false;
    }

    public Connection getConnection() {
        return connection;
    }

    public JmsConsumer setConnection(Connection connection) {
        this.connection = connection;
        return this;
    }

    public Session getSession() {
        return session;
    }

    public JmsConsumer setSession(Session session) {
        this.session = session;
        return this;
    }

    public javax.jms.MessageConsumer getConsumer() {
        return consumer;
    }

    public JmsConsumer setConsumer(javax.jms.MessageConsumer consumer) {
        this.consumer = consumer;
        return this;
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void setId(int id) {
        idString = "[" + store.getName() + "-C-" + id + "]";
    }

    public void setStringId(String idString) {
        this.idString = idString;
    }

    public String getId() {
        return getIdAsString();
    }

    private String getIdAsString() {
        if (idString == null) {
            return "[unknown-consumer]";
        }
        return idString;
    }

    private boolean checkConnection() {
        if (consumer == null) {
            if (logger.isDebugEnabled()) {
                logger.debug(getId() + " cannot proceed. Message consumer is null.");
            }
            return false;
        }
        if (session == null) {
            if (logger.isDebugEnabled()) {
                logger.debug(getId() + " cannot proceed. JMS Session is null.");
            }
            return false;
        }
        if (connection == null) {
            if (logger.isDebugEnabled()) {
                logger.debug(getId() + " cannot proceed. JMS Connection is null.");
            }
            return false;
        }
        return true;
    }

    private void writeToFileSystem() {
    }

    private void updateCache(Message message, MessageContext synCtx, String messageId,
                        boolean receiveError) {
        isReceiveError = receiveError;
        cachedMessage.setMessage(message);
        cachedMessage.setMc(synCtx);
        cachedMessage.setId(messageId);
    }

    private boolean reconnect() {
        JmsConsumer consumer = (JmsConsumer) store.getConsumer();

        if (consumer.getConsumer() == null) {
            if (logger.isDebugEnabled()) {
                logger.debug(getId() + " could not reconnect to the broker.");
            }
            return false;
        }
        connection = consumer.getConnection();
        session = consumer.getSession();
        this.consumer = consumer.getConsumer();
        if (logger.isDebugEnabled()) {
            logger.debug(getId() + " ===> " + consumer.getId());
        }
        //setStringId(consumer.getId());
        return true;
    }

    private final class CachedMessage {
        private Message message = null;
        private MessageContext mc = null;
        private String id = "";

        public CachedMessage setMessage(Message message) {
            this.message = message;
            return this;
        }

        public boolean ack() {
            try {
                if (message != null) {
                    message.acknowledge();
                }
            } catch (JMSException e) {
                logger.error(getId() + " cannot ack last read message. Error:"
                             + e.getLocalizedMessage(), e);
                return false;
            }
            return true;
        }

        public Message getMessage() {
            return message;
        }

        public CachedMessage setMc(MessageContext mc) {
            this.mc = mc;
            return this;
        }

        public CachedMessage setId(String id) {
            this.id = id;
            return this;
        }

        public String getId() {
            return id;
        }
    }
}
