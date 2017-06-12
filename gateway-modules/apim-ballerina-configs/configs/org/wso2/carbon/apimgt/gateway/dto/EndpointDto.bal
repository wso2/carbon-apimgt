package org.wso2.carbon.apimgt.gateway.dto;

import ballerina.net.http;

struct EndpointDto{
    string name;
    http:ClientConnector clientConnector;
    boolean securityEnable = false;
    Endpoint_Security security;
    string type;
}
struct Endpoint_Security{
    string type;
    string username;
    string password;
}