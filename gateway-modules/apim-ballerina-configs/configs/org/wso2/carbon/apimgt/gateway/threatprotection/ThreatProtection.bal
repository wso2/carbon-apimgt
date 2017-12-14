package org.wso2.carbon.apimgt.gateway.threatprotection;

import ballerina.lang.errors;
import ballerina.lang.messages;
import ballerina.lang.system;
import ballerina.net.http;

import org.wso2.carbon.apimgt.gateway.constants;
import org.wso2.carbon.apimgt.ballerina.threatprotection;

function requestInterceptor(message m) (boolean, message) {
    system:println("invoking threat protection interceptor");
    string apiContext = messages:getProperty(m, constants:BASE_PATH);
    //extract api when support arrives
    //use apiContext + ":" + apiVersion to obtain apiId from holder
    string policyId = "GLOBAL";
    return analyzePayload(m, apiContext, policyId);
}

function responseInterceptor (message m) (boolean, message) {
    system:println("invoking threat protection response interceptor");
    return true, m;
}

function analyzePayload(message m, string apiContext, string policyId) (boolean, message) {
    string contentType;
    try {
        contentType = messages:getHeader(m, "Content-Type");
    } catch (errors:Error e) {
        system:println("Threat Protection: No Content-Type declared for " + apiContext);
        return true, m;
    }
    string payload = messages:getStringPayload(m);
    boolean ok;
    string errMessage;
    ok, errMessage = threatprotection:analyze(contentType, payload, apiContext, policyId);

    if (ok) {
        return true, m;
    }

    system:println(errMessage);
    message response = {};
    http:setStatusCode(response, 400);
    messages:setStringPayload(response, "Malformed Payload");

    return false, response;
}