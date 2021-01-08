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

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * 
 * Represents the API information stored in persistence layer, that is used for publisher operations
 **/
public class PublisherAPIProduct extends PublisherAPIProductInfo {

    private String description;
    private String visibility;
    private String visibleRoles;
    private String visibleOrganizations; //visibleTenants   
    private String technicalOwner;
    private String technicalOwnerEmail;
    private String businessOwner;
    private String businessOwnerEmail;
    private String subscriptionAvailability; // e.g. "CURRENT_TENANT";who is allowed for subscriptions
    private String subscriptionAvailableOrgs; // subscriptionAvailableTenants;    
    private int cacheTimeout;    
    private Set<String> availableTierNames;    
    private Set<String> environments;    
    private String transports;    
    private CORSConfiguration corsConfiguration;    
    private String authorizationHeader;  
    private String apiSecurity;
    private String contextTemplate;
    private boolean enableSchemaValidation;
    private boolean isMonetizationEnabled;
    private Map<String, String> monetizationProperties = new JSONObject();
    private Set<String> apiCategories;
    private String definition;
    private boolean enableStore;
    private String thumbnail;
    private String createdTime;
    private String lastUpdated;
    private Set<String> tags = new LinkedHashSet<>();
    private String accessControl; // publisher accessControl : 'restricted', 'all'
    private Set<String> accessControlRoles; // reg has a just String
    
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
    public int getCacheTimeout() {
        return cacheTimeout;
    }
    public void setCacheTimeout(int cacheTimeout) {
        this.cacheTimeout = cacheTimeout;
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
    public String getTransports() {
        return transports;
    }
    public void setTransports(String transports) {
        this.transports = transports;
    }
    public CORSConfiguration getCorsConfiguration() {
        return corsConfiguration;
    }
    public void setCorsConfiguration(CORSConfiguration corsConfiguration) {
        this.corsConfiguration = corsConfiguration;
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
    public String getContextTemplate() {
        return contextTemplate;
    }
    public void setContextTemplate(String contextTemplate) {
        this.contextTemplate = contextTemplate;
    }
    public boolean isEnableSchemaValidation() {
        return enableSchemaValidation;
    }
    public void setEnableSchemaValidation(boolean enableSchemaValidation) {
        this.enableSchemaValidation = enableSchemaValidation;
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
    public Set<String> getApiCategories() {
        return apiCategories;
    }
    public void setApiCategories(Set<String> apiCategories) {
        this.apiCategories = apiCategories;
    }
    public String getDefinition() {
        return definition;
    }
    public void setDefinition(String definition) {
        this.definition = definition;
    }
    public boolean isEnableStore() {
        return enableStore;
    }
    public void setEnableStore(boolean enableStore) {
        this.enableStore = enableStore;
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
    
    
    /*
    private String inSequence;
    private String outSequence;
    private String faultSequence;
    private String responseCache;
    private String redirectURL;  // check ??
    private String apiOwner;
    private boolean advertiseOnly;
    private String endpointConfig;
    private String implementation;
    private String productionMaxTps;
    private String sandboxMaxTps;
    private String testKey;
    private Set<String> gatewayLabels;
    private List<String> keyManagers = new ArrayList<>();
    private Set<DeploymentEnvironments> deploymentEnvironments;

    private Map<String, String> additionalProperties;
    */

}
