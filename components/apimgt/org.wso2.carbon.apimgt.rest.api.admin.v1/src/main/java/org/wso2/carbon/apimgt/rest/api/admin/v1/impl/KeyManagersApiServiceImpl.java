package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.api.dto.KeyManagerPermissionConfigurationDTO;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.kmclient.ApacheFeignHttpClient;
import org.wso2.carbon.apimgt.impl.kmclient.KMClientErrorDecoder;
import org.wso2.carbon.apimgt.impl.kmclient.model.OpenIDConnectDiscoveryClient;
import org.wso2.carbon.apimgt.impl.kmclient.model.OpenIdConnectConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.admin.v1.KeyManagersApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.KeyManagerDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.KeyManagerListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.KeyManagerWellKnownResponseDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.KeyManagerMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import javax.ws.rs.core.Response;

import static org.wso2.carbon.utils.multitenancy.MultitenantConstants.SUPER_TENANT_DOMAIN_NAME;

public class KeyManagersApiServiceImpl implements KeyManagersApiService {

    private static final Log log = LogFactory.getLog(KeyManagersApiServiceImpl.class);

    @Override
    public Response keyManagersDiscoverPost(String url, String type, MessageContext messageContext)
            throws APIManagementException {
        if (StringUtils.isNotEmpty(url)) {
            Gson gson = new GsonBuilder().serializeNulls().create();
            OpenIDConnectDiscoveryClient openIDConnectDiscoveryClient =
                    Feign.builder().client(new ApacheFeignHttpClient(APIUtil.getHttpClient(url)))
                            .encoder(new GsonEncoder(gson)).decoder(new GsonDecoder(gson))
                            .errorDecoder(new KMClientErrorDecoder())
                            .target(OpenIDConnectDiscoveryClient.class, url);
            OpenIdConnectConfiguration openIdConnectConfiguration =
                    openIDConnectDiscoveryClient.getOpenIdConnectConfiguration();
            if (openIdConnectConfiguration != null) {
                KeyManagerWellKnownResponseDTO keyManagerWellKnownResponseDTO = KeyManagerMappingUtil
                        .fromOpenIdConnectConfigurationToKeyManagerConfiguration(openIdConnectConfiguration);
                keyManagerWellKnownResponseDTO.getValue().setWellKnownEndpoint(url);
                keyManagerWellKnownResponseDTO.getValue().setType(type);
                return Response.ok().entity(keyManagerWellKnownResponseDTO).build();
            }

        }
        return Response.ok(new KeyManagerWellKnownResponseDTO()).build();
    }

    public Response keyManagersGet(MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getOrganization(messageContext);
        APIAdmin apiAdmin = new APIAdminImpl();
        List<KeyManagerConfigurationDTO> keyManagerConfigurationsByOrganization =
                apiAdmin.getKeyManagerConfigurationsByOrganization(organization);
        KeyManagerListDTO keyManagerListDTO =
                KeyManagerMappingUtil.toKeyManagerListDTO(keyManagerConfigurationsByOrganization);
        return Response.ok().entity(keyManagerListDTO).build();
    }

    public Response keyManagersKeyManagerIdDelete(String keyManagerId, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getOrganization(messageContext);

        APIAdmin apiAdmin = new APIAdminImpl();
        KeyManagerConfigurationDTO keyManagerConfigurationDTO =
                apiAdmin.getKeyManagerConfigurationById(organization, keyManagerId);
        if (keyManagerConfigurationDTO != null) {
            apiAdmin.deleteKeyManagerConfigurationById(organization, keyManagerConfigurationDTO);
            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.KEY_MANAGER,
                    new Gson().toJson(keyManagerConfigurationDTO), APIConstants.AuditLogConstants.DELETED,
                    RestApiCommonUtil.getLoggedInUsername());
            return Response.ok().build();
        } else {
            throw new APIManagementException("Requested KeyManager not found", ExceptionCodes.KEY_MANAGER_NOT_FOUND);
        }
    }

    public Response keyManagersKeyManagerIdGet(String keyManagerId, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getOrganization(messageContext);
        APIAdmin apiAdmin = new APIAdminImpl();
        KeyManagerConfigurationDTO keyManagerConfigurationDTO =
                apiAdmin.getKeyManagerConfigurationById(organization, keyManagerId);
        if (keyManagerConfigurationDTO != null) {
            KeyManagerDTO keyManagerDTO = KeyManagerMappingUtil.toKeyManagerDTO(keyManagerConfigurationDTO);
            return Response.ok(keyManagerDTO).build();
        }
        throw new APIManagementException("Requested KeyManager not found", ExceptionCodes.KEY_MANAGER_NOT_FOUND);
    }

    public Response keyManagersKeyManagerIdPut(String keyManagerId, KeyManagerDTO body, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getOrganization(messageContext);
        APIAdmin apiAdmin = new APIAdminImpl();
        try {
            KeyManagerConfigurationDTO keyManagerConfigurationDTO =
                    KeyManagerMappingUtil.toKeyManagerConfigurationDTO(organization, body);
            KeyManagerPermissionConfigurationDTO keyManagerPermissionConfigurationDTO =
                    keyManagerConfigurationDTO.getPermissions();
            this.validatePermissions(keyManagerPermissionConfigurationDTO);
            keyManagerConfigurationDTO.setUuid(keyManagerId);
            KeyManagerConfigurationDTO oldKeyManagerConfigurationDTO =
                    apiAdmin.getKeyManagerConfigurationById(organization, keyManagerId);
            if (oldKeyManagerConfigurationDTO == null) {
                throw new APIManagementException("Requested KeyManager not found",
                        ExceptionCodes.KEY_MANAGER_NOT_FOUND);
            } else {
                if (!oldKeyManagerConfigurationDTO.getName().equals(keyManagerConfigurationDTO.getName())) {
                    RestApiUtil.handleBadRequest("Key Manager name couldn't able to change", log);
                }
                KeyManagerConfigurationDTO retrievedKeyManagerConfigurationDTO =
                        apiAdmin.updateKeyManagerConfiguration(keyManagerConfigurationDTO);
                APIUtil.logAuditMessage(APIConstants.AuditLogConstants.KEY_MANAGER,
                        new Gson().toJson(keyManagerConfigurationDTO),
                        APIConstants.AuditLogConstants.UPDATED, RestApiCommonUtil.getLoggedInUsername());
                return Response.ok(KeyManagerMappingUtil.toKeyManagerDTO(retrievedKeyManagerConfigurationDTO)).build();
            }
        } catch (APIManagementException e) {
            String error =
                    "Error while updating Key Manager configuration for " + keyManagerId + " in organization " +
                            organization;
            throw new APIManagementException(error, e, ExceptionCodes.INTERNAL_ERROR);
        } catch (IllegalArgumentException e) {
            String error = "Error while storing key manager permissions with name "
                    + body.getName() + " in tenant " + organization;
            throw new APIManagementException(error, e, ExceptionCodes.ROLE_DOES_NOT_EXIST);
        }
    }

    public Response keyManagersPost(KeyManagerDTO body, MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getOrganization(messageContext);
        APIAdmin apiAdmin = new APIAdminImpl();
        try {
            KeyManagerConfigurationDTO keyManagerConfigurationDTO =
                    KeyManagerMappingUtil.toKeyManagerConfigurationDTO(organization, body);
            KeyManagerPermissionConfigurationDTO keyManagerPermissionConfigurationDTO =
                    keyManagerConfigurationDTO.getPermissions();
            this.validatePermissions(keyManagerPermissionConfigurationDTO);
            KeyManagerConfigurationDTO createdKeyManagerConfiguration =
                    apiAdmin.addKeyManagerConfiguration(keyManagerConfigurationDTO);
            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.KEY_MANAGER,
                    new Gson().toJson(keyManagerConfigurationDTO),
                    APIConstants.AuditLogConstants.CREATED, RestApiCommonUtil.getLoggedInUsername());
            URI location = new URI(RestApiConstants.KEY_MANAGERS + "/" + createdKeyManagerConfiguration.getUuid());
            return Response.created(location)
                    .entity(KeyManagerMappingUtil.toKeyManagerDTO(createdKeyManagerConfiguration)).build();
        } catch (URISyntaxException e) {
            String error = "Error while creating Key Manager configuration in organization " + organization;
            throw new APIManagementException(error, e, ExceptionCodes.INTERNAL_ERROR);
        } catch (IllegalArgumentException e) {
            String error = "Error while storing Key Manager permission roles with name "
                    + body.getName() + " in tenant " + organization;
            throw new APIManagementException(error, e, ExceptionCodes.ROLE_DOES_NOT_EXIST);
        }
    }

    public void validatePermissions(KeyManagerPermissionConfigurationDTO permissionDTO)
            throws IllegalArgumentException, APIManagementException {

        if (permissionDTO != null && permissionDTO.getRoles() != null) {
            String username = RestApiCommonUtil.getLoggedInUsername();
            String[] allowedPermissionTypes = {"PUBLIC", "ALLOW", "DENY"};
            String permissionType = permissionDTO.getPermissionType();
            if (!Arrays.stream(allowedPermissionTypes).anyMatch(permissionType::equals)) {
                throw new APIManagementException("Invalid permission type");
            }
            for (String role : permissionDTO.getRoles()) {
                if (!APIUtil.isRoleNameExist(username, role)) {
                    throw new IllegalArgumentException("Invalid user roles found in visibleRoles list");
                }
            }
        }
    }

}
