package org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer;

import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;

public interface ArtifactDeployer {

    public GatewayAPIDTO deployArtifact(String APIId, String APIName, String label);

}
