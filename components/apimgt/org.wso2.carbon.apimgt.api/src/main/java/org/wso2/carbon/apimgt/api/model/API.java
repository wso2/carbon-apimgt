/*
*  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
package org.wso2.carbon.apimgt.api.model;

import org.apache.commons.lang.StringUtils;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.model.policy.Policy;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Provider's & system's view of API
 */
@SuppressWarnings("unused")
public class API implements Serializable {

    private APIIdentifier id;

    // uuid of registry artifact
    // this id is provider's username independent
    private String uuid;

    private String description;
    private String url;
    private String sandboxUrl;
    private String wsdlUrl;
    private String wsdlArchivePath;
    private String wadlUrl;
    private String type;
    private String context;
    private String contextTemplate;
    private String thumbnailUrl;
    private ResourceFile wsdlArchive;
    private Set<String> tags = new LinkedHashSet<String>();
    private Set<Documentation> documents = new LinkedHashSet<Documentation>();
    private String httpVerb;
    private Date lastUpdated;
    private Set<Tier> availableTiers = new LinkedHashSet<Tier>();
    private Set<Policy> availableSubscriptionLevelPolicies = new LinkedHashSet<Policy>();
    private String apiLevelPolicy;
    private AuthorizationPolicy authorizationPolicy;
    private Set<URITemplate> uriTemplates = new LinkedHashSet<URITemplate>();

    //dirty pattern to identify which parts to be updated
    private boolean apiHeaderChanged;
    private boolean apiResourcePatternsChanged;

    private String status;

    private String technicalOwner;
    private String technicalOwnerEmail;
    private String businessOwner;
    private String businessOwnerEmail;

    // Used for keeping Production & Sandbox Throttling limits.
    private String productionMaxTps;
    private String sandboxMaxTps;

    private String visibility;
    private String visibleRoles;
    private String visibleTenants;

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
    private String subscriptionAvailableTenants;
    private CORSConfiguration corsConfiguration;
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

    private Set<String> environments;

    private String createdTime;
    /**
     * Customized properties relevant to the particular API.
     */
    private JSONObject additionalProperties;

    // Used for endpoint environments configured with non empty URLs
    private Set<String> environmentList;

    // API security at the gateway level.
    private String apiSecurity = "oauth2";

    private static final String NULL_VALUE = "NULL";
    
    public void setEnvironmentList(Set<String> environmentList) {
        this.environmentList = environmentList;
    }

    public Set<String> getEnvironmentList() {
        return environmentList;
    }

    /**
     * To get the additional properties
     *
     * @return additional properties of the API
     */
    public JSONObject getAdditionalProperties() {
        return additionalProperties;
    }

    /**
     * To assign a set of customized properties to the API.
     *
     * @param properties Properties that need to be assigned to.
     */
    public void setAdditionalProperties(JSONObject properties) {
        this.additionalProperties = properties;
    }

    /**
     * To add a new property to additional properties list.
     *
     * @param key   Name of the property.
     * @param value Value of the property.
     */
    public void addProperty(String key, String value) {
        additionalProperties.put(key, value);
    }

    /**
     * To get the value of the property.
     *
     * @param key Name of the property
     * @return value of the property.
     */
    public String getProperty(String key) {
        return additionalProperties.get(key).toString();
    }

    /**
     * Publisher access control related parameters.
     * AccessControl -> Specifies whether that particular API is restricted to certain set of publishers and creators.
     * AccessControlRoles -> Specifies the roles that the particular API is visible to.
     */
    private String accessControl;
    private String accessControlRoles;

    public Set<String> getEnvironments() {
        return environments;
    }

    public void setEnvironments(Set<String> environments) {
        this.environments = environments;
    }

    /**
     * Contains flag indicating whether dummy backend or not
     *
     * @return
     */
    public String getImplementation() {
        return implementation;
    }

    /**
     * Returns flag indicating whether dummy backend or not
     *
     * @param implementation
     */
    public void setImplementation(String implementation) {
        this.implementation = implementation;
    }

    /**
     * The average rating provided by the API subscribers
     */
    private float rating;

    private boolean isLatest;

    //TODO: missing - total user count, up time statistics,tier

    public String getUUID() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
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

    public API(APIIdentifier id) {
        this.id = id;
        additionalProperties = new JSONObject();
    }

    public APIIdentifier getId() {
        return id;
    }

    public String getTransports() {
        return transports;
    }

    public void setTransports(String transports) {
        this.transports = transports;
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

    public void setContext(String context) {
        this.context = context;
    }

    public String getContext() {
        return context;
    }

    public void setContextTemplate(String contextTemplate) {
        this.contextTemplate = contextTemplate;
    }

    public String getContextTemplate() {
        return contextTemplate;
    }

    public void setWsdlUrl(String wsdlUrl) {
        this.wsdlUrl = wsdlUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public Set<String> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    public void addTags(Set<String> tags) {
        this.tags.addAll(tags);
    }

    public void removeTags(Set<String> tags) {
        this.tags.removeAll(tags);
    }

    public Set<Documentation> getDocuments() {
        return Collections.unmodifiableSet(documents);
    }

    public void addDocuments(Set<Documentation> documents) {
        this.documents.addAll(documents);
    }

    public void removeDocuments(Set<Documentation> documents) {
        this.documents.removeAll(documents);
    }

    public String getHttpVerb() {
        return httpVerb;
    }

    public void setHttpVerb(String httpVerb) {
        this.httpVerb = httpVerb;
    }

    public Date getLastUpdated() {
        return new Date(lastUpdated.getTime());
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = new Date(lastUpdated.getTime());
    }

    public Set<Tier> getAvailableTiers() {
        return Collections.unmodifiableSet(availableTiers);
    }

    public void addAvailableTiers(Set<Tier> availableTiers) {
        this.availableTiers.addAll(availableTiers);
    }

    /**
     * Removes all Tiers from the API object.
     */
    public void removeAllTiers() {
        availableTiers.clear();
    }

    /**
     * Removes all Policies from the API object.
     */
    public void removeAllPolicies() {
        availableSubscriptionLevelPolicies.clear();
    }

    public void removeAvailableTiers(Set<Tier> availableTiers) {
        this.availableTiers.removeAll(availableTiers);
    }

    public Set<URITemplate> getUriTemplates() {
        return uriTemplates;
    }

    public void setUriTemplates(Set<URITemplate> uriTemplates) {
        this.uriTemplates = uriTemplates;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setStatus(APIStatus status) {
        this.status = status.getStatus();
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public void setLatest(boolean latest) {
        isLatest = latest;
    }

    /**
     * @return true if the current version of the API is the latest
     */
    public boolean isLatest() {
        return isLatest;
    }

    public AuthorizationPolicy getAuthorizationPolicy() {
        return authorizationPolicy;
    }

    public void setAuthorizationPolicy(AuthorizationPolicy authorizationPolicy) {
        this.authorizationPolicy = authorizationPolicy;
    }

    public String getWadlUrl() {
        return wadlUrl;
    }

    public void setWadlUrl(String wadlUrl) {
        this.wadlUrl = wadlUrl;
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

    public String getVisibleTenants() {
        return visibleTenants;
    }

    public void setVisibleTenants(String visibleTenants) {
        this.visibleTenants = visibleTenants;
    }

    public List<Label> getGatewayLabels() {
        return gatewayLabels;
    }

    public void setGatewayLabels(List<Label> gatewayLabels) {
        this.gatewayLabels = gatewayLabels;
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

    /**
     * @return the endpointUTUsername
     */
    public String getEndpointUTUsername() {
        return endpointUTUsername;
    }

    /**
     * @param endpointUTUsername the endpointUTUsername to set
     */
    public void setEndpointUTUsername(String endpointUTUsername) {
        this.endpointUTUsername = endpointUTUsername;
    }

    /**
     * @return the endpointUTPassword
     */
    public String getEndpointUTPassword() {
        return endpointUTPassword;
    }

    /**
     * @param endpointUTPassword the endpointUTPassword to set
     */
    public void setEndpointUTPassword(String endpointUTPassword) {
        this.endpointUTPassword = endpointUTPassword;
    }

    /**
     * @return the endpointSecured
     */
    public boolean isEndpointSecured() {
        return endpointSecured;
    }

    /**
     * @param endpointSecured the endpointSecured to set
     */
    public void setEndpointSecured(boolean endpointSecured) {
        this.endpointSecured = endpointSecured;
    }

    /**
     * @return the endpointAuthDigest
     */
    public boolean isEndpointAuthDigest() {
        return endpointAuthDigest;
    }

    /**
     * @param endpointAuthDigest the endpointAuthDigest to set
     */
    public void setEndpointAuthDigest(boolean endpointAuthDigest) {
        this.endpointAuthDigest = endpointAuthDigest;
    }

    public String getInSequence() {
        return inSequence;
    }

    /**
     * @param inSeq insequence for the API
     */
    public void setInSequence(String inSeq) {
        this.inSequence = inSeq;
    }

    public String getOutSequence() {
        return outSequence;
    }

    /**
     * @param outSeq outSequence for the API
     */
    public void setOutSequence(String outSeq) {
        this.outSequence = outSeq;
    }

    /**
     * remove custom sequences from api object
     */
    public void removeCustomSequences() {
        this.inSequence = null;
        this.outSequence = null;
        this.faultSequence = null;
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

    public String getSubscriptionAvailability() {
        return subscriptionAvailability;
    }

    public void setSubscriptionAvailability(String subscriptionAvailability) {
        this.subscriptionAvailability = subscriptionAvailability;
    }

    public String getSubscriptionAvailableTenants() {
        return subscriptionAvailableTenants;
    }

    public void setSubscriptionAvailableTenants(String subscriptionAvailableTenants) {
        this.subscriptionAvailableTenants = subscriptionAvailableTenants;
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

    public String getFaultSequence() {
        return faultSequence;
    }

    public void setFaultSequence(String faultSequence) {
        this.faultSequence = faultSequence;
    }

    public String getOldFaultSequence() {
        return oldFaultSequence;
    }

    public void setOldFaultSequence(String oldFaultSequence) {
        this.oldFaultSequence = oldFaultSequence;
    }

    public Set<Scope> getScopes() {
        return scopes;
    }

    public void setScopes(Set<Scope> scopes) {
        this.scopes = scopes;
    }

    public void setAsDefaultVersion(boolean value) {
        isDefaultVersion = value;
    }

    public void setAsPublishedDefaultVersion(boolean value) {
        isPublishedDefaultVersion = value;
    }

    public boolean isDefaultVersion() {
        return isDefaultVersion;
    }

    public boolean isPublishedDefaultVersion() {
        return isPublishedDefaultVersion;
    }

    public CORSConfiguration getCorsConfiguration() {
        return corsConfiguration;
    }

    public void setCorsConfiguration(CORSConfiguration corsConfiguration) {
        this.corsConfiguration = corsConfiguration;
    }

    public String getMonetizationCategory() {
        return this.monetizationCategory;
    }

    public void setMonetizationCategory(String monetizationCategory) {
        this.monetizationCategory = monetizationCategory;
    }

    public String getApiLevelPolicy() {
        return apiLevelPolicy;
    }

    public void setApiLevelPolicy(String apiLevelPolicy) {
        this.apiLevelPolicy = apiLevelPolicy;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (StringUtils.isEmpty(type) || NULL_VALUE.equalsIgnoreCase(StringUtils.trim(type))) {
            this.type = "HTTP";
        } else {
            this.type = StringUtils.trim(type).toUpperCase();
        }
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getAccessControlRoles() {
        return accessControlRoles;
    }

    public void setAccessControlRoles(String accessControlRoles) {
        this.accessControlRoles = accessControlRoles;
    }

    public String getAccessControl() {
        return accessControl;
    }

    public void setAccessControl(String accessControl) {
        this.accessControl = accessControl;
    }

    public String getAuthorizationHeader() {
        return authorizationHeader;
    }

    public void setAuthorizationHeader(String authorizationHeader) {
        this.authorizationHeader = authorizationHeader;
    }

    /**
     * To set the gateway security for the relevant API.
     *
     * @param apiSecurity Relevant type of gateway security for the API.
     */
    public void setApiSecurity(String apiSecurity) {
        if (apiSecurity != null) {
            this.apiSecurity = apiSecurity;
        }
    }

    /**
     * To get the gateway level security specific to the relevant API.
     *
     * @return Relevant type of gateway security.
     */
    public String getApiSecurity() {
        return apiSecurity;
    }

    public String getWsdlArchivePath() {
        return wsdlArchivePath;
    }

    public void setWsdlArchivePath(String wsdlArchivePath) {
        this.wsdlArchivePath = wsdlArchivePath;
    }

    public ResourceFile getWsdlArchive() {
        return wsdlArchive;
    }

    public void setWsdlArchive(ResourceFile wsdlArchive) {
        this.wsdlArchive = wsdlArchive;
    }
}
