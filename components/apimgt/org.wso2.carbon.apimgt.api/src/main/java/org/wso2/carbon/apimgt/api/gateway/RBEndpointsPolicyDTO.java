package org.wso2.carbon.apimgt.api.gateway;

import java.util.List;

public class RBEndpointsPolicyDTO {

    List<RBEndpointDTO> production;
    List<RBEndpointDTO> sandbox;
    Long suspendDuration;

    public List<RBEndpointDTO> getProduction() {

        return production;
    }

    public void setProduction(List<RBEndpointDTO> production) {

        this.production = production;
    }

    public List<RBEndpointDTO> getSandbox() {

        return sandbox;
    }

    public void setSandbox(List<RBEndpointDTO> sandbox) {

        this.sandbox = sandbox;
    }

    public Long getSuspendDuration() {

        return suspendDuration;
    }

    public void setSuspendDuration(Long suspendDuration) {

        this.suspendDuration = suspendDuration;
    }
}
