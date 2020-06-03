/*
 *  Copyright (c) 2020, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.admin.v1.SettingsApiService;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.*;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.SettingsMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import javax.ws.rs.core.Response;
import java.util.Map;

public class SettingsApiServiceImpl implements SettingsApiService {

    private static final Log log = LogFactory.getLog(SettingsApiServiceImpl.class);

    @Override
    public Response settingsGet(MessageContext messageContext) {
        try {
            String username = RestApiUtil.getLoggedInUsername();
            boolean isUserAvailable = false;
            if (!APIConstants.WSO2_ANONYMOUS_USER.equalsIgnoreCase(username)) {
                isUserAvailable = true;
            }
            SettingsMappingUtil settingsMappingUtil = new SettingsMappingUtil();
            SettingsDTO settingsDTO = settingsMappingUtil.fromSettingstoDTO(isUserAvailable);
            return Response.ok().entity(settingsDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Admin Settings";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override public Response settingsRoleAliasesGet(MessageContext messageContext) throws APIManagementException {
        return null;
    }

    @Override public Response settingsRoleAliasesPost(MessageContext messageContext) throws APIManagementException {
        return null;
    }

    @Override public Response settingsRoleAliasesRoleAliasDelete(String roleAlias, MessageContext messageContext)
            throws APIManagementException {
        return null;
    }

    /**
     * Get all scopes of a user
     *
     * @param username Search query
     * @return Scope list
     */
    @Override
    public Response settingsScopesScopeGet(String username, String scopeName, MessageContext messageContext) {
        String[] userRoles;
        ScopeSettingsDTO scopeSettingsDTO = new ScopeSettingsDTO();
        ErrorDTO errorDTO = new ErrorDTO();
        Map<String, String> scopeRoleMapping = APIUtil.getRESTAPIScopesForTenant(MultitenantUtils
                .getTenantDomain(username));
        try {
            if (APIUtil.isUserExist(username) && scopeRoleMapping.containsKey(scopeName)) {
                userRoles = APIUtil.getListOfRoles(username);
                SettingsMappingUtil settingsMappingUtil = new SettingsMappingUtil();

                if (settingsMappingUtil.GetRoleScopeList(userRoles, scopeRoleMapping).contains(scopeName)) {
                    scopeSettingsDTO.setName(scopeName);
                }
            } else {
                errorDTO.setCode(404l);
                errorDTO.description("Username or Scope does not exist. Username: "
                        + username + ", " + "Scope: " + scopeName);
                errorDTO.setMessage("Not Found");
                return Response.ok().entity(errorDTO).build();
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error when getting the list of scopes. Username: " + username + " , "
                    + "Scope: " + scopeName;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return Response.ok().entity(scopeSettingsDTO).build();
    }

    /**
     * Get all scopes and respective roles
     *
     * @return Role-Scope list
     */
    @Override
    public Response settingsScopesGet(MessageContext messageContext) {
        try {
            Map<String, String> roleScopeMapping = APIUtil.getRESTAPIScopesForTenant(MultitenantUtils
                    .getTenantDomain(RestApiUtil.getLoggedInUsername()));
            ScopeListDTO scopeListDTO = SettingsMappingUtil.fromScopeListToScopeListDTO(roleScopeMapping);
            return Response.ok().entity(scopeListDTO).build();
        } catch (Exception e) {
            String errorMessage = "Error when getting the list of role-scopes mapping.";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override public Response settingsScopesScopeDelete(String keyManagerId, MessageContext messageContext)
            throws APIManagementException {
        return null;
    }

    @Override public Response settingsScopesScopePut(String keyManagerId, KeyManagerDTO body,
            MessageContext messageContext) throws APIManagementException {
        return null;
    }
}
