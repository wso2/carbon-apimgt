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

package org.wso2.carbon.apimgt.gateway.throttling;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.databridge.agent.DataPublisher;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class will hold throttle data per given node. All throttle handler objects should refer values from this.
 * When throttle data holder initialize it should read complete throttle decision table from global policy engine
 * via web service calls. In addition to that it should subscribe to topic and listen throttle updates.
 */

public class ThrottleDataHolder {

    private static final Log log = LogFactory.getLog(ThrottleDataHolder.class);
    private String streamID;

    public Map<String, String> getThrottleDataMap() {
        return throttleDataMap;
    }

    public void setThrottleDataMap(Map<String, String> throttleDataMap) {
        this.throttleDataMap = throttleDataMap;
    }


    public Map<String, String> getBlockedAPIConditionsMap() {
        return blockedAPIConditionsMap;
    }

    public void setBlockedAPIConditionsMap(Map<String, String> blockedAPIConditionsMap) {
        this.blockedAPIConditionsMap = blockedAPIConditionsMap;
    }

    public Map<String, String> getBlockedApplicationConditionsMap() {
        return blockedApplicationConditionsMap;
    }

    public void setBlockedApplicationConditionsMap(Map<String, String> blockedApplicationConditionsMap) {
        this.blockedApplicationConditionsMap = blockedApplicationConditionsMap;
    }

    public Map<String, String> getBlockedUserConditionsMap() {
        return blockedUserConditionsMap;
    }

    public void setBlockedUserConditionsMap(Map<String, String> blockedUserConditionsMap) {
        this.blockedUserConditionsMap = blockedUserConditionsMap;
    }

    public Map<String, String> getBlockedCustomConditionsMap() {
        return blockedCustomConditionsMap;
    }

    public void setBlockedCustomConditionsMap(Map<String, String> blockedCustomConditionsMap) {
        this.blockedCustomConditionsMap = blockedCustomConditionsMap;
    }

    Map<String, String> blockedAPIConditionsMap = new ConcurrentHashMap();
    Map<String, String> blockedApplicationConditionsMap = new ConcurrentHashMap();
    Map<String, String> blockedUserConditionsMap = new ConcurrentHashMap();
    Map<String, String> blockedCustomConditionsMap = new ConcurrentHashMap();
    Map<String, String> blockedIpConditionsMap = new ConcurrentHashMap();
    Map<String, String> keyTemplateMap = new ConcurrentHashMap();

    public Map<String, String> getKeyTemplateMap() {
        return keyTemplateMap;
    }

    public void setKeyTemplateMap(Map<String, String> keyTemplateMap) {
        this.keyTemplateMap = keyTemplateMap;
    }

    Map<String, String> throttleDataMap = new ConcurrentHashMap();

    /**
     * This method will check given key in throttle data Map. Throttle data map need to be update from topic
     * subscriber with all latest updates from global policy engine. This method will perfoem only local map
     * lookup and return results.
     *
     * @param key String unique key of throttle event.
     * @return Return true if event throttled(means key is available in throttle data map).
     * false if key is not there in throttle map(that means its not throttled).
     */
    public boolean isThrottled(String key) {
        return this.throttleDataMap.containsKey(key) || this.throttleDataMap.containsKey(key+"_default");
    }


    /**
     * This method will used to push un-throttled data events to global policy engine.
     *
     * @param throttleRequest is objects map which contains certain information retrieved from incoming message
     *                        need to add as much as data to this map and send events.
     */
    public void sendToGlobalThrottler(Object[] throttleRequest, DataPublisher dataPublisher) {
        org.wso2.carbon.databridge.commons.Event event = new org.wso2.carbon.databridge.commons.Event(streamID,
                System.currentTimeMillis(), null, null, throttleRequest);
        dataPublisher.tryPublish(event);
    }

    public boolean isRequestBlocked(String apiBlockingKey, String applicationBlockingKey, String userBlockingKey,
                                    String ipBlockingKey) {
        return (blockedAPIConditionsMap.containsKey(apiBlockingKey) ||
                blockedApplicationConditionsMap.containsKey(applicationBlockingKey) ||
                blockedUserConditionsMap.containsKey(userBlockingKey) ||
                blockedCustomConditionsMap.containsKey("test") ||
                blockedIpConditionsMap.containsKey(ipBlockingKey));
    }

    public Map<String, String> getBlockedIpConditionsMap() {
        return blockedIpConditionsMap;
    }

    public void setBlockedIpConditionsMap(Map<String, String> blockedIpConditionsMap) {
        this.blockedIpConditionsMap = blockedIpConditionsMap;
    }
}
