package org.wso2.carbon.apimgt.gateway.dto;

import ballerina.net.http;

struct EndpointDto{
    string name;
    http:ClientConnector clientConnector;
    boolean securityEnable = false;
    Endpoint_Security security;
    string protocol;
}
struct Endpoint_Security{
    string securityType;
    string username;
    string password;
}