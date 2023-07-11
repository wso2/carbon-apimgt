package org.wso2.carbon.apimgt.api.gateway;

public class GatewayPolicyDTO {
    private GatewayContentDTO[] gatewayPolicySequenceToBeAdd;
    private String tenantDomain;

    public GatewayContentDTO[] getGatewayPolicySequenceToBeAdd() {
        return gatewayPolicySequenceToBeAdd;
    }

    public void setGatewayPolicySequenceToBeAdd(GatewayContentDTO[] gatewayPolicySequenceToBeAdd) {
        this.gatewayPolicySequenceToBeAdd = gatewayPolicySequenceToBeAdd;
    }

    public String getTenantDomain() {
        return tenantDomain;
    }

    public void setTenantDomain(String tenantDomain) {
        this.tenantDomain = tenantDomain;
    }
}
