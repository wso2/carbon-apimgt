package org.wso2.carbon.apimgt.gateway.holders;

import ballerina.lang.maps;
import ballerina.lang.system;
import ballerina.lang.messages;
import ballerina.lang.strings;
import org.wso2.carbon.apimgt.gateway.constants;
import org.wso2.carbon.apimgt.gateway.dto;
import org.wso2.carbon.apimgt.ballerina.util as apimgtUtil;

map keyTemplateMap = {};
map throttleDataMap = {};

boolean isBlockingConditionsPresent = false;
boolean isKeyTemplatesPresent = false;

function addThrottleData (string key, string value) {
    throttleDataMap[key] = value;
}

function removeThrottleData (string key) {
    maps:remove(throttleDataMap, key);
}

function addKeyTemplate (string key, string value) {
    keyTemplateMap[key] = value;
    isKeyTemplatesPresent = true;
}


function removeKeyTemplate (string key) {
    maps:remove(keyTemplateMap, key);
    if (maps:length(keyTemplateMap) > 0) {
        isKeyTemplatesPresent = true;
    } else {
        isKeyTemplatesPresent = false;
    }
}

function getKeyTemplateMap () (map) {
    return keyTemplateMap;
}

function getThrottleNextAccessTimestamp (string key) (string) {
    return (string)throttleDataMap[key];
}

function isBlockingConditionsPresent () (boolean) {
    return isBlockingConditionsPresent;
}

function setBlockingConditionsPresent (boolean blockingConditionsPresent) {
    isBlockingConditionsPresent = blockingConditionsPresent;
}

function isKeyTemplatesPresent () (boolean) {
    return isKeyTemplatesPresent;
}

function setKeyTemplatesPresent (boolean keyTemplatesPresent) {
    isKeyTemplatesPresent = keyTemplatesPresent;
}

function isRequestBlocked (string apiBlockingKey, string applicationBlockingKey, string userBlockingKey, string ipBlockingKey) (boolean) {
    map blockConditions = getBlockConditionMap();
    return (blockConditions[apiBlockingKey] != null ||
            blockConditions[applicationBlockingKey] != null ||
            blockConditions[userBlockingKey] != null ||
            blockConditions[ipBlockingKey] != null || isIpRangeBlocked(ipBlockingKey, blockConditions));
}
function isIpRangeBlocked (string ipBlockingKey, map conditionsMap) (boolean) {
    boolean status = false;
    if (ipBlockingKey != "") {
        int longValueOfIp = apimgtUtil:convertIpToLong(ipBlockingKey);
        string[] conditionKeys = maps:keys(conditionsMap);
        int length = maps:length(conditionsMap);
        int i = 0;
        while (i > length) {
            string key = conditionKeys[i];
            if (strings:contains(key, constants:BLOCKING_CONDITION_IP_RANGE)) {
                dto:BlockConditionDto condition = (dto:BlockConditionDto)conditionsMap[key];
                if ((condition.startingIP <= longValueOfIp) && (condition.endingIP <= longValueOfIp)) {
                    status =  true;
                    break;
                }
            }
        }
    }
    return status;
}

function isThrottled (string throttleKey, message msg) (boolean) {

    if (throttleDataMap[throttleKey] != null) {

        int currentTime = system:currentTimeMillis();
        string expiryTime = (string)throttleDataMap[throttleKey];
        int expiryStamp = (int)expiryTime;
        messages:setProperty(msg, "THROTTLE_EXPIRE_TIME", expiryTime);
        if (expiryStamp >= currentTime) {
            return true;
        } else {
            maps:remove(throttleDataMap, throttleKey);
            return false;
        }
    }

    return false;
}
