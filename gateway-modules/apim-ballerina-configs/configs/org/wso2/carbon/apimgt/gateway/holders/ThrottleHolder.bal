package org.wso2.carbon.apimgt.gateway.holders;

import ballerina.lang.maps;
import ballerina.lang.system;
import ballerina.lang.strings;
import org.wso2.carbon.apimgt.gateway.constants;
import org.wso2.carbon.apimgt.gateway.dto;
import org.wso2.carbon.apimgt.ballerina.util as apimgtUtil;
import org.wso2.carbon.apimgt.ballerina.maps as mapHolder;
import ballerina.lang.errors;

map keyTemplateMap = {};
map throttleDataMap = {};

boolean isBlockingConditionsPresent = false;
boolean isKeyTemplatesPresent = false;

function addThrottleData (string key, string value) {
    system:println("addThrottleData() in ThrottleHolder");
    throttleDataMap = getMapFromHolder("throttleDataMap");
    throttleDataMap[key] = value;
    mapHolder:putMapEntry("throttleDataMap", throttleDataMap);
}

function removeThrottleData (string key) {
    system:println("removeThrottleData() in ThrottleHolder");
    throttleDataMap = getMapFromHolder("throttleDataMap");
    maps:remove(throttleDataMap, key);
    mapHolder:putMapEntry("throttleDataMap", throttleDataMap);
}

function addKeyTemplate (string key, string value) {
    system:println("addKeyTemplate() in ThrottleHolder");
    keyTemplateMap = getMapFromHolder("keyTemplateMap");
    keyTemplateMap[key] = value;
    mapHolder:putMapEntry("keyTemplateMap", keyTemplateMap);
    isKeyTemplatesPresent = true;
}

function removeKeyTemplate (string key) {
    system:println("removeKeyTemplate() in ThrottleHolder");
    keyTemplateMap = getMapFromHolder("keyTemplateMap");
    maps:remove(keyTemplateMap, key);
    mapHolder:putMapEntry("keyTemplateMap", keyTemplateMap);
    if (maps:length(keyTemplateMap) > 0) {
        isKeyTemplatesPresent = true;
    } else {
        isKeyTemplatesPresent = false;
    }
}

function getKeyTemplateMap () (map) {
    system:println("getKeyTemplateMap() in ThrottleHolder");
    keyTemplateMap = getMapFromHolder("keyTemplateMap");
    return keyTemplateMap;
}

function getThrottleNextAccessTimestamp (string key) (string) {
    system:println("getThrottleNextAccessTimestamp() in ThrottleHolder");
    errors:TypeCastError err;
    string value;
    throttleDataMap = getMapFromHolder("throttleDataMap");
    value, err = (string)throttleDataMap[key];
    return value;
}

function isBlockingConditionsPresent () (boolean) {
    system:println("isBlockingConditionsPresent() in ThrottleHolder");
    return isBlockingConditionsPresent;
}

function setBlockingConditionsPresent (boolean blockingConditionsPresent) {
    system:println("setBlockingConditionsPresent() in ThrottleHolder");
    isBlockingConditionsPresent = blockingConditionsPresent;
}

function isKeyTemplatesPresent () (boolean) {
    system:println("isKeyTemplatesPresent() in ThrottleHolder");
    return isKeyTemplatesPresent;
}

function setKeyTemplatesPresent (boolean keyTemplatesPresent) {
    system:println("setKeyTemplatesPresent() in ThrottleHolder");
    isKeyTemplatesPresent = keyTemplatesPresent;
}

function isRequestBlocked (string apiBlockingKey, string applicationBlockingKey, string userBlockingKey, string ipBlockingKey) (boolean) {
    system:println("isRequestBlocked() in ThrottleHolder");
    map blockConditions = getBlockConditionMap();
    return (blockConditions[apiBlockingKey] != null ||
            blockConditions[applicationBlockingKey] != null ||
            blockConditions[userBlockingKey] != null ||
            blockConditions[ipBlockingKey] != null || isIpRangeBlocked(ipBlockingKey, blockConditions));
}
function isIpRangeBlocked (string ipBlockingKey, map conditionsMap) (boolean) {
    system:println("isIpRangeBlocked() in ThrottleHolder");
    boolean status = false;
    if (ipBlockingKey != "") {
        int longValueOfIp = apimgtUtil:convertIpToLong(ipBlockingKey);
        string[] conditionKeys = maps:keys(conditionsMap);
        int length = maps:length(conditionsMap);
        int i = 0;
        while (i > length) {
            string key = conditionKeys[i];
            if (strings:contains(key, constants:BLOCKING_CONDITION_IP_RANGE)) {
                dto:BlockConditionDto condition;
                errors:TypeCastError err;
                condition, err = (dto:BlockConditionDto)conditionsMap[key];

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
    system:println("isThrottled() in ThrottleHolder");
    throttleDataMap = getMapFromHolder("throttleDataMap");
    if (throttleDataMap[throttleKey] != null) {

        //int currentTime = system:currentTimeMillis();
        //errors:TypeCastError err;
        //string expiryTime;
        //int expiryStamp;
        //expiryTime, err = (string)throttleDataMap[throttleKey];
        //expiryStamp = 123456798;//(int)expiryTime;
        //messages:setProperty(msg, "THROTTLE_EXPIRE_TIME", expiryTime);
        //if (expiryStamp >= currentTime) {
        //    return true;
        //} else {
        //    maps:remove(throttleDataMap, throttleKey);
        //    return false;
        //}
        return true;
    }

    return false;
}

function getMapFromHolder(string mapName)(map){
    system:println("getMapfromHolder() in ThrottleHolder");
    any object = mapHolder:getMapEntry(mapName);
    var retrivedMap, castErr = (map)object;
    if(castErr != null) {
        system:println("Error while casting throttle maps: " + castErr.msg);
        retrivedMap = {};
    }
    return retrivedMap;
}

function addThrottleMaps()(boolean){
    system:println("addThrottleMaps() in ThrottleHolder");
    map throttleMap = {};
    map keyTemplateMap = {};
    mapHolder:putMapEntry("throttleDataMap", throttleMap);
    mapHolder:putMapEntry("keyTemplateMap", keyTemplateMap);
    return true;
}