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
    throttleDataMap = getMapFromHolder("throttleDataMap");
    throttleDataMap[key] = value;
    mapHolder:putMapEntry("throttleDataMap", throttleDataMap);
}

function removeThrottleData (string key) {
    throttleDataMap = getMapFromHolder("throttleDataMap");
    maps:remove(throttleDataMap, key);
    mapHolder:putMapEntry("throttleDataMap", throttleDataMap);
}

function addKeyTemplate (string key, string value) {
    keyTemplateMap = getMapFromHolder("keyTemplateMap");
    keyTemplateMap[key] = value;
    mapHolder:putMapEntry("keyTemplateMap", keyTemplateMap);
    isKeyTemplatesPresent = true;
}

function removeKeyTemplate (string key) {
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
    keyTemplateMap = getMapFromHolder("keyTemplateMap");
    return keyTemplateMap;
}

function getThrottleNextAccessTimestamp (string key) (string) {
    errors:TypeCastError err;
    string value;
    throttleDataMap = getMapFromHolder("throttleDataMap");
    value, err = (string)throttleDataMap[key];
    return value;
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
    any object = mapHolder:getMapEntry(mapName);
    var retrivedMap, castErr = (map)object;
    if(castErr != null) {
        system:println("Error while casting throttle maps: " + castErr.msg);
        retrivedMap = {};
    }
    return retrivedMap;
}

function addThrottleMaps()(boolean){
    map throttleMap = {};
    map keyTemplateMap = {};
    mapHolder:putMapEntry("throttleDataMap", throttleMap);
    mapHolder:putMapEntry("keyTemplateMap", keyTemplateMap);
    return true;
}