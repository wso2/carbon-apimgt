/*
 * Copyright (c) 2025, WSO2 LLC. (https://www.wso2.com/).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.jms.listener.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.certificatemgt.PeerNodeCertificateManager;
import org.wso2.carbon.apimgt.impl.notifier.events.CertificateEvent;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.jms.Topic;
import java.nio.charset.StandardCharsets;

/**
 * This class listens for and processes JMS messages related to certificate management, performing actions based on the
 * message content and type.
 */
public class CertificateManagerJMSMessageListener implements MessageListener {

    private static final Log log = LogFactory.getLog(CertificateManagerJMSMessageListener.class);
    private static final Gson gson = new Gson();

    /**
     * Handles incoming JMS messages and processes them based on their content and type. This method is triggered
     * whenever a message is received by the JMS Event Receiver.
     *
     * @param message The message received from the JMS topic.
     */
    @Override
    public void onMessage(Message message) {

        try {
            if (message != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Event received in JMS Event Receiver - " + message);
                }
                javax.jms.Destination destination = message.getJMSDestination();
                if (!(destination instanceof Topic)) {
                    log.warn("Skipping message from non-topic destination: " + destination);
                    return;
                }
                Topic jmsDestination = (Topic) destination;
                if (message instanceof TextMessage) {
                    String textMessage = ((TextMessage) message).getText();
                    JsonNode payloadData = new ObjectMapper().readTree(textMessage).path(APIConstants.EVENT_PAYLOAD).
                            path(APIConstants.EVENT_PAYLOAD_DATA);

                    if (APIConstants.TopicNames.TOPIC_NOTIFICATION.equalsIgnoreCase(jmsDestination.getTopicName())) {
                        if (payloadData.get(APIConstants.EVENT_TYPE).asText() != null) {
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
        }
    }

    /**
     * Handles a notification message by decoding the event payload and performing appropriate actions based on the
     * event type.
     *
     * @param eventType    The type of the event.
     * @param encodedEvent The Base64-encoded event payload containing event details.
     */
    private void handleNotificationMessage(String eventType, String encodedEvent) {

        byte[] eventDecoded = Base64.decodeBase64(encodedEvent);
        String eventJson = new String(eventDecoded, StandardCharsets.UTF_8);

        if (APIConstants.EventType.ENDPOINT_CERTIFICATE_ADD.toString().equals(eventType)) {
            CertificateEvent certificateEvent = gson.fromJson(eventJson, CertificateEvent.class);
            if (log.isDebugEnabled()) {
                log.debug("Adding certificate to peer node. Alias: " + certificateEvent.getAlias() + ", Endpoint: "
                        + certificateEvent.getEndpoint() + ", TenantId: " + certificateEvent.getTenantId());
            }
            PeerNodeCertificateManager.getInstance().addCertificateToPeerNode(certificateEvent.getAlias(),
                    certificateEvent.getEndpoint(), certificateEvent.getTenantId());
        } else if (APIConstants.EventType.ENDPOINT_CERTIFICATE_REMOVE.toString().equals(eventType)) {
            CertificateEvent certificateEvent = gson.fromJson(eventJson, CertificateEvent.class);
            if (log.isDebugEnabled()) {
                log.debug("Removing certificate from peer node. Alias: " + certificateEvent.getAlias());
            }
            PeerNodeCertificateManager.getInstance().deleteCertificateFromPeerNode(certificateEvent.getAlias());
        }
    }
}
