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
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;

import javax.jms.*;

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

    private void handleRevokedTokenMessage(String revokedToken, long expiryTime,String tokenType) {

        boolean isJwtToken = false;
        if (StringUtils.isEmpty(revokedToken)) {
            return;
        }

        //handle JWT tokens
        if (APIConstants.API_KEY_AUTH_TYPE.equals(tokenType) || APIConstants.JWT.equals(tokenType)) {
            ServiceReferenceHolder.getInstance().getRevokedTokenService()
                    .addRevokedJWTIntoMap(revokedToken, expiryTime);
            // Add revoked token to revoked JWT map
            isJwtToken = true;
        }
        ServiceReferenceHolder.getInstance().getRevokedTokenService()
                .removeTokenFromGatewayCache(revokedToken, isJwtToken);
    }
}
