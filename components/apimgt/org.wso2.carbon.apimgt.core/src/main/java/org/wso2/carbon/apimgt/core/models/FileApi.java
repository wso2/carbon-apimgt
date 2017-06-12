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

import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.lcm.core.impl.LifecycleState;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Representation Api for File system
 */
public class FileApi {
    private String id;
    private String provider;
    private String name;
    private String version;
    private String context;
    private String description;
    private String lifeCycleStatus;
    private String lifecycleInstanceId;
    private Map<String, Endpoint> endpoint;
    private String gatewayConfig;
    private String wsdlUri;
    private boolean isResponseCachingEnabled;
    private int cacheTimeout;
    private boolean isDefaultVersion;
    private Set<String> transport;
    private Set<String> tags;
    private Set<String> labels;
    private Set<String> policies;
    private API.Visibility visibility;
    private Set<String> visibleRoles;
    private BusinessInformation businessInformation;
    private CorsConfiguration corsConfiguration;
    private String applicationId;
    private LocalDateTime createdTime;
    private String createdBy;
    private String updatedBy;
    private LocalDateTime lastUpdatedTime;
    private LifecycleState lifecycleState;
    private Map<String, FileUriTemplate> uriTemplates;
    private String copiedFromApiId;
    private String apiDefinition;
    private Map permissionMap;
    private String apiPermission;
    private String workflowStatus;
    private String apiPolicy;
    private List<String> userSpecificApiPermissions;
    private int securityScheme;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProvider() {
        return provider;
    }

    public void setProvider(String provider) {
        this.provider = provider;
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

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLifeCycleStatus() {
        return lifeCycleStatus;
    }

    public void setLifeCycleStatus(String lifeCycleStatus) {
        this.lifeCycleStatus = lifeCycleStatus;
    }

    public String getLifecycleInstanceId() {
        return lifecycleInstanceId;
    }

    public void setLifecycleInstanceId(String lifecycleInstanceId) {
        this.lifecycleInstanceId = lifecycleInstanceId;
    }

    public Map<String, Endpoint> getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(Map<String, Endpoint> endpoint) {
        this.endpoint = endpoint;
    }

    public String getGatewayConfig() {
        return gatewayConfig;
    }

    public void setGatewayConfig(String gatewayConfig) {
        this.gatewayConfig = gatewayConfig;
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

    public boolean isDefaultVersion() {
        return isDefaultVersion;
    }

    public void setDefaultVersion(boolean defaultVersion) {
        isDefaultVersion = defaultVersion;
    }

    public Set<String> getTransport() {
        return transport;
    }

    public void setTransport(Set<String> transport) {
        this.transport = transport;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public FileApi(API api) {
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
        api.getPolicies().forEach((Policy policy) -> policies.add(policy.getPolicyName()));
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
            uriTemplates.put(s, new FileUriTemplate(uriTemplate));
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
        userSpecificApiPermissions = api.getUserSpecificApiPermissions();
        if (api.getApiPolicy() != null) {
            this.apiPolicy = api.getApiPolicy().getPolicyName();
        }
    }

    public Set<String> getLabels() {
        return labels;
    }

    public void setLabels(Set<String> labels) {
        this.labels = labels;
    }

    public Set<String> getPolicies() {
        return policies;
    }

    public void setPolicies(Set<String> policies) {
        this.policies = policies;
    }

    public API.Visibility getVisibility() {
        return visibility;
    }

    public void setVisibility(API.Visibility visibility) {
        this.visibility = visibility;
    }

    public Set<String> getVisibleRoles() {
        return visibleRoles;
    }

    public void setVisibleRoles(Set<String> visibleRoles) {
        this.visibleRoles = visibleRoles;
    }

    public BusinessInformation getBusinessInformation() {
        return businessInformation;
    }

    public void setBusinessInformation(BusinessInformation businessInformation) {
        this.businessInformation = businessInformation;
    }

    public CorsConfiguration getCorsConfiguration() {
        return corsConfiguration;
    }

    public void setCorsConfiguration(CorsConfiguration corsConfiguration) {
        this.corsConfiguration = corsConfiguration;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        this.applicationId = applicationId;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
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

    public LocalDateTime getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(LocalDateTime lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public LifecycleState getLifecycleState() {
        return lifecycleState;
    }

    public void setLifecycleState(LifecycleState lifecycleState) {
        this.lifecycleState = lifecycleState;
    }

    public Map<String, FileUriTemplate> getUriTemplates() {
        return uriTemplates;
    }

    public void setUriTemplates(Map<String, FileUriTemplate> uriTemplates) {
        this.uriTemplates = uriTemplates;
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

    public void setApiDefinition(String apiDefinition) {
        this.apiDefinition = apiDefinition;
    }

    public Map getPermissionMap() {
        return permissionMap;
    }

    public void setPermissionMap(Map permissionMap) {
        this.permissionMap = permissionMap;
    }

    public String getApiPermission() {
        return apiPermission;
    }

    public void setApiPermission(String apiPermission) {
        this.apiPermission = apiPermission;
    }

    public String getWorkflowStatus() {
        return workflowStatus;
    }

    public void setWorkflowStatus(String workflowStatus) {
        this.workflowStatus = workflowStatus;
    }

    public String getApiPolicy() {
        return apiPolicy;
    }

    public void setApiPolicy(String apiPolicy) {
        this.apiPolicy = apiPolicy;
    }

    public List<String> getUserSpecificApiPermissions() {
        return userSpecificApiPermissions;
    }

    public void setUserSpecificApiPermissions(List<String> userSpecificApiPermissions) {
        this.userSpecificApiPermissions = userSpecificApiPermissions;
    }

    public int getSecurityScheme() {
        return securityScheme;
    }

    public void setSecurityScheme(int securityScheme) {
        this.securityScheme = securityScheme;
    }

    public FileApi() {
    }

    /**
     * Representation of Uritemplate for file system
     */
    public static class FileUriTemplate {
        private String templateId;
        private String uriTemplate;
        private String httpVerb;
        private String authType;
        private String policy;
        private Map<String, Endpoint> endpoint;

        public FileUriTemplate(UriTemplate uriTemplate) {
            this.templateId = uriTemplate.getTemplateId();
            this.uriTemplate = uriTemplate.getUriTemplate();
            this.authType = uriTemplate.getAuthType();
            this.httpVerb = uriTemplate.getHttpVerb();
            this.policy = uriTemplate.getPolicy().getPolicyName();
        }

        public String getTemplateId() {
            return templateId;
        }

        public void setTemplateId(String templateId) {
            this.templateId = templateId;
        }

        public String getUriTemplate() {
            return uriTemplate;
        }

        public void setUriTemplate(String uriTemplate) {
            this.uriTemplate = uriTemplate;
        }

        public String getHttpVerb() {
            return httpVerb;
        }

        public void setHttpVerb(String httpVerb) {
            this.httpVerb = httpVerb;
        }

        public String getAuthType() {
            return authType;
        }

        public void setAuthType(String authType) {
            this.authType = authType;
        }

        public String getPolicy() {
            return policy;
        }

        public void setPolicy(String policy) {
            this.policy = policy;
        }

        public Map<String, Endpoint> getEndpoint() {
            return endpoint;
        }

        public void setEndpoint(Map<String, Endpoint> endpoint) {
            this.endpoint = endpoint;
        }
    }
}

