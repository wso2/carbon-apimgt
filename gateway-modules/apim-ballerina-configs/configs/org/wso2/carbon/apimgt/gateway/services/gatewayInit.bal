package org.wso2.carbon.apimgt.gateway.services;

import ballerina.lang.system;
import ballerina.lang.errors;
import org.wso2.carbon.apimgt.gateway.micro as micro;
import org.wso2.carbon.apimgt.gateway.holders as holder;
import ballerina.net.http;

service<http> gatewayInitService {

    boolean isCacheInitialized = holder:initializeCache();
    boolean isReady = initGateway();

}

function initGateway () (boolean) {
    system:println("initGateway() in gatewayInit");
    try {
        micro:loadAPIKeys();
    } catch (errors:Error e) {
        system:println("Error while initilazing API gateway. " + e.msg);
    }
    return true;
}

