package org.wso2.carbon.apimgt.gateway.dto;

struct ThrottleEventDTO {
    string messageID;
    string appKey;
    string applicationTier;
    string apiKey;
    string apiTier;
    string subscriptionKey;
    string subscriptionTier;
    string resourceLevelThrottleKey;
    string resourceTier;
    string userId;
    string apiContext;
    string apiVersion;
    string appTenant;
    string apiTenant;
    string applicationId;
    string apiName;
    string properties;
}
