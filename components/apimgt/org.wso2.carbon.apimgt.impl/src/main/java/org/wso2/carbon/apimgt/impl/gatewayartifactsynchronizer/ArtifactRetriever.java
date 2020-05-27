package org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer;

import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;

public interface ArtifactRetriever {

    public GatewayAPIDTO retrieveArtifacts (String APIId, String APIName, String label);

}
