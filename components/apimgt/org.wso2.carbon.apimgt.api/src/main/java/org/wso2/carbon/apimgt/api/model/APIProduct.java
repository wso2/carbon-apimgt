/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONObject;

public class APIProduct {
    // TODO add rest of the properties
    private APIProductIdentifier id;
    private String uuid;
    private String type;
    private int productId;
    private String context;
    private String description;
    private Set<Tier> availableTiers = new LinkedHashSet<Tier>();
    private String productLevelPolicy;
    private Set<String> tags = new LinkedHashSet<String>();
    private String contextTemplate;
    private String visibility;
    private String visibleRoles;
    private String visibleTenants;
    private String subscriptionAvailability;
    private String subscriptionAvailableTenants;
    private String state;
    private String businessOwner;
    private String businessOwnerEmail;
    private String technicalOwner;
    private String technicalOwnerEmail;

    private String tenantDomain;
    private String rating;
    private List<APIProductResource> productResources = new ArrayList<>();
    private String definition;
    private JSONObject additionalProperties = new JSONObject();
    private Set<String> environments = new HashSet<>();
    private List<Label> gatewayLabels = new ArrayList<>();
    private boolean enableSchemaValidation = false;
    private Set<Scope> scopes = new HashSet<>();

    private JSONObject monetizationProperties = new JSONObject();
    private boolean isMonetizationEnabled = false;

    /**
     * API security at the gateway level.
     */
    private String apiSecurity = "oauth2";
    private static final String NULL_VALUE = "NULL";
    private static final String API_PRODUCT = "APIProduct";

    private String transports;
    private String responseCache;
    private int cacheTimeout;
    private String thumbnailUrl;

    /**
     * Used for keeping Production & Sandbox Throttling limits.
     */
    private String productionMaxTps;
    private String sandboxMaxTps;

    /**
     * Custom authorization header specific to the API
     */
    private String authorizationHeader;

    private CORSConfiguration corsConfiguration;

    /**
     * Publisher access control related parameters.
     * AccessControl -> Specifies whether that particular API Product is restricted to certain set of publishers and creators.
     * AccessControlRoles -> Specifies the roles that the particular API Product is visible to.
     */
    private String accessControl;
    private String accessControlRoles;

    private List<APICategory> apiCategories;

    private Date lastUpdated;
    private Date createdTime;

    public APIProduct(){}

    public APIProduct(APIProductIdentifier id) {
        this.id = id;
    }

    /*todo : temporary method until proper constructer is added*/
    public void setID(APIProductIdentifier id) {
        this.id = id;
    }
    public APIProductIdentifier getId() {
        return id;
    }
    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    public List<APIProductResource> getProductResources() {
        return productResources;
    }
    public void setProductResources(List<APIProductResource> productResources) {
        this.productResources = productResources;
    }
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    public String getVisibility() {
        return visibility;
    }
    public void setVisibility(String visibility) {
        this.visibility = visibility;
    }
    public String getSubscriptionAvailability() {
        return subscriptionAvailability;
    }
    public void setSubscriptionAvailability(String subscriptionAvailability) {
        this.subscriptionAvailability = subscriptionAvailability;
    }
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        if (StringUtils.isEmpty(type) || NULL_VALUE.equalsIgnoreCase(StringUtils.trim(type))) {
            this.type = API_PRODUCT;
        } else {
            this.type = type;
        }
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
    public int getProductId() {
        return productId;
    }
    public JSONObject getMonetizationProperties() {
        return monetizationProperties;
    }
    public boolean getMonetizationStatus() {
        return isMonetizationEnabled;
    }
    public void setMonetizationStatus(boolean monetizationStatus) {
        this.isMonetizationEnabled = monetizationStatus;
    }
    public void setMonetizationProperties(JSONObject monetizationProperties) {
        this.monetizationProperties = monetizationProperties;
    }
    public void addMonetizationProperty(String key, String value) {
        monetizationProperties.put(key, value);
    }
    public void setProductId(int productId) {
        this.productId = productId;
    }
    public Set<Tier> getAvailableTiers() {
        return availableTiers;
    }
    public void setAvailableTiers(Set<Tier> availableTiers) {
        this.availableTiers = availableTiers;
    }
    public String getSubscriptionAvailableTenants() {
        return subscriptionAvailableTenants;
    }
    public void setSubscriptionAvailableTenants(String subscriptionAvailableTenants) {
        this.subscriptionAvailableTenants = subscriptionAvailableTenants;
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
    public String getTenantDomain() {
        return tenantDomain;
    }
    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }
    public String getDefinition() {
        return definition;
    }
    public void setDefinition(String definition) {
        this.definition = definition;
    }

    public Set<String> getEnvironments() {
        return environments;
    }

    public void setEnvironments(Set<String> environments) {
        this.environments = environments;
    }

    /**
     * To get the additional properties
     *
     * @return additional properties of the API Product
     */
    public JSONObject getAdditionalProperties() {
        return additionalProperties;
    }

    /**
     * To assign a set of customized properties to the API Product
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

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getTransports() {
        return transports;
    }

    public void setTransports(String transports) {
        this.transports = transports;
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

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setAccessControl(String accessControl) {
        this.accessControl = accessControl;
    }

    public String getAccessControl() {
        return accessControl;
    }

    public void setAccessControlRoles(String accessControlRoles) {
        this.accessControlRoles = accessControlRoles;
    }

    public String getAccessControlRoles() {
        return accessControlRoles;
    }

    /**
     * Check the status of the Json schema validation property.
     *
     * @return Status of the validator property.
     */
    public boolean isEnabledSchemaValidation() {
        return enableSchemaValidation;
    }

    /**
     * To set the JSON schema validation enable/disable.
     *
     * @param enableSchemaValidation Given Status.
     */
    public void setEnableSchemaValidation(boolean enableSchemaValidation) {
        this.enableSchemaValidation = enableSchemaValidation;
    }

    public String getApiSecurity() {
        return apiSecurity;
    }

    public void setApiSecurity(String apiSecurity) {
        this.apiSecurity = apiSecurity;
    }

    public String getAuthorizationHeader() {
        return authorizationHeader;
    }

    public void setAuthorizationHeader(String authorizationHeader) {
        this.authorizationHeader = authorizationHeader;
    }

    public CORSConfiguration getCorsConfiguration() {
        return corsConfiguration;
    }

    public void setCorsConfiguration(CORSConfiguration corsConfiguration) {
        this.corsConfiguration = corsConfiguration;
    }

    public Set<Scope> getScopes() {
        return scopes;
    }

    public void setScopes(Set<Scope> scopes) {
        this.scopes = scopes;
    }

    public void addScope(Scope scope) {
        this.scopes.add(scope);
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setContextTemplate(String contextTemplate) {
        this.contextTemplate = contextTemplate;
    }

    public String getContextTemplate() {
        return contextTemplate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof APIProduct)) {
            return false;
        }

        APIProduct that = (APIProduct) o;
        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        String tiers = "";
        for (Tier tier : availableTiers) {
            tiers += tier.getName() + " ";
        }
        String env = "";
        for (String environment : environments ) {
            env += environment + " ";
        }
        return "APIProduct [name=" + id.getName() + ", version=" + id.getVersion() + ", uuid=" + uuid + ", productId="
                + productId + ", provider=" + id.getProviderName() + ", description=" + description
                + ", availableTiers=" + tiers + ", visibility=" + visibility + ", visibleRoles=" + visibleRoles
                + ", visibleTenants=" + visibleTenants + ", environments=" + env + ", subscriptionAvailability="
                + subscriptionAvailability + ", subscriptionAvailableTenants=" + subscriptionAvailableTenants
                + ", accessControl=" + accessControl + ", accessControlRoles=" + accessControlRoles + ", state=" + state
                + ", businessOwner=" + businessOwner + ", businessOwnerEmail=" + businessOwnerEmail + ", tenantDomain="
                + tenantDomain + ", productResources=" + productResources + "]";
    }

    public List<Label> getGatewayLabels() {
        return gatewayLabels;
    }

    public void setGatewayLabels(List<Label> gatewayLabels) {
        this.gatewayLabels = gatewayLabels;
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

    public String getRating() {
        return rating;
    }

    public void setRating(String rating) {
        this.rating = rating;
    }

    public String getProductLevelPolicy() {
        return productLevelPolicy;
    }

    public void setProductLevelPolicy(String productLevelPolicy) {
        this.productLevelPolicy = productLevelPolicy;
    }

    /**
     * Removes all Tiers from the API object.
     */
    public void removeAllTiers() {
        availableTiers.clear();
    }

    public void removeAvailableTiers(Set<Tier> availableTiers) {
        this.availableTiers.removeAll(availableTiers);
    }

    public void setApiCategories(List<APICategory> apiCategories) {
        this.apiCategories = apiCategories;
    }

    public List<APICategory> getApiCategories() {
        return apiCategories;
    }

}
