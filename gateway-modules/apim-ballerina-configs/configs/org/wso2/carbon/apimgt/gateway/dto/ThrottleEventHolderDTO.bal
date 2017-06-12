package org.wso2.carbon.apimgt.gateway.dto;

struct ThrottleEventHolderDTO {
    string streamName = "PreRequestStream";
    string version = "1.0.0";
    int timestamp;
    ThrottleEventDTO throttleEventDTO;
}
