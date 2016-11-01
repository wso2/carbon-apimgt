/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.models;



/**
 * Representation of an API object
 */

public final class API {
    public enum VisibilityEnum {
        PUBLIC,  PRIVATE,  RESTRICTED,  CONTROLLED,
    };

    private String provider;
    private String version;
    private String description;
    private String name;
    private String context;
    private String id;
    private String status;
    private String apiDefinition;
    private String wsdlUri;
    private boolean isResponseCachingEnabled;
    private int cacheTimeout;
    private boolean isDestinationStatsEnabled;
    private boolean isDefaultVersion;
    private List<String> transport = new ArrayList<>();
    private List<String> tags = new ArrayList<>();
    private List<String> policies = new ArrayList<>();
    private VisibilityEnum visibility = VisibilityEnum.PUBLIC;
    private List<String> visibleRoles = new ArrayList<>();
    private List<Endpoint> endpoints = new ArrayList<>();
    private List<Environment> gatewayEnvironments;

    private String businessOwnerEmail;
    private String technicalOwnerEmail;
    private String technicalOwner;
    private String businessOwner;
    private Date createdTime;
    private String createdBy;
    private Date lastUpdatedTime;

    private CorsConfiguration corsConfiguration;

    public API(String provider, String version, String name) {
        this.provider = provider;
        this.version = version;
        this.name = name;
    }

    public Date getCreatedTime() {
        return new Date(createdTime.getTime());
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = new Date(createdTime.getTime());
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public Date getLastUpdatedTime() {
        return new Date(lastUpdatedTime.getTime());
    }

    public void setLastUpdatedTime(Date lastUpdatedTime) {
        this.lastUpdatedTime = new Date(lastUpdatedTime.getTime());
    }

    public VisibilityEnum getVisibility() {
        return visibility;
    }

    public void setVisibility(VisibilityEnum visibility) {
        this.visibility = visibility;
    }

    public List<String> getVisibleRoles() {
        return visibleRoles;
    }

    public void setVisibleRoles(List<String> visibleRoles) {
        this.visibleRoles = visibleRoles;
    }

    public List<Endpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<Endpoint> endpoints) {
        this.endpoints = endpoints;
    }

    public List<Environment> getGatewayEnvironments() {
        return gatewayEnvironments;
    }

    public void setGatewayEnvironments(List<Environment> gatewayEnvironments) {
        this.gatewayEnvironments = gatewayEnvironments;
    }

    public String getBusinessOwnerEmail() {
        return businessOwnerEmail;
    }

    public void setBusinessOwnerEmail(String businessOwnerEmail) {
        this.businessOwnerEmail = businessOwnerEmail;
    }

    public String getTechnicalOwnerEmail() {
        return technicalOwnerEmail;
    }

    public void setTechnicalOwnerEmail(String technicalOwnerEmail) {
        this.technicalOwnerEmail = technicalOwnerEmail;
    }

    public String getTechnicalOwner() {
        return technicalOwner;
    }

    public void setTechnicalOwner(String technicalOwner) {
        this.technicalOwner = technicalOwner;
    }

    public String getBusinessOwner() {
        return businessOwner;
    }

    public void setBusinessOwner(String businessOwner) {
        this.businessOwner = businessOwner;
    }

    public CorsConfiguration getCorsConfiguration() {
        return corsConfiguration;
    }

    public void setCorsConfiguration(CorsConfiguration corsConfiguration) {
        this.corsConfiguration = corsConfiguration;
    }

    public List<String> getTransport() {
        return transport;
    }

    public void setTransport(List<String> transport) {
        this.transport = transport;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public List<String> getPolicies() {
        return policies;
    }

    public void setPolicies(List<String> policies) {
        this.policies = policies;
    }

    public String getApiDefinition() {
        return apiDefinition;
    }

    public void setApiDefinition(String apiDefinition) {
        this.apiDefinition = apiDefinition;
    }

    public String getWsdlUri() {
        return wsdlUri;
    }

    public void setWsdlUri(String wsdlUri) {
        this.wsdlUri = wsdlUri;
    }

    public boolean isResponseCachingEnabled() {
        return isResponseCachingEnabled;
    }

    public void setResponseCachingEnabled(boolean responseCachingEnabled) {
        isResponseCachingEnabled = responseCachingEnabled;
    }

    public int getCacheTimeout() {
        return cacheTimeout;
    }

    public void setCacheTimeout(int cacheTimeout) {
        this.cacheTimeout = cacheTimeout;
    }

    public boolean isDestinationStatsEnabled() {
        return isDestinationStatsEnabled;
    }

    public void setDestinationStatsEnabled(boolean destinationStatsEnabled) {
        isDestinationStatsEnabled = destinationStatsEnabled;
    }

    public boolean isDefaultVersion() {
        return isDefaultVersion;
    }

    public void setDefaultVersion(boolean defaultVersion) {
        isDefaultVersion = defaultVersion;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        API that = (API) o;
        return (name.equals(that.name) && provider.equals(that.provider) && version.equals(that.version));
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + provider.hashCode();
        result = 31 * result + version.hashCode();
        return result;
    }
    
}
