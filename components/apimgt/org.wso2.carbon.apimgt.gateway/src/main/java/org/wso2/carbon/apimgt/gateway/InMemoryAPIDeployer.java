package org.wso2.carbon.apimgt.gateway;

import org.apache.axis2.AxisFault;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.gateway.service.APIGatewayAdmin;
import org.wso2.carbon.apimgt.impl.dto.GatewayArtifactSynchronizerProperties;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.exception.ArtifactSynchronizerException;
import org.wso2.carbon.apimgt.gateway.internal.ServiceReferenceHolder;


public class InMemoryAPIDeployer {

    private static Log log = LogFactory.getLog(InMemoryAPIDeployer.class);
    APIGatewayAdmin apiGatewayAdmin;

    public InMemoryAPIDeployer() {

        apiGatewayAdmin = new APIGatewayAdmin();
    }


    public boolean deployAPI (String apiName, String label, String apiId) {

        GatewayArtifactSynchronizerProperties gatewayArtifactSynchronizerProperties = ServiceReferenceHolder
                .getInstance().getAPIManagerConfiguration().getGatewayArtifactSynchronizerProperties();

        if (gatewayArtifactSynchronizerProperties.isInMemoryArtifactSynchronizer()) {
            if (gatewayArtifactSynchronizerProperties.getGatewayLabels().contains(label)) {
                try {
                    GatewayAPIDTO gatewayAPIDTO = ServiceReferenceHolder.getInstance().getArtifactRetriever()
                            .retrieveArtifacts(apiId, apiName, label);
                    apiGatewayAdmin.deployAPI(gatewayAPIDTO);
                    return true;
                } catch (AxisFault | ArtifactSynchronizerException axisFault) {
                    log.error("Error publishing " + apiName + " API from the Gateway");
                }
            }
        }
        return false;
    }

    public boolean unDeployAPI (String apiName, String label, String apiId) {

        GatewayArtifactSynchronizerProperties gatewayArtifactSynchronizerProperties = ServiceReferenceHolder.getInstance()
                .getAPIManagerConfiguration().getGatewayArtifactSynchronizerProperties();

        if (gatewayArtifactSynchronizerProperties.isInMemoryArtifactSynchronizer()) {
            if (gatewayArtifactSynchronizerProperties.getGatewayLabels().contains(label)) {
                try {
                    GatewayAPIDTO gatewayAPIDTO = ServiceReferenceHolder.getInstance().getArtifactRetriever()
                            .retrieveArtifacts(apiId, apiName, label);
                    apiGatewayAdmin.unDeployAPI(gatewayAPIDTO);
                    ServiceReferenceHolder.getInstance().getArtifactRetriever().deleteArtifacts(gatewayAPIDTO);
                    return true;
                } catch (AxisFault | ArtifactSynchronizerException axisFault) {
                    log.error(axisFault);
                }
            }
        }
        return false;
    }

}
