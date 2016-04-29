package org.wso2.carbon.apimgt.impl.dto;


import java.util.Map;

public class ThrottleDataDTO {
    String clientIP;
    String messageSizeInBytes;
    Map<String, String> transportHeaders;
    Map<String, String> queryParameters;

    public Map<String, String> getTransportHeaders() {
        return transportHeaders;
    }

    public void setTransportHeaders(Map<String, String> transportHeaders) {
        this.transportHeaders = transportHeaders;
    }

    public Map<String, String> getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(Map<String, String> queryParameters) {
        this.queryParameters = queryParameters;
    }

    public String getClientIP() {
        return clientIP;
    }

    public void setClientIP(String clientIP) {
        this.clientIP = clientIP;
    }

    public String getMessageSizeInBytes() {
        return messageSizeInBytes;
    }

    public void setMessageSizeInBytes(String messageSizeInBytes) {
        this.messageSizeInBytes = messageSizeInBytes;
    }

}
