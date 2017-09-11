package org.wso2.carbon.apimgt.gateway.services;

import ballerina.lang.system;
import ballerina.lang.errors;
import org.wso2.carbon.apimgt.gateway.micro as micro;
import org.wso2.carbon.apimgt.gateway.holders as holder;
import ballerina.net.http;

service<http> gatewayInitService {

    boolean isCacheInitialized = holder:initializeCache();
    boolean isMapsAdded = holder:addThrottleMaps();
    boolean isReady = initGateway();

    boolean offlineSubsInitialized = micro:retrieveOfflineSubscriptions();

}

function initGateway () (boolean) {
    system:println("initGateway() in gatewayInit");
    try {

        micro:loadOfflineAPIs();

    } catch (errors:Error e) {
        system:println("Error while initilazing API gateway. " + e.msg);
    }
    return true;
}

