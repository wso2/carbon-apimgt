/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.rest.api.admin.v1.*;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.*;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.KeyManagerDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.KeyManagerListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.KeyManagerMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.exception.ForbiddenException;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;


public class GlobalKeyManagersApiServiceImpl implements GlobalKeyManagersApiService {

    private static final Log log = LogFactory.getLog(GlobalKeyManagersApiServiceImpl.class);

    public Response globalKeyManagersGet(MessageContext messageContext) throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        List<KeyManagerConfigurationDTO> globalKeyManagerConfigurations = apiAdmin.getGlobalKeyManagerConfigurations();
        KeyManagerListDTO keyManagerListDTO =
                KeyManagerMappingUtil.toKeyManagerListDTO(globalKeyManagerConfigurations);
        return Response.ok().entity(keyManagerListDTO).build();
    }

    public Response globalKeyManagersKeyManagerIdDelete(String keyManagerId, MessageContext messageContext)
            throws APIManagementException {
        checkTenantDomain();
        APIAdmin apiAdmin = new APIAdminImpl();
        apiAdmin.deleteGlobalKeyManagerConfigurationById(keyManagerId);
        return Response.ok().build();
    }

    public Response globalKeyManagersKeyManagerIdGet(String keyManagerId, MessageContext messageContext)
            throws APIManagementException {
        APIAdmin apiAdmin = new APIAdminImpl();
        KeyManagerConfigurationDTO keyManagerConfigurationDTO =
                apiAdmin.getGlobalKeyManagerConfigurationById(keyManagerId);
        if (keyManagerConfigurationDTO != null) {
            KeyManagerDTO keyManagerDTO = KeyManagerMappingUtil.toKeyManagerDTO(keyManagerConfigurationDTO);
            return Response.ok(keyManagerDTO).build();
        }
        RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_KEY_MANAGER, keyManagerId, log);
        return null;
    }

    public Response globalKeyManagersKeyManagerIdPut(String keyManagerId, KeyManagerDTO keyManagerDTO, MessageContext messageContext) {
        checkTenantDomain();
        APIAdmin apiAdmin = new APIAdminImpl();
        try {
            KeyManagerConfigurationDTO keyManagerConfigurationDTO = KeyManagerMappingUtil.toKeyManagerConfigurationDTO(
                    APIConstants.GLOBAL_KEY_MANAGER_TENANT_DOMAIN, keyManagerDTO);
            keyManagerConfigurationDTO.setUuid(keyManagerId);
            KeyManagerConfigurationDTO oldKeyManagerConfigurationDTO =
                    apiAdmin.getGlobalKeyManagerConfigurationById(keyManagerId);
            if (oldKeyManagerConfigurationDTO == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_KEY_MANAGER, keyManagerId, log);
            } else {
                if (!oldKeyManagerConfigurationDTO.getName().equals(keyManagerConfigurationDTO.getName())) {
                    RestApiUtil.handleBadRequest("Key Manager name couldn't able to change", log);
                }
                KeyManagerConfigurationDTO retrievedKeyManagerConfigurationDTO =
                        apiAdmin.updateKeyManagerConfiguration(keyManagerConfigurationDTO);
                return Response.ok(KeyManagerMappingUtil.toKeyManagerDTO(retrievedKeyManagerConfigurationDTO)).build();
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while Retrieving Global Key Manager configuration for " +
                    keyManagerId + ".", e, log);
        }
        return null;
    }

    public Response globalKeyManagersPost(KeyManagerDTO keyManagerDTO, MessageContext messageContext)
            throws APIManagementException {
        checkTenantDomain();
        APIAdmin apiAdmin = new APIAdminImpl();
        try {
            KeyManagerConfigurationDTO keyManagerConfigurationDTO =
                    KeyManagerMappingUtil.toKeyManagerConfigurationDTO(
                            APIConstants.GLOBAL_KEY_MANAGER_TENANT_DOMAIN, keyManagerDTO);
            KeyManagerConfigurationDTO createdKeyManagerConfiguration =
                    apiAdmin.addKeyManagerConfiguration(keyManagerConfigurationDTO);
            URI location = new URI(RestApiConstants.KEY_MANAGERS + "/" + createdKeyManagerConfiguration.getUuid());
            return Response.created(location)
                    .entity(KeyManagerMappingUtil.toKeyManagerDTO(createdKeyManagerConfiguration)).build();
        } catch (URISyntaxException e) {
            RestApiUtil.handleInternalServerError("Error while Creating Global Key Manager configuration.", e, log);
        }
        return null;
    }

    /**
     * Checks if the logged-in user belongs to super tenant and throws 403 error if not
     *
     * @throws ForbiddenException
     */
    private void checkTenantDomain() throws ForbiddenException {
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        if (!tenantDomain.equals(MultitenantConstants.SUPER_TENANT_DOMAIN_NAME)) {
            RestApiUtil.handleAuthorizationFailure("You are not allowed to access this resource",
                    new APIManagementException("Tenant " + tenantDomain + " is not allowed to access global key " +
                            "managers. Only super tenant is allowed"), log);
        }
    }
}