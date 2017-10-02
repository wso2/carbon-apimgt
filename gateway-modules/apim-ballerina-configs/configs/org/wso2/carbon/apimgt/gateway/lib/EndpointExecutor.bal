package org.wso2.carbon.apimgt.gateway.lib;

import org.wso2.carbon.apimgt.gateway.dto;
import org.wso2.carbon.apimgt.gateway.holders;
import ballerina.net.http;
import ballerina.utils;
import ballerina.lang.messages;
import org.wso2.carbon.apimgt.gateway.constants;

function execute_endpoint (string endpointName, string httpVerb, string path, message m) (message) {
    dto:EndpointDto endpoint = holders:getFromEndpointCache(endpointName);
    message response;
    if (endpoint != null) {
        if (endpoint.securityEnable) {
            dto:Endpoint_Security endpointSecurity = endpoint.security;
            if (endpointSecurity.type == "basic") {
                messages:setHeader(m, constants:AUTHORIZATION, "Basic " + utils:base64encode(endpointSecurity.username + ":" + endpointSecurity.password));
            }
        }
        response = http:ClientConnector.execute(endpoint.clientConnector, httpVerb, path, m);
    }
    return response;
}