package org.wso2.carbon.apimgt.api.model;

import java.util.Map;
import java.util.Objects;

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
    private int apiId;

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

    public int getApiId() {
        return apiId;
    }

    public void setApiId(int apiId) {
        this.apiId = apiId;
    }

    @Override public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        ResourceEndpoint that = (ResourceEndpoint) o;
        return usageCount == that.usageCount && id.equals(that.id) && name.equals(that.name)
                && endpointType == that.endpointType && url.equals(that.url) && Objects
                .equals(securityConfig, that.securityConfig) && Objects.equals(generalConfig, that.generalConfig);
    }

    @Override public int hashCode() {
        return Objects.hash(id, name, endpointType, url, securityConfig, generalConfig, usageCount);
    }
}
