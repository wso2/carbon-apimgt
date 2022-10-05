/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.ScopesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.impl.ScopesApiCommonImpl;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ScopeDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ScopeListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SharedScopeUsageDTO;

import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.core.Response;

/**
 * This is the service implementation class for scopes related operations.
 */
public class ScopesApiServiceImpl implements ScopesApiService {

    /**
     * Check whether the given scope already used in APIs.
     *
     * @param name Base64 URL encoded form of scope name -Base64URLEncode{scope name}
     * @param messageContext
     * @return boolean to indicate existence
     */
    @Override
    public Response validateScope(String name, MessageContext messageContext) throws APIManagementException {

        ScopesApiCommonImpl.validateScope(name);
        return Response.status(Response.Status.OK).build();
    }

    /**
     * Add Shared Scope.
     *
     * @param scopeDTO           Scope DTO object to add
     * @param messageContext CXF Message Context
     * @return Created Scope as DTO
     * @throws APIManagementException If an error occurs while adding shared scope.
     */
    @Override
    public Response addSharedScope(ScopeDTO scopeDTO, MessageContext messageContext) throws APIManagementException {

        ScopeDTO createdScopeDTO = ScopesApiCommonImpl.addSharedScope(scopeDTO);
        String createdScopeURIString = RestApiConstants.RESOURCE_PATH_SHARED_SCOPES_SCOPE_ID
                .replace(RestApiConstants.SHARED_SCOPE_ID_PARAM, createdScopeDTO.getId());
        try {
            URI createdScopeURI = new URI(createdScopeURIString);
            return Response.created(createdScopeURI).entity(createdScopeDTO).build();
        } catch (URISyntaxException e) {
            throw new APIManagementException("Error while creating URI for shared scope", e,
                    ExceptionCodes.from(ExceptionCodes.ERROR_CREATING_URI_FOR_SHARED_SCOPE, scopeDTO.getName()));
        }
    }

    /**
     * Delete shared scope.
     *
     * @param scopeId        Scope UUID
     * @param messageContext CXF Message Context
     * @return Deletion Response
     * @throws APIManagementException If an error occurs while deleting shared scope
     */
    @Override
    public Response deleteSharedScope(String scopeId, MessageContext messageContext) throws APIManagementException {

        ScopesApiCommonImpl.deleteSharedScope(scopeId);
        return Response.ok().build();
    }

    /**
     * Get shared scope by Id.
     *
     * @param scopeId        UUID of the scope
     * @param messageContext CXF Message Context
     * @return Shared Scope DTO
     * @throws APIManagementException If an error occurs while getting shared scope
     */
    @Override
    public Response getSharedScope(String scopeId, MessageContext messageContext) throws APIManagementException {

        ScopeDTO scopeDTO = ScopesApiCommonImpl.getSharedScope(scopeId);
        return Response.ok().entity(scopeDTO).build();
    }

    @Override
    public Response getSharedScopeUsages(String scopeId, MessageContext messageContext)
            throws APIManagementException {

        SharedScopeUsageDTO sharedScopeUsageDTO = ScopesApiCommonImpl.getSharedScopeUsages(scopeId);
        return Response.ok().entity(sharedScopeUsageDTO).build();
    }

    /**
     * Get all shared scopes for tenant.
     *
     * @param messageContext CXF Message Context
     * @return Shared Scopes DTO List
     * @throws APIManagementException if an error occurs while retrieving shared scope
     */
    @Override
    public Response getSharedScopes(Integer limit, Integer offset, MessageContext messageContext)
            throws APIManagementException {

        ScopeListDTO sharedScopeListDTO = ScopesApiCommonImpl.getSharedScopes(limit, offset);
        return Response.ok().entity(sharedScopeListDTO).build();
    }

    /**
     * Update Shared Scope By Id.
     *
     * @param scopeId        Shared Scope Id
     * @param scopeDTO           Shared Scope DTO
     * @param messageContext CXF Message Context
     * @return Updated Shared Scope DTO
     * @throws APIManagementException if an error occurs while updating shared scope
     */
    @Override
    public Response updateSharedScope(String scopeId, ScopeDTO scopeDTO, MessageContext messageContext)
            throws APIManagementException {

        ScopeDTO updatedScopeDTO = ScopesApiCommonImpl.updateSharedScope(scopeId, scopeDTO);
        return Response.ok().entity(updatedScopeDTO).build();
    }
}
