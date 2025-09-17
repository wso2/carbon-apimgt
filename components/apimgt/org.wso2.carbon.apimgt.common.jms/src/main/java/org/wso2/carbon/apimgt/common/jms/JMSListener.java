/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.common.jms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.common.jms.utils.JMSUtils;

import java.util.Hashtable;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * The revamped JMS Transport listener implementation. Creates {@link JMSTaskManager} instances
 * for each service requesting exposure over JMS, and stops these if they are undeployed / stopped.
 * <p/>
 * A service indicates a JMS Connection factory definition by name, which would be defined in the
 * JMSListner on the axis2.xml, and this provides a way to reuse common configuration between
 * services, as well as to optimize resources utilized
 * <p/>
 * If the connection factory name was not specified, it will default to the one named "default"
 * <p/>
 * If a destination JNDI name is not specified, a service will expect to use a Queue with the same
 * JNDI name as of the service. Additional Parameters allows one to bind to a Topic or specify
 * many more detailed control options. See package documentation for more details
 * <p/>
 * All Destinations / JMS Administered objects used MUST be pre-created or already available
 */
public class JMSListener implements Runnable {

    private static final Log log = LogFactory.getLog(JMSListener.class);
    private final String listenerName;
    private final JMSTaskManager jmsTaskManager;

    public JMSListener(String listenerName, JMSTaskManager jmsTaskManager) {

        this.listenerName = listenerName;
        this.jmsTaskManager = jmsTaskManager;
    }


    /**
     * Listen for JMS messages on behalf of the given service
     */
    private void start() {

        JMSTaskManager stm = jmsTaskManager;
        boolean connected = false;

        /* The following parameters are used when trying to connect with the JMS provider at the start up*/
        int r = 1;
        long retryDuration = 10000;
        double reconnectionProgressionFactor = 2.0;
        long maxReconnectDuration = 1000 * 60 * 60; // 1 hour


        // First we will check whether jms provider is started or not, as if not it will throw a continuous error log
        // If jms provider not started we will wait for exponentially increasing time intervals,
        // till the provider is started
        while (!connected) {
            boolean jmsProviderStarted = checkJMSConnection(stm);
            if (jmsProviderStarted) {
                log.info("Connection attempt: " + r + " for JMS Provider for listener: " + listenerName
                        + " was successful!");
                connected = true;
                stm.start();

                for (int i = 0; i < 3; i++) {
                    // Check the consumer count rather than the active task count. Reason: if the
                    // destination is of type topic, then the transport is only ready to receive
                    // messages if at least one consumer exists. This is of not much importance,
                    // except for automated tests.
                    if (stm.getConsumerCount() > 0) {
                        log.info("Started to listen on destination : " + stm.getDestinationJNDIName() +
                                " of type " + JMSUtils.getDestinationTypeAsString(stm.getDestinationType()) +
                                " for listener " + listenerName);
                        return;
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignore) {
                    }
                }

                log.warn("Polling tasks on destination : " + stm.getDestinationJNDIName() +
                        " of type " + JMSUtils.getDestinationTypeAsString(stm.getDestinationType()) +
                        " for listener " + listenerName + " have not yet started after 3 seconds ..");
            } else {
                log.error("JMS Provider is not yet started. Please start the JMS provider now.");
                retryDuration = (long) (retryDuration * reconnectionProgressionFactor);
                log.error("Connection attempt : " + (r++) + " for JMS Provider failed. Next retry in "
                        + (retryDuration / 1000) + " seconds");
                if (retryDuration > maxReconnectDuration) {
                    retryDuration = maxReconnectDuration;
                }
                try {
                    Thread.sleep(retryDuration);
                } catch (InterruptedException ignore) {
                }
            }
        }
    }

    private boolean checkJMSConnection(JMSTaskManager stm) {

        Connection connection = null;
        Hashtable<String, String> jmsProperties = stm.getJmsProperties();
        try {
            ConnectionFactory jmsConFactory = null;
            try {
                jmsConFactory = JMSUtils.lookup(
                        new InitialContext(stm.getJmsProperties()), ConnectionFactory.class,
                        stm.getConnFactoryJNDIName());
            } catch (NamingException e) {
                log.error("Error looking up connection factory : " + stm.getConnFactoryJNDIName() +
                        " using JNDI properties for listener: " + listenerName, e);
            }
            connection = JMSUtils.createConnection(
                    jmsConFactory,
                    jmsProperties.get(JMSConstants.PARAM_JMS_USERNAME),
                    jmsProperties.get(JMSConstants.PARAM_JMS_PASSWORD),
                    stm.isJmsSpec11(), stm.isQueue(), stm.isSubscriptionDurable(), stm.getDurableSubscriberClientId());
        } catch (JMSException ignore) {
        } finally {
            // we silently ignore this as a JMSException can be expected when connection is not available

            // Closing the connection, because if left open, when shutting down broker errors will be thrown
            // complaining about active subscriptions.
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException e) {
                    log.warn("Error while closing test connection.", e);
                }
            }
        }
        if (connection != null) {
            log.info("Connection successfully created towards the JMS provider for the listener: "
                            + stm.getJmsConsumerName() + "#" + stm.getDestinationJNDIName()
                            + ". The connected JMS provider is " + connection.toString()
                            .replace("\n", " | "));
        } else {
            log.warn("Connection could not be created towards the JMS provider for the listener: " 
                                + stm.getJmsConsumerName() + "#" + stm.getDestinationJNDIName());
        }
        return (connection != null);
    }

    /**
     * Stops listening for messages for the service thats undeployed or stopped
     */
    public void stopListener() {
        JMSTaskManager stm = jmsTaskManager;
        if (log.isDebugEnabled()) {
            log.debug("Stopping listening on destination : " + stm.getDestinationJNDIName() +
                    " for listener : " + listenerName);
        }

        stm.stop();

        log.info("Stopped listening for JMS messages to listener : " + listenerName);
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.       *
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        start();
    }

    public void startListener() {
        new Thread(this).start();
    }
}
