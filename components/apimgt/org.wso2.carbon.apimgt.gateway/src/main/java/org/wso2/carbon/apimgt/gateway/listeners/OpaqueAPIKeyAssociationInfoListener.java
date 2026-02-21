/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com/).
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

package org.wso2.carbon.apimgt.gateway.listeners;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.model.APIKeyInfo;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

public class OpaqueAPIKeyAssociationInfoListener implements MessageListener {

    private static final Log log = LogFactory.getLog(OpaqueAPIKeyAssociationInfoListener.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public void onMessage(Message message) {

        if (log.isDebugEnabled()) {
            log.debug("Opaque API Key Association Info JMS message received");
        }

        try {
            if (message != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Event received in JMS Event Receiver - " + message);
                }
                if (message instanceof TextMessage) {
                    String textMessage = ((TextMessage) message).getText();
                    // Navigate to payloadData
                    JsonNode payload = null;
                    payload = objectMapper.readTree(textMessage)
                                .path("event")
                                .path("payloadData");

                    APIKeyInfo apiKeyInfo;
                    String lookupKey = "Api|" + payload.path(APIConstants.NotificationEvent.API_KEY_HASH).asText();
                    if (payload.path(APIConstants.NotificationEvent.ASSOCIATION_TYPE).asText().
                            equalsIgnoreCase("CREATE_ASSOCIATION")) {
                        apiKeyInfo = DataHolder.getInstance().getOpaqueAPIKeyInfo(lookupKey);
                        if (apiKeyInfo != null) {
                            APIKeyInfo updated = new APIKeyInfo();
                            updated.setKeyDisplayName(apiKeyInfo.getKeyDisplayName());
                            updated.setApiKeyHash(apiKeyInfo.getApiKeyHash());
                            updated.setLookupKey(apiKeyInfo.getLookupKey());
                            updated.setKeyType(apiKeyInfo.getKeyType());
                            updated.setAuthUser(apiKeyInfo.getAuthUser());
                            updated.setCreatedTime(apiKeyInfo.getCreatedTime());
                            updated.setValidityPeriod(apiKeyInfo.getValidityPeriod());
                            updated.setApplicationId(payload.path(APIConstants.NotificationEvent.APPLICATION_UUID).asText());
                            updated.setAppId(payload.path(APIConstants.NotificationEvent.APPLICATION_ID).asInt());
                            DataHolder.getInstance().addOpaqueAPIKeyInfo(updated);
                        }
                    } else if (payload.path(APIConstants.NotificationEvent.ASSOCIATION_TYPE).asText().
                            equalsIgnoreCase("REMOVE_ASSOCIATION")) {
                        apiKeyInfo = DataHolder.getInstance().getOpaqueAPIKeyInfo(lookupKey);
                        if (apiKeyInfo != null) {
                            APIKeyInfo updated = new APIKeyInfo();
                            updated.setKeyDisplayName(apiKeyInfo.getKeyDisplayName());
                            updated.setApiKeyHash(apiKeyInfo.getApiKeyHash());
                            updated.setLookupKey(apiKeyInfo.getLookupKey());
                            updated.setKeyType(apiKeyInfo.getKeyType());
                            updated.setAuthUser(apiKeyInfo.getAuthUser());
                            updated.setCreatedTime(apiKeyInfo.getCreatedTime());
                            updated.setValidityPeriod(apiKeyInfo.getValidityPeriod());
                            updated.setApplicationId(null);
                            updated.setAppId(0);
                            DataHolder.getInstance().addOpaqueAPIKeyInfo(updated);
                        }
                    }
                }
            } else {
                log.warn("Dropping the empty/null event received through jms receiver");
            }
        } catch (JMSException | JsonProcessingException e) {
            log.error("JMSException occurred when processing the received message ", e);
        }
    }
}
