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

package org.wso2.carbon.apimgt.jms.listener.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.common.jms.JMSConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.jms.listener.internal.ServiceReferenceHolder;


import javax.jms.*;

public class KeyManagerJMSMessageListener implements MessageListener {

    private static final Log log = LogFactory.getLog(KeyManagerJMSMessageListener.class);

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
                    if (JMSConstants.TOPIC_KEY_MANAGER.equalsIgnoreCase(jmsDestination.getTopicName())) {
                        if (APIConstants.KeyManager.KeyManagerEvent.KEY_MANAGER_CONFIGURATION
                                .equals(payloadData.get(APIConstants.KeyManager.KeyManagerEvent.EVENT_TYPE).asText())) {
                            String name = payloadData.get(APIConstants.KeyManager.KeyManagerEvent.NAME).asText();
                            String organization =
                                    payloadData.get(APIConstants.KeyManager.KeyManagerEvent.ORGANIZATION).asText();
                            String action = payloadData.get(APIConstants.KeyManager.KeyManagerEvent.ACTION).asText();
                            String type = payloadData.get(APIConstants.KeyManager.KeyManagerEvent.TYPE).asText();
                            String value = payloadData.get(APIConstants.KeyManager.KeyManagerEvent.VALUE).asText();
                            if (StringUtils.isNotEmpty(value)) {
                                KeyManagerConfiguration keyManagerConfiguration =
                                        APIUtil.toKeyManagerConfiguration(value);
                                if (APIConstants.KeyManager.KeyManagerEvent.ACTION_ADD.equals(action)) {
                                    ServiceReferenceHolder.getInstance().getKeyManagerService()
                                            .addKeyManagerConfiguration(organization, name, type,
                                                    keyManagerConfiguration);
                                }
                                if (APIConstants.KeyManager.KeyManagerEvent.ACTION_UPDATE.equals(action)) {
                                    ServiceReferenceHolder.getInstance().getKeyManagerService()
                                            .updateKeyManagerConfiguration(organization, name, type,
                                                    keyManagerConfiguration);
                                }
                            }
                            if (APIConstants.KeyManager.KeyManagerEvent.ACTION_DELETE.equals(action)) {
                                ServiceReferenceHolder.getInstance().getKeyManagerService()
                                        .removeKeyManagerConfiguration(organization, name);
                            }
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
        } catch (APIManagementException e) {
            log.error("Error occurred while registering Key Manager", e);
        }
    }

}
