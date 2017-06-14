package org.wso2.carbon.apimgt.gateway.dto;

struct ThrottleEventHolderDTO {
    string streamName;
    string executionPlanName;
    int timestamp;
    ThrottleEventDTO throttleEventDTO;
}
