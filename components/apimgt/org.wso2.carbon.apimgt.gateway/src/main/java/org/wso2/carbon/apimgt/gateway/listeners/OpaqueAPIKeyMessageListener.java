/*
 *  Copyright (c) 2026, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.apimgt.api.model.APIKeyInfo;
import org.wso2.carbon.apimgt.gateway.internal.DataHolder;
import org.wso2.carbon.apimgt.impl.APIConstants;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import javax.jms.Topic;
import java.util.HashMap;

public class OpaqueAPIKeyMessageListener implements MessageListener {

    private static final Log log = LogFactory.getLog(OpaqueAPIKeyMessageListener.class);

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

                    if (APIConstants.TopicNames.OPAQUE_API_KEY_INFO.equalsIgnoreCase(jmsDestination.getTopicName())) {
                        if (payloadData.get(APIConstants.NotificationEvent.API_KEY_HASH).asText() != null) {
                            /*
                             * This message contains opaque api key data
                             */
                            handleOpaqueAPIKeyInfoMessage(payloadData.get(APIConstants.ENCODED_API_KEY_INFO).asText());
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

    private void handleOpaqueAPIKeyInfoMessage(String apiKeyInfoEvent) {

        if (StringUtils.isEmpty(apiKeyInfoEvent)) {
            return;
        }
        HashMap<String, Object> opaqueApiKeyInfoMap = base64Decode(apiKeyInfoEvent);
        if (opaqueApiKeyInfoMap.containsKey(APIConstants.NotificationEvent.LOOKUP_KEY) &&
                opaqueApiKeyInfoMap.get(APIConstants.NotificationEvent.LOOKUP_KEY) != null) {
            APIKeyInfo apiKeyInfo = new APIKeyInfo();
            apiKeyInfo.setLookupKey((String) opaqueApiKeyInfoMap.get(APIConstants.NotificationEvent.LOOKUP_KEY));
            apiKeyInfo.setApiKeyHash((String) opaqueApiKeyInfoMap.get(APIConstants.NotificationEvent.API_KEY_HASH));
            apiKeyInfo.setApplicationId((String) opaqueApiKeyInfoMap.get(APIConstants.NotificationEvent.APPLICATION_ID));
            apiKeyInfo.setSalt((String) opaqueApiKeyInfoMap.get(APIConstants.NotificationEvent.SALT));
            apiKeyInfo.setKeyType((String) opaqueApiKeyInfoMap.get(APIConstants.NotificationEvent.KEY_TYPE));
            apiKeyInfo.setValidityPeriod((Long) opaqueApiKeyInfoMap.get(APIConstants.NotificationEvent.VALIDITY_PERIOD));
            apiKeyInfo.setStatus((String) opaqueApiKeyInfoMap.get(APIConstants.NotificationEvent.STATUS));
            apiKeyInfo.setAdditionalProperties(((String) opaqueApiKeyInfoMap.get(APIConstants.NotificationEvent.ADDITIONAL_PROPERTIES)));
            DataHolder.getInstance().addOpaqueAPIKeyInfo(apiKeyInfo);
        }
    }

    private HashMap<String, Object> base64Decode(String encodedOpaqueAPIKeyInfo) {

        byte[] eventDecoded = Base64.decodeBase64(encodedOpaqueAPIKeyInfo);
        String eventJson = new String(eventDecoded);
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(eventJson, HashMap.class);
        } catch (JsonProcessingException e) {
            log.error("Error while decoding opaque api key event.");
        }
        return new HashMap<>();
    }
}
