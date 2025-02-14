/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.governance.api.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * This class represents a governance Policy
 */
public class APIMGovernancePolicy {
    private String id;
    private String name;
    private String description;
    private List<String> rulesetIds;
    private List<String> labels;
    private List<APIMGovernableState> apimGovernableStates;
    private List<APIMGovernanceAction> actions;
    private String createdBy;
    private String createdTime;
    private String updatedBy;
    private String updatedTime;
    private boolean isGlobal = false;

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

    public List<String> getRulesetIds() {

        return new ArrayList<>(rulesetIds);
    }

    public void setRulesetIds(List<String> rulesetIds) {

        this.rulesetIds = Collections.unmodifiableList(new ArrayList<>(rulesetIds));
    }

    public List<String> getLabels() {

        return new ArrayList<>(labels);
    }

    public void setLabels(List<String> labels) {

        this.labels = Collections.unmodifiableList(new ArrayList<>(labels));
    }

    public List<APIMGovernableState> getGovernableStates() {

        return new ArrayList<>(apimGovernableStates);
    }

    public void setGovernableStates(List<APIMGovernableState> apimGovernableStates) {

        this.apimGovernableStates = Collections.unmodifiableList(new ArrayList<>(apimGovernableStates));
    }

    public List<APIMGovernanceAction> getActions() {

        return new ArrayList<>(actions);
    }

    public void setActions(List<APIMGovernanceAction> actions) {

        this.actions = Collections.unmodifiableList(new ArrayList<>(actions));
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

    public boolean isGlobal() {
        return isGlobal;
    }

    public void setGlobal(boolean global) {
        isGlobal = global;
    }
}
