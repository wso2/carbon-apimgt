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
    boolean offlineSubsInitialized = gatewayUtil:retrieveOfflineSubscriptions();

    boolean applicationsInitialized = gatewayUtil:retrieveApplications();
    boolean offlineAppssInitialized = gatewayUtil:retrieveOfflineApplications();

    boolean policiesInitialized = gatewayUtil:retrievePolicies();

}

function initGateway () (boolean) {
    system:println("initGateway() in gatewayInit");
    try {
        //Register gateway in API Core
        gatewayUtil:registerGateway();
        //Retrieve APIs from API Core and deploy
        system:println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        gatewayUtil:loadAPIs();
        gatewayUtil:loadOfflineAPIs();
        system:println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        gatewayUtil:loadGlobalEndpoints();
        system:println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
        gatewayUtil:loadBlockConditions();
        system:println("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
    } catch (errors:Error e) {
        system:println("Error while initilazing API gateway. " + e.msg);
    }
    return true;
}

