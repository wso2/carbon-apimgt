package org.wso2.carbon.apimgt.rest.api.gateway.v1.impl;

import org.json.JSONObject;
import org.wso2.carbon.apimgt.api.gateway.GatewayAPIDTO;
import org.wso2.carbon.apimgt.gateway.InMemoryAPIDeployer;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.*;
import org.apache.cxf.jaxrs.ext.MessageContext;

import javax.ws.rs.core.Response;

public class ApiArtifactApiServiceImpl implements ApiArtifactApiService {

    public Response apiArtifactGet(String apiName, String label, String apiId, MessageContext messageContext) {

        InMemoryAPIDeployer inMemoryApiDeployer = new InMemoryAPIDeployer();
        GatewayAPIDTO gatewayAPIDTO = inMemoryApiDeployer.getAPIArtifact(apiId, label);
        String definition;
        JSONObject responseObj = new JSONObject();
        if (gatewayAPIDTO != null) {
            definition = gatewayAPIDTO.getApiDefinition();
            responseObj.put("Definition", definition);
            String responseStringObj = String.valueOf(responseObj);
            return Response.ok().entity(responseStringObj).build();
        } else {
            responseObj.put("Message", "Error");
            String responseStringObj = String.valueOf(responseObj);
            return Response.serverError().entity(responseStringObj).build();
        }
    }
}
