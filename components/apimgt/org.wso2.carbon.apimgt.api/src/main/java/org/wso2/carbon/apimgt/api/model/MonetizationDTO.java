package org.wso2.carbon.apimgt.api.model;

import java.util.HashMap;

public class MonetizationDTO {

    Long currentTimestamp;
    String apiUuid;
    String tenantDomain;
    String applicationName;
    String applicationOwner;
    HashMap<String, Object> properties;
    Long requestCount;

    public MonetizationDTO(Long currentTimestamp, String apiUuid, String tenantDomain, String applicationName, String applicationOwner, HashMap<String, Object> properties, Long requestCount) {
        this.currentTimestamp = currentTimestamp;
        this.apiUuid = apiUuid;
        this.tenantDomain = tenantDomain;
        this.applicationName = applicationName;
        this.applicationOwner = applicationOwner;
        this.properties = properties;
        this.requestCount = requestCount;
    }

    public Long getCurrentTimestamp() {
        return currentTimestamp;
    }

    public void setCurrentTimestamp(Long currentTimestamp) {
        this.currentTimestamp = currentTimestamp;
    }

    public String getApiUuid() {
        return apiUuid;
    }

    public void setApiUuid(String apiUuid) {
        this.apiUuid = apiUuid;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getApplicationOwner() {
        return applicationOwner;
    }

    public void setApplicationOwner(String applicationOwner) {
        this.applicationOwner = applicationOwner;
    }

    public HashMap<String, Object> getProperties() {
        return properties;
    }

    public void setProperties(HashMap<String, Object> properties) {
        this.properties = properties;
    }

    public Long getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(Long requestCount) {
        this.requestCount = requestCount;
    }
}
