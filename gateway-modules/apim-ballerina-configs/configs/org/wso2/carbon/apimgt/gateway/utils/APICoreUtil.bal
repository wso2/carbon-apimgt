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
        string apiConfig = getAPIServiceConfig(api.id);

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
        string query = "?labels=Private,Public";
        response = http:ClientConnector.get (client, "/api/am/core/v1.0/apis" + query, request);
        apiList = messages:getJsonPayload(response);
        return apiList;
    } catch (errors:Error e) {
        system:println("Error occurred while retrieving gateway APIs from API Core. " + e.msg);
        throw e;
    }
    return apiList;
}


function getAPIServiceConfig (string apiId) (string) {
    message request = {};
    message response = {};
    string apiConfig;
    try {
        http:ClientConnector client = create http:ClientConnector(getAPICoreURL());
        response = http:ClientConnector.get (client, "/api/am/core/v1.0/apis/" + apiId + "/gateway-config", request);
        apiConfig = messages:getStringPayload(response);
    } catch (errors:Error e) {
        system:println("Error occurred while retrieving service configuration for API : " + apiId);
        throw e;
    }
    return apiConfig;
}

function getAPICoreURL () (string){
    string apiCoreURL;
    if(system:getEnv(Constants:API_CORE_URL) != null) {
        apiCoreURL = system:getEnv(Constants:API_CORE_URL);
    } else {
        apiCoreURL = "https://localhost:9292";
    }
    return apiCoreURL;
}


function deployService (dto:APIDTO api, string config) {
    //TODO:To be implemented
    deployment:deployService(api.id, config);
}
function undeployService (dto:APIDTO api) {
    //TODO:To be implemented
}
function updateService (dto:APIDTO api, string config) {
    //TODO:To be implemented
}


function buildPayload () (json) {
    json label1 = {};
    label1.name = "Private";
    json label1AccessURLs = ["https://local.privatfdsafe"];
    label1.accessUrls = label1AccessURLs;

    json label2 = {};
    label2.name = "Public";
    json label2AccessURLs = ["https://local.pubfdsafe"];
    label2.accessUrls = label2AccessURLs;

    json labelList = [label1, label2];
    json labelInfo = {};
    labelInfo.labelList = labelList;
    labelInfo.overwriteLabels = false;

    return labelInfo;
}
