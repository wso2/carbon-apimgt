package org.wso2.carbon.apimgt.gateway.microServices;

import ballerina.lang.system;
import ballerina.lang.errors;
import org.wso2.carbon.apimgt.gateway.holders as holder;
import ballerina.net.http;

service<http> gatewayInitService {

    boolean isCacheInitialized = holder:initializeCache();
    boolean isReady = initGateway();

    boolean subscriptionsInitialized = retrieveOfflineSubscriptions();
    boolean applicationsInitialized = retrieveOfflineApplications();
}

function initGateway () (boolean) {
    system:println("initGateway() in microGatewayInit.bal");
    try {
        loadOfflineAPIs();
        return true;
    } catch (errors:Error e) {
        system:println("Error while initilazing API gateway. " + e.msg);
        return false;
    }
    return true;
    //otherwise, missing return statement
}