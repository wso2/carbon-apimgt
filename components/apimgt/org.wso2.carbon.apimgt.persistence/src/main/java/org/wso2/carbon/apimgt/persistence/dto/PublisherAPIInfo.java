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

import java.util.Date;
import java.util.Map;
import java.util.Set;

/**
 * A subset of org.wso2.carbon.apimgt.persistence.models.PublisherAPI. Minimal API information required only for
 * listing APIs in publisher which are stored in the
 * persistence layer are included in this.
 */
public class PublisherAPIInfo {
    private String id;
    private String apiName;
    private String version;
    private String providerName;
    private String context;
    private String status;
    private String type;
    private String thumbnail; // thumbnail is not required for normal ApiGet
    private String createdTime;
    private Date updatedTime;
    private String audience;
    private Set<String> audiences;
    private Map<String, String> additionalProperties;
    private String description;
    private String gatewayVendor;
    private boolean advertiseOnly;
    private String updatedBy;
    private String businessOwner;
    private String businessOwnerEmail;
    private String technicalOwner;
    private String technicalOwnerEmail;
    private Boolean isMonetizationEnabled;

    public String getAudience() {
        return audience;
    }

    public void setAudience(String audience) {
        this.audience = audience;
    }

    public Set<String> getAudiences() {
        return audiences;
    }

    public void setAudiences(Set<String> audiences) {
        this.audiences = audiences;
    }

    public String getApiName() {
        return apiName;
    }

    public String getVersion() {
        return version;
    }

    public String getProviderName() {
        return providerName;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public void setApiName(String apiName) {
        this.apiName = apiName;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public void setProviderName(String providerName) {
        this.providerName = providerName;
    }

    public String getCreatedTime() { return createdTime; }

    public void setCreatedTime(String createdTime) { this.createdTime = createdTime; }

    public Date getUpdatedTime() { return updatedTime; }

    public void setUpdatedTime(Date updatedTime) { this.updatedTime = updatedTime; }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getGatewayVendor() {
        return gatewayVendor;
    }

    public void setGatewayVendor(String gatewayVendor) {
        this.gatewayVendor = gatewayVendor;
    }

    public boolean isAdvertiseOnly() {
        return advertiseOnly;
    }

    public void setAdvertiseOnly(boolean advertiseOnly) {
        this.advertiseOnly = advertiseOnly;
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

    public Boolean getMonetizationStatus() {
        return isMonetizationEnabled;
    }

    public void setMonetizationStatus(Boolean isMonetizationEnabled) {
        this.isMonetizationEnabled = isMonetizationEnabled;
    }

    @Override
    public String toString() {
        return "PublisherAPIInfo [id=" + id + ", apiName=" + apiName + ", description=" + description + ", version=" +
                version + ", providerName=" + providerName + ", context=" + context + ", status=" + status + ", type="
                + type + ", thumbnail=" + thumbnail + ", advertiseOnly=" + advertiseOnly + ", businessOwner=" +
                businessOwner + ", businessOwnerEmail=" + businessOwnerEmail + ", technicalOwner=" + technicalOwner +
                ", technicalOwnerEmail=" + technicalOwnerEmail + ", isMonetizationEnabled=" + isMonetizationEnabled +"]";
    }

    public Map<String, String> getAdditionalProperties() {
        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String, String> additionalProperties) {
        this.additionalProperties = additionalProperties;
    }
}
