package org.wso2.carbon.apimgt.gateway.handler;

import org.wso2.carbon.apimgt.gateway.event.analytics.response;
import ballerina.lang.messages;
import ballerina.lang.system;
import org.wso2.carbon.apimgt.gateway.event.util;

function main(string[] args) {
    system:println("main() in handlerInvocation");
    system:println("Hello, World!");
}

function requestInterceptor (message m) (boolean, message) {
    system:println("requestInterceptor() in handlerInvocation");
    system:println("invoking analytics request interceptor");

    util:simulate(m);
    messages:setProperty(m, "am.request_start_time", system:currentTimeMillis()+"");

    return true, m;
}

function responseInterceptor (message m) (boolean, message) {
    system:println("responseInterceptor() in handlerInvocation");
    system:println("invoking analytics respose interceptor");
    messages:setProperty(m, "am.request_end_time", system:currentTimeMillis()+"");
    response:mediate(m, m);
    return true, m;
}

function mediateEndpointRequest (message m) {
    system:println("mediateEndpointRequest() in handlerInvocation");
    messages:setProperty(m, "am.backend_start_time", system:currentTimeMillis()+"");
}

function mediateEndpointResponse (message m) {
    system:println("mediateEndpointResponse() in handlerInvocation");
    messages:setProperty(m, "am.backend_end_time", system:currentTimeMillis()+"");
}