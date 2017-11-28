package org.wso2.carbon.apimgt.gateway.microgateway;
import ballerina.lang.errors;
import ballerina.lang.system;
import ballerina.lang.strings;
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
            system:println("WARNING : gateway configuration not found");
        }
    }
}

function isSwaggerJsonFile (json apiData) (boolean) {
    try {
        string swagger = jsons:getString(apiData,"$.swagger");
    } catch (errors:Error error) {
        return false;
    }
    return true;
}

function readFromJSONFile (string filePath) (json) {
    string gwHome = system:getEnv(Constants:GW_HOME);
    try {
        files:File jsonFile = {path:gwHome + "/microgateway/" + filePath};
        files:open(jsonFile, "r");
        var content, n = files:read(jsonFile, 100000000);
        string strAPIData = blobs:toString(content, "utf-8");
        json apiData = util:parse(strAPIData);
        return apiData;
    } catch (errors:Error err) {
        system:println("ERROR: " + err.msg);
        return null;
    }
    return null;
}

function buildAPIDTO (json apiData) (dto:APIDTO) {
    dto:APIDTO APIDTO = {};
    try {
        APIDTO.name = jsons:getString(apiData, "$.info.title");
        APIDTO.version = jsons:getString(apiData, "$.info.version");
        APIDTO.context = jsons:getString(apiData, "$.basePath");
        APIDTO.lifeCycleStatus = "PUBLISHED";
        APIDTO.securityScheme = 2;
    } catch (errors:Error error) {
        system:println("WARNING " + err.msg);
        return null;
    }
    return APIDTO;
}

function loadOfflineAPIs () {
    string name = system:getEnv(Constants:GW_HOME);
    string[] filenames = util:listJSONFiles(name+"/microgateway");
    int index = 0;
    try {
        int count = filenames.length;
        while (index < count) {
            var apiData = readFromJSONFile(filenames[index]);
            if (isSwaggerJsonFile(apiData)) {
                var api = buildAPIDTO(apiData);
                holders:putIntoAPICache(api);
                retrieveOfflineResources(apiData);
            }
            index = index + 1;
        }
    } catch (errors:Error error) {
        system:println("ERROR : No APIs found! : " + err.msg);
    }
}

function buildResourceDto (string url, string res, string authType) (dto:ResourceDto) {
    dto:ResourceDto  resourceDto = {};
    resourceDto.uriTemplate = "/";
    resourceDto.httpVerb = strings:toUpperCase(res);
    resourceDto.authType = authType;
    return resourceDto;
}

function retrieveOfflineResources (json apiData) {
    string apiContext = jsons:getString(apiData,"$.basePath");
    string apiVersion = jsons:getString(apiData,"$.info.version");
    string authType;
    try {
        jsons:getString(apiData,"$.security");
        authType = "Any";
    } catch (errors:Error error) {
        authType = "None";
    }

    json paths = jsons:getJson(apiData,"$.paths");
    string[] pathNames = util:getKeys(paths);
    int i = 0;
    int pathsCount = pathNames.length;

    while (i < pathsCount) {
        json resources = jsons:getJson(paths,"$."+ pathNames[i]);
        string[] resourceNames = util:getKeys(resources);
        int j = 0;
        int rescount = resourceNames.length;
        while (j < rescount) {
            string res = resourceNames[j];
            dto:ResourceDto resDto = buildResourceDto(pathNames[i], res, authType);
            holders:putIntoResourceCache(apiContext, apiVersion, resDto);
            j = j + 1;
        }
        i = i + 1;
    }
}

function buildSubscriptionDto (json apiData, json apiKey) (dto:SubscriptionDto) {
    dto:SubscriptionDto subscriptionDto = {};
    subscriptionDto.apiName = jsons:getString(apiData,"$.info.title");
    subscriptionDto.apiVersion = jsons:getString(apiData,"$.info.version");
    subscriptionDto.apiContext = jsons:getString(apiData,"$.basePath");
    subscriptionDto.consumerKey, err = (string) apiKey;
    subscriptionDto.keyEnvType = Constants:PRODUCTION;
    subscriptionDto.status = "ACTIVE";
    return subscriptionDto;
}

function retrieveOfflineSubscriptions () (boolean) {
    string gwHome = system:getEnv(Constants:GW_HOME);
    string[] filenames = util:listJSONFiles(gwHome + "/microgateway");

    int index = 0;
    if (filenames != null) {
        int count = filenames.length;
        while (index < count) {
            json apiData = readFromJSONFile(filenames[index]);
            if (isSwaggerJsonFile(apiData)) {
                try {
                    json security = jsons:getJson(apiData, "$.security");
                    int secCount = jsons:getInt(security, "$.length()");
                    json apps = security[0].api_key; //always the first element has to be api_key
                    int noOfApps = jsons:getInt(apps, "$.length()");
                    int j = 0;
                    while (j < noOfApps) {
                        dto:SubscriptionDto subs = buildSubscriptionDto(apiData, apps[j]);
                        holders:putIntoSubscriptionCache(subs);
                        j = j + 1;
                    }
                } catch (errors:Error error) {
                    system:println(error.msg);
                }
            }
            index = index+1;
        }
        return true;
    }else{
        system:println("No APIs found.");
        return false;
    }
}
