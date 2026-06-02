/*
 * Copyright (c) 2026, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a Devportal Governance template used by the Admin Portal template wizard.
 */
public class DevportalGovernanceTemplate {

    public static final String STATUS_DRAFT = "DRAFT";
    public static final String STATUS_PUBLISHED = "PUBLISHED";

    private String id;
    private String name;
    private String description;
    private List<String> tags = new ArrayList<>();
    private String icon;
    private Map<String, Object> formConfig = new HashMap<>();
    private String formConfigHash;
    private String status = STATUS_DRAFT;
    private boolean isDefault;
    private boolean isGlobal;
    private String organization;
    private String createdBy;
    private String createdTime;
    private String updatedBy;
    private String updatedTime;
    private List<DevportalGovernanceRulesetBinding> rulesetBindings = new ArrayList<>();

    public String getId() {

        return id;
    }

    public void setId(String id) {

        this.id = id;
    }

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public List<String> getTags() {

        return new ArrayList<>(tags);
    }

    public void setTags(List<String> tags) {

        this.tags = tags == null ? new ArrayList<>() : new ArrayList<>(tags);
    }

    public String getIcon() {

        return icon;
    }

    public void setIcon(String icon) {

        this.icon = icon;
    }

    public Map<String, Object> getFormConfig() {

        return new HashMap<>(formConfig);
    }

    public void setFormConfig(Map<String, Object> formConfig) {

        if (formConfig == null) {
            this.formConfig = Collections.emptyMap();
        } else {
            this.formConfig = Collections.unmodifiableMap(new HashMap<>(formConfig));
        }
    }

    public String getFormConfigHash() {

        return formConfigHash;
    }

    public void setFormConfigHash(String formConfigHash) {

        this.formConfigHash = formConfigHash;
    }

    public String getStatus() {

        return status;
    }

    public void setStatus(String status) {

        this.status = status;
    }

    public boolean isDefault() {

        return isDefault;
    }

    public void setDefault(boolean aDefault) {

        isDefault = aDefault;
    }

    public boolean isGlobal() {

        return isGlobal;
    }

    public void setGlobal(boolean global) {

        isGlobal = global;
    }

    public String getOrganization() {

        return organization;
    }

    public void setOrganization(String organization) {

        this.organization = organization;
    }

    public String getCreatedBy() {

        return createdBy;
    }

    public void setCreatedBy(String createdBy) {

        this.createdBy = createdBy;
    }

    public String getCreatedTime() {

        return createdTime;
    }

    public void setCreatedTime(String createdTime) {

        this.createdTime = createdTime;
    }

    public String getUpdatedBy() {

        return updatedBy;
    }

    public void setUpdatedBy(String updatedBy) {

        this.updatedBy = updatedBy;
    }

    public String getUpdatedTime() {

        return updatedTime;
    }

    public void setUpdatedTime(String updatedTime) {

        this.updatedTime = updatedTime;
    }

    public List<DevportalGovernanceRulesetBinding> getRulesetBindings() {

        return new ArrayList<>(rulesetBindings);
    }

    public void setRulesetBindings(List<DevportalGovernanceRulesetBinding> rulesetBindings) {

        if (rulesetBindings == null) {
            this.rulesetBindings = Collections.emptyList();
        } else {
            this.rulesetBindings = Collections.unmodifiableList(new ArrayList<>(rulesetBindings));
        }
    }
}
