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
import org.wso2.carbon.apimgt.api.model.ApplicationInfoKeyManager;
import org.wso2.carbon.apimgt.api.model.KeyManagerApplicationUsages;
import org.wso2.carbon.apimgt.api.model.OrganizationInfo;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.kmclient.ApacheFeignHttpClient;
import org.wso2.carbon.apimgt.impl.kmclient.KMClientErrorDecoder;
import org.wso2.carbon.apimgt.impl.kmclient.model.OpenIDConnectDiscoveryClient;
import org.wso2.carbon.apimgt.impl.kmclient.model.OpenIdConnectConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.persistence.dto.AdminContentSearchResult;
import org.wso2.carbon.apimgt.rest.api.admin.v1.KeyManagersApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.KeyManagerCertificatesDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.KeyManagerDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.KeyManagerEndpointDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.KeyManagerListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.KeyManagerWellKnownResponseDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.RestApiAdminUtils;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.KeyManagerMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;

public class KeyManagersApiServiceImpl implements KeyManagersApiService {

    private static final Log log = LogFactory.getLog(KeyManagersApiServiceImpl.class);

    @Override
    public Response keyManagersDiscoverPost(String url, String type, MessageContext messageContext)
            throws APIManagementException {
        if (StringUtils.isNotEmpty(url)) {
            APIUtil.validateRemoteURL(url, RestApiCommonUtil.getLoggedInUserTenantDomain());
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
                apiAdmin.getKeyManagerConfigurationsByOrganization(organization, true);
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
            List<String> allowedOrgs = keyManagerDTO.getAllowedOrganizations();
            OrganizationInfo userOrganizationInfo = RestApiUtil.getOrganizationInfo(messageContext);
            if (userOrganizationInfo != null) {
                if (allowedOrgs != null && allowedOrgs.size() == 1 && allowedOrgs.contains(
                        userOrganizationInfo.getOrganizationId())) {
                    allowedOrgs.clear();
                    allowedOrgs.add("NONE");
                    keyManagerDTO.setAllowedOrganizations(allowedOrgs);
                }
            }
            return Response.ok(keyManagerDTO).build();
        }
        throw new APIManagementException("Requested KeyManager not found", ExceptionCodes.KEY_MANAGER_NOT_FOUND);
    }

    public Response keyManagersKeyManagerIdPut(String keyManagerId, KeyManagerDTO body, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getOrganization(messageContext);
        APIAdmin apiAdmin = new APIAdminImpl();

        List<String> allowedOrgs = body.getAllowedOrganizations();
        if (allowedOrgs != null && allowedOrgs.contains("NONE")) {
            OrganizationInfo userOrganizationInfo = RestApiUtil.getOrganizationInfo(messageContext);
            if (userOrganizationInfo != null) {
                allowedOrgs.clear();
                allowedOrgs.add(userOrganizationInfo.getOrganizationId());
                body.setAllowedOrganizations(allowedOrgs);
            }
        }
        validateKeyManagerURLs(body);
        try {
            KeyManagerConfigurationDTO keyManagerConfigurationDTO =
                    KeyManagerMappingUtil.toKeyManagerConfigurationDTO(organization, body);
            KeyManagerPermissionConfigurationDTO keyManagerPermissionConfigurationDTO =
                    keyManagerConfigurationDTO.getPermissions();
            RestApiAdminUtils.validateKeyManagerConstraints(keyManagerConfigurationDTO.getAdditionalProperties());
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

    @Override
    public Response keyManagersKeyManagerIdApiUsagesGet(String keyManagerId, Integer offset, Integer limit,
            MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getOrganization(messageContext);
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        APIAdminImpl apiAdmin = new APIAdminImpl();
        KeyManagerConfigurationDTO keyManager = apiAdmin.getKeyManagerConfigurationById(organization, keyManagerId);
        if (keyManager == null) {
            keyManager = apiAdmin.getGlobalKeyManagerConfigurationById(keyManagerId);
        }
        String KeyManagerName;
        if (keyManager != null) {
            KeyManagerName = keyManager.getName();
        } else {
            throw new APIManagementException("Requested KeyManager not found", ExceptionCodes.KEY_MANAGER_NOT_FOUND);
        }
        AdminContentSearchResult result = apiAdmin.getAPIUsagesByKeyManagerNameAndOrganization(organization,
                KeyManagerName, offset, limit);

        return Response.ok().entity(KeyManagerMappingUtil.toKeyManagerAPIUsagesDTO(result)).build();
    }

    @Override
    public Response keyManagersKeyManagerIdAppUsagesGet(String keyManagerId, Integer offset, Integer limit,
            MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getOrganization(messageContext);
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        limit = limit != null ? limit : Integer.MAX_VALUE;
        APIAdminImpl apiAdmin = new APIAdminImpl();
        KeyManagerConfigurationDTO keyManager = apiAdmin.getKeyManagerConfigurationById(organization, keyManagerId);
        if (keyManager == null) {
            keyManager = apiAdmin.getGlobalKeyManagerConfigurationById(keyManagerId);
        }
        if (keyManager == null) {
            throw new APIManagementException("Requested KeyManager not found", ExceptionCodes.KEY_MANAGER_NOT_FOUND);
        }
        KeyManagerApplicationUsages result = apiAdmin.getApplicationsOfKeyManager(keyManagerId, offset, limit);
        return Response.ok().entity(KeyManagerMappingUtil.toKeyManagerAppUsagesDTO(result)).build();
    }

    public Response keyManagersPost(KeyManagerDTO body, MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getOrganization(messageContext);
        APIAdmin apiAdmin = new APIAdminImpl();
        try {
            validateKeyManagerURLs(body);
            KeyManagerConfigurationDTO keyManagerConfigurationDTO =
                    KeyManagerMappingUtil.toKeyManagerConfigurationDTO(organization, body);
            KeyManagerPermissionConfigurationDTO keyManagerPermissionConfigurationDTO =
                    keyManagerConfigurationDTO.getPermissions();
            RestApiAdminUtils.validateKeyManagerConstraints(keyManagerConfigurationDTO.getAdditionalProperties());
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

    /**
     * Validates all outbound URLs defined in the given Key Manager configuration against
     * network security access control policies. Blank and non-URL values are silently skipped.
     * If a URL fails validation with a client error (HTTP 400), a field-specific bad request
     * is returned. Internal errors are propagated unchanged.
     *
     * @param body Key Manager configuration containing URLs to validate
     * @throws APIManagementException if URL validation fails
     */
    private void validateKeyManagerURLs(KeyManagerDTO body) throws APIManagementException {
        Map<String, String> urlFields = new LinkedHashMap<>();
        urlFields.put("well-known endpoint", body.getWellKnownEndpoint());
        urlFields.put("token endpoint", body.getTokenEndpoint());
        urlFields.put("introspection endpoint", body.getIntrospectionEndpoint());
        urlFields.put("client registration endpoint", body.getClientRegistrationEndpoint());
        urlFields.put("revoke endpoint", body.getRevokeEndpoint());
        urlFields.put("user info endpoint", body.getUserInfoEndpoint());
        urlFields.put("authorize endpoint", body.getAuthorizeEndpoint());
        urlFields.put("scope management endpoint", body.getScopeManagementEndpoint());

        for (Map.Entry<String, String> entry : urlFields.entrySet()) {
            validateKeyManagerURLOrBadRequest(entry.getValue(), entry.getKey());
        }
        if (body.getEndpoints() != null) {
            for (KeyManagerEndpointDTO endpoint : body.getEndpoints()) {
                if (endpoint != null) {
                    validateKeyManagerURLOrBadRequest(endpoint.getValue(),
                            "custom endpoint '" + endpoint.getName() + "'");
                }
            }
        }
        if (body.getCertificates() != null
                && KeyManagerCertificatesDTO.TypeEnum.JWKS.equals(body.getCertificates().getType())) {
            validateKeyManagerURLOrBadRequest(body.getCertificates().getValue(), "JWKS endpoint");
        }
    }

    /**
     * Validates a single Key Manager URL and translates a client error (HTTP 400) into a bad request response.
     * Failures that are not client errors are propagated unchanged.
     *
     * @param url       URL to validate; blank and non-URL values are silently skipped
     * @param fieldName descriptive name of the Key Manager URL field being validated
     * @throws APIManagementException if URL validation fails with a non-client error
     */
    private void validateKeyManagerURLOrBadRequest(String url, String fieldName) throws APIManagementException {
        try {
            validateKeyManagerURL(url, fieldName);
        } catch (APIManagementException e) {
            if (e.getErrorHandler() != null && e.getErrorHandler().getHttpStatusCode() == 400) {
                log.warn(e.getMessage(), e);
                RestApiUtil.handleBadRequest(e.getMessage());
            } else {
                throw e;
            }
        }
    }

    /**
     * Validates a single Key Manager endpoint URL against network security access control policies.
     * Blank and non-URL values (e.g. "none") are silently skipped for backward compatibility.
     * If validation fails with a client error (HTTP 400), the exception is re-thrown with a
     * field-specific message. Other failures are propagated unchanged.
     *
     * @param url       URL to validate; blank and non-URL values are silently skipped
     * @param fieldName descriptive name of the Key Manager URL field being validated
     * @throws APIManagementException if the URL is blocked by a host validation policy
     */
    private void validateKeyManagerURL(String url, String fieldName)
            throws APIManagementException {
        if (StringUtils.isBlank(url)) {
            return;
        }
        URI parsedUrl;
        try {
            parsedUrl = new URI(url);
        } catch (URISyntaxException e) {
            return; // not a URI, skip validation
        }
        // Only an absolute URL (scheme + host) is outbound-fetchable. Non-URL sentinels such as "none" and relative
        // values are not, so skip them for backward compatibility instead of failing them as malformed.
        if (parsedUrl.getScheme() == null || StringUtils.isBlank(parsedUrl.getHost())) {
            return;
        }
        try {
            APIUtil.validateRemoteURL(url, RestApiCommonUtil.getLoggedInUserTenantDomain());
        } catch (APIManagementException e) {
            throw toKeyManagerUrlError(e, fieldName);
        }
    }

    /**
     * Maps a Key Manager URL validation failure to the exception to surface. Only a policy block (UNTRUSTED_URL) means
     * the URL is untrusted, so that is re-thrown with a field-specific message; any other error (e.g. a malformed URL)
     * is propagated unchanged so its message stays accurate.
     *
     * @param e         the validation failure raised by {@code validateRemoteURL}
     * @param fieldName descriptive name of the Key Manager URL field being validated
     * @return the exception to throw
     */
    private APIManagementException toKeyManagerUrlError(APIManagementException e, String fieldName) {
        if (e.getErrorHandler() != null
                && e.getErrorHandler().getErrorCode() == ExceptionCodes.UNTRUSTED_URL.getErrorCode()) {
            return new APIManagementException(
                    "Invalid Key Manager URL configuration. The " + fieldName
                            + " URL is not trusted. Please contact the system administrator.",
                    e.getErrorHandler());
        }
        return e;
    }
}
