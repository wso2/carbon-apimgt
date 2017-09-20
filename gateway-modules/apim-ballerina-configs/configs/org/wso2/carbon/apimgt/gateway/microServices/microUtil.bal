package org.wso2.carbon.apimgt.gateway.microServices;

import ballerina.lang.errors;
import ballerina.lang.system;

import ballerina.lang.jsons;
import org.wso2.carbon.apimgt.gateway.constants as Constants;
import org.wso2.carbon.apimgt.gateway.dto as dto;
import org.wso2.carbon.apimgt.gateway.holders as holders;
import org.wso2.carbon.apimgt.ballerina.util as apimgtUtil;

import ballerina.lang.files;
import ballerina.lang.blobs;
import org.wso2.carbon.apimgt.ballerina.util;
errors:TypeCastError err;


function loadAPIKeys () {
    system:println("start loadAPIKeys() in APICoreUtil");

    //string name = system:getEnv("OFFLINE_GATEWAY_REPO");
    string name = system:getEnv("GW_HOME");

    system:println(name);

    files:File t = {path:name+"/microgateway/apiKeys.json"};
    //replace the path seperaor
    files:open(t, "r");         //opens the file in the read mode
    var content, n = files:read(t, 100000000);
    //so there's a limit! only 100000000 can be read

    string strAPIKeyList = blobs:toString(content, "utf-8");

    json apiKeys = util:parse(strAPIKeyList);

    int index = 0;
    errors:TypeCastError err;
    int count;
    count, err = (int)apiKeys.count;
    json apiList = apiKeys.list;

    while(index<count){
        dto:APIKeyDTO apiKey = fromJSONToAPIKeyDTO(apiList[index]);
        //list[index] = api;

        string apiConfig;
        int status;

        // system:println(api);

        status, apiConfig = getOfflineAPIServiceConfig(apiKey.name);

        system:println("status");
        system:println(status);
        system:println("apiConfig");
        system:println(apiConfig);

        int maxRetries = 3;
        int i = 0;
        while (status == Constants:NOT_FOUND) {
            apimgtUtil:wait(10000);
            status, apiConfig = getOfflineAPIServiceConfig(apiKey.name);
            i = i + 1;
            if (i > maxRetries) {
                break;
            }
        }
        //todo : tobe implement
        // deployService(api, apiConfig);
        //Update API cache
        holders:putIntoAPIKeyCache(apiKey);

        system:println("api :");
        system:println(apiKey);

        system:println("putIntoAPIKeyCache(api) okay!! :D");

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