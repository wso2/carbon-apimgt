package org.wso2.carbon.apimgt.gateway;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.gateway.service.APIGatewayAdmin;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;

/**
 * This class contains the methods used to retrieve artifacts from a storage and deploy and undeploy the API in gateway
 * */
public class InMemoryAPIDeployer {

    private static Log log = LogFactory.getLog(InMemoryAPIDeployer.class);
    APIGatewayAdmin apiGatewayAdmin;

    public InMemoryAPIDeployer() {

        apiGatewayAdmin = new APIGatewayAdmin();
    }

    /**
     * Deploy an API in the gateway using the deployAPI method in gateway admin
     *
     * @param apiId        - UUID of the API
     * @param gatewayLabel - Label of the Gateway
     * @return True if API artifact retrieved from the storage and successfully deployed without any error. else false
     */
    public boolean deployAPI(String apiId, String gatewayLabel) {

        GatewayArtifactSynchronizerProperties gatewayArtifactSynchronizerProperties = ServiceReferenceHolder
                .getInstance().getAPIManagerConfiguration().getGatewayArtifactSynchronizerProperties();

        if (gatewayArtifactSynchronizerProperties.getGatewayLabels().contains(gatewayLabel)) {
            try {
                GatewayAPIDTO gatewayAPIDTO = ServiceReferenceHolder.getInstance().getArtifactRetriever()
                        .retrieveArtifact(apiId, gatewayLabel);
                apiGatewayAdmin.deployAPI(gatewayAPIDTO);
                return true;
            } catch (AxisFault | ArtifactSynchronizerException axisFault) {
                log.error("Error deploying " + apiId + " in Gateway");
            }
        }
        return false;
    }

    /**
     * UnDeploy an API in the gateway using the uneployAPI method in gateway admin
     *
     * @param apiId        - UUID of the API
     * @param gatewayLabel - Label of the Gateway
     * @return True if API artifact retrieved from the storage and successfully undeployed without any error. else false
     */
    public boolean unDeployAPI(String apiId, String gatewayLabel) {

        GatewayArtifactSynchronizerProperties gatewayArtifactSynchronizerProperties =
                ServiceReferenceHolder.getInstance()
                        .getAPIManagerConfiguration().getGatewayArtifactSynchronizerProperties();

        if (gatewayArtifactSynchronizerProperties.getGatewayLabels().contains(gatewayLabel)) {
            try {
                GatewayAPIDTO gatewayAPIDTO = ServiceReferenceHolder.getInstance().getArtifactRetriever()
                        .retrieveArtifact(apiId, gatewayLabel);
                apiGatewayAdmin.unDeployAPI(gatewayAPIDTO);
                return true;
            } catch (AxisFault | ArtifactSynchronizerException axisFault) {
                log.error("Error undeploying " + apiId + " in Gateway");
            }
        }
        return false;
    }

    /**
     * Retrieve artifacts from the storage
     *
     * @param apiId        - UUID of the API
     * @param gatewayLabel - Label of the Gateway
     * @return DTO Object that contains the information and artifacts of the API for the given label
     */
    public GatewayAPIDTO getAPIArtifact(String apiId, String gatewayLabel) {

        GatewayArtifactSynchronizerProperties gatewayArtifactSynchronizerProperties =
                ServiceReferenceHolder.getInstance()
                        .getAPIManagerConfiguration().getGatewayArtifactSynchronizerProperties();

        GatewayAPIDTO gatewayAPIDTO = null;

        if (gatewayArtifactSynchronizerProperties.getGatewayLabels().contains(gatewayLabel)) {
            try {
                gatewayAPIDTO = ServiceReferenceHolder.getInstance().getArtifactRetriever()
                        .retrieveArtifact(apiId, gatewayLabel);
            } catch (ArtifactSynchronizerException axisFault) {
                log.error("Error retrieving artifacts of " + apiId + " from storage");
            }
        }

        return gatewayAPIDTO;
    }

}
