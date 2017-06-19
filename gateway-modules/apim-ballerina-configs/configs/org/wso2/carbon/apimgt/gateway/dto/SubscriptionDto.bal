package org.wso2.carbon.apimgt.gateway.dto;

struct SubscriptionDto {
    string apiName;
    string apiContext;
    string apiVersion;
    string apiProvider;
    string consumerKey;
    string subscriptionPolicy;
    string applicationName;
    string applicationOwner;
    string keyEnvType;
    string applicationId;
    string applicationTier;
    string status;
    string apiLevelPolicy = "Unlimited";
}