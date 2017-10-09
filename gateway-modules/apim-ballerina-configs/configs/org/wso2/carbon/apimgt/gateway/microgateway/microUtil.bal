package org.wso2.carbon.apimgt.gateway.microgateway;
import ballerina.lang.errors;
import ballerina.lang.system;
import ballerina.lang.jsons;
import org.wso2.carbon.apimgt.gateway.dto as dto;
import org.wso2.carbon.apimgt.gateway.holders as holders;
import ballerina.lang.files;
import ballerina.lang.blobs;
import org.wso2.carbon.apimgt.ballerina.util;
import org.wso2.carbon.apimgt.gateway.utils as gatewayUtil;
import org.wso2.carbon.apimgt.gateway.constants as Constants;
errors:TypeCastError err;

function loadConfigs () {

    string name = system:getEnv(Constants:GW_HOME);
    files:File t = {path:name + "/microgateway/microConf.json"};

    if (files:exists(t)) {
        try {
            files:open(t, "r");
            var content, n = files:read(t, 100000000);

            string strConf = blobs:toString(content, "utf-8");
            json conf = util:parse(strConf);
            dto:GatewayConfDTO gatewayConfDTO = gatewayUtil:fromJsonToGatewayConfDTO(conf);
            holders:setGatewayConf(gatewayConfDTO);
        } catch (errors:Error error) {
            system:println("WARNING : analytics configuration not found");
        }
    }
}

function readFromJSONFile () (json) {
    //to read from the json data file, return the apiList

    string name = system:getEnv(Constants:GW_HOME);
    try {
        system:println(name);
        files:File t = {path:name + "/microgateway/apiKeys.json"};
        files:open(t, "r");
        system:println("my java check@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");

        //files:File target = {path:"/microgateway"};
        //system:println(files:exists(target));

        string[] filenames = util:listJSONFiles("/home/sabeena/Documents/apim/testing");
        if (filenames != null) {
            system:println(filenames[0]);
        }else{
            system:println("null :/");
        }
        system:println("my java check@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");
        var content, n = files:read(t, 100000000);
        string strAPIData = blobs:toString(content, "utf-8");
        json apiData = util:parse(strAPIData);
        return apiData.apis;
    } catch (errors:Error err) {
        system:println("ERROR: " + err.msg);
        return null;
    }
    return null;
}

function buildAPIDTO (json api) (dto:APIDTO, errors:Error) {
    //build the APIDto, to be passed to the API cache

    if (api.name != null && api.version != null && api.context != null && api.securityScheme != null) {
        dto:APIDTO APIDTO = {};
        APIDTO.name, err = (string)api.name;
        APIDTO.version, err = (string)api.version;
        APIDTO.context, err = (string)api.context;
        if(api.lifeCycleStatus != null){
            APIDTO.lifeCycleStatus,err = (string)api.lifeCycleStatus;
        } else {
            APIDTO.lifeCycleStatus = "PUBLISHED";
        }
        APIDTO.securityScheme = jsons:getInt(api, "$.securityScheme");
        return APIDTO,null;
    } else {
        errors:Error error = {msg:"Prime attribute missing for the API"};
        return null,error;
    }

}

function loadOfflineAPIs () {
    //load apis for the offline gateway

    json apiList = readFromJSONFile();

    if (apiList != null) {
        int index = 0;
        int count = jsons:getInt(apiList, "$.length()");

        while (index<count) {
            var api,error = buildAPIDTO(apiList[index]);
            if (error == null) {
                holders:putIntoAPICache(api);
                retrieveOfflineResources(api.context, api.version);
            } else {
                system:println("ERROR: " + error.msg);
            }
            index = index+1;
        }
    }
}

function buildSubscriptionDto (json api,json app,string env) (dto:SubscriptionDto,errors:Error) {
    //build the subscriptionDto from the json file

    if (api.name != null && api.context != null && api.version != null) {
        dto:SubscriptionDto subscriptionDto = {};
        subscriptionDto.apiName, err = (string)api.name;
        subscriptionDto.apiContext, err = (string)api.context;
        subscriptionDto.apiVersion, err = (string)api.version;
        if (env == Constants:SANDBOX) {
            subscriptionDto.consumerKey, err = (string)app.sandbox;
        } else if (env == Constants:PRODUCTION){
            subscriptionDto.consumerKey, err = (string)app.production;
        }
        subscriptionDto.applicationName, err = (string)app.name;
        subscriptionDto.keyEnvType = env;
        subscriptionDto.status = "ACTIVE";
        return subscriptionDto,null;
    } else {
        errors:Error error = {msg:"Subscription Details missing!"};
        return null,error;
    }

}

function retrieveOfflineSubscriptions () (boolean) {
    //put the subscriptionDtos into the subscription cache

    json apiList = readFromJSONFile();
    if(apiList != null){
        int noOfAPIs = jsons:getInt(apiList, "$.length()");
        int i = 0;
        while(i < noOfAPIs) {
            int j = 0;
            json apps = apiList[i].apps;
            int noOfApps = jsons:getInt(apps,"$.length()");
            while (j<noOfApps){
                if(apps[j].sandbox != null){
                    var subs,error = buildSubscriptionDto(apiList[i],apps[j],Constants:SANDBOX);
                    if(error == null){
                        holders:putIntoSubscriptionCache(subs);
                    }else{
                        system:println("WARNING: " + error.msg);
                    }
                }

                if(apps[j].production != null){
                    var subs,error = buildSubscriptionDto(apiList[i],apps[j],Constants:PRODUCTION);
                    if(error == null){
                        holders:putIntoSubscriptionCache(subs);
                    }else{
                        system:println("WARNING: " + error.msg);
                    }
                }
                j = j + 1;
            }
            i = i + 1;
        }
        return true;
    }else{
        return false;
    }

}

function findResources (string apiContext, string apiVersion) (json, errors:Error) {
    //to find out the resources of that particular api

    json apiList = readFromJSONFile();
    json resources;
    int i = 0;
    int count = jsons:getInt(apiList, "$.length()");
    string context;
    string version;

    while (i < count) {
        context, err = (string)apiList[i].context;
        version, err = (string)apiList[i].version;
        if (context == apiContext && version == apiVersion) {
            resources = apiList[i].resources;
            if (resources == null) {
                errors:Error error = {msg:"Resources not found for the API"};
                return null,error;
            }
            break;
        }
        i = i + 1;
    }
    return resources,null;
}

function retrieveOfflineResources (string apiContext, string apiVersion) {
    //put the resourceDtos of the given api to the cache

    var resources, error = findResources(apiContext, apiVersion);
    if (error == null){
        int length = jsons:getInt(resources, "$.length()");
        int j = 0;
        while (j < length) {
            json resource1 = resources[j];
            var res, _ = <dto:ResourceDto>resource1;
            holders:putIntoResourceCache(apiContext, apiVersion, res);
            j = j + 1;
        }
    } else {
        system:println("WARNING: " + error.msg);
    }
}