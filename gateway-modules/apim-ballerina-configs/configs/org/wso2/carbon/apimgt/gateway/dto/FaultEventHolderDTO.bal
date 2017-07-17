package org.wso2.carbon.apimgt.gateway.dto;

struct FaultEventHolderDTO {
    string streamName = "CompositeFaultStream";
    string version = "1.0.0";
    int timestamp;
    RequestEventDTO requestEventDTO;
    FaultEventDTO faultEventDTO;
}
