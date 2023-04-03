/*
 *  Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.gateway.listeners;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.gateway.dto.APICondition;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.jms.Topic;

public class JMSMessageListener implements MessageListener {

    private static final Log log = LogFactory.getLog(JMSMessageListener.class);

    // These patterns will be used to determine for which type of keys the throttling condition has occurred.
    private Pattern apiPattern = Pattern.compile("/.*/(.*):\\1_(condition_(\\d*)|default)");
    private static final int API_PATTERN_GROUPS = 3;
    private static final int API_PATTERN_CONDITION_INDEX = 2;

    private Pattern resourcePattern = Pattern.compile("/.*/(.*)/\\1(.*)?:[A-Z]{0,7}_(condition_(\\d*)|default)");
    public static final int RESOURCE_PATTERN_GROUPS = 4;
    public static final int RESOURCE_PATTERN_CONDITION_INDEX = 3;

    private Pattern productResourcePattern = Pattern.compile("/.*/(.*):[A-Z]{0,5}_(condition_(\\d*)|default)");
    private static final int PRODUCT_RESOURCE_PATTERN_GROUPS = 3;
    private static final int PRODUCT_RESOURCE_CONDITION_INDEX = 2;

    private Pattern productAPIPattern = Pattern.compile("/.*:.*(condition_(\\d*)|default)");
    private static final int PRODUCT_API_PATTERN_GROUPS = 2;
    private static final int PRODUCT_API_CONDITION_INDEX = 1;

    public void onMessage(Message message) {

        try {
            if (message != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Event received in JMS Event Receiver - " + message);
                }
                Topic jmsDestination = (Topic) message.getJMSDestination();
                if (message instanceof TextMessage) {
                    String textMessage = ((TextMessage) message).getText();
                    JsonNode payloadData = new ObjectMapper().readTree(textMessage).path(APIConstants.EVENT_PAYLOAD).
                            path(APIConstants.EVENT_PAYLOAD_DATA);
                    if (APIConstants.TopicNames.TOPIC_THROTTLE_DATA.equalsIgnoreCase(jmsDestination.getTopicName())) {
                        if (payloadData.get(APIConstants.THROTTLE_KEY) != null) {
                            /*
                             * This message contains throttle data in map which contains Keys
                             * throttleKey - Key of particular throttling level
                             * isThrottled - Whether message has throttled or not
                             * expiryTimeStamp - When the throttling time window will expires
                             */

                            handleThrottleUpdateMessage(payloadData);
                        } else if (payloadData.get(APIConstants.BLOCKING_CONDITION_KEY) != null) {
                            /*
                             * This message contains blocking condition data
                             * blockingCondition - Blocking condition type
                             * conditionValue - blocking condition value
                             * state - State whether blocking condition is enabled or not
                             */
                            handleBlockingMessage(payloadData);
                        }
                    }
                }else{
                    log.warn("Event dropped due to unsupported message type " + message.getClass());
                }
            } else {
                log.warn("Dropping the empty/null event received through jms receiver");
            }
        } catch (JMSException e) {
            log.error("JMSException occurred when processing the received message ", e);
        } catch (ParseException e) {
            log.error("Error while processing evaluatedConditions", e);
        } catch (JsonProcessingException e) {
            log.error("Error while parsing JMS payload", e);
        }
    }

    private void handleThrottleUpdateMessage(JsonNode msg) throws ParseException {
        String throttleKey = msg.get(APIConstants.AdvancedThrottleConstants.THROTTLE_KEY).asText();
        String throttleState = msg.get(APIConstants.AdvancedThrottleConstants.IS_THROTTLED).asText();
        Long timeStamp = Long.parseLong(msg.get(APIConstants.AdvancedThrottleConstants.EXPIRY_TIMESTAMP).asText());
        Object evaluatedConditionObject = msg.get(APIConstants.AdvancedThrottleConstants.EVALUATED_CONDITIONS);

        if (log.isDebugEnabled()) {
            log.debug("Received Key -  throttleKey : " + throttleKey + " , " +
                    "isThrottled :" + throttleState + " , expiryTime : " + new Date(timeStamp).toString());
        }

        if (APIConstants.AdvancedThrottleConstants.TRUE.equalsIgnoreCase(throttleState)) {
            ServiceReferenceHolder.getInstance().getAPIThrottleDataService().
                    addThrottleData(throttleKey, timeStamp);

            APICondition extractedKey = extractAPIorResourceKey(throttleKey);

            if (extractedKey != null) {
                if (evaluatedConditionObject != null) {
                    ServiceReferenceHolder.getInstance().getAPIThrottleDataService().addThrottledApiConditions
                            (extractedKey.getResourceKey(), extractedKey.getName(), APIUtil.extractConditionDto(
                                    evaluatedConditionObject.toString()));
                }
                if (!ServiceReferenceHolder.getInstance().getAPIThrottleDataService().isAPIThrottled(extractedKey
                        .getResourceKey())) {
                    ServiceReferenceHolder.getInstance().getAPIThrottleDataService().addThrottledAPIKey(extractedKey
                            .getResourceKey(), timeStamp);
                    if (log.isDebugEnabled()) {
                        log.debug("Adding throttling key : " + extractedKey);
                    }
                }

            }
        } else {
            ServiceReferenceHolder.getInstance().getAPIThrottleDataService().removeThrottleData(throttleKey);
            APICondition extractedKey = extractAPIorResourceKey(throttleKey);
            if (extractedKey != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Removing throttling key : " + extractedKey.getResourceKey());
                }

                ServiceReferenceHolder.getInstance().getAPIThrottleDataService()
                        .removeThrottledAPIKey(extractedKey.getResourceKey());
                ServiceReferenceHolder.getInstance().getAPIThrottleDataService()
                        .removeThrottledApiConditions(extractedKey.getResourceKey(), extractedKey.getName());
            }
        }
    }

    //Synchronized due to blocking data contains or not can updated by multiple threads. Will not be a performance
    // isssue
    //as this will not happen more frequently
    private synchronized void handleBlockingMessage(JsonNode msg) {
        if (log.isDebugEnabled()) {
            log.debug("Received Key -  blockingCondition : " + msg.get(APIConstants.BLOCKING_CONDITION_KEY).asText() +
                    " , " +
                    "conditionValue :" + msg.get(APIConstants.BLOCKING_CONDITION_VALUE).asText() + " , " +
                    "tenantDomain : " + msg.get(APIConstants.BLOCKING_CONDITION_DOMAIN).asText());
        }

        String condition = msg.get(APIConstants.BLOCKING_CONDITION_KEY).asText();
        String conditionValue = msg.get(APIConstants.BLOCKING_CONDITION_VALUE).asText();
        String conditionState = msg.get(APIConstants.BLOCKING_CONDITION_STATE).asText();
        int conditionId = msg.get(APIConstants.BLOCKING_CONDITION_ID).asInt();
        String tenantDomain = msg.get(APIConstants.BLOCKING_CONDITION_DOMAIN).asText();

        if (APIConstants.BLOCKING_CONDITIONS_APPLICATION.equals(condition)) {
            if (APIConstants.AdvancedThrottleConstants.TRUE.equals(conditionState)) {
                ServiceReferenceHolder.getInstance().getAPIThrottleDataService()
                        .addBlockingCondition(APIConstants.BLOCKING_CONDITIONS_APPLICATION, conditionValue,
                                conditionValue);
            } else {
                ServiceReferenceHolder.getInstance().getAPIThrottleDataService()
                        .removeBlockCondition(APIConstants.BLOCKING_CONDITIONS_APPLICATION, conditionValue);
            }
        } else if (APIConstants.BLOCKING_CONDITIONS_API.equals(condition)) {
            if (APIConstants.AdvancedThrottleConstants.TRUE.equals(conditionState)) {
                ServiceReferenceHolder.getInstance().getAPIThrottleDataService()
                        .addBlockingCondition(APIConstants.BLOCKING_CONDITIONS_API, conditionValue, conditionValue);
            } else {
                ServiceReferenceHolder.getInstance().getAPIThrottleDataService()
                        .removeBlockCondition(APIConstants.BLOCKING_CONDITIONS_API, conditionValue);
            }
        } else if (APIConstants.BLOCKING_CONDITIONS_USER.equals(condition)) {
            if (APIConstants.AdvancedThrottleConstants.TRUE.equals(conditionState)) {
                ServiceReferenceHolder.getInstance().getAPIThrottleDataService()
                        .addBlockingCondition(APIConstants.BLOCKING_CONDITIONS_USER, conditionValue, conditionValue);
            } else {
                ServiceReferenceHolder.getInstance().getAPIThrottleDataService()
                        .removeBlockCondition(APIConstants.BLOCKING_CONDITIONS_USER, conditionValue);
            }
        } else if (APIConstants.BLOCKING_CONDITIONS_IP.equals(condition) ||
                APIConstants.BLOCK_CONDITION_IP_RANGE.equals(condition)) {
            if (APIConstants.AdvancedThrottleConstants.TRUE.equals(conditionState)) {
                ServiceReferenceHolder.getInstance().getAPIThrottleDataService()
                        .addIpBlockingCondition(tenantDomain, conditionId, conditionValue, condition);
            } else {
                ServiceReferenceHolder.getInstance().getAPIThrottleDataService().removeIpBlockingCondition(tenantDomain,
                        conditionId);
            }
        } else if (APIConstants.BLOCKING_CONDITIONS_SUBSCRIPTION.equals(condition)) {
            if (APIConstants.AdvancedThrottleConstants.TRUE.equals(conditionState)) {
                ServiceReferenceHolder.getInstance().getAPIThrottleDataService().addBlockingCondition(
                        APIConstants.BLOCKING_CONDITIONS_SUBSCRIPTION, conditionValue, conditionValue);
            } else {
                ServiceReferenceHolder.getInstance().getAPIThrottleDataService()
                        .removeBlockCondition(APIConstants.BLOCKING_CONDITIONS_SUBSCRIPTION,conditionValue);
            }
        }
    }

    private APICondition extractAPIorResourceKey(String throttleKey) {
        Matcher m = resourcePattern.matcher(throttleKey);
        if (m.matches()) {
            if (m.groupCount() == RESOURCE_PATTERN_GROUPS) {
                String condition = m.group(RESOURCE_PATTERN_CONDITION_INDEX);
                String resourceKey = throttleKey.substring(0, throttleKey.indexOf("_" + condition));
                return new APICondition(resourceKey, condition);
            }
        } else {
            m = apiPattern.matcher(throttleKey);
            if (m.matches()) {
                if (m.groupCount() == API_PATTERN_GROUPS) {
                    String condition = m.group(API_PATTERN_CONDITION_INDEX);
                    String resourceKey = throttleKey.substring(0, throttleKey.indexOf("_" + condition));
                    return new APICondition(resourceKey, condition);
                }
            }
        }
        // For API Products
        m = productResourcePattern.matcher(throttleKey);
        if (m.matches()) {
            if (m.groupCount() == PRODUCT_RESOURCE_PATTERN_GROUPS) {
                String condition = m.group(PRODUCT_RESOURCE_CONDITION_INDEX);
                String resourceKey = throttleKey.substring(0, throttleKey.indexOf("_" + condition));
                return new APICondition(resourceKey, condition);
            }
        } else {
            m = productAPIPattern.matcher(throttleKey);
            if (m.matches()) {
                if (m.groupCount() == PRODUCT_API_PATTERN_GROUPS) {
                    String condition = m.group(PRODUCT_API_CONDITION_INDEX);
                    String resourceKey = throttleKey.substring(0, throttleKey.indexOf("_" + condition));
                    return new APICondition(resourceKey, condition);
                }
            }
        }
        return null;
    }
}
