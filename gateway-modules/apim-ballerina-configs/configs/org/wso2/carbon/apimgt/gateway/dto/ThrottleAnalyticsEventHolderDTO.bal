package org.wso2.carbon.apimgt.gateway.dto;

struct ThrottleEventAnalyticsHolderDTO {
    string streamName = "ThrottleStream";
    string version = "1.0.0";
    int timestamp;
    ThrottleAnalyticsEventDTO throttleAnalyticsEventDTO;
}
