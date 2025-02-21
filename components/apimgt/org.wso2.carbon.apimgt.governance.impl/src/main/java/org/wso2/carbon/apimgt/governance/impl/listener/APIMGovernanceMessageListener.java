/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.impl.listener;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.governance.api.error.APIMGovernanceException;
import org.wso2.carbon.apimgt.governance.impl.ComplianceManager;
import org.wso2.carbon.apimgt.governance.impl.PolicyManager;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.notifier.events.LabelEvent;

import java.nio.charset.StandardCharsets;
import java.util.Set;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.jms.Topic;

/**
 * This class will listen to the JMS events published by the API Manager and handle the events
 */
public class APIMGovernanceMessageListener implements MessageListener {

    private static final Log log = LogFactory.getLog(APIMGovernanceMessageListener.class);


    /**
     * This method will be called when a message is received in the JMS Event Receiver
     *
     * @param message The received message
     */
    @Override
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
                    if (APIConstants.TopicNames.TOPIC_NOTIFICATION.equalsIgnoreCase(jmsDestination.getTopicName())) {
                        if (payloadData.get(APIConstants.EVENT_TYPE).asText() != null) {
                            /*
                             * This message contains notification
                             * eventType - type of the event
                             * timestamp - system time of the event published
                             * event - event data
                             */
                            if (log.isDebugEnabled()) {
                                log.debug("Event received from the topic of " + jmsDestination.getTopicName());
                            }
                            handleNotificationMessage(payloadData.get(APIConstants.EVENT_TYPE).asText(),
                                    payloadData.get(APIConstants.EVENT_PAYLOAD).asText());
                        }
                    }
                } else {
                    log.warn("Event dropped due to unsupported message type " + message.getClass());
                }
            } else {
                log.warn("Dropping the empty/null event received through jms receiver");
            }
        } catch (JMSException | JsonProcessingException e) {
            log.error("JMSException occurred when processing the received message ", e);
        } catch (InterruptedException e) {
            log.error("Error occurred while waiting to retrieve artifacts from event hub", e);
        }
    }

    /**
     * This method will handle the notification message received from the JMS Event Receiver
     *
     * @param eventType    Type of the event
     * @param eventEncoded Encoded event data
     * @throws InterruptedException If an error occurs while waiting to retrieve artifacts from event hub
     */
    private void handleNotificationMessage(String eventType, String eventEncoded) throws InterruptedException {
        byte[] eventDecoded = Base64.decodeBase64(eventEncoded);
        String eventJson = new String(eventDecoded, StandardCharsets.UTF_8);

        if (APIConstants.EventType.LABEL_DELETE.toString().equals(eventType)) {
            LabelEvent event = new Gson().fromJson(eventJson, LabelEvent.class);
            String label = event.getLabelId();
            String organization = event.getTenantDomain();
            if (label == null) {
                log.warn("Label ID is not provided in the event payload");
                return;
            } else if (organization == null) {
                log.warn("Organization is not provided in the event payload");
                return;
            }
            if (log.isDebugEnabled()) {
                log.debug("Label delete event received for label ID: " + label);
            }
            // Handle label delete event
            try {
                PolicyManager policyManager = new PolicyManager();
                ComplianceManager complianceManager = new ComplianceManager();
                Set<String> policies = policyManager.getPoliciesByLabel(label, organization).keySet();
                policyManager.deleteLabelPolicyMappings(label);
                for (String policyId : policies) {
                    complianceManager.handlePolicyChangeEvent(policyId, organization);
                }
            } catch (APIMGovernanceException e) {
                log.error("Error occurred while handling label delete event", e);
            }

        }
    }
}
