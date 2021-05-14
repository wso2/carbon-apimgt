package org.wso2.carbon.apimgt.gatewaybridge.deployers;

import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;

/**
 * Class for deploying apis.
 */
public interface APIDeployer {

    /**
     * Deploys artifacts to other API gateways.
     * @param gatewayAPIDTO     the API DTO contains API details
     */
     void deployArtifacts(GatewayAPIDTO gatewayAPIDTO , String topic)
            throws Exception;

     void unDeployArtifacts(String artifactName)
            throws Exception;

}
