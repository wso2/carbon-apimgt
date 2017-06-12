package org.wso2.carbon.apimgt.gateway.dto;
struct KeyValidationDto {
    string username;
    string applicationPolicy;
    string subscriptionPolicy;
    string apiLevelPolicy;
    string resourceLevelPolicy;
    string verb;
    string apiName;
    string apiProvider;
    string apiContext;
    string apiVersion;
    string applicationId;
    string applicationName;
    string keyType;
    string subscriber;
    string resourcePath;
    boolean stopOnQuotaReach;
}