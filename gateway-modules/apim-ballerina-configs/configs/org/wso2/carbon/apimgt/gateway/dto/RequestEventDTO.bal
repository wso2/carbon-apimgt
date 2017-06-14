package org.wso2.carbon.apimgt.gateway.dto;

struct RequestEventDTO {
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
    int requestCount;
    int requestTime;
    string gatewayDomain;
    string gatewayIp;
    boolean isThrottled;
    string throttledReason;
    string throttledPolicy;
    string clientIp;
    string userAgent;
    string hostName;
}
