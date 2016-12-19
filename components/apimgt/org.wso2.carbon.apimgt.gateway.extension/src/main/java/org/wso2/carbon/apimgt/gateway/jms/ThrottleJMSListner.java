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
import org.wso2.andes.client.AMQConnectionFactory;
import org.wso2.andes.url.URLSyntaxException;
import org.wso2.carbon.apimgt.gateway.APIMConfigurations;
import org.wso2.carbon.apimgt.gateway.throttling.ThrottleDataHolder;
import org.wso2.carbon.apimgt.gateway.throttling.constants.APIConstants;
import org.wso2.carbon.apimgt.gateway.throttling.constants.APIThrottleConstants;
import org.wso2.carbon.apimgt.gateway.throttling.constants.JMSConfigs;

import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.NamingException;


/**
 * This class is used to subscribe to a jms topic and update the throttle maps
 */
public class ThrottleJMSListner {

    private static final Logger log = LoggerFactory.getLogger(ThrottleJMSListner.class);
    private TopicConnection topicConnection;
    private TopicSession topicSession;

    // These patterns will be used to determine for which type of keys the throttling condition has occurred.
    private Pattern apiPattern = Pattern.compile("/.*/(.*):\\1_(condition_(\\d*)|default)");
    private static final int API_PATTERN_GROUPS = 3;
    private static final int API_PATTERN_CONDITION_INDEX = 2;

    private Pattern resourcePattern = Pattern.compile("/.*/(.*)/\\1(.*)?:[A-Z]{0,5}_(condition_(\\d*)|default)");
    public static final int RESOURCE_PATTERN_GROUPS = 4;
    public static final int RESOURCE_PATTERN_CONDITION_INDEX = 3;

    private boolean isSubscribed = false;

    /**
     * Subscribe to the topic
     *
     * @return subscriber
     * @throws NamingException    throws if any name resolution issue occur
     * @throws JMSException       throws if JMS exception occurred
     * @throws URLSyntaxException throws if connection string exception found
     */
    public TopicSubscriber subscribe() throws NamingException, JMSException, URLSyntaxException {
        // Lookup connection factory
        TopicSubscriber topicSubscriber = null;
        try {
            TopicConnectionFactory connFactory = new AMQConnectionFactory(
                    getTCPConnectionURL(JMSConfigs.JMS_USERNAME, JMSConfigs.JMS_PASSWORD));
            topicConnection = connFactory.createTopicConnection();
            topicConnection.start();
            topicSession = topicConnection.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
            // Send message
            Topic topic = topicSession.createTopic(JMSConfigs.THROTTLING_TOPIC_NAME);
            topicSubscriber = topicSession.createSubscriber(topic);
            isSubscribed = true;

        } catch (JMSException e) {
            //swallowing Exception since this method will be periodically called until a jms conncetion is established
            isSubscribed = false;
            log.error("Jms connection failure ");
        }

        return topicSubscriber;
    }

    /**
     * Try to read msg from topic
     *
     * @param topicSubscriber current subscriber
     * @throws NamingException throws if any name resolution issue occur
     * @throws JMSException    throws if JMS exception occurred
     */
    public void receive(TopicSubscriber topicSubscriber) throws NamingException, JMSException {

        Message message = topicSubscriber.receive();

        if (log.isDebugEnabled()) {
            log.debug(" Event received in JMS Event Receiver - " + message);
        }

        try {
            if (message != null) {

                if (message instanceof MapMessage) {
                    MapMessage mapMessage = (MapMessage) message;
                    Map<String, Object> map = new HashMap<String, Object>();
                    Enumeration enumeration = mapMessage.getMapNames();
                    while (enumeration.hasMoreElements()) {
                        String key = (String) enumeration.nextElement();
                        map.put(key, mapMessage.getObject(key));
                    }

                    if (log.isDebugEnabled()) {
                        log.debug("JMS message map :" + map);
                    }

                    if (map.get(APIConstants.THROTTLE_KEY) != null) {
                        /**
                         * This message contains throttle data in map which contains Keys
                         * throttleKey - Key of particular throttling level
                         * isThrottled - Whether message has throttled or not
                         * expiryTimeStamp - When the throttling time window will expires
                         */
                        handleThrottleUpdateMessage(map);
                    } else if (map.get(APIConstants.BLOCKING_CONDITION_KEY) != null) {
                        /**
                         * This message contains blocking condition data
                         * blockingCondition - Blocking condition type
                         * conditionValue - blocking condition value
                         * state - State whether blocking condition is enabled or not
                         */
                        handleBlockingMessage(map);
                    } else if (map.get(APIConstants.POLICY_TEMPLATE_KEY) != null) {
                        /**
                         * This message contains key template data
                         * keyTemplateValue - Value of key template
                         * keyTemplateState - whether key template active or not
                         */
                        handleKeyTemplateMessage(map);
                    }

                } else {
                    log.warn("Event dropped due to unsupported message type " + message.getClass());
                }
            } else {
                log.warn("Dropping the empty/null event received through jms receiver");
            }
        } catch (JMSException e) {
            log.error("JMSException occurred when processing the received message ", e);
        }
    }

    /**
     * Stops the jms listener
     */
    public void stop() {
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
        APIMConfigurations apimConfig = new APIMConfigurations();
        return new StringBuffer().append("amqp://").append(username).append(":").append(password).append("@")
                .append(apimConfig.getCarbonClientId()).append("/")
                .append(apimConfig.getCarbonVirtualHostName()).append("?brokerlist='tcp://")
                .append(apimConfig.getTopicServerHost()).append(":")
                .append(apimConfig.getTopicServerPort()).append("'").toString();
    }


    private void handleThrottleUpdateMessage(Map<String, Object> map) {

        String throttleKey = map.get(APIThrottleConstants.THROTTLE_KEY).toString();
        String throttleState = map.get(APIThrottleConstants.IS_THROTTLED).toString();
        Long timeStamp = Long.parseLong(map.get(APIThrottleConstants.EXPIRY_TIMESTAMP).toString());

        if (log.isDebugEnabled()) {
            log.debug("Received Key -  throttleKey : " + throttleKey + " , " +
                    "isThrottled :" + throttleState + " , expiryTime : " + new Date(timeStamp).toString());
        }

        if (APIThrottleConstants.TRUE.equalsIgnoreCase(throttleState)) {
            ThrottleDataHolder.getInstance().addThrottleData(throttleKey, timeStamp);

            String extractedKey = extractAPIorResourceKey(throttleKey);
            if (extractedKey != null) {
                if (!ThrottleDataHolder.getInstance().isAPIThrottled(extractedKey)) {
                    ThrottleDataHolder.getInstance().addThrottledAPIKey(extractedKey, timeStamp);
                    if (log.isDebugEnabled()) {
                        log.debug("Adding throttling key : " + extractedKey);
                    }
                }

            }
        } else {
            ThrottleDataHolder.getInstance().
                    removeThrottleData(throttleKey);
            String extractedKey = extractAPIorResourceKey(throttleKey);
            if (extractedKey != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Removing throttling key : " + extractedKey);
                }
                ThrottleDataHolder.getInstance().removeThrottledAPIKey(extractedKey);
            }
        }
    }

    // Synchronized due to blocking data contains or not can updated by multiple threads.
    // Will not be a performance issue as this will not happen more frequently
    private synchronized void handleBlockingMessage(Map<String, Object> map) {
        if (log.isDebugEnabled()) {
            log.debug("Received Key -  blockingCondition : " + map.get(APIConstants.BLOCKING_CONDITION_KEY).toString()
                    + " , " + "conditionValue :" + map.get(APIConstants.BLOCKING_CONDITION_VALUE).toString() + " , " +
                    "tenantDomain : " + map.get(APIConstants.BLOCKING_CONDITION_DOMAIN));
        }

        String condition = map.get(APIConstants.BLOCKING_CONDITION_KEY).toString();
        String conditionValue = map.get(APIConstants.BLOCKING_CONDITION_VALUE).toString();
        String conditionState = map.get(APIConstants.BLOCKING_CONDITION_STATE).toString();

        if (APIConstants.BLOCKING_CONDITIONS_APPLICATION.equals(condition)) {
            if (APIThrottleConstants.TRUE.equals(conditionState)) {
                ThrottleDataHolder.getInstance().addApplicationBlockingCondition(conditionValue, conditionValue);
            } else {
                ThrottleDataHolder.getInstance().removeApplicationBlockingCondition(conditionValue);
            }
        } else if (APIConstants.BLOCKING_CONDITIONS_API.equals(condition)) {
            if (APIThrottleConstants.TRUE.equals(conditionState)) {
                ThrottleDataHolder.getInstance().addAPIBlockingCondition(conditionValue, conditionValue);
            } else {
                ThrottleDataHolder.getInstance().removeAPIBlockingCondition(conditionValue);
            }
        } else if (APIConstants.BLOCKING_CONDITIONS_USER.equals(condition)) {
            if (APIThrottleConstants.TRUE.equals(conditionState)) {
                ThrottleDataHolder.getInstance().addUserBlockingCondition(conditionValue, conditionValue);
            } else {
                ThrottleDataHolder.getInstance().removeUserBlockingCondition(conditionValue);
            }
        } else if (APIConstants.BLOCKING_CONDITIONS_IP.equals(condition)) {
            if (APIThrottleConstants.TRUE.equals(conditionState)) {
                ThrottleDataHolder.getInstance().addIplockingCondition(conditionValue, conditionValue);
            } else {
                ThrottleDataHolder.getInstance().removeIpBlockingCondition(conditionValue);
            }
        }
    }

    private String extractAPIorResourceKey(String throttleKey) {
        Matcher m = resourcePattern.matcher(throttleKey);
        if (m.matches()) {
            if (m.groupCount() == RESOURCE_PATTERN_GROUPS) {
                String condition = m.group(RESOURCE_PATTERN_CONDITION_INDEX);
                String resourceKey = throttleKey.substring(0, throttleKey.indexOf("_" + condition));
                return resourceKey;
            }
        } else {
            m = apiPattern.matcher(throttleKey);
            if (m.matches()) {
                if (m.groupCount() == API_PATTERN_GROUPS) {
                    String condition = m.group(API_PATTERN_CONDITION_INDEX);
                    String resourceKey = throttleKey.substring(0, throttleKey.indexOf("_" + condition));
                    return resourceKey;
                }
            }
        }
        return null;
    }

    private synchronized void handleKeyTemplateMessage(Map<String, Object> map) {
        if (!log.isDebugEnabled()) {
            log.debug("Received Key -  KeyTemplate : " + map.get(APIConstants.POLICY_TEMPLATE_KEY).toString());
        }
        String keyTemplateValue = map.get(APIConstants.POLICY_TEMPLATE_KEY).toString();
        String keyTemplateState = map.get(APIConstants.TEMPLATE_KEY_STATE).toString();
        if (APIThrottleConstants.ADD.equals(keyTemplateState)) {
            ThrottleDataHolder.getInstance().addKeyTemplate(keyTemplateValue, keyTemplateValue);
        } else {
            ThrottleDataHolder.getInstance().removeKeyTemplate(keyTemplateValue);
        }
    }

    public boolean isSubscribed() {
        return isSubscribed;
    }
}
