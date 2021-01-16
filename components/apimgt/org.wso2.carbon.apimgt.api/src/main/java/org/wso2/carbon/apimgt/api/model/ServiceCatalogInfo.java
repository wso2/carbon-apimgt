package org.wso2.carbon.apimgt.api.model;

import java.sql.Timestamp;

public class ServiceCatalogInfo {

    private String uuid;
    private String key;
    private String md5;
    private String name;
    private String version;
    private String displayName;
    private String serviceUrl;
    private String defType;
    private String defUrl;
    private String description;
    private String securityType;
    private boolean isMutualSSLEnabled;
    private String createdBy;
    private String updatedBy;
    private Timestamp createdTime;
    private Timestamp lastUpdatedTime;

    public Timestamp getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(Timestamp lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public boolean isMutualSSLEnabled() {
        return isMutualSSLEnabled;
    }

    public void setMutualSSLEnabled(boolean mutualSSLEnabled) {
        isMutualSSLEnabled = mutualSSLEnabled;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public String getDefType() {
        return defType;
    }

    public void setDefType(String defType) {
        this.defType = defType;
    }

    public String getDefUrl() {
        return defUrl;
    }

    public void setDefUrl(String defUrl) {
        this.defUrl = defUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSecurityType() {
        return securityType;
    }

    public void setSecurityType(String securityType) {
        this.securityType = securityType;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public Timestamp getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Timestamp createdTime) {
        this.createdTime = createdTime;
    }
}
