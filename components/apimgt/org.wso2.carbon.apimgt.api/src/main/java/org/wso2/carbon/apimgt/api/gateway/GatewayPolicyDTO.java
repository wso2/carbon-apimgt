package org.wso2.carbon.apimgt.api.gateway;

public class GatewayPolicyDTO {
    private GatewayContentDTO[] gatewayPolicySequenceToBeAdded;
    private String tenantDomain;

    public GatewayContentDTO[] getGatewayPolicySequenceToBeAdded() {
        return gatewayPolicySequenceToBeAdded;
    }

    public void setGatewayPolicySequenceToBeAdded(GatewayContentDTO[] gatewayPolicySequenceToBeAdded) {
        this.gatewayPolicySequenceToBeAdded = gatewayPolicySequenceToBeAdded;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }
}
