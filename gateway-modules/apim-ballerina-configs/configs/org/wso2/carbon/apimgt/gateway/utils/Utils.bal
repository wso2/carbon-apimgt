package org.wso2.carbon.apimgt.gateway.utils;
import ballerina.lang.messages;
import ballerina.lang.jsons;
import ballerina.lang.errors;
import ballerina.lang.system;
import ballerina.net.http;
import org.wso2.carbon.apimgt.gateway.dto as dto;
import org.wso2.carbon.apimgt.gateway.holders as holders;
import org.wso2.carbon.apimgt.gateway.constants;
import ballerina.lang.strings;
import ballerina.lang.blobs;
import ballerina.lang.files;
errors:TypeCastError err;

function constructAccessTokenNotFoundPayload (message response) {
    system:println("constructAccessTokenNotFoundPayload() in Utils");
    json payload = {"code":900902, "message":"accessToken invalid"};
    messages:setJsonPayload(response, payload);
}
function constructAccessTokenExpiredPayload (message response) {
    system:println("constructAccessTokenExpiredPayload() in Utils");
    json payload = {"code":900901, "message":"accessToken expired"};
    messages:setJsonPayload(response, payload);
}
function constructSubscriptionNotFound (message response) {
    system:println("constructSubscriptionNotFound() in Utils");
    json payload = {"code":900903, "message":"subscription not found"};
    messages:setJsonPayload(response, payload);
}
function constructSubscriptionBlocked (message response, string context, string version) {
    system:println("constructSubscriptionBlocked() in Utils");
    json payload = {
                       "fault":{
                                   "code":900907,
                                   "message":"The requested API is temporarily blocked",
                                   "description":"Access failure for API: " + context + ", version: " + version + " status: (900907) - The requested API is temporarily blocked"
                               }
                   };
    http:setStatusCode(response, 401);
    messages:setJsonPayload(response, payload);
}
function constructAPIIsInMaintenance (message response) {
    system:println("constructAPIIsInMaintenance() in Utils");
    messages:setHeader(response, "Content-Type", "application/json");
    http:setStatusCode(response, 503);
    json payload = {"code":700700, "message":"This API has been blocked temporarily"};
    messages:setJsonPayload(response, payload);
}

function constructIncorrectAuthorization (message response) {
    json payload = {"code":900902, "message":"Incorrect authorization details found"};
    messages:setJsonPayload(response, payload);
}

function fromJsonToIntrospectDto (json introspectResponse) (dto:IntrospectDto) {
    system:println("fromJsonToIntrospectDto() in Utils");
    dto:IntrospectDto introspectDto = {};
    introspectDto.active, err = (boolean)introspectResponse.active;
    if (introspectDto.active) {
        if (introspectResponse.exp != null) {
            introspectDto.exp, err = (int)introspectResponse.exp;
        } else {
            // https://github.com/ballerinalang/ballerina/issues/2396
            introspectDto.exp = -1;
        }
        if (introspectResponse.username != null) {
            introspectDto.username, err = (string)introspectResponse.username;
        }
        if (introspectResponse.scope != null) {
            introspectDto.scope, err = (string)introspectResponse.scope;
        }
        if (introspectResponse.token_type != null) {
            introspectDto.token_type, err = (string)introspectResponse.token_type;
        }
        if (introspectResponse.iat != null) {
            introspectDto.iat, err = (int)introspectResponse.iat;
        }
        if (introspectResponse.client_id != null) {
            introspectDto.client_id, err = (string)introspectResponse.client_id;
        }
    }
    return introspectDto;
}
function fromJsonToSubscriptionDto (json subscriptionResponse) (dto:SubscriptionDto) {
    system:println("fromJsonToSubscriptionDto() in Utils");
    dto:SubscriptionDto subscriptionDto = {};
    subscriptionDto.apiName, err = (string)subscriptionResponse.apiName;
    subscriptionDto.apiContext, err = (string)subscriptionResponse.apiContext;
    subscriptionDto.apiVersion, err = (string)subscriptionResponse.apiVersion;
    subscriptionDto.apiProvider, err = (string)subscriptionResponse.apiProvider;
    subscriptionDto.consumerKey, err = (string)subscriptionResponse.consumerKey;
    subscriptionDto.subscriptionPolicy, err = (string)subscriptionResponse.subscriptionPolicy;
    subscriptionDto.keyEnvType, err = (string)subscriptionResponse.keyEnvType;
    subscriptionDto.applicationId, err = (string)subscriptionResponse.applicationId;
    subscriptionDto.status, err = (string)subscriptionResponse.status;
    return subscriptionDto;
}
function fromJsonToResourceDto (json resourceResponse) (dto:ResourceDto) {
    system:println("fromJsonToResourceDto() in Utils");
    dto:ResourceDto resourceDto = {};
    resourceDto.uriTemplate, err = (string)resourceResponse.uriTemplate;
    resourceDto.httpVerb, err = (string)resourceResponse.httpVerb;
    resourceDto.authType, err = (string)resourceResponse.authType;
    resourceDto.scope, err = (string)resourceResponse.scope;
    resourceDto.policy, err = (string)resourceResponse.policy;
    return resourceDto;
}
function retrieveSubscriptions () (boolean) {
    system:println("retrieveSubscriptions() in Utils");
    string query = "/api/am/core/v1.0/subscriptions?limit=-1";
    message request = {};
    http:ClientConnector apiInfoConnector = create http:ClientConnector(getAPICoreURL());
    messages:setHeader(request, "Content-Type", "application/json");
    message response = http:ClientConnector.get(apiInfoConnector, query, request);
    json subscriptions = messages:getJsonPayload(response);
    system:println("subscriptions : ");
    system:println(subscriptions);
    putIntoSubscriptionCache(subscriptions.list);
    return true;
}

function putIntoSubscriptionCache (json subscriptions) {
    system:println("putIntoSubscriptionCache() in Utils");
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
    system:println("removefromSubscriptionCache() in Utils");
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

function returnVal(string pair)(string){
    string[] array = strings:split(pair,":");
    return array[1];
}

function returnResource(int i,string[] array)(dto:ResourceDto){
    dto:ResourceDto res = {};

    res.uriTemplate=returnVal(array[i]);
    system:println(res.uriTemplate);
    res.httpVerb=returnVal(array[i+1]);
    system:println(res.httpVerb);
    res.authType=returnVal(array[i+2]);
    system:println(res.authType);
    res.policy=returnVal(array[i+3]);
    system:println(res.policy);
    res.scope=returnVal(array[i+4]);
    system:println(res.scope);

    return res;
}

function retrieveOfflineResources(string apiContext, string apiVersion){
    system:println("start retrieveOfflineResources() in Utils");

    files:File t = {path:"/home/sabeena/Desktop/API Repo/retrieveOfflineResources.txt"};
    files:open(t, "r");
    var content, n = files:read(t, 100000000);

    string strAPIList = blobs:toString(content, "utf-8");
    string[] array = strings:split(strAPIList, "\n");
    system:println(array);

    int index = 0;

    while(index<6){
        dto:ResourceDto res = returnResource(5*index,array);
        holders:putIntoResourceCache(apiContext, apiVersion, res);
        system:println(res);
        index = index + 1;
    }

    system:println("end retrieveROfflineesources() in Utils");
}

function retrieveResources (string apiContext, string apiVersion) {
    system:println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
    system:println("retrieveResources() in Utils");

    string query = "/api/am/core/v1.0/resources/?apiContext=" + apiContext + "&apiVersion=" + apiVersion;
    message request = {};
    http:ClientConnector apiInfoConnector = create http:ClientConnector(getAPICoreURL());
    messages:setHeader(request, "Content-Type", "application/json");  //message object, header name, header value
    message response = http:ClientConnector.get(apiInfoConnector, query, request);
    json resources = messages:getJsonPayload(response);
    int length = jsons:getInt(resources, "$.list.length()");//Evaluates the JSONPath on a JSON object and returns the integer value.
    int i = 0;
    while (i < length) {
        json resource1 = resources.list[i];

        system:println("resource1  : ");
       system:println(resource1);
        holders:putIntoResourceCache(apiContext, apiVersion, fromJsonToResourceDto(resource1));
        i = i + 1;
    }

    system:println("resources :");
    system:println(resources);

    system:println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
}
function retrieveApplications () (boolean) {
    system:println("retrieveApplications() in Utils");
    string query = "/api/am/core/v1.0/applications";
    message request = {};
    http:ClientConnector apiInfoConnector = create http:ClientConnector(getAPICoreURL());
    messages:setHeader(request, "Content-Type", "application/json");
    message response = http:ClientConnector.get(apiInfoConnector, query, request);
    json applications = messages:getJsonPayload(response);
    int length = jsons:getInt(applications, "$.list.length()");
    int i = 0;
    system:println("applications :");
    system:println(applications);
    if (length > 0) {
        while (i < length) {
            json application = applications.list[i];
            putIntoApplicationCache(application);
            i = i + 1;
        }
    }
    return true;
}
function retrievePolicies () (boolean) {
    system:println("retrievePolicies() in Utils");
    string query = "/api/am/core/v1.0/policies";
    message request = {};
    http:ClientConnector apiInfoConnector = create http:ClientConnector(getAPICoreURL());
    messages:setHeader(request, "Content-Type", "application/json");
    message response = http:ClientConnector.get(apiInfoConnector, query, request);
    json policies = messages:getJsonPayload(response);
    system:println("policies :");
    system:println(policies);
    int length;
    length, err = (int)policies.count;
    int i = 0;
    if (length > 0) {
        while (i < length) {
            json policy = policies.list[i];
            putIntoPolicyCache(policy);
            i = i + 1;
        }
    }
    return true;
}

function putIntoApplicationCache (json application) {
    system:println("putIntoApplicationCache() in Utils");
    dto:ApplicationDto applicationDto = {};
    applicationDto.applicationId, err = (string)application.applicationId;
    applicationDto.applicationName, err = (string)application.name;
    applicationDto.applicationOwner, err = (string)application.subscriber;
    applicationDto.applicationPolicy, err = (string)application.throttlingTier;
    holders:putIntoApplicationCache(applicationDto);
}
function putIntoPolicyCache (json policy) {
    system:println("putIntoPolicyCache() in Utils");
    dto:PolicyDto policyDto = {};
    policyDto.id, err = (string)policy.id;
    policyDto.name, err = (string)policy.name;
    policyDto.stopOnQuotaReach, err = (boolean)policy.stopOnQuotaReach;
    holders:putIntoPolicyCache(policyDto);
}
function removeFromApplicationCache (json application) {
    system:println("removeFromApplicationCache() in Utils");
    string applicationId;
    applicationId, err = (string)application.applicationId;
    holders:removeApplicationFromCache(applicationId);
}
function fromJsonToGatewayConfDTO (json conf) (dto:GatewayConfDTO) {
    system:println("fromJsonToGatewayConfDTO() in Utils");
    dto:GatewayConfDTO gatewayConf = {};

    //Extract key manager information and populate KeyManageInfoDTO to be cached
    json keyManagerInfo = conf.keyManagerInfo;
    dto:KeyManagerInfoDTO keyManagerInfoDTO = {};
    keyManagerInfoDTO.dcrEndpoint, err = (string)keyManagerInfo.dcrEndpoint;

    keyManagerInfoDTO.tokenEndpoint, err = (string)keyManagerInfo.tokenEndpoint;
    keyManagerInfoDTO.revokeEndpoint, err = (string)keyManagerInfo.revokeEndpoint;
    keyManagerInfoDTO.introspectEndpoint, err = (string)keyManagerInfo.introspectEndpoint;

    dto:CredentialsDTO keyManagerCredentialsDTO = {};
    json keyManagerCredentials = keyManagerInfo.credentials;
    keyManagerCredentialsDTO.username, err = (string)keyManagerCredentials.username;
    keyManagerCredentialsDTO.password, err = (string)keyManagerCredentials.password;
    gatewayConf.keyManagerInfo = keyManagerInfoDTO;
    keyManagerInfoDTO.credentials = keyManagerCredentialsDTO;
    //Extract JWT information and populate JWTInfoDTO to be cached
    json jwTInfo = conf.jwTInfo;
    dto:JWTInfoDTO jwtInfoDTO = {};
    jwtInfoDTO.enableJWTGeneration, err = (boolean)jwTInfo.enableJWTGeneration;
    jwtInfoDTO.jwtHeader, err = (string)jwTInfo.jwtHeader;
    gatewayConf.jwtInfo = jwtInfoDTO;

    //todo: pass the missed attributes from APIM core
    //Extract Analytics Server information and populate AnalyticsInfoDTO to be cached
    json analyticsInfo = conf.analyticsInfo;
    dto:AnalyticsInfoDTO analyticsInfoDTO = {};
    analyticsInfoDTO.enabled, err = (boolean)analyticsInfo.enabled;
    analyticsInfoDTO.type = "binary"; //(string)analyticsInfo.type;
    analyticsInfoDTO.serverURL, err = (string)analyticsInfo.serverURL;
    analyticsInfoDTO.authServerURL = "ssl://localhost:9712"; //(string)analyticsInfo.authServerURL;
    dto:CredentialsDTO analyticsServerCredentialsDTO = {};
    json analyticsServerCredentials = analyticsInfo.credentials;
    analyticsServerCredentialsDTO.username, err = (string)analyticsServerCredentials.username;
    analyticsServerCredentialsDTO.password, err = (string)analyticsServerCredentials.password;
    analyticsInfoDTO.credentials = analyticsServerCredentialsDTO;
    gatewayConf.analyticsInfo = analyticsInfoDTO;

    //Extract Throttling Server information and populate ThrottlingInfoDTO to be cached
    json throttlingInfo = conf.throttlingInfo;
    dto:ThrottlingInfoDTO throttlingInfoDTO = {};
    throttlingInfoDTO.enabled = true; //(boolean)throttlingInfo.enabled;
    throttlingInfoDTO.type = "binary"; //(string)throttlingInfo.type;
    throttlingInfoDTO.serverURL, err = (string)throttlingInfo.serverURL;
    throttlingInfoDTO.authServerURL = "ssl://localhost:9712"; //(string)throttlingInfo.authServerURL;
    json throttlingServerCredentials = throttlingInfo.credentials;
    dto:CredentialsDTO throttlingServerCredentialsDTO = {};
    throttlingServerCredentialsDTO.username, err = (string)throttlingServerCredentials.username;
    throttlingServerCredentialsDTO.password, err = (string)throttlingServerCredentials.password;
    throttlingInfoDTO.credentials = throttlingServerCredentialsDTO;
    gatewayConf.throttlingInfo = throttlingInfoDTO;

    return gatewayConf;
}

function fromJSONToAPIDTO (json api) (dto:APIDTO) {
    system:println("fromJsonToAPIDTO() in Utils");
    dto:APIDTO APIDTO = {};
    APIDTO.id, err = (string)api.id;
    APIDTO.name, err = (string)api.name;
    APIDTO.version, err = (string)api.version;
    APIDTO.context, err = (string)api.context;
    APIDTO.lifeCycleStatus, err = (string)api.lifeCycleStatus;
    APIDTO.securityScheme = jsons:getInt(api, "$.securityScheme");
    return APIDTO;

}

function getSystemProperty (string prop) (string) {
    system:println("getSystemProperty() in Utils");
    string pathValue = "";
    try {
        pathValue = system:getEnv(prop);
        system:println(pathValue);
        if (pathValue != "") {
            return pathValue;
        }
    } catch (errors:Error e) {
        return "";
    }
    return pathValue;
}

function getStringProperty (message msg, string propertyKey) (string) {
    system:println("getStringProperty() in Utils");
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

function getJsonString (json jsonObject, string jsonPath) (string) {
    system:println("getJsonString() in Utils");
    string value = "";
    value, err = (string)jsonObject.jsonPath;
    return value;
}
function fromJsonToBlockConditionDto (json event) (dto:BlockConditionDto) {
    system:println("fromJsonToBlockConditionDto() in Utils");
    string key = "";
    dto:BlockConditionDto blockConditionDto = {};
    blockConditionDto.enabled, err = (boolean)event.enabled;
    blockConditionDto.conditionType, err = (string)event.conditionType;
    key = key + blockConditionDto.conditionType;
    blockConditionDto.uuid, err = (string)event.uuid;
    if (blockConditionDto.conditionType == constants:BLOCKING_CONDITION_IP_RANGE) {
        blockConditionDto.startingIP, err = (int)event.startingIP;
        blockConditionDto.endingIP, err = (int)event.endingIP;
        key = key + " : " + blockConditionDto.startingIP + " : " + blockConditionDto.endingIP;
    } else if (blockConditionDto.conditionType == constants:BLOCKING_CONDITIONS_IP) {
        blockConditionDto.fixedIp, err = (int)event.fixedIp;
        key = key + " : " + blockConditionDto.fixedIp;
    } else {
        blockConditionDto.conditionValue, err = (string)event.conditionValue;
    }
    blockConditionDto.key = key;
    return blockConditionDto;
}