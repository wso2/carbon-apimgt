/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.jwt.RevokedJWTDataHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.jms.Topic;
import java.util.HashMap;

public class GatewayTokenRevocationMessageListener implements MessageListener {

    private static final Log log = LogFactory.getLog(GatewayTokenRevocationMessageListener.class);

    public void onMessage(Message message) {

        try {
            if (message != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Event received in JMS Event Receiver - " + message);
                }
                Topic jmsDestination = (Topic) message.getJMSDestination();
                if (message instanceof TextMessage) {
                    String textMessage = ((TextMessage) message).getText();
                    JsonNode payloadData =  new ObjectMapper().readTree(textMessage).path(APIConstants.EVENT_PAYLOAD).
                            path(APIConstants.EVENT_PAYLOAD_DATA);

                    if (APIConstants.TopicNames.TOPIC_TOKEN_REVOCATION.equalsIgnoreCase(jmsDestination.getTopicName())) {
                        if (payloadData.get(APIConstants.REVOKED_TOKEN_KEY).asText() != null) {
                            /*
                             * This message contains revoked token data
                             * revokedToken - Revoked Token which should be removed from the cache
                             * expiryTime - ExpiryTime of the token if token is JWT, otherwise expiry is set to 0
                             */
                            handleRevokedTokenMessage(payloadData.get(APIConstants.REVOKED_TOKEN_KEY).asText(),
                                    payloadData.get(APIConstants.REVOKED_TOKEN_EXPIRY_TIME).asLong(),
                                    payloadData.get(APIConstants.REVOKED_TOKEN_TYPE).asText());
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

    private void handleRevokedTokenMessage(String revokedToken, long expiryTime, String tokenType) {

        boolean isJwtToken = false;
        if (StringUtils.isEmpty(revokedToken)) {
            return;
        }

        if (APIConstants.NotificationEvent.CONSUMER_APP_REVOCATION_EVENT.equals(tokenType)) {
            HashMap<String, Object> revokedTokenMap = base64Decode(revokedToken);
            if (revokedTokenMap.containsKey(APIConstants.NotificationEvent.CONSUMER_KEY) &&
                    revokedTokenMap.get(APIConstants.NotificationEvent.CONSUMER_KEY) != null &&
                    revokedTokenMap.containsKey(APIConstants.NotificationEvent.REVOCATION_TIME) &&
                    revokedTokenMap.get(APIConstants.NotificationEvent.REVOCATION_TIME) != null) {
                try {
                    RevokedJWTDataHolder.getInstance().addRevokedConsumerKeyToMap(
                            (String) revokedTokenMap.get(APIConstants.NotificationEvent.CONSUMER_KEY),
                            convertRevokedTime(revokedTokenMap));
                } catch (NumberFormatException e) {
                    log.warn("Event dropped due to unsupported value type for "
                            + APIConstants.NotificationEvent.REVOCATION_TIME + " : "
                            + revokedTokenMap.get(APIConstants.NotificationEvent.REVOCATION_TIME));
                }
            }
        } else if (APIConstants.NotificationEvent.SUBJECT_ENTITY_REVOCATION_EVENT.equals(tokenType)) {
            HashMap<String, Object> revokedTokenMap = base64Decode(revokedToken);
            if (revokedTokenMap.get(APIConstants.NotificationEvent.ENTITY_TYPE) != null &&
                    revokedTokenMap.get(APIConstants.NotificationEvent.REVOCATION_TIME) != null &&
                    revokedTokenMap.get(APIConstants.NotificationEvent.ENTITY_ID) != null) {
                String entityType = (String) revokedTokenMap.get(APIConstants.NotificationEvent.ENTITY_TYPE);
                long revocationTime = 0;
                try {
                    revocationTime = convertRevokedTime(revokedTokenMap);
                } catch (NumberFormatException e) {
                    log.warn("Event dropped due to unsupported value type for "
                            + APIConstants.NotificationEvent.REVOCATION_TIME + " : "
                            + revokedTokenMap.get(APIConstants.NotificationEvent.REVOCATION_TIME));
                }
                String entityId = (String) revokedTokenMap.get(APIConstants.NotificationEvent.ENTITY_ID);
                if (APIConstants.NotificationEvent.ENTITY_TYPE_USER_ID.equals(entityType)) {
                    RevokedJWTDataHolder.getInstance().addRevokedSubjectEntityUserToMap(entityId, revocationTime);
                } else if (APIConstants.NotificationEvent.ENTITY_TYPE_CLIENT_ID.equals(entityType)) {
                    RevokedJWTDataHolder.getInstance()
                            .addRevokedSubjectEntityConsumerAppToMap(entityId, revocationTime);
                }
            }
        } else {
            //handle JWT tokens
            if (APIConstants.API_KEY_AUTH_TYPE.equals(tokenType) || APIConstants.JWT.equals(tokenType)) {
                ServiceReferenceHolder.getInstance().getRevokedTokenService()
                        .addRevokedJWTIntoMap(revokedToken, expiryTime);
                // Add revoked token to revoked JWT map
                isJwtToken = true;
            }
            if (APIConstants.API_KEY_AUTH_TYPE.equals(tokenType)) {
                ServiceReferenceHolder.getInstance().getRevokedTokenService()
                        .removeApiKeyFromGatewayCache(revokedToken);
            } else {
                ServiceReferenceHolder.getInstance().getRevokedTokenService()
                        .removeTokenFromGatewayCache(revokedToken, isJwtToken);
            }
        }
    }

    private HashMap<String, Object> base64Decode(String encodedRevokedToken) {

        byte[] eventDecoded = Base64.decodeBase64(encodedRevokedToken);
        String eventJson = new String(eventDecoded);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(eventJson, HashMap.class);
        } catch (JsonProcessingException e) {
            log.error("Error while decoding revoked token event.");
        }
        return new HashMap<>();
    }

    private long convertRevokedTime(HashMap<String, Object> revokedTokenMap) throws NumberFormatException {

        return Long.parseLong((String) revokedTokenMap.get(APIConstants.NotificationEvent.REVOCATION_TIME));
    }
}
