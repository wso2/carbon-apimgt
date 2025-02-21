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
import java.util.List;

/**
 * This class represents the default governance policy
 */
public class APIMDefaultGovPolicy {
    String name;
    String description;
    List<String> labels;
    List<String> governableStates;
    List<String> rulesetNames;

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

    public List<String> getLabels() {
        return new ArrayList<>(labels);
    }

    public void setLabels(List<String> labels) {
        this.labels = new ArrayList<>(labels);
    }

    public List<String> getGovernableStates() {
        return new ArrayList<>(governableStates);
    }

    public void setGovernableStates(List<String> governableStates) {
        this.governableStates = new ArrayList<>(governableStates);
    }

    public List<String> getRulesetNames() {
        return new ArrayList<>(rulesetNames);
    }

    public void setRulesetNames(List<String> rulesetNames) {
        this.rulesetNames = new ArrayList<>(rulesetNames);
    }
}
