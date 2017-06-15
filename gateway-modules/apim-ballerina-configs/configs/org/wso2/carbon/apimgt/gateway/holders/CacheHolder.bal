package org.wso2.carbon.apimgt.gateway.holders;
import org.wso2.carbon.apimgt.gateway.dto as dto;
import org.wso2.carbon.apimgt.gateway.constants;
import org.wso2.carbon.apimgt.ballerina.caching;
map apiCache = {};
dto:GatewayConfDTO gatewayConf = {};
dto:KeyManagerInfoDTO keyManagerConf = {};
map applicationCache = {};
map userInfoCache = {};
string apiCoreUrl;
function getFromTokenCache (string key) (dto:IntrospectDto){
    any introspect = caching:getCacheEntry(constants:TOKEN_CACHE,key);
    if(introspect != null){
        return (dto:IntrospectDto)introspect;
    }else{
        return null;
    }
}
function putIntoTokenCache (string key, dto:IntrospectDto introspectDto) {
    caching:putCacheEntry(constants:TOKEN_CACHE,key,introspectDto);
}
function getFromSubscriptionCache (string apiContext, string version, string consumerKey) (dto:SubscriptionDto){
    string key = apiContext + ":" + version + ":" + consumerKey;
    any subscription = caching:getCacheEntry(constants:SUBSCRIPTION_CACHE,key);
    if(subscription != null){
        return (dto:SubscriptionDto)subscription;
    }else{
        return null;
    }
}
function putIntoSubscriptionCache (dto:SubscriptionDto subscriptionDto) {
    string key = "/" + subscriptionDto.apiContext + ":" + subscriptionDto.apiVersion + ":" + subscriptionDto.consumerKey;
    caching:putCacheEntry(constants:SUBSCRIPTION_CACHE,key,subscriptionDto);
}
function getFromResourceCache (string apiContext, string apiVersion, string resourceUri, string httpVerb) (dto:ResourceDto){
    string internalKey = resourceUri + ":" + httpVerb;
    string key = apiContext + ":" + apiVersion;
    any resourceMapEntry = caching:getCacheEntry(constants:RESOURCE_CACHE,key);
    if (resourceMapEntry != null) {
        map resourceMap = (map)resourceMapEntry;
        if (resourceMap[internalKey] != null) {
            return (dto:ResourceDto)resourceMap[internalKey];
        } else {
            return null;
        }
    } else {
        return null;
    }
}
function putIntoResourceCache (string apiContext, string apiVersion, dto:ResourceDto resourceDto) {
    string internalKey = resourceDto.uriTemplate + ":" + resourceDto.httpVerb;
    string key = "/" + apiContext + ":" + apiVersion;
    map resourceMap = {};
    any resourceMapEntry = caching:getCacheEntry(constants:RESOURCE_CACHE,key);
    if (resourceMapEntry != null) {
        resourceMap = (map)resourceMapEntry;
        caching:removeCacheEntry(constants:RESOURCE_CACHE,key);
    }
    resourceMap[internalKey] = resourceDto;
    caching:putCacheEntry(constants:RESOURCE_CACHE,key,resourceMap);
}
function removeFromTokenCache (string key) {
    caching:removeCacheEntry(constants:TOKEN_CACHE,key);
}
function putIntoAPICache (dto:APIDTO apidto) {
    string key = "/" + apidto.context + ":" + apidto.version;
    caching:putCacheEntry(constants:API_CACHE,key,apidto);
}
function removeFromAPICache (dto:APIDTO apidto) {
    string key = apidto.context + ":" + apidto.version;
    caching:removeCacheEntry(constants:API_CACHE,key);
}
function getFromAPICache (string key) (dto:APIDTO){
    any api = caching:getCacheEntry(constants:API_CACHE,key);
    if(api != null){
        return (dto:APIDTO)api;
    }else{
        return null;
    }
}
function setGatewayConf (dto:GatewayConfDTO conf) {
    gatewayConf = conf;
    keyManagerConf = conf.keyManagerInfo;
}
function getGatewayConf () (dto:GatewayConfDTO){
    return gatewayConf;
}
function  getKeyManagerConf() (dto:KeyManagerInfoDTO){
    return keyManagerConf;
}
function getAnalyticsConf () (dto:AnalyticsInfoDTO){
    return gatewayConf.analyticsInfo;
}

function putIntoApplicationCache (dto:ApplicationDto applicationDto) {
    caching:putCacheEntry("APPLICATION_CACHE",applicationDto.applicationId,applicationDto);
}
function getFromApplicationCache (string applicationId) (dto:ApplicationDto){
    any application = caching:getCacheEntry(constants:APPLICATION_CACHE,applicationId);
    if(application != null){
        return (dto:ApplicationDto)application;
    }else{
        return null;
    }
}
function removeApplicationFromCache (string applicationId) {
    caching:removeCacheEntry(constants:APPLICATION_CACHE,applicationId);
}
function removeFromResources (string apiContext, string apiVersion) {
    string key = apiContext + ":" + apiVersion;
    caching:removeCacheEntry(constants:RESOURCE_CACHE,key);
}
function removeFromSubscriptionCache (string apiContext, string apiVersion, string consumerKey) {
    string key = apiContext + ":" + apiVersion + ":" + consumerKey;
    caching:removeCacheEntry(constants:SUBSCRIPTION_CACHE,key);
}

function initializeCache()(boolean ){
    //cache for token introspect
    caching:createCache(constants:TOKEN_CACHE,"15");
    //cache for subscription
    caching:createCache(constants:SUBSCRIPTION_CACHE,"15");
    //cache for resource
    caching:createCache(constants:RESOURCE_CACHE,"15");
    //cache for application
    caching:createCache(constants:APPLICATION_CACHE,"15");
    //cache for policies
    caching:createCache(constants:POLICY_CACHE,"15");
    //cache for userinfo
    caching:createCache(constants:USER_INFO_CACHE,"15");
    //cache for api
    caching:createCache(constants:API_CACHE,"15");

    return true;
}
function getFromUserInfoCache (string userId) (json){
    if (userInfoCache[userId] != null) {
        return (json)userInfoCache[userId];
    } else {
        return null;
    }
}
function putIntoUserInfoCache (string userId, json userInfo) {
    if (userInfoCache[userId] == null) {
        userInfoCache[userId] = userInfo;
    }
}