package org.wso2.carbon.apimgt.rest.api.gateway.v1.impl;

import org.wso2.carbon.apimgt.gateway.APIDeployer;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.*;
import org.apache.cxf.jaxrs.ext.MessageContext;
import javax.ws.rs.core.Response;

public class DeployApiApiServiceImpl implements DeployApiApiService {
      public Response deployApiPost(String apiName, String label, String apiId, MessageContext messageContext) {

          APIDeployer apiDeployer = new APIDeployer();
          apiDeployer.deployAPI(apiName, label, apiId);

          return Response.ok().entity("magic!").build();
  }
}
