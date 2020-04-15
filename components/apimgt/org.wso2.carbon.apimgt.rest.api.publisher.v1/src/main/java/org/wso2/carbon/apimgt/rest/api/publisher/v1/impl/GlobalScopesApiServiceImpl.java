/*
 * Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GlobalScopeListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ScopeDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings.GlobalScopeMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.ws.rs.core.Response;

public class GlobalScopesApiServiceImpl implements GlobalScopesApiService {

    public static final Log log = LogFactory.getLog(GlobalScopesApiServiceImpl.class);

    /**
     * Add Global Scope.
     *
     * @param body           Scope DTO object to add
     * @param messageContext CXF Message Context
     * @return Created Scope as DTO
     * @throws APIManagementException If an error occurs while adding global scope.
     */
    public Response addGlobalScope(ScopeDTO body, MessageContext messageContext) throws APIManagementException {

        String scopeName = body.getName();
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
            if (StringUtils.isEmpty(scopeName)) {
                throw new APIManagementException("Global Scope Name cannot be null or empty",
                        ExceptionCodes.GLOBAL_SCOPE_NAME_NOT_SPECIFIED);
            }
            if (apiProvider.isGlobalScopeNameExists(scopeName, tenantDomain)) {
                throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.GLOBAL_SCOPE_ALREADY_REGISTERED,
                        scopeName));
            }
            Scope scopeToAdd = GlobalScopeMappingUtil.fromDTOToScope(body);
            String globalScopeId = apiProvider.addGlobalScope(scopeToAdd, tenantDomain);
            //Get registered global scope
            Scope createdScope = apiProvider.getGlobalScopeByUUID(globalScopeId, tenantDomain);
            ScopeDTO createdScopeDTO = GlobalScopeMappingUtil.fromScopeToDTO(createdScope);
            String createdScopeURIString = RestApiConstants.RESOURCE_PATH_GLOBAL_SCOPES_SCOPE_ID
                    .replace(RestApiConstants.GLOBAL_SCOPE_ID_PARAM, createdScopeDTO.getId());
            URI createdScopeURI = new URI(createdScopeURIString);
            return Response.created(createdScopeURI).entity(createdScopeDTO).build();
        } catch (URISyntaxException e) {
            throw new APIManagementException("Error while creating global scope: " + scopeName, e);
        }
    }

    /**
     * Delete global scope.
     *
     * @param scopeId        Scope UUID
     * @param messageContext CXF Message Context
     * @return Deletion Response
     * @throws APIManagementException If an error occurs while deleting global scope
     */
    public Response deleteGlobalScope(String scopeId, MessageContext messageContext) throws APIManagementException {

        APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        if (StringUtils.isEmpty(scopeId)) {
            throw new APIManagementException("Scope Id cannot be null or empty",
                    ExceptionCodes.GLOBAL_SCOPE_ID_NOT_SPECIFIED);
        }
        Scope existingScope = apiProvider.getGlobalScopeByUUID(scopeId, tenantDomain);
        if (apiProvider.isScopeKeyAssignedToAPI(existingScope.getName(), tenantDomain)) {
            RestApiUtil.handleConflict("Cannot remove the Global Scope " + scopeId + " as it is used by one "
                    + "or more APIs", log);
        }
        apiProvider.deleteGlobalScope(scopeId, tenantDomain);
        return Response.ok().build();
    }

    /**
     * Get global scope by Id.
     *
     * @param scopeId        UUID of the scope
     * @param messageContext CXF Message Context
     * @return Global Scope DTO
     * @throws APIManagementException If an error occurs while getting global scope
     */
    public Response getGlobalScope(String scopeId, MessageContext messageContext) throws APIManagementException {

        APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        if (StringUtils.isEmpty(scopeId)) {
            throw new APIManagementException("Scope Id cannot be null or empty",
                    ExceptionCodes.GLOBAL_SCOPE_ID_NOT_SPECIFIED);
        }
        Scope scope = apiProvider.getGlobalScopeByUUID(scopeId, tenantDomain);
        ScopeDTO scopeDTO = GlobalScopeMappingUtil.fromScopeToDTO(scope);
        return Response.ok().entity(scopeDTO).build();
    }

    /**
     * Get all global scopes for tenant.
     * TODO: Add pagination from rest api layer only
     *
     * @param messageContext CXF Message Context
     * @return Global Scopes DTO List
     * @throws APIManagementException if an error occurs while retrieving global scope
     */
    public Response getGlobalScopes(MessageContext messageContext) throws APIManagementException {

        APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();

        List<Scope> scopeList = apiProvider.getAllGlobalScopes(tenantDomain);
        GlobalScopeListDTO globalScopeListDTO = GlobalScopeMappingUtil.fromScopeListToDTO(scopeList);
        return Response.ok().entity(globalScopeListDTO).build();
    }

    /**
     * Update Global Scope By Id.
     *
     * @param scopeId        Global Scope Id
     * @param body           Global Scope DTO
     * @param messageContext CXF Message Context
     * @return Updated Global Scope DTO
     * @throws APIManagementException if an error occurs while updating global scope
     */
    public Response updateGlobalScope(String scopeId, ScopeDTO body, MessageContext messageContext)
            throws APIManagementException {

        String scopeName = body.getName();
        APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        if (StringUtils.isEmpty(scopeName)) {
            throw new APIManagementException("Global Scope Name cannot be null or empty",
                    ExceptionCodes.GLOBAL_SCOPE_NAME_NOT_SPECIFIED);
        }
        Scope existingScope = apiProvider.getGlobalScopeByUUID(scopeId, tenantDomain);
        //Override scope Id and name in request body from existing scope
        body.setId(existingScope.getId());
        body.setName(existingScope.getName());
        Scope scope = GlobalScopeMappingUtil.fromDTOToScope(body);
        apiProvider.updateGlobalScope(scope, tenantDomain);
        //Get updated global scope
        scope = apiProvider.getGlobalScopeByUUID(scope.getId(), tenantDomain);
        ScopeDTO updatedScopeDTO = GlobalScopeMappingUtil.fromScopeToDTO(scope);
        return Response.ok().entity(updatedScopeDTO).build();
    }
}
