package org.wso2.carbon.apimgt.rest.api.gateway.v1.impl;

import org.json.JSONObject;
import org.wso2.carbon.apimgt.gateway.InMemoryAPIDeployer;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.*;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.DeployResponseDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.ErrorDTO;

import java.util.List;

import java.io.InputStream;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class RedeployApiApiServiceImpl implements RedeployApiApiService {

    public Response redeployApiPost(String apiName, String version , String tenantDomain,
            MessageContext messageContext) {

        InMemoryAPIDeployer inMemoryApiDeployer = new InMemoryAPIDeployer();
        if (tenantDomain == null) {
            tenantDomain = APIConstants.SUPER_TENANT_DOMAIN;
        }
        Map<String, String> apiAttributes = inMemoryApiDeployer.getGatewayAPIAttributes(apiName, version, tenantDomain);
        String apiId = apiAttributes.get(APIConstants.GatewayArtifactSynchronizer.API_ID);
        String label = apiAttributes.get(APIConstants.GatewayArtifactSynchronizer.LABEL);
        boolean status = inMemoryApiDeployer.deployAPI(apiId, label);

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
