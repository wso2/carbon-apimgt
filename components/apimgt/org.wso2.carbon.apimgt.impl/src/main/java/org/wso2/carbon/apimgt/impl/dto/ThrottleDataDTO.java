package org.wso2.carbon.apimgt.impl.dto;


import java.util.Map;

/**
 * This class is used to hold throttling data before publish them to
 * global policy engine side. We decided to maintain this in impl as
 * this can be used by other components such as usage publisher.
 * In future we may consider adding all properties to this class.
 */
public class ThrottleDataDTO {
    String clientIP;
    int messageSizeInBytes;
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

    public int getMessageSizeInBytes() {
        return messageSizeInBytes;
    }

    public void setMessageSizeInBytes(int messageSizeInBytes) {
        this.messageSizeInBytes = messageSizeInBytes;
    }

    /**
     * Cleaning DTO by setting null reference for all it instance variables.
     */
    public void cleanDTO(){
        this.clientIP = null;
        this.messageSizeInBytes = 0;
        this.transportHeaders =null;
        this.queryParameters =null;
    }

}
