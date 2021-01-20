package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.json.simple.JSONObject;

import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.admin.v1.*;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ScopeSettingsDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.SystemScopesMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.Base64;
import java.util.Map;

import javax.ws.rs.core.Response;

public class SystemScopesApiServiceImpl implements SystemScopesApiService {

    private static final Log log = LogFactory.getLog(SystemScopesApiServiceImpl.class);

    public Response systemScopesScopeNameGet(String scope, String username, MessageContext messageContext)
            throws APIManagementException {
        ScopeSettingsDTO scopeSettingsDTO = new ScopeSettingsDTO();
        APIAdmin apiAdmin = new APIAdminImpl();
        String decodedScope = new String(Base64.getDecoder().decode(scope));
        boolean existence;

        if (username == null) {
            existence = apiAdmin.isScopeExists(RestApiCommonUtil.getLoggedInUsername(), decodedScope);
            if (existence) {
                scopeSettingsDTO.setName(decodedScope);
            } else {
                throw new APIManagementException("Scope Not Found. Scope : " + decodedScope,
                        ExceptionCodes.SCOPE_NOT_FOUND);
            }
        } else {
            existence = apiAdmin.isScopeExistsForUser(username, decodedScope);
            if (existence) {
                scopeSettingsDTO.setName(decodedScope);
            } else {
                throw new APIManagementException("User does not have scope. Username : " + username + " Scope : "
                        + decodedScope, ExceptionCodes.SCOPE_NOT_FOUND_FOR_USER);
            }
        }
        return Response.ok().entity(scopeSettingsDTO).build();
    }

    public Response systemScopesGet(MessageContext messageContext) {
        try {
            Map<String, String> scopeRoleMapping = APIUtil.getRESTAPIScopesForTenantWithoutRoleMappings(MultitenantUtils
                    .getTenantDomain(RestApiCommonUtil.getLoggedInUsername()));
            ScopeListDTO scopeListDTO = SystemScopesMappingUtil.fromScopeListToScopeListDTO(scopeRoleMapping);
            return Response.ok().entity(scopeListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error when getting the list of scopes-role mapping.";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    public Response updateRolesForScope(ScopeListDTO body, MessageContext messageContext)
            throws APIManagementException {
        JSONObject newScopeRoleJson = SystemScopesMappingUtil.createJsonObjectOfScopeMapping(body);
        APIUtil.updateTenantConfOfRoleScopeMapping(newScopeRoleJson, RestApiCommonUtil.getLoggedInUsername());
        Map<String, String> scopeRoleMapping = APIUtil.getRESTAPIScopesForTenantWithoutRoleMappings(MultitenantUtils
                .getTenantDomain(RestApiCommonUtil.getLoggedInUsername()));
        ScopeListDTO scopeListDTO = SystemScopesMappingUtil.fromScopeListToScopeListDTO(scopeRoleMapping);
        return Response.ok().entity(scopeListDTO).build();
    }

    @Override
    public Response systemScopesRoleAliasesGet(MessageContext messageContext) throws APIManagementException {
        String tenantDomain = MultitenantUtils.getTenantDomain(RestApiCommonUtil.getLoggedInUsername());
        JSONObject tenantConfig = APIUtil.getTenantConfig(tenantDomain);
        JSONObject roleMapping = (JSONObject) tenantConfig.get(APIConstants.REST_API_ROLE_MAPPINGS_CONFIG);
        RoleAliasListDTO roleAliasListDTO = new RoleAliasListDTO();
        if (roleMapping != null) {
            roleAliasListDTO = SystemScopesMappingUtil.fromRoleAliasListToRoleAliasListDTO(
                    SystemScopesMappingUtil.createMapOfRoleMapping((roleMapping)));
        }
        return Response.ok().entity(roleAliasListDTO).build();
    }

    @Override
    public Response systemScopesRoleAliasesPut(RoleAliasListDTO body, MessageContext messageContext)
            throws APIManagementException {
        RoleAliasListDTO roleAliasListDTO = new RoleAliasListDTO();
        JSONObject newRoleMappingJson = SystemScopesMappingUtil.createJsonObjectOfRoleMapping(body);
        String username = RestApiCommonUtil.getLoggedInUsername();
        String tenantDomain = MultitenantUtils.getTenantDomain(username);
        APIUtil.updateTenantConfRoleAliasMapping(newRoleMappingJson, username);
        JSONObject tenantConfig = APIUtil.getTenantConfig(tenantDomain);
        JSONObject roleMapping = (JSONObject) tenantConfig.get(APIConstants.REST_API_ROLE_MAPPINGS_CONFIG);
        if (roleMapping != null) {
            roleAliasListDTO = SystemScopesMappingUtil.fromRoleAliasListToRoleAliasListDTO(
                    SystemScopesMappingUtil.createMapOfRoleMapping((roleMapping)));
        }
        return Response.ok().entity(roleAliasListDTO).build();
    }
}
