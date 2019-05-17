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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONObject;

public class APIProduct {
    // TODO add rest of the properties
    private String name;
    private String uuid;
    private int productId;
    private String provider;
    private String description;
    private Set<Tier> availableTiers = new LinkedHashSet<Tier>();
    private String visibility;
    private String visibleRoles;
    private String visibleTenants;
    private String subscriptionAvailability;
    private String subscriptionAvailableTenants;
    private String state;
    private String businessOwner;
    private String businessOwnerEmail;
    private String tenantDomain;
    private List<APIProductResource> productResources = new ArrayList<>();
    private String definition;
    private JSONObject additionalProperties = new JSONObject();
    
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    public String getProvider() {
        return provider;
    }
    public void setProvider(String provider) {
        this.provider = provider;
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
    
    @Override
    public String toString() {
        String tiers = "";
        for (Tier tier : availableTiers) {
            tiers += tier.getName() + " ";
        }
        return "APIProduct [name=" + name + ", uuid=" + uuid + ", productId=" + productId + ", provider=" + provider
                + ", description=" + description + ", availableTiers=" + tiers + ", visibility=" + visibility
                + ", visibleRoles=" + visibleRoles + ", visibleTenants=" + visibleTenants
                + ", subscriptionAvailability=" + subscriptionAvailability + ", subscriptionAvailableTenants="
                + subscriptionAvailableTenants + ", state=" + state + ", businessOwner=" + businessOwner
                + ", businessOwnerEmail=" + businessOwnerEmail + ", tenantDomain=" + tenantDomain
                + ", productResources=" + productResources + "]";
    }

}
