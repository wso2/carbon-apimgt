package org.wso2.carbon.apimgt.gateway.auth;

import ballerina.lang.system;

function main(string[] args) {
    system:println("Hello, World!");
}

function requestInterceptor (message m) (boolean, message) {
    system:println("invoking auth interceptor");
    boolean authenticated = true;
    return authenticated, m;
}

function responseInterceptor (message m) (boolean, message) {
    system:println("invoking response auth interceptor");
    return true, m;
}