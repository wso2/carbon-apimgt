package org.wso2.carbon.apimgt.gateway;

import ballerina.lang.system;
import ballerina.lang.errors;
import org.wso2.carbon.apimgt.gateway.utils as apiCoreUtil;

service gatewayInitService {
    boolean isReady = initGateway();
}

function initGateway () (boolean) {

    try {
        //Register gateway in API Core
        apiCoreUtil:registerGateway();
        //Retrieve APIs from API Core and deploy
        apiCoreUtil:loadAPIs();
    } catch (errors:Error e) {
        system:println("Error while initilazing API gateway. " + e.msg);
    }
    return true;
}

