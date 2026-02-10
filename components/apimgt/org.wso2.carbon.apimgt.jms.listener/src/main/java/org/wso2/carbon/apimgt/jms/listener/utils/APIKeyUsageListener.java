/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
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

package org.wso2.carbon.apimgt.jms.listener.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

/**
 * API key usage listener to handle API key usage events.
 */
public class APIKeyUsageListener implements MessageListener {

    private static final Log log = LogFactory.getLog(APIKeyUsageListener.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onMessage(Message message) {

        if (log.isDebugEnabled()) {
            log.debug("API Key Usage JMS message received");
        }

        try {
            if (message != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Event received in JMS Event Receiver - " + message);
                }
                if (message instanceof TextMessage) {
                    String textMessage = ((TextMessage) message).getText();
                    // Navigate to payloadData
                    JsonNode payloadData = objectMapper.readTree(textMessage).path("event").path("payloadData");

                    String apiKeyHash = payloadData.get(APIConstants.NotificationEvent.API_KEY_HASH).asText();
                    String lastUsedTime = payloadData.get(APIConstants.NotificationEvent.LAST_USED_TIME).asText();

                    if (apiKeyHash != null && !apiKeyHash.isEmpty()) {
                        ApiMgtDAO.getInstance().updateAPIKeyUsage(apiKeyHash, lastUsedTime);
                    } else {
                        log.warn("Received API key usage event with empty apiKeyHash.");
                    }
                }
            } else {
                log.warn("Dropping the empty/null event received through jms receiver.");
            }
        } catch (JMSException | JsonProcessingException | APIManagementException e) {
            log.error("Error occurred when processing the API key usage message ", e);
        }
    }
}



