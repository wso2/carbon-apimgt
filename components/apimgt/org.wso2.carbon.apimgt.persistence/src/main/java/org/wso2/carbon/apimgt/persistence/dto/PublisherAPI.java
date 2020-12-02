/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.persistence.dto;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/*
  Represents the API information stored in persistence layer, that is used for publisher operations
 */
public class PublisherAPI extends PublisherAPIInfo {

    // below all the attributes are added (via createAPIArtifactContent() method) and taken back from registry when
    // getting an API by ID.
    private boolean isDefaultVersion = false;
    private String description;
    private String wsdlUrl;
    private String wadlUrl; // is this required?
    private String technicalOwner;
    private String technicalOwnerEmail;
    private String businessOwner;
    private String businessOwnerEmail;
    private String visibility;
    private String visibleRoles;
    private String visibleOrganizations; //visibleTenants
    private boolean endpointSecured;
    private String swaggerDefinition;
    private boolean endpointAuthDigest;
    private String endpointUTUsername;
    private String endpointUTPassword;
    private String transports;
    private String inSequence;
    private String outSequence;
    private String faultSequence;
    private String responseCache;
    private int cacheTimeout;
    private String redirectURL;  // check ??
    private String apiOwner;
    private boolean advertiseOnly;
    private String endpointConfig;
    private String subscriptionAvailability; // e.g. "CURRENT_TENANT";who is allowed for subscriptions
    private String subscriptionAvailableOrgs; // subscriptionAvailableTenants;
    private String implementation;
    private String productionMaxTps;
    private String sandboxMaxTps;
    private String authorizationHeader;
    private String apiSecurity; // ?check whether same to private List<String> securityScheme = new ArrayList<>();
    private boolean enableSchemaValidation;
    private boolean enableStore;
    private String testKey;
    private String contextTemplate;
    private Set<String> availableTierNames;
    private Set<String> environments;
    private CORSConfiguration corsConfiguration;
    private Set<String> gatewayLabels;
    private Set<String> apiCategories;
    private boolean isMonetizationEnabled;
    private Map<String, String> monetizationProperties = new JSONObject();
    private List<String> keyManagers = new ArrayList<>();
    private Set<DeploymentEnvironments> deploymentEnvironments;
    private Set<String> tags = new LinkedHashSet<>();
    private String accessControl; // publisher accessControl : 'restricted', 'all'
    private Set<String> accessControlRoles; // reg has a just String
    private Map<String, String> additionalProperties;
    private String thumbnail;
    private String createdTime;
    private String lastUpdated;
    private APIDocumentation documentation;

    public APIDocumentation getDocumentation() {
        return documentation;
    }

    public void setDocumentation(APIDocumentation documentation) {
        this.documentation = documentation;
    }

    public String getSwaggerDefinition() {
        return swaggerDefinition;
    }

    public void setSwaggerDefinition(String swaggerDefinition) {
        this.swaggerDefinition = swaggerDefinition;
    }

    public boolean isDefaultVersion() {
        return isDefaultVersion;
    }

    public void setDefaultVersion(boolean isDefaultVersion) {
        this.isDefaultVersion = isDefaultVersion;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWsdlUrl() {
        return wsdlUrl;
    }

    public void setWsdlUrl(String wsdlUrl) {
        this.wsdlUrl = wsdlUrl;
    }

    public String getWadlUrl() {
        return wadlUrl;
    }

    public void setWadlUrl(String wadlUrl) {
        this.wadlUrl = wadlUrl;
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

    public String getVisibility() {
        return visibility;
    }

    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }

    public String getVisibleRoles() {
        return visibleRoles;
    }

    public void setVisibleRoles(String visibleRoles) {
        this.visibleRoles = visibleRoles;
    }

    public String getVisibleOrganizations() {
        return visibleOrganizations;
    }

    public void setVisibleOrganizations(String visibleOrganizations) {
        this.visibleOrganizations = visibleOrganizations;
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

    public String getRedirectURL() {
        return redirectURL;
    }

    public void setRedirectURL(String redirectURL) {
        this.redirectURL = redirectURL;
    }

    public String getApiOwner() {
        return apiOwner;
    }

    public void setApiOwner(String apiOwner) {
        this.apiOwner = apiOwner;
    }

    public boolean isAdvertiseOnly() {
        return advertiseOnly;
    }

    public void setAdvertiseOnly(boolean advertiseOnly) {
        this.advertiseOnly = advertiseOnly;
    }

    public String getEndpointConfig() {
        return endpointConfig;
    }

    public void setEndpointConfig(String endpointConfig) {
        this.endpointConfig = endpointConfig;
    }

    public String getSubscriptionAvailability() {
        return subscriptionAvailability;
    }

    public void setSubscriptionAvailability(String subscriptionAvailability) {
        this.subscriptionAvailability = subscriptionAvailability;
    }

    public String getSubscriptionAvailableOrgs() {
        return subscriptionAvailableOrgs;
    }

    public void setSubscriptionAvailableOrgs(String subscriptionAvailableOrgs) {
        this.subscriptionAvailableOrgs = subscriptionAvailableOrgs;
    }

    public String getImplementation() {
        return implementation;
    }

    public void setImplementation(String implementation) {
        this.implementation = implementation;
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

    public String getAuthorizationHeader() {
        return authorizationHeader;
    }

    public void setAuthorizationHeader(String authorizationHeader) {
        this.authorizationHeader = authorizationHeader;
    }

    public String getApiSecurity() {
        return apiSecurity;
    }

    public void setApiSecurity(String apiSecurity) {
        this.apiSecurity = apiSecurity;
    }

    public boolean isEnableSchemaValidation() {
        return enableSchemaValidation;
    }

    public void setEnableSchemaValidation(boolean enableSchemaValidation) {
        this.enableSchemaValidation = enableSchemaValidation;
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

    public String getContextTemplate() {
        return contextTemplate;
    }

    public void setContextTemplate(String contextTemplate) {
        this.contextTemplate = contextTemplate;
    }

    public Set<String> getAvailableTierNames() {
        return availableTierNames;
    }

    public void setAvailableTierNames(Set<String> availableTierNames) {
        this.availableTierNames = availableTierNames;
    }

    public Set<String> getEnvironments() {
        return environments;
    }

    public void setEnvironments(Set<String> environments) {
        this.environments = environments;
    }

    public CORSConfiguration getCorsConfiguration() {
        return corsConfiguration;
    }

    public void setCorsConfiguration(CORSConfiguration corsConfiguration) {
        this.corsConfiguration = corsConfiguration;
    }

    public Set<String> getGatewayLabels() {
        return gatewayLabels;
    }

    public void setGatewayLabels(Set<String> gatewayLabels) {
        this.gatewayLabels = gatewayLabels;
    }

    public Set<String> getApiCategories() {
        return apiCategories;
    }

    public void setApiCategories(Set<String> apiCategories) {
        this.apiCategories = apiCategories;
    }

    public boolean isMonetizationEnabled() {
        return isMonetizationEnabled;
    }

    public void setMonetizationEnabled(boolean isMonetizationEnabled) {
        this.isMonetizationEnabled = isMonetizationEnabled;
    }

    public Map<String, String> getMonetizationProperties() {
        return monetizationProperties;
    }

    public void setMonetizationProperties(Map<String, String> monetizationProperties) {
        this.monetizationProperties = monetizationProperties;
    }

    public List<String> getKeyManagers() {
        return keyManagers;
    }

    public void setKeyManagers(List<String> keyManagers) {
        this.keyManagers = keyManagers;
    }

    public Set<DeploymentEnvironments> getDeploymentEnvironments() {
        return deploymentEnvironments;
    }

    public void setDeploymentEnvironments(Set<DeploymentEnvironments> deploymentEnvironments) {
        this.deploymentEnvironments = deploymentEnvironments;
    }

    public Set<String> getTags() {
        return tags;
    }

    public void setTags(Set<String> tags) {
        this.tags = tags;
    }

    public String getAccessControl() {
        return accessControl;
    }

    public void setAccessControl(String accessControl) {
        this.accessControl = accessControl;
    }

    public Set<String> getAccessControlRoles() {
        return accessControlRoles;
    }

    public void setAccessControlRoles(Set<String> accessControlRoles) {
        this.accessControlRoles = accessControlRoles;
    }

    public Map<String, String> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, String> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(String lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return "PublisherAPI [isDefaultVersion=" + isDefaultVersion + ", description=" + description + ", wsdlUrl="
                + wsdlUrl + ", wadlUrl=" + wadlUrl + ", technicalOwner=" + technicalOwner + ", technicalOwnerEmail="
                + technicalOwnerEmail + ", businessOwner=" + businessOwner + ", businessOwnerEmail="
                + businessOwnerEmail + ", visibility=" + visibility + ", visibleRoles=" + visibleRoles
                + ", visibleOrganizations=" + visibleOrganizations + ", endpointSecured=" + endpointSecured
                + ", endpointAuthDigest=" + endpointAuthDigest + ", endpointUTUsername=" + endpointUTUsername
                + ", endpointUTPassword=" + endpointUTPassword + ", transports=" + transports + ", inSequence="
                + inSequence + ", outSequence=" + outSequence + ", faultSequence=" + faultSequence + ", responseCache="
                + responseCache + ", cacheTimeout=" + cacheTimeout + ", redirectURL=" + redirectURL + ", apiOwner="
                + apiOwner + ", advertiseOnly=" + advertiseOnly + ", endpointConfig=" + endpointConfig
                + ", subscriptionAvailability=" + subscriptionAvailability + ", subscriptionAvailableOrgs="
                + subscriptionAvailableOrgs + ", implementation=" + implementation + ", productionMaxTps="
                + productionMaxTps + ", sandboxMaxTps=" + sandboxMaxTps + ", authorizationHeader=" + authorizationHeader
                + ", apiSecurity=" + apiSecurity + ", enableSchemaValidation=" + enableSchemaValidation
                + ", enableStore=" + enableStore + ", testKey=" + testKey + ", contextTemplate=" + contextTemplate
                + ", availableTierNames=" + availableTierNames + ", environments=" + environments
                + ", corsConfiguration=" + corsConfiguration + ", gatewayLabels=" + gatewayLabels + ", apiCategories="
                + apiCategories + ", isMonetizationEnabled=" + isMonetizationEnabled + ", monetizationProperties="
                + monetizationProperties + ", keyManagers=" + keyManagers + ", deploymentEnvironments="
                + deploymentEnvironments + ", tags=" + tags + ", accessControl=" + accessControl
                + ", accessControlRoles=" + accessControlRoles + ", additionalProperties=" + additionalProperties
                + ", thumbnail=" + thumbnail + ", createdTime=" + createdTime + ", lastUpdated=" + lastUpdated
                + ", toString()=" + super.toString() + "]";
    }
}

/*
    is needed? >
       artifact.setAttribute(APIConstants.API_OVERVIEW_VERSION_TYPE, "context"); no usage found.
        API_OVERVIEW_VERSION_TYPE = "overview_versionType";

       artifact.setAttribute(APIConstants.API_OVERVIEW_IS_LATEST, "true"); API_OVERVIEW_IS_LATEST =      "overview_isLatest" // this is  set only
       if api state is 'published'. no usage found
 */
