package org.wso2.carbon.apimgt.gateway.dto;

struct ResponseEventDTO {
    int responseCount;
    int responseTime;
    boolean cacheHit;
    int contentLength;
    string protocol;
    int statusCode;
    string destination;
}
