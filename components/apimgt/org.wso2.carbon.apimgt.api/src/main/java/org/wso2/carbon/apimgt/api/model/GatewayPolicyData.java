/*
 * Copyright (c) 2023, WSO2 LLC. (https://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.api.model;

import java.util.List;
import java.util.Set;

/**
 * This class represent the gateway policy mapping data
 */
public class GatewayPolicyData {

    private String policyMappingId;
    private String policyMappingName;
    private String policyMappingDescription;
    private String organization;
    private List<OperationPolicy> gatewayPolicies;
    private Set<String> gatewayLabels;

    public String getPolicyMappingId() {
        return policyMappingId;
    }

    public void setPolicyMappingId(String policyMappingId) {
        this.policyMappingId = policyMappingId;
    }

    public String getPolicyMappingName() {
        return policyMappingName;
    }

    public void setPolicyMappingName(String policyMappingName) {
        this.policyMappingName = policyMappingName;
    }

    public String getPolicyMappingDescription() {
        return policyMappingDescription;
    }

    public void setPolicyMappingDescription(String policyMappingDescription) {
        this.policyMappingDescription = policyMappingDescription;
    }

    public String getOrganization() {
        return organization;
    }

    public void setOrganization(String organization) {
        this.organization = organization;
    }

    public List<OperationPolicy> getGatewayPolicies() {
        return gatewayPolicies;
    }

    public void setGatewayPolicies(List<OperationPolicy> gatewayPolicies) {
        this.gatewayPolicies = gatewayPolicies;
    }

    public Set<String> getGatewayLabels() {
        return gatewayLabels;
    }

    public void setGatewayLabels(Set<String> gatewayLabels) {
        this.gatewayLabels = gatewayLabels;
    }
}
