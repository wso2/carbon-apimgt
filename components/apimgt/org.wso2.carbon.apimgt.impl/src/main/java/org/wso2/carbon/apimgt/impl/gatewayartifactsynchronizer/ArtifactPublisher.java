package org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer;

import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;

public interface ArtifactPublisher {

    public void publishArtifacts (GatewayAPIDTO gatewayAPIDTO);

    public void updateArtifacts (GatewayAPIDTO gatewayAPIDTO);

    public void deleteArtifacts (GatewayAPIDTO gatewayAPIDTO);

}
