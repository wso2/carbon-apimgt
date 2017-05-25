package org.wso2.carbon.apimgt.gateway.utils;
import ballerina.lang.messages;
import ballerina.lang.jsons;
import ballerina.lang.errors;
import ballerina.lang.system;
import ballerina.net.http;
import org.wso2.carbon.apimgt.gateway.dto as dto;
import org.wso2.carbon.apimgt.gateway.holders as holders;
import org.wso2.carbon.apimgt.gateway.constants as Constants;

function constructAccessTokenNotFoundPayload (message response) {
    json payload = {"code":900902, "message":"accessToken invalid"};
    messages:setJsonPayload(response, payload);
}
function constructAccessTokenExpiredPayload (message response) {
    json payload = {"code":900901, "message":"accessToken expired"};
    messages:setJsonPayload(response, payload);
}
function constructSubscriptionNotFound (message response) {
    json payload = {"code":900903, "message":"subscription not found"};
    messages:setJsonPayload(response, payload);
}
function fromJsonToIntrospectDto (json introspectResponse) (dto:IntrospectDto){
    dto:IntrospectDto introspectDto = {};
    introspectDto.active = (boolean)introspectResponse.active;
    if (introspectDto.active) {
        if (introspectResponse.exp != null) {
            introspectDto.exp = (int)introspectResponse.exp;
        } else {
            // https://github.com/ballerinalang/ballerina/issues/2396
            introspectDto.exp = - 1;
        }
        if (introspectResponse.username != null) {
            introspectDto.username = (string)introspectResponse.username;
        }
        if (introspectResponse.scope != null) {
            introspectDto.scope = (string)introspectResponse.scope;
        }
        if (introspectResponse.token_type != null) {
            introspectDto.token_type = (string)introspectResponse.token_type;
        }
        if (introspectResponse.iat != null) {
            introspectDto.iat = (int)introspectResponse.iat;
        }
        if (introspectResponse.client_id != null) {
            introspectDto.client_id = (string)introspectResponse.client_id;
        }
    }
    return introspectDto;
}
function fromJsonToSubscriptionDto (json subscriptionResponse) (dto:SubscriptionDto){
    dto:SubscriptionDto subscriptionDto = {};
    subscriptionDto.apiName = (string)subscriptionResponse.apiName;
    subscriptionDto.apiContext = (string)subscriptionResponse.apiContext;
    subscriptionDto.apiVersion = (string)subscriptionResponse.apiVersion;
    subscriptionDto.apiProvider = (string)subscriptionResponse.apiProvider;
    subscriptionDto.consumerKey = (string)subscriptionResponse.consumerKey;
    subscriptionDto.subscriptionPolicy = (string)subscriptionResponse.subscriptionPolicy;
    subscriptionDto.applicationOwner = (string)subscriptionResponse.applicationOwner;
    subscriptionDto.applicationName = (string)subscriptionResponse.applicationName;
    subscriptionDto.keyEnvType = (string)subscriptionResponse.keyEnvType;
    subscriptionDto.applicationTier = (string)subscriptionResponse.applicationTier;
    subscriptionDto.applicationId = (string)subscriptionResponse.applicationId;
    return subscriptionDto;
}
function fromJsonToResourceDto (json resourceResponse) (dto:ResourceDto){
    dto:ResourceDto resourceDto = {};
    resourceDto.uriTemplate = (string)resourceResponse.uriTemplate;
    resourceDto.httpVerb = (string)resourceResponse.httpVerb;
    resourceDto.authType = (string)resourceResponse.authType;
    resourceDto.scope = (string)resourceResponse.scope;
    return resourceDto;
}
function retrieveSubscriptions (json subscriptions) {
    int length = jsons:getInt(subscriptions, "$.length()");
    int i = 0;
    while (i < length) {
        dto:SubscriptionDto subscriptionDto = fromJsonToSubscriptionDto(subscriptions[i]);
        dto:ApplicationDto applicationDto = {};
        applicationDto.applicationId = subscriptionDto.applicationId;
        applicationDto.applicationName = subscriptionDto.applicationName;
        applicationDto.applicationOwner = subscriptionDto.applicationOwner;
        applicationDto.applicationPolicy = subscriptionDto.applicationTier;
        holders:putIntoSubscriptionCache(subscriptionDto);
        holders:putIntoApplicationCache(applicationDto);
        i = i + 1;
    }
}
function retrieveResources (string apiContext, string apiVersion, json resources) {
    int length = jsons:getInt(resources, "$.length()");
    int i = 0;
    while (i < length) {
        holders:putIntoResourceCache(apiContext, apiVersion, fromJsonToResourceDto(resources[i]));
        i = i + 1;
    }
}
function retrieveAPIInformation (string apiContext, string apiVersion) {
    system:println(apiContext);
    string coreUrl = "https://localhost:9293/api/am/core/v1.0";
    string query = "/apis-summary/?apiContext=" + apiContext + "&apiVersion=" + apiVersion;
    message request = {};
    http:ClientConnector apiInfoConnector = create http:ClientConnector(coreUrl);
    messages:setHeader(request, "Content-Type", "application/json");
    message response = http:ClientConnector.get (apiInfoConnector, query, request);
    json apiInfo = messages:getJsonPayload(response);
    int length = jsons:getInt(apiInfo, "$.list.length()");
    if (length > 0) {
        json resources = apiInfo["list"][0]["resources"];
        json subscriptions = apiInfo["list"][0]["subscriptions"];
        retrieveResources(apiContext, apiVersion, resources);
        retrieveSubscriptions(subscriptions);
        system:println("after");
    }
}

function fromJsonToGatewayConf (json conf) (dto:GatewayConf){
    dto:GatewayConf gatewayConf = {};
    gatewayConf.keyManagerURL = "";
    gatewayConf.brokerURL = "";

    return gatewayConf;
}

function fromJSONToAPIDto (json api) (dto:APIDto){
    dto:APIDto apiDto = {};
    apiDto.id = jsons:getString(api, "id");
    apiDto.name = jsons:getString(api, "name");
    apiDto.version = jsons:getString(api, "version");
    apiDto.context = jsons:getString(api, "context");
    return apiDto;

}

function getAPIServiceConfig (string apiId) (string) {
    message request = {};
    message response = {};
    string apiConfig;
    try {
        http:ClientConnector client = create http:ClientConnector(getSystemProperty(Constants:API_CORE_URL));
        response = http:ClientConnector.get (client, "/api/am/core/v1.0/apis/" + apiId + "/gateway-config", request);
        apiConfig = messages:getStringPayload(response);
    } catch (errors:Error e) {
        system:println("[Error] : Error occurred while retrieving service configuration for API : " + apiId);
        throw e;
    }
    return apiConfig;
}

function getSystemProperty (string prop) (string) {
    string pathValue = system:getEnv(prop);
    return pathValue;
}

function deployService (dto:APIDto api, string config) {
    //TODO:To be implemented
}
function undeployService (dto:APIDto api) {
    //TODO:To be implemented
}
function updateService (dto:APIDto api, string config) {
    //TODO:To be implemented
}