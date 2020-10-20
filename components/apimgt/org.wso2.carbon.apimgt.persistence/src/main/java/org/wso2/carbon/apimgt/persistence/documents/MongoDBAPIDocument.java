/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.persistence.documents;

import org.apache.commons.lang3.StringUtils;
import org.bson.codecs.pojo.annotations.BsonId;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.bson.types.ObjectId;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.model.APICategory;
import org.wso2.carbon.apimgt.api.model.APIEndpoint;
import org.wso2.carbon.apimgt.api.model.AuthorizationPolicy;
import org.wso2.carbon.apimgt.api.model.DeploymentEnvironments;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.Label;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.policy.Policy;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class MongoDBAPIDocument {

    @BsonProperty(value = "_id")
    @BsonId
    private ObjectId uuid;
    private String name;
    private String version;
    private String description;
    private String provider;
    private String url;
    private String sandboxUrl;
    private String wsdlUrl;
    private String wsdlArchivePath;
    private String wadlUrl;
    private String swaggerDefinition;
    private String graphQLSchema;
    private String type;
    private String context;
    private String contextTemplate;
    private String thumbnailUrl;
    private ResourceFile wsdlResource;
    private Set<String> tags;
    private Set<Documentation> documents;
    private String httpVerb;
    private Date lastUpdated;
    private Set<TiersDocument> availableTiers = new LinkedHashSet<>();
    private Set<Policy> availableSubscriptionLevelPolicies = new LinkedHashSet<Policy>();
    private String apiLevelPolicy;
    private AuthorizationPolicy authorizationPolicy;
    private Set<URITemplateDocument> uriTemplates = new LinkedHashSet<URITemplateDocument>();
    //dirty pattern to identify which parts to be updated
    private boolean apiHeaderChanged;
    private boolean apiResourcePatternsChanged;
    private OrganizationDocument organization;

    private String status;

    private String technicalOwner;
    private String technicalOwnerEmail;
    private String businessOwner;
    private String businessOwnerEmail;

    // Used for keeping Production & Sandbox Throttling limits.
    private String productionMaxTps;
    private String sandboxMaxTps;

    @BsonProperty(value = "devportalVisibility")
    private String visibility;
    @BsonProperty(value = "devportalVisibleRoles")
    private Set<String> visibleRoles;
    @BsonProperty(value = "devportalVisibleTenants")
    private Set<String> visibleTenants;

    private List<Label> gatewayLabels;

    private boolean endpointSecured = false;
    private boolean endpointAuthDigest = false;
    private String endpointUTUsername;
    private String endpointUTPassword;

    private String transports;
    private String inSequence;
    private String outSequence;
    private String faultSequence;

    private String oldInSequence;
    private String oldOutSequence;
    private String oldFaultSequence;

    private boolean advertiseOnly;
    private String apiOwner;
    private String redirectURL;

    private String subscriptionAvailability;
    private Set<String> subscriptionAvailableTenants;
    private CORSConfigurationDocument corsConfiguration;
    private String endpointConfig;

    private String responseCache;
    private int cacheTimeout;

    private String implementation = "ENDPOINT";

    private String monetizationCategory;

    //Custom authorization header specific to the API
    private String authorizationHeader;
    private Set<Scope> scopes;

    private boolean isDefaultVersion = false;
    private boolean isPublishedDefaultVersion = false;
    private List<String> keyManagers = new ArrayList<>();
    /**
     * Used to set the workflow status in lifecycle state change workflow
     */
    private String workflowStatus = null;

    private Set<String> environments;

    private String createdTime;
    /**
     * Customized properties relevant to the particular API.
     */
    private JSONObject additionalProperties;

    /**
     * Properties relevant to monetization of the particular API.
     */
    private String monetizationProperties;

    /**
     * Property to indicate the monetization status of the particular API.
     */
    private boolean isMonetizationEnabled = false;

    /**
     * Property to hold selected deployment environments of the  particular API.
     */
    private Set<DeploymentEnvironments> deploymentEnvironments;

    // Used for endpoint environments configured with non empty URLs
    private Set<String> environmentList;

    // API security at the gateway level.
    private String apiSecurity = "oauth2";

    private List<APIEndpoint> endpoints = new ArrayList<APIEndpoint>();

    /**
     * Property to hold the enable/disable status of the json schema validation.
     */
    private boolean enableSchemaValidation = false;

    private List<APICategory> apiCategories;

    /**
     * Property to hold enable/disable status of the store visibility.
     */
    private boolean enableStore = true;

    private String testKey;

    @BsonProperty(value = "publisherAccessControl")
    private String accessControl;
    @BsonProperty(value = "publisherAccessControlRoles")
    private String accessControlRoles;
    /**
     * The average rating provided by the API subscribers
     */
    private float rating;

    private boolean isLatest;
    private String versionType;

    public OrganizationDocument getDocument() {
        return organization;
    }

    public void setDocument(OrganizationDocument organization) {
        this.organization = organization;
    }

    public String getVersionType() {
        return versionType;
    }

    public void setVersionType(String versionType) {
        this.versionType = versionType;
    }

    public String getAccessControl() {
        return accessControl;
    }

    public void setAccessControl(String accessControl) {
        this.accessControl = accessControl;
    }

    public String getAccessControlRoles() {
        return accessControlRoles;
    }

    public void setAccessControlRoles(String accessControlRoles) {
        this.accessControlRoles = accessControlRoles;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public boolean isLatest() {
        return isLatest;
    }

    public void setLatest(boolean latest) {
        isLatest = latest;
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

    public ObjectId getUuid() {
        return uuid;
    }

    public void setUuid(ObjectId uuid) {
        this.uuid = uuid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSandboxUrl() {
        return sandboxUrl;
    }

    public void setSandboxUrl(String sandboxUrl) {
        this.sandboxUrl = sandboxUrl;
    }

    public String getWsdlUrl() {
        return wsdlUrl;
    }

    public void setWsdlUrl(String wsdlUrl) {
        this.wsdlUrl = wsdlUrl;
    }

    public String getWsdlArchivePath() {
        return wsdlArchivePath;
    }

    public void setWsdlArchivePath(String wsdlArchivePath) {
        this.wsdlArchivePath = wsdlArchivePath;
    }

    public String getWadlUrl() {
        return wadlUrl;
    }

    public void setWadlUrl(String wadlUrl) {
        this.wadlUrl = wadlUrl;
    }

    public String getSwaggerDefinition() {
        return swaggerDefinition;
    }

    public void setSwaggerDefinition(String swaggerDefinition) {
        this.swaggerDefinition = swaggerDefinition;
    }

    public String getGraphQLSchema() {
        return graphQLSchema;
    }

    public void setGraphQLSchema(String graphQLSchema) {
        this.graphQLSchema = graphQLSchema;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (StringUtils.isEmpty(type) || "NULL".equalsIgnoreCase(StringUtils.trim(type))) {
            this.type = "HTTP";
        } else {
            this.type = StringUtils.trim(type).toUpperCase();
        }
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getContextTemplate() {
        return contextTemplate;
    }

    public void setContextTemplate(String contextTemplate) {
        this.contextTemplate = contextTemplate;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public ResourceFile getWsdlResource() {
        return wsdlResource;
    }

    public void setWsdlResource(ResourceFile wsdlResource) {
        this.wsdlResource = wsdlResource;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public Set<Documentation> getDocuments() {
        return documents;
    }

    public void setDocuments(Set<Documentation> documents) {
        this.documents = documents;
    }

    public String getHttpVerb() {
        return httpVerb;
    }

    public void setHttpVerb(String httpVerb) {
        this.httpVerb = httpVerb;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Set<TiersDocument> getAvailableTiers() {
        return availableTiers;
    }

    public void setAvailableTiers(Set<TiersDocument> availableTiers) {
        this.availableTiers = availableTiers;
    }

    public Set<Policy> getAvailableSubscriptionLevelPolicies() {
        return availableSubscriptionLevelPolicies;
    }

    public void setAvailableSubscriptionLevelPolicies(
            Set<Policy> availableSubscriptionLevelPolicies) {
        this.availableSubscriptionLevelPolicies = availableSubscriptionLevelPolicies;
    }

    public String getApiLevelPolicy() {
        return apiLevelPolicy;
    }

    public void setApiLevelPolicy(String apiLevelPolicy) {
        this.apiLevelPolicy = apiLevelPolicy;
    }

    public AuthorizationPolicy getAuthorizationPolicy() {
        return authorizationPolicy;
    }

    public void setAuthorizationPolicy(AuthorizationPolicy authorizationPolicy) {
        this.authorizationPolicy = authorizationPolicy;
    }

    public Set<URITemplateDocument> getUriTemplates() {
        return uriTemplates;
    }

    public void setUriTemplates(Set<URITemplateDocument> uriTemplates) {
        this.uriTemplates = uriTemplates;
    }

    public boolean isApiHeaderChanged() {
        return apiHeaderChanged;
    }

    public void setApiHeaderChanged(boolean apiHeaderChanged) {
        this.apiHeaderChanged = apiHeaderChanged;
    }

    public boolean isApiResourcePatternsChanged() {
        return apiResourcePatternsChanged;
    }

    public void setApiResourcePatternsChanged(boolean apiResourcePatternsChanged) {
        this.apiResourcePatternsChanged = apiResourcePatternsChanged;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTechnicalOwner() {
        return technicalOwner;
    }

    public void setTechnicalOwner(String technicalOwner) {
        this.technicalOwner = technicalOwner;
    }

    public String getTechnicalOwnerEmail() {
        return technicalOwnerEmail;
    }

    public void setTechnicalOwnerEmail(String technicalOwnerEmail) {
        this.technicalOwnerEmail = technicalOwnerEmail;
    }

    public String getBusinessOwner() {
        return businessOwner;
    }

    public void setBusinessOwner(String businessOwner) {
        this.businessOwner = businessOwner;
    }

    public String getBusinessOwnerEmail() {
        return businessOwnerEmail;
    }

    public void setBusinessOwnerEmail(String businessOwnerEmail) {
        this.businessOwnerEmail = businessOwnerEmail;
    }

    public String getProductionMaxTps() {
        return productionMaxTps;
    }

    public void setProductionMaxTps(String productionMaxTps) {
        this.productionMaxTps = productionMaxTps;
    }

    public String getSandboxMaxTps() {
        return sandboxMaxTps;
    }

    public void setSandboxMaxTps(String sandboxMaxTps) {
        this.sandboxMaxTps = sandboxMaxTps;
    }

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public Set<String> getVisibleRoles() {
        return visibleRoles;
    }

    public void setVisibleRoles(Set<String> visibleRoles) {
        this.visibleRoles = visibleRoles;
    }

    public Set<String> getVisibleTenants() {
        return visibleTenants;
    }

    public void setVisibleTenants(Set<String> visibleTenants) {
        this.visibleTenants = visibleTenants;
    }

    public List<Label> getGatewayLabels() {
        return gatewayLabels;
    }

    public void setGatewayLabels(List<Label> gatewayLabels) {
        this.gatewayLabels = gatewayLabels;
    }

    public boolean isEndpointSecured() {
        return endpointSecured;
    }

    public void setEndpointSecured(boolean endpointSecured) {
        this.endpointSecured = endpointSecured;
    }

    public boolean isEndpointAuthDigest() {
        return endpointAuthDigest;
    }

    public void setEndpointAuthDigest(boolean endpointAuthDigest) {
        this.endpointAuthDigest = endpointAuthDigest;
    }

    public String getEndpointUTUsername() {
        return endpointUTUsername;
    }

    public void setEndpointUTUsername(String endpointUTUsername) {
        this.endpointUTUsername = endpointUTUsername;
    }

    public String getEndpointUTPassword() {
        return endpointUTPassword;
    }

    public void setEndpointUTPassword(String endpointUTPassword) {
        this.endpointUTPassword = endpointUTPassword;
    }

    public String getTransports() {
        return transports;
    }

    public void setTransports(String transports) {
        this.transports = transports;
    }

    public String getInSequence() {
        return inSequence;
    }

    public void setInSequence(String inSequence) {
        this.inSequence = inSequence;
    }

    public String getOutSequence() {
        return outSequence;
    }

    public void setOutSequence(String outSequence) {
        this.outSequence = outSequence;
    }

    public String getFaultSequence() {
        return faultSequence;
    }

    public void setFaultSequence(String faultSequence) {
        this.faultSequence = faultSequence;
    }

    public String getOldInSequence() {
        return oldInSequence;
    }

    public void setOldInSequence(String oldInSequence) {
        this.oldInSequence = oldInSequence;
    }

    public String getOldOutSequence() {
        return oldOutSequence;
    }

    public void setOldOutSequence(String oldOutSequence) {
        this.oldOutSequence = oldOutSequence;
    }

    public String getOldFaultSequence() {
        return oldFaultSequence;
    }

    public void setOldFaultSequence(String oldFaultSequence) {
        this.oldFaultSequence = oldFaultSequence;
    }

    public boolean isAdvertiseOnly() {
        return advertiseOnly;
    }

    public void setAdvertiseOnly(boolean advertiseOnly) {
        this.advertiseOnly = advertiseOnly;
    }

    public String getApiOwner() {
        return apiOwner;
    }

    public void setApiOwner(String apiOwner) {
        this.apiOwner = apiOwner;
    }

    public String getRedirectURL() {
        return redirectURL;
    }

    public void setRedirectURL(String redirectURL) {
        this.redirectURL = redirectURL;
    }

    public String getSubscriptionAvailability() {
        return subscriptionAvailability;
    }

    public void setSubscriptionAvailability(String subscriptionAvailability) {
        this.subscriptionAvailability = subscriptionAvailability;
    }

    public Set<String> getSubscriptionAvailableTenants() {
        return subscriptionAvailableTenants;
    }

    public void setSubscriptionAvailableTenants(Set<String> subscriptionAvailableTenants) {
        this.subscriptionAvailableTenants = subscriptionAvailableTenants;
    }

    public CORSConfigurationDocument getCorsConfiguration() {
        return corsConfiguration;
    }

    public void setCorsConfiguration(CORSConfigurationDocument corsConfiguration) {
        this.corsConfiguration = corsConfiguration;
    }

    public String getEndpointConfig() {
        return endpointConfig;
    }

    public void setEndpointConfig(String endpointConfig) {
        this.endpointConfig = endpointConfig;
    }

    public String getResponseCache() {
        return responseCache;
    }

    public void setResponseCache(String responseCache) {
        this.responseCache = responseCache;
    }

    public int getCacheTimeout() {
        return cacheTimeout;
    }

    public void setCacheTimeout(int cacheTimeout) {
        this.cacheTimeout = cacheTimeout;
    }

    public String getImplementation() {
        return implementation;
    }

    public void setImplementation(String implementation) {
        this.implementation = implementation;
    }

    public String getMonetizationCategory() {
        return monetizationCategory;
    }

    public void setMonetizationCategory(String monetizationCategory) {
        this.monetizationCategory = monetizationCategory;
    }

    public String getAuthorizationHeader() {
        return authorizationHeader;
    }

    public void setAuthorizationHeader(String authorizationHeader) {
        this.authorizationHeader = authorizationHeader;
    }

    public Set<Scope> getScopes() {
        return scopes;
    }

    public void setScopes(Set<Scope> scopes) {
        this.scopes = scopes;
    }

    public boolean isDefaultVersion() {
        return isDefaultVersion;
    }

    public void setDefaultVersion(boolean defaultVersion) {
        isDefaultVersion = defaultVersion;
    }

    public boolean isPublishedDefaultVersion() {
        return isPublishedDefaultVersion;
    }

    public void setPublishedDefaultVersion(boolean publishedDefaultVersion) {
        isPublishedDefaultVersion = publishedDefaultVersion;
    }

    public List<String> getKeyManagers() {
        return keyManagers;
    }

    public void setKeyManagers(List<String> keyManagers) {
        this.keyManagers = keyManagers;
    }

    public String getWorkflowStatus() {
        return workflowStatus;
    }

    public void setWorkflowStatus(String workflowStatus) {
        this.workflowStatus = workflowStatus;
    }

    public Set<String> getEnvironments() {
        return environments;
    }

    public void setEnvironments(Set<String> environments) {
        this.environments = environments;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public JSONObject getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(JSONObject additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public String getMonetizationProperties() {
        return monetizationProperties;
    }

    public void setMonetizationProperties(String monetizationProperties) {
        this.monetizationProperties = monetizationProperties;
    }

    public boolean isMonetizationEnabled() {
        return isMonetizationEnabled;
    }

    public void setMonetizationEnabled(boolean monetizationEnabled) {
        isMonetizationEnabled = monetizationEnabled;
    }

    public Set<DeploymentEnvironments> getDeploymentEnvironments() {
        return deploymentEnvironments;
    }

    public void setDeploymentEnvironments(
            Set<DeploymentEnvironments> deploymentEnvironments) {
        this.deploymentEnvironments = deploymentEnvironments;
    }

    public Set<String> getEnvironmentList() {
        return environmentList;
    }

    public void setEnvironmentList(Set<String> environmentList) {
        this.environmentList = environmentList;
    }

    public String getApiSecurity() {
        return apiSecurity;
    }

    public void setApiSecurity(String apiSecurity) {
        this.apiSecurity = apiSecurity;
    }

    public List<APIEndpoint> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<APIEndpoint> endpoints) {
        this.endpoints = endpoints;
    }

    public boolean isEnableSchemaValidation() {
        return enableSchemaValidation;
    }

    public void setEnableSchemaValidation(boolean enableSchemaValidation) {
        this.enableSchemaValidation = enableSchemaValidation;
    }

    public List<APICategory> getApiCategories() {
        return apiCategories;
    }

    public void setApiCategories(List<APICategory> apiCategories) {
        this.apiCategories = apiCategories;
    }

    public boolean isEnableStore() {
        return enableStore;
    }

    public void setEnableStore(boolean enableStore) {
        this.enableStore = enableStore;
    }

    public String getTestKey() {
        return testKey;
    }

    public void setTestKey(String testKey) {
        this.testKey = testKey;
    }
}
