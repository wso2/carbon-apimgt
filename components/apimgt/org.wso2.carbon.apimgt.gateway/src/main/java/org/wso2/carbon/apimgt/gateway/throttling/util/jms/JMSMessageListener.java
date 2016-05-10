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

package org.wso2.carbon.apimgt.gateway.throttling.util.jms;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.gateway.throttling.ThrottleDataHolder;
import org.wso2.carbon.apimgt.gateway.throttling.util.ThrottleConstants;
import org.wso2.carbon.apimgt.impl.APIConstants;


import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class JMSMessageListener implements MessageListener {

    private static final Log log = LogFactory.getLog(JMSMessageListener.class);

    public JMSMessageListener(ThrottleDataHolder throttleDataHolder) {
    }

    public void onMessage(Message message) {
        try {
            if (message != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Event received in JMS Event Receiver - " + message);
                }

                if (message instanceof MapMessage) {
                    MapMessage mapMessage = (MapMessage) message;
                    Map<String, Object> map = new HashMap<String, Object>();
                    Enumeration enumeration = mapMessage.getMapNames();
                    while (enumeration.hasMoreElements()) {
                        String key = (String) enumeration.nextElement();
                        map.put(key, mapMessage.getObject(key));
                    }

                    if (map.get(APIConstants.THROTTLE_KEY) != null) {
                        handleThrottleUpdateMessage(map);
                    } else if (map.get(APIConstants.BLOCKING_CONDITION_KEY) != null) {
                        handleBlockingMessage(map);
                    } else if (map.get(APIConstants.POLICY_TEMPLATE_KEY) != null) {
                        handleKeyTemplateMessage(map);
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

    private void handleThrottleUpdateMessage(Map<String, Object> map) {
        if (log.isDebugEnabled()) {
            log.debug("Received Key -  throttleKey : " + map.get("throttleKey").toString() + " , " +
                      "isThrottled :" + map.get("isThrottled").toString());
        }

        String throttleKey = map.get("throttleKey").toString();
        String throttleState = map.get("isThrottled").toString();
        long timeStamp = Long.parseLong(map.get("expiryTimeStamp").toString());
        if (ThrottleConstants.TRUE.equalsIgnoreCase(throttleState)) {
            ServiceReferenceHolder.getInstance().getThrottleDataHolder().
                    addThrottleData(throttleKey, timeStamp);
        } else {
            ServiceReferenceHolder.getInstance().getThrottleDataHolder().
                    removeThrottleData(throttleKey);
        }
    }

    //Synchronized due to blocking data contains or not can updated by multiple threads. Will not be a performance isssue
    //as this will not happen more frequently
    private synchronized void handleBlockingMessage(Map<String, Object> map) {
        if (log.isDebugEnabled()) {
            log.debug("Received Key -  blockingCondition : " + map.get(APIConstants.BLOCKING_CONDITION_KEY).toString() + " , " +
                      "conditionValue :" + map.get(APIConstants.BLOCKING_CONDITION_VALUE).toString() + " , " +
                      "tenantDomain : " + map.get(APIConstants.BLOCKING_CONDITION_DOMAIN));
        }

        String condition = map.get(APIConstants.BLOCKING_CONDITION_KEY).toString();
        String conditionValue = map.get(APIConstants.BLOCKING_CONDITION_VALUE).toString();
        String conditionState = map.get(APIConstants.BLOCKING_CONDITION_STATE).toString();

        if (APIConstants.BLOCKING_CONDITIONS_APPLICATION.equals(condition)) {
            if (ThrottleConstants.TRUE.equals(conditionState)) {
                ServiceReferenceHolder.getInstance().getThrottleDataHolder().addApplicationBlockingCondition(conditionValue, conditionValue);
            } else {
                ServiceReferenceHolder.getInstance().getThrottleDataHolder().removeApplicationBlockingCondition(conditionValue);
            }
        } else if (APIConstants.BLOCKING_CONDITIONS_API.equals(condition)) {
            if (ThrottleConstants.TRUE.equals(conditionState)) {
                ServiceReferenceHolder.getInstance().getThrottleDataHolder().addAPIBlockingCondition(conditionValue, conditionValue);
            } else {
                ServiceReferenceHolder.getInstance().getThrottleDataHolder().removeAPIBlockingCondition(conditionValue);
            }
        } else if (APIConstants.BLOCKING_CONDITIONS_USER.equals(condition)) {
            if (ThrottleConstants.TRUE.equals(conditionState)) {
                ServiceReferenceHolder.getInstance().getThrottleDataHolder().addUserBlockingCondition(conditionValue, conditionValue);
            } else {
                ServiceReferenceHolder.getInstance().getThrottleDataHolder().removeUserBlockingCondition(conditionValue);
            }
        } else if (APIConstants.BLOCKING_CONDITIONS_IP.equals(condition)) {
            if (ThrottleConstants.TRUE.equals(conditionState)) {
                ServiceReferenceHolder.getInstance().getThrottleDataHolder().addIplockingCondition(conditionValue, conditionValue);
            } else {
                ServiceReferenceHolder.getInstance().getThrottleDataHolder().removeIpBlockingCondition(conditionValue);
            }
        }
    }

    private synchronized void handleKeyTemplateMessage(Map<String, Object> map) {
        if (log.isDebugEnabled()) {
            log.debug("Received Key -  KeyTemplate : " + map.get(APIConstants.POLICY_TEMPLATE_KEY).toString());
        }
        String keyTemplateValue = map.get(APIConstants.POLICY_TEMPLATE_KEY).toString();
        String keyTemplateState = map.get(APIConstants.TEMPLATE_KEY_STATE).toString();
        if (ThrottleConstants.ADD.equals(keyTemplateState)) {
            ServiceReferenceHolder.getInstance().getThrottleDataHolder()
                    .addKeyTemplate(keyTemplateValue, keyTemplateValue);
        } else {
            ServiceReferenceHolder.getInstance().getThrottleDataHolder()
                    .removeKeyTemplate(keyTemplateValue);
        }
    }
}
