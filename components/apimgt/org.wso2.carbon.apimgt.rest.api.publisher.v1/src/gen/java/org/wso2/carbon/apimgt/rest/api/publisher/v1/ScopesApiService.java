package org.wso2.carbon.apimgt.rest.api.publisher.v1;


import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ScopeDTO;

import javax.ws.rs.core.Response;


public interface ScopesApiService {
      public Response addSharedScope(ScopeDTO scopeDTO, MessageContext messageContext) throws APIManagementException;
      public Response deleteSharedScope(String scopeId, MessageContext messageContext) throws APIManagementException;
      public Response getSharedScope(String scopeId, MessageContext messageContext) throws APIManagementException;
      public Response getSharedScopeUsages(String scopeId, MessageContext messageContext) throws APIManagementException;
      public Response getSharedScopes(Integer limit, Integer offset, MessageContext messageContext) throws APIManagementException;
      public Response updateSharedScope(String scopeId, ScopeDTO scopeDTO, MessageContext messageContext) throws APIManagementException;
      public Response validateScope(String scopeId, MessageContext messageContext) throws APIManagementException;
}
