package org.wso2.carbon.apimgt.api.gateway;

public class GatewayPolicyDTO {
    private GatewayContentDTO[] gatewayPolicySequenceToBeAdd;

    public GatewayContentDTO[] getGatewayPolicySequenceToBeAdd() {
        return gatewayPolicySequenceToBeAdd;
    }

    public void setGatewayPolicySequenceToBeAdd(GatewayContentDTO[] gatewayPolicySequenceToBeAdd) {
        this.gatewayPolicySequenceToBeAdd = gatewayPolicySequenceToBeAdd;
    }
}
