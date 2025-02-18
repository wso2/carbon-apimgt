package org.wso2.carbon.apimgt.impl.dto;

import org.wso2.carbon.apimgt.api.model.GatewayDeployer;

public class GatewayDto {
    private String name;
    private GatewayDeployer gatewayDeployer;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public GatewayDeployer getGatewayDeployer() {
        return gatewayDeployer;
    }

    public void setGatewayDeployer(GatewayDeployer gatewayDeployer) {
        this.gatewayDeployer = gatewayDeployer;
    }
}
