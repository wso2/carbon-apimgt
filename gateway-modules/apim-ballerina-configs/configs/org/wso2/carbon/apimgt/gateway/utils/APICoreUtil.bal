package org.wso2.carbon.apimgt.gateway.utils;

import ballerina.lang.errors;
import ballerina.lang.system;
import ballerina.lang.jsons;
import ballerina.net.http;
import ballerina.lang.messages;
import org.wso2.carbon.apimgt.gateway.constants as Constants;
import org.wso2.carbon.apimgt.gateway.dto as dto;
import org.wso2.carbon.apimgt.gateway.holders as holder;
import org.wso2.carbon.apimgt.ballerina.deployment;
import org.wso2.carbon.apimgt.ballerina.util as apimgtUtil;
function registerGateway () (json) {

    json labelInfoPayload = {};
    message request = {};
    message response = {};
    json gatewayConfig = {};
    try {
        labelInfoPayload.labelInfo = buildPayload();
        http:ClientConnector client = create http:ClientConnector(getAPICoreURL());
        messages:setJsonPayload(request, labelInfoPayload);
        response = http:ClientConnector.post (client, "/api/am/core/v1.0/gateways/register", request);
        gatewayConfig = messages:getJsonPayload(response);

        //Set gateway configuration into global cache
        dto:GatewayConfDTO gatewayConfDTO = fromJsonToGatewayConfDTO(gatewayConfig);
        holder:setGatewayConf(gatewayConfDTO);
    } catch (errors:Error error) {
        system:println("Error occurred while registering gateway in API Core. " + error.msg);
        throw error;
    }
    return gatewayConfig;
}

function loadAPIs () {

    json apis = getAPIs();
    int index = 0;
    int count = jsons:getInt(apis, "count");
    json apiList = apis.list;

    while (index < count) {

        dto:APIDTO api = fromJSONToAPIDTO(apiList[index]);

        //Retrieve API configuration
        string apiConfig ;
        int status;
        status,apiConfig = getAPIServiceConfig(api.id);
        int maxRetries = 3;
        int i =0;
        while(status == Constants:NOT_FOUND){
            apimgtUtil:wait(10000);
            status,apiConfig = getAPIServiceConfig(api.id);
            i = i+1;
            if(i>maxRetries){
                break;
            }
        }
        deployService(api, apiConfig);
        //Update API cache
        holder:putIntoAPICache(api);
        index = index + 1;
    }
}

function getAPIs () (json) {

    string apiCoreURL;
    message request = {};
    message response = {};
    json apiList;
    try {
        http:ClientConnector client = create http:ClientConnector(getAPICoreURL());
        string query = "?labels=Default";
        response = http:ClientConnector.get (client, "/api/am/core/v1.0/apis" + query, request);
        apiList = messages:getJsonPayload(response);
        return apiList;
    } catch (errors:Error e) {
        system:println("Error occurred while retrieving gateway APIs from API Core. " + e.msg);
        throw e;
    }
    return apiList;
}


function getAPIServiceConfig (string apiId) (int,string) {
    message request = {};
    message response = {};
    string apiConfig;
    int status;
    try {
        http:ClientConnector client = create http:ClientConnector(getAPICoreURL());
        response = http:ClientConnector.get (client, "/api/am/core/v1.0/apis/" + apiId + "/gateway-config", request);
        apiConfig = messages:getStringPayload(response);
        status = http:getStatusCode(response);
    } catch (errors:Error e) {
        system:println("Error occurred while retrieving service configuration for API : " + apiId);
        throw e;
    }
    return status,apiConfig;
}

function getAPICoreURL () (string){
    string apiCoreURL;
    if(system:getEnv(Constants:API_CORE_URL) != "") {
        apiCoreURL = system:getEnv(Constants:API_CORE_URL);
    } else {
        apiCoreURL = "https://localhost:9292";
    }
    holder:apiCoreUrl = apiCoreURL;

    return apiCoreURL;
}


function deployService (dto:APIDTO api, string config) {
    deployment:deployService(api.id, config,"org/wso2/carbon/apimgt/gateway");
}
function undeployService (dto:APIDTO api) {
    //TODO:To be implemented
}
function updateService (dto:APIDTO api, string config) {
    //TODO:To be implemented
}


function buildPayload () (json) {
    json label1 = {};
    label1.name = "Default";
    json label1AccessURLs = ["https://localhost:9092"];
    label1.accessUrls = label1AccessURLs;

    //json label2 = {};
    //label2.name = "Public";
    //json label2AccessURLs = ["https://local.pubfdsafe"];
    //label2.accessUrls = label2AccessURLs;

    //json labelList = [label1, label2];
    json labelList = [label1];
    json labelInfo = {};
    labelInfo.labelList = labelList;
    labelInfo.overwriteLabels = false;

    return labelInfo;
}
