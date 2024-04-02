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

import java.util.Set;

/**
 * A subset of org.wso2.carbon.apimgt.persistence.models.PublisherAPIProduct. Minimal API information required only for
 * listing API Products in publisher which are stored in the
 * persistence layer are included in this.
 */
public class PublisherAPIProductInfo {
    private String id;
    private String apiProductName;
    private String version;
    private String providerName;
    private String context;
    private String state;
    private String type;
    private String apiSecurity;
    private String thumbnail;
    private String businessOwner;
    private String businessOwnerEmail;
    private String technicalOwner;
    private String technicalOwnerEmail;
    private Boolean isMonetizationEnabled;
    private Set<String> audiences;

    public Set<String> getAudiences() {
        return audiences;
    }
    public void setAudiences(Set<String> audiences) {
        this.audiences = audiences;
    }
    public String getThumbnail() {
        return thumbnail;
    }
    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getApiProductName() {
        return apiProductName;
    }
    public void setApiProductName(String apiProductName) {
        this.apiProductName = apiProductName;
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
    public String getState() {
        return state;
    }
    public void setState(String state) {
        this.state = state;
    }
    public String getApiSecurity() {
        return apiSecurity;
    }
    public void setApiSecurity(String apiSecurity) {
        this.apiSecurity = apiSecurity;
    }
    private String gatewayVendor;
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
}
