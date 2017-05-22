package org.wso2.carbon.apimgt.gateway.holders;
import org.wso2.carbon.apimgt.gateway.dto as dto;
import ballerina.lang.maps;

map tokenCacheMap = {};
map subscriptionCache = {};
map resourceCacheMap = {};
map jwtTokenCache = {};
map keyValidationInfoCache = {};
map apiCache = {};
dto:LabelInfoDto labelInfo= {};
dto:GatewayConf gatewayConf = {};

function getFromTokenCache(string key)(dto:IntrospectDto){
    return (dto:IntrospectDto)tokenCacheMap[key];
}
function putIntoTokenCache(string key,dto:IntrospectDto introspectDto){
    tokenCacheMap[key] = introspectDto;
}
function getFromSubscriptionCache(string key)(dto:SubscriptionDto){
    return (dto:SubscriptionDto)subscriptionCache[key];
}
function putIntoSubscriptionCache(dto:SubscriptionDto subscriptionDto){
    string key = subscriptionDto.apiContext+":"+subscriptionDto.apiVersion+":"+subscriptionDto.consumerKey;
    subscriptionCache[key] = subscriptionDto;
}
function getFromResourceCache(string key)(dto:ResourceDto){
    return (dto:ResourceDto)resourceCacheMap[key];
}
function putIntoResourceCache(string apiContext,string apiVersion,dto:ResourceDto resourceDto){
    string key = apiContext+":"+apiVersion+":"+resourceDto.uriTemplate+":"+resourceDto.httpVerb;
    resourceCacheMap[key] = resourceDto;
}
function putIntoAPICache (dto:APIDto apiDto) {
    string key = apiDto.context + ":" + apiDto.version;
    apiCache[key] = apiDto;
}
function removeFromAPICache (dto:APIDto apiDto) {
    string key = apiDto.context + ":" + apiDto.version;
    maps:remove(apiCache,key);
}
function getFromAPICache(string key)(dto:APIDto){
    return (dto:APIDto)apiCache[key];
}
function setGatewayConf (dto:GatewayConf conf) {
    gatewayConf = conf;
}
function getGatewayConf()(dto:GatewayConf){
    return gatewayConf;
}

function getLabelInfo()(dto:LabelInfoDto){
    return labelInfo;
}

function setLabelInfo(dto:LabelInfoDto labelInfoDto){
    labelInfo = labelInfoDto;
}