package org.wso2.carbon.apimgt.api.model;

import java.util.List;
import java.util.Set;

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
