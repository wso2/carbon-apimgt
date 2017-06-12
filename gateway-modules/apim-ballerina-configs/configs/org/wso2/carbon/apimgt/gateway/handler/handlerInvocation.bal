package org.wso2.carbon.apimgt.gateway.handler;

import org.wso2.carbon.apimgt.gateway.event.analytics.response;
import org.wso2.carbon.apimgt.gateway.event.throttling;
import org.wso2.carbon.apimgt.gateway.event.util;
import org.wso2.carbon.apimgt.gateway.handler;
import org.wso2.carbon.apimgt.gateway;

function mediateIn (message m) {
    boolean status = false;
    status, m = gateway:authenticate(m);
    throttling:mediate(m);
}

function mediateOut (message m, message res) {
    //skip analytics event publishing
    //response:mediate(m, res);
}