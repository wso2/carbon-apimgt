package org.wso2.carbon.apimgt.rest.api.gateway.v1.impl;

import org.json.JSONArray;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.api.gateway.GatewayContentDTO;
import org.wso2.carbon.apimgt.gateway.InMemoryAPIDeployer;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.*;
import org.apache.cxf.jaxrs.ext.MessageContext;

import javax.ws.rs.core.Response;

public class SequenceApiServiceImpl implements SequenceApiService {

    public Response sequenceGet(String apiName, String label, String apiId, MessageContext messageContext) {

        InMemoryAPIDeployer inMemoryApiDeployer = new InMemoryAPIDeployer();
        GatewayAPIDTO gatewayAPIDTO = inMemoryApiDeployer.getAPIArtifact(apiId, label);

        JSONObject responseObj = new JSONObject();
        JSONArray sequencesArray = new JSONArray();
        if (gatewayAPIDTO != null) {
            if (gatewayAPIDTO.getSequenceToBeAdd() != null) {
                for (GatewayContentDTO sequence : gatewayAPIDTO.getSequenceToBeAdd()) {
                    sequencesArray.put(sequence.getContent());
                }
            }
            responseObj.put("Sequence", sequencesArray);
            String responseStringObj = String.valueOf(responseObj);
            return Response.ok().entity(responseStringObj).build();
        } else {
            responseObj.put("Message", "Error");
            String responseStringObj = String.valueOf(responseObj);
            return Response.serverError().entity(responseStringObj).build();
        }
    }
}
