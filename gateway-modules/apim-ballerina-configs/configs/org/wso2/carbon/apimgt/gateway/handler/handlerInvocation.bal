package org.wso2.carbon.apimgt.gateway.handler;

import org.wso2.carbon.apimgt.gateway.event.analytics.response;
import org.wso2.carbon.apimgt.gateway.event.throttling;
import org.wso2.carbon.apimgt.gateway.event.util;

function mediateIn (message m) {
    //simulating data
    util:simulate(m);

    //deprecating old throttle event publishing
    // invoke throttle handler logic
    //throttling:mediate(m);
}

function mediateOut (message m, message res) {
    //skip analytics event publishing
    //response:mediate(m, res);
}