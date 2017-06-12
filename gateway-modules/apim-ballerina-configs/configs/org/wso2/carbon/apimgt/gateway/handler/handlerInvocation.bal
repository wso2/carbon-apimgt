package org.wso2.carbon.apimgt.gateway.handler;


function mediateIn (message m)(boolean ,message ) {
    boolean status = false;
    return status,m;
    //todo: have to do throttling
}

function mediateOut (message m, message res) {
    //skip analytics event publishing
    //response:mediate(m, res);
}