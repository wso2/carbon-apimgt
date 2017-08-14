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
import ballerina.lang.files;
import ballerina.lang.blobs;
import org.wso2.carbon.apimgt.ballerina.util;
function registerGateway () (json) {
    system:println("registerGateway() in APICoreUtil");
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

    system:println("loadAPIs() in APICoreUtil");
    system:println(apis);
    system:println(apiList);
    while (index < count) {
        //for every API loaded this will run

        dto:APIDTO api = fromJSONToAPIDTO(apiList[index]);
        system:println(apiList[index]);
        //Retrieve API configuration
        string apiConfig;
        int status;
        system:println("*******************************************************");
        system:println("api :");
        system:println(api);
        system:println("api.id :");
        system:println(api.id);
        system:println("*******************************************************");

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
    system:println("start getAPIs() in APICoreUtil");
    string apiCoreURL;
    message request = {};
    message response = {};
    json apiList;
    try {
        http:ClientConnector client = create http:ClientConnector(getAPICoreURL());
        string query = "?labels=Default";
        response = http:ClientConnector.get(client, "/api/am/core/v1.0/apis" + query, request);
        apiList = messages:getJsonPayload(response);
        system:println(apiList);

        // compare with the offline repo, if any change update it accordingly

        system:println("end getAPIs() in APICoreUtil");
        return apiList;
    } catch (errors:Error e) {
        system:println("Error occurred while retrieving gateway APIs from API Core. " + e.msg);
        throw e;
    }
    system:println("end getAPIs() in APICoreUtil");
    return apiList;
}

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
        dto:APIDTO api = returnAPI(5*index,array);
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
        holder:putIntoAPICache(api);

        //system:println("ai me :(");

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

function getEndpoints () (json) {
    system:println("getEndpoints() in APICoreUtil");
    string apiCoreURL;
    message request = {};
    message response = {};
    json endpointList;
    try {
        http:ClientConnector client = create http:ClientConnector(getAPICoreURL());
        string query = "?limit=-1";
        response = http:ClientConnector.get(client, "/api/am/core/v1.0/endpoints" + query, request);
        endpointList = messages:getJsonPayload(response);
        system:println("endpointList");
        system:println(endpointList);
        return endpointList;
    } catch (errors:Error e) {
        system:println("Error occurred while retrieving gateway APIs from API Core. " + e.msg);
        throw e;
    }
    return endpointList;
}

function getOfflineEndpoints () (json) {
    system:println("getOfflineEndpoints() in APICoreUtil");
    string apiCoreURL;
    message request = {};
    message response = {};
    json endpointList;
    try {
        http:ClientConnector client = create http:ClientConnector(getAPICoreURL());
        string query = "?limit=-1";
        response = http:ClientConnector.get(client, "/api/am/core/v1.0/endpoints" + query, request);
        endpointList = messages:getJsonPayload(response);
        system:println(endpointList);
        return endpointList;
    } catch (errors:Error e) {
        system:println("Error occurred while retrieving gateway APIs from API Core. " + e.msg);
        throw e;
    }
    return endpointList;
}

function getBlockConditions () (json) {
    system:println("getBlockConditions() in APICoreUtil");
    string apiCoreURL;
    message request = {};
    message response = {};
    json blockConditionList;
    try {
        http:ClientConnector client = create http:ClientConnector(getAPICoreURL());
        string query = "?limit=-1";
        response = http:ClientConnector.get(client, "/api/am/core/v1.0/blacklist" + query, request);
        blockConditionList = messages:getJsonPayload(response);
        system:println("block Condition List");
        system:println(blockConditionList);
        return blockConditionList;
    } catch (errors:Error e) {
        system:println("Error occurred while retrieving gateway APIs from API Core. " + e.msg);
        throw e;
    }
    return blockConditionList;
}
function loadGlobalEndpoints () {
    system:println("loadGlobalEndpoints() in APICoreUtil");
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
    system:println("loadBlockConditions() in APICoreUtil");
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
    system:println("getAPIServiceConfig() in APICoreUtil");
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
    system:println("status");
    system:println(status);
    system:println("apiConfig");
    system:println(apiConfig);
    return status, apiConfig;
}

function getEndpointConfig (string endpointId) (int, string) {
    system:println("getEndpointConfig() in APICoreUtil");
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

function getAPICoreURL () (string) {
    system:println("getAPICoreURL() in APICoreUtil");
    string apiCoreURL;

    if (getSystemProperty(Constants:API_CORE_URL) != "") {
        apiCoreURL = getSystemProperty(Constants:API_CORE_URL);
    } else {
        apiCoreURL = "https://localhost:9292";
    }
    return apiCoreURL;
}

function deployService (dto:APIDTO api, string config) {
    system:println("deployService() in APICoreUtil");
    string fileName = api.id + ".bal";
    string serviceName = api.name + "_" + strings:replace(api.id, "-", "_");
    deployment:deployService(fileName, serviceName, config, "org/wso2/carbon/apimgt/gateway");
}
function deployFile (string id, string config) {
    system:println("deployFile() in APICoreUtil");
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
    system:println("buildPayload() in APICoreUtil");
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
