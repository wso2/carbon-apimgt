/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Representation of a Composite API object. Only immutable instances of this class can be created via the provided
 * inner static {@code APIBuilder} class which implements the builder pattern as outlined in "Effective Java 2nd Edition
 * by Joshua Bloch(Item 2)"
 */

public class CompositeAPI {

    private final String id;
    private final String provider;
    private final String name;
    private final String version;
    private final String context;
    private final String description;
    private final String gatewayConfig;
    private final Set<String> transport;
    private final List<String> labels;
    private final String applicationId;
    private final Instant createdTime;
    private final String createdBy;
    private final String updatedBy;
    private final Instant lastUpdatedTime;
    private final Map<String, UriTemplate> uriTemplates;
    private final String copiedFromApiId;
    private final String apiDefinition;
    private final HashMap permissionMap;
    private final boolean hasOwnGateway;
    private final String apiPermission;
    private final String workflowStatus;
    private final Set<String> threatProtectionPolicies;

    private CompositeAPI(Builder builder) {
        id = builder.id;
        provider = builder.provider;
        name = builder.name;
        version = builder.version;
        context = builder.context;
        description = builder.description;
        gatewayConfig = builder.gatewayConfig;
        transport = builder.transport;
        labels = builder.labels;
        applicationId = builder.applicationId;
        createdTime = builder.createdTime;
        createdBy = builder.createdBy;
        updatedBy = builder.updatedBy;
        lastUpdatedTime = builder.lastUpdatedTime;
        uriTemplates = builder.uriTemplates;
        copiedFromApiId = builder.copiedFromApiId;
        apiDefinition = builder.apiDefinition;
        permissionMap = builder.permissionMap;
        hasOwnGateway = builder.hasOwnGateway;
        apiPermission = builder.apiPermission;
        workflowStatus = builder.workflowStatus;
        threatProtectionPolicies = builder.threatProtectionPolicies;
    }


    public String getId() {
        return id;
    }

    public String getProvider() {
        return provider;
    }

    public String getName() {
        return name;
    }

    public String getVersion() {
        return version;
    }

    public String getContext() {
        return context;
    }

    public String getDescription() {
        return description;
    }

    public String getGatewayConfig() {
        return gatewayConfig;
    }

    public Set<String> getTransport() {
        return transport;
    }

    public List<String> getLabels() {
        return labels;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public Instant getCreatedTime() {
        return createdTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public Instant getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public Map<String, UriTemplate> getUriTemplates() {
        return uriTemplates;
    }

    public String getCopiedFromApiId() {
        return copiedFromApiId;
    }

    public String getApiDefinition() {
        return apiDefinition;
    }

    public HashMap getPermissionMap() {
        return permissionMap;
    }

    public String getApiPermission() {
        return apiPermission;
    }

    public String getWorkflowStatus() {
        return workflowStatus;
    }

    public Set<String> getThreatProtectionPolicies() {
        return threatProtectionPolicies;
    }

    public boolean hasOwnGateway() {
        return hasOwnGateway;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CompositeAPI that = (CompositeAPI) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(provider, that.provider) &&
                Objects.equals(name, that.name) &&
                Objects.equals(version, that.version) &&
                Objects.equals(context, that.context) &&
                Objects.equals(description, that.description) &&
                Objects.equals(gatewayConfig, that.gatewayConfig) &&
                Objects.equals(transport, that.transport) &&
                Objects.equals(labels, that.labels) &&
                Objects.equals(applicationId, that.applicationId) &&
                Objects.equals(createdTime, that.createdTime) &&
                Objects.equals(createdBy, that.createdBy) &&
                Objects.equals(updatedBy, that.updatedBy) &&
                Objects.equals(lastUpdatedTime, that.lastUpdatedTime) &&
                Objects.equals(uriTemplates, that.uriTemplates) &&
                Objects.equals(copiedFromApiId, that.copiedFromApiId) &&
                Objects.equals(apiDefinition, that.apiDefinition) &&
                Objects.equals(permissionMap, that.permissionMap) &&
                Objects.equals(apiPermission, that.apiPermission) &&
                Objects.equals(workflowStatus, that.workflowStatus) &&
                Objects.equals(threatProtectionPolicies, that.threatProtectionPolicies) &&
                Objects.equals(hasOwnGateway, that.hasOwnGateway);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, provider, name, version, context, description, gatewayConfig, transport, labels,
                applicationId, createdTime, createdBy, updatedBy, lastUpdatedTime, uriTemplates, copiedFromApiId,
                apiDefinition, permissionMap, apiPermission, workflowStatus, threatProtectionPolicies, hasOwnGateway);
    }

    /**
     * {@code CompositeAPI} builder static inner class.
     */
    public static final class Builder {
        private String id;
        private String provider;
        private String name;
        private String version;
        private String context;
        private String description;
        private String gatewayConfig;
        private Set<String> transport;
        private List<String> labels;
        private String applicationId;
        private Instant createdTime;
        private String createdBy;
        private String updatedBy;
        private Instant lastUpdatedTime;
        private Map<String, UriTemplate> uriTemplates;
        private String copiedFromApiId;
        private String apiDefinition;
        private HashMap permissionMap;
        private boolean hasOwnGateway;
        private String apiPermission;
        private String workflowStatus;
        private Set<String> threatProtectionPolicies;

        public Builder() {
        }

        public Builder(CompositeAPI copy) {
            this.id = copy.id;
            this.provider = copy.provider;
            this.name = copy.name;
            this.version = copy.version;
            this.context = copy.context;
            this.description = copy.description;
            this.gatewayConfig = copy.gatewayConfig;
            this.transport = copy.transport;
            this.labels = copy.labels;
            this.hasOwnGateway = copy.hasOwnGateway;
            this.applicationId = copy.applicationId;
            this.createdTime = copy.createdTime;
            this.createdBy = copy.createdBy;
            this.updatedBy = copy.updatedBy;
            this.lastUpdatedTime = copy.lastUpdatedTime;
            this.uriTemplates = copy.uriTemplates;
            this.copiedFromApiId = copy.copiedFromApiId;
            this.apiDefinition = copy.apiDefinition;
            this.permissionMap = copy.permissionMap;
            this.apiPermission = copy.apiPermission;
            this.workflowStatus = copy.workflowStatus;
            this.threatProtectionPolicies = copy.threatProtectionPolicies;
        }

        /**
         * Sets the {@code id} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param id the {@code id} to set
         * @return a reference to this Builder
         */
        public Builder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * Sets the {@code provider} and returns a reference to this Builder so that the methods can be chained
         * together.
         *
         * @param provider the {@code provider} to set
         * @return a reference to this Builder
         */
        public Builder provider(String provider) {
            this.provider = provider;
            return this;
        }

        /**
         * Sets the {@code name} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param name the {@code name} to set
         * @return a reference to this Builder
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the {@code version} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param version the {@code version} to set
         * @return a reference to this Builder
         */
        public Builder version(String version) {
            this.version = version;
            return this;
        }

        /**
         * Sets the {@code context} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param context the {@code context} to set
         * @return a reference to this Builder
         */
        public Builder context(String context) {
            this.context = context;
            return this;
        }

        /**
         * Sets the {@code description} and returns a reference to this Builder so that the methods can be
         * chained together.
         *
         * @param description the {@code description} to set
         * @return a reference to this Builder
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Sets the {@code gatewayConfig} and returns a reference to this Builder so that the methods can be
         * chained together.
         *
         * @param gatewayConfig the {@code gatewayConfig} to set
         * @return a reference to this Builder
         */
        public Builder gatewayConfig(String gatewayConfig) {
            this.gatewayConfig = gatewayConfig;
            return this;
        }

        /**
         * Sets the {@code transport} and returns a reference to this Builder so that the methods can be
         * chained together.
         *
         * @param transport the {@code transport} to set
         * @return a reference to this Builder
         */
        public Builder transport(Set<String> transport) {
            this.transport = transport;
            return this;
        }

        /**
         * Sets the {@code labels} and returns a reference to this Builder so that the methods can be chained together.
         *
         * @param labels the {@code labels} to set
         * @return a reference to this Builder
         */
        public Builder labels(List<String> labels) {
            this.labels = labels;
            return this;
        }

        /**
         * Sets the {@code applicationId} and returns a reference to this Builder so that the methods can be
         * chained together.
         *
         * @param applicationId the {@code applicationId} to set
         * @return a reference to this Builder
         */
        public Builder applicationId(String applicationId) {
            this.applicationId = applicationId;
            return this;
        }

        /**
         * Sets the {@code createdTime} and returns a reference to this Builder so that the methods can be
         * chained together.
         *
         * @param createdTime the {@code createdTime} to set
         * @return a reference to this Builder
         */
        public Builder createdTime(Instant createdTime) {
            this.createdTime = createdTime;
            return this;
        }

        /**
         * Sets the {@code createdBy} and returns a reference to this Builder so that the methods can be
         * chained together.
         *
         * @param createdBy the {@code createdBy} to set
         * @return a reference to this Builder
         */
        public Builder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        /**
         * Sets the {@code updatedBy} and returns a reference to this Builder so that the methods can be
         * chained together.
         *
         * @param updatedBy the {@code updatedBy} to set
         * @return a reference to this Builder
         */
        public Builder updatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        /**
         * Sets the {@code lastUpdatedTime} and returns a reference to this Builder so that the methods can be
         * chained together.
         *
         * @param lastUpdatedTime the {@code lastUpdatedTime} to set
         * @return a reference to this Builder
         */
        public Builder lastUpdatedTime(Instant lastUpdatedTime) {
            this.lastUpdatedTime = lastUpdatedTime;
            return this;
        }

        /**
         * Sets the {@code uriTemplates} and returns a reference to this Builder so that the methods can be
         * chained together.
         *
         * @param uriTemplates the {@code uriTemplates} to set
         * @return a reference to this Builder
         */
        public Builder uriTemplates(Map<String, UriTemplate> uriTemplates) {
            this.uriTemplates = uriTemplates;
            return this;
        }

        /**
         * Sets the {@code copiedFromApiId} and returns a reference to this Builder so that the methods can be
         * chained together.
         *
         * @param copiedFromApiId the {@code copiedFromApiId} to set
         * @return a reference to this Builder
         */
        public Builder copiedFromApiId(String copiedFromApiId) {
            this.copiedFromApiId = copiedFromApiId;
            return this;
        }

        /**
         * Sets the {@code apiDefinition} and returns a reference to this Builder so that the methods can be
         * chained together.
         *
         * @param apiDefinition the {@code apiDefinition} to set
         * @return a reference to this Builder
         */
        public Builder apiDefinition(String apiDefinition) {
            this.apiDefinition = apiDefinition;
            return this;
        }

        /**
         * Sets the {@code permissionMap} and returns a reference to this Builder so that the methods can be
         * chained together.
         *
         * @param permissionMap the {@code permissionMap} to set
         * @return a reference to this Builder
         */
        public Builder permissionMap(HashMap permissionMap) {
            this.permissionMap = permissionMap;
            return this;
        }

        /**
         * Sets the {@code apiPermission} and returns a reference to this Builder so that the methods can be
         * chained together.
         *
         * @param apiPermission the {@code apiPermission} to set
         * @return a reference to this Builder
         */
        public Builder apiPermission(String apiPermission) {
            this.apiPermission = apiPermission;
            return this;
        }

        /**
         * Sets the {@code workflowStatus} and returns a reference to this Builder so that the methods can be
         * chained together.
         *
         * @param workflowStatus the {@code workflowStatus} to set
         * @return a reference to this Builder
         */
        public Builder workflowStatus(String workflowStatus) {
            this.workflowStatus = workflowStatus;
            return this;
        }

        public Builder threatProtectionPolicies(Set<String> policies) {
            this.threatProtectionPolicies = policies;
            return this;
        }

        /**
         * Sets the {@code hasOwnGateway} and returns a reference to Builder so that methods can be chained together.
         *
         * @param hasOwnGateway the {@code hasOwnGateway} to set
         * @return a reference to this Builder
        */
       public Builder hasOwnGateway(boolean hasOwnGateway) {
            this.hasOwnGateway = hasOwnGateway;
            return this;
        }

        /**
         * Returns a {@code CompositeAPI} built from the parameters previously set.
         *
         * @return a {@code CompositeAPI} built with parameters of this {@code CompositeAPI.Builder}
         */
        public CompositeAPI build() {
            return new CompositeAPI(this);
        }

        public String getId() {
            return id;
        }

        public String getProvider() {
            return provider;
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }

        public String getContext() {
            return context;
        }

        public String getDescription() {
            return description;
        }

        public String getGatewayConfig() {
            return gatewayConfig;
        }

        public Set<String> getTransport() {
            return transport;
        }

        public List<String> getLabels() {
            return labels;
        }

        public String getApplicationId() {
            return applicationId;
        }

        public Instant getCreatedTime() {
            return createdTime;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public String getUpdatedBy() {
            return updatedBy;
        }

        public Instant getLastUpdatedTime() {
            return lastUpdatedTime;
        }

        public Map<String, UriTemplate> getUriTemplates() {
            return uriTemplates;
        }

        public String getCopiedFromApiId() {
            return copiedFromApiId;
        }

        public String getApiDefinition() {
            return apiDefinition;
        }

        public HashMap getPermissionMap() {
            return permissionMap;
        }

        public String getApiPermission() {
            return apiPermission;
        }

        public String getWorkflowStatus() {
            return workflowStatus;
        }

        public Set<String> getThreatProtectionPolicies() {
            return threatProtectionPolicies;
        }

        public boolean getHasOwnGateway() {
            return hasOwnGateway;
        }
    }
}
