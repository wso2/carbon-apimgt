package org.wso2.carbon.apimgt.api.model;

import java.util.Map;

public class ResourceEndpoint {
    public enum EndpointType {
        HTTP,
        ADDRESS
    };

    private String id;
    private String name;
    private EndpointType endpointType;
    private String url;
    private Map<String, String> securityConfig;
    private Map<String, String> generalConfig;
    private int usageCount;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public EndpointType getEndpointType() {
        return endpointType;
    }

    public String getUrl() {
        return url;
    }

    public Map<String, String> getSecurityConfig() {
        return securityConfig;
    }

    public Map<String, String> getGeneralConfig() {
        return generalConfig;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEndpointType(EndpointType endpointType) {
        this.endpointType = endpointType;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setSecurityConfig(Map<String, String> securityConfig) {
        this.securityConfig = securityConfig;
    }

    public void setGeneralConfig(Map<String, String> generalConfig) {
        this.generalConfig = generalConfig;
    }

    public void setUsageCount(int usageCount) {
        this.usageCount = usageCount;
    }

    public int getUsageCount() {
        return usageCount;
    }
}
