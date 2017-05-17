package org.wso2.carbon.apimgt.gateway.utils;
import ballerina.lang.messages;
import org.wso2.carbon.apimgt.gateway.dto as dto;
import org.wso2.carbon.apimgt.gateway.holders as holders;
import ballerina.lang.jsons;
import ballerina.net.http;
function constructAccessTokenNotFoundPayload(message response){
    json payload = {"code":900902,"message":"accessToken invalid"};
    messages:setJsonPayload(response,payload);
}
function constructAccessTokenExpiredPayload(message response){
    json payload = {"code":900901,"message":"accessToken expired"};
    messages:setJsonPayload(response,payload);
}
function fromJsonToIntrospectDto(json introspectResponse)(dto:IntrospectDto){
    dto:IntrospectDto introspectDto = {};
    introspectDto.active = jsons:getBoolean(introspectResponse,"$.active");
    if(introspectDto.active){
        introspectDto.exp = jsons:getInt(introspectResponse,"$.iat");
        introspectDto.username = jsons:getString(introspectResponse,"$.username");
        introspectDto.scope = jsons:getString(introspectResponse,"$.scope");
        introspectDto.token_type = jsons:getString(introspectResponse,"$.token_type");
        introspectDto.client_id = jsons:getString(introspectResponse,"$.client_id");
        introspectDto.iat = jsons:getInt(introspectResponse,"$.iat");
    }
    return introspectDto;
}
function fromJsonToSubscriptionDto(json subscriptionResponse)(dto:SubscriptionDto){
    dto:SubscriptionDto subscriptionDto = {};
    subscriptionDto.apiName = jsons:getString(subscriptionResponse,"$.apiName");
    subscriptionDto.apiContext = jsons:getString(subscriptionResponse,"$.apiContext");
    subscriptionDto.apiVersion = jsons:getString(subscriptionResponse,"$.apiVersion");
    subscriptionDto.apiProvider = jsons:getString(subscriptionResponse,"$.apiProvider");
    subscriptionDto.consumerKey = jsons:getString(subscriptionResponse,"$.consumerKey");
    subscriptionDto.subscriptionPolicy = jsons:getString(subscriptionResponse,"$.subscriptionPolicy");
    subscriptionDto.applicationOwner = jsons:getString(subscriptionResponse,"$.applicationOwner");
    subscriptionDto.applicationName = jsons:getString(subscriptionResponse,"$.applicationName");
    subscriptionDto.keyEnvType = jsons:getString(subscriptionResponse,"$.keyEnvType");
    return subscriptionDto;
}
function fromJsonToResourceDto(json resourceResponse)(dto:ResourceDto){
    dto:ResourceDto resourceDto = {};
    resourceDto.uriTemplate = jsons:getString(resourceResponse,"$.uriTemplate");
    resourceDto.httpVerb = jsons:getString(resourceResponse,"$.httpVerb");
    resourceDto.authType = jsons:getString(resourceResponse,"$.authType");
    string [] scopes = {};
    int length = jsons:getInt(resourceResponse,"$.scopes.length()");
    int i = 0;
    while(i<length){
        scopes[i] = jsons:getString(resourceResponse,"$.scopes["+i+"]");
        i = i+1;
    }
    return resourceDto;
}
function retrieveSubscriptions (string apiContext,string apiVersion){
    string coreUrl = "https://localhost:9293/api/am/core/v1.0";
    string query = "/subscriptions";
    if((apiContext != "") && (apiVersion != "")){
        query = query + "/?apiContext="+apiContext+"&apiVersion="+apiVersion;
    }
    message request = {};
    http:ClientConnector subscriptionConnector = create http:ClientConnector(coreUrl);
    messages:setHeader(request, "Content-Type", "application/json");
    message response = http:ClientConnector.get(subscriptionConnector,query,request);
    json subscription = messages:getJsonPayload(response);
    int length = jsons:getInt(subscription,"$.list.length()");
    int i = 0;
    while(i<length){
        holders:putIntoSubscriptionCache(fromJsonToSubscriptionDto(jsons:getJson(subscription,"$.list["+i+"]")));
        i = i+1;
    }
}
function retrieveResources (string apiContext,string apiVersion){
    string coreUrl = "https://localhost:9293/api/am/core/v1.0";
    string query = "/resources";
    if((apiContext != "") && (apiVersion != "")){
        query = query + "/?apiContext="+apiContext+"&apiVersion="+apiVersion;
        message request = {};
        http:ClientConnector subscriptionConnector = create http:ClientConnector(coreUrl);
        messages:setHeader(request, "Content-Type", "application/json");
        message response = http:ClientConnector.get(subscriptionConnector,query,request);
        json subscription = messages:getJsonPayload(response);
        int length = jsons:getInt(subscription,"$.list.length()");
        int i = 0;
        while(i<length){
            holders:putIntoResourceCache(apiContext,apiVersion,fromJsonToResourceDto(jsons:getJson(subscription,"$.list["+i+"]")));
            i = i+1;
        }
    }
}