package org.wso2.carbon.apimgt.gateway.utils;

import ballerina.lang.errors;
import ballerina.lang.system;
import ballerina.net.http;
import ballerina.lang.messages;
import org.wso2.carbon.apimgt.gateway.constants as Constants;
import org.wso2.carbon.apimgt.gateway.dto as dto;
import org.wso2.carbon.apimgt.gateway.holders as holder;
import org.wso2.carbon.apimgt.ballerina.deployment;
import org.wso2.carbon.apimgt.ballerina.util as apimgtUtil;
import ballerina.lang.strings;
import org.wso2.carbon.apimgt.ballerina.util;
function registerGateway () (json) {

    json labelInfoPayload = {};
    message request = {};
    message response = {};
    json gatewayConfig = {};
    try {
        labelInfoPayload.labelInfo = buildPayload();
        http:ClientConnector client = create http:ClientConnector(getAPICoreURL());
        messages:setJsonPayload(request, labelInfoPayload);
        response = http:ClientConnector.post(client, "/api/am/core/v1.0/gateways/register", request);
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
    errors:TypeCastError err;
    int count;
    count, err = (int)apis.count;
    json apiList = apis.list;

    while (index < count) {

        dto:APIDTO api = fromJSONToAPIDTO(apiList[index]);

        //Retrieve API configuration
        string apiConfig;
        int status;
        status, apiConfig = getAPIServiceConfig(api.id);
        int maxRetries = 3;
        int i = 0;
        while (status == Constants:NOT_FOUND) {
            apimgtUtil:wait(10000);
            status, apiConfig = getAPIServiceConfig(api.id);
            i = i + 1;
            if (i > maxRetries) {
                break;
            }
        }
        //todo : tobe implement
        // deployService(api, apiConfig);
        //Update API cache
        holder:putIntoAPICache(api);
        retrieveResources(api.context, api.version);
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
        response = http:ClientConnector.get(client, "/api/am/core/v1.0/apis" + query, request);
        apiList = messages:getJsonPayload(response);
        return apiList;
    } catch (errors:Error e) {
        system:println("Error occurred while retrieving gateway APIs from API Core. " + e.msg);
        throw e;
    }
    return apiList;
}
function getEndpoints () (json) {

    string apiCoreURL;
    message request = {};
    message response = {};
    json endpointList;
    try {
        http:ClientConnector client = create http:ClientConnector(getAPICoreURL());
        string query = "?limit=-1";
        response = http:ClientConnector.get(client, "/api/am/core/v1.0/endpoints" + query, request);
        endpointList = messages:getJsonPayload(response);
        return endpointList;
    } catch (errors:Error e) {
        system:println("Error occurred while retrieving gateway APIs from API Core. " + e.msg);
        throw e;
    }
    return endpointList;
}
function getBlockConditions () (json) {

    string apiCoreURL;
    message request = {};
    message response = {};
    json blockConditionList;
    try {
        http:ClientConnector client = create http:ClientConnector(getAPICoreURL());
        string query = "?limit=-1";
        response = http:ClientConnector.get(client, "/api/am/core/v1.0/blacklist" + query, request);
        blockConditionList = messages:getJsonPayload(response);
        return blockConditionList;
    } catch (errors:Error e) {
        system:println("Error occurred while retrieving gateway APIs from API Core. " + e.msg);
        throw e;
    }
    return blockConditionList;
}
function loadGlobalEndpoints () {

    json endpoints = getEndpoints();
    int index = 0;
    errors:TypeCastError err;
    int count;
    count, err = (int)endpoints.count;
    json endpointList = endpoints.list;

    while (index < count) {

        dto:EndpointDto endpoint = fromJsonToEndpointDto(endpointList[index]);
        holder:putIntoEndpointCache(endpoint);
        index = index+1;
    }
}
function loadBlockConditions () {

    json blockConditions = getBlockConditions();
    int index = 0;
    errors:TypeCastError err;
    int count;
    count, err = (int)blockConditions.count;
    json blockConditionList = blockConditions.list;

    while (index < count) {

        dto:BlockConditionDto condition = fromJsonToBlockConditionDto(blockConditionList[index]);
        holder:addBlockConditions(condition);
        index = index+1;
    }
}

function getAPIServiceConfig (string apiId) (int, string) {
    message request = {};
    message response = {};
    string apiConfig;
    int status;
    try {
        http:ClientConnector client = create http:ClientConnector(getAPICoreURL());
        response = http:ClientConnector.get(client, "/api/am/core/v1.0/apis/" + apiId + "/gateway-config", request);
        apiConfig = messages:getStringPayload(response);
        status = http:getStatusCode(response);
    } catch (errors:Error e) {
        system:println("Error occurred while retrieving service configuration for API : " + apiId);
        throw e;
    }
    return status, apiConfig;
}
function getEndpointConfig (string endpointId) (int, string) {
    message request = {};
    message response = {};
    string apiConfig;
    int status;
    try {
        http:ClientConnector client = create http:ClientConnector(getAPICoreURL());
        response = http:ClientConnector.get(client, "/api/am/core/v1.0/endpoints/" + endpointId + "/gateway-config", request);
        apiConfig = messages:getStringPayload(response);
        status = http:getStatusCode(response);
    } catch (errors:Error e) {
        system:println("Error occurred while retrieving service configuration for Endpoint : " + endpointId);
        throw e;
    }
    return status, apiConfig;
}
function fromJsonToEndpointDto (json endpointConfig) (dto:EndpointDto) {
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
function getAPICoreURL () (string) {
    string apiCoreURL;

    if (getSystemProperty(Constants:API_CORE_URL) != "") {
        apiCoreURL = getSystemProperty(Constants:API_CORE_URL);
    } else {
        apiCoreURL = "https://localhost:9292";
    }
    return apiCoreURL;
}


function deployService (dto:APIDTO api, string config) {
    string fileName = api.id + ".bal";
    string serviceName = api.name + "_" + strings:replace(api.id, "-", "_");
    deployment:deployService(fileName, serviceName, config, "org/wso2/carbon/apimgt/gateway");
}
function deployFile (string id, string config) {
    string fileName = id + ".bal";
    deployment:deploy(fileName, config, "org/wso2/carbon/apimgt/gateway");
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
