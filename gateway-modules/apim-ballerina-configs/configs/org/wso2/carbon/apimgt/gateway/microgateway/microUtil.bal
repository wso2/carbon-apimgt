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
            system:println("WARNING : analytics configuration not found");
        }
    }
}

function isSwaggerFile (json apiData) (boolean) {
    //returns true if it's a swagger file

    try {
        string swagger = jsons:getString(apiData,"$.swagger");
    } catch (errors:Error error) {
        return false;
    }
    return true;
}

function readFromJSONFile (string filePath) (json) {
    //******************have to check whether it is a swagger file or not
    //to read from the json data file, return the apiList
    //returns null if the json file is not a swagger file, else return the swagger file
    //returns an error if any error occured while reading the json file.

    string name = system:getEnv(Constants:GW_HOME);
    try {
        //system:println(name);
        files:File t = {path:name + "/microgateway/" + filePath};
        //files:File t = {path:"/home/sabeena/Documents/apim/testing/" + filePath};
        files:open(t, "r");

        var content, n = files:read(t, 100000000);
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
    //build the APIDto, to be passed to the API cache
    dto:APIDTO APIDTO = {};
    try {
        APIDTO.name = jsons:getString(apiData, "$.info.title");
        APIDTO.version = jsons:getString(apiData, "$.info.version");
        APIDTO.context = jsons:getString(apiData, "$.basePath");
        APIDTO.lifeCycleStatus = "PUBLISHED";
        APIDTO.securityScheme = 2;
        system:println(APIDTO);
    } catch (errors:Error error) {
        system:println("WARNING " + err.msg);
        return null;
    }
    return APIDTO;
}

function loadOfflineAPIs () {
    //load apis for the offline gateway

    string name = system:getEnv(Constants:GW_HOME);
    string[] filenames = util:listJSONFiles(name+"/microgateway");
    int index = 0;
    try {
        int count = filenames.length;
        while (index < count) {
            var apiData = readFromJSONFile(filenames[index]);
            if (isSwaggerFile(apiData)) { // if the json file is only a swagger json
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

function buildResourceDto (string url, json res, string authType) (dto:ResourceDto) {
    //build the APIDto, to be passed to the API cache

    dto:ResourceDto  resourceDto = {};
    resourceDto.uriTemplate = url;
    resourceDto.httpVerb = strings:toUpperCase(jsons:getString(res,"$.operationId"));
    resourceDto.authType = authType;
    //try{
    //    jsons:getJson(res,"$.security");
    //    resourceDto.authType = "Any";
    //} catch (errors:Error error) {
    //    resourceDto.authType = "None";
    //}
    //if (jsons:getJson(res,"$.security") != null) {
    //    resourceDto.authType = "Any";
    //} else {
    //    resourceDto.authType = "None";
    //}
    system:println(resourceDto);
    return resourceDto;
}

function retrieveOfflineResources (json apiData) {
    //put the resourceDtos of the given api into the cache

    string apiContext = jsons:getString(apiData,"$.basePath");
    string apiVersion = jsons:getString(apiData,"$.info.version");
    string authType;
    try {
        jsons:getString(apiData,"$.security");
        authType = "Any";
    } catch (errors:Error error) {
        authType = "None";
    }

    //if (jsons:getJson(apiData,"$.paths") != null) {

    json paths = jsons:getJson(apiData,"$.paths");
    string[] pathNames = util:getKeys(paths);
    int i = 0;
    int pathsCount = pathNames.length;

    while (i < pathsCount) {
        //system:println(urls[index]); // "/"
        json resources = jsons:getJson(paths,"$."+ pathNames[i]);
        string[] resourceNames = util:getKeys(resources); // urls
        int j = 0;
        int rescount = resourceNames.length;
        while (j < rescount) {
            json res = jsons:getJson(resources, "$." + resourceNames[j]);
            dto:ResourceDto resDto = buildResourceDto(pathNames[i], res, authType);
            holders:putIntoResourceCache(apiContext, apiVersion, resDto);
            j = j + 1;
        }
        i = i + 1;
    }
    //}
}

function buildSubscriptionDto (json apiData, json app, string env) (dto:SubscriptionDto) {
    //build the subscriptionDto from the json file

    dto:SubscriptionDto subscriptionDto = {};
    subscriptionDto.apiName = jsons:getString(apiData,"$.info.title");
    subscriptionDto.apiVersion = jsons:getString(apiData,"$.info.version");
    subscriptionDto.apiContext = jsons:getString(apiData,"$.basePath");
    if (env == Constants:SANDBOX) {
        subscriptionDto.consumerKey, err = (string) app.sandbox;
    } else if (env == Constants:PRODUCTION){
        subscriptionDto.consumerKey, err = (string) app.production;
    }
    subscriptionDto.keyEnvType = env;
    subscriptionDto.status = "ACTIVE";
    return subscriptionDto;
}

function retrieveOfflineSubscriptions () (boolean) {
    //put the subscriptionDtos into the subscription cache

    string name = system:getEnv(Constants:GW_HOME);
    string[] filenames = util:listJSONFiles(name + "/microgateway");
    int index = 0;
    if (filenames != null) {
        int count = filenames.length;
        while (index < count) {
            json apiData = readFromJSONFile(filenames[index]);
            if (isSwaggerFile(apiData)) {
                try {
                    json security = jsons:getJson(apiData, "$.security");
                    //int secCount = jsons:getInt(security, "$.length()");
                    json apps = security[0].api_key;
                    int noOfApps = jsons:getInt(apps, "$.length()");
                    int j = 0;
                    while (j < noOfApps) {
                        if (apps[j].sandbox != null) {
                            dto:SubscriptionDto subs = buildSubscriptionDto(apiData, apps[j], Constants:SANDBOX);
                            system:println("subs");
                            system:println(subs);
                            holders:putIntoSubscriptionCache(subs);
                        }
                        if (apps[j].production != null) {
                            dto:SubscriptionDto subs = buildSubscriptionDto(apiData, apps[j], Constants:PRODUCTION);
                            system:println("subs");
                            system:println(subs);
                            holders:putIntoSubscriptionCache(subs);
                        }
                        j = j + 1;
                    }

                } catch (errors:Error error) {
                    system:println(error.msg);
                }

                //json apps = security[0].api_key;
                //system:println("apps");
                //system:println(apps);
                //int noOfApps = jsons:getInt(apps, "$.length()");
            }
            //    json paths = jsons:getJson(apiData, "$.paths");
            //
            //    string[] pathNames = util:getKeys(paths);
            //    int i = 0;
            //    int pathsCount = pathNames.length;
            //
            //    while (i < pathsCount) {
            //        json resources = jsons:getJson(paths, "$." + pathNames[i]);
            //        string[] resourceNames = util:getKeys(resources); // urls
            //
            //        json res = jsons:getJson(resources, "$." + resourceNames[0]);
            //        try {
            //            json sec = jsons:getJson(res, "$.security");
            //            system:println("sec");
            //            system:println(sec);
            //            json apps = sec[0].api_key;
            //            system:println("apps");
            //            system:println(apps);
            //            int noOfApps = jsons:getInt(apps, "$.length()");
            //            int j = 0;
            //            while (j < noOfApps) {
            //                if (apps[j].sandbox != null) {
            //                    dto:SubscriptionDto subs = buildSubscriptionDto(apiData, Constants:SANDBOX);
            //                    system:println("subs");
            //                    system:println(subs);
            //                    holders:putIntoSubscriptionCache(subs);
            //                }
            //                if (apps[j].production != null) {
            //                    dto:SubscriptionDto subs = buildSubscriptionDto(apiData, apps[j], Constants:PRODUCTION);
            //                    system:println("subs");
            //                    system:println(subs);
            //                    holders:putIntoSubscriptionCache(subs);
            //                }
            //                j = j + 1;
            //            }
            //        } catch (errors:Error err) {
            //            system:println("No sercurity found : " + err.msg);
            //        }
            //        i = i + 1;
            //    }
            //}
            index = index+1;
        }
        return true;
    }else{
        system:println("No APIs found.");
        return false;
    }
}
