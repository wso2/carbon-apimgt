/*
 *
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
 *
 */

package org.wso2.carbon.apimgt.api.model;

import java.io.InputStream;

/**
 * Endpoint Registry Entry Object.
 */
public class EndpointRegistryEntry {

    private String entryUUID = null;

    private int registryId;

    private String name = null;

    private String productionServiceURL = null;

    private String sandboxServiceUrl = null;

    private String serviceType = null;

    private String serviceCategory = null;

    private String definitionType = null;

    private String definitionURL = null;

    private InputStream endpointDefinition = null;

    private String metaData = null;

    private String owner = null;

    private String updatedBy = null;

    private String createdTime = null;

    private String lastUpdatedTime = null;

    public String getEntryId() {

        return entryUUID;
    }

    public int getRegistryId() {

        return registryId;
    }

    public String getName() {

        return name;
    }

    public String getProductionServiceURL() {

        return productionServiceURL;
    }

    public String getServiceType() {

        return serviceType;
    }

    public String getDefinitionType() {

        return definitionType;
    }

    public String getDefinitionURL() {

        return definitionURL;
    }

    public InputStream getEndpointDefinition() {

        return endpointDefinition;
    }

    public String getMetaData() {

        return metaData;
    }

    public void setEntryId(String entryUUID) {

        this.entryUUID = entryUUID;
    }

    public void setRegistryId(int registryId) {

        this.registryId = registryId;
    }

    public void setName(String name) {

        this.name = name;
    }

    public void setProductionServiceURL(String productionServiceURL) {

        this.productionServiceURL = productionServiceURL;
    }

    public void setServiceType(String serviceType) {

        this.serviceType = serviceType;
    }

    public void setDefinitionType(String definitionType) {

        this.definitionType = definitionType;
    }

    public void setDefinitionURL(String definitionURL) {

        this.definitionURL = definitionURL;
    }

    public void setEndpointDefinition(InputStream endpointDefinition) {

        this.endpointDefinition = endpointDefinition;
    }

    public void setMetaData(String metaData) {

        this.metaData = metaData;
    }

    public String getServiceCategory() {

        return serviceCategory;
    }

    public void setServiceCategory(String serviceCategory) {

        this.serviceCategory = serviceCategory;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {
        this.updatedBy = updatedBy;
    }

    public String getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(String createdTime) {
        this.createdTime = createdTime;
    }

    public String getLastUpdatedTime() {
        return lastUpdatedTime;
    }

    public void setLastUpdatedTime(String lastUpdatedTime) {
        this.lastUpdatedTime = lastUpdatedTime;
    }

    public String getSandboxServiceUrl() {

        return sandboxServiceUrl;
    }

    public void setSandboxServiceUrl(String sandboxServiceUrl) {

        this.sandboxServiceUrl = sandboxServiceUrl;
    }
}
