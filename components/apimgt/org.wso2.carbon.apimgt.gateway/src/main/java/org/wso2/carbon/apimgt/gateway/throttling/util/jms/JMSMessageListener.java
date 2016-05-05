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

                    if(map.get(APIConstants.THROTTLE_KEY) != null){
                        handleThrottleUpdateMessage(map);
                    } else if(map.get(APIConstants.BLOCKING_CONDITION_KEY) != null){
                        handleBlockingMessage(map);
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
        if (throttleState.equals("true")) {
            ServiceReferenceHolder.getInstance().getThrottleDataHolder().
                    getThrottleDataMap().put(throttleKey, throttleState);
        } else {
            ServiceReferenceHolder.getInstance().getThrottleDataHolder().
                    getThrottleDataMap().remove(throttleKey);
        }
    }

    private void handleBlockingMessage(Map<String, Object> map) {
        if (log.isDebugEnabled()) {
            log.debug("Received Key -  blockingCondition : " + map.get(APIConstants.BLOCKING_CONDITION_KEY).toString() + " , " +
                      "conditionValue :" + map.get(APIConstants.BLOCKING_CONDITION_VALUE).toString() + " , " +
                      "tenantDomain : " + map.get(APIConstants.BLOCKING_CONDITION_DOMAIN));
        }

        String condition = map.get(APIConstants.BLOCKING_CONDITION_KEY).toString();
        String conditionValue = map.get(APIConstants.BLOCKING_CONDITION_VALUE).toString();
        String conditionState = map.get(APIConstants.BLOCKING_CONDITION_STATE).toString();
        String tenantDomain = map.get(APIConstants.BLOCKING_CONDITION_DOMAIN).toString();

        Map<String,String> blockingMap = null;

        if(APIConstants.BLOCKING_CONDITIONS_APPLICATION.equals(condition)){
            blockingMap =ServiceReferenceHolder.getInstance().getThrottleDataHolder()
                    .getBlockedApplicationConditionsMap();
        } else if(APIConstants.BLOCKING_CONDITIONS_API.equals(condition)){
            blockingMap = ServiceReferenceHolder.getInstance().getThrottleDataHolder().getBlockedAPIConditionsMap();
        }else if(APIConstants.BLOCKING_CONDITIONS_USER.equals(condition)){
            blockingMap = ServiceReferenceHolder.getInstance().getThrottleDataHolder().getBlockedUserConditionsMap();
        } else if (APIConstants.BLOCKING_CONDITIONS_IP.equals(condition)){
            blockingMap = ServiceReferenceHolder.getInstance().getThrottleDataHolder().getBlockedIpConditionsMap();
        }

        if("true".equals(conditionState)){
            blockingMap.put(conditionValue,conditionValue);
        }else {
            blockingMap.remove(conditionValue);
        }
    }
}
