package org.wso2.carbon.apimgt.gateway.dto;

struct IntrospectDto {
    boolean active;
    int exp;
    string username;
    string scope;
    string token_type;
    string client_id;
    int iat;
}