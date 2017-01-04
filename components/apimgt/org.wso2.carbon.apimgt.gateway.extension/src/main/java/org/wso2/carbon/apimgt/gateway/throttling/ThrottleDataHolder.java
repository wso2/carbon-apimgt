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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class will hold throttle data per given node. All throttle handler objects should refer values from this.
 * When throttle data holder initialize it should read complete throttle decision table from global policy engine
 * via web service calls. In addition to that it should subscribe to topic and listen throttle updates.
 */
public class ThrottleDataHolder {

    private static final Logger log = LoggerFactory.getLogger(ThrottleDataHolder.class);
    private Map<String, String> blockedAPIConditionsMap = new ConcurrentHashMap<String, String>();
    private Map<String, String> blockedApplicationConditionsMap = new ConcurrentHashMap<String, String>();
    private Map<String, String> blockedUserConditionsMap = new ConcurrentHashMap<String, String>();
    private Map<String, String> blockedIpConditionsMap = new ConcurrentHashMap<String, String>();
    private Map<String, String> keyTemplateMap = new ConcurrentHashMap<String, String>();
    private Map<String, Long> throttleDataMap = new ConcurrentHashMap<String, Long>();
    private Map<String, Long> throttledAPIKeysMap = new ConcurrentHashMap<String, Long>();
    private boolean isBlockingConditionsPresent = false;
    private boolean isKeyTemplatesPresent = false;

    private static final ThrottleDataHolder instance = new ThrottleDataHolder();

    public static ThrottleDataHolder getInstance() {
        return instance;
    }

    public void addThrottleData(String key, Long value) {
        throttleDataMap.put(key, value);
    }

    public void addThrottleDataFromMap(Map<String, Long> data) {
        throttleDataMap.putAll(data);
    }

    public void addThrottledAPIKey(String key, Long value) {
        throttledAPIKeysMap.put(key, value);
    }

    public void removeThrottledAPIKey(String key) {
        throttledAPIKeysMap.remove(key);
    }

    public boolean isAPIThrottled(String apiKey) {
        boolean isThrottled = this.throttledAPIKeysMap.containsKey(apiKey);
        if (isThrottled) {
            long currentTime = System.currentTimeMillis();
            long timestamp = this.throttledAPIKeysMap.get(apiKey);
            if (timestamp >= currentTime) {
                return isThrottled;
            } else {
                this.throttledAPIKeysMap.remove(apiKey);
                return false;
            }
        } else {
            return isThrottled;
        }
    }

    public void removeThrottleData(String key) {
        throttleDataMap.remove(key);
    }

    public void addAPIBlockingCondition(String name, String value) {
        isBlockingConditionsPresent = true;
        blockedAPIConditionsMap.put(name, value);
    }

    public void addApplicationBlockingCondition(String name, String value) {
        isBlockingConditionsPresent = true;
        blockedApplicationConditionsMap.put(name, value);
    }


    public void addUserBlockingCondition(String name, String value) {
        isBlockingConditionsPresent = true;
        blockedUserConditionsMap.put(name, value);
    }

    public void addIplockingCondition(String name, String value) {
        isBlockingConditionsPresent = true;
        blockedIpConditionsMap.put(name, value);
    }

    public void addUserBlockingConditionsFromMap(Map<String, String> data) {
        if (data.size() > 0) {
            blockedUserConditionsMap.putAll(data);
            isBlockingConditionsPresent = true;
        }
    }

    public void addIplockingConditionsFromMap(Map<String, String> data) {
        if (data.size() > 0) {
            blockedIpConditionsMap.putAll(data);
            isBlockingConditionsPresent = true;
        }
    }

    public void addAPIBlockingConditionsFromMap(Map<String, String> data) {
        if (data.size() > 0) {
            blockedAPIConditionsMap.putAll(data);
            isBlockingConditionsPresent = true;
        }
    }

    public void addApplicationBlockingConditionsFromMap(Map<String, String> data) {
        if (data.size() > 0) {
            blockedApplicationConditionsMap.putAll(data);
            isBlockingConditionsPresent = true;
        }
    }

    public void removeAPIBlockingCondition(String name) {
        blockedAPIConditionsMap.remove(name);
        if (isAnyBlockedMapContainsData()) {
            isBlockingConditionsPresent = true;
        } else {
            isBlockingConditionsPresent = false;
        }
    }

    public void removeApplicationBlockingCondition(String name) {
        blockedApplicationConditionsMap.remove(name);
        if (isAnyBlockedMapContainsData()) {
            isBlockingConditionsPresent = true;
        } else {
            isBlockingConditionsPresent = false;
        }
    }


    public void removeUserBlockingCondition(String name) {
        blockedUserConditionsMap.remove(name);
        if (isAnyBlockedMapContainsData()) {
            isBlockingConditionsPresent = true;
        } else {
            isBlockingConditionsPresent = false;
        }
    }

    public void removeIpBlockingCondition(String name) {
        blockedIpConditionsMap.remove(name);
        if (isAnyBlockedMapContainsData()) {
            isBlockingConditionsPresent = true;
        } else {
            isBlockingConditionsPresent = false;
        }
    }

    public void addKeyTemplate(String key, String value) {
        keyTemplateMap.put(key, value);
        isKeyTemplatesPresent = true;
    }

    public void addKeyTemplateFromMap(Map<String, String> data) {
        if (data.size() > 0) {
            keyTemplateMap.putAll(data);
            isKeyTemplatesPresent = true;
        }
    }

    public void removeKeyTemplate(String name) {
        keyTemplateMap.remove(name);
        if (keyTemplateMap.size() > 0) {
            isKeyTemplatesPresent = true;
        } else {
            isKeyTemplatesPresent = false;
        }
    }

    public Map<String, String> getKeyTemplateMap() {
        return keyTemplateMap;
    }

    public boolean isRequestBlocked(String apiBlockingKey, String applicationBlockingKey, String userBlockingKey,
                                    String ipBlockingKey) {
        return (blockedAPIConditionsMap.containsKey(apiBlockingKey) ||
                blockedApplicationConditionsMap.containsKey(applicationBlockingKey) ||
                blockedUserConditionsMap.containsKey(userBlockingKey) ||
                blockedIpConditionsMap.containsKey(ipBlockingKey));
    }

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
        boolean isThrottled = this.throttleDataMap.containsKey(key);
        if (isThrottled) {
            long currentTime = System.currentTimeMillis();
            long timestamp = this.throttleDataMap.get(key);
            if (timestamp >= currentTime) {
                return isThrottled;
            } else {
                this.throttleDataMap.remove(key);
                return false;
            }
        } else {
            return isThrottled;
        }
    }

    /**
     * This method used to get the next access timestamp of a given key
     *
     * @param key String unique key of throttle event.
     * @return throttle next access timestamp
     */
    public long getThrottleNextAccessTimestamp(String key) {
        return this.throttleDataMap.get(key);
    }

    public boolean isBlockingConditionsPresent() {
        return isBlockingConditionsPresent;
    }

    public void setBlockingConditionsPresent(boolean blockingConditionsPresent) {
        isBlockingConditionsPresent = blockingConditionsPresent;
    }

    private boolean isAnyBlockedMapContainsData() {
        if (blockedAPIConditionsMap.size() > 0 || blockedIpConditionsMap.size() > 0
                || blockedApplicationConditionsMap.size() > 0 || blockedUserConditionsMap.size() > 0) {
            return true;
        }
        return false;
    }

    public boolean isKeyTemplatesPresent() {
        return isKeyTemplatesPresent;
    }

    public void setKeyTemplatesPresent(boolean keyTemplatesPresent) {
        isKeyTemplatesPresent = keyTemplatesPresent;
    }

    public void addDummyThrottlingData() {
        addUserBlockingCondition("admin", "true");
    }
}
