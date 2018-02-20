package org.wso2.carbon.apimgt.gateway.services;

import ballerina.lang.system;
import ballerina.lang.errors;
import org.wso2.carbon.apimgt.gateway.utils as gatewayUtil;
import org.wso2.carbon.apimgt.gateway.holders as holder;
import org.wso2.carbon.apimgt.gateway.threatprotection;

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
        //Test to get the gateway Label. Can uncomment when required.
       // string label = gatewayUtil:getSystemProperty("label");
        gatewayUtil:registerGateway();
        //Retrieve APIs from API Core and deploy
        gatewayUtil:loadAPIs();
        gatewayUtil:loadGlobalEndpoints();
        gatewayUtil:loadBlockConditions();
        //set threat protection configurations
        threatprotection:initThreatProtection();

    } catch (errors:Error e) {
        system:println("Error while initilazing API gateway. " + e.msg);
    }
    return true;
}


