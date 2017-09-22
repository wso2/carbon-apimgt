package org.wso2.carbon.apimgt.gateway.microServices;

import ballerina.lang.errors;
import ballerina.lang.system;

import ballerina.lang.jsons;

import org.wso2.carbon.apimgt.gateway.dto as dto;
import org.wso2.carbon.apimgt.gateway.holders as holders;

import ballerina.lang.files;
import ballerina.lang.blobs;
import org.wso2.carbon.apimgt.ballerina.deployment;
import org.wso2.carbon.apimgt.ballerina.util;
errors:TypeCastError err;

function readFromJSONFile()(json){

    string name = system:getEnv("GW_HOME");
    system:println(name);
    files:File t = {path:name+"/microgateway/apiKeys.json"};
    //replace the path seperaor
    files:open(t, "r");         //opens the file in the read mode
    var content, n = files:read(t, 100000000);
    //so there's a limit! only 100000000 can be read

    string strAPIKeyList = blobs:toString(content, "utf-8");

    json apiKeys = util:parse(strAPIKeyList);
    system:println("end readFromJSONFile in microUtil");
    system:println(apiKeys);
    return apiKeys;

}

function loadOfflineAPIs () {
    system:println("start loadAPIKeys() in APICoreUtil");

    //string name = system:getEnv("OFFLINE_GATEWAY_REPO");
    json apiKeys = readFromJSONFile();

    int index = 0;
    errors:TypeCastError err;

    json apiList = apiKeys.list;
    int count = jsons:getInt(apiList, "$.length()");

    while(index<count){
        system:println(apiList[index]);
        dto:APIDTO api = buildAPIDTO(apiList[index]);
        holders:putIntoAPICache(api);
        retrieveOfflineResources(api.context, api.version);
        system:println("api :");
        system:println(api);

        system:println("putIntoAPICache(api) okay!! :D");

        //retrieveOfflineResources(api.context, api.version);
        index = index+1;

    }
    system:println("end loadAPIKeys() in APICoreUtil");
}

function getOfflineAPIServiceConfig (string apiName) (int, string) {
    //string name = system:getEnv("OFFLINE_GATEWAY_REPO");
    system:println("start getOfflineAPIServiceConfig() in APICoreUtil");
    string apiConfig;
    int status;
    string name = system:getEnv("GW_HOME");
    files:File target = {path:name+"/microgateway/"+ apiName + ".bal"};
    boolean b = files:exists(target);
    system:println(b);
    if(b){
        files:open(target, "r");
        var content, error = files:read(target, 100000000);
        apiConfig = blobs:toString(content, "utf-8");
        status = 200;
    }else{
        target = {path:"/home/sabeena/Desktop/API-Repo/ErrorDTO.bal"};
        files:open(target, "r");
        var content, error = files:read(target, 100000000);
        apiConfig = blobs:toString(content, "utf-8");
        status= 404;
    }
    //if any error,log
    system:println("end getOfflineAPIServiceConfig() in APICoreUtil");
    return status, apiConfig;
}


function fromJSONToAPIKeyDTO (json apiKey) (dto:APIKeyDTO) {
    system:println("fromJSONToAPIKeyDTO() in Utils");
    dto:APIKeyDTO APIKeyDTO = {};

    APIKeyDTO.name, err = (string)apiKey.name;
    APIKeyDTO.context, err = (string)apiKey.context;
    APIKeyDTO.apiKey, err = (string)apiKey.apiKey;
    APIKeyDTO.securityScheme = jsons:getInt(apiKey, "$.securityScheme");
    return APIKeyDTO;

}

function buildAPIDTO (json api) (dto:APIDTO) {
    system:println("buildAPIDTO() in microUtils");
    dto:APIDTO APIDTO = {};
    APIDTO.name, err = (string)api.name;
    APIDTO.version, err = (string)api.version;
    APIDTO.context, err = (string)api.context;
    APIDTO.lifeCycleStatus = "PUBLISHED";
    APIDTO.securityScheme = jsons:getInt(api, "$.securityScheme");
    return APIDTO;
    //have to add another method to retrieveSubscriptions
}


function retrieveOfflineSubscriptions () (boolean) {
    //build the subscriptionDto from the json file and put them into the Subscription cache
    system:println("retrieveOfflineSubscriptions() in microUtils");
    system:println("****************************************************************************************************************");
    json apiKeys = readFromJSONFile();
    json apiList = apiKeys.list;
    int length = jsons:getInt(apiList, "$.length()");
    int i =0;
    while(i<length){
        holders:putIntoSubscriptionCache(buildSubscriptionDto(apiList[i]));
        i = i+1;
    }
    system:println("****************************************************************************************************************");
    return true;
}

function buildSubscriptionDto (json data) (dto:SubscriptionDto) {
    system:println("buildSubscriptionDto() in microUtils");
    dto:SubscriptionDto subscriptionDto = {};
    subscriptionDto.apiName, err = (string)data.name;
    subscriptionDto.apiContext, err = (string)data.context;
    subscriptionDto.apiVersion, err = (string)data.version;
    subscriptionDto.consumerKey, err = (string)data.apiKey;
    subscriptionDto.keyEnvType = "PRODUCTION"; //not using sandbox???
    subscriptionDto.status = "ACTIVE";
    return subscriptionDto;
}

function retrieveOfflineResources (string apiContext, string apiVersion) {
    //chceck this before the code review

    system:println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
    system:println("retrieveOfflineResources() in microUtils");

    int i = 0;
    string[] httpVerbs = ["DELETE","GET","HEAD","PATCH","POST","PUT"];

    while (i < httpVerbs.length) {
        dto:ResourceDto resourceDto = buildResourceDto(httpVerbs[i]);
        system:println("resourceDto1  : ");
        system:println(resourceDto);
        holders:putIntoResourceCache(apiContext, apiVersion, resourceDto);
        i = i + 1;
    }

    system:println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
}

function buildResourceDto(string httpVerb) (dto:ResourceDto) {
    //Sab, review this method b4 the code review!

    system:println("buildResourceDto() in microUtils");
    dto:ResourceDto resourceDto = {};

    resourceDto.uriTemplate = "/";
    resourceDto.httpVerb = httpVerb;
    resourceDto.authType = "Any";
    return resourceDto;
}

function deployService (dto:APIDTO api, string config) {
    system:println("deployService() in microUtil");
    string fileName = api.name + ".bal";
    string serviceName = api.name;
    deployment:deployService(fileName, serviceName, config, "microgateway");
}