package org.wso2.carbon.apimgt.rest.api.gateway.v1.impl;

import org.wso2.carbon.apimgt.gateway.InMemoryAPIDeployer;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.*;
import org.apache.cxf.jaxrs.ext.MessageContext;

import javax.ws.rs.core.Response;

import org.json.JSONObject;

public class DeployApiApiServiceImpl implements DeployApiApiService {

    public Response deployApiPost(String apiName, String label, String apiId, MessageContext messageContext) {

        InMemoryAPIDeployer inMemoryApiDeployer = new InMemoryAPIDeployer();
        boolean status = inMemoryApiDeployer.deployAPI(apiName, label, apiId);

        JSONObject responseObj = new JSONObject();
        if (status) {
            responseObj.put("Message", "Success");
            String responseStringObj = String.valueOf(responseObj);
            return Response.ok().entity(responseStringObj).build();
        } else {
            responseObj.put("Message", "Error");
            String responseStringObj = String.valueOf(responseObj);
            return Response.serverError().entity(responseStringObj).build();
        }
    }
}
