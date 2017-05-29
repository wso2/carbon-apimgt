package org.wso2.carbon.apimgt.gateway.handler;

import ballerina.lang.system;
import org.wso2.carbon.apimgt.gateway.event.analytics.response;
import org.wso2.carbon.apimgt.gateway.event.throttling;
import org.wso2.carbon.apimgt.gateway.event.util;

function mediateIn (message m) {
    system:println("Hello, World!");

    //simulating data
    util:simulate(m);

    throttling:mediate(m);
}

function mediateOut (message m, message res) {
    response:mediate(m, res);
}