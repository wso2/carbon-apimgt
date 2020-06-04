package org.wso2.carbon.apimgt.rest.api.gateway.v1.impl;

import org.wso2.carbon.apimgt.gateway.InMemoryAPIDeployer;
import org.wso2.carbon.apimgt.rest.api.gateway.v1.*;
import org.apache.cxf.jaxrs.ext.MessageContext;
import javax.ws.rs.core.Response;

public class DeployApiApiServiceImpl implements DeployApiApiService {
      public Response deployApiPost(String apiName, String label, String apiId, MessageContext messageContext) {

          InMemoryAPIDeployer inMemoryApiDeployer = new InMemoryAPIDeployer();
          inMemoryApiDeployer.deployAPI(apiName, label, apiId);

          return Response.ok().entity("magic!").build();
  }
}
