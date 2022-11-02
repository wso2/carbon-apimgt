package org.wso2.carbon.apimgt.rest.api.admin.v1;


import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.api.APIManagementException;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.RoleAliasListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ScopeListDTO;

import javax.ws.rs.core.Response;


public interface SystemScopesApiService {
      public Response systemScopesGet(MessageContext messageContext) throws APIManagementException;
      public Response systemScopesRoleAliasesGet(MessageContext messageContext) throws APIManagementException;
      public Response systemScopesRoleAliasesPut(RoleAliasListDTO roleAliasListDTO, MessageContext messageContext) throws APIManagementException;
      public Response systemScopesScopeNameGet(String scopeName, String username, MessageContext messageContext) throws APIManagementException;
      public Response updateRolesForScope(ScopeListDTO scopeListDTO, MessageContext messageContext) throws APIManagementException;
}
