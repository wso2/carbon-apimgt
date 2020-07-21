package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
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
    private static final Log log = LogFactory.getLog(GatewaySynapseArtifactsApiServiceImpl.class);


    public Response gatewaySynapseArtifactsGet(String gatewayLabel, MessageContext messageContext) {

        List<String> gatewayRuntimeArtifactsArray;
        try {
            gatewayRuntimeArtifactsArray  =
                    gatewayArtifactsMgtDAO.getAllGatewayPublishedAPIArtifacts(gatewayLabel);
        } catch (APIManagementException e) {
            JSONObject responseObj = new JSONObject();
            responseObj.put("Message", "Error retrieving artifacts for the  gateway label of  " + gatewayLabel + " "
                    + "from DB");
            String responseStringObj = String.valueOf(responseObj);
            log.error("Error retrieving artifacts for the  gateway label of  " + gatewayLabel + " "
                    + "from DB", e);
            return Response.serverError().entity(responseStringObj).build();
        }
        synapseArtifactListDTOS.list(gatewayRuntimeArtifactsArray);
        synapseArtifactListDTOS.setCount(gatewayRuntimeArtifactsArray.size());
        return Response.ok().entity(synapseArtifactListDTOS).build();
    }
}
