package org.wso2.carbon.apimgt.gateway.holders;
import org.wso2.carbon.apimgt.gateway.dto as dto;
import org.wso2.carbon.apimgt.gateway.constants;
import org.wso2.carbon.apimgt.ballerina.caching;
import org.wso2.carbon.apimgt.ballerina.maps as apimgtMaps;
import ballerina.lang.system;
import ballerina.lang.maps;
import ballerina.lang.errors;

map endpointCache = {};
dto:GatewayConfDTO gatewayConf = {};
dto:KeyManagerInfoDTO keyManagerConf = {};
map applicationCache = {};
map userInfoCache = {};
string apiCoreUrl;
function getFromTokenCache (string key) (dto:IntrospectDto) {
    system:println("getFromTokenCache() in CacheHolder");
    any introspect = caching:getCacheEntry(constants:TOKEN_CACHE, key);
    if (introspect != null) {
        dto:IntrospectDto dto;
        errors:TypeCastError err;
        dto, err = (dto:IntrospectDto)introspect;
        return dto;
    } else {
        return null;
    }
}
function putIntoTokenCache (string key, dto:IntrospectDto introspectDto) {
    system:println("putIntoTokenCache() in CacheHolder");
    caching:putCacheEntry(constants:TOKEN_CACHE, key, introspectDto);
}
function getFromSubscriptionCache (string apiContext, string version, string consumerKey) (dto:SubscriptionDto) {
    system:println("getFromSubscriptionCache() in CacheHolder");
    string key = apiContext + ":" + version + ":" + consumerKey;
    any subscription = caching:getCacheEntry(constants:SUBSCRIPTION_CACHE, key);
    if (subscription != null) {
        dto:SubscriptionDto dto;
        errors:TypeCastError err;
        dto, err = (dto:SubscriptionDto)subscription;
        system:println(dto);
        return dto;
    } else {
        return null;
    }
}
function putIntoSubscriptionCache (dto:SubscriptionDto subscriptionDto) {
    system:println("putIntoSubscriptionCache() in CacheHolder");
    string key = subscriptionDto.apiContext + ":" + subscriptionDto.apiVersion + ":" + subscriptionDto.consumerKey;
    caching:putCacheEntry(constants:SUBSCRIPTION_CACHE, key, subscriptionDto);
}
function getFromResourceCache (string apiContext, string apiVersion, string resourceUri, string httpVerb) (dto:ResourceDto) {
    system:println("getFromResourceCache() in CacheHolder");
    string internalKey = resourceUri + ":" + httpVerb;
    string key = apiContext + ":" + apiVersion;
    any resourceMapEntry = caching:getCacheEntry(constants:RESOURCE_CACHE, key);
    if (resourceMapEntry != null) {
        errors:TypeCastError err;
        map resourceMap;
        resourceMap, err = (map)resourceMapEntry;
        if (resourceMap[internalKey] != null) {
            dto:ResourceDto dto;
            dto, err = (dto:ResourceDto)resourceMap[internalKey];
            return dto;
        } else {
            return null;
        }
    } else {
        return null;
    }
}
function putIntoResourceCache (string apiContext, string apiVersion, dto:ResourceDto resourceDto) {
    system:println("putIntoResourceCache() in CacheHolder");
    string internalKey = resourceDto.uriTemplate + ":" + resourceDto.httpVerb;
    string key = apiContext + ":" + apiVersion;
    map resourceMap = {};
    any resourceMapEntry = caching:getCacheEntry(constants:RESOURCE_CACHE, key);
    errors:TypeCastError err;
    if (resourceMapEntry != null) {
        resourceMap, err = (map)resourceMapEntry;
        caching:removeCacheEntry(constants:RESOURCE_CACHE, key);
    }
    resourceMap[internalKey] = resourceDto;
    caching:putCacheEntry(constants:RESOURCE_CACHE, key, resourceMap);
}
function removeFromTokenCache (string key) {
    system:println("removeFromTokenCache() in CacheHolder");
    caching:removeCacheEntry(constants:TOKEN_CACHE, key);
}
function putIntoAPICache (dto:APIDTO apidto) {
    system:println("putIntoAPICache() in CacheHolder");
    string key = apidto.context + ":" + apidto.version;
    caching:putCacheEntry(constants:API_CACHE, key, apidto);
}
function removeFromAPICache (dto:APIDTO apidto) {
    system:println("removeFromAPICache() in CacheHolder");
    string key = apidto.context + ":" + apidto.version;
    caching:removeCacheEntry(constants:API_CACHE, key);
}
function getFromAPICache (string key) (dto:APIDTO) {
    system:println("getFromAPICache() in CacheHolder");
    any api = caching:getCacheEntry(constants:API_CACHE, key);
    if (api != null) {
        dto:APIDTO dto;
        errors:TypeCastError err;
        dto, err = (dto:APIDTO)api;
        return dto;
    } else {
        return null;
    }
}
function setGatewayConf (dto:GatewayConfDTO conf) {
    system:println("setGatewayConf() in CacheHolder");
    gatewayConf = conf;
    apimgtMaps:putMapEntry("gatewayConfig", gatewayConf);
    keyManagerConf = conf.keyManagerInfo;
}
function getGatewayConf () (dto:GatewayConfDTO) {

    system:println("getGatewayConf() in CacheHolder");
    any dto = apimgtMaps:getMapEntry("gatewayConfig");

    errors:TypeCastError err;
    if(dto!=null){
        gatewayConf, err = (dto:GatewayConfDTO)dto;
        keyManagerConf = gatewayConf.keyManagerInfo;
    }
    return gatewayConf;
}
function getKeyManagerConf () (dto:KeyManagerInfoDTO) {
    system:println("getKeyManagerConf() in CacheHolder");
    getGatewayConf();
    return keyManagerConf;
}
function getAnalyticsConf () (dto:AnalyticsInfoDTO) {

    getGatewayConf();
    system:println("getAnalyticsConf() in CacheHolder");

    return gatewayConf.analyticsInfo;
}

function getThrottleConf () (dto:ThrottlingInfoDTO) {

    getGatewayConf();
    system:println("getThrottleConf() in CacheHolder");
    return gatewayConf.throttlingInfo;
}

function putIntoApplicationCache (dto:ApplicationDto applicationDto) {
    system:println("putIntoApplicationCache() in CacheHolder");
    caching:putCacheEntry(constants:APPLICATION_CACHE, applicationDto.applicationId, applicationDto);
}
function getFromApplicationCache (string applicationId) (dto:ApplicationDto) {
    system:println("getFromApplicationCache() in CacheHolder");
    any application = caching:getCacheEntry(constants:APPLICATION_CACHE, applicationId);
    if (application != null) {
        dto:ApplicationDto dto;
        errors:TypeCastError err;
        dto, err = (dto:ApplicationDto)application;
        system:println(dto);
        return dto;
    } else {
        return null;
    }
}
function removeApplicationFromCache (string applicationId) {
    system:println("removeApplicationFromCache() in CacheHolder");
    caching:removeCacheEntry(constants:APPLICATION_CACHE, applicationId);
}
function removeFromResources (string apiContext, string apiVersion) {
    system:println("removeFromResources() in CacheHolder");
    string key = apiContext + ":" + apiVersion;
    caching:removeCacheEntry(constants:RESOURCE_CACHE, key);
}
function removeFromSubscriptionCache (string apiContext, string apiVersion, string consumerKey) {
    system:println("removeFromSubscriptionCache() in CacheHolder");
    string key = apiContext + ":" + apiVersion + ":" + consumerKey;
    caching:removeCacheEntry(constants:SUBSCRIPTION_CACHE, key);
}

function initializeCache () (boolean) {
    system:println("initializeCache() in CacheHolder");
    //cache for token introspect
    caching:createCache(constants:TOKEN_CACHE, "15");
    //cache for subscription
    caching:createCache(constants:SUBSCRIPTION_CACHE, "15");
    //cache for resource
    caching:createCache(constants:RESOURCE_CACHE, "15");
    //cache for application
    caching:createCache(constants:APPLICATION_CACHE, "15");
    //cache for policies
    caching:createCache(constants:POLICY_CACHE, "15");
    //cache for userinfo
    caching:createCache(constants:USER_INFO_CACHE, "15");
    //cache for api
    caching:createCache(constants:API_CACHE, "15");
    caching:createCache(constants:ENDPOINT_CACHE, "15");

    return true;
}
function getFromUserInfoCache (string userId) (json) {
    system:println("getFromUserInfoCache() in CacheHolder");
    if (userInfoCache[userId] != null) {
        json value;
        errors:TypeCastError err;
        value, err = (json)userInfoCache[userId];
        return value;
    } else {
        return null;
    }
}
function putIntoUserInfoCache (string userId, json userInfo) {
    system:println("putIntoUserInfoCache() in CacheHolder");
    if (userInfoCache[userId] == null) {
        userInfoCache[userId] = userInfo;
    }
}
function putIntoPolicyCache (dto:PolicyDto policyDto) {
    system:println("putIntoPolicyCache() in CacheHolder");
    caching:putCacheEntry(constants:POLICY_CACHE, policyDto.id, policyDto);
}
function getFromPolicyCache (string id) (dto:PolicyDto) {
    system:println("getFromPolicyCache() in CacheHolder");
    any policy = caching:getCacheEntry(constants:POLICY_CACHE, id);
    if (policy != null) {
        dto:PolicyDto dto;
        errors:TypeCastError err;
        dto, err = (dto:PolicyDto)policy;
        system:println(dto);
        return dto;
    } else {
        return null;
    }
}
function removeFromPolicyCache (string id) {
    system:println("removeFromPolicyCache() in CacheHolder");
    caching:removeCacheEntry(constants:POLICY_CACHE, id);
}
function putIntoEndpointCache (dto:EndpointDto endpointDto) {
    system:println("putIntoEndpointCache() in CacheHolder");
    caching:putCacheEntry(constants:ENDPOINT_CACHE, endpointDto.name, endpointDto);

}
function removeFromEndpointCache (string endpointId) {
    system:println("removeFromEndpointCache() in CacheHolder");
    caching:removeCacheEntry(constants:ENDPOINT_CACHE, endpointId);
}
function getFromEndpointCache (string endpointId) (dto:EndpointDto) {
    system:println("getFromEndpointCache() in CacheHolder");
    any endpoint = caching:getCacheEntry(constants:ENDPOINT_CACHE, endpointId);
    if (endpoint != null) {
        system:println("not null");
        dto:EndpointDto dto;
        errors:TypeCastError err;
        dto, err = (dto:EndpointDto)endpoint;
        return dto;
    } else {
        return null;
    }
}
function updateEndpointCache (dto:EndpointDto endpointDto) {
    system:println("updateEndpointCache() in CacheHolder");
    removeFromEndpointCache(endpointDto.name);
    putIntoEndpointCache(endpointDto);
}
function addBlockConditions (dto:BlockConditionDto blockConditionDto) {
    system:println("addBlockConditions() in CacheHolder");
    any entry = apimgtMaps:getMapEntry(constants:BLOCK_CONDITION_MAP);
    system:println(blockConditionDto.key);
    map blockConditionMap = {};
    errors:TypeCastError err;
    if (entry != null) {
        blockConditionMap, err = (map)entry;
    }
    blockConditionMap[blockConditionDto.key] = blockConditionDto;
    apimgtMaps:putMapEntry(constants:BLOCK_CONDITION_MAP, blockConditionMap);
}
function removeBlockCondition (dto:BlockConditionDto blockConditionDto) {
    system:println("removeBlockConditions() in CacheHolder");
    any entry = apimgtMaps:getMapEntry(constants:BLOCK_CONDITION_MAP);
    map blockConditionMap = {};
    errors:TypeCastError err;
    if (entry != null) {
        blockConditionMap, err = (map)entry;
    }
    maps:remove(blockConditionMap,blockConditionDto.key);
    apimgtMaps:putMapEntry(constants:BLOCK_CONDITION_MAP, blockConditionMap);
}
function updateBlockCondition (dto:BlockConditionDto blockConditionDto) {
    system:println("updateBlockConditions() in CacheHolder");
    any entry = apimgtMaps:getMapEntry(constants:BLOCK_CONDITION_MAP);
    map blockConditionMap = {};
    errors:TypeCastError err;
    if (entry != null) {
        blockConditionMap, err = (map)entry;
    }
    maps:remove(blockConditionMap,blockConditionDto.key);
    if (blockConditionDto.enabled) {
        blockConditionMap[blockConditionDto.key] = blockConditionDto;
    }
    apimgtMaps:putMapEntry(constants:BLOCK_CONDITION_MAP, blockConditionMap);
}
function getBlockConditionMap ()(map) {
    system:println("getBlockConditionMap() in CacheHolder");
    any entry = apimgtMaps:getMapEntry(constants:BLOCK_CONDITION_MAP);
    map blockConditionMap = {};
    errors:TypeCastError err;
    if (entry != null) {
        blockConditionMap, err = (map)entry;
    }
    return blockConditionMap;
}