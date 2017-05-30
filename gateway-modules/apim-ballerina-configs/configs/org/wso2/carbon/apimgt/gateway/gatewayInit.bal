package org.wso2.carbon.apimgt.gateway;

import ballerina.lang.messages;
import ballerina.lang.system;
import ballerina.net.http;
import ballerina.lang.errors;
import ballerina.lang.jsons;
import org.wso2.carbon.apimgt.gateway.constants as Constants;
import org.wso2.carbon.apimgt.gateway.utils as gatewayUtil;
import org.wso2.carbon.apimgt.gateway.dto as dto;
import org.wso2.carbon.apimgt.gateway.holders as holder;

service gatewayInitService {
    boolean isReady = initGateway();
    boolean isCacheInitialized = holder:initializeCache();
}

function initGateway () (boolean) {

    try {
        //Register gateway in APIM core
        registerGateway ();

        //Retrieve API list to be deployed
        json apiList = getAPIMSummary();
        int index = 0;
        int apiCount = jsons:getInt(apiList,"$.length()");

        while( index < apiCount){
            dto:APIDto api = gatewayUtil:fromJSONToAPIDto(apiList[index]);

            //Retrieve API configuration
            string apiConfig = gatewayUtil:getAPIServiceConfig(api.id);
            //Deploy API service
            gatewayUtil:deployService(api, apiConfig);
            //Update API cache
            holder:putIntoAPICache(api);
            index = index + 1;
        }

    } catch (errors:Error e) {
        system:println("[Error] : Error while initilazing API gateway ");
    }

    return true;

}

function registerGateway () {

    string apiCoreURL;
    json labelInfoPayload = {};
    message request = {};
    message response = {};
    json configDetails;
    try {
        apiCoreURL = getSystemProperty(Constants:API_CORE_URL);
        labelInfoPayload.labelInfo = buildPayload();
        http:ClientConnector client = create http:ClientConnector(apiCoreURL);
        messages:setJsonPayload(request, labelInfoPayload);
        response = http:ClientConnector.post (client, "/api/am/core/v1.0/gateways/register", request);
        configDetails = messages:getJsonPayload(response);
        dto:GatewayConf conf = gatewayUtil:fromJsonToGatewayConf(configDetails);
        holder:setGatewayConf(conf);

    } catch (errors:Error e) {
        system:println("[Error] : Error occurred while registration of gateway");
        throw e;
    }
}

function getAPIMSummary () (json) {

    string apiCoreURL;
    message request = {};
    message response = {};
    json apiList;
    try {
        apiCoreURL = getSystemProperty(Constants:API_CORE_URL);
        http:ClientConnector client = create http:ClientConnector(apiCoreURL);
        response = http:ClientConnector.get (client, "/api/am/core/v1.0/apis-summary", request);
        apiList = messages:getJsonPayload(response);
    } catch (errors:Error e) {
        system:println("[Error] : Error occurred while retrieving gateway APIs");
        throw e;
    }
    return apiList;
}

function getSystemProperty (string prop) (string) {
    string pathValue = system:getEnv(prop);
    return pathValue;
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


