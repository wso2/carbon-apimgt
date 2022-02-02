/*
 * Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public class OperationPolicySpecification {

    public enum PolicyCategory {
        Mediation,
        Security
    }

    private PolicyCategory category = PolicyCategory.Mediation;
    private String name;
    private String displayName;
    private String description;
    private List<String> applicableFlows = new ArrayList<>();
    private List<String> supportedGateways = new ArrayList<>();
    private List<String> supportedApiTypes = new ArrayList<>();
    private List<OperationPolicySpecAttribute> policyAttributes = new ArrayList<>();
    private boolean multipleAllowed;

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getDisplayName() {

        return displayName;
    }

    public void setDisplayName(String displayName) {

        this.displayName = displayName;
    }

    public String getDescription() {

        return description;
    }

    public void setDescription(String description) {

        this.description = description;
    }

    public List<String> getApplicableFlows() {

        return applicableFlows;
    }

    public void setApplicableFlows(List<String> applicableFlows) {

        this.applicableFlows = applicableFlows;
    }

    public List<String> getSupportedGateways() {

        return supportedGateways;
    }

    public void setSupportedGateways(List<String> supportedGateways) {

        this.supportedGateways = supportedGateways;
    }

    public List<String> getSupportedApiTypes() {

        return supportedApiTypes;
    }

    public void setSupportedApiTypes(List<String> supportedApiTypes) {

        this.supportedApiTypes = supportedApiTypes;
    }

    public List<OperationPolicySpecAttribute> getPolicyAttributes() {

        return policyAttributes;
    }

    public void setPolicyAttributes(
            List<OperationPolicySpecAttribute> policyAttributes) {

        this.policyAttributes = policyAttributes;
    }

    public PolicyCategory getCategory() {

        return category;
    }

    public void setCategory(PolicyCategory category) {

        this.category = category;
    }

    public boolean isMultipleAllowed() {

        return multipleAllowed;
    }

    public void setMultipleAllowed(boolean multipleAllowed) {

        this.multipleAllowed = multipleAllowed;
    }
}
