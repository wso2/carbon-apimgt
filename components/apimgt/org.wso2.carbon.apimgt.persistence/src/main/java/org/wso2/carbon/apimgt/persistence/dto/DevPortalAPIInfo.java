/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.wso2.carbon.apimgt.persistence.dto;

import java.util.Set;

/**
 * A subset of org.wso2.carbon.apimgt.persistence.models.DevPortalAPI. Minimal API information required only for
 * listing
 * APIs in DevPortal which are stored in the persistence layer are included in this.
 */
public class DevPortalAPIInfo {
    private String id;
    private String apiName;
    private String version;
    private String providerName;
    private String context;
    private String type;
    private String thumbnail;
    private String businessOwner;
    private String status;
    private Set<String> availableTierNames;
    private String subscriptionAvailability; 
    private String subscriptionAvailableOrgs;
    private String createdTime;
    private String description;
    private String gatewayVendor;

    public void setGatewayVendor(String gatewayVendor) {
        this.gatewayVendor = gatewayVendor;
    }

    public String getGatewayVendor() {
        return gatewayVendor;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }
    //monetizationCategory which is returned to UI as 'monetizationLabel' is not required. it is derived from the
    // attached tiers.
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getApiName() {
        return apiName;
    }
    public void setApiName(String apiName) {
        this.apiName = apiName;
    }
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }
    public String getProviderName() {
        return providerName;
    }
    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }
    public String getContext() {
        return context;
    }
    public void setContext(String context) {
        this.context = context;
    }
    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }
    public String getThumbnail() {
        return thumbnail;
    }
    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
    public String getBusinessOwner() {
        return businessOwner;
    }
    public void setBusinessOwner(String businessOwner) {
        this.businessOwner = businessOwner;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public Set<String> getAvailableTierNames() {
        return availableTierNames;
    }
    public void setAvailableTierNames(Set<String> availableTierNames) {
        this.availableTierNames = availableTierNames;
    }
    public String getSubscriptionAvailableOrgs() {
        return subscriptionAvailableOrgs;
    }
    public void setSubscriptionAvailableOrgs(String subscriptionAvailableOrgs) {
        this.subscriptionAvailableOrgs = subscriptionAvailableOrgs;
    }
    public String getSubscriptionAvailability() {
        return subscriptionAvailability;
    }
    public void setSubscriptionAvailability(String subscriptionAvailability) {
        this.subscriptionAvailability = subscriptionAvailability;
    }

    @Override
    public String toString() {
        return "DevPortalAPIInfo [id=" + id + ", apiName=" + apiName + ", description=" + description + ", version="
                + version + ", providerName=" + providerName + ", context=" + context + ", type=" + type + ", " +
                "thumbnail=" + thumbnail + ", businessOwner=" + businessOwner + ", status=" + status + ", " +
                "availableTierNames=" + availableTierNames + ", subscriptionAvailability=" + subscriptionAvailability
                + ", subscriptionAvailableOrgs=" + subscriptionAvailableOrgs + "]";
    }

}
