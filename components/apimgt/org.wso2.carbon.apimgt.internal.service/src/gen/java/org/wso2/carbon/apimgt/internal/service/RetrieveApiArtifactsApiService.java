package org.wso2.carbon.apimgt.internal.service;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.internal.service.dto.UUIDListDTO;

import javax.ws.rs.core.Response;

public interface RetrieveApiArtifactsApiService {
      public Response retrieveApiArtifactsPost(String xWSO2Tenant, String gatewayLabel, String type, UUIDListDTO uuidList, MessageContext messageContext) throws APIManagementException;
}
