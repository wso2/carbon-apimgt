package org.wso2.carbon.apimgt.rest.api.admin.v1;


import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.EnvironmentDTO;



import javax.ws.rs.core.Response;


public interface EnvironmentsApiService {
      public Response environmentsEnvironmentIdDelete(String environmentId, MessageContext messageContext) throws APIManagementException;
      public Response environmentsEnvironmentIdPut(String environmentId, EnvironmentDTO environmentDTO, MessageContext messageContext) throws APIManagementException;
      public Response environmentsGet(MessageContext messageContext) throws APIManagementException;
      public Response environmentsPost(EnvironmentDTO environmentDTO, MessageContext messageContext) throws APIManagementException;
}
