/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.andes.client.AMQConnectionFactory;
import org.wso2.andes.url.URLSyntaxException;
import org.wso2.carbon.apimgt.gateway.APIMConfigurations;
import org.wso2.carbon.apimgt.gateway.GatewayConstants;
import org.wso2.carbon.apimgt.gateway.exception.APISubscriptionValidationException;
import org.wso2.carbon.apimgt.gateway.subscription.APISubscriptionDataHolder;

import java.util.HashMap;
import java.util.Map;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.NamingException;

/**
 * This class is used to subscribe to the jms topic and save the read configuration to the file system
 */
class APISubscriptionMessageJMSListener {
    private static final Logger log = LoggerFactory.getLogger(APITopicSubscriber.class);
    private TopicConnection topicConnection;
    private TopicSession topicSession;
    private APIMConfigurations config = null;

    APISubscriptionMessageJMSListener() {
        config = new APIMConfigurations();
    }

    /**
     * Subscribe to the topic
     *
     * @return subscriber
     * @throws NamingException    throws if any name resolution issue occur
     * @throws JMSException       throws if JMS exception occurred
     * @throws URLSyntaxException throws if connection string exception found
     */
    TopicSubscriber subscribe() throws NamingException, JMSException, URLSyntaxException {
        // Lookup connection factory
        TopicConnectionFactory connFactory = new AMQConnectionFactory(
                getTCPConnectionURL(config.getUsername(), config.getPassword()));
        topicConnection = connFactory.createTopicConnection();
        topicConnection.start();
        topicSession = topicConnection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
        // Send message
        Topic topic = topicSession.createTopic(GatewayConstants.SUBSCRIPTION_TOPIC_NAME);
        return topicSession.createSubscriber(topic);
    }

    /**
     * Try to read msg from topic
     *
     * @param topicSubscriber current subscriber
     * @throws NamingException                    throws if any name resolution issue occur
     * @throws APISubscriptionValidationException throws if subscription validation exception occurred
     */
    void receive(TopicSubscriber topicSubscriber) throws APISubscriptionValidationException {
        try {
            Message message = topicSubscriber.receive();
            if (message instanceof TextMessage) {
                TextMessage textMessage = (TextMessage) message;
                String msg = textMessage.getText();
                if (log.isDebugEnabled()) {
                    log.debug("Got API Subscription from topic subscriber = " + msg);
                }
                JsonObject jsonMsg = new Gson().fromJson(msg, JsonObject.class);

                String action = jsonMsg.get(GatewayConstants.ACTION).getAsString();

                if (!GatewayConstants.ACTION_NEW.equalsIgnoreCase(action) &&
                        !GatewayConstants.ACTION_REMOVED.equalsIgnoreCase(action)) {
                    throw new APISubscriptionValidationException("Invalid " + GatewayConstants.ACTION + " (" + action
                            + ") found in subscription message.");
                }

                String apiContext = jsonMsg.get(GatewayConstants.API_CONTEXT).getAsString();
                String apiVersion = jsonMsg.get(GatewayConstants.API_VERSION).getAsString();
                String consumerKey = jsonMsg.get(GatewayConstants.CONSUMER_KEY).getAsString();
                if (GatewayConstants.ACTION_NEW.equalsIgnoreCase(action)) {
                    String apiProvider = jsonMsg.get(GatewayConstants.API_PROVIDER).getAsString();
                    String appName = jsonMsg.get(GatewayConstants.APPLICATION_NAME).getAsString();
                    String appOwner = jsonMsg.get(GatewayConstants.APPLICATION_OWNER).getAsString();
                    String subscriptionPolicy = jsonMsg.get(GatewayConstants.SUBSCRIPTION_POLICY).getAsString();
                    String keyEnvType = jsonMsg.get(GatewayConstants.KEY_ENV_TYPE).getAsString();
                    Map<String, String> subscriptionData = new HashMap<>();
                    subscriptionData.put(GatewayConstants.SUBSCRIPTION_POLICY, subscriptionPolicy);
                    subscriptionData.put(GatewayConstants.API_CONTEXT, apiContext);
                    subscriptionData.put(GatewayConstants.API_VERSION, apiVersion);
                    subscriptionData.put(GatewayConstants.API_PROVIDER, apiProvider);
                    subscriptionData.put(GatewayConstants.APPLICATION_NAME, appName);
                    subscriptionData.put(GatewayConstants.APPLICATION_OWNER, appOwner);
                    subscriptionData.put(GatewayConstants.CONSUMER_KEY, consumerKey);
                    subscriptionData.put(GatewayConstants.KEY_ENV_TYPE, keyEnvType);
                    APISubscriptionDataHolder.getInstance().addApiSubscriptionToMap(apiContext, apiVersion, consumerKey,
                            subscriptionData);
                } else if (GatewayConstants.ACTION_REMOVED.equalsIgnoreCase(action)) {
                    APISubscriptionDataHolder.getInstance().removeApiSubscriptionFromMap(apiContext, apiVersion,
                            consumerKey);
                }
            }
        } catch (JMSException e) {
            throw new APISubscriptionValidationException(e);
        }
    }

    /**
     * Stop the jms listener
     */
    void stop() {
        if (topicSession != null) {
            try {
                topicSession.close();
            } catch (JMSException e) {
                // TODO Auto-generated catch block
                log.error("Error closing connections", e);
            }
        }
        if (topicConnection != null) {
            try {
                topicConnection.stop();
                topicConnection.close();
            } catch (JMSException e) {
                // TODO Auto-generated catch block
                log.error("Error closing connections", e);
            }
        }

    }

    /**
     * Construct and get JMS connection String
     *
     * @param username user name
     * @param password password
     * @return connection String as String
     */
    private String getTCPConnectionURL(String username, String password) {
        // amqp://{username}:{password}@carbon/carbon?brokerlist='tcp://{hostname}:{port}'
        return "amqp://" + username + ':' + password + '@' +
                config.getCarbonClientId() + '/' +
                config.getCarbonVirtualHostName() + "?brokerlist='tcp://" +
                config.getTopicServerHost() + ':' +
                config.getTopicServerPort() + '\'';
    }
}
