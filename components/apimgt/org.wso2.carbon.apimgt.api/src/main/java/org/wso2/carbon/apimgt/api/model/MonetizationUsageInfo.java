package org.wso2.carbon.apimgt.api.model;

import java.util.HashMap;

public class MonetizationUsageInfo {

    Long currentTimestamp;
    String apiUuid;
    String apiName;
    String apiVersion;
    String tenantDomain;
    String applicationName;
    String applicationOwner;
    HashMap<String, Object> customAttributes;
    Long requestCount;

    public MonetizationUsageInfo(Long currentTimestamp, String apiUuid, String apiName, String apiVersion, String tenantDomain, String applicationName, String applicationOwner, HashMap<String, Object> customAttributes, Long requestCount) {
        this.currentTimestamp = currentTimestamp;
        this.apiUuid = apiUuid;
        this.apiName = apiName;
        this.apiVersion = apiVersion;
        this.tenantDomain = tenantDomain;
        this.applicationName = applicationName;
        this.applicationOwner = applicationOwner;
        this.customAttributes = customAttributes;
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

    public String getApiName() {
        return apiName;
    }

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public String getApiVersion() {
        return apiVersion;
    }

    public void setApiVersion(String apiVersion) {
        this.apiVersion = apiVersion;
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

    public HashMap<String, Object> getCustomAttributes() {
        return customAttributes;
    }

    public void setCustomAttributes(HashMap<String, Object> customAttributes) {
        this.customAttributes = customAttributes;
    }

    public Long getRequestCount() {
        return requestCount;
    }

    public void setRequestCount(Long requestCount) {
        this.requestCount = requestCount;
    }
}
