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

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.Set;

public class APIProduct {
    private APIProductIdentifier id;
    private String uuid;

    private String description;
    private String productTiers;
    private Date createdTime;
    private String createdUser;
    private Date updatedTime;
    private String updatedUser;
    private APIProductStatus status;
    private String thumbnailUrl;
    private String apiProductLevelPolicy;
    private Set<Tier> availableTiers = new LinkedHashSet<Tier>();
    private String monetizationCategory;
    private boolean isLatest;
    private Set<String> tags = new LinkedHashSet<String>();

    private String visibility;
    private String visibleRoles;
    private String visibleTenants;

    private String technicalOwner;
    private String technicalOwnerEmail;
    private String businessOwner;
    private String businessOwnerEmail;

    private String apiProductOwner;

    private String subscriptionAvailability;
    private String subscriptionAvailableTenants;

    public APIProduct(APIProductIdentifier id) {
        this.id = id;
    }

    public String getApiProductTier() {
        return apiProductTier;
    }

    public void setApiProductTier(String apiProductTier) {
        this.apiProductTier = apiProductTier;
    }

    private String apiProductTier;

    public String getUUID() {
        return uuid;
    }

    public void setUUID(String uuid) {
        this.uuid = uuid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getProductTiers() {
        return productTiers;
    }

    public void setProductTiers(String productTiers) {
        this.productTiers = productTiers;
    }

    public Date getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(Date createdTime) {
        this.createdTime = createdTime;
    }

    public String getCreatedUser() {
        return createdUser;
    }

    public void setCreatedUser(String createdUser) {
        this.createdUser = createdUser;
    }

    public Date getUpdatedTime() {
        return updatedTime;
    }

    public void setUpdatedTime(Date updatedTime) {
        this.updatedTime = updatedTime;
    }

    public String getUpdatedUser() {
        return updatedUser;
    }

    public void setUpdatedUser(String updatedUser) {
        this.updatedUser = updatedUser;
    }


    public APIProductStatus getStatus() {
        return status;
    }

    public void setStatus(APIProductStatus status) {
        this.status = status;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
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

    public String getSubscriptionAvailableTenants() {
        return subscriptionAvailableTenants;
    }

    public void setSubscriptionAvailableTenants(String subscriptionAvailableTenants) {
        this.subscriptionAvailableTenants = subscriptionAvailableTenants;
    }

    public String getApiProductLevelPolicy() {
        return apiProductLevelPolicy;
    }

    public void setApiProductLevelPolicy(String apiProductLevelPolicy) {
        this.apiProductLevelPolicy = apiProductLevelPolicy;
    }

    public APIProductIdentifier getId() {
        return id;
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
    public void removeAllTiers(){
        availableTiers.clear();
    }

    public void removeAvailableTiers(Set<Tier> availableTiers) {
        this.availableTiers.removeAll(availableTiers);
    }

    public String getMonetizationCategory() {
        return monetizationCategory;
    }

    public void setMonetizationCategory(String monetizationCategory) {
        this.monetizationCategory = monetizationCategory;
    }

    public boolean isLatest() {
        return isLatest;
    }

    public void setLatest(boolean latest) {
        isLatest = latest;
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

    public String getApiProductOwner() {
        return apiProductOwner;
    }

    public void setApiProductOwner(String apiProductOwner) {
        this.apiProductOwner = apiProductOwner;
    }
}
