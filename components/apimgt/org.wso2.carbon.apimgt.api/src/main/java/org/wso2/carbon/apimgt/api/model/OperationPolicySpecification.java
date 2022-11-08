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
import java.util.Objects;

public class OperationPolicySpecification {

    public enum PolicyCategory {
        Mediation,
        Security
    }

    private PolicyCategory category = PolicyCategory.Mediation;
    private String name = null;
    private String version = "v1";
    private String displayName = null;
    private String description = null;
    private List<String> applicableFlows = new ArrayList<>();
    private List<String> supportedGateways = new ArrayList<>();
    private List<String> supportedApiTypes = new ArrayList<>();
    private List<OperationPolicySpecAttribute> policyAttributes = new ArrayList<>();

    public String getName() {

        return name;
    }

    public void setName(String name) {

        this.name = name;
    }

    public String getVersion() {

        return version;
    }

    public void setVersion(String version) {

        this.version = version;
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

    public void setPolicyAttributes(List<OperationPolicySpecAttribute> policyAttributes) {

        this.policyAttributes = policyAttributes;
    }

    public PolicyCategory getCategory() {

        return category;
    }

    public void setCategory(PolicyCategory category) {

        this.category = category;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o)
            return true;
        if (!(o instanceof OperationPolicySpecification))
            return false;
        OperationPolicySpecification policySpecObj = (OperationPolicySpecification) o;
        return category == policySpecObj.category && name.equals(policySpecObj.name)
                && displayName.equals(policySpecObj.displayName) && Objects.equals(description, policySpecObj.description)
                && applicableFlows.equals(policySpecObj.applicableFlows) && supportedGateways.equals(policySpecObj.supportedGateways)
                && supportedApiTypes.equals(policySpecObj.supportedApiTypes) && Objects.equals(policyAttributes,
                policySpecObj.policyAttributes);
    }

    @Override
    public int hashCode() {

        return Objects.hash(category, name, displayName, description, applicableFlows, supportedGateways,
                supportedApiTypes, policyAttributes);
    }
}
