package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.rest.api.admin.KeyManagersApiService;
import org.wso2.carbon.apimgt.rest.api.admin.dto.KeyManagerDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.KeyManagerListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.utils.mappings.KeyManagerMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.ws.rs.core.Response;

public class KeyManagersApiServiceImpl extends KeyManagersApiService {

    private static final Log log = LogFactory.getLog(KeyManagersApiServiceImpl.class);

    @Override
    public Response keyManagersGet() {

        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        APIAdmin apiAdmin = new APIAdminImpl();
        try {
            List<KeyManagerConfigurationDTO> keyManagerConfigurationsByTenant =
                    apiAdmin.getKeyManagerConfigurationsByTenant(tenantDomain);
            KeyManagerListDTO keyManagerListDTO =
                    KeyManagerMappingUtil.toKeyManagerListDTO(keyManagerConfigurationsByTenant);
            return Response.ok().entity(keyManagerListDTO).build();
        } catch (APIManagementException e) {
            String error = "Error while retrieving Key Manager configurations for tenant " + tenantDomain;
            RestApiUtil.handleInternalServerError(error, e, log);
        }
        return null;
    }

    @Override
    public Response keyManagersKeyManagerIdDelete(String keyManagerId) {

        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        APIAdmin apiAdmin = new APIAdminImpl();
        try {
            apiAdmin.deleteKeyManagerConfigurationById(tenantDomain, keyManagerId);
            return Response.ok().build();
        } catch (APIManagementException e) {
            String error =
                    "Error while deleting Key Manager configuration for " + keyManagerId + " in tenant " + tenantDomain;
            RestApiUtil.handleInternalServerError(error, e, log);
        }
        return null;

    }

    @Override
    public Response keyManagersKeyManagerIdGet(String keyManagerId) {

        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        APIAdmin apiAdmin = new APIAdminImpl();
        try {
            KeyManagerConfigurationDTO keyManagerConfigurationDTO =
                    apiAdmin.getKeyManagerConfigurationById(tenantDomain, keyManagerId);
            if (keyManagerConfigurationDTO != null) {
                KeyManagerDTO keyManagerDTO = KeyManagerMappingUtil.toKeyManagerDTO(keyManagerConfigurationDTO);
                return Response.ok(keyManagerDTO).build();
            }   
            RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_KEY_MANAGER, keyManagerId, log);
        } catch (APIManagementException e) {
            String error =
                    "Error while Retrieving Key Manager configuration for " + keyManagerId + " in tenant " +
                            tenantDomain;
            RestApiUtil.handleInternalServerError(error, e, log);
        }
        return null;
    }

    @Override
    public Response keyManagersKeyManagerIdPut(String keyManagerId, KeyManagerDTO body) {

        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        APIAdmin apiAdmin = new APIAdminImpl();
        try {
            KeyManagerConfigurationDTO keyManagerConfigurationDTO =
                    KeyManagerMappingUtil.toKeyManagerConfigurationDTO(tenantDomain, body);
            keyManagerConfigurationDTO.setUuid(keyManagerId);
            KeyManagerConfigurationDTO oldKeyManagerConfigurationDTO =
                    apiAdmin.getKeyManagerConfigurationById(tenantDomain, keyManagerId);
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
            String error =
                    "Error while Retrieving Key Manager configuration for " + keyManagerId + " in tenant " +
                            tenantDomain;
            RestApiUtil.handleInternalServerError(error, e, log);
        }
        return null;
    }

    @Override
    public Response keyManagersPost(KeyManagerDTO body) throws APIManagementException {

        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        APIAdmin apiAdmin = new APIAdminImpl();
        try {
            KeyManagerConfigurationDTO keyManagerConfigurationDTO =
                    KeyManagerMappingUtil.toKeyManagerConfigurationDTO(tenantDomain, body);
            KeyManagerConfigurationDTO createdKeyManagerConfiguration =
                    apiAdmin.addKeyManagerConfiguration(keyManagerConfigurationDTO);
            URI location = new URI(RestApiConstants.KEY_MANAGERS + "/" + createdKeyManagerConfiguration.getUuid());
            return Response.created(location)
                    .entity(KeyManagerMappingUtil.toKeyManagerDTO(createdKeyManagerConfiguration)).build();
        } catch (URISyntaxException e) {
            String error = "Error while Creating Key Manager configuration in tenant " + tenantDomain;
            RestApiUtil.handleInternalServerError(error, e, log);
        }
        return null;
    }
}
