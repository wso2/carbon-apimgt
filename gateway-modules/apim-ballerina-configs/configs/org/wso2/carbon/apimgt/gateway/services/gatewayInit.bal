package org.wso2.carbon.apimgt.gateway.services;

import ballerina.lang.system;
import ballerina.lang.errors;
import org.wso2.carbon.apimgt.gateway.utils as gatewayUtil;
import org.wso2.carbon.apimgt.gateway.holders as holder;
import ballerina.net.http;

service<http> gatewayInitService {
    boolean isCacheInitialized = holder:initializeCache();
    boolean isMapsAdded = holder:addThrottleMaps();
    boolean isReady = initGateway();
    boolean subscriptionsInitialized = gatewayUtil:retrieveSubscriptions();
    boolean applicationsInitialized = gatewayUtil:retrieveApplications();
    boolean policiesInitialized = gatewayUtil:retrievePolicies();

}

function initGateway () (boolean) {

    try {
        //Register gateway in API Core
        system:println("Registering gateway...");
        gatewayUtil:registerGateway();
        system:println("Gateway registered successfully.");
        //Retrieve APIs from API Core and deploy
        gatewayUtil:loadAPIs();
        system:println("Loaded APIs successfully.");
        gatewayUtil:loadGlobalEndpoints();
        system:println("Loaded global endpoints successfully.");
        gatewayUtil:loadBlockConditions();
        system:println("Loaded block conditions successfully.");
        //gatewayUtil:deployFile("a","b");
    } catch (errors:Error e) {
        system:println("Error while initilazing API gateway. " + e.msg);
    }
    return true;
}

