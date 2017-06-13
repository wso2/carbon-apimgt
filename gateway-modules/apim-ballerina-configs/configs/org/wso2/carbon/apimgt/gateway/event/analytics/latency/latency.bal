package org.wso2.carbon.apimgt.gateway.event.analytics.latency;

import ballerina.lang.messages;
import ballerina.lang.system;
import org.wso2.carbon.apimgt.gateway.event.util;

function mediateIn (message m) {
    util:simulate(m);
    messages:setProperty(m, "am.request_start_time", system:currentTimeMillis());
}

function mediateOut (message m) {
    messages:setProperty(m, "am.request_end_time", system:currentTimeMillis());
}

function mediateEndpointRequest (message m) {
    messages:setProperty(m, "am.backend_start_time", system:currentTimeMillis());
}

function mediateEndpointResponse (message m) {
    messages:setProperty(m, "am.backend_end_time", system:currentTimeMillis());
}