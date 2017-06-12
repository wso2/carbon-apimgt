package org.wso2.carbon.apimgt.gateway.dto;

struct EventHolderDTO {
    string streamName = "CompositeResponseStream";
    string version = "1.0.0";
    int timestamp;
    RequestEventDTO requestEventDTO;
    ResponseEventDTO responseEventDTO;
    LatencyEventDTO latencyEventDTO;
}
