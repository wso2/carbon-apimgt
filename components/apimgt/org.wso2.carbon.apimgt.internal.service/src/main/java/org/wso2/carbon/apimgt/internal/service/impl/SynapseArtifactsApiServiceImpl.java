package org.wso2.carbon.apimgt.internal.service.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.GatewayArtifactsMgtDAO;
import org.wso2.carbon.apimgt.internal.service.SynapseArtifactsApiService;
import org.apache.cxf.jaxrs.ext.MessageContext;
import javax.ws.rs.core.Response;



public class SynapseArtifactsApiServiceImpl implements SynapseArtifactsApiService {

    private GatewayArtifactsMgtDAO gatewayArtifactsMgtDAO = GatewayArtifactsMgtDAO.getInstance();

    @Override
    public Response synapseArtifactsGet( String apiId, String gatewayLabel, String gatewayInstruction,
                                        MessageContext messageContext) throws APIManagementException {
        String gatewayRuntimeArtifacts;
        try {
            gatewayRuntimeArtifacts = gatewayArtifactsMgtDAO .getGatewayPublishedAPIArtifacts(apiId, gatewayLabel,
                    gatewayInstruction);
        } catch (APIManagementException e ) {
            throw new APIManagementException("Error retrieving Artifact belongs to  " + apiId + " from DB", e);
        }
        return Response.ok().entity(gatewayRuntimeArtifacts).build();
    }
}
