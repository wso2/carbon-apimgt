package org.wso2.carbon.apimgt.gateway.common.analytics.publishers.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;

/**
 * analytics event data
 */
public class Event {
    @JsonUnwrapped
    private API api;
    @JsonUnwrapped
    private Operation operation;
    @JsonUnwrapped
    private Target target;
    @JsonUnwrapped
    private Application application;
    @JsonUnwrapped
    private Latencies latencies;
    @JsonUnwrapped
    private MetaInfo metaInfo;
    @JsonUnwrapped
    private Error error;
    private int proxyResponseCode;
    private String requestTimestamp;
    private String userAgentHeader;

    private String errorType;

    public API getApi() {
        return api;
    }

    public void setApi(API api) {
        this.api = api;
    }

    public Operation getOperation() {
        return operation;
    }

    public void setOperation(Operation operation) {
        this.operation = operation;
    }

    public Target getTarget() {
        return target;
    }

    public void setTarget(Target target) {
        this.target = target;
    }

    public Application getApplication() {
        return application;
    }

    public void setApplication(Application application) {
        this.application = application;
    }

    public Latencies getLatencies() {
        return latencies;
    }

    public void setLatencies(Latencies latencies) {
        this.latencies = latencies;
    }

    public int getProxyResponseCode() {
        return proxyResponseCode;
    }

    public void setProxyResponseCode(int proxyResponseCode) {
        this.proxyResponseCode = proxyResponseCode;
    }

    public String getRequestTimestamp() {
        return requestTimestamp;
    }

    public void setRequestTimestamp(String requestTimestamp) {
        this.requestTimestamp = requestTimestamp;
    }

    public Error getError() {
        return error;
    }

    public void setError(Error error) {
        this.error = error;
    }

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public MetaInfo getMetaInfo() {
        return metaInfo;
    }

    public void setMetaInfo(MetaInfo metaInfo) {
        this.metaInfo = metaInfo;
    }

    public String getUserAgentHeader() {
        return userAgentHeader;
    }

    public void setUserAgentHeader(String userAgentHeader) {
        this.userAgentHeader = userAgentHeader;
    }
}
