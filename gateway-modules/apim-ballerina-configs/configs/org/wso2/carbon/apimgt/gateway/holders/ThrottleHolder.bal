package org.wso2.carbon.apimgt.gateway.holders;

import ballerina.lang.maps;

map blockedAPIConditionsMap = {};
map blockedApplicationConditionsMap = {};
map blockedUserConditionsMap = {};
map blockedIpConditionsMap = {};
map keyTemplateMap = {};
map throttleDataMap = {};
map throttledAPIKeysMap = {};

boolean isBlockingConditionsPresent = false;
boolean isKeyTemplatesPresent = false;

function addThrottleData(string key,string value) {
    throttleDataMap[key] = value;
}

function addThrottledAPIKey(string key, string value){
    throttledAPIKeysMap[key] = value;
}

function removeThrottledAPIKey(string key){
    maps:remove(throttledAPIKeysMap,key);
}

function removeThrottleData(string key) {
    maps:remove(throttleDataMap,key);
}

function addAPIBlockingCondition(string key, string value) {
    isBlockingConditionsPresent = true;
    blockedAPIConditionsMap[key] = value;
}

function addApplicationBlockingCondition(string key, string value) {
    isBlockingConditionsPresent = true;
    blockedApplicationConditionsMap[key] = value;
}

function addUserBlockingCondition(string key, string value) {
    isBlockingConditionsPresent = true;
    blockedUserConditionsMap[key] = value;
}

function addIpBlockingCondition(string key, string value) {
    isBlockingConditionsPresent = true;
    blockedIpConditionsMap[key] = value;
}

function removeAPIBlockingCondition(string key) {
    maps:remove(blockedAPIConditionsMap,key);
    if(isAnyBlockedMapContainsData()) {
        isBlockingConditionsPresent = true;
    } else {
        isBlockingConditionsPresent = false;
    }
}

function removeApplicationBlockingCondition(string key) {
    maps:remove(blockedApplicationConditionsMap,key);
    if(isAnyBlockedMapContainsData()) {
        isBlockingConditionsPresent = true;
    } else {
        isBlockingConditionsPresent = false;
    }
}


function removeUserBlockingCondition(string key) {
    maps:remove(blockedUserConditionsMap,key);
    if(isAnyBlockedMapContainsData()) {
        isBlockingConditionsPresent = true;
    } else {
        isBlockingConditionsPresent = false;
    }
}

function removeIpBlockingCondition(string key) {
    maps:remove(blockedIpConditionsMap,key);
    if(isAnyBlockedMapContainsData()) {
        isBlockingConditionsPresent = true;
    } else {
        isBlockingConditionsPresent = false;
    }
}

function addKeyTemplate(string key, string value) {
    keyTemplateMap[key] = value;
    isKeyTemplatesPresent = true;
}


function removeKeyTemplate(string key) {
    maps:remove(keyTemplateMap,key);
    if(maps:length(keyTemplateMap)> 0) {
        isKeyTemplatesPresent = true;
    } else {
        isKeyTemplatesPresent = false;
    }
}

function getKeyTemplateMap()(map) {
    return keyTemplateMap;
}
    
function getThrottleNextAccessTimestamp(string key)(string) {
    return (string)throttleDataMap[key];
}

function isBlockingConditionsPresent()(boolean) {
    return isBlockingConditionsPresent;
}

function setBlockingConditionsPresent(boolean blockingConditionsPresent) {
    isBlockingConditionsPresent = blockingConditionsPresent;
}

function isKeyTemplatesPresent()(boolean) {
    return isKeyTemplatesPresent;
}

function setKeyTemplatesPresent(boolean keyTemplatesPresent) {
    isKeyTemplatesPresent = keyTemplatesPresent;
}

function isAnyBlockedMapContainsData()(boolean) {
    if ( maps:length(blockedAPIConditionsMap) > 0 || maps:length(blockedIpConditionsMap) > 0 || 
         maps:length(blockedApplicationConditionsMap) > 0 || maps:length(blockedUserConditionsMap) > 0) {
        return true;
    }
    return false;
}

function isRequestBlocked(string apiBlockingKey, string applicationBlockingKey, string userBlockingKey, string ipBlockingKey)(boolean){

    string apiCondition = (string)blockedAPIConditionsMap[apiBlockingKey];
    string appCondition = (string)blockedApplicationConditionsMap[applicationBlockingKey];
    string userCondition = (string)blockedUserConditionsMap[userBlockingKey];
    string ipCondition = (string)blockedIpConditionsMap[ipBlockingKey];

    return ( apiCondition != "" || appCondition != "" || userCondition != "" || ipCondition != "");
}


function isAPIThrottled(string apiKey)(boolean){
    // used for condition implementation 
    string value = (string)throttledAPIKeysMap[apiKey];

    if (value != ""){
        return true;
    }
    // todo Check for throttle time
    return false;
}

function isThrottled(string apiKey)(boolean){

    string value = (string)throttleDataMap[apiKey];
    if (value != ""){
        return true;
    }
    // todo Check for throttle time
    return false;
}
