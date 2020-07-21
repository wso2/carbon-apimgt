package org.wso2.carbon.apimgt.internal.service.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.dao.GatewayArtifactsMgtDAO;
import org.wso2.carbon.apimgt.internal.service.SynapseArtifactsApiService;
import org.apache.cxf.jaxrs.ext.MessageContext;
import javax.ws.rs.core.Response;



public class SynapseArtifactsApiServiceImpl implements SynapseArtifactsApiService {

    private GatewayArtifactsMgtDAO gatewayArtifactsMgtDAO = GatewayArtifactsMgtDAO.getInstance();
    private static final Log log = LogFactory.getLog(SynapseArtifactsApiServiceImpl.class);

    @Override
    public Response synapseArtifactsGet( String apiId, String gatewayLabel, String gatewayInstruction,
                                        MessageContext messageContext) {
        String gatewayRuntimeArtifacts;
        try {
            gatewayRuntimeArtifacts = gatewayArtifactsMgtDAO.getGatewayPublishedAPIArtifacts(apiId, gatewayLabel,
                    gatewayInstruction);
        } catch (APIManagementException e ) {
            JSONObject responseObj = new JSONObject();
            responseObj.put("Message", "Error retrieving Artifact belongs to  " + apiId + " from DB");
            String responseStringObj = String.valueOf(responseObj);
            log.error("Error retrieving Artifact belongs to  " + apiId + " from DB", e);
            return Response.serverError().entity(responseStringObj).build();
        }
        return Response.ok().entity(gatewayRuntimeArtifacts).build();
    }
}
