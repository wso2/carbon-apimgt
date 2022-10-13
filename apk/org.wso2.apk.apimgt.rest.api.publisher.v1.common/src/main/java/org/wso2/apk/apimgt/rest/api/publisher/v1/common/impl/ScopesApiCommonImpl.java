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

package org.wso2.apk.apimgt.rest.api.publisher.v1.common.impl;

import org.apache.commons.lang3.StringUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.apk.apimgt.api.model.Scope;
import org.wso2.apk.apimgt.api.model.SharedScopeUsage;
import org.wso2.apk.apimgt.impl.utils.APIUtil;
import org.wso2.apk.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.apk.apimgt.rest.api.common.RestApiConstants;
import org.wso2.apk.apimgt.rest.api.publisher.v1.common.mappings.SharedScopeMappingUtil;
import org.wso2.apk.apimgt.rest.api.publisher.v1.dto.ScopeDTO;
import org.wso2.apk.apimgt.rest.api.publisher.v1.dto.ScopeListDTO;
import org.wso2.apk.apimgt.rest.api.publisher.v1.dto.SharedScopeUsageDTO;

import java.util.Base64;
import java.util.List;

/**
 * Utility class for operations related to ScopeAPIService
 */
public class ScopesApiCommonImpl {

    public static final String ID_CANNOT_BE_NULL_OR_EMPTY = "Scope Id cannot be null or empty";

    private ScopesApiCommonImpl() {
        //To hide the default constructor
    }

    /**
     * Validates whether the scope exists for the given scope name
     *
     * @param name Scope Name
     * @throws APIManagementException If scope is not found or any error occurred while checking the scope name
     */
    public static void validateScope(String name) throws APIManagementException {

        String scopeName = new String(Base64.getUrlDecoder().decode(name));
        if (APIUtil.isAllowedScope(scopeName)) {
            throw new APIManagementException(ExceptionCodes.SCOPE_NOT_FOUND);
        }
        boolean isScopeExist;
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            isScopeExist =
                    apiProvider.isScopeKeyExist(scopeName, APIUtil.getTenantIdFromTenantDomain(tenantDomain));
        } catch (APIManagementException e) {
            throw new APIManagementException(e.getMessage(),
                    ExceptionCodes.from(ExceptionCodes.ERROR_CHECKING_SCOPE_NAME, scopeName));
        }
        if (!isScopeExist) {
            throw new APIManagementException(ExceptionCodes.SCOPE_NOT_FOUND);
        }
    }

    public static ScopeDTO addSharedScope(ScopeDTO scopeDTO) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        String scopeName = scopeDTO.getName();
        if (StringUtils.isEmpty(scopeName)) {
            throw new APIManagementException("Shared Scope Name cannot be null or empty",
                    ExceptionCodes.SHARED_SCOPE_NAME_NOT_SPECIFIED);
        }
        if (StringUtils.isEmpty(scopeDTO.getDisplayName())) {
            throw new APIManagementException("Shared scope Display Name cannot be null or empty",
                    ExceptionCodes.SHARED_SCOPE_DISPLAY_NAME_NOT_SPECIFIED);
        }
        if (apiProvider.isScopeKeyExist(scopeName, APIUtil.getTenantIdFromTenantDomain(tenantDomain))) {
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.SCOPE_ALREADY_REGISTERED,
                    scopeName));
        }
        Scope scopeToAdd = SharedScopeMappingUtil.fromDTOToScope(scopeDTO);
        String sharedScopeId = apiProvider.addSharedScope(scopeToAdd, tenantDomain);
        //Get registered shared scope
        Scope createdScope = apiProvider.getSharedScopeByUUID(sharedScopeId, tenantDomain);
        return SharedScopeMappingUtil.fromScopeToDTO(createdScope);
    }

    /**
     * Retrieves the Scope for the given scope ID
     *
     * @param scopeId Scope ID
     * @return Scope associated to the given scope ID
     * @throws APIManagementException If the scope ID is invalid or error while retrieving the scope
     */
    public static ScopeDTO getSharedScope(String scopeId) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        if (StringUtils.isEmpty(scopeId)) {
            throw new APIManagementException(ID_CANNOT_BE_NULL_OR_EMPTY, ExceptionCodes.SHARED_SCOPE_ID_NOT_SPECIFIED);
        }
        Scope sharedScope = apiProvider.getSharedScopeByUUID(scopeId, tenantDomain);
        return SharedScopeMappingUtil.fromScopeToDTO(sharedScope);
    }

    /**
     * Delete the shared scope for the given scope ID
     *
     * @param scopeId Scope ID
     * @throws APIManagementException If the scope ID is invalid or error while deleting the scope
     */
    public static void deleteSharedScope(String scopeId) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        if (StringUtils.isEmpty(scopeId)) {
            throw new APIManagementException(ID_CANNOT_BE_NULL_OR_EMPTY, ExceptionCodes.SHARED_SCOPE_ID_NOT_SPECIFIED);
        }
        Scope existingScope = apiProvider.getSharedScopeByUUID(scopeId, tenantDomain);
        if (apiProvider.isScopeKeyAssignedToAPI(existingScope.getKey(), tenantDomain)) {
            throw new APIManagementException("Cannot remove the Shared Scope " + scopeId + " as it is used by one "
                    + "or more APIs", ExceptionCodes.from(ExceptionCodes.SHARED_SCOPE_ALREADY_ATTACHED, scopeId));
        }
        apiProvider.deleteSharedScope(existingScope.getKey(), tenantDomain);
    }

    /**
     * Retrieves shared scope usage for the given Scope ID
     *
     * @param scopeId Scope ID
     * @return Shared scope usage
     * @throws APIManagementException If the scope ID is invalid or error while retrieving shared scope usage
     */
    public static SharedScopeUsageDTO getSharedScopeUsages(String scopeId) throws APIManagementException {

        if (StringUtils.isEmpty(scopeId)) {
            throw new APIManagementException(ID_CANNOT_BE_NULL_OR_EMPTY, ExceptionCodes.SHARED_SCOPE_ID_NOT_SPECIFIED);
        }
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        int tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        SharedScopeUsage sharedScopeUsage = apiProvider.getSharedScopeUsage(scopeId, tenantId);
        return SharedScopeMappingUtil.fromSharedScopeUsageToDTO(sharedScopeUsage);
    }

    /**
     * Retrieves shared scopes
     *
     * @param limit  Pagination Limit
     * @param offset Pagination offset
     * @return Shared scopes DTO
     * @throws APIManagementException Error occurred while retrieving shared scopes
     */
    public static ScopeListDTO getSharedScopes(Integer limit, Integer offset) throws APIManagementException {

        // setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();

        List<Scope> sharedScopesList = apiProvider.getAllSharedScopes(tenantDomain);
        ScopeListDTO sharedScopeListDTO = SharedScopeMappingUtil.fromScopeListToDTO(sharedScopesList, offset, limit);
        SharedScopeMappingUtil.setPaginationParams(sharedScopeListDTO, limit, offset, sharedScopesList.size());
        return sharedScopeListDTO;
    }

    /**
     * Update Shared Scope By Id
     *
     * @param scopeId  Scope Id
     * @param scopeDTO Scope DTO with update details
     * @return Updated Scope DTO
     * @throws APIManagementException If an error occurred while updating the shared scope
     */
    public static ScopeDTO updateSharedScope(String scopeId, ScopeDTO scopeDTO) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        if (StringUtils.isEmpty(scopeId)) {
            throw new APIManagementException("Shared " + ID_CANNOT_BE_NULL_OR_EMPTY,
                    ExceptionCodes.SHARED_SCOPE_ID_NOT_SPECIFIED);
        }
        if (StringUtils.isEmpty(scopeDTO.getDisplayName())) {
            throw new APIManagementException("Shared scope Display Name cannot be null or empty",
                    ExceptionCodes.SHARED_SCOPE_DISPLAY_NAME_NOT_SPECIFIED);
        }
        Scope existingScope = apiProvider.getSharedScopeByUUID(scopeId, tenantDomain);
        //Override scope Id and name in request body from existing scope
        scopeDTO.setId(existingScope.getId());
        scopeDTO.setName(existingScope.getKey());
        Scope scope = SharedScopeMappingUtil.fromDTOToScope(scopeDTO);
        apiProvider.updateSharedScope(scope, tenantDomain);
        //Get updated shared scope
        scope = apiProvider.getSharedScopeByUUID(scope.getId(), tenantDomain);
        return SharedScopeMappingUtil.fromScopeToDTO(scope);
    }
}
