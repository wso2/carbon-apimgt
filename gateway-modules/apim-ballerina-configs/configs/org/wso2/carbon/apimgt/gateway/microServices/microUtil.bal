package org.wso2.carbon.apimgt.gateway.microServices;

import ballerina.lang.errors;
import ballerina.lang.system;

import ballerina.lang.jsons;

import org.wso2.carbon.apimgt.gateway.dto as dto;
import org.wso2.carbon.apimgt.gateway.holders as holders;

import ballerina.lang.files;
import ballerina.lang.blobs;

import org.wso2.carbon.apimgt.ballerina.util;
errors:TypeCastError err;

function readFromJSONFile()(json){

    string name = system:getEnv("GW_HOME");
    files:File t = {path:name+"/microgateway/apiKeys.json"};
    //replace the path seperator                           ??????????????????
    files:open(t, "r");
    var content, n = files:read(t, 100000000);
    //so there's a limit! only 100000000 can be read         ????????????????

    string strAPIKeyList = blobs:toString(content, "utf-8");
    json apiData = util:parse(strAPIKeyList);
    //system:println(apiData);
    system:println("end readFromJSONFile in microUtil");
    return apiData;

}

function loadOfflineAPIs () {
    system:println("start loadAPIKeys() in APICoreUtil");

    //string name = system:getEnv("OFFLINE_GATEWAY_REPO");
    json apiData = readFromJSONFile();

    int index = 0;
    json apiList = apiData.list;
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

function buildAPIDTO (json api) (dto:APIDTO) {
    system:println("buildAPIDTO() in microUtils");
    dto:APIDTO APIDTO = {};
    APIDTO.name, err = (string)api.name;
    APIDTO.version, err = (string)api.version;
    APIDTO.context, err = (string)api.context;
    APIDTO.lifeCycleStatus = "PUBLISHED";
    APIDTO.securityScheme = jsons:getInt(api, "$.securityScheme");
    return APIDTO;
}


function retrieveOfflineSubscriptions () (boolean) {
    //build the subscriptionDto from the json file and put them into the Subscription cache
    system:println("retrieveOfflineSubscriptions() in microUtils");
    json apiData = readFromJSONFile();
    json apiList = apiData.list;
    int noOfAPIs = jsons:getInt(apiList, "$.length()");
    int i =0;
    while(i< noOfAPIs) {
        int j=0;
        json apps = apiList[i].apps;
        //json[] apps = <json[]>apiList[i].apiKeys;        //'json' cannot be converted to 'json[]'
        //int noOfApps = jsons:getInt(apps,"$.length");   //arraynode, no length property
        //int noOfApps = lengthof apps;                   //lengthof not an idetifier
        //int noOfApps = apps.length;                     //json cannot be assigned to int
        //int noOfApps = apps.length();           //invalid action invocation. connector variable expected
        int noOfApps = 2;
        system:println(noOfApps);
        while (j<noOfApps){
            int k=0;
            while(k<2){
                holders:putIntoSubscriptionCache(buildSubscriptionDto(apiList[i],apps[j],k));
                k=k+1;
            }
            j=j+1;
        }
        i = i+1;
    }
    return true;
}

function buildSubscriptionDto (json api,json app,int i) (dto:SubscriptionDto) {
    //if i==0, production else if i==1, sandbox
    system:println("buildSubscriptionDto() in microUtils");

    dto:SubscriptionDto subscriptionDto = {};
    subscriptionDto.apiName, err = (string)api.name;
    subscriptionDto.apiContext, err = (string)api.context;
    subscriptionDto.apiVersion, err = (string)api.version;
    subscriptionDto.consumerKey, err = (string)app.keys[i];
    subscriptionDto.applicationId, err = (string)app.appId;
    subscriptionDto.applicationName, err = (string)app.name;
    subscriptionDto.applicationOwner, err = (string)app.owner;
    if(i== 0){
        subscriptionDto.keyEnvType = "PRODUCTION";
    }else{
        subscriptionDto.keyEnvType = "SAND BOX";
    }
    subscriptionDto.status = "ACTIVE";

    system:println("subscriptionDto");
    system:println(subscriptionDto);
    return subscriptionDto;
}
function findResources (string apiContext, string apiVersion)(json) {
    //to find out the resources of that particular api

    json apiData = readFromJSONFile();
    json resources;
    int i = 0;
    json apiList = apiData.list;
    int count = jsons:getInt(apiList, "$.length()");
    string context;
    string version;
    while(i<count){
        context, err = (string)apiList[i].context;
        version, err = (string)apiList[i].version;
        if(context == apiContext && version == apiVersion){
            resources = apiList[i].resources;
            break;
        }
        i = i +1;
    }
    return resources;
}

function retrieveOfflineResources (string apiContext, string apiVersion) {
    //chceck this before the code review
    system:println("retrieveOfflineResources() in microUtils");

    json resources = findResources(apiContext,apiVersion);
    int length = jsons:getInt(resources, "$.length()");//Evaluates the JSONPath on a JSON object and returns the integer value.
    int j = 0;
    while (j < length) {
        json resource1 = resources[j];
        var res, _ = <dto:ResourceDto>resource1;
        system:println("resource1  : ");
        system:println(resource1);
        holders:putIntoResourceCache(apiContext, apiVersion, res);
        j = j + 1;
    }
}

function buildApplicationDto(json app) (dto:ApplicationDto) {
    //Sab, review this method b4 the code review!

    system:println("buildApplicationDto() in microUtils");
    dto:ApplicationDto applicationDto = {};

    applicationDto.applicationId,err = (string)app.appId;
    applicationDto.applicationName,err  = (string)app.name;
    applicationDto.applicationOwner,err = (string)app.owner;
    return applicationDto;
}

function retrieveOfflineApplications () (boolean) {
    //build the applicationDto from the json file and put them into the Subscription cache

    json apiData = readFromJSONFile();
    json apiList = apiData.list;
    int noOfAPIs = jsons:getInt(apiList, "$.length()");
    int i =0;
    while(i< noOfAPIs) {
        int j=0;
        json apps = apiList[i].apps;
        //int noOfApps = jsons:getInt(apps,"$.length");   //arraynode, no length property
        //int noOfApps = lengthof apps;                   //lengthof not an idetifier
        //int noOfApps = apps.length;                     //json cannot be assigned to int
        //int noOfApps = apps.length();           //invalid action invocation. connector variable expected
        int noOfApps = 2;
        system:println(noOfApps);
        while (j<noOfApps){
            holders:putIntoApplicationCache(buildApplicationDto(apps[j]));
            system:println(apps[j]);
            j=j+1;
        }
        i = i+1;
    }
    return true;
}