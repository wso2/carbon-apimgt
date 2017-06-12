package org.wso2.carbon.apimgt.gateway.dto;

struct EventHolderDTO {
    string streamName;
    string executionPlanName;
    int timestamp;
    RequestEventDTO requestEventDTO;
    ResponseEventDTO responseEventDTO;
    LatencyEventDTO latencyEventDTO;
}
