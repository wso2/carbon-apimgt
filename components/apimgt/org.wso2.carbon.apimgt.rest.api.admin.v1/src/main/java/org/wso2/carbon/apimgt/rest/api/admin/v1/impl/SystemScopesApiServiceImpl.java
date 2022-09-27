package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.rest.api.admin.v1.SystemScopesApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.common.impl.SystemScopesCommonImpl;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.RoleAliasListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ScopeListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ScopeSettingsDTO;

import javax.ws.rs.core.Response;

public class SystemScopesApiServiceImpl implements SystemScopesApiService {

    @Override
    public Response systemScopesScopeNameGet(String scope, String username, MessageContext messageContext)
            throws APIManagementException {
        ScopeSettingsDTO scopeSettingsDTO = SystemScopesCommonImpl.systemScopesScopeNameGet(scope, username);
        return Response.ok().entity(scopeSettingsDTO).build();
    }

    @Override
    public Response systemScopesGet(MessageContext messageContext) throws APIManagementException {
        ScopeListDTO scopeListDTO = SystemScopesCommonImpl.systemScopesGet();
        return Response.ok().entity(scopeListDTO).build();
    }

    @Override
    public Response updateRolesForScope(ScopeListDTO body, MessageContext messageContext)
            throws APIManagementException {
        ScopeListDTO scopeListDTO = SystemScopesCommonImpl.updateRolesForScope(body);
        return Response.ok().entity(scopeListDTO).build();
    }

    @Override
    public Response getRoleAliasMappings(MessageContext messageContext) throws APIManagementException {
        RoleAliasListDTO roleAliasListDTO = SystemScopesCommonImpl.getRoleAliasMappings();
        return Response.ok().entity(roleAliasListDTO).build();
    }

    @Override
    public Response addRoleAliasMapping(RoleAliasListDTO body, MessageContext messageContext)
            throws APIManagementException {
        RoleAliasListDTO roleAliasListDTO = SystemScopesCommonImpl.addRoleAliasMapping(body);
        return Response.ok().entity(roleAliasListDTO).build();
    }
}
