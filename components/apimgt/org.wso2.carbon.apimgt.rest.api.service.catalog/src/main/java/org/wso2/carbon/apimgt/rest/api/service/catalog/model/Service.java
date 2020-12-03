package org.wso2.carbon.apimgt.rest.api.service.catalog.model;

import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ServiceDTO;

public class Service {
    private String id;
    private String name;
    private String displayName;
    private String description;
    private String version;
    private String serviceUrl;
    private ServiceDTO.DefinitionTypeEnum definitionType;
    private ServiceDTO.SecurityTypeEnum securityType;
    private Boolean mutualSSLEnabled;
    private Integer usage;
    private String createdTime;
    private String lastUpdatedTime;
    private String etag;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getServiceUrl() {
        return serviceUrl;
    }

    public void setServiceUrl(String serviceUrl) {
        this.serviceUrl = serviceUrl;
    }

    public ServiceDTO.DefinitionTypeEnum getDefinitionType() {
        return definitionType;
    }

    public void setDefinitionType(ServiceDTO.DefinitionTypeEnum definitionType) {
        this.definitionType = definitionType;
    }

    public ServiceDTO.SecurityTypeEnum getSecurityType() {
        return securityType;
    }

    public void setSecurityType(ServiceDTO.SecurityTypeEnum securityType) {
        this.securityType = securityType;
    }

    public Boolean getMutualSSLEnabled() {
        return mutualSSLEnabled;
    }

    public void setMutualSSLEnabled(Boolean mutualSSLEnabled) {
        this.mutualSSLEnabled = mutualSSLEnabled;
    }

    public Integer getUsage() {
        return usage;
    }

    public void setUsage(Integer usage) {
        this.usage = usage;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(String lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public String getEtag() {
        return etag;
    }

    public void setEtag(String etag) {
        this.etag = etag;
    }
}
