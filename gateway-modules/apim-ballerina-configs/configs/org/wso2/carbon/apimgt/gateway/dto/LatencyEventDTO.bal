package org.wso2.carbon.apimgt.gateway.dto;

struct LatencyEventDTO {
    int responseTime;
    int serviceTime;
    int backendTime;
    int backendLatency;
    int securityLatency;
    int throttlingLatency;
    int request_mediationLatency;
    int response_mediationLatency;
    int otherLatency;
}
