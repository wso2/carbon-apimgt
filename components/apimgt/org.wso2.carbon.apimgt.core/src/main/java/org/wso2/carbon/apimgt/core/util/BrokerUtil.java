/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package org.wso2.carbon.apimgt.core.util;

import com.google.gson.Gson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.Broker;
import org.wso2.carbon.apimgt.core.dto.GatewayDTO;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.exception.GatewayException;

import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;

/**
 * The util class to handle broker related operations
 */
public class BrokerUtil {
    private static final Logger log = LoggerFactory.getLogger(BrokerUtil.class);
    private static Broker broker;

    /**
     * Initialize Broker Utils
     *
     * @param broker Broker reference
     */
    public static synchronized void initialize(Broker broker) {
        if (BrokerUtil.broker != null) {
            return;
        }
        BrokerUtil.broker = broker;
    }

    /**
     * @param topicName
     * @param gatewayDTO
     */
    public static void publishToTopic(String topicName, GatewayDTO gatewayDTO) throws GatewayException {
        TopicSession topicSession = null;
        Topic topic = null;
        TopicPublisher topicPublisher = null;
        TopicConnection topicConnection = null;
        try {
            topicConnection = getTopicConnection();
            topicSession = topicConnection.createTopicSession(false, Session.AUTO_ACKNOWLEDGE);

            topic = topicSession.createTopic(topicName);
            topicPublisher = topicSession.createPublisher(topic);
            TextMessage textMessage = topicSession.createTextMessage(new Gson().toJson(gatewayDTO));
            topicPublisher.publish(textMessage);
        } catch (JMSException e) {
            String errorMessage = "Error occurred while publishing " + gatewayDTO.getEventType() + " event to JMS " +
                    "topic :" + topicName;
            log.error(errorMessage, e);
            throw new GatewayException(errorMessage, ExceptionCodes.GATEWAY_EXCEPTION);
        } finally {
            if (topicPublisher != null) {

                try {
                    topicPublisher.close();
                } catch (JMSException e) {
                    log.error("Error occurred while closing topic publisher for topic : " + topicName);
                }
            }
            if (topicSession != null) {
                try {
                    topicSession.close();
                } catch (JMSException e) {
                    log.error("Error occurred while closing topic session for topic : " + topicName);
                }
            }
            if (topicConnection != null) {
                try {
                    topicConnection.close();
                } catch (JMSException e) {
                    log.error("Error occurred while closing topic connection for topic : " + topicName);
                }
            }
        }
    }

    /**
     * Retrieve a new TopicConnection from broker connection pool
     *
     * @return topicConnection  new topic connection to broker
     * @throws JMSException if it failed to create a new topic connection
     */
    private static TopicConnection getTopicConnection() throws JMSException {
        if (broker == null) {
            throw new JMSException("Error while creating topic connection");
        }
        return broker.getTopicConnection();
    }

}

