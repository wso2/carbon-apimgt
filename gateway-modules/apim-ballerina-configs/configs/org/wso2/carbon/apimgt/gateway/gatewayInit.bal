package org.wso2.carbon.apimgt.gateway;

import ballerina.lang.system;
import ballerina.lang.errors;
import org.wso2.carbon.apimgt.gateway.utils as gatewayUtil;
import org.wso2.carbon.apimgt.gateway.holders as holder;


service gatewayInitService {
    boolean isCacheInitialized = holder:initializeCache();
    boolean isReady = initGateway();
    boolean subscriptionsInitialized = gatewayUtil:retrieveSubscriptions();
    boolean applicationsInitialized = gatewayUtil:retrieveApplications();
}

function initGateway () (boolean) {

    try {
        //Register gateway in API Core
        gatewayUtil:registerGateway();
        //Retrieve APIs from API Core and deploy
        gatewayUtil:loadAPIs();
    } catch (errors:Error e) {
        system:println("Error while initilazing API gateway. " + e.msg);
    }
    return true;
}

