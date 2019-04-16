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
import java.util.List;

public class APIProduct {
    // TODO add rest of the properties
    private String name;
    private String uuid;
    private String provider;
    private String description;
    private String productTier;
    private String visibility;
    private String subscriptionAvailability;
    private String state;
    private String businessOwner;
    private String businessOwnerEmail;
    
    //private String thumbnailImage
    
    private List<APIProductResource> productResources = new ArrayList<>();
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
    public String getProductTier() {
        return productTier;
    }
    public void setProductTier(String productTier) {
        this.productTier = productTier;
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

}
