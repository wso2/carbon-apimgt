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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.text.StringEscapeUtils;
import org.wso2.carbon.apimgt.api.model.APIKeyInfo;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.Map;

public class OpaqueAPIKeyInfoListener implements MessageListener {

    private static final Log log = LogFactory.getLog(OpaqueAPIKeyInfoListener.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public void onMessage(Message message) {

        if (log.isDebugEnabled()) {
            log.debug("Opaque API Key Info JMS message received");
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

                    APIKeyInfo apiKeyInfo = new APIKeyInfo();
                    apiKeyInfo.setApiKeyHash(payload.path(APIConstants.NotificationEvent.API_KEY_HASH).asText());
                    apiKeyInfo.setSalt(payload.path(APIConstants.NotificationEvent.SALT).asText());
                    apiKeyInfo.setKeyType(payload.path(APIConstants.NotificationEvent.KEY_TYPE).asText());
                    apiKeyInfo.setOrigin(payload.path(APIConstants.NotificationEvent.ORIGIN).asText());
                    if (apiKeyInfo.getOrigin().equalsIgnoreCase("APP")) {
                        apiKeyInfo.setAppId(payload.path(APIConstants.NotificationEvent.APPLICATION_ID).asInt());
                    } else if (apiKeyInfo.getOrigin().equalsIgnoreCase("API")) {
                        apiKeyInfo.setApiId(payload.path(APIConstants.NotificationEvent.API_ID).asInt());
                    }
                    apiKeyInfo.setStatus(payload.path(APIConstants.NotificationEvent.STATUS).asText());
                    apiKeyInfo.setValidityPeriod(payload.path(APIConstants.NotificationEvent.VALIDITY_PERIOD).asLong());
                    apiKeyInfo.setLookupKey(payload.path(APIConstants.NotificationEvent.LOOKUP_KEY).asText());

                    String additionalPropsEscaped = payload.path(APIConstants.NotificationEvent.ADDITIONAL_PROPERTIES).
                            asText(null);
                    Map<String, String> additionalPropsMap = null;
                    if (additionalPropsEscaped != null) {
                        // Unescape and convert back to Map
                        String unescaped = StringEscapeUtils.unescapeJson(additionalPropsEscaped);
                        additionalPropsMap = new ObjectMapper().readValue(unescaped,
                                new TypeReference<Map<String, String>>() {});
                    }
                    apiKeyInfo.setAdditionalProperties(additionalPropsMap);

                    // Add to GW cache
                    DataHolder.getInstance().addOpaqueAPIKeyInfo(apiKeyInfo);
                }
            } else {
                log.warn("Dropping the empty/null event received through jms receiver");
            }
        } catch (JMSException | JsonProcessingException e) {
            log.error("JMSException occurred when processing the received message ", e);
        }
    }
}
