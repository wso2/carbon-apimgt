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
    caching:putCacheEntry(constants:TOKEN_CACHE, key, introspectDto);
}
function getFromSubscriptionCache (string apiContext, string version, string consumerKey) (dto:SubscriptionDto) {
    string key = apiContext + ":" + version + ":" + consumerKey;
    any subscription = caching:getCacheEntry(constants:SUBSCRIPTION_CACHE, key);
    if (subscription != null) {
        dto:SubscriptionDto dto;
        errors:TypeCastError err;
        dto, err = (dto:SubscriptionDto)subscription;
        return dto;
    } else {
        return null;
    }
}
function putIntoSubscriptionCache (dto:SubscriptionDto subscriptionDto) {
    string key = subscriptionDto.apiContext + ":" + subscriptionDto.apiVersion + ":" + subscriptionDto.consumerKey;
    caching:putCacheEntry(constants:SUBSCRIPTION_CACHE, key, subscriptionDto);
}
function getFromResourceCache (string apiContext, string apiVersion, string resourceUri, string httpVerb) (dto:ResourceDto) {
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
    caching:removeCacheEntry(constants:TOKEN_CACHE, key);
}
function putIntoAPICache (dto:APIDTO apidto) {
    string key = apidto.context + ":" + apidto.version;
    caching:putCacheEntry(constants:API_CACHE, key, apidto);
}
function removeFromAPICache (dto:APIDTO apidto) {
    string key = apidto.context + ":" + apidto.version;
    caching:removeCacheEntry(constants:API_CACHE, key);
}
function getFromAPICache (string key) (dto:APIDTO) {
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
    gatewayConf = conf;
    apimgtMaps:putMapEntry("gatewayConfig", gatewayConf);
    keyManagerConf = conf.keyManagerInfo;
}
function getGatewayConf () (dto:GatewayConfDTO) {
    any dto = apimgtMaps:getMapEntry("gatewayConfig");
    errors:TypeCastError err;
    if(dto!=null){
        gatewayConf, err = (dto:GatewayConfDTO)dto;
        keyManagerConf = gatewayConf.keyManagerInfo;
    }
    return gatewayConf;
}
function getKeyManagerConf () (dto:KeyManagerInfoDTO) {
    getGatewayConf();
    return keyManagerConf;
}
function getAnalyticsConf () (dto:AnalyticsInfoDTO) {
    getGatewayConf();
    return gatewayConf.analyticsInfo;
}

function getGAnalyticsTrackingConf() (dto:GAnalyticsTrackingInfoDTO) {
    getGatewayConf();
    return gatewayConf.gAnalyticsTrackingInfo;
}

function getThrottleConf () (dto:ThrottlingInfoDTO) {
    getGatewayConf();
    return gatewayConf.throttlingInfo;
}

function putIntoApplicationCache (dto:ApplicationDto applicationDto) {
    caching:putCacheEntry(constants:APPLICATION_CACHE, applicationDto.applicationId, applicationDto);
}
function getFromApplicationCache (string applicationId) (dto:ApplicationDto) {
    any application = caching:getCacheEntry(constants:APPLICATION_CACHE, applicationId);
    if (application != null) {
        dto:ApplicationDto dto;
        errors:TypeCastError err;
        dto, err = (dto:ApplicationDto)application;
        return dto;
    } else {
        return null;
    }
}
function removeApplicationFromCache (string applicationId) {
    caching:removeCacheEntry(constants:APPLICATION_CACHE, applicationId);
}
function removeFromResources (string apiContext, string apiVersion) {
    string key = apiContext + ":" + apiVersion;
    caching:removeCacheEntry(constants:RESOURCE_CACHE, key);
}
function removeFromSubscriptionCache (string apiContext, string apiVersion, string consumerKey) {
    string key = apiContext + ":" + apiVersion + ":" + consumerKey;
    caching:removeCacheEntry(constants:SUBSCRIPTION_CACHE, key);
}

function initializeCache () (boolean) {
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
    if (userInfoCache[userId] == null) {
        userInfoCache[userId] = userInfo;
    }
}
function putIntoPolicyCache (dto:PolicyDto policyDto) {
    caching:putCacheEntry(constants:POLICY_CACHE, policyDto.id, policyDto);
}
function getFromPolicyCache (string id) (dto:PolicyDto) {
    any policy = caching:getCacheEntry(constants:POLICY_CACHE, id);
    if (policy != null) {
        dto:PolicyDto dto;
        errors:TypeCastError err;
        dto, err = (dto:PolicyDto)policy;
        return dto;
    } else {
        return null;
    }
}
function removeFromPolicyCache (string id) {
    caching:removeCacheEntry(constants:POLICY_CACHE, id);
}
function putIntoEndpointCache (dto:EndpointDto endpointDto) {
    caching:putCacheEntry(constants:ENDPOINT_CACHE, endpointDto.name, endpointDto);

}
function removeFromEndpointCache (string endpointId) {
    caching:removeCacheEntry(constants:ENDPOINT_CACHE, endpointId);
}
function getFromEndpointCache (string endpointId) (dto:EndpointDto) {
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
    removeFromEndpointCache(endpointDto.name);
    putIntoEndpointCache(endpointDto);
}
function addBlockConditions (dto:BlockConditionDto blockConditionDto) {
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
    any entry = apimgtMaps:getMapEntry(constants:BLOCK_CONDITION_MAP);
    map blockConditionMap = {};
    errors:TypeCastError err;
    if (entry != null) {
        blockConditionMap, err = (map)entry;
    }
    return blockConditionMap;
}