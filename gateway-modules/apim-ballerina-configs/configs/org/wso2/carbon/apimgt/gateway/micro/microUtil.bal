package org.wso2.carbon.apimgt.gateway.micro;

import ballerina.lang.errors;
import ballerina.lang.system;
import ballerina.net.http;
import ballerina.lang.jsons;
import org.wso2.carbon.apimgt.gateway.constants as Constants;
import org.wso2.carbon.apimgt.gateway.dto as dto;

import org.wso2.carbon.apimgt.ballerina.util as apimgtUtil;
import ballerina.lang.strings;
import ballerina.lang.files;
import ballerina.lang.blobs;
import org.wso2.carbon.apimgt.ballerina.util;
errors:TypeCastError err;

function returnValue(string pair)(string){
    string[] array = strings:split(pair," ");
    return array[1];
}

function returnAPI(int i,string[] array)(dto:APIDTO){
    dto:APIDTO api = {};
    api.id=returnValue(array[i+1]);
    // system:println(api.id);
    api.name=returnValue(array[i+2]);
    //system:println(api.name);
    api.context=returnValue(array[i+3]);
    //system:println(api.context);
    api.version=returnValue(array[i+4]);
    //system:println(api.version);
    api.lifeCycleStatus=returnValue(array[i+5]);
    //system:println(api.lifeCycleStatus);
    api.securityScheme,_=<int>returnValue(array[i+6]);
    //system:println(api.securityScheme);
    return api;
}
function loadOfflineAPIs () {
    system:println("start loadOfflineAPIs() in APICoreUtil");
    json apiList={};

    files:File t = {path:"/home/sabeena/Desktop/API Repo/getOfflineAPIs.txt"};
    files:open(t, "r");         //opens the file in the read mode
    var content, n = files:read(t, 100000000);
    //so there's a limit! only 100000000 can be read

    string strAPIList = blobs:toString(content, "utf-8");
    string[] array = strings:split(strAPIList, "\n");

    //system:println("array okay");
    //system:println(array);

    var count,_ = <int>array[0];
    apiList.count = count;
    //system:println(apiList);
    //dto:APIDTO list = [];
    int index=0;

    while(index<count){
        dto:APIDTO api = returnAPI(6*index,array);
        //list[index] = api;

        string apiConfig;
        int status;

        // system:println(api);

        status, apiConfig = getOfflineAPIServiceConfig(api.id);

        system:println("status");
        system:println(status);
        system:println("apiConfig");
        system:println(apiConfig);

        int maxRetries = 3;
        int i = 0;
        while (status == Constants:NOT_FOUND) {
            apimgtUtil:wait(10000);
            status, apiConfig = getOfflineAPIServiceConfig(api.id);
            i = i + 1;
            if (i > maxRetries) {
                break;
            }
        }
        //todo : tobe implement
        // deployService(api, apiConfig);
        //Update API cache
        putIntoAPICache(api);

        system:println("putIntoAPICache(api) okay!! :D");

        retrieveOfflineResources(api.context, api.version);

        index = index+1;

    }
    system:println("end loadOfflineAPIs() in APICoreUtil");
}

function getOfflineAPIServiceConfig (string apiId) (int, string) {
    system:println("start getOfflineAPIServiceConfig() in APICoreUtil");
    string apiConfig;
    int status;
    files:File target = {path:"/home/sabeena/Desktop/API Repo/"+ apiId + ".bal"};
    boolean b = files:exists(target);
    system:println(b);
    if(b){
        files:open(target, "r");
        var content, n = files:read(target, 100000000);
        apiConfig = blobs:toString(content, "utf-8");
        status = 200;
    }else{
        target = {path:"/home/sabeena/Desktop/API Repo/ErrorDTO.bal"};
        files:open(target, "r");
        var content, n = files:read(target, 100000000);
        apiConfig = blobs:toString(content, "utf-8");
        status= 404;
    }
    system:println("end getOfflineAPIServiceConfig() in APICoreUtil");
    return status, apiConfig;
}

function fromJsonToEndpointDto (json endpointConfig) (dto:EndpointDto) {
    system:println("fromJsonToEndpointDto() in APICoreUtil");
    dto:EndpointDto endpointDto = {};
    errors:TypeCastError err;
    string endpointConfigValue;
    string securityConfigValue;
    string serviceUrlValue;
    endpointConfigValue, err = (string )endpointConfig["endpointConfig"];
    json config = util:parse(endpointConfigValue);

    serviceUrlValue, err = (string )config["serviceUrl"];
    endpointDto.clientConnector = create http:ClientConnector(serviceUrlValue);
    endpointDto.name, err = (string)endpointConfig.name;
    securityConfigValue, err = (string )endpointConfig["security"];
    json security = util:parse(securityConfigValue);
    endpointDto.securityEnable, err = (boolean )security.enabled;
    if (endpointDto.securityEnable) {
        dto:Endpoint_Security endpointSecurity = {};
        endpointSecurity.username, err = (string)security.username;
        endpointSecurity.password, err = (string)security.password;
        endpointDto.security = endpointSecurity;
    }
    return endpointDto;
}


function returnVal(string pair)(string){
    string[] array = strings:split(pair,":");
    return array[1];
}

function returnSubscription(int i,string[] array)(dto:SubscriptionDto){
    dto:SubscriptionDto sub = {};

    sub.apiName=returnVal(array[i]);
    //system:println(sub.apiName);
    sub.apiContext=returnVal(array[i+1]);
    //system:println(sub.apiContext);
    sub.apiVersion=returnVal(array[i+2]);
    //system:println(sub.apiVersion);
    sub.apiProvider=returnVal(array[i+3]);
    //system:println(sub.apiProvider);
    sub.consumerKey=returnVal(array[i+4]);
    //system:println(sub.consumerKey);
    sub.subscriptionPolicy=returnVal(array[i+5]);
    //system:println(sub.subscriptionPolicy);
    sub.keyEnvType=returnVal(array[i+6]);
    //system:println(sub.keyEnvType);
    sub.applicationId=returnVal(array[i+7]);
    //system:println(sub.applicationId);
    sub.status=returnVal(array[i+8]);
    //system:println(sub.status);

    return sub;
}

function retrieveOfflineSubscriptions () (boolean) {
    system:println("retrieveOfflineSubscriptions() in microUtil.bal");
    system:println("****************************************************************************************************************");
    files:File t = {path:"/home/sabeena/Desktop/API Repo/retrieveOfflineSubscriptions.txt"};
    files:open(t, "r");
    var content, n = files:read(t, 100000000);

    string strSubsList = blobs:toString(content, "utf-8");
    string[] array = strings:split(strSubsList, "\n");
    system:println(array);
    if(array.length % 9 == 0) {
        int count = array.length / 9;
        int index = 0;

        while (index < count) {
            dto:SubscriptionDto subs = returnSubscription(9 * index, array);
            putIntoSubscriptionCache(subs);
            system:println(subs);
            index = index + 1;
        }
    }

    system:println("****************************************************************************************************************");
    return true;
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
        putIntoResourceCache(apiContext, apiVersion, res);
        //system:println(res);
        index = index + 1;
    }

    system:println("end retrieveROfflineesources() in Utils");
}

function returnApplication(int i,string[] array)(dto:ApplicationDto){
    dto:ApplicationDto app = {};

    app.applicationId = returnVal(array[i]);
    system:println(app.applicationId);
    app.applicationName = returnVal(array[i + 1]);
    system:println(app.applicationName);
    app.applicationPolicy = returnVal(array[i + 3]);
    system:println(app.applicationPolicy);
    app.applicationOwner = returnVal(array[i + 2]);
    system:println(app.applicationOwner);

    return app;
}

function retrieveOfflineApplications () (boolean) {
    system:println("retrieveOfflineApplications() in Utils");
    system:println("****************************************************************************************************************");

    files:File t = {path:"/home/sabeena/Desktop/API Repo/retrieveOfflineApplications.txt"};
    files:open(t, "r");
    var content, n = files:read(t, 100000000);

    string strSubsList = blobs:toString(content, "utf-8");
    string[] array = strings:split(strSubsList, "\n");
    system:println(array);

    if(array.length % 4 == 0) {
        int count = array.length / 4;
        int index = 0;

        while (index < count) {
            dto:ApplicationDto app = returnApplication(4* index, array);
            putIntoApplicationCache(app);
            system:println(app);
            index = index + 1;
        }
    }

    system:println("****************************************************************************************************************");
    return true;
}

function removeFromApplicationCache (json application) {
    system:println("removeFromApplicationCache() in Utils");
    string applicationId;
    applicationId, err = (string)application.applicationId;
    removeApplicationFromCache(applicationId);
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