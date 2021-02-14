package org.wso2.carbon.apimgt.gateway.common.analytics.publishers.dto;

/**
 * Latency attribute in analytics event
 */
public class Latencies {
    private long responseLatency;
    private long backendLatency;
    private long requestMediationLatency;
    private long responseMediationLatency;

    public long getResponseLatency() {
        return responseLatency;
    }

    public void setResponseLatency(long responseLatency) {
        this.responseLatency = responseLatency;
    }

    public long getBackendLatency() {
        return backendLatency;
    }

    public void setBackendLatency(long backendLatency) {
        this.backendLatency = backendLatency;
    }

    public long getRequestMediationLatency() {
        return requestMediationLatency;
    }

    public void setRequestMediationLatency(long requestMediationLatency) {
        this.requestMediationLatency = requestMediationLatency;
    }

    public long getResponseMediationLatency() {
        return responseMediationLatency;
    }

    public void setResponseMediationLatency(long responseMediationLatency) {
        this.responseMediationLatency = responseMediationLatency;
    }
}
