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
import org.apache.synapse.SynapseException;
import org.apache.synapse.config.SynapseConfiguration;
import org.apache.synapse.core.SynapseEnvironment;
import org.apache.synapse.core.axis2.Axis2MessageContext;
import org.apache.synapse.core.axis2.Axis2SynapseEnvironment;
import org.apache.synapse.message.MessageConsumer;
import org.apache.synapse.message.MessageProducer;
import org.apache.synapse.message.store.AbstractMessageStore;
import org.apache.synapse.message.store.Constants;

import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueConnectionFactory;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.Set;

public class JmsStore extends AbstractMessageStore {
    /** JMS Broker username */
    public static final String USERNAME = "store.jms.username";
    /** JMS Broker password */
    public static final String PASSWORD = "store.jms.password";
    /** Whether to cache the connection or not */
    public static final String CACHE = "store.jms.cache.connection";
    /** JMS destination (ie. Queue) name that this message store must store the messages to. */
    public static final String DESTINATION = "store.jms.destination";
    /** JMS Specification version */
    public static final String JMS_VERSION = "store.jms.JMSSpecVersion";
    /** */
    public static final String CONSUMER_TIMEOUT = "store.jms.ConsumerReceiveTimeOut";
    /** */
    public static final String CONN_FACTORY = "store.jms.connection.factory";
    /** */
    public static final String NAMING_FACTORY_INITIAL = "java.naming.factory.initial";
    /** */
    public static final String CONNECTION_STRING = "connectionfactory.QueueConnectionFactory";
    /** */
    public static final String PROVIDER_URL = "java.naming.provider.url";
    /** JNDI Queue Prefix */
    public static final String QUEUE_PREFIX = "queue.";

    /** JMS connection properties */
    private final Properties properties = new Properties();
    /** JMS username */
    private String userName;
    /** JMS password */
    private String password;
    /** JMS queue name */
    private String destination;
    /** type of the JMS destination. we support queue */
    private String destinationType = "queue";
    /** */
    private static final Log logger = LogFactory.getLog(JmsStore.class.getName());
    /** */
    private int cacheLevel = 0;
    /** */
    public static final String JMS_SPEC_11 = "1.1";
    /** Is JMS Version 1.1? */
    private boolean isVersion11 = true;
    /** Look up context */
    private Context context;
    /** JMS cachedConnection factory */
    private javax.jms.ConnectionFactory connectionFactory;
    /** JMS destination */
    private Destination queue;
    /** */
    private final Object queueLock = new Object();
    /** JMS Connection used to send messages to the queue */
    private Connection producerConnection;
    /** lock protecting the producer connection */
    private final Object producerLock = new Object();
    /** records the last retried time between the broker and ESB */
    private long retryTime = -1;

    public MessageProducer getProducer() {
        JmsProducer producer = new JmsProducer(this);
        producer.setId(nextProducerId());
        Throwable throwable = null;
        Session session = null;
        javax.jms.MessageProducer messageProducer;
        boolean error = false;
        try {
            synchronized (producerLock) {
                if (producerConnection == null) {
                    boolean ok = newWriteConnection();
                    if (!ok) {
                        return producer;
                    }
                }
            }
            try {
                session = newSession(producerConnection(), Session.AUTO_ACKNOWLEDGE);
            } catch (JMSException e) {
                synchronized (producerLock) {
                    boolean ok = newWriteConnection();
                    if (!ok) {
                        return producer;
                    }
                }
                session = newSession(producerConnection(), Session.AUTO_ACKNOWLEDGE);
                logger.info(nameString() + " established a connection to the broker.");
            }
            messageProducer = newProducer(session);
            producer.setConnection(producerConnection())
                    .setSession(session)
                    .setProducer(messageProducer);
        } catch (Throwable t) {
            error = true;
            throwable = t;
        }
        if (error) {
            String errorMsg = "Could not create a Message Producer for "
                              + nameString() + ". Error:" + throwable.getLocalizedMessage();
            logger.error(errorMsg, throwable);
            synchronized (producerLock) {
                cleanup(producerConnection, session, true);
                producerConnection = null;
            }
            return producer;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(nameString() + " created message producer " + producer.getId());
        }
        return producer;
    }

    public MessageConsumer getConsumer() {
        JmsConsumer consumer =  new JmsConsumer(this);
        consumer.setId(nextConsumerId());
        Connection connection = null;
        try {
            // Had to add a condition to avoid piling up log files with the error message and throttle retries.
            // need to improve this to allow the client to configure it.
            if ((System.currentTimeMillis() - retryTime) >= 3000) {
                connection = newConnection();
                retryTime = -1;
            }
        } catch (JMSException e) {

            retryTime = System.currentTimeMillis();

            if (logger.isDebugEnabled()) {
                logger.error("Could not create a Message Consumer for "
                             + nameString() + ". Could not create connection.");
            }
            return consumer;
        }
        if (connection == null) {
            return consumer;
        }
        Session session;
        try {
            session = newSession(connection, Session.CLIENT_ACKNOWLEDGE);
        } catch (JMSException e) {
            if (logger.isDebugEnabled()) {
                logger.error("Could not create a Message Consumer for "
                             + nameString() + ". Could not create session.");
            }
            return consumer;
        }
        if (session == null) {
            return consumer;
        }
        javax.jms.MessageConsumer c;
        try {
            c = newConsumer(session);
        } catch (JMSException e) {
            if (logger.isDebugEnabled()) {
                logger.error("Could not create a Message Consumer for "
                             + nameString() + ". Could not create consumer.");
            }
            return consumer;
        }
        consumer.setConnection(connection)
                .setSession(session)
                .setConsumer(c);
        if (logger.isDebugEnabled()) {
            logger.debug(nameString() + " created message consumer " + consumer.getId());
        }
        return consumer;
    }

    public int getType() {
        return Constants.JMS_MS;
    }

    /** JMS Message store does not support following operations. */
    public MessageContext remove() throws NoSuchElementException {
        return null;
    }

    public void clear() {
    }

    public MessageContext remove(String messageID) {
        return null;
    }

    public MessageContext get(int index) {
        return null;
    }

    public List<MessageContext> getAll() {
        return null;
    }

    public MessageContext get(String messageId) {
        return null;
    } /** End of unsupported operations. */

    public void init(SynapseEnvironment se) {
        if (se == null) {
            logger.error("Cannot initialize store.");
            return;
        }
        boolean initOk = initme();
        super.init(se);
        if (initOk) {
            logger.info(nameString() + ". Initialized... ");
        } else {
            logger.info(nameString() + ". Initialization failed...");
        }
    }

    public void destroy() {
        // do whatever...
        if (logger.isDebugEnabled()) {
            logger.debug("Destroying " + nameString() + "...");
        }
        closeWriteConnection();
        super.destroy();
    }

    /**
     * Creates a new JMS Connection.
     *
     * @return A connection to the JMS Queue used as the store of this message store.
     * @throws JMSException
     */
    public Connection newConnection() throws JMSException {
        Connection connection;
        if (connectionFactory == null) {
            logger.error(nameString() + ". Could not create a new connection to the broker." +
                    " Initial Context Factory:[" + parameters.get(NAMING_FACTORY_INITIAL) + "]; Provider URL:[" + parameters.get(PROVIDER_URL) + "]; Connection Factory:[null].");
            return null;
        }
        if (isVersion11) {
            if (userName != null && password != null) {
                connection = connectionFactory.createConnection(userName, password);
            } else {
                connection = connectionFactory.createConnection();
            }
        } else {
            QueueConnectionFactory connectionFactory;
            connectionFactory = (QueueConnectionFactory) this.connectionFactory;
            if (userName != null && password != null) {
                connection = connectionFactory.createQueueConnection(userName, password);
            } else {
                connection = connectionFactory.createQueueConnection();
            }
        }
        connection.start();
        if (logger.isDebugEnabled()) {
            logger.debug(nameString() + ". Created JMS Connection.");
        }
        return connection;
    }

    /**
     * Creates a new JMS Session.
     *
     * @param connection The JMS Connection that must be used when creating the session.
     * @param mode Acknowledgement mode that must be used for this session.
     * @return A JMS Session.
     * @throws JMSException
     */
    public Session newSession(Connection connection, int mode) throws JMSException {
        if (connection == null) {
            logger.error(nameString() + " cannot create JMS Session. Invalid connection.");
            return null;
        }
        Session session;
        if (isVersion11) {
            session = connection.createSession(false, mode);
        } else {
            session = ((QueueConnection) connection).createQueueSession(false, mode);
        }
        if (logger.isDebugEnabled()) {
            logger.debug(nameString() + ". Created JMS Session.");
        }
        return session;
    }

    /**
     * Creates a new JMS Message Producer.
     *
     * @param session  A JMS Session.
     * @return A JMS Message Producer.
     * @throws JMSException
     */
    public javax.jms.MessageProducer newProducer(Session session) throws JMSException {
        if (session == null) {
            logger.error(nameString() + " cannot create JMS Producer. Invalid session.");
            return null;
        }
        if (!createDestIfAbsent(session)) {
            logger.error(nameString() + " cannot create JMS Producer. " +
                         "Destination queue is invalid.");
            return null;
        }
        javax.jms.MessageProducer producer;
        if (isVersion11) {
            producer = session.createProducer(queue);
        } else {
            producer = ((QueueSession) session).createSender((javax.jms.Queue) queue);
        }
        if (logger.isDebugEnabled()) {
            logger.debug(nameString() + " created JMS Message Producer to destination ["
                         + queue.toString() + "].");
        }
        return producer;
    }

    /**
     * Returns a new JMS Message Consumer.
     * @param session A JMS Session
     * @return A JMS Message Consumer
     * @throws JMSException
     */
    public javax.jms.MessageConsumer newConsumer(Session session) throws JMSException {
        if (session == null) {
            logger.error(nameString() + " cannot create JMS Consumer. Invalid session.");
            return null;
        }
        if (!createDestIfAbsent(session)) {
            logger.error(nameString() + " cannot create JMS Consumer. " +
                         "Destination queue is invalid.");
            return null;
        }
        javax.jms.MessageConsumer consumer;
        if(isVersion11) {
            consumer = session.createConsumer(queue);
        } else {
            consumer = ((QueueSession) session).createReceiver((Queue) queue);
        }
        if (logger.isDebugEnabled()) {
            logger.debug(nameString() + " created JMS Message Consumer to destination ["
                         + queue.toString() + "].");
        }
        return consumer;
    }

    /**
     * Creates a new JMS Message producer connection.
     *
     * @return true if new producer connection was successfully created, <br/>
     * false otherwise.
     */
    public boolean newWriteConnection() {
        synchronized (producerLock) {
            if (producerConnection != null) {
                if (!closeConnection(producerConnection)) {
                    return false;
                }
            }
            try {
                producerConnection = newConnection();
            } catch (JMSException e) {
                logger.error(nameString() + " cannot create connection to the broker. Error:"
                             + e.getLocalizedMessage()
                        + ". Initial Context Factory:[" + parameters.get(NAMING_FACTORY_INITIAL) + "]; Provider URL:[" + parameters.get(PROVIDER_URL) + "]; Connection Factory:[" + connectionFactory + "].");
                producerConnection = null;
            }
        }
        return producerConnection != null;
    }

    /**
     * Closes the existing JMS message producer connection.
     *
     * @return true if the producer connection was closed without any error, <br/>
     * false otherwise.
     */
    public boolean closeWriteConnection() {
        synchronized (producerLock) {
            if (producerConnection != null) {
                if (!closeConnection(producerConnection)) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns the existing JMS message producer connection.
     *
     * @return The current JMS Connection used to create message producers.
     */
    public Connection producerConnection() {
        return producerConnection;
    }

    /**
     * Closes the given JMS Connection.
     *
     * @param connection The JMS Connection to be closed.
     * @return true if the connection was successfully closed. false otherwise.
     */
    public boolean closeConnection(Connection connection) {
        try {
            connection.close();
            if (logger.isDebugEnabled()) {
                logger.debug(nameString() + " closed connection to JMS broker.");
            }
        } catch (JMSException e) {
            return false;
        }
        return true;
    }

    /**
     * Cleans up the JMS Connection and Session associated with a JMS client.
     *
     * @param connection  JMS Connection
     * @param session JMS Session associated with the given connection
     * @param error is this method called upon an error
     * @return {@code true} if the cleanup is successful. {@code false} otherwise.
     */
    public boolean cleanup(Connection connection, Session session, boolean error) {
        if (connection == null && error) {
            return true;
        }
        try {
            if (session != null) {
                session.close();
            }
        } catch (JMSException e) {
            return false;
        }
        try {
            if (connection != null && error) {
                connection.close();
            }
        } catch (JMSException e) {
            return false;
        }
        return true;
    }

    public org.apache.axis2.context.MessageContext newAxis2Mc() {
        return ((Axis2SynapseEnvironment) synapseEnvironment)
                .getAxis2ConfigurationContext().createMessageContext();
    }

    public org.apache.synapse.MessageContext newSynapseMc(
            org.apache.axis2.context.MessageContext msgCtx) {
        SynapseConfiguration configuration = synapseEnvironment.getSynapseConfiguration();
        return new Axis2MessageContext(msgCtx, configuration, synapseEnvironment);
    }

    public void setParameters(Map<String, Object> parameters) {
        if (parameters == null || parameters.isEmpty()) {
            throw new SynapseException("Cannot initialize JMS Store [" + getName() +
                                       "]. Required parameters are not available.");
        }
        super.setParameters(parameters);
    }

    private boolean initme() {
        Set<Map.Entry<String, Object>> mapSet = parameters.entrySet();
        for (Map.Entry<String, Object> e : mapSet) {
            if (e.getValue() instanceof String) {
                properties.put(e.getKey(), e.getValue());
            }
        }
        userName = (String) parameters.get(USERNAME);
        password = (String) parameters.get(PASSWORD);
        String conCaching = (String) parameters.get(CACHE);
        if ("true".equals(conCaching)) {
            if (logger.isDebugEnabled()) {
                logger.debug(nameString() + " enabling connection Caching");
            }
            cacheLevel = 1;
        }
        String destination = (String) parameters.get(DESTINATION);
        if (destination != null) {
            this.destination = destination;
        } else {
            String name = getName();
            String defaultDest;
            if (name != null && !name.isEmpty()) {
                defaultDest = name + "_Queue";
            } else {
                defaultDest =  "JmsStore_" + System.currentTimeMillis() + "_Queue";
            }
            logger.warn(nameString() + ". Destination not provided. " +
                        "Setting default destination to [" + defaultDest + "].");
            this.destination = defaultDest;
        }
        destinationType = "queue";
        String version = (String) parameters.get(JMS_VERSION);
        if (version != null) {
            if (!JMS_SPEC_11.equals(version)) {
                isVersion11 = false;
            }
        }
        String consumerReceiveTimeOut = (String) parameters.get(CONSUMER_TIMEOUT);
        int consumerReceiveTimeOutI = 6000;
        if (consumerReceiveTimeOut != null) {
            try {
                consumerReceiveTimeOutI = Integer.parseInt(consumerReceiveTimeOut);
            } catch (NumberFormatException e) {
                //logger.error(nameString() + ". Error parsing consumer receive time out value. " +
                //             "Set to 60s.");
            }
        } //else {
            //logger.warn(nameString() + ". Consumer Receiving time out not passed in. " +
            //            "Set to 60s.");
        //}
        String connectionFac = null;
        try {
            context = new InitialContext(properties);
            connectionFac = (String) parameters.get(CONN_FACTORY);
            if (connectionFac == null) {
                connectionFac = "QueueConnectionFactory";
            }
            connectionFactory = lookup(context, javax.jms.ConnectionFactory.class, connectionFac);
            if (connectionFactory == null) {
                throw new SynapseException(nameString() + " could not initialize JMS Connection Factory. " +
                                           "Connection factory not found : " + connectionFac);
            }
            createDestIfAbsent(null);
            if (queue == null) {
                logger.warn(nameString() + ". JMS Destination [" + destination + "] does not exist.");
            }
        } catch (NamingException e) {
            logger.error(nameString() + ". Could not initialize JMS Message Store. Error:"
                    + e.getLocalizedMessage() + ". Initial Context Factory:[" + parameters.get(NAMING_FACTORY_INITIAL) + "]; Provider URL:[" + parameters.get(PROVIDER_URL) + "]; Connection Factory:[" + connectionFac + "].");
        } catch (Throwable t) {
            logger.error(nameString() + ". Could not initialize JMS Message Store. Error:"
                         + t.getMessage() + ". Initial Context Factory:[" + parameters.get(NAMING_FACTORY_INITIAL) + "]; Provider URL:[" + parameters.get(PROVIDER_URL) + "]; Connection Factory:[" + connectionFac + "].");
        }
        if (!newWriteConnection()) {
            logger.warn(nameString() + ". Starting with a faulty connection to the broker.");
            return false;
        }
        return true;
    }

    private Destination getDestination(Session session) {
        Destination dest = queue;
        if (dest != null) {
            return dest;
        }
        InitialContext newContext = null;
        try {
            dest = lookup(context, javax.jms.Destination.class, destination);
        } catch (NamingException e) {
            if (logger.isDebugEnabled()) {
                logger.debug(nameString() + ". Could not lookup destination [" + destination
                             + "]. Message: " + e.getLocalizedMessage());
            }
            newContext = newContext();
        }
        try {
            if (newContext != null) {
                dest = lookup(newContext, javax.jms.Destination.class, destination);
            }
        } catch (Throwable t) {
            logger.info(nameString() + ". Destination [" + destination
                        + "] not defined in JNDI context. Message:" + t.getLocalizedMessage());
        }
        if (dest == null && session != null) {
            try {
                dest = session.createQueue(destination);
                if (logger.isDebugEnabled()) {
                    logger.debug(nameString() + " created destination ["
                                 + destination + "] from session object.");
                }
            } catch (JMSException e) {
                logger.error(nameString() + " could not create destination ["
                             + destination + "]. Error:" + e.getLocalizedMessage(), e);
                dest = null;
            }
        }
        if (dest == null && session == null) {
            if (logger.isDebugEnabled()) {
                logger.debug(nameString() + ". Both destination and session is null." +
                             " Could not create destination.");
            }
        }
        synchronized (queueLock) {
            queue = dest;
        }
        return dest;
    }

    private InitialContext newContext() {
        Properties properties = new Properties();
        InitialContext newContext;
        Map env;
        try {
            env = context.getEnvironment();
            Object o = env.get(NAMING_FACTORY_INITIAL);
            if (o != null) {
                properties.put(NAMING_FACTORY_INITIAL, o);
            }
            o = env.get(CONNECTION_STRING);
            if (o != null) {
                properties.put(CONNECTION_STRING, o);
            }
            o = env.get(PROVIDER_URL);
            if (o != null) {
                properties.put(PROVIDER_URL, o);
            }
            properties.put(QUEUE_PREFIX + destination, destination);
            newContext = new InitialContext(properties);
        } catch (NamingException e) {
            logger.info(nameString() + " could not create a new Context. Message:"
                        + e.getLocalizedMessage());
            return null;
        }
        if (logger.isDebugEnabled()) {
            logger.debug(nameString() + " Created a new Context.");
        }
        return newContext;
    }

    private <T> T lookup(Context context, Class<T> clazz, String name)
            throws NamingException {
        if (context == null) {
            logger.error(nameString() + ". Cannot perform JNDI lookup. Invalid context.");
            return null;
        }
        if (name == null || "".equals(name)) {
            logger.error(nameString() + ". Cannot perform JNDI lookup. Invalid name.");
            return null;
        }
        Object object = context.lookup(name);
        try {
            return clazz.cast(object);
        } catch (ClassCastException e) {
            logger.error(nameString() + ". Cannot perform JNDI lookup for the name ["
                         + name + "].", e);
            return null;
        }
    }

    private boolean destinationNonNull() {
        synchronized (queueLock) {
            return queue != null;
        }
    }

    private boolean createDestIfAbsent(Session session) {
        synchronized (queueLock) {
            return getDestination(session) != null;
        }
    }

    private String nameString() {
        return "Store [" + getName() + "]";
    }
}
