package org.wso2.carbon.apimgt.api.model;

public class GatewayPolicyDeployment {
    private String mappingUuid;
    private String gatewayLabel;

    public String getMappingUuid() {
        return mappingUuid;
    }

    public void setMappingUuid(String mappingUuid) {
        this.mappingUuid = mappingUuid;
    }

    public String getGatewayLabel() {
        return gatewayLabel;
    }

    public void setGatewayLabel(String gatewayLabel) {
        this.gatewayLabel = gatewayLabel;
    }
}
