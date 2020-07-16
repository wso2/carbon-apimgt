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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.gateway.dto.IPRange;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.ConditionDto;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class will hold throttle data per given node. All throttle handler objects should refer values from this.
 * When throttle data holder initialize it should read complete throttle decision table from global policy engine
 * via web service calls. In addition to that it should subscribe to topic and listen throttle updates.
 */

public class ThrottleDataHolder {

    private static final Log log = LogFactory.getLog(ThrottleDataHolder.class);
    private Map<String, String> blockedAPIConditionsMap = new ConcurrentHashMap<String, String>();
    private Map<String, String> blockedApplicationConditionsMap = new ConcurrentHashMap<String, String>();
    private Map<String, String> blockedUserConditionsMap = new ConcurrentHashMap<String, String>();
    private Map<String, Set<IPRange>> blockedIpConditionsMap = new ConcurrentHashMap<>();
    private Map<String, String> keyTemplateMap = new ConcurrentHashMap<String, String>();
    private boolean isBlockingConditionsPresent = true;
    private boolean isKeyTemplatesPresent = false;
    private Map<String, Long> throttleDataMap = new ConcurrentHashMap<String, Long>();
    private Map<String,Long> throttledAPIKeysMap = new ConcurrentHashMap<String, Long>();
    private Map<String, Map<String, List<ConditionDto>>> conditionDtoMap = new ConcurrentHashMap<>();
    public void addThrottleData(String key, Long value) {
        throttleDataMap.put(key, value);
    }
    private Map<String, String> blockedSubscriptionConditionsMap = new ConcurrentHashMap<String, String>();

    public void addThrottleDataFromMap(Map<String, Long> data) {
        throttleDataMap.putAll(data);
    }

    public void addThrottledAPIKey(String key, Long value){
        throttledAPIKeysMap.put(key,value);
    }

    public void addThrottledApiConditions(String key, String conditionKey, List<ConditionDto> conditionValue) {

        Map<String, List<ConditionDto>> conditionMap;
        if (conditionDtoMap.containsKey(key)) {
            conditionMap = conditionDtoMap.get(key);
        } else {
            conditionMap = new ConcurrentHashMap<>();
            conditionDtoMap.put(key, conditionMap);
        }
        if (!conditionMap.containsKey(conditionKey)) {
            conditionMap.put(conditionKey, conditionValue);
        }
    }

    public void removeThrottledApiConditions(String key, String conditionKey) {
        if (conditionDtoMap.containsKey(key)) {
            Map<String, List<ConditionDto>> conditionMap = conditionDtoMap.get(key);
            conditionMap.remove(conditionKey);
            if (conditionMap.isEmpty()) {
                conditionDtoMap.remove(key);
            }
        }
    }

    public void addSubscriptionBlockingCondition(String name, String value) {
        blockedSubscriptionConditionsMap.put(name, value);
    }

    public void addSubscriptionBlockingConditionsFromMap(Map<String, String> data) {
        if (data.size() > 0) {
            blockedSubscriptionConditionsMap.putAll(data);
        }
    }

    public void removeSubscriptionBlockingCondition(String name) {
        blockedSubscriptionConditionsMap.remove(name);
    }

    public void removeThrottledAPIKey(String key){
        throttledAPIKeysMap.remove(key);
    }

    public boolean isAPIThrottled(String apiKey){
        boolean isThrottled = this.throttledAPIKeysMap.containsKey(apiKey);
        if(isThrottled) {
            long currentTime = System.currentTimeMillis();
            long timestamp = this.throttledAPIKeysMap.get(apiKey);
            if(timestamp >= currentTime) {
                return isThrottled;
            } else {
                this.throttledAPIKeysMap.remove(apiKey);
                this.conditionDtoMap.remove(apiKey);
                return false;
            }
        } else {
            return isThrottled;
        }
    }

    public boolean isConditionsAvailable(String key) {
        return conditionDtoMap.containsKey(key);
    }

    public Map<String,List<ConditionDto>> getConditionDtoMap(String key){
        return conditionDtoMap.get(key);
    }

    public void removeThrottleData(String key) {
        throttleDataMap.remove(key);
    }

    public void addAPIBlockingCondition(String name, String value) {
        blockedAPIConditionsMap.put(name, value);
    }

    public void addApplicationBlockingCondition(String name, String value) {
        blockedApplicationConditionsMap.put(name, value);
    }


    public void addUserBlockingCondition(String name, String value) {
        blockedUserConditionsMap.put(name, value);
    }

    public void addIpBlockingCondition(String tenantDomain, int conditionId, String value, String type) {

        Set<IPRange> ipRanges = blockedIpConditionsMap.get(tenantDomain);
        if (ipRanges == null){
            ipRanges = new HashSet<>();
        }

        ipRanges.add(convertValueToIPRange(tenantDomain, conditionId, value, type));
        blockedIpConditionsMap.put(tenantDomain, ipRanges);
    }

    private IPRange convertValueToIPRange(String tenantDomain, int conditionId, String value, String type) {

        IPRange ipRange = new IPRange();
        ipRange.setId(conditionId);
        ipRange.setTenantDomain(tenantDomain);
        JsonObject ipLevelJson = (JsonObject) new JsonParser().parse(value);
        if (APIConstants.BLOCKING_CONDITIONS_IP.equals(type)) {
            ipRange.setType(APIConstants.BLOCKING_CONDITIONS_IP);
            JsonElement fixedIpElement = ipLevelJson.get(APIConstants.BLOCK_CONDITION_FIXED_IP);
            if (fixedIpElement != null && StringUtils.isNotEmpty(fixedIpElement.getAsString())) {
                ipRange.setFixedIp(fixedIpElement.getAsString());
            }
        } else if (APIConstants.BLOCK_CONDITION_IP_RANGE.equals(type)) {
            ipRange.setType(APIConstants.BLOCK_CONDITION_IP_RANGE);
            JsonElement startingIpElement = ipLevelJson.get(APIConstants.BLOCK_CONDITION_START_IP);
            if (startingIpElement != null && StringUtils.isNotEmpty(startingIpElement.getAsString())) {
                ipRange.setStartingIP(startingIpElement.getAsString());
                ipRange.setStartingIpBigIntValue(APIUtil.ipToBigInteger(startingIpElement.getAsString()));
            }
            JsonElement endingIpElement = ipLevelJson.get(APIConstants.BLOCK_CONDITION_ENDING_IP);
            if (endingIpElement != null && StringUtils.isNotEmpty(endingIpElement.getAsString())) {
                ipRange.setEndingIp(endingIpElement.getAsString());
                ipRange.setEndingIpBigIntValue(APIUtil.ipToBigInteger(endingIpElement.getAsString()));
            }
        }
        if (ipLevelJson.has(APIConstants.BLOCK_CONDITION_INVERT)) {
            ipRange.setInvert(ipLevelJson.get(APIConstants.BLOCK_CONDITION_INVERT).getAsBoolean());
        }
        return ipRange;
    }
    public void addUserBlockingConditionsFromMap(Map<String, String> data) {
        if(data.size() > 0) {
            blockedUserConditionsMap.putAll(data);
        }
    }

    public void addIplockingConditionsFromMap(Map<String, Set<IPRange>> data) {
        if(data.size() > 0) {
            blockedIpConditionsMap.putAll(data);
        }
    }

    public void addAPIBlockingConditionsFromMap(Map<String, String> data) {
        if(data.size() > 0) {
            blockedAPIConditionsMap.putAll(data);
        }
    }

    public void addApplicationBlockingConditionsFromMap(Map<String, String> data) {
        if(data.size() > 0) {
            blockedApplicationConditionsMap.putAll(data);
        }
    }

    public void removeAPIBlockingCondition(String name) {
        blockedAPIConditionsMap.remove(name);
    }

    public void removeApplicationBlockingCondition(String name) {
        blockedApplicationConditionsMap.remove(name);
    }


    public void removeUserBlockingCondition(String name) {
        blockedUserConditionsMap.remove(name);
    }

    public void removeIpBlockingCondition(String tenantDomain, int conditionId) {

        Set<IPRange> ipRanges = blockedIpConditionsMap.get(tenantDomain);
        if (ipRanges != null) {
            Iterator<IPRange> iterator = ipRanges.iterator();
            while (iterator.hasNext()) {
                IPRange ipRange = iterator.next();
                if (ipRange.getId() == conditionId) {
                    iterator.remove();
                    break;
                }
            }
        }
    }

    public void addKeyTemplate(String key, String value) {
        keyTemplateMap.put(key, value);
        isKeyTemplatesPresent = true;
    }

    public void addKeyTemplateFromMap(Map<String, String> data) {
        if(data.size() > 0) {
            keyTemplateMap.putAll(data);
            isKeyTemplatesPresent = true;
        }
    }

    public void removeKeyTemplate(String name) {
        keyTemplateMap.remove(name);
        if(keyTemplateMap.size() > 0) {
            isKeyTemplatesPresent = true;
        } else {
            isKeyTemplatesPresent = false;
        }
    }

    public Map<String, String> getKeyTemplateMap() {
        return keyTemplateMap;
    }

    public boolean isRequestBlocked(String apiBlockingKey, String applicationBlockingKey, String userBlockingKey,
                                    String ipBlockingKey, String apiTenantDomain, String subscriptionBlockingKey) {
        return (blockedAPIConditionsMap.containsKey(apiBlockingKey) ||
                blockedApplicationConditionsMap.containsKey(applicationBlockingKey) ||
                blockedUserConditionsMap.containsKey(userBlockingKey) ||
                blockedSubscriptionConditionsMap.containsKey(subscriptionBlockingKey) ||
                isIpLevelBlocked(apiTenantDomain, ipBlockingKey));
    }

    private boolean isIpLevelBlocked(String apiTenantDomain, String ip) {

        Set<IPRange> ipRanges = blockedIpConditionsMap.get(apiTenantDomain);
        if (ipRanges != null && ipRanges.size() > 0) {
            log.debug("Tenant " + apiTenantDomain + " contains block conditions");
            for (IPRange ipRange : ipRanges) {
                if (APIConstants.BLOCKING_CONDITIONS_IP.equals(ipRange.getType())) {
                    if (ip.equals(ipRange.getFixedIp())) {
                        if (!ipRange.isInvert()) {
                            log.debug("Block IP selected for Blocked");
                            return true;
                        }
                    } else {
                        if (ipRange.isInvert()) {
                            log.debug("Block IP selected for Blocked");
                            return true;
                        }
                    }
                } else if (APIConstants.BLOCK_CONDITION_IP_RANGE.equals(ipRange.getType())) {
                    BigInteger ipBigIntegerValue = APIUtil.ipToBigInteger(ip);

                    if (((ipBigIntegerValue.compareTo(ipRange.getStartingIpBigIntValue()) > 0) &&
                            (ipBigIntegerValue.compareTo(ipRange.getEndingIpBigIntValue()) < 0))) {
                        if (!ipRange.isInvert()) {
                            log.debug("Block IPRange selected for Blocked");
                            return true;
                        }
                    } else {
                        if (ipRange.isInvert()) {
                            log.debug("Block IPRange selected for Blocked");
                            return true;
                        }
                    }
                }
            }
        }
        return false;
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
        if(isThrottled) {
            long currentTime = System.currentTimeMillis();
            long timestamp = this.throttleDataMap.get(key);
            if(timestamp >= currentTime) {
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

    public boolean isKeyTemplatesPresent() {
        return isKeyTemplatesPresent;
    }

    public void setKeyTemplatesPresent(boolean keyTemplatesPresent) {
        isKeyTemplatesPresent = keyTemplatesPresent;
    }
}
