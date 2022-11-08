package org.wso2.carbon.apimgt.internal.service;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.internal.service.dto.DeployedAPIRevisionDTO;
import java.util.List;
import org.wso2.carbon.apimgt.internal.service.dto.UnDeployedAPIRevisionDTO;

import javax.ws.rs.core.Response;

public interface ApisApiService {
      public Response apisGet(String xWSO2Tenant, String apiId, String context, String version, String gatewayLabel, Boolean expand, String accept, MessageContext messageContext) throws APIManagementException;
      public Response deployedAPIRevision(List<DeployedAPIRevisionDTO> deployedAPIRevisionDTOList, MessageContext messageContext) throws APIManagementException;
      public Response unDeployedAPIRevision(UnDeployedAPIRevisionDTO unDeployedAPIRevisionDTO, MessageContext messageContext) throws APIManagementException;
}
