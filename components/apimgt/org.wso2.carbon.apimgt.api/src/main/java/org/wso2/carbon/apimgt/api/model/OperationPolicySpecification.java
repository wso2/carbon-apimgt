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

    private PolicyCategory policyCategory = PolicyCategory.Mediation;
    private String policyName;
    private String policyDisplayName;
    private String policyDescription;
    private List<String> applicableFlows = new ArrayList<>();
    private List<String> supportedGateways = new ArrayList<>();
    private List<String> supportedApiTypes = new ArrayList<>();
    private List<OperationPolicySpecAttribute> policyAttributes = new ArrayList<>();
    private boolean multipleAllowed;

    public String getPolicyName() {

        return policyName;
    }

    public void setPolicyName(String policyName) {

        this.policyName = policyName;
    }

    public String getPolicyDisplayName() {

        return policyDisplayName;
    }

    public void setPolicyDisplayName(String policyDisplayName) {

        this.policyDisplayName = policyDisplayName;
    }

    public String getPolicyDescription() {

        return policyDescription;
    }

    public void setPolicyDescription(String policyDescription) {

        this.policyDescription = policyDescription;
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

    public PolicyCategory getPolicyCategory() {

        return policyCategory;
    }

    public void setPolicyCategory(PolicyCategory policyCategory) {

        this.policyCategory = policyCategory;
    }

    public boolean isMultipleAllowed() {

        return multipleAllowed;
    }

    public void setMultipleAllowed(boolean multipleAllowed) {

        this.multipleAllowed = multipleAllowed;
    }
}
