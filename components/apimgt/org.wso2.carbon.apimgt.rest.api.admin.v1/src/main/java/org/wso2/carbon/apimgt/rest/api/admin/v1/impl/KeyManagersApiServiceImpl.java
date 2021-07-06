package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import feign.Feign;
import feign.gson.GsonDecoder;
import feign.gson.GsonEncoder;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIAdmin;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.dto.KeyManagerConfigurationDTO;
import org.wso2.carbon.apimgt.api.model.KeyManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIAdminImpl;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.kmclient.ApacheFeignHttpClient;
import org.wso2.carbon.apimgt.impl.kmclient.KMClientErrorDecoder;
import org.wso2.carbon.apimgt.impl.kmclient.model.OpenIDConnectDiscoveryClient;
import org.wso2.carbon.apimgt.impl.kmclient.model.OpenIdConnectConfiguration;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.admin.v1.KeyManagersApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ClaimMappingEntryDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.KeyManagerCertificatesDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.KeyManagerDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.KeyManagerListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.KeyManagerWellKnownResponseDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.KeyManagerMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.identity.application.common.model.ClaimConfig;
import org.wso2.carbon.identity.application.common.model.ClaimMapping;
import org.wso2.carbon.identity.application.common.model.IdentityProvider;
import org.wso2.carbon.identity.application.common.model.IdentityProviderProperty;
import org.wso2.carbon.identity.application.common.util.IdentityApplicationConstants;
import org.wso2.carbon.idp.mgt.IdentityProviderManagementException;
import org.wso2.carbon.idp.mgt.IdentityProviderManager;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import javax.ws.rs.core.Response;

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
            if (openIdConnectConfiguration != null){
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
        for (KeyManagerConfigurationDTO keyManagerConfigurationDTO: keyManagerConfigurationsByOrganization) {
            if (StringUtils.equals(KeyManagerConfiguration.TokenType.EXCHANGED.toString(),
                    keyManagerConfigurationDTO.getTokenType())) {
                try {
                    if (keyManagerConfigurationDTO.getExternalReferenceId() != null) {
                        IdentityProvider identityProvider = IdentityProviderManager.getInstance()
                                .getIdPByResourceId(keyManagerConfigurationDTO.getExternalReferenceId(),
                                        APIUtil.getTenantDomainFromTenantId(
                                                APIUtil.getInternalOrganizationId(organization)), Boolean.FALSE);
                        // Only two parameters that are common to IdentityProvider object and the KeyManagerInfoDTO (that
                        // will be used in KeyManagerMappingUtil.toKeyManagerListDTO) are the description and the enabled.
                        keyManagerConfigurationDTO.setDescription(identityProvider.getIdentityProviderDescription());
                        keyManagerConfigurationDTO.setEnabled(identityProvider.isEnable());
                    }
                } catch (IdentityProviderManagementException e) {
                    throw new APIManagementException("IdP retrieval failed. " + e.getMessage(), e,
                            ExceptionCodes.IDP_RETRIEVAL_FAILED);
                }
            }
        }
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
        if (StringUtils.equals(KeyManagerConfiguration.TokenType.EXCHANGED.toString(),
                keyManagerConfigurationDTO.getTokenType())) {
            try {
                if (keyManagerConfigurationDTO.getExternalReferenceId() != null) {
                    IdentityProviderManager.getInstance()
                            .deleteIdPByResourceId(keyManagerConfigurationDTO.getExternalReferenceId(),
                                    APIUtil.getInternalOrganizationDomain(organization));
                }
            } catch (IdentityProviderManagementException e) {
                throw new APIManagementException("IdP deletion failed. " + e.getMessage(), e,
                        ExceptionCodes.IDP_DELETION_FAILED);
            }
        }
        apiAdmin.deleteKeyManagerConfigurationById(organization, keyManagerId);
        APIUtil.logAuditMessage(APIConstants.AuditLogConstants.KEY_MANAGER,
                new Gson().toJson(keyManagerConfigurationDTO), APIConstants.AuditLogConstants.DELETED,
                RestApiCommonUtil.getLoggedInUsername());
        return Response.ok().build();
    }

    public Response keyManagersKeyManagerIdGet(String keyManagerId, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getOrganization(messageContext);
        APIAdmin apiAdmin = new APIAdminImpl();
        KeyManagerConfigurationDTO keyManagerConfigurationDTO =
                apiAdmin.getKeyManagerConfigurationById(organization, keyManagerId);
        if (keyManagerConfigurationDTO != null) {
            KeyManagerDTO keyManagerDTO = KeyManagerMappingUtil.toKeyManagerDTO(keyManagerConfigurationDTO);
            if (StringUtils.equals(KeyManagerConfiguration.TokenType.EXCHANGED.toString(),
                    keyManagerConfigurationDTO.getTokenType())) {
                try {
                    if (keyManagerConfigurationDTO.getExternalReferenceId() != null) {
                        IdentityProvider identityProvider = IdentityProviderManager.getInstance()
                                .getIdPByResourceId(keyManagerConfigurationDTO.getExternalReferenceId(),
                        APIUtil.getInternalOrganizationDomain(organization), Boolean.FALSE);
                        mergeIdpWithKeyManagerConfiguration(identityProvider, keyManagerDTO);
                    }
                } catch (IdentityProviderManagementException e) {
                    throw new APIManagementException("IdP retrieval failed. " + e.getMessage(), e,
                            ExceptionCodes.IDP_RETRIEVAL_FAILED);
                }
            }
            return Response.ok(keyManagerDTO).build();
        }
        RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_KEY_MANAGER, keyManagerId, log);
        return null;
    }

    public Response keyManagersKeyManagerIdPut(String keyManagerId, KeyManagerDTO body, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getOrganization(messageContext);
        APIAdmin apiAdmin = new APIAdminImpl();
        try {
            KeyManagerConfigurationDTO keyManagerConfigurationDTO =
                    KeyManagerMappingUtil.toKeyManagerConfigurationDTO(organization, body);
            validateIdpTypeFromTokenType(keyManagerConfigurationDTO);
            keyManagerConfigurationDTO.setUuid(keyManagerId);
            KeyManagerConfigurationDTO oldKeyManagerConfigurationDTO =
                    apiAdmin.getKeyManagerConfigurationById(organization, keyManagerId);
            if (oldKeyManagerConfigurationDTO == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_KEY_MANAGER, keyManagerId, log);
            } else {
                if (!oldKeyManagerConfigurationDTO.getName().equals(keyManagerConfigurationDTO.getName())) {
                    RestApiUtil.handleBadRequest("Key Manager name couldn't able to change", log);
                }
                if (StringUtils.equals(KeyManagerConfiguration.TokenType.EXCHANGED.toString(),
                        body.getTokenType().toString())) {
                    IdentityProvider identityProvider = IdentityProviderManager.getInstance()
                            .updateIdPByResourceId(oldKeyManagerConfigurationDTO.getExternalReferenceId(),
                                    createIdp(keyManagerConfigurationDTO, body, organization),
                                    APIUtil.getInternalOrganizationDomain(organization));
                    keyManagerConfigurationDTO.setExternalReferenceId(identityProvider.getResourceId());
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
                    "Error while Retrieving Key Manager configuration for " + keyManagerId + " in organization " +
                            organization;
            RestApiUtil.handleInternalServerError(error, e, log);
        } catch (IdentityProviderManagementException e) {
            throw new APIManagementException("IdP adding failed. " + e.getMessage(), e,
                    ExceptionCodes.IDP_ADDING_FAILED);
        }
        return null;
    }

    public Response keyManagersPost(KeyManagerDTO body, MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getOrganization(messageContext);
        APIAdmin apiAdmin = new APIAdminImpl();
        try {
            KeyManagerConfigurationDTO keyManagerConfigurationDTO =
                    KeyManagerMappingUtil.toKeyManagerConfigurationDTO(organization, body);
            validateIdpTypeFromTokenType(keyManagerConfigurationDTO);
            if (StringUtils
                    .equals(KeyManagerConfiguration.TokenType.EXCHANGED.toString(), body.getTokenType().toString())) {
                keyManagerConfigurationDTO.setUuid(UUID.randomUUID().toString());
                IdentityProvider identityProvider = IdentityProviderManager.getInstance()
                        .addIdPWithResourceId(createIdp(keyManagerConfigurationDTO, body, organization),
                                APIUtil.getInternalOrganizationDomain(organization));
                keyManagerConfigurationDTO.setExternalReferenceId(identityProvider.getResourceId());
            }
            KeyManagerConfigurationDTO createdKeyManagerConfiguration =
                    apiAdmin.addKeyManagerConfiguration(keyManagerConfigurationDTO);
            APIUtil.logAuditMessage(APIConstants.AuditLogConstants.KEY_MANAGER,
                    new Gson().toJson(keyManagerConfigurationDTO),
                    APIConstants.AuditLogConstants.CREATED, RestApiCommonUtil.getLoggedInUsername());
            URI location = new URI(RestApiConstants.KEY_MANAGERS + "/" + createdKeyManagerConfiguration.getUuid());
            return Response.created(location)
                    .entity(KeyManagerMappingUtil.toKeyManagerDTO(createdKeyManagerConfiguration)).build();
        } catch (URISyntaxException e) {
            String error = "Error while Creating Key Manager configuration in organization " + organization;
            RestApiUtil.handleInternalServerError(error, e, log);
        } catch (IdentityProviderManagementException e) {
            throw new APIManagementException("IdP adding failed. " + e.getMessage(), e,
                    ExceptionCodes.IDP_ADDING_FAILED);
        }
        return null;
    }

    private void validateIdpTypeFromTokenType(KeyManagerConfigurationDTO keyManagerConfigurationDTO)
            throws APIManagementException {
        
        String tokenType = keyManagerConfigurationDTO.getTokenType();
        String keyManagerType = keyManagerConfigurationDTO.getType();
        if (StringUtils.equalsIgnoreCase(tokenType, KeyManagerConfiguration.TokenType.EXCHANGED.toString())) {
            Stream<KeyManagerConfiguration.IdpTypeOfExchangedTokens> streamIdpType = Stream
                    .of(KeyManagerConfiguration.IdpTypeOfExchangedTokens.values());
            boolean isAllowedIdP = streamIdpType
                    .anyMatch(idpType -> StringUtils.equalsIgnoreCase(idpType.toString(), keyManagerType));
            if (!isAllowedIdP) {
                String errMsg = "Identity Provider type: " + keyManagerType + " not allowed for the token type "
                        + KeyManagerConfiguration.TokenType.EXCHANGED + ". Should be a value from " + Arrays
                        .asList(KeyManagerConfiguration.IdpTypeOfExchangedTokens.values());
                throw new APIManagementException(errMsg, ExceptionCodes.from(ExceptionCodes.INVALID_IDP_TYPE, errMsg));
            }
        }
    }

    private IdentityProvider createIdp(KeyManagerConfigurationDTO keyManagerConfigurationDTO,
            KeyManagerDTO keyManagerDTO, String organization) {

        IdentityProvider identityProvider = new IdentityProvider();
        String idpName = sanitizeName(
                getSubstringOfTen(keyManagerConfigurationDTO.getName()) + "_" + organization + "_"
                        + keyManagerConfigurationDTO.getUuid());
        identityProvider.setIdentityProviderName(idpName);
        identityProvider.setDisplayName(keyManagerConfigurationDTO.getDisplayName());
        identityProvider.setPrimary(Boolean.FALSE);
        identityProvider.setIdentityProviderDescription(keyManagerConfigurationDTO.getDescription());
        identityProvider.setAlias(keyManagerConfigurationDTO.getAlias());
        KeyManagerCertificatesDTO keyManagerCertificatesDTO = keyManagerDTO.getCertificates();

        List<IdentityProviderProperty> idpProperties = new ArrayList<IdentityProviderProperty>();

        if (keyManagerCertificatesDTO != null) {
            if (keyManagerCertificatesDTO.getType().equals(KeyManagerCertificatesDTO.TypeEnum.JWKS)) {
                String idpJWKSUri = keyManagerCertificatesDTO.getValue();
                if (StringUtils.isNotBlank(idpJWKSUri)) {
                    IdentityProviderProperty jwksProperty = new IdentityProviderProperty();
                    jwksProperty.setName(RestApiConstants.JWKS_URI);
                    jwksProperty.setValue(idpJWKSUri);
                    idpProperties.add(jwksProperty);
                }
            } else if (keyManagerCertificatesDTO.getType().equals(KeyManagerCertificatesDTO.TypeEnum.PEM)) {
                identityProvider.setCertificate(StringUtils.join(keyManagerCertificatesDTO.getValue(), ""));
            }
        }

        if (StringUtils.isNotBlank(keyManagerDTO.getIssuer())) {
            IdentityProviderProperty identityProviderProperty = new IdentityProviderProperty();
            identityProviderProperty.setName(IdentityApplicationConstants.IDP_ISSUER_NAME);
            identityProviderProperty.setValue(keyManagerDTO.getIssuer());
            idpProperties.add(identityProviderProperty);
        }

        if (idpProperties.size() > 0) {
            identityProvider.setIdpProperties(idpProperties.toArray(new IdentityProviderProperty[0]));
        }

        identityProvider.setEnable(keyManagerConfigurationDTO.isEnabled());
        updateClaims(identityProvider, keyManagerDTO.getClaimMapping());
        return identityProvider;
    }

    private void updateClaims(IdentityProvider idp, List<ClaimMappingEntryDTO> claims) {
        if (claims != null) {
            ClaimConfig claimConfig = new ClaimConfig();
            List<ClaimMapping> claimMappings = new ArrayList<>();
            List<org.wso2.carbon.identity.application.common.model.Claim> idpClaims = new ArrayList<>();

            if (CollectionUtils.isNotEmpty(claims)) {
                claimConfig.setLocalClaimDialect(false);

                for (ClaimMappingEntryDTO claimMappingEntry : claims) {
                    String idpClaimUri = claimMappingEntry.getRemoteClaim();
                    String localClaimUri = claimMappingEntry.getLocalClaim();

                    ClaimMapping internalMapping = new ClaimMapping();
                    org.wso2.carbon.identity.application.common.model.Claim remoteClaim =
                            new org.wso2.carbon.identity.application.common.model.Claim();
                    remoteClaim.setClaimUri(idpClaimUri);

                    org.wso2.carbon.identity.application.common.model.Claim localClaim =
                            new org.wso2.carbon.identity.application.common.model.Claim();
                    localClaim.setClaimUri(localClaimUri);

                    internalMapping.setRemoteClaim(remoteClaim);
                    internalMapping.setLocalClaim(localClaim);
                    claimMappings.add(internalMapping);
                    idpClaims.add(remoteClaim);
                }
            } else {
                claimConfig.setLocalClaimDialect(true);
            }

            claimConfig.setClaimMappings(claimMappings.toArray(new ClaimMapping[0]));
            claimConfig.setIdpClaims(idpClaims.toArray(new org.wso2.carbon.identity.application.common.model.Claim[0]));
            idp.setClaimConfig(claimConfig);
        }
    }

    private void mergeIdpWithKeyManagerConfiguration(IdentityProvider identityProvider, KeyManagerDTO keyManagerDTO) {
        keyManagerDTO.setDisplayName(identityProvider.getDisplayName());
        keyManagerDTO.setDescription(identityProvider.getIdentityProviderDescription());

        IdentityProviderProperty identityProviderProperties[] = identityProvider.getIdpProperties();
        KeyManagerCertificatesDTO keyManagerCertificatesDTO = new KeyManagerCertificatesDTO();
        if (identityProviderProperties.length > 0) {
            for (IdentityProviderProperty identityProviderProperty :identityProviderProperties) {
                if (StringUtils.equals(identityProviderProperty.getName(), RestApiConstants.JWKS_URI)) {
                    keyManagerCertificatesDTO.setType(KeyManagerCertificatesDTO.TypeEnum.JWKS);
                    keyManagerCertificatesDTO.setValue(identityProviderProperty.getValue());
                    keyManagerDTO.setCertificates(keyManagerCertificatesDTO);
                }
                if (StringUtils
                        .equals(identityProviderProperty.getName(), IdentityApplicationConstants.IDP_ISSUER_NAME)) {
                    keyManagerDTO.setIssuer(identityProviderProperty.getValue());
                }
            }

        } else if (StringUtils.isNotBlank(identityProvider.getCertificate())) {
            keyManagerCertificatesDTO.setType(KeyManagerCertificatesDTO.TypeEnum.PEM);
            keyManagerCertificatesDTO.setValue(identityProvider.getCertificate());
            keyManagerDTO.setCertificates(keyManagerCertificatesDTO);
        }

        keyManagerDTO.setEnabled(identityProvider.isEnable());
        keyManagerDTO.setAlias(identityProvider.getAlias());

        ClaimConfig claimConfig = identityProvider.getClaimConfig();
        org.wso2.carbon.identity.application.common.model.Claim[] idpClaims = claimConfig.getIdpClaims();
        List<ClaimMappingEntryDTO> claimMappingEntryDTOList = new ArrayList<>();
        for (ClaimMapping claimMapping: claimConfig.getClaimMappings()) {
            ClaimMappingEntryDTO claimMappingEntryDTO = new ClaimMappingEntryDTO();
            claimMappingEntryDTO.setLocalClaim(claimMapping.getLocalClaim().getClaimUri());
            claimMappingEntryDTO.setRemoteClaim(claimMapping.getRemoteClaim().getClaimUri());
            claimMappingEntryDTOList.add(claimMappingEntryDTO);
        }
        keyManagerDTO.setClaimMapping(claimMappingEntryDTOList);
    }

    private String sanitizeName(String inputName) {
        return inputName.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
    }

    private String getSubstringOfTen(String inputString) {
        return inputString.length() < 10 ? inputString : inputString.substring(0, 10);
    }
}
