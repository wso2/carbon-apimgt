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

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.wso2.carbon.apimgt.core.models.policy.APIPolicy;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.models.policy.SubscriptionPolicy;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.lcm.core.ManagedLifecycle;
import org.wso2.carbon.lcm.core.exception.LifecycleException;
import org.wso2.carbon.lcm.core.impl.LifecycleState;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Representation of an API object. Only immutable instances of this class can be created via the provided inner static
 * {@code APIBuilder} class which implements the builder pattern as outlined in "Effective Java 2nd Edition
 * by Joshua Bloch(Item 2)"
 */

public final class API {
    private API(APIBuilder builder) {
        id = builder.id;
        provider = builder.provider;
        name = builder.name;
        version = builder.version;
        context = builder.context;
        description = builder.description;
        lifeCycleStatus = builder.lifeCycleStatus;
        lifecycleInstanceId = builder.lifecycleInstanceId;
        endpoint = builder.endpoint;
        wsdlUri = builder.wsdlUri;
        isResponseCachingEnabled = builder.isResponseCachingEnabled;
        cacheTimeout = builder.cacheTimeout;
        isDefaultVersion = builder.isDefaultVersion;
        transport = builder.transport;
        tags = builder.tags;
        labels = builder.labels;
        policies = builder.policies;
        visibility = builder.visibility;
        visibleRoles = builder.visibleRoles;
        businessInformation = builder.businessInformation;
        corsConfiguration = builder.corsConfiguration;
        createdTime = builder.createdTime;
        applicationId = builder.applicationId;
        createdBy = builder.createdBy;
        updatedBy = builder.updatedBy;
        lastUpdatedTime = builder.lastUpdatedTime;
        lifecycleState = builder.lifecycleState;
        uriTemplates = builder.uriTemplates;
        copiedFromApiId = builder.copiedFromApiId;
        gatewayConfig = builder.gatewayConfig;
        apiDefinition = builder.apiDefinition;
        if (builder.apiPermission != null) {
            apiPermission = builder.apiPermission;
        } else {
            apiPermission = "";
        }
        permissionMap = builder.permissionMap;
        workflowStatus = builder.workflowStatus;
        apiPolicy = builder.apiPolicy;
        userSpecificApiPermissions = builder.userSpecificApiPermissions;
        securityScheme = builder.securityScheme;
    }

    public Map getPermissionMap() {
        return permissionMap;
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

    public Policy getApiPolicy() {
        return apiPolicy;
    }

    public String getDescription() {
        return description;
    }

    public String getLifeCycleStatus() {
        return lifeCycleStatus;
    }

    public String getLifecycleInstanceId() {
        return lifecycleInstanceId;
    }

    public Map<String, Endpoint> getEndpoint() {
        return endpoint;
    }

    public String getGatewayConfig() {
        return gatewayConfig;
    }

    public String getWsdlUri() {
        return wsdlUri;
    }

    public boolean isResponseCachingEnabled() {
        return isResponseCachingEnabled;
    }

    public int getCacheTimeout() {
        return cacheTimeout;
    }

    public boolean isDefaultVersion() {
        return isDefaultVersion;
    }

    public Set<String> getTransport() {
        return transport;
    }

    public Set<String> getTags() {
        return tags;
    }

    public Set<String> getLabels() {
        return labels;
    }

    public Set<Policy> getPolicies() {
        return policies;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public Set<String> getVisibleRoles() {
        return visibleRoles;
    }

    public BusinessInformation getBusinessInformation() {
        return businessInformation;
    }

    public CorsConfiguration getCorsConfiguration() {
        return corsConfiguration;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public LocalDateTime getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public LifecycleState getLifecycleState() {
        return lifecycleState;
    }

    public Map<String, UriTemplate> getUriTemplates() {
        return uriTemplates;
    }

    public String getApiPermission() {
        return apiPermission;
    }

    public String getApiDefinition() {
        return apiDefinition;
    }

    public String getCopiedFromApiId() {
        return copiedFromApiId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public List<String> getUserSpecificApiPermissions() {
        return userSpecificApiPermissions;
    }

    public int getSecurityScheme() {
        return securityScheme;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        API api = (API) o;
        return isResponseCachingEnabled == api.isResponseCachingEnabled &&
                cacheTimeout == api.cacheTimeout &&
                isDefaultVersion == api.isDefaultVersion &&
                Objects.equals(id, api.id) &&
                Objects.equals(provider, api.provider) &&
                Objects.equals(name, api.name) &&
                Objects.equals(version, api.version) &&
                Objects.equals(context, api.context) &&
                Objects.equals(description, api.description) &&
                Objects.equals(lifeCycleStatus, api.lifeCycleStatus) &&
                Objects.equals(lifecycleInstanceId, api.lifecycleInstanceId) &&
                Objects.equals(endpoint, api.endpoint) &&
                Objects.equals(gatewayConfig, api.gatewayConfig) &&
                Objects.equals(wsdlUri, api.wsdlUri) &&
                Objects.equals(transport, api.transport) &&
                Objects.equals(tags, api.tags) &&
                Objects.equals(labels, api.labels) &&
                visibility == api.visibility &&
                Objects.equals(visibleRoles, api.visibleRoles) &&
                Objects.equals(businessInformation, api.businessInformation) &&
                Objects.equals(corsConfiguration, api.corsConfiguration) &&
                Objects.equals(applicationId, api.applicationId) &&
                APIUtils.isTimeStampsEquals(createdTime, api.createdTime) &&
                Objects.equals(createdBy, api.createdBy) &&
                Objects.equals(updatedBy, api.updatedBy) &&
                APIUtils.isTimeStampsEquals(lastUpdatedTime, api.lastUpdatedTime) &&
                Objects.equals(lifecycleState, api.lifecycleState) &&
                Objects.equals(uriTemplates, api.uriTemplates) &&
                Objects.equals(copiedFromApiId, api.copiedFromApiId) &&
                Objects.equals(endpoint, api.endpoint) &&
                Objects.equals(securityScheme, api.securityScheme);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, provider, name, version, context, description, lifeCycleStatus, lifecycleInstanceId,
                endpoint, gatewayConfig, wsdlUri, isResponseCachingEnabled, cacheTimeout, isDefaultVersion,
                transport, tags, labels, visibility, visibleRoles, businessInformation, corsConfiguration,
                applicationId, createdTime, createdBy, updatedBy, lastUpdatedTime, lifecycleState,
                uriTemplates, copiedFromApiId, workflowStatus, securityScheme);
    }


    /**
     * Visibility options
     */
    public enum Visibility {
        PUBLIC, PRIVATE, RESTRICTED, CONTROLLED,
    }

    private final String id;
    private final String provider;
    private final String name;
    private final String version;
    private final String context;
    private final String description;
    private final String lifeCycleStatus;
    private final String lifecycleInstanceId;
    private final Map<String, Endpoint> endpoint;
    private final String gatewayConfig;
    private final String wsdlUri;
    private final boolean isResponseCachingEnabled;
    private final int cacheTimeout;
    private final boolean isDefaultVersion;
    private final Set<String> transport;
    private final Set<String> tags;
    private final Set<String> labels;
    private final Set<Policy> policies;
    private final Visibility visibility;
    private final Set<String> visibleRoles;
    private final BusinessInformation businessInformation;
    private final CorsConfiguration corsConfiguration;
    private final String applicationId;
    private final LocalDateTime createdTime;
    private final String createdBy;
    private final String updatedBy;
    private final LocalDateTime lastUpdatedTime;
    private final LifecycleState lifecycleState;
    private final Map<String, UriTemplate> uriTemplates;
    private String copiedFromApiId;
    private final String apiDefinition;
    private final Map permissionMap;
    private String apiPermission;
    private final String workflowStatus;
    private final Policy apiPolicy;
    private List<String> userSpecificApiPermissions;
    private int securityScheme;

    public String getWorkflowStatus() {
        return workflowStatus;
    }

    /**
     * {@code API} builder static inner class.
     */
    @SuppressFBWarnings("CD_CIRCULAR_DEPENDENCY")
    public static final class APIBuilder implements ManagedLifecycle {
        private String id;
        private String provider;
        private String name;

        public APIBuilder(FileApi api) {
            id = api.getId();
            provider = api.getProvider();
            name = api.getName();
            version = api.getVersion();
            context = api.getContext();
            description = api.getDescription();
            lifeCycleStatus = api.getLifeCycleStatus();
            lifecycleInstanceId = api.getLifecycleInstanceId();
            endpoint = api.getEndpoint();
            wsdlUri = api.getWsdlUri();
            isResponseCachingEnabled = api.isResponseCachingEnabled();
            cacheTimeout = api.getCacheTimeout();
            isDefaultVersion = api.isDefaultVersion();
            transport = api.getTransport();
            tags = api.getTags();
            labels = api.getLabels();
            policies = new HashSet<>();
            api.getPolicies().forEach(v -> policies.add(new SubscriptionPolicy(v)));
            visibility = api.getVisibility();
            visibleRoles = api.getVisibleRoles();
            businessInformation = api.getBusinessInformation();
            corsConfiguration = api.getCorsConfiguration();
            createdTime = api.getCreatedTime();
            applicationId = api.getApplicationId();
            createdBy = api.getCreatedBy();
            updatedBy = api.getUpdatedBy();
            lastUpdatedTime = api.getLastUpdatedTime();
            lifecycleState = api.getLifecycleState();
            uriTemplates = new HashMap<>();
            api.getUriTemplates().forEach((s, uriTemplate) -> {
                uriTemplates.put(s, new UriTemplate.UriTemplateBuilder(uriTemplate).build());
            });
            copiedFromApiId = api.getCopiedFromApiId();
            gatewayConfig = api.getGatewayConfig();
            apiDefinition = api.getApiDefinition();
            if (api.getApiPermission() != null) {
                apiPermission = api.getApiPermission();
            } else {
                apiPermission = "";
            }
            permissionMap = api.getPermissionMap();
            workflowStatus = api.getWorkflowStatus();
            if (api.getApiPolicy() != null) {
                apiPolicy = new APIPolicy(api.getApiPolicy());
            }
            userSpecificApiPermissions = api.getUserSpecificApiPermissions();
        }

        public String getId() {
            return id;
        }

        public String getProvider() {
            return provider;
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

        public String getLifeCycleStatus() {
            return lifeCycleStatus;
        }

        public String getLifecycleInstanceId() {
            return lifecycleInstanceId;
        }

        public Map<String, Endpoint> getEndpoint() {
            return endpoint;
        }

        public String getGatewayConfig() {
            return gatewayConfig;
        }

        public String getApiPermission() {
            return apiPermission;
        }

        public String getWsdlUri() {
            return wsdlUri;
        }

        public boolean isResponseCachingEnabled() {
            return isResponseCachingEnabled;
        }

        public int getCacheTimeout() {
            return cacheTimeout;
        }

        public boolean isDefaultVersion() {
            return isDefaultVersion;
        }

        public Policy getApiPolicy() {
            return apiPolicy;
        }

        public Set<String> getTransport() {
            return transport;
        }

        public Set<String> getTags() {
            return tags;
        }

        public Set<String> getLabels() {
            return labels;
        }

        public Set<Policy> getPolicies() {
            return policies;
        }

        public Visibility getVisibility() {
            return visibility;
        }

        public Set<String> getVisibleRoles() {
            return visibleRoles;
        }

        public BusinessInformation getBusinessInformation() {
            return businessInformation;
        }

        public List<String> getUserSpecificApiPermissions() {
            return userSpecificApiPermissions;
        }

        public int getSecurityScheme() {
            return securityScheme;
        }

        private String version;
        private String context;
        private String description;
        private String lifeCycleStatus;
        private String lifecycleInstanceId;
        private Map permissionMap;
        private String apiPermission;
        private Map<String, Endpoint> endpoint = Collections.EMPTY_MAP;
        private String gatewayConfig;
        private String wsdlUri;
        private boolean isResponseCachingEnabled;
        private int cacheTimeout;
        private boolean isDefaultVersion;
        private Policy apiPolicy;
        private Set<String> transport = Collections.emptySet();
        private Set<String> tags = Collections.emptySet();
        private Set<String> labels = Collections.emptySet();
        private Set<Policy> policies = Collections.emptySet();
        private Visibility visibility = Visibility.PUBLIC;
        private Set<String> visibleRoles = Collections.emptySet();
        private BusinessInformation businessInformation;
        private CorsConfiguration corsConfiguration;
        private String applicationId;
        private LocalDateTime createdTime;
        private String createdBy;
        private String updatedBy;
        private LocalDateTime lastUpdatedTime;
        private LifecycleState lifecycleState;
        private Map<String, UriTemplate> uriTemplates = Collections.EMPTY_MAP;
        private String copiedFromApiId;
        private String apiDefinition;
        private String workflowStatus;
        private List<String> userSpecificApiPermissions;
        private int securityScheme;

        public APIBuilder(String provider, String name, String version) {
            this.provider = provider;
            this.name = name;
            this.version = version;
        }

        public APIBuilder(API copy) {
            this.id = copy.id;
            this.provider = copy.provider;
            this.name = copy.name;
            this.version = copy.version;
            this.context = copy.context;
            this.description = copy.description;
            this.lifeCycleStatus = copy.lifeCycleStatus;
            this.lifecycleInstanceId = copy.lifecycleInstanceId;
            this.endpoint = copy.endpoint;
            this.wsdlUri = copy.wsdlUri;
            this.isResponseCachingEnabled = copy.isResponseCachingEnabled;
            this.cacheTimeout = copy.cacheTimeout;
            this.isDefaultVersion = copy.isDefaultVersion;
            this.transport = copy.transport;
            this.tags = copy.tags;
            this.labels = copy.labels;
            this.policies = copy.policies;
            this.visibility = copy.visibility;
            this.visibleRoles = copy.visibleRoles;
            this.businessInformation = copy.businessInformation;
            this.corsConfiguration = copy.corsConfiguration;
            this.applicationId = copy.applicationId;
            this.createdTime = copy.createdTime;
            this.createdBy = copy.createdBy;
            this.lastUpdatedTime = copy.lastUpdatedTime;
            this.lifecycleState = copy.lifecycleState;
            this.uriTemplates = copy.uriTemplates;
            this.copiedFromApiId = copy.copiedFromApiId;
            this.apiDefinition = copy.apiDefinition;
            if (copy.apiPermission != null) {
                this.apiPermission = copy.apiPermission;
            } else {
                this.apiPermission = "";
            }
            this.apiPolicy = copy.apiPolicy;
            this.permissionMap = new HashMap<>();
            this.workflowStatus = copy.workflowStatus;
            this.userSpecificApiPermissions = new ArrayList<String>();
            this.securityScheme = copy.securityScheme;
        }

        /**
         * Sets the {@code id} and returns a reference to this APIBuilder so that the methods can be chained together.
         *
         * @param id the {@code id} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder id(String id) {
            this.id = id;
            return this;
        }

        /**
         * Sets the {@code provider} and returns a reference to this APIBuilder so that the methods can be chained
         * together.
         *
         * @param provider the {@code provider} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder provider(String provider) {
            this.provider = provider;
            return this;
        }

        /**
         * Sets the {@code name} and returns a reference to this APIBuilder so that the methods can be chained together.
         *
         * @param name the {@code name} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Sets the {@code version} and returns a reference to this APIBuilder so that the methods can be chained
         * together.
         *
         * @param version the {@code version} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder version(String version) {
            this.version = version;
            return this;
        }

        /**
         * Sets the {@code context} and returns a reference to this APIBuilder so that the methods can be chained
         * together.
         *
         * @param context the {@code context} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder context(String context) {
            this.context = context;
            return this;
        }

        /**
         * Sets the {@code description} and returns a reference to this APIBuilder so that the methods can be chained
         * together.
         *
         * @param description the {@code description} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder description(String description) {
            this.description = description;
            return this;
        }

        /**
         * Sets the {@code lifeCycleStatus} and returns a reference to this APIBuilder so that the methods can be
         * chained together.
         *
         * @param lifeCycleStatus the {@code lifeCycleStatus} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder lifeCycleStatus(String lifeCycleStatus) {
            this.lifeCycleStatus = lifeCycleStatus;
            return this;
        }

        /**
         * Sets the {@code lifeCycleInstanceID} and returns a reference to this APIBuilder so that the methods can be
         * chained together.
         *
         * @param lifecycleInstanceId the {@code lifeCycleInstanceID} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder lifecycleInstanceId(String lifecycleInstanceId) {
            this.lifecycleInstanceId = lifecycleInstanceId;
            return this;
        }

        /**
         * Sets the lifecycleState and return a reference to this APIBuilder
         *
         * @param lifecycleState Lifecycle state object.
         * @return a reference to APIBuilder
         */
        public APIBuilder lifecycleState(LifecycleState lifecycleState) {
            this.lifecycleState = lifecycleState;
            return this;
        }

        /**
         * Sets the {@code endpoint} and returns a reference to this APIBuilder so that the methods can be chained
         * together.
         *
         * @param endpoint the {@code endpoint} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder endpoint(Map<String, Endpoint> endpoint) {
            this.endpoint = endpoint;
            return this;
        }

        public APIBuilder apiPermission(String apiPermission) {
            this.apiPermission = apiPermission;
            return this;
        }

        /**
         * Sets the {@code gatewayConfig} and returns a reference to this APIBuilder so that the methods can be chained
         * together.
         *
         * @param gatewayConfig the {@code gatewayConfig} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder gatewayConfig(String gatewayConfig) {
            this.gatewayConfig = gatewayConfig;
            return this;
        }

        /**
         * Sets the {@code wsdlUri} and returns a reference to this APIBuilder so that the methods can be chained
         * together.
         *
         * @param wsdlUri the {@code wsdlUri} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder wsdlUri(String wsdlUri) {
            this.wsdlUri = wsdlUri;
            return this;
        }

        /**
         * Sets the {@code isResponseCachingEnabled} and returns a reference to this APIBuilder so that the methods can
         * be chained together.
         *
         * @param isResponseCachingEnabled the {@code isResponseCachingEnabled} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder isResponseCachingEnabled(boolean isResponseCachingEnabled) {
            this.isResponseCachingEnabled = isResponseCachingEnabled;
            return this;
        }

        /**
         * Sets the {@code cacheTimeout} and returns a reference to this APIBuilder so that the methods can be chained
         * together.
         *
         * @param cacheTimeout the {@code cacheTimeout} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder cacheTimeout(int cacheTimeout) {
            this.cacheTimeout = cacheTimeout;
            return this;
        }

        /**
         * Sets the {@code isDefaultVersion} and returns a reference to this APIBuilder so that the methods can be
         * chained together.
         *
         * @param isDefaultVersion the {@code isDefaultVersion} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder isDefaultVersion(boolean isDefaultVersion) {
            this.isDefaultVersion = isDefaultVersion;
            return this;
        }

        /**
         * Sets the {@code apiPolicy} and returns a reference to this APIBuilder so that the methods can be chained
         * together.
         *
         * @param apiPolicy the {@code apiPolicy} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder apiPolicy(Policy apiPolicy) {
            this.apiPolicy = apiPolicy;
            return this;
        }

        /**
         * Sets the {@code transport} and returns a reference to this APIBuilder so that the methods can be chained
         * together.
         *
         * @param transport the {@code transport} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder transport(Set<String> transport) {
            this.transport = transport;
            return this;
        }

        /**
         * Sets the {@code tags} and returns a reference to this APIBuilder so that the methods can be chained together.
         *
         * @param tags the {@code tags} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder tags(Set<String> tags) {
            this.tags = tags;
            return this;
        }

        /**
         * Sets the {@code labels} and returns a reference to this APIBuilder so that the methods can be chained
         * together.
         *
         * @param labels the {@code labels} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder labels(Set<String> labels) {
            this.labels = labels;
            return this;
        }

        /**
         * Sets the {@code policies} and returns a reference to this APIBuilder so that the methods can be chained
         * together.
         *
         * @param policies the {@code policies} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder policies(Set<Policy> policies) {
            this.policies = policies;
            return this;
        }

        /**
         * Sets the {@code policies} and returns a reference to this APIBuilder so that the methods can be chained
         * together.
         *
         * @param uriTemplates the {@code uriTemplates} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder uriTemplates(Map<String, UriTemplate> uriTemplates) {
            this.uriTemplates = uriTemplates;
            return this;
        }

        public APIBuilder permissionMap(Map map) {
            this.permissionMap = map;
            return this;
        }

        /**
         * Sets the {@code visibility} and returns a reference to this APIBuilder so that the methods can be chained
         * together.
         *
         * @param visibility the {@code visibility} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder visibility(Visibility visibility) {
            this.visibility = visibility;
            return this;
        }

        /**
         * Sets the {@code visibleRoles} and returns a reference to this APIBuilder so that the methods can be chained
         * together.
         *
         * @param visibleRoles the {@code visibleRoles} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder visibleRoles(Set<String> visibleRoles) {
            this.visibleRoles = visibleRoles;
            return this;
        }

        /**
         * Sets the {@code businessInformation} and returns a reference to this APIBuilder so that the methods can be
         * chained together.
         *
         * @param businessInformation the {@code businessInformation} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder businessInformation(BusinessInformation businessInformation) {
            this.businessInformation = businessInformation;
            return this;
        }

        /**
         * Sets the {@code corsConfiguration} and returns a reference to this APIBuilder so that the methods can be
         * chained together.
         *
         * @param corsConfiguration the {@code corsConfiguration} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder corsConfiguration(CorsConfiguration corsConfiguration) {
            this.corsConfiguration = corsConfiguration;
            return this;
        }

        /**
         * Sets the {@code applicationId} and returns a reference to this APIBuilder so that the methods can be chained
         * together.
         *
         * @param applicationId the {@code applicationId} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder applicationId(String applicationId) {
            this.applicationId = applicationId;
            return this;
        }

        /**
         * Sets the {@code createdTime} and returns a reference to this APIBuilder so that the methods can be chained
         * together.
         *
         * @param createdTime the {@code createdTime} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder createdTime(LocalDateTime createdTime) {
            this.createdTime = createdTime;
            return this;
        }

        /**
         * Sets the {@code createdBy} and returns a reference to this APIBuilder so that the methods can be chained
         * together.
         *
         * @param createdBy the {@code createdBy} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder createdBy(String createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        /**
         * Sets the {@code updatedBy} and returns a reference to this APIBuilder so that the methods can be chained
         * together.
         *
         * @param updatedBy the {@code updatedBy} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder updatedBy(String updatedBy) {
            this.updatedBy = updatedBy;
            return this;
        }

        /**
         * Sets the {@code lastUpdatedTime} and returns a reference to this APIBuilder so that the methods can be
         * chained together.
         *
         * @param lastUpdatedTime the {@code lastUpdatedTime} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder lastUpdatedTime(LocalDateTime lastUpdatedTime) {
            this.lastUpdatedTime = lastUpdatedTime;
            return this;
        }

        /**
         * Sets the {@code copiedFromApiId} and returns a reference to this APIBuilder so that the methods can be
         * chained together.
         *
         * @param copiedFromApiId the {@code copiedFromApiId} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder copiedFromApiId(String copiedFromApiId) {
            this.copiedFromApiId = copiedFromApiId;
            return this;
        }

        /**
         * Sets the {@code copiedFromApiId} and returns a reference to this APIBuilder so that the methods can be
         * chained together.
         *
         * @param apiDefinition the {@code apiDefinition} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder apiDefinition(String apiDefinition) {
            this.apiDefinition = apiDefinition;
            return this;
        }

        /**
         * Sets the {@code workflowStatus} and returns a reference to this APIBuilder so that the methods can be
         * chained together.
         *
         * @param workflowStatus the {@code workflowStatus} to set
         * @return a reference to this APIBuilder
         */
        public APIBuilder workflowStatus(String workflowStatus) {
            this.workflowStatus = workflowStatus;
            return this;
        }

        public APIBuilder securityScheme(int securityScheme) {
            this.securityScheme = securityScheme;
            return this;
        }

        /**
         * Returns a {@code API} built from the parameters previously set.
         *
         * @return a {@code API} built with parameters of this {@code API.APIBuilder}
         */

        public API build() {
            return new API(this);
        }

        /**
         * This method should be implemented to create association between object which implementing Managed
         * Lifecycle and
         * the Lifecycle framework. This method should implement logic which saves the returned uuid in the external
         * party (API, APP etc). So both parties will have lifecycle uuid saved in their side which will cater the
         * purpose of mapping.
         *
         * @param lifecycleState Lifecycle state object.
         */
        @Override
        public void associateLifecycle(LifecycleState lifecycleState) throws LifecycleException {
            lifecycleInstanceId = lifecycleState.getLifecycleId();
            lifeCycleStatus = lifecycleState.getState();
            this.lifecycleState = lifecycleState;
        }

        /**
         * @param lcName Name of the lifecycle to be removed.
         *               This method should be implemented to remove the lifecycle data from the object which
         *               implements this interface.
         *               Persisted lifecycle state id (say stored in database) should be removed by implementing this
         *               method.
         */
        @Override
        public void dissociateLifecycle(String lcName) throws LifecycleException {
        }

        /**
         * @param lifecycleState Lifecycle state object.
         *                       This method should be implemented to update the lifecycle state after state change
         *                       operation and check list
         *                       item operation
         */
        @Override
        public void setLifecycleStateInfo(LifecycleState lifecycleState) throws LifecycleException {
            lifeCycleStatus = lifecycleState.getState();
            this.lifecycleState = lifecycleState;
        }

        public String getName() {
            return name;
        }

        public LifecycleState getLifecycleState() {
            return lifecycleState;
        }

        public LocalDateTime getCreatedTime() {
            return createdTime;
        }

        public String getCreatedBy() {
            return createdBy;
        }

        public LocalDateTime getLastUpdatedTime() {
            return lastUpdatedTime;
        }

        public Map<String, UriTemplate> getUriTemplates() {
            return uriTemplates;
        }

        public String getCopiedFromApiId() {
            return copiedFromApiId;
        }

        public void setCopiedFromApiId(String copiedFromApiId) {
            this.copiedFromApiId = copiedFromApiId;
        }

        public String getApiDefinition() {
            return apiDefinition;
        }

        public void setPermissionMap(HashMap permissionMap) {
            this.permissionMap = permissionMap;
        }

        public Map getPermissionMap() {
            return permissionMap;
        }

        public String getWorkflowStatus() {
            return workflowStatus;
        }

        public void setUserSpecificApiPermissions(List<String> userSpecificApiPermissions) {
            this.userSpecificApiPermissions = userSpecificApiPermissions;
        }
    }

    public void setCopiedFromApiId(String copiedFromApiId) {
        this.copiedFromApiId = copiedFromApiId;
    }

    public void setUserSpecificApiPermissions(List<String> userSpecificApiPermissions) {
        this.userSpecificApiPermissions = userSpecificApiPermissions;
    }

    public void setApiPermission(String apiPermission) {
        this.apiPermission = apiPermission;
    }
}
