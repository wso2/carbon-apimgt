package org.wso2.carbon.apimgt.internal.service.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.GatewayArtifactsMgtDAO;
import org.wso2.carbon.apimgt.internal.service.GatewaySynapseArtifactsApiService;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.internal.service.dto.SynapseArtifactListDTO;
import java.util.List;
import javax.ws.rs.core.Response;



public class GatewaySynapseArtifactsApiServiceImpl implements GatewaySynapseArtifactsApiService {

    private GatewayArtifactsMgtDAO gatewayArtifactsMgtDAO = GatewayArtifactsMgtDAO.getInstance();

    private SynapseArtifactListDTO synapseArtifactListDTOS = new SynapseArtifactListDTO();

    public Response gatewaySynapseArtifactsGet(String gatewayLabel, MessageContext messageContext) throws APIManagementException {

        List<String> gatewayRuntimeArtifactsArray;
        try {
            gatewayRuntimeArtifactsArray  =
                    gatewayArtifactsMgtDAO.getAllGatewayPublishedAPIArtifacts(gatewayLabel);
        } catch (APIManagementException e) {
            throw new APIManagementException("Error retrieving Artifact from DB", e);
        }
        synapseArtifactListDTOS.list(gatewayRuntimeArtifactsArray);
        synapseArtifactListDTOS.setCount(gatewayRuntimeArtifactsArray.size());
        return Response.ok().entity(synapseArtifactListDTOS).build();
    }
}
