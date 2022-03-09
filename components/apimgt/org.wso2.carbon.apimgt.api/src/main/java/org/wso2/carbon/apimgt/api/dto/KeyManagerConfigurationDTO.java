/*
 * Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.api.dto;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *KeyManagerConfiguration model
 */
public class KeyManagerConfigurationDTO implements Serializable {


    private static final long serialVersionUID = 1L;

    private String name;
    private String uuid;
    private String displayName;
    private String description;
    private String organization;
    private Map<String,Object> additionalProperties = new HashMap();
    private Map<String,String> endpoints = new HashMap<>();
    private String type;
    private boolean enabled;
    private String tokenType;
    private String externalReferenceId = null;
    private String alias = null;

    public KeyManagerConfigurationDTO() {

    }

    public KeyManagerConfigurationDTO(KeyManagerConfigurationDTO keyManagerConfigurationDTO) {

        this.name = keyManagerConfigurationDTO.getName();
        this.uuid = keyManagerConfigurationDTO.getUuid();
        this.displayName = keyManagerConfigurationDTO.getDisplayName();
        this.description = keyManagerConfigurationDTO.getDescription();
        this.organization = keyManagerConfigurationDTO.getOrganization();
        this.additionalProperties = new HashMap<>(keyManagerConfigurationDTO.getAdditionalProperties());
        this.type = keyManagerConfigurationDTO.getType();
        this.enabled = keyManagerConfigurationDTO.isEnabled();
        this.tokenType = keyManagerConfigurationDTO.getTokenType();
        this.externalReferenceId = keyManagerConfigurationDTO.getExternalReferenceId();
        this.endpoints = keyManagerConfigurationDTO.getEndpoints();
    }
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

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public String getOrganization() {

        return organization;
    }

    public void setOrganization(String organization) {

        this.organization = organization;
    }

    public Map<String,Object> getAdditionalProperties() {

        return additionalProperties;
    }

    public void setAdditionalProperties(Map<String,Object> additionalProperties) {

        this.additionalProperties = additionalProperties;
    }

    public String getType() {

        return type;
    }

    public void setType(String type) {

        this.type = type;
    }

    public boolean isEnabled() {

        return enabled;
    }

    public String getDisplayName() {

        return displayName;
    }

    public void setDisplayName(String displayName) {

        this.displayName = displayName;
    }

    public void setEnabled(boolean enabled) {

        this.enabled = enabled;
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getExternalReferenceId() {
        return externalReferenceId;
    }

    public void setExternalReferenceId(String externalReferenceId) {
        this.externalReferenceId = externalReferenceId;
    }

    public void setTokenType(String tokenType) {
        this.tokenType = tokenType;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void addProperty(String key,Object value){
        additionalProperties.put(key,value);
    }
    public Object getProperty(String key){
        return additionalProperties.get(key);
    }
    public void removeProperty(String key){
        additionalProperties.remove(key);
    }

    public Map<String, String> getEndpoints() {

        return endpoints;
    }

    public void setEndpoints(Map<String, String> endpoints) {

        this.endpoints = endpoints;
    }
}
