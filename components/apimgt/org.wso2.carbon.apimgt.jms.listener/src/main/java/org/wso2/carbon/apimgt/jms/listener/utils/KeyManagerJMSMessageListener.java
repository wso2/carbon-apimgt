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

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.jms.listener.internal.ServiceReferenceHolder;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.Topic;

public class KeyManagerJMSMessageListener implements MessageListener {

    private static final Log log = LogFactory.getLog(KeyManagerJMSMessageListener.class);

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
                    if (JMSConstants.TOPIC_KEY_MANAGER.equalsIgnoreCase(jmsDestination.getTopicName())) {
                        if (APIConstants.KeyManager.KeyManagerEvent.KEY_MANAGER_CONFIGURATION
                                .equals(map.get(APIConstants.KeyManager.KeyManagerEvent.EVENT_TYPE))) {
                            String name = (String) map.get(APIConstants.KeyManager.KeyManagerEvent.NAME);
                            String tenantDomain =
                                    (String) map.get(APIConstants.KeyManager.KeyManagerEvent.TENANT_DOMAIN);
                            String action = (String) map.get(APIConstants.KeyManager.KeyManagerEvent.ACTION);
                            String type = (String) map.get(APIConstants.KeyManager.KeyManagerEvent.TYPE);
                            boolean enabled = (Boolean) map.get(APIConstants.KeyManager.KeyManagerEvent.ENABLED);
                            Object value = map.get(APIConstants.KeyManager.KeyManagerEvent.VALUE);
                            if (value != null && StringUtils.isNotEmpty((String) value)){
                                KeyManagerConfiguration keyManagerConfiguration =
                                        APIUtil.toKeyManagerConfiguration((String) value);
                                keyManagerConfiguration.setEnabled(enabled);
                                if (APIConstants.KeyManager.KeyManagerEvent.ACTION_ADD.equals(action)) {
                                    ServiceReferenceHolder.getInstance().getKeyManagerService()
                                            .addKeyManagerConfiguration(tenantDomain, name, type, keyManagerConfiguration);
                                }
                                if (APIConstants.KeyManager.KeyManagerEvent.ACTION_UPDATE.equals(action)) {
                                    ServiceReferenceHolder.getInstance().getKeyManagerService()
                                            .updateKeyManagerConfiguration(tenantDomain, name, type,
                                                    keyManagerConfiguration);
                                }
                            }
                            if (APIConstants.KeyManager.KeyManagerEvent.ACTION_DELETE.equals(action)) {
                                ServiceReferenceHolder.getInstance().getKeyManagerService()
                                        .removeKeyManagerConfiguration(tenantDomain, name);
                            }
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
        } catch (APIManagementException e) {
            log.error("Error occurred while registering Key Manager", e);
        }
    }

}
