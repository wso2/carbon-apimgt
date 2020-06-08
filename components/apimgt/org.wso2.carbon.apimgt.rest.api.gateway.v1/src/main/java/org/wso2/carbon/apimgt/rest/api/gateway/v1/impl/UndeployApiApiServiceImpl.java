package org.wso2.carbon.apimgt.rest.api.gateway.v1.impl;

import org.json.JSONObject;
import org.wso2.carbon.apimgt.gateway.InMemoryAPIDeployer;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.*;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.DeployResponseDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class UndeployApiApiServiceImpl implements UndeployApiApiService {

    public Response undeployApiPost(String apiName, String label, String apiId, MessageContext messageContext) {
        InMemoryAPIDeployer inMemoryApiDeployer = new InMemoryAPIDeployer();
        boolean status = inMemoryApiDeployer.unDeployAPI(apiName, label, apiId);

        JSONObject responseObj = new JSONObject();
        if (status) {
            responseObj.put("Message","Success");
            String responseStringObj = String.valueOf(responseObj);
            return Response.ok().entity(responseStringObj).build();
        } else {
            responseObj.put("Message","Error");
            String responseStringObj = String.valueOf(responseObj);
            return Response.serverError().entity(responseStringObj).build();
        }
    }
}
