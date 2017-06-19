package org.wso2.carbon.apimgt.gateway.utils;
import ballerina.lang.messages;
import ballerina.lang.jsons;
import ballerina.lang.errors;
import ballerina.lang.system;
import ballerina.net.http;
import org.wso2.carbon.apimgt.gateway.dto as dto;
import org.wso2.carbon.apimgt.gateway.holders as holders;

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
function constructSubscriptionBlocked (message response,string context,string version) {
    json payload = {
                       "fault": {
                                    "code": 900907,
                                    "message": "The requested API is temporarily blocked",
                                    "description":"Access failure for API: " + context + ", version: " + version + " status: (900907) - The requested API is temporarily blocked"
                                }
                   };
    http:setStatusCode(response, 401);
    messages:setJsonPayload(response, payload);
}
function constructAPIIsInMaintenance (message response) {
    messages:setHeader(response, "Content-Type", "application/json");
    http:setStatusCode(response, 503);
    json payload = {"code":700700, "message":"This API has been blocked temporarily"};
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
    subscriptionDto.keyEnvType = (string)subscriptionResponse.keyEnvType;
    subscriptionDto.applicationId = (string)subscriptionResponse.applicationId;
    subscriptionDto.status = (string)subscriptionResponse.status;
    return subscriptionDto;
}
function fromJsonToResourceDto (json resourceResponse) (dto:ResourceDto){
    dto:ResourceDto resourceDto = {};
    resourceDto.uriTemplate = (string)resourceResponse.uriTemplate;
    resourceDto.httpVerb = (string)resourceResponse.httpVerb;
    resourceDto.authType = (string)resourceResponse.authType;
    resourceDto.scope = (string)resourceResponse.scope;
    resourceDto.policy = (string)resourceResponse.policy;
    return resourceDto;
}
function retrieveSubscriptions () (boolean){
    string query = "/api/am/core/v1.0/subscriptions?limit=-1";
    message request = {};
    http:ClientConnector apiInfoConnector = create http:ClientConnector(getAPICoreURL());
    messages:setHeader(request, "Content-Type", "application/json");
    message response = http:ClientConnector.get (apiInfoConnector, query, request);
    json subscriptions = messages:getJsonPayload(response);
    putIntoSubscriptionCache(subscriptions.list);
    return true;
}

function putIntoSubscriptionCache (json subscriptions) {
    int length = jsons:getInt(subscriptions, "$.length()");
    int i = 0;
    while (i < length) {
        json subscription = subscriptions[i];
        dto:SubscriptionDto subscriptionDto = fromJsonToSubscriptionDto(subscription);
        holders:putIntoSubscriptionCache(subscriptionDto);
        i = i + 1;
    }

}

function removeFromSubscriptionCache (json subscriptions) {
    int length = jsons:getInt(subscriptions, "$.length()");
    int i = 0;
    while (i < length) {
        json subscription = subscriptions[i];
        dto:SubscriptionDto subscriptionDto = fromJsonToSubscriptionDto(subscription);
        holders:removeFromSubscriptionCache(subscriptionDto.apiContext, subscriptionDto.apiVersion, subscriptionDto
                                                                                                    .consumerKey);
        i = i + 1;
    }

}

function retrieveResources (string apiContext, string apiVersion) {
    string query = "/api/am/core/v1.0/resources/?apiContext=" + apiContext + "&apiVersion=" + apiVersion;
    message request = {};
    http:ClientConnector apiInfoConnector = create http:ClientConnector(getAPICoreURL());
    messages:setHeader(request, "Content-Type", "application/json");
    message response = http:ClientConnector.get (apiInfoConnector, query, request);
    json resources = messages:getJsonPayload(response);
    int length = jsons:getInt(resources, "$.list.length()");
    int i = 0;
    while (i < length) {
        json resource1 = resources.list[i];
        holders:putIntoResourceCache(apiContext, apiVersion, fromJsonToResourceDto(resource1));
        i = i + 1;
    }
}
function retrieveApplications () (boolean) {
    string query = "/api/am/core/v1.0/applications";
    message request = {};
    http:ClientConnector apiInfoConnector = create http:ClientConnector(getAPICoreURL());
    messages:setHeader(request, "Content-Type", "application/json");
    message response = http:ClientConnector.get (apiInfoConnector, query, request);
    json applications = messages:getJsonPayload(response);
    int length = jsons:getInt(applications, "$.list.length()");
    int i = 0;
    if (length > 0) {
        while (i < length) {
            json application = applications.list[i];
            putIntoApplicationCache(application);
            i = i + 1;
        }
    }
    return true;
}

function putIntoApplicationCache (json application) {
    dto:ApplicationDto applicationDto = {};
    applicationDto.applicationId = (string)application.applicationId;
    applicationDto.applicationName = (string)application.name;
    applicationDto.applicationOwner = (string)application.subscriber;
    applicationDto.applicationPolicy = (string)application.throttlingTier;
    holders:putIntoApplicationCache(applicationDto);
}

function removeFromApplicationCache (json application) {
    string applicationId = (string)application.applicationId;
    holders:removeApplicationFromCache(applicationId);
}
function fromJsonToGatewayConfDTO (json conf) (dto:GatewayConfDTO){
    dto:GatewayConfDTO gatewayConf = {};

    //Extract key manager information and populate KeyManageInfoDTO to be cached
    json keyManagerInfo = conf.keyManagerInfo;
    dto:KeyManagerInfoDTO keyManagerInfoDTO = {};
    keyManagerInfoDTO.dcrEndpoint = (string)keyManagerInfo.dcrEndpoint;

    keyManagerInfoDTO.tokenEndpoint = (string)keyManagerInfo.tokenEndpoint;
    keyManagerInfoDTO.revokeEndpoint = (string)keyManagerInfo.revokeEndpoint;
    keyManagerInfoDTO.introspectEndpoint = (string)keyManagerInfo.introspectEndpoint;

    dto:CredentialsDTO keyManagerCredentialsDTO = {};
    json keyManagerCredentials = keyManagerInfo.credentials;
    keyManagerCredentialsDTO.username = (string)keyManagerCredentials.username;
    keyManagerCredentialsDTO.password = (string)keyManagerCredentials.password;
    gatewayConf.keyManagerInfo = keyManagerInfoDTO;
    keyManagerInfoDTO.credentials = keyManagerCredentialsDTO;
    //Extract JWT information and populate JWTInfoDTO to be cached
    json jwTInfo = conf.jwTInfo;
    dto:JWTInfoDTO jwtInfoDTO = {};
    jwtInfoDTO.enableJWTGeneration = (boolean )jwTInfo.enableJWTGeneration;
    jwtInfoDTO.jwtHeader = (string )jwTInfo.jwtHeader;
    gatewayConf.jwtInfo = jwtInfoDTO;

    //Extract Analytics Server information and populate AnalyticsInfoDTO to be cached
    json analyticsInfo = conf.analyticsInfo;
    dto:AnalyticsInfoDTO analyticsInfoDTO = {};
    analyticsInfoDTO.serverURL = (string )analyticsInfo.serverURL;
    dto:CredentialsDTO analyticsServerCredentialsDTO = {};
    json analyticsServerCredentials = analyticsInfo.credentials;
    analyticsServerCredentialsDTO.username = (string )analyticsServerCredentials.username;
    analyticsServerCredentialsDTO.password = (string )analyticsServerCredentials.password;
    analyticsInfoDTO.credentials = analyticsServerCredentialsDTO;
    gatewayConf.analyticsInfo = analyticsInfoDTO;

    //Extract Throttling Server information and populate ThrottlingInfoDTO to be cached
    json throttlingInfo = conf.throttlingInfo;
    dto:ThrottlingInfoDTO throttlingInfoDTO = {};
    throttlingInfoDTO.serverURL = (string )throttlingInfo.serverURL;
    json throttlingServerCredentials = throttlingInfo.credentials;
    dto:CredentialsDTO throttlingServerCredentialsDTO = {};
    throttlingServerCredentialsDTO.username = (string)throttlingServerCredentials.username;
    throttlingServerCredentialsDTO.password = (string)throttlingServerCredentials.password;
    throttlingInfoDTO.credentials = throttlingServerCredentialsDTO;
    gatewayConf.throttlingInfo = throttlingInfoDTO;

    return gatewayConf;
}

function fromJSONToAPIDTO (json api) (dto:APIDTO){
    dto:APIDTO APIDTO = {};
    APIDTO.id = (string)api.id;
    APIDTO.name = (string)api.name;
    APIDTO.version = (string)api.version;
    APIDTO.context = (string)api.context;
    APIDTO.lifeCycleStatus = (string)api.lifeCycleStatus;
    return APIDTO;

}

function getSystemProperty (string prop) (string) {
    string pathValue = "";
    try {
        pathValue = system:getEnv(prop);
        if (pathValue != "") {
            return pathValue;
        }
    } catch (errors:Error e) {
        return "";
    }
    return pathValue;
}

function getStringProperty (message msg, string propertyKey) (string){
    string value = "";
    try {
        value = messages:getProperty(msg, propertyKey);
        if (value != "") {
            return value;
        }
    } catch (errors:Error e) {
        return "";
    }
    return value;
}

function getJsonString (json jsonObject, string jsonPath) (string){
    string value = "";
    try {
        value = (string )jsonObject.jsonPath;
    } catch (errors:Error e) {
        return "";
    }
    return value;
}