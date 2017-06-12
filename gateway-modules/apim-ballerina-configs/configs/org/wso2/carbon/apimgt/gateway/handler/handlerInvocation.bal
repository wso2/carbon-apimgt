package org.wso2.carbon.apimgt.gateway.handler;

import org.wso2.carbon.apimgt.gateway.event.analytics.response;
import ballerina.lang.messages;
import ballerina.lang.system;
import org.wso2.carbon.apimgt.gateway.event.util;

function mediateIn (message m)(boolean ,message ) {
    util:simulate(m);
    messages:setProperty(m, "am.request_start_time", system:currentTimeMillis());
    boolean status = false;
    return status,m;
    //todo: have to do throttling
}

function mediateOut (message m, message res) {
    messages:setProperty(m, "am.request_end_time", system:currentTimeMillis());
    response:mediate(m, res);
}

function mediateEndpointRequest (message m) {
    messages:setProperty(m, "am.backend_start_time", system:currentTimeMillis());
}

function mediateEndpointResponse (message m) {
    messages:setProperty(m, "am.backend_end_time", system:currentTimeMillis());
}