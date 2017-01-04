/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

package org.wso2.carbon.apimgt.gateway.jms;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.andes.AMQConnectionFailureException;
import org.wso2.andes.client.AMQConnectionFactory;
import org.wso2.andes.url.URLSyntaxException;
import org.wso2.carbon.apimgt.gateway.throttling.dto.JMSConfigDTO;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;

/**
 * JMS receiver thread, try to fetch message from JMS topic periodic
 */
public class JmsThrottleReceiver {
    private static final Logger log = LoggerFactory.getLogger(JmsReceiver.class);

    private static final int MAX_RETRY = 10;
    private static final int RETRY_INTERVAL = 15000;
    private int noOfRetries = 0;

    private TopicConnection topicConnection;
    private TopicSubscriber topicSubscriber;
    private TopicSession topicSession;

    public static final String QPID_ICF = "org.wso2.andes.jndi.PropertiesFileInitialContextFactory";
    private static final String CF_NAME_PREFIX = "connectionfactory.";
    private static final String CF_NAME = "qpidConnectionfactory";

    /**
     * Creates a JMS Topic subscription and registers a meesageListner
     *
     * @param config
     */
    public void registerSubscriber(JMSConfigDTO config) {
        try {

            TopicConnectionFactory topicConnectionFactory = new AMQConnectionFactory(getTCPConnectionURL(config));
            topicConnection = topicConnectionFactory.createTopicConnection();

            // if there is no Exception Listner a default exception listner will be set
            if (config.getDefaultExceptionListener() == null) {
                topicConnection.setExceptionListener(config.getDefaultExceptionListener());
            } else {

                topicConnection.setExceptionListener(new ExceptionListener() {
                    @Override
                    public void onException(JMSException exception) {
                        log.info("TopicConnection ExceptionListener triggered for: " + config.getTopicName(),
                                exception);
                        reSubscribeToJMSTopic(config);
                    }
                });
            }
            topicConnection.start();
            topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);
            Topic topic = topicSession.createTopic(config.getTopicName());
            topicSubscriber = topicSession.createSubscriber(topic);
            topicSubscriber.setMessageListener(config.getMessageListenerl());

            // once the connection is established no of retries counter is set to 0
            noOfRetries = 0;
            log.info("JmsThrottleReceiver started for Topic:" + config.getTopicName());

        } catch (JMSException e) {
            handleException(e, config);
        } catch (URLSyntaxException e) {
            log.error("Jms Subscription Failed", e);
        }


    }

    /**
     * Resubscribe to JMS topic until Max number of retries reached
     *
     * @param config
     */
    private void reSubscribeToJMSTopic(JMSConfigDTO config) {

        log.info("Retrying JMS Subscription to topic:" + config.getTopicName() + " Attempt :" + noOfRetries);
        closeConnection();
        if (noOfRetries <= MAX_RETRY) {
            try {
                Thread.sleep(RETRY_INTERVAL);
                noOfRetries++;
                registerSubscriber(config);
            } catch (InterruptedException ex) {
                log.error("JMS Receiver Thread Interupted", ex);
            }
        }
    }

    /**
     * Construct and get JMS connection String
     *
     * @param jmsConfigDTO
     * @return a connection String
     * formt amqp://{username}:{password}@carbon/carbon?brokerlist='tcp://{hostname}:{port}'
     */
    private static String getTCPConnectionURL(JMSConfigDTO jmsConfigDTO) {
        return new StringBuffer()
                .append("amqp://").append(jmsConfigDTO.getUsername()).append(":").append(jmsConfigDTO.getPassword())
                .append("@").append(jmsConfigDTO.getClientId())
                .append("/").append(jmsConfigDTO.getVirtualHostName())
                .append("?brokerlist='tcp://")
                .append(jmsConfigDTO.getDefaultHostname())
                .append(":")
                .append(jmsConfigDTO.getDefaultPort())
                .append("'").toString();
    }

    private void closeConnection() {

        if (topicConnection != null) {
            try {
                topicConnection.close();
            } catch (JMSException ex) {
                // swallowing exception since it wont affect the flow
                log.warn("Exception while closing topicConnection ", ex);
            }
        }

        if (topicSession != null) {
            try {
                topicSession.close();
            } catch (JMSException ex) {
                // swallowing exception since it wont affect the flow
                log.warn("Exception while closing topicSession ", ex);
            }
        }

    }

    private void handleException(Exception exception, JMSConfigDTO config) {
        // If its a connection failure restarting the connection
        if (exception.getCause().getClass().equals(AMQConnectionFailureException.class)) {
            log.error("JMS connection failed for " + config.getTopicName(), exception);
            reSubscribeToJMSTopic(config);
        } else {
            log.error("JMS Subscription Failed for topic :" + config.getTopicName(), exception);
        }
    }
}
