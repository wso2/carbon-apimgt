package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.rest.api.admin.v1.KeyManagersApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.KeyManagerDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.KeyManagerListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.KeyManagerMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.ws.rs.core.Response;

public class KeyManagersApiServiceImpl implements KeyManagersApiService {

    private static final Log log = LogFactory.getLog(KeyManagersApiServiceImpl.class);

    public Response keyManagersGet(MessageContext messageContext) throws APIManagementException {

        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        APIAdmin apiAdmin = new APIAdminImpl();
        List<KeyManagerConfigurationDTO> keyManagerConfigurationsByTenant =
                apiAdmin.getKeyManagerConfigurationsByTenant(tenantDomain);
        KeyManagerListDTO keyManagerListDTO =
                KeyManagerMappingUtil.toKeyManagerListDTO(keyManagerConfigurationsByTenant);
        return Response.ok().entity(keyManagerListDTO).build();
    }

    public Response keyManagersKeyManagerIdDelete(String keyManagerId, MessageContext messageContext)
            throws APIManagementException {

        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        APIAdmin apiAdmin = new APIAdminImpl();
        apiAdmin.deleteKeyManagerConfigurationById(tenantDomain, keyManagerId);
        return Response.ok().build();
    }

    public Response keyManagersKeyManagerIdGet(String keyManagerId, MessageContext messageContext)
            throws APIManagementException {

        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        APIAdmin apiAdmin = new APIAdminImpl();
        KeyManagerConfigurationDTO keyManagerConfigurationDTO =
                apiAdmin.getKeyManagerConfigurationById(tenantDomain, keyManagerId);
        if (keyManagerConfigurationDTO != null) {
            KeyManagerDTO keyManagerDTO = KeyManagerMappingUtil.toKeyManagerDTO(keyManagerConfigurationDTO);
            return Response.ok(keyManagerDTO).build();
        }
        RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_KEY_MANAGER, keyManagerId, log);
        return null;
    }

    public Response keyManagersKeyManagerIdPut(String keyManagerId, KeyManagerDTO body, MessageContext messageContext) {

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

    public Response keyManagersPost(KeyManagerDTO body, MessageContext messageContext) throws APIManagementException {

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
