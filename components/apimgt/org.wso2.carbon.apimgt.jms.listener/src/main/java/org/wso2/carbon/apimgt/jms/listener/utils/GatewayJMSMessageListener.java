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

package org.wso2.carbon.apimgt.jms.listener.utils;

import com.google.gson.Gson;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.InMemoryAPIDeployer;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.apimgt.impl.notifier.events.DeployAPIInGatewayEvent;
import org.wso2.carbon.apimgt.jms.listener.internal.ServiceReferenceHolder;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;

public class GatewayJMSMessageListener implements MessageListener {

    private static final Log log = LogFactory.getLog(GatewayJMSMessageListener.class);
    private InMemoryAPIDeployer inMemoryApiDeployer = new InMemoryAPIDeployer();
    GatewayArtifactSynchronizerProperties gatewayArtifactSynchronizerProperties = ServiceReferenceHolder
            .getInstance().getAPIMConfiguration().getGatewayArtifactSynchronizerProperties();

    public void onMessage(Message message) {

        try {
            if (message != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Event received in JMS Event Receiver - " + message);
                }
                Topic jmsDestination = (Topic) message.getJMSDestination();
                if (message instanceof MapMessage) {
                    MapMessage mapMessage = (MapMessage) message;
                    Map<String, Object> map = new HashMap<String, Object>();
                    Enumeration enumeration = mapMessage.getMapNames();
                    while (enumeration.hasMoreElements()) {
                        String key = (String) enumeration.nextElement();
                        map.put(key, mapMessage.getObject(key));
                    }
                    if (JMSConstants.TOPIC_NOTIFICATION.equalsIgnoreCase(jmsDestination.getTopicName())) {
                        if (map.get(APIConstants.EVENT_TYPE) !=
                                null) {
                            /*
                             * This message contains notification
                             * eventType - type of the event
                             * timestamp - system time of the event published
                             * event - event data
                             */
                            handleNotificationMessage((String) map.get(APIConstants.EVENT_TYPE),
                                    (Long) map.get(APIConstants.EVENT_TIMESTAMP),
                                    (String) map.get(APIConstants.EVENT_PAYLOAD));
                        }
                    }

                } else {
                    log.warn("Event dropped due to unsupported message type " + message.getClass());
                }
            } else {
                log.warn("Dropping the empty/null event received through jms receiver");
            }
        } catch (JMSException e) {
            log.error("JMSException occurred when processing the received message ", e);
        }
    }

    private void handleNotificationMessage(String eventType, long timestamp, String event) {

        byte[] eventDecoded = Base64.decodeBase64(event);

        if ((APIConstants.EventType.DEPLOY_API_IN_GATEWAY.name().equals(eventType)
                || APIConstants.EventType.REMOVE_API_FROM_GATEWAY.name().equals(eventType))
                && gatewayArtifactSynchronizerProperties.isRetrieveFromStorage()) {
            DeployAPIInGatewayEvent gatewayEvent = new Gson().fromJson(new String(eventDecoded),
                    DeployAPIInGatewayEvent.class);
            if (gatewayArtifactSynchronizerProperties.getGatewayLabels().contains(gatewayEvent.getGatewayLabel())) {
                if (APIConstants.EventType.DEPLOY_API_IN_GATEWAY.name().equals(eventType)) {
                    inMemoryApiDeployer.deployAPI(gatewayEvent.getApiId(), gatewayEvent.getGatewayLabel());
                } else if (APIConstants.EventType.REMOVE_API_FROM_GATEWAY.name().equals(eventType)) {
                    inMemoryApiDeployer.unDeployAPI(gatewayEvent.getApiId(), gatewayEvent.getGatewayLabel());
                }
            }
        }
    }
}
