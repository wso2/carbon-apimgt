/*
 *
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  n compliance with the License.
 *  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.apimgt.jms.listener.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.common.jms.JMSConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.correlation.CorrelationConfigManager;
import org.wso2.carbon.apimgt.impl.notifier.events.CorrelationConfigEvent;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.jms.Topic;

/**
 * The JMS Message Listener for Correlation Configs.
 */
public class CorrelationConfigJMSMessageListener implements MessageListener {

    private static final Log log = LogFactory.getLog(CorrelationConfigJMSMessageListener.class);
    private boolean debugEnabled = log.isDebugEnabled();
    @Override public void onMessage(Message message) {
        try {
            if (message != null) {
                if (debugEnabled) {
                    log.debug("Event received in JMS Event Receiver - " + message);
                }
                Topic jmsDestination = (Topic) message.getJMSDestination();
                if (message instanceof TextMessage) {
                    String textMessage = ((TextMessage) message).getText();
                    JsonNode payloadData = new ObjectMapper().readTree(textMessage).path(APIConstants.EVENT_PAYLOAD).
                            path(APIConstants.EVENT_PAYLOAD_DATA);
                    if (JMSConstants.TOPIC_NOTIFICATION.equalsIgnoreCase(jmsDestination.getTopicName())) {
                        if (payloadData.get(APIConstants.EVENT_TYPE).asText() != null) {
                            /*
                             * This message contains notification
                             * eventType - type of the event
                             * timestamp - system time of the event published
                             * event - event data
                             */
                            if (debugEnabled) {
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
        }
    }

    private void handleNotificationMessage(String eventType, String encodedEvent) {
        byte[] eventDecoded = Base64.decodeBase64(encodedEvent);
        String eventJson = new String(eventDecoded);

        if (APIConstants.EventType.UPDATE_CORRELATION_CONFIGS.toString().equals(eventType)) {
            CorrelationConfigEvent correlationConfigEvent  =
                    new Gson().fromJson(eventJson, CorrelationConfigEvent.class);
            CorrelationConfigManager.getInstance().updateCorrelationConfigs(
                    correlationConfigEvent.getCorrelationConfigDTOList());
        }
    }
}
