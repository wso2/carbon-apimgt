package org.wso2.carbon.apimgt.rest.api.gateway.v1.impl;

import org.wso2.carbon.apimgt.impl.APIGatewayManager;

import org.wso2.carbon.apimgt.rest.api.gateway.v1.*;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.dto.WorkflowResponseDTO;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;


public class DeployApiApiServiceImpl implements DeployApiApiService {
      public Response deployApiPost(String apiName, String environment, String apiId, MessageContext messageContext) {

          APIGatewayManager apiGatewayManager = APIGatewayManager.getInstance();
          apiGatewayManager.deployAPI(apiName, environment, apiId);

          return Response.ok().entity("magic!").build();
  }
}
