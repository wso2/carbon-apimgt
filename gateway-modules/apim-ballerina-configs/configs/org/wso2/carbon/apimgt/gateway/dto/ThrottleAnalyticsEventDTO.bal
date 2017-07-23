package org.wso2.carbon.apimgt.gateway.dto;

struct ThrottleAnalyticsEventDTO {
    string api;
    string context;
    string version;
    string publisher;
    string subscriptionPolicy;
    string uriTemplate;
    string httpMethod;
    string consumerKey;
    string applicationName;
    string applicationId;
    string applicationOwner;
    string userId;
    string subscriber;
    int throttleCount;
    int throttleTime;
    string gatewayDomain;
    string gatewayIp;
    string throttledReason;
    string throttledPolicy;
    string hostName;
}
