/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.apk.apimgt.rest.api.admin.v1.common.impl;

import org.json.simple.JSONObject;
import org.wso2.apk.apimgt.api.APIAdmin;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.apk.apimgt.rest.api.admin.v1.common.utils.mappings.SystemScopesMappingUtil;
import org.wso2.apk.apimgt.rest.api.admin.v1.dto.RoleAliasListDTO;
import org.wso2.apk.apimgt.rest.api.admin.v1.dto.ScopeListDTO;
import org.wso2.apk.apimgt.rest.api.admin.v1.dto.ScopeSettingsDTO;
import org.wso2.apk.apimgt.rest.api.common.RestApiCommonUtil;

import java.util.Base64;
import java.util.Map;

public class SystemScopesCommonImpl {

    private SystemScopesCommonImpl() {
    }

    /**
     * Get system scope by scope name
     *
     * @param scope    Scope name
     * @param username Username
     * @return Scope settings
     * @throws APIManagementException When an internal error occurs
     */
    public static ScopeSettingsDTO systemScopesScopeNameGet(String scope, String username)
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
        return scopeSettingsDTO;
    }

    /**
     * Get all system scopes
     *
     * @return List of system scopes
     * @throws APIManagementException When an internal error occurs
     */
    public static ScopeListDTO systemScopesGet() throws APIManagementException {
        try {
            Map<String, String> scopeRoleMapping = APIUtil.getRESTAPIScopesForTenantWithoutRoleMappings(
                    RestApiCommonUtil.getLoggedInUserTenantDomain());
            return SystemScopesMappingUtil.fromScopeListToScopeListDTO(scopeRoleMapping);
        } catch (APIManagementException e) {
            String error = "Error when getting the list of scopes-role mapping";
            throw new APIManagementException(error, e,
                    ExceptionCodes.from(ExceptionCodes.INTERNAL_ERROR_WITH_SPECIFIC_MESSAGE, error));
        }
    }

    /**
     * Update scope role mappings
     *
     * @param body Scope mappings to be updated
     * @return List of scope role mappings
     * @throws APIManagementException When an internal error occurs
     */
    public static ScopeListDTO updateRolesForScope(ScopeListDTO body) throws APIManagementException {
        JSONObject newScopeRoleJson = SystemScopesMappingUtil.createJsonObjectOfScopeMapping(body);
        APIUtil.updateTenantConfOfRoleScopeMapping(newScopeRoleJson, RestApiCommonUtil.getLoggedInUsername());
        Map<String, String> scopeRoleMapping = APIUtil.getRESTAPIScopesForTenantWithoutRoleMappings(
                RestApiCommonUtil.getLoggedInUserTenantDomain());
        return SystemScopesMappingUtil.fromScopeListToScopeListDTO(scopeRoleMapping);
    }

    /**
     * Get role aliases of scope mappings
     *
     * @return List of role aliases
     * @throws APIManagementException When an internal error occurs
     */
    public static RoleAliasListDTO getRoleAliasMappings() throws APIManagementException {
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        JSONObject tenantConfig = APIUtil.getTenantConfig(tenantDomain);
        JSONObject roleMapping = (JSONObject) tenantConfig.get(APIConstants.REST_API_ROLE_MAPPINGS_CONFIG);
        RoleAliasListDTO roleAliasListDTO = new RoleAliasListDTO();
        if (roleMapping != null) {
            roleAliasListDTO = SystemScopesMappingUtil.fromRoleAliasListToRoleAliasListDTO(
                    SystemScopesMappingUtil.createMapOfRoleMapping(roleMapping));
        }
        return roleAliasListDTO;
    }

    /**
     * Add new role alias scope mapping
     *
     * @param body New role alias for scope mapping
     * @return List of role aliases
     * @throws APIManagementException When an internal error occurs
     */
    public static RoleAliasListDTO addRoleAliasMapping(RoleAliasListDTO body) throws APIManagementException {
        RoleAliasListDTO roleAliasListDTO = new RoleAliasListDTO();
        JSONObject newRoleMappingJson = SystemScopesMappingUtil.createJsonObjectOfRoleMapping(body);
        String username = RestApiCommonUtil.getLoggedInUsername();
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        APIUtil.updateTenantConfRoleAliasMapping(newRoleMappingJson, username);
        JSONObject tenantConfig = APIUtil.getTenantConfig(tenantDomain);
        JSONObject roleMapping = (JSONObject) tenantConfig.get(APIConstants.REST_API_ROLE_MAPPINGS_CONFIG);
        if (roleMapping != null) {
            roleAliasListDTO = SystemScopesMappingUtil.fromRoleAliasListToRoleAliasListDTO(
                    SystemScopesMappingUtil.createMapOfRoleMapping((roleMapping)));
        }
        return roleAliasListDTO;
    }

}
