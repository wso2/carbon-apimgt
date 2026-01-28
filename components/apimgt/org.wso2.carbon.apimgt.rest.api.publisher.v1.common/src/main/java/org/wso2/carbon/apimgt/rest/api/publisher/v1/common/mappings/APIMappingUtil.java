/*
 *
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */
package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ErrorHandler;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.UsedByMigrationClient;
import org.wso2.carbon.apimgt.api.WorkflowStatus;
import org.wso2.carbon.apimgt.api.dto.APIEndpointValidationDTO;
import org.wso2.carbon.apimgt.api.model.AIConfiguration;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APICategory;
import org.wso2.carbon.apimgt.api.model.APIEndpointInfo;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIOperationMapping;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProductResource;
import org.wso2.carbon.apimgt.api.model.APIResourceMediationPolicy;
import org.wso2.carbon.apimgt.api.model.APIRevision;
import org.wso2.carbon.apimgt.api.model.APIRevisionDeployment;
import org.wso2.carbon.apimgt.api.model.APIStateChangeResponse;
import org.wso2.carbon.apimgt.api.model.Backend;
import org.wso2.carbon.apimgt.api.model.BackendOperationMapping;
import org.wso2.carbon.apimgt.api.model.BackendThrottlingConfiguration;
import org.wso2.carbon.apimgt.api.model.CORSConfiguration;
import org.wso2.carbon.apimgt.api.model.GatewayAgentConfiguration;
import org.wso2.carbon.apimgt.api.model.GatewayDeployer;
import org.wso2.carbon.apimgt.api.model.LifeCycleEvent;
import org.wso2.carbon.apimgt.api.model.Mediation;
import org.wso2.carbon.apimgt.api.model.OperationPolicy;
import org.wso2.carbon.apimgt.api.model.OrganizationTiers;
import org.wso2.carbon.apimgt.api.model.ResourcePath;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.SequenceBackendData;
import org.wso2.carbon.apimgt.api.model.ServiceEntry;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.WebsubSubscriptionConfiguration;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.ServiceCatalogImpl;
import org.wso2.carbon.apimgt.impl.lifecycle.CheckListItem;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.wsdl.model.WSDLInfo;
import org.wso2.carbon.apimgt.impl.wsdl.model.WSDLValidationResponse;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIBusinessInformationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APICorsConfigurationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO.AudienceEnum;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIEndpointDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIEndpointListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIInfoAdditionalPropertiesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIInfoAdditionalPropertiesMapDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIMaxTpsDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIMaxTpsTokenBasedThrottlingConfigurationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIMetadataDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIMetadataListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIMonetizationInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIOperationMappingDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIOperationPoliciesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIOperationsDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductBusinessInformationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionAPIInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionDeploymentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIScopeDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIServiceInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AdvertiseInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ApiEndpointValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AsyncAPISpecificationValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.AsyncAPISpecificationValidationResponseInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.BackendDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.BackendOperationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.BackendOperationMappingDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorListItemDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleHistoryDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleHistoryItemDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleStateAvailableTransitionsDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleStateCheckItemsDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleStateDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MCPServerDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MCPServerMetadataDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MCPServerMetadataListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MCPServerOperationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MCPServerScopeDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MaxTpsDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MediationPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MockResponsePayloadInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MockResponsePayloadListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OpenAPIDefinitionValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OpenAPIDefinitionValidationResponseInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OrganizationPoliciesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.PaginationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ProductAPIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ResourcePathDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ResourcePathListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ResourcePolicyInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ResourcePolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ScopeDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SequenceBackendDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SequenceBackendListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SubtypeConfigurationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.WSDLInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.WSDLValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.WSDLValidationResponseWsdlInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.WSDLValidationResponseWsdlInfoEndpointsDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.WebsubSubscriptionConfigurationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.WorkflowResponseDTO;
import org.wso2.carbon.apimgt.spec.parser.definitions.AbstractAsyncApiParser;
import org.wso2.carbon.apimgt.spec.parser.definitions.AsyncApiParserUtil;
import org.wso2.carbon.apimgt.spec.parser.definitions.OASParserUtil;
import org.wso2.carbon.apimgt.spec.parser.definitions.asyncapi.AsyncApiParseOptions;
import org.wso2.carbon.apimgt.spec.parser.definitions.asyncapi.AsyncApiParserFactory;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.getDefaultWebsubSubscriptionConfiguration;
import static org.wso2.carbon.apimgt.impl.utils.APIUtil.handleException;
import static org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.PublisherCommonUtils.buildThrottlingConfiguration;
import static org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.PublisherCommonUtils.buildThrottlingConfigurationDTO;

/**
 * This class used for mapping utility to API.
 */
public class APIMappingUtil {

    private static final Log log = LogFactory.getLog(APIMappingUtil.class);
    private static final String FORWARD_SLASH = "/";
    private static final String EMPTY_STRING = "";

    private static String migrationEnabled = System.getProperty(APIConstants.MIGRATE);

    public static API fromDTOtoAPI(APIDTO dto, String provider) throws APIManagementException {

        String providerEmailDomainReplaced = APIUtil.replaceEmailDomain(provider);

        // The provider name that is coming from the body is not honored for now.
        // Later we can use it by checking admin privileges of the user.
        APIIdentifier apiId = new APIIdentifier(providerEmailDomainReplaced, dto.getName(), dto.getVersion());
        API model = new API(apiId);

        String context = dto.getContext();
        final String originalContext = context;

        if (context.endsWith(FORWARD_SLASH + RestApiConstants.API_VERSION_PARAM)) {
            context = context.replace(FORWARD_SLASH + RestApiConstants.API_VERSION_PARAM, EMPTY_STRING);
        }

        context = context.startsWith(FORWARD_SLASH) ? context : (FORWARD_SLASH + context);
        String providerDomain = MultitenantUtils.getTenantDomain(provider);
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(providerDomain) && dto.getId() == null
                && !context.contains("/t/" + providerDomain)) {
            //Create tenant aware context for API
            context = "/t/" + providerDomain + context;
        }

        // This is to support the pluggable version strategy
        // if the context does not contain any {version} segment, we use the default version strategy.
        context = checkAndSetVersionParam(context);
        model.setContextTemplate(context);

        context = updateContextWithVersion(dto.getVersion(), originalContext, context);
        model.setContext(context);
        model.setDescription(dto.getDescription());
        model.setDisplayName((dto.getDisplayName() != null && !dto.getDisplayName().trim().isEmpty())
                ? dto.getDisplayName() : dto.getName());

        Object endpointConfig = dto.getEndpointConfig();
        if (endpointConfig != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                if (endpointConfig instanceof LinkedHashMap) {
                    ((LinkedHashMap) endpointConfig).remove(APIConstants.IMPLEMENTATION_STATUS);
                }
                model.setEndpointConfig(mapper.writeValueAsString(endpointConfig));
            } catch (IOException e) {
                handleException("Error while converting endpointConfig to json", e);
            }
        }

        model.setImplementation(dto.getEndpointImplementationType().toString());
        model.setType(dto.getType().toString());
        if (dto.getLifeCycleStatus() != null) {
            model.setStatus((dto.getLifeCycleStatus() != null) ? dto.getLifeCycleStatus().toUpperCase() : null);
        }
        if (dto.isIsDefaultVersion() != null) {
            model.setAsDefaultVersion(dto.isIsDefaultVersion());
        }
        if (dto.isEnableSchemaValidation() != null) {
            model.setEnableSchemaValidation(dto.isEnableSchemaValidation());
        }
        if (dto.isEnableSubscriberVerification() != null) {
            model.setEnableSubscriberVerification(dto.isEnableSubscriberVerification());
        }
        model.setEnableStore(true);
        if (dto.getAdvertiseInfo() != null) {
            AdvertiseInfoDTO advertiseInfoDTO = dto.getAdvertiseInfo();
            model.setAdvertiseOnly(advertiseInfoDTO.isAdvertised());
            model.setApiExternalProductionEndpoint(advertiseInfoDTO.getApiExternalProductionEndpoint());
            model.setApiExternalSandboxEndpoint(advertiseInfoDTO.getApiExternalSandboxEndpoint());
            model.setRedirectURL(advertiseInfoDTO.getOriginalDevPortalUrl());
            model.setApiOwner(advertiseInfoDTO.getApiOwner());
            model.setAdvertiseOnlyAPIVendor(dto.getAdvertiseInfo().getVendor().value());
        }
        if (dto.isResponseCachingEnabled() != null && dto.isResponseCachingEnabled()) {
            model.setResponseCache(APIConstants.ENABLED);
        } else {
            model.setResponseCache(APIConstants.DISABLED);
        }
        if (dto.getCacheTimeout() != null) {
            model.setCacheTimeout(dto.getCacheTimeout());
        } else {
            model.setCacheTimeout(APIConstants.API_RESPONSE_CACHE_TIMEOUT);
        }

        if (dto.getMediationPolicies() != null) {
            List<MediationPolicyDTO> policies = dto.getMediationPolicies();

            //validate whether provided sequences are available
            for (MediationPolicyDTO policy : policies) {
                if (APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN.equalsIgnoreCase(policy.getType())) {
                    model.setInSequence(policy.getName());
                } else if (APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT.equalsIgnoreCase(policy.getType())) {
                    model.setOutSequence(policy.getName());
                } else {
                    model.setFaultSequence(policy.getName());
                }
            }
        }

        if (dto.getSubscriptionAvailability() != null) {
            model.setSubscriptionAvailability(
                    mapSubscriptionAvailabilityFromDTOtoAPI(dto.getSubscriptionAvailability()));
        }

        if (dto.getSubscriptionAvailableTenants() != null) {
            model.setSubscriptionAvailableTenants(StringUtils.join(dto.getSubscriptionAvailableTenants(), ","));
        }

        Set<Scope> scopes = getScopes(dto);
        model.setScopes(scopes);

        if (dto.getGatewayType() != null) {
            model.setGatewayType(dto.getGatewayType());
        }
        Boolean initiatedFromGateway = dto.isInitiatedFromGateway();
        if (initiatedFromGateway != null) {
            model.setInitiatedFromGateway(initiatedFromGateway);
        }

        //URI Templates
        // No default topics for AsyncAPIs. Therefore set URITemplates only for non-AsyncAPIs.
        Set<URITemplate> uriTemplates = getURITemplates(model, dto.getOperations());
        model.setUriTemplates(uriTemplates);

        if (dto.getApiPolicies() != null) {
            List<OperationPolicy> policyList = OperationPolicyMappingUtil.fromDTOToAPIOperationPoliciesList(
                    dto.getApiPolicies());
            model.setApiPolicies(policyList);
        }

        // wsUriMapping
        if (dto.getType().toString().equals(APIConstants.API_TYPE_WS)) {
            Map<String, String> wsUriMapping = new HashMap<>();
            for (APIOperationsDTO operationsDTO : dto.getOperations()) {
                wsUriMapping
                        .put(operationsDTO.getVerb() + "_" + operationsDTO.getTarget(), operationsDTO.getUriMapping());
            }
            model.setWsUriMapping(wsUriMapping);
        }

        if (dto.getTags() != null) {
            Set<String> apiTags = new HashSet<>(dto.getTags());
            model.addTags(apiTags);
        }

        // Set tiers without considering organizations
        Set<Tier> apiTiers = new HashSet<>();
        List<String> tiersFromDTO = dto.getPolicies();
        for (String tier : tiersFromDTO) {
            apiTiers.add(new Tier(tier));
        }
        model.addAvailableTiers(apiTiers);

        if (APIUtil.isOrganizationAccessControlEnabled()) {
            // Set tiers for organizations
            Set<OrganizationTiers> organizationAPITiers = new HashSet<>();
            List<OrganizationPoliciesDTO> organizationPoliciesDTOs = dto.getOrganizationPolicies();

            for (OrganizationPoliciesDTO organizationPoliciesDTO : organizationPoliciesDTOs) {
                OrganizationTiers organizationTiers = new OrganizationTiers();
                Set<Tier> apiTiersForOrganization = new HashSet<>();
                for (String tier : organizationPoliciesDTO.getPolicies()) {
                    apiTiersForOrganization.add(new Tier(tier));
                }
                organizationTiers.setOrganizationID(organizationPoliciesDTO.getOrganizationID());
                organizationTiers.setTiers(apiTiersForOrganization);
                organizationAPITiers.add(organizationTiers);
            }
            model.setAvailableTiersForOrganizations(organizationAPITiers);
        }


        model.setApiLevelPolicy(dto.getApiThrottlingPolicy());

        String transports = StringUtils.join(dto.getTransport(), ',');
        model.setTransports(transports);
        if (dto.getVisibility() != null) {
            model.setVisibility(mapVisibilityFromDTOtoAPI(dto.getVisibility()));
        }
        if (dto.getVisibleRoles() != null) {
            String visibleRoles = StringUtils.join(dto.getVisibleRoles(), ',');
            model.setVisibleRoles(visibleRoles);
        }

        if (dto.getVisibleTenants() != null) {
            if (APIUtil.isCrossTenantSubscriptionsEnabled()) {
                String visibleTenants = StringUtils.join(dto.getVisibleTenants(), ',');
                model.setVisibleTenants(visibleTenants);
            }
        }

        if (dto.getVisibleOrganizations() != null && !dto.getVisibleOrganizations().isEmpty()) {
            String visibleOrgs = StringUtils.join(dto.getVisibleOrganizations(), ',');
            model.setVisibleOrganizations(visibleOrgs);
        } else {
            model.setVisibleOrganizations(APIConstants.VISIBLE_ORG_NONE);
        }

        List<String> accessControlRoles = dto.getAccessControlRoles();
        if (accessControlRoles == null || accessControlRoles.isEmpty()) {
            model.setAccessControl(APIConstants.NO_ACCESS_CONTROL);
            model.setAccessControlRoles("null");
        } else {
            if (log.isDebugEnabled()) {
                log.debug("Setting access control roles for API: " + apiId);
            }
            model.setAccessControlRoles(StringUtils.join(accessControlRoles, ',').toLowerCase());
            model.setAccessControl(APIConstants.API_RESTRICTED_VISIBILITY);
        }

        List<APIInfoAdditionalPropertiesDTO> additionalProperties = dto.getAdditionalProperties();
        if (additionalProperties != null) {
            for (APIInfoAdditionalPropertiesDTO property : additionalProperties) {
                if (property.isDisplay()) {
                    model.addProperty(property.getName() + APIConstants.API_RELATED_CUSTOM_PROPERTIES_SURFIX, property
                            .getValue());
                } else {
                    model.addProperty(property.getName(), property.getValue());
                }
            }
        }

        Map<String, APIInfoAdditionalPropertiesMapDTO> additionalPropertiesMap = dto.getAdditionalPropertiesMap();
        if (additionalPropertiesMap != null && !additionalPropertiesMap.isEmpty()) {
            for (Map.Entry<String, APIInfoAdditionalPropertiesMapDTO> entry : additionalPropertiesMap.entrySet()) {
                String propertyKey = null;
                if (entry.getValue().isDisplay()) {
                    propertyKey = entry.getKey() + APIConstants.API_RELATED_CUSTOM_PROPERTIES_SURFIX;
                } else {
                    propertyKey = entry.getKey();
                }
                // If this property already added from the additional properties, avoid overriding it
                if (propertyKey != null && !model.getAdditionalProperties().containsKey(propertyKey)) {
                    model.addProperty(propertyKey, entry.getValue().getValue());
                }
            }
        }

        ObjectMapper objectMapper = new ObjectMapper();
        APIBusinessInformationDTO apiBusinessInformationDTO = objectMapper.convertValue(dto.getBusinessInformation(),
                APIBusinessInformationDTO.class);
        if (apiBusinessInformationDTO != null) {
            model.setBusinessOwner(apiBusinessInformationDTO.getBusinessOwner());
            model.setBusinessOwnerEmail(apiBusinessInformationDTO.getBusinessOwnerEmail());
            model.setTechnicalOwner(apiBusinessInformationDTO.getTechnicalOwner());
            model.setTechnicalOwnerEmail(apiBusinessInformationDTO.getTechnicalOwnerEmail());
        }
        APICorsConfigurationDTO apiCorsConfigurationDTO = dto.getCorsConfiguration();
        CORSConfiguration corsConfiguration;
        if (apiCorsConfigurationDTO != null) {
            corsConfiguration =
                    new CORSConfiguration(apiCorsConfigurationDTO.isCorsConfigurationEnabled(),
                            apiCorsConfigurationDTO.getAccessControlAllowOrigins(),
                            apiCorsConfigurationDTO.isAccessControlAllowCredentials(),
                            apiCorsConfigurationDTO.getAccessControlAllowHeaders(),
                            apiCorsConfigurationDTO.getAccessControlAllowMethods());

        } else {
            corsConfiguration = APIUtil.getDefaultCorsConfiguration();
        }
        model.setCorsConfiguration(corsConfiguration);
        setMaxTpsFromApiDTOToModel(dto, model);
        model.setAuthorizationHeader(dto.getAuthorizationHeader());
        model.setApiKeyHeader(dto.getApiKeyHeader());
        if (model.getApiKeyHeader() == null) {
            model.setApiKeyHeader(APIConstants.API_KEY_HEADER_DEFAULT);
        }
        model.setApiSecurity(getSecurityScheme(dto.getSecurityScheme()));

        if (dto.getType().toString().equals(APIConstants.API_TYPE_WEBSUB)) {
            WebsubSubscriptionConfigurationDTO websubSubscriptionConfigurationDTO
                    = dto.getWebsubSubscriptionConfiguration();
            WebsubSubscriptionConfiguration websubSubscriptionConfiguration;
            if (websubSubscriptionConfigurationDTO != null) {
                websubSubscriptionConfiguration = new WebsubSubscriptionConfiguration(
                        websubSubscriptionConfigurationDTO.isEnable(),
                        websubSubscriptionConfigurationDTO.getSecret(),
                        websubSubscriptionConfigurationDTO.getSigningAlgorithm(),
                        websubSubscriptionConfigurationDTO.getSignatureHeader());
            } else {
                websubSubscriptionConfiguration = getDefaultWebsubSubscriptionConfiguration();
            }
            model.setWebsubSubscriptionConfiguration(websubSubscriptionConfiguration);
        }

        //attach api categories to API model
        setAPICategoriesToModel(dto, model, provider);
        if (dto.getKeyManagers() instanceof List) {
            model.setKeyManagers((List<String>) dto.getKeyManagers());
        } else if (dto.getKeyManagers() == null) {
            model.setKeyManagers(Collections.singletonList(APIConstants.KeyManager.API_LEVEL_ALL_KEY_MANAGERS));
        } else {
            String errMsg = "KeyManagers value needs to be an array";
            ExceptionCodes errorHandler = ExceptionCodes.KEYMANAGERS_VALUE_NOT_ARRAY;
            throw new APIManagementException(errMsg, errorHandler);
        }

        APIServiceInfoDTO serviceInfoDTO = dto.getServiceInfo();
        if (serviceInfoDTO != null) {
            ObjectMapper mapper = new ObjectMapper();
            JSONParser parser = new JSONParser();
            JSONObject serviceInfoJson;
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            try {
                int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().
                        getTenantId(tenantDomain);
                serviceInfoJson = (JSONObject) parser.parse(mapper.writeValueAsString(serviceInfoDTO));

                ServiceCatalogImpl serviceCatalog = new ServiceCatalogImpl();
                ServiceEntry service = serviceCatalog.getServiceByKey(dto.getServiceInfo().getKey(), tenantId);
                // Set the md5 of the service which is already available in the system to the API model
                if (service == null) {
                    if (log.isDebugEnabled()) {
                        log.debug("A service with key" + dto.getServiceInfo().getKey() + " referenced in the API "
                                + "information is not available in the service catalog");
                    }
                } else {
                    serviceInfoJson.put("md5", service.getMd5());
                }
                model.setServiceInfo(serviceInfoJson);
            } catch (JsonProcessingException | ParseException e) {
                String msg = "Error while getting json representation of APIServiceInfo";
                handleException(msg, e);
            } catch (UserStoreException e) {
                String msg = "Error while getting tenantId from the given tenant domain " + tenantDomain;
                handleException(msg, e);
            }
        }
        if (dto.getAudience() != null) {
            model.setAudience(dto.getAudience().toString());
        }
        if (dto.getAudiences() != null) {
            List<String> audiences = dto.getAudiences();
            if (audiences.isEmpty()) {
                audiences.add(APIConstants.ALL_AUDIENCES);
            }
            model.setAudiences(new HashSet<>(audiences));
        }
        if (dto.getGatewayVendor() != null) {
            model.setGatewayVendor(dto.getGatewayVendor());
        }

        if (dto.getWsdlUrl() != null) {
            model.setWsdlUrl(dto.getWsdlUrl());
        }

        if (dto.getAsyncTransportProtocols() != null) {
            String asyncTransports = StringUtils.join(dto.getAsyncTransportProtocols(), ',');
            model.setAsyncTransportProtocols(asyncTransports);
        }
        if (dto.getSubtypeConfiguration() != null && dto.getSubtypeConfiguration().getSubtype() != null) {
            if (APIConstants.API_SUBTYPE_AI_API.equals(dto.getSubtypeConfiguration().getSubtype())
                    && dto.getSubtypeConfiguration().getConfiguration() != null) {
                model.setSubtype(APIConstants.API_SUBTYPE_AI_API);
                model.setAiConfiguration(
                        new Gson().fromJson(dto.getSubtypeConfiguration().getConfiguration().toString(),
                                AIConfiguration.class));
            } else if (APIConstants.API_TYPE_MCP.equals(dto.getType().value())) {
                model.setSubtype(dto.getSubtypeConfiguration().getSubtype());
            } else {
                model.setSubtype(APIConstants.API_SUBTYPE_DEFAULT);
            }
        } else {
                model.setSubtype(APIConstants.API_SUBTYPE_DEFAULT);
        }

        // Set primary endpoint mapping
        model.setPrimaryProductionEndpointId(dto.getPrimaryProductionEndpointId());
        model.setPrimarySandboxEndpointId(dto.getPrimarySandboxEndpointId());

        try {
            GatewayAgentConfiguration gatewayConfiguration =
                    org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder.getInstance()
                                    .getExternalGatewayConnectorConfiguration(model.getGatewayType());
            if (gatewayConfiguration != null &&
                    StringUtils.isNotEmpty(gatewayConfiguration.getGatewayDeployerImplementation())) {
                GatewayDeployer deployer =
                        (GatewayDeployer) Class.forName(gatewayConfiguration.getGatewayDeployerImplementation())
                                        .getDeclaredConstructor().newInstance();
                deployer.transformAPI(model);
            }
        } catch (NoSuchMethodException | ClassNotFoundException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            String msg = "Error while fetching gateway deployer instance";
            handleException(msg, e);
        }
        return model;
    }

    public static API fromMCPServerDTOtoAPI(MCPServerDTO dto, String provider) throws APIManagementException {

        String providerEmailDomainReplaced = APIUtil.replaceEmailDomain(provider);
        APIIdentifier apiId = new APIIdentifier(providerEmailDomainReplaced, dto.getName(), dto.getVersion());
        API model = new API(apiId);
        model.setUuid(dto.getId());

        String context = dto.getContext();
        final String originalContext = context;

        if (context.endsWith(FORWARD_SLASH + RestApiConstants.API_VERSION_PARAM)) {
            context = context.replace(FORWARD_SLASH + RestApiConstants.API_VERSION_PARAM, EMPTY_STRING);
        }

        context = context.startsWith(FORWARD_SLASH) ? context : (FORWARD_SLASH + context);
        String providerDomain = MultitenantUtils.getTenantDomain(provider);
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(providerDomain) && dto.getId() == null
                && !context.contains("/t/" + providerDomain)) {
            context = "/t/" + providerDomain + context;
        }

        context = checkAndSetVersionParam(context);
        model.setContextTemplate(context);

        context = updateContextWithVersion(dto.getVersion(), originalContext, context);
        model.setContext(context);
        model.setDescription(dto.getDescription());

        Object endpointConfig = dto.getEndpointConfig();
        if (endpointConfig != null) {
            ObjectMapper mapper = new ObjectMapper();
            try {
                if (endpointConfig instanceof LinkedHashMap) {
                    ((LinkedHashMap) endpointConfig).remove(APIConstants.IMPLEMENTATION_STATUS);
                }
                model.setEndpointConfig(mapper.writeValueAsString(endpointConfig));
            } catch (IOException e) {
                handleException("Error while converting endpointConfig to json", e);
            }
        }

        model.setType(APIConstants.API_TYPE_MCP);
        if (dto.getLifeCycleStatus() != null) {
            model.setStatus((dto.getLifeCycleStatus() != null) ? dto.getLifeCycleStatus().toUpperCase() : null);
        }
        if (dto.isIsDefaultVersion() != null) {
            model.setAsDefaultVersion(dto.isIsDefaultVersion());
        }
        if (dto.isEnableSchemaValidation() != null) {
            model.setEnableSchemaValidation(dto.isEnableSchemaValidation());
        }
        model.setEnableStore(true);
        model.setResponseCache(APIConstants.DISABLED);

        if (dto.getSubscriptionAvailability() != null) {
            model.setSubscriptionAvailability(
                    mapSubscriptionAvailabilityFromDTOtoAPI(dto.getSubscriptionAvailability()));
        }

        if (dto.getSubscriptionAvailableTenants() != null) {
            model.setSubscriptionAvailableTenants(StringUtils.join(dto.getSubscriptionAvailableTenants(), ","));
        }

        Set<Scope> scopes = getScopes(dto);
        model.setScopes(scopes);

        if (dto.getGatewayType() != null) {
            model.setGatewayType(dto.getGatewayType());
        }

        Set<URITemplate> uriTemplates = getURITemplatesForMCPServers(model, dto.getOperations());
        model.setUriTemplates(uriTemplates);

        if (dto.getMcpServerPolicies() != null) {
            List<OperationPolicy> policyList =
                    OperationPolicyMappingUtil.fromDTOToMCPServerOperationPoliciesList(dto.getMcpServerPolicies());
            model.setApiPolicies(policyList);
        }

        if (dto.getTags() != null) {
            Set<String> apiTags = new HashSet<>(dto.getTags());
            model.addTags(apiTags);
        }

        // Set tiers without considering organizations
        Set<Tier> apiTiers = new HashSet<>();
        List<String> tiersFromDTO = dto.getPolicies();
        for (String tier : tiersFromDTO) {
            apiTiers.add(new Tier(tier));
        }
        model.addAvailableTiers(apiTiers);

        if (APIUtil.isOrganizationAccessControlEnabled()) {
            // Set tiers for organizations
            Set<OrganizationTiers> organizationAPITiers = new HashSet<>();
            List<OrganizationPoliciesDTO> organizationPoliciesDTOs = dto.getOrganizationPolicies();

            for (OrganizationPoliciesDTO organizationPoliciesDTO : organizationPoliciesDTOs) {
                OrganizationTiers organizationTiers = new OrganizationTiers();
                Set<Tier> apiTiersForOrganization = new HashSet<>();
                for (String tier : organizationPoliciesDTO.getPolicies()) {
                    apiTiersForOrganization.add(new Tier(tier));
                }
                organizationTiers.setOrganizationID(organizationPoliciesDTO.getOrganizationID());
                organizationTiers.setTiers(apiTiersForOrganization);
                organizationAPITiers.add(organizationTiers);
            }
            model.setAvailableTiersForOrganizations(organizationAPITiers);
        }

        model.setApiLevelPolicy(dto.getThrottlingPolicy());

        String transports = StringUtils.join(dto.getTransport(), ',');
        model.setTransports(transports);
        if (dto.getVisibility() != null) {
            model.setVisibility(mapVisibilityFromDTOtoAPI(dto.getVisibility()));
        }
        if (dto.getVisibleRoles() != null) {
            String visibleRoles = StringUtils.join(dto.getVisibleRoles(), ',');
            model.setVisibleRoles(visibleRoles);
        }

        if (dto.getVisibleTenants() != null) {
            if (APIUtil.isCrossTenantSubscriptionsEnabled()) {
                String visibleTenants = StringUtils.join(dto.getVisibleTenants(), ',');
                model.setVisibleTenants(visibleTenants);
            }
        }

        if (dto.getVisibleOrganizations() != null && !dto.getVisibleOrganizations().isEmpty()) {
            String visibleOrgs = StringUtils.join(dto.getVisibleOrganizations(), ',');
            model.setVisibleOrganizations(visibleOrgs);
        } else {
            model.setVisibleOrganizations(APIConstants.VISIBLE_ORG_NONE);
        }

        List<String> accessControlRoles = dto.getAccessControlRoles();
        if (accessControlRoles == null || accessControlRoles.isEmpty()) {
            model.setAccessControl(APIConstants.NO_ACCESS_CONTROL);
            model.setAccessControlRoles("null");
        } else {
            model.setAccessControlRoles(StringUtils.join(accessControlRoles, ',').toLowerCase());
            model.setAccessControl(APIConstants.API_RESTRICTED_VISIBILITY);
        }

        Map<String, APIInfoAdditionalPropertiesMapDTO> additionalPropertiesMap = dto.getAdditionalPropertiesMap();
        if (additionalPropertiesMap != null && !additionalPropertiesMap.isEmpty()) {
            for (Map.Entry<String, APIInfoAdditionalPropertiesMapDTO> entry : additionalPropertiesMap.entrySet()) {
                String propertyKey = null;
                if (entry.getValue().isDisplay()) {
                    propertyKey = entry.getKey() + APIConstants.API_RELATED_CUSTOM_PROPERTIES_SURFIX;
                } else {
                    propertyKey = entry.getKey();
                }
                // If this property already added from the additional properties, avoid overriding it
                if (propertyKey != null && !model.getAdditionalProperties().containsKey(propertyKey)) {
                    model.addProperty(propertyKey, entry.getValue().getValue());
                }
            }
        }

        ObjectMapper objectMapper = new ObjectMapper();
        APIBusinessInformationDTO apiBusinessInformationDTO = objectMapper.convertValue(dto.getBusinessInformation(),
                APIBusinessInformationDTO.class);
        if (apiBusinessInformationDTO != null) {
            model.setBusinessOwner(apiBusinessInformationDTO.getBusinessOwner());
            model.setBusinessOwnerEmail(apiBusinessInformationDTO.getBusinessOwnerEmail());
            model.setTechnicalOwner(apiBusinessInformationDTO.getTechnicalOwner());
            model.setTechnicalOwnerEmail(apiBusinessInformationDTO.getTechnicalOwnerEmail());
        }
        APICorsConfigurationDTO apiCorsConfigurationDTO = dto.getCorsConfiguration();
        CORSConfiguration corsConfiguration;
        if (apiCorsConfigurationDTO != null) {
            corsConfiguration =
                    new CORSConfiguration(apiCorsConfigurationDTO.isCorsConfigurationEnabled(),
                            apiCorsConfigurationDTO.getAccessControlAllowOrigins(),
                            apiCorsConfigurationDTO.isAccessControlAllowCredentials(),
                            apiCorsConfigurationDTO.getAccessControlAllowHeaders(),
                            apiCorsConfigurationDTO.getAccessControlAllowMethods());

        } else {
            corsConfiguration = APIUtil.getDefaultCorsConfiguration();
        }

        // Set Mandatory CORS Headers for MCP servers if they are not already present
        List<String> existingAllowHeaders = corsConfiguration.getAccessControlAllowHeaders();
        List<String> updatedAllowHeaders = new ArrayList<>(
                existingAllowHeaders != null ? existingAllowHeaders : Collections.emptyList());
        if (updatedAllowHeaders.stream().noneMatch(APIConstants.MCP.MCP_PROTOCOL_VERSION_HEADER::equalsIgnoreCase)) {
            updatedAllowHeaders.add(APIConstants.MCP.MCP_PROTOCOL_VERSION_HEADER);
        }

        if (updatedAllowHeaders.stream().noneMatch(APIConstants.MCP.HEADER_MCP_SESSION_ID::equalsIgnoreCase)) {
            updatedAllowHeaders.add(APIConstants.MCP.HEADER_MCP_SESSION_ID);
        }
        corsConfiguration.setAccessControlAllowHeaders(updatedAllowHeaders);

        //temporarily set the CORS enabled to true for MCP servers
        corsConfiguration.setCorsConfigurationEnabled(true);

        model.setCorsConfiguration(corsConfiguration);
        setMaxTpsFromMcpServerDTOToModel(dto, model);
        model.setAuthorizationHeader(dto.getAuthorizationHeader());
        model.setApiKeyHeader(dto.getApiKeyHeader());
        if (model.getApiKeyHeader() == null) {
            model.setApiKeyHeader(APIConstants.API_KEY_HEADER_DEFAULT);
        }
        model.setApiSecurity(getSecurityScheme(dto.getSecurityScheme()));

        //attach api categories to API model
        setAPICategoriesToModel(dto, model, provider);
        if (dto.getKeyManagers() instanceof List) {
            model.setKeyManagers((List<String>) dto.getKeyManagers());
        } else if (dto.getKeyManagers() == null) {
            model.setKeyManagers(Collections.singletonList(APIConstants.KeyManager.API_LEVEL_ALL_KEY_MANAGERS));
        } else {
            String errMsg = "KeyManagers value needs to be an array";
            ExceptionCodes errorHandler = ExceptionCodes.KEYMANAGERS_VALUE_NOT_ARRAY;
            throw new APIManagementException(errMsg, errorHandler);
        }
        if (dto.getAudiences() != null) {
            List<String> audiences = dto.getAudiences();
            if (audiences.isEmpty()) {
                audiences.add(APIConstants.ALL_AUDIENCES);
            }
            model.setAudiences(new HashSet<>(audiences));
        }
        if (dto.getGatewayVendor() != null) {
            model.setGatewayVendor(dto.getGatewayVendor());
        }
        if (dto.getSubtypeConfiguration() != null && dto.getSubtypeConfiguration().getSubtype() != null) {
            model.setSubtype(dto.getSubtypeConfiguration().getSubtype());
        }
        String protocolVersion = dto.getProtocolVersion();
        if (protocolVersion != null && !protocolVersion.isEmpty()) {
            model.getMetadata().put(APIConstants.MCP.PROTOCOL_VERSION_KEY, protocolVersion);
        }
        String displayName = dto.getDisplayName();
        if (displayName != null && !displayName.trim().isEmpty()) {
            model.setDisplayName(displayName);
        } else {
            model.setDisplayName(dto.getName());
        }

        try {
            GatewayAgentConfiguration gatewayConfiguration =
                    org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder.getInstance().
                            getExternalGatewayConnectorConfiguration(model.getGatewayType());
            if (gatewayConfiguration != null) {
                GatewayDeployer deployer = (GatewayDeployer) Class.forName(gatewayConfiguration
                                .getGatewayDeployerImplementation()).getDeclaredConstructor().newInstance();
                if (deployer != null) {
                    deployer.transformAPI(model);
                }
            }
        } catch (NoSuchMethodException | ClassNotFoundException | InstantiationException | IllegalAccessException |
                 InvocationTargetException e) {
            String msg = "Error while fetching gateway deployer instance";
            handleException(msg, e);
        }

        return model;
    }

    /**
     * This method creates the list of MockPayloadDTO objects for resetting inline prototyping scripts.
     *
     * @param mockPayloads APIResourceMediationPolicy List
     * @return mockResponsePayloadListDTO (list of MockPayloadDTO)
     * @throws APIManagementException
     */
    public static MockResponsePayloadListDTO fromMockPayloadsToListDTO(List<APIResourceMediationPolicy> mockPayloads)
            throws APIManagementException {

        MockResponsePayloadListDTO mockResponsePayloadListDTO = new MockResponsePayloadListDTO();
        List<MockResponsePayloadInfoDTO> mockResponsePayloadInfoDTOS = mockResponsePayloadListDTO.getList();
        for (APIResourceMediationPolicy apiResourceMediationPolicy : mockPayloads) {
            mockResponsePayloadInfoDTOS.add(fromMockPayloadToDTO(apiResourceMediationPolicy));
        }
        return mockResponsePayloadListDTO;
    }

    /**
     * This method creates object of MockPayloadDTO.
     *
     * @param model APIResourceMediationPolicy object
     * @return mockResponsePayloadInfoDTO object
     */
    public static MockResponsePayloadInfoDTO fromMockPayloadToDTO(APIResourceMediationPolicy model) {

        MockResponsePayloadInfoDTO mockResponsePayloadInfoDTO = new MockResponsePayloadInfoDTO();
        mockResponsePayloadInfoDTO.setPath(model.getPath());
        mockResponsePayloadInfoDTO.setVerb(model.getVerb());
        mockResponsePayloadInfoDTO.setContent(model.getContent());
        return mockResponsePayloadInfoDTO;
    }

    public static SequenceBackendListDTO fromSequenceDataToDTO(List<SequenceBackendData> list) {
        SequenceBackendListDTO res = new SequenceBackendListDTO();
        res.setCount(list.size());
        List<SequenceBackendDTO> backends = new ArrayList<>();

        for (SequenceBackendData backend : list) {
            SequenceBackendDTO dto = new SequenceBackendDTO();
            dto.sequenceId(backend.getId());
            dto.setSequenceName(backend.getName());
            dto.setSequenceType(backend.getType());
            backends.add(dto);
        }
        res.setList(backends);
        return res;
    }

    /**
     * This method creates the API monetization information DTO.
     *
     * @param apiId API apiid
     * @param organization identifier of the organization
     * @return monetization information DTO
     * @throws APIManagementException if failed to construct the DTO
     */
    public static APIMonetizationInfoDTO getMonetizationInfoDTO(String apiId, String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        API api = apiProvider.getLightweightAPIByUUID(apiId, organization);
        APIMonetizationInfoDTO apiMonetizationInfoDTO = new APIMonetizationInfoDTO();
        //set the information relatated to monetization to the DTO
        apiMonetizationInfoDTO.setEnabled(api.getMonetizationStatus());
        Map<String, String> monetizationPropertiesMap = new HashMap<>();

        if (api.getMonetizationProperties() != null) {
            JSONObject monetizationProperties = api.getMonetizationProperties();
            for (Object propertyKey : monetizationProperties.keySet()) {
                String key = (String) propertyKey;
                monetizationPropertiesMap.put(key, (String) monetizationProperties.get(key));
            }
        }
        apiMonetizationInfoDTO.setProperties(monetizationPropertiesMap);
        return apiMonetizationInfoDTO;
    }

    /**
     * Get map of monetized policies to plan mapping.
     *
     * @param uuid apiuuid
     * @param organization organization
     * @param monetizedPoliciesToPlanMapping map of monetized policies to plan mapping
     * @return DTO of map of monetized policies to plan mapping
     * @throws APIManagementException if failed to construct the DTO
     */
    public static APIMonetizationInfoDTO getMonetizedTiersDTO(String uuid, String organization,
                                                              Map<String, String> monetizedPoliciesToPlanMapping)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        API api = apiProvider.getLightweightAPIByUUID(uuid, organization);
        APIMonetizationInfoDTO apiMonetizationInfoDTO = new APIMonetizationInfoDTO();
        apiMonetizationInfoDTO.setEnabled(api.getMonetizationStatus());
        apiMonetizationInfoDTO.setProperties(monetizedPoliciesToPlanMapping);
        return apiMonetizationInfoDTO;
    }

    /**
     * Returns the APIIdentifier given the uuid.
     *
     * @param apiId API uuid
     * @return APIIdentifier which represents the given id
     * @throws APIManagementException
     */
    public static APIIdentifier getAPIIdentifierFromUUID(String apiId)
            throws APIManagementException {

        return APIUtil.getAPIIdentifierFromUUID(apiId);
    }

    /**
     * Returns an API with minimal info given the uuid.
     *
     * @param apiUUID               API uuid
     * @param organization organization of the API
     * @return API which represents the given id
     * @throws APIManagementException
     */
    public static API getAPIInfoFromUUID(String apiUUID, String organization)
            throws APIManagementException {

        API api;
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        api = apiProvider.getLightweightAPIByUUID(apiUUID, organization);
        return api;
    }

    /**
     * Converts a List object of APIs into a DTO.
     *
     * @param apiList List of APIs
     * @return APIListDTO object containing APIDTOs
     */
    public static Object fromAPIListToDTO(List<API> apiList) throws APIManagementException {
        return fromAPIListToInfoDTO(apiList);
    }

    /**
     * Converts a List object of APIs into Info DTO List.
     *
     * @param apiList List of APIs
     * @return APIListDTO object containing APIDTOs
     */
    public static APIListDTO fromAPIListToInfoDTO(List<API> apiList) throws APIManagementException {

        APIListDTO apiListDTO = new APIListDTO();
        List<APIInfoDTO> apiInfoDTOs = apiListDTO.getList();
        for (API api : apiList) {
            apiInfoDTOs.add(fromAPIToInfoDTO(api));
        }
        apiListDTO.setCount(apiInfoDTOs.size());
        return apiListDTO;
    }

    /**
     * Converts a List object of APIs into quick info DTO List.
     *
     * @param apiList List of APIs
     * @return APIListDTO object containing APIDTOs
     */
    public static APIMetadataListDTO fromAPIListToAPIMetadataListDTO(List<API> apiList) {

        APIMetadataListDTO apiMetadataListDTO = new APIMetadataListDTO();
        List<APIMetadataDTO> apiInfoDTOs = apiMetadataListDTO.getList();
        for (API api : apiList) {
            apiInfoDTOs.add(fromAPIToAPIMetadataDTO(api));
        }
        apiMetadataListDTO.setCount(apiInfoDTOs.size());

        return apiMetadataListDTO;
    }

    /**
     * Converts a List object of URITemplates into APIOperations DTO List.
     *
     * @param uriTemplateList uriTemplateList
     * @return List of APIOperationsDTO object
     */
    public static List<APIOperationsDTO> fromURITemplateListToOprationList(List<URITemplate> uriTemplateList) {

        int index = 0;
        List<APIOperationsDTO> operations = new ArrayList<>();
        for (URITemplate uriTemplate : uriTemplateList) {
            uriTemplate.setId((index++));
            operations.add(fromURITemplateToOperationList(uriTemplate));
        }
        return operations;
    }

    /**
     * Converts a uriTemplate to APIOperations DTO.
     *
     * @param uriTemplate uriTemplate
     * @return APIOperationsDTO object
     */
    private static APIOperationsDTO fromURITemplateToOperationList(URITemplate uriTemplate) {

        APIOperationsDTO operation = new APIOperationsDTO();
        operation.setId(Integer.toString(uriTemplate.getId()));
        operation.setVerb(uriTemplate.getHTTPVerb());
        operation.setTarget(uriTemplate.getUriTemplate());
        return operation;
    }

    /**
     * Converts a List of APIOperationsDTO into URITemplates.
     *
     * @param operations list of APIOperationsDTO
     * @return List of URITemplate objects
     */
    public static Set<URITemplate> fromOperationListToURITemplateList(List<APIOperationsDTO> operations) {

        Set<URITemplate> uriTemplateList = new HashSet<>();
        for (APIOperationsDTO operation : operations) {
            URITemplate template = fromOperationToURITemplate(operation);
            uriTemplateList.add(template);
        }
        return uriTemplateList;
    }

    /**
     * Converts a single APIOperationsDTO to URITemplate.
     *
     * @param operation APIOperationsDTO
     * @return URITemplate object
     */
    private static URITemplate fromOperationToURITemplate(APIOperationsDTO operation) {

        URITemplate template = new URITemplate();
        template.setHTTPVerb(operation.getVerb());
        template.setUriTemplate(operation.getTarget());
        return template;
    }

    /**
     * Creates a minimal DTO representation of an API object.
     *
     * @param api API object
     * @return a minimal representation DTO
     */
    public static APIInfoDTO fromAPIToInfoDTO(API api) {

        APIInfoDTO apiInfoDTO = new APIInfoDTO();
        apiInfoDTO.setDescription(api.getDescription());
        String context = api.getContextTemplate();
        if (context.endsWith(FORWARD_SLASH + RestApiConstants.API_VERSION_PARAM)) {
            context = context.replace(FORWARD_SLASH + RestApiConstants.API_VERSION_PARAM, EMPTY_STRING);
        }
        apiInfoDTO.setContext(context);
        apiInfoDTO.setId(api.getUUID());
        APIIdentifier apiId = api.getId();
        apiInfoDTO.setName(apiId.getApiName());
        apiInfoDTO.setVersion(apiId.getVersion());
        apiInfoDTO.setType(api.getType());
        String providerName = api.getId().getProviderName();
        apiInfoDTO.setProvider(APIUtil.replaceEmailDomainBack(providerName));
        apiInfoDTO.setLifeCycleStatus(api.getStatus());
        apiInfoDTO.setHasThumbnail(!StringUtils.isBlank(api.getThumbnailUrl()));
        if (api.getAudience() != null) {
            apiInfoDTO.setAudience(org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIInfoDTO.AudienceEnum
                    .valueOf(api.getAudience()));
        }
        if (api.getAudiences() != null) {
            Set<String> audiences = api.getAudiences();
            if (audiences.contains(APIConstants.ALL_AUDIENCES) && audiences.size() > 1) {
                audiences.remove(APIConstants.ALL_AUDIENCES);
            }
            apiInfoDTO.setAudiences(new ArrayList<>(audiences));
        }
        if (api.getCreatedTime() != null) {
            Date createdTime = new Date(Long.parseLong(api.getCreatedTime()));
            apiInfoDTO.setCreatedTime(String.valueOf(createdTime.getTime()));
        }
        if (api.getLastUpdated() != null) {
            Date lastUpdatedTime = api.getLastUpdated();
            apiInfoDTO.setUpdatedTime(String.valueOf(lastUpdatedTime.getTime()));
        }
        apiInfoDTO.updatedBy(api.getUpdatedBy());
        apiInfoDTO.setAdvertiseOnly(api.isAdvertiseOnly());
        if (api.getAdditionalProperties() != null) {
            JSONObject additionalProperties = api.getAdditionalProperties();
            List<APIInfoAdditionalPropertiesDTO> additionalPropertiesList = new ArrayList<>();
            Map<String, APIInfoAdditionalPropertiesMapDTO> additionalPropertiesMap = new HashMap<>();
            for (Object propertyKey : additionalProperties.keySet()) {
                APIInfoAdditionalPropertiesDTO additionalPropertiesDTO = new APIInfoAdditionalPropertiesDTO();
                APIInfoAdditionalPropertiesMapDTO apiInfoAdditionalPropertiesMapDTO =
                        new APIInfoAdditionalPropertiesMapDTO();
                String key = (String) propertyKey;
                int index = key.lastIndexOf(APIConstants.API_RELATED_CUSTOM_PROPERTIES_SURFIX);
                additionalPropertiesDTO.setValue((String) additionalProperties.get(key));
                apiInfoAdditionalPropertiesMapDTO.setValue((String) additionalProperties.get(key));
                if (index > 0) {
                    additionalPropertiesDTO.setName(key.substring(0, index));
                    apiInfoAdditionalPropertiesMapDTO.setName(key.substring(0, index));
                    additionalPropertiesDTO.setDisplay(true);
                } else {
                    additionalPropertiesDTO.setName(key);
                    apiInfoAdditionalPropertiesMapDTO.setName(key);
                    additionalPropertiesDTO.setDisplay(false);
                }
                apiInfoAdditionalPropertiesMapDTO.setDisplay(false);
                additionalPropertiesMap.put(key, apiInfoAdditionalPropertiesMapDTO);
                additionalPropertiesList.add(additionalPropertiesDTO);
            }
            apiInfoDTO.setAdditionalProperties(additionalPropertiesList);
            apiInfoDTO.setAdditionalPropertiesMap(additionalPropertiesMap);
            apiInfoDTO.setGatewayVendor(api.getGatewayVendor());
        }
        apiInfoDTO.setMonetizedInfo(api.isMonetizationEnabled());
        apiInfoDTO.setBusinessOwner(api.getBusinessOwner());
        apiInfoDTO.setBusinessOwnerEmail(api.getBusinessOwnerEmail());
        apiInfoDTO.setTechnicalOwner(api.getTechnicalOwner());
        apiInfoDTO.setTechnicalOwnerEmail(api.getTechnicalOwnerEmail());
        apiInfoDTO.setGatewayType(api.getGatewayType());
        apiInfoDTO.setGatewayVendor(api.getGatewayVendor());
        apiInfoDTO.setEgress(api.isEgress() == 1); // true -1, false - 0
        apiInfoDTO.setSubtype(api.getSubtype());
        apiInfoDTO.setInitiatedFromGateway(api.isInitiatedFromGateway());
        apiInfoDTO.setDisplayName(api.getDisplayName() != null ? api.getDisplayName() : api.getId().getApiName());
        return apiInfoDTO;
    }

    /**
     * Creates a very minimal DTO representation of an API object.
     *
     * @param api API object
     * @return a very minimal representation DTO
     */
    public static APIMetadataDTO fromAPIToAPIMetadataDTO(API api) {

        String context = api.getContextTemplate();
        if (context.endsWith(FORWARD_SLASH + RestApiConstants.API_VERSION_PARAM)) {
            context = context.replace(FORWARD_SLASH + RestApiConstants.API_VERSION_PARAM, EMPTY_STRING);
        }

        APIMetadataDTO apiMetadataDTO = new APIMetadataDTO();
        apiMetadataDTO.setContext(context);
        apiMetadataDTO.setId(api.getUuid());
        APIIdentifier apiId = api.getId();
        apiMetadataDTO.setName(apiId.getApiName());
        apiMetadataDTO.setVersion(apiId.getVersion());
        String providerName = api.getId().getProviderName();
        apiMetadataDTO.setProvider(APIUtil.replaceEmailDomainBack(providerName));

        return apiMetadataDTO;
    }

    /**
     * Converts a List object of APIs into quick info DTO List.
     *
     * @param apiList List of APIs
     * @return APIListDTO object containing APIDTOs
     */
    public static MCPServerMetadataListDTO fromAPIListToMCPServerMetadataListDTO(List<API> apiList) {

        MCPServerMetadataListDTO mcpServerMetadataListDTO = new MCPServerMetadataListDTO();
        List<MCPServerMetadataDTO> apiInfoDTOs = mcpServerMetadataListDTO.getList();
        for (API api : apiList) {
            apiInfoDTOs.add(fromAPIToMCPServerMetadataDTO(api));
        }
        mcpServerMetadataListDTO.setCount(apiInfoDTOs.size());

        return mcpServerMetadataListDTO;
    }

    /**
     * Creates a very minimal DTO representation of an MCP Server object.
     *
     * @param api API object
     * @return a very minimal representation DTO
     */
    public static MCPServerMetadataDTO fromAPIToMCPServerMetadataDTO(API api) {

        APIIdentifier apiId = api.getId();
        MCPServerMetadataDTO mcpServerMetadataDTO = new MCPServerMetadataDTO();
        mcpServerMetadataDTO.setId(apiId.getUUID());
        mcpServerMetadataDTO.setName(apiId.getApiName());
        mcpServerMetadataDTO.setVersion(apiId.getVersion());
        mcpServerMetadataDTO.setProvider(APIUtil.replaceEmailDomainBack(apiId.getProviderName()));

        return mcpServerMetadataDTO;
    }

    /**
     * Creates  a list of conversion policies into a DTO.
     *
     * @param conversionPolicyStr conversion policies
     * @return ConversionPolicyListDTO object containing ConversionPolicyInfoDTOs
     * @throws APIManagementException
     */
    public static ResourcePolicyListDTO fromResourcePolicyStrToDTO(String conversionPolicyStr)
            throws APIManagementException {

        ResourcePolicyListDTO policyListDTO = new ResourcePolicyListDTO();
        List<ResourcePolicyInfoDTO> policyInfoDTOs = policyListDTO.getList();
        if (StringUtils.isNotEmpty(conversionPolicyStr)) {
            try {
                JSONObject conversionPolicyObj = (JSONObject) new JSONParser().parse(conversionPolicyStr);
                for (Object key : conversionPolicyObj.keySet()) {
                    JSONObject policyInfo = (JSONObject) conversionPolicyObj.get(key);
                    String keyStr = ((String) key);
                    ResourcePolicyInfoDTO policyInfoDTO = new ResourcePolicyInfoDTO();
                    policyInfoDTO.setId(policyInfo.get(RestApiConstants.SEQUENCE_ARTIFACT_ID).toString());
                    policyInfoDTO.setHttpVerb(policyInfo.get(RestApiConstants.HTTP_METHOD).toString());
                    if (keyStr.contains("_")) {
                        policyInfoDTO.setResourcePath(keyStr.substring(0, keyStr.lastIndexOf("_")));
                    } else {
                        policyInfoDTO.setResourcePath(keyStr);
                    }
                    policyInfoDTO.setContent(policyInfo.get(RestApiConstants.SEQUENCE_CONTENT).toString());
                    policyInfoDTOs.add(policyInfoDTO);
                }
            } catch (ParseException e) {
                throw new APIManagementException("Couldn't parse the conversion policy string.", e);
            }
        }
        policyListDTO.setCount(policyInfoDTOs.size());
        return policyListDTO;
    }

    /**
     * Creates a DTO consisting a single conversion policy.
     *
     * @param conversionPolicyStr conversion policy string
     * @return ConversionPolicyInfoDTO consisting given conversion policy string
     * @throws APIManagementException
     */
    public static ResourcePolicyInfoDTO fromResourcePolicyStrToInfoDTO(String conversionPolicyStr)
            throws APIManagementException {

        ResourcePolicyInfoDTO policyInfoDTO = new ResourcePolicyInfoDTO();
        if (StringUtils.isNotEmpty(conversionPolicyStr)) {
            try {
                JSONObject conversionPolicyObj = (JSONObject) new JSONParser().parse(conversionPolicyStr);
                for (Object key : conversionPolicyObj.keySet()) {
                    JSONObject policyInfo = (JSONObject) conversionPolicyObj.get(key);
                    String keyStr = ((String) key);
                    policyInfoDTO.setId(policyInfo.get(RestApiConstants.SEQUENCE_ARTIFACT_ID).toString());
                    policyInfoDTO.setHttpVerb(policyInfo.get(RestApiConstants.HTTP_METHOD).toString());
                    if (keyStr.contains("_")) {
                        policyInfoDTO.setResourcePath(keyStr.substring(0, keyStr.lastIndexOf("_")));
                    } else {
                        policyInfoDTO.setResourcePath(keyStr);
                    }
                    policyInfoDTO.setContent(policyInfo.get(RestApiConstants.SEQUENCE_CONTENT).toString());
                }
            } catch (ParseException e) {
                throw new APIManagementException("Couldn't parse the conversion policy string.", e);
            }
        }
        return policyInfoDTO;
    }

    /**
     * Sets pagination urls for a APIListDTO object given pagination parameters and url parameters.
     *
     * @param apiListDTO a APIListDTO object
     * @param query      search condition
     * @param limit      max number of objects returned
     * @param offset     starting index
     * @param size       max offset
     */
    public static void setPaginationParams(Object apiListDTO, String query, int offset, int limit, int size) {

        //acquiring pagination parameters and setting pagination urls
        Map<String, Integer> paginatedParams = RestApiCommonUtil.getPaginationParams(offset, limit, size);
        String paginatedPrevious = EMPTY_STRING;
        String paginatedNext = EMPTY_STRING;

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = RestApiCommonUtil
                    .getAPIPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT), query);
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = RestApiCommonUtil
                    .getAPIPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT), query);
        }

        PaginationDTO paginationDTO = CommonMappingUtil
                .getPaginationDTO(limit, offset, size, paginatedNext, paginatedPrevious);
        ((APIListDTO) apiListDTO).setPagination(paginationDTO);
    }

    /**
     * Sets pagination urls for a APIMetadataListDTO object given pagination parameters and url parameters.
     *
     * @param apiMetadataListDTO a APIMetadataListDTO object
     * @param alias      alias of the certificate
     * @param limit      max number of objects returned
     * @param offset     starting index
     * @param size       max offset
     */
    public static void setPaginationParamsForAPIMetadataListDTO(
            Object apiMetadataListDTO, String alias, int offset, int limit, int size) {

        //acquiring pagination parameters and setting pagination urls
        Map<String, Integer> paginatedParams = RestApiCommonUtil.getPaginationParams(offset, limit, size);
        String paginatedPrevious = EMPTY_STRING;
        String paginatedNext = EMPTY_STRING;

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = RestApiCommonUtil
                    .getCertificateUsagePaginatedURL(alias,
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT));
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = RestApiCommonUtil
                    .getCertificateUsagePaginatedURL(alias,
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT));
        }

        PaginationDTO paginationDTO = CommonMappingUtil
                .getPaginationDTO(limit, offset, size, paginatedNext, paginatedPrevious);
        ((APIMetadataListDTO) apiMetadataListDTO).setPagination(paginationDTO);
    }

    private static String checkAndSetVersionParam(String context) {
        // This is to support the new Pluggable version strategy
        // if the context does not contain any {version} segment, we use the default version strategy.
        if (!context.contains(RestApiConstants.API_VERSION_PARAM)) {
            if (!context.endsWith(FORWARD_SLASH)) {
                context = context + FORWARD_SLASH;
            }
            context = context + RestApiConstants.API_VERSION_PARAM;
        }
        return context;
    }

    private static String getThumbnailUri(String uuid) {

        return RestApiConstants.RESOURCE_PATH_THUMBNAIL.replace(RestApiConstants.APIID_PARAM, uuid);
    }

    /**
     * Maps the visibility from {@link APIDTO.VisibilityEnum} to the corresponding internal API visibility string.
     *
     * @param visibility the visibility enum from APIDTO
     * @return the internal visibility string, or null if the input is null
     */
    private static String mapVisibilityFromDTOtoAPI(APIDTO.VisibilityEnum visibility) {

        if (visibility == null) {
            return null;
        }
        return mapVisibility(visibility.name());
    }

    /**
     * Maps the visibility from {@link MCPServerDTO.VisibilityEnum} to the corresponding internal API visibility string.
     *
     * @param visibility the visibility enum from MCPServerDTO
     * @return the internal visibility string, or null if the input is null
     */
    private static String mapVisibilityFromDTOtoAPI(MCPServerDTO.VisibilityEnum visibility) {

        if (visibility == null) {
            return null;
        }
        return mapVisibility(visibility.name());
    }

    /**
     * Converts the string name of a visibility level to its internal representation.
     *
     * @param visibilityName the name of the visibility level
     * @return the internal visibility string, or null if the input doesn't match a known level
     */
    private static String mapVisibility(String visibilityName) {

        switch (visibilityName) {
            case APIConstants.PUBLIC:
                return APIConstants.API_GLOBAL_VISIBILITY;
            case APIConstants.PRIVATE:
                return APIConstants.API_PRIVATE_VISIBILITY;
            case APIConstants.RESTRICTED:
                return APIConstants.API_RESTRICTED_VISIBILITY;
            default:
                return null;
        }
    }

    /**
     * Maps the subscription availability from {@link APIDTO.SubscriptionAvailabilityEnum}
     * to the corresponding internal subscription availability string.
     *
     * @param availability the subscription availability enum from APIDTO
     * @return the internal subscription availability string, or null if the input is null
     */
    private static String mapSubscriptionAvailabilityFromDTOtoAPI(APIDTO.SubscriptionAvailabilityEnum availability) {

        if (availability == null) {
            return null;
        }
        return mapSubscriptionAvailability(availability.name());
    }

    /**
     * Maps the subscription availability from {@link MCPServerDTO.SubscriptionAvailabilityEnum}
     * to the corresponding internal subscription availability string.
     *
     * @param availability the subscription availability enum from MCPServerDTO
     * @return the internal subscription availability string, or null if the input is null
     */
    private static String mapSubscriptionAvailabilityFromDTOtoAPI(
            MCPServerDTO.SubscriptionAvailabilityEnum availability) {

        if (availability == null) {
            return null;
        }
        return mapSubscriptionAvailability(availability.name());
    }

    /**
     * Converts the string name of a subscription availability level to its internal representation.
     *
     * @param availabilityName the name of the subscription availability level
     * @return the internal subscription availability string, or null if the input doesn't match a known level
     */
    private static String mapSubscriptionAvailability(String availabilityName) {

        switch (availabilityName) {
            case APIConstants.CURRENT_TENANT:
                return APIConstants.SUBSCRIPTION_TO_CURRENT_TENANT;
            case APIConstants.ALL_TENANTS:
                return APIConstants.SUBSCRIPTION_TO_ALL_TENANTS;
            case APIConstants.SPECIFIC_TENANTS:
                return APIConstants.SUBSCRIPTION_TO_SPECIFIC_TENANTS;
            default:
                return null;
        }
    }

    private static String updateContextWithVersion(String version, String contextVal, String context) {
        // This condition should not be true for any occasion but we keep it so that there are no loopholes in
        // the flow.
        if (version == null) {
            // context template patterns - /{version}/foo or /foo/{version}
            // if the version is null, then we remove the /{version} part from the context
            context = contextVal.replace(FORWARD_SLASH + RestApiConstants.API_VERSION_PARAM, EMPTY_STRING);
        } else {
            context = context.replace(RestApiConstants.API_VERSION_PARAM, version);
        }
        return context;
    }

    /**
     * Populates the {@link API} model with max TPS values from an {@link MCPServerDTO}.
     *
     * @param mcpServerDto the MCP server DTO containing max TPS details
     * @param api          the API model to populate
     */
    private static void setMaxTpsFromMcpServerDTOToModel(MCPServerDTO mcpServerDto, API api) {

        if (mcpServerDto == null) {
            return;
        }
        MaxTpsDTO maxTpsDto = mcpServerDto.getMaxTps();
        BackendThrottlingConfiguration backendConfig = new BackendThrottlingConfiguration();

        if (maxTpsDto != null) {
            applyCommonMaxTpsToModel(
                    maxTpsDto.getProduction(),
                    maxTpsDto.getProductionTimeUnit() != null ? maxTpsDto.getProductionTimeUnit().toString() : null,
                    maxTpsDto.getSandbox(),
                    maxTpsDto.getSandboxTimeUnit() != null ? maxTpsDto.getSandboxTimeUnit().toString() : null,
                    api,
                    backendConfig
            );
        }
        api.setBackendThrottlingConfiguration(backendConfig);
    }

    /**
     * Populates the {@link API} model with max TPS values from an {@link APIDTO}.
     *
     * @param apiDto the API DTO containing max TPS details
     * @param api    the API model to populate
     */
    private static void setMaxTpsFromApiDTOToModel(APIDTO apiDto, API api) {

        if (apiDto == null) {
            return;
        }
        APIMaxTpsDTO maxTpsDto = apiDto.getMaxTps();
        BackendThrottlingConfiguration backendConfig = new BackendThrottlingConfiguration();

        if (maxTpsDto != null) {
            applyCommonMaxTpsToModel(
                    maxTpsDto.getProduction(),
                    maxTpsDto.getProductionTimeUnit() != null ? maxTpsDto.getProductionTimeUnit().toString() : null,
                    maxTpsDto.getSandbox(),
                    maxTpsDto.getSandboxTimeUnit() != null ? maxTpsDto.getSandboxTimeUnit().toString() : null,
                    api,
                    backendConfig
            );

            APIMaxTpsTokenBasedThrottlingConfigurationDTO tokenConfig =
                    maxTpsDto.getTokenBasedThrottlingConfiguration();
            if (tokenConfig != null && tokenConfig.isIsTokenBasedThrottlingEnabled()) {
                backendConfig.setTokenBasedThrottlingConfiguration(buildThrottlingConfiguration(tokenConfig));
            }
        }
        api.setBackendThrottlingConfiguration(backendConfig);
    }

    /**
     * Applies common max TPS values from DTOs into the API model and backend throttling configuration.
     *
     * @param productionTps      production TPS value
     * @param productionTimeUnit production time unit
     * @param sandboxTps         sandbox TPS value
     * @param sandboxTimeUnit    sandbox time unit
     * @param api                API model to populate
     * @param backendConfig      backend throttling configuration to populate
     */
    private static void applyCommonMaxTpsToModel(Long productionTps, String productionTimeUnit,
                                                 Long sandboxTps, String sandboxTimeUnit,
                                                 API api, BackendThrottlingConfiguration backendConfig) {

        if (productionTps != null) {
            String prodTpsStr = productionTps.toString();
            api.setProductionMaxTps(prodTpsStr);
            backendConfig.setProductionMaxTps(prodTpsStr);
        }
        if (productionTimeUnit != null) {
            String prodTimeUnitMs = getTimeUnitInMilliseconds(productionTimeUnit);
            api.setProductionTimeUnit(prodTimeUnitMs);
            backendConfig.setProductionTimeUnit(prodTimeUnitMs);
        }
        if (sandboxTps != null) {
            String sandboxTpsStr = sandboxTps.toString();
            api.setSandboxMaxTps(sandboxTpsStr);
            backendConfig.setSandboxMaxTps(sandboxTpsStr);
        }
        if (sandboxTimeUnit != null) {
            String sandboxTimeUnitMs = getTimeUnitInMilliseconds(sandboxTimeUnit);
            api.setSandboxTimeUnit(sandboxTimeUnitMs);
            backendConfig.setSandboxTimeUnit(sandboxTimeUnitMs);
        }
    }

    /**
     * Retrieves millisecond value for pre-defined time units.
     * @param timeUnit String value of the time unit
     * @return Millisecond value of the time unit
     */
    private static String getTimeUnitInMilliseconds(String timeUnit) {
        switch(timeUnit) {
            case APIConstants.API_BACKEND_THROTTLE_TIMEUNIT_MINUTE:
                return APIConstants.API_BACKEND_THROTTLE_TIMEUNIT_MINUTE_MS;
            case APIConstants.API_BACKEND_THROTTLE_TIMEUNIT_HOUR:
                return APIConstants.API_BACKEND_THROTTLE_TIMEUNIT_HOUR_MS;
            default:
                return APIConstants.API_BACKEND_THROTTLE_TIMEUNIT_SECOND_MS;
        }
    }

    /**
     * Retrieved predefined strings for millisecond value.
     * @param timeUnitInMillis Time in milliseconds
     * @return  Time unit string for the given milliseconds value
     */
    private static String convertFromMilliseconds(String timeUnitInMillis) {
        switch(timeUnitInMillis) {
            case APIConstants.API_BACKEND_THROTTLE_TIMEUNIT_MINUTE_MS:
                return APIConstants.API_BACKEND_THROTTLE_TIMEUNIT_MINUTE;
            case APIConstants.API_BACKEND_THROTTLE_TIMEUNIT_HOUR_MS:
                return APIConstants.API_BACKEND_THROTTLE_TIMEUNIT_HOUR;
            default:
                return APIConstants.API_BACKEND_THROTTLE_TIMEUNIT_SECOND;
        }
    }

    public static APIDTO fromAPItoDTO(API model) throws APIManagementException {

        return fromAPItoDTO(model, false, null);
    }

    public static APIDTO fromAPItoDTO(API model, APIProvider apiProvider)
            throws APIManagementException {

        return fromAPItoDTO(model, false, apiProvider);
    }

    public static APIDTO fromAPItoDTO(API model, boolean preserveCredentials,
                                      APIProvider apiProviderParam)
            throws APIManagementException {

        APIProvider apiProvider;
        if (apiProviderParam != null) {
            apiProvider = apiProviderParam;
        } else {
            apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        }
        APIDTO dto = new APIDTO();
        dto.setName(model.getId().getApiName());
        dto.setDisplayName((model.getDisplayName() != null && !model.getDisplayName().trim().isEmpty())
                ? model.getDisplayName() : model.getId().getApiName());
        dto.setVersion(model.getId().getVersion());
        String providerName = model.getId().getProviderName();
        dto.setProvider(APIUtil.replaceEmailDomainBack(providerName));
        dto.setId(model.getUUID());
        String context = model.getContextTemplate();
        if (context.endsWith(FORWARD_SLASH + RestApiConstants.API_VERSION_PARAM)) {
            context = context.replace(FORWARD_SLASH + RestApiConstants.API_VERSION_PARAM, EMPTY_STRING);
        }
        dto.setContext(context);
        dto.setCreatedTime(model.getCreatedTime());
        if (model.getLastUpdated() != null) {
            dto.setLastUpdatedTime(Long.toString(model.getLastUpdated().getTime()));
        }
        dto.setDescription(model.getDescription());
        dto.setIsDefaultVersion(model.isDefaultVersion());
        dto.setIsRevision(model.isRevision());
        dto.setRevisionedApiId(model.getRevisionedApiId());
        dto.setRevisionId(model.getRevisionId());
        dto.setEnableSchemaValidation(model.isEnabledSchemaValidation());
        dto.setEnableSubscriberVerification(model.isEnableSubscriberVerification());
        AdvertiseInfoDTO advertiseInfoDTO = new AdvertiseInfoDTO();
        advertiseInfoDTO.setAdvertised(model.isAdvertiseOnly());
        advertiseInfoDTO.setApiExternalProductionEndpoint(model.getApiExternalProductionEndpoint());
        advertiseInfoDTO.setApiExternalSandboxEndpoint(model.getApiExternalSandboxEndpoint());
        advertiseInfoDTO.setOriginalDevPortalUrl(model.getRedirectURL());
        advertiseInfoDTO.setApiOwner(model.getApiOwner());
        if (model.getAdvertiseOnlyAPIVendor() != null) {
            advertiseInfoDTO.setVendor(AdvertiseInfoDTO.VendorEnum.valueOf(model.getAdvertiseOnlyAPIVendor()));
        }
        dto.setAdvertiseInfo(advertiseInfoDTO);

        if (APIConstants.ENABLED.equals(model.getResponseCache())) {
            dto.setResponseCachingEnabled(Boolean.TRUE);
        } else {
            dto.setResponseCachingEnabled(Boolean.FALSE);
        }
        String serviceKey = model.getServiceInfo("key");
        if (StringUtils.isNotEmpty(serviceKey)) {
            APIServiceInfoDTO apiServiceInfoDTO = new APIServiceInfoDTO();
            apiServiceInfoDTO.setKey(serviceKey);
            apiServiceInfoDTO.setOutdated(Boolean.parseBoolean(model.getServiceInfo("outdated")));
            apiServiceInfoDTO.setName(model.getServiceInfo("name"));
            apiServiceInfoDTO.setVersion(model.getServiceInfo("version"));
            dto.setServiceInfo(apiServiceInfoDTO);
        }
        dto.setCacheTimeout(model.getCacheTimeout());
        String endpointConfig = model.getEndpointConfig();
        if (!StringUtils.isBlank(endpointConfig)) {
            try {
                JSONParser parser = new JSONParser();
                JSONObject endpointConfigJson = (JSONObject) parser.parse(endpointConfig);
                // AWS Lambda: set secret key based on preserveCredentials
                if (APIConstants.ENDPOINT_TYPE_AWSLAMBDA.equalsIgnoreCase(
                        (String) endpointConfigJson.get(APIConstants.API_ENDPOINT_CONFIG_PROTOCOL_TYPE))) {
                    handleAWSCredentials(endpointConfigJson, preserveCredentials);
                }
                CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
                if (endpointConfigJson.get(APIConstants.ENDPOINT_SECURITY) != null) {
                    JSONObject endpointSecurity = (JSONObject) endpointConfigJson.get(APIConstants.ENDPOINT_SECURITY);
                    if (endpointSecurity.get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_PRODUCTION) != null) {
                        JSONObject productionEndpointSecurity = (JSONObject) endpointSecurity
                                .get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_PRODUCTION);
                        String productionEndpointType = (String) productionEndpointSecurity
                                .get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_TYPE);
                        if (productionEndpointSecurity
                                .get(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS) != null) {
                            Object customParametersObject = productionEndpointSecurity
                                    .get(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS);
                            JSONObject customParameters;
                            if (customParametersObject instanceof JSONObject) {
                                customParameters = (JSONObject) customParametersObject;
                            } else {
                                customParameters = (JSONObject) parser.parse(customParametersObject.toString());
                            }
                            decryptCustomOauthParameters(customParameters, cryptoUtil);
                            productionEndpointSecurity.put(
                                    APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS, customParameters);
                        }

                        if (APIConstants.OAuthConstants.OAUTH.equals(productionEndpointType)) {
                            String clientSecret = (String) productionEndpointSecurity
                                    .get(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET);
                            if (StringUtils.isNotEmpty(clientSecret)) {
                                productionEndpointSecurity.put(APIConstants
                                                .OAuthConstants.OAUTH_CLIENT_SECRET,
                                        new String(cryptoUtil.base64DecodeAndDecrypt(clientSecret)));
                            }
                        }
                        if (APIConstants.ENDPOINT_SECURITY_TYPE_API_KEY.equals(productionEndpointType)) {
                            String apiKeyValue = (String) productionEndpointSecurity
                                    .get(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE);
                            if (StringUtils.isNotEmpty(apiKeyValue)) {
                                productionEndpointSecurity.put(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE,
                                        new String(cryptoUtil.base64DecodeAndDecrypt(apiKeyValue)));
                            }
                        }
                        if (APIConstants.ENDPOINT_SECURITY_TYPE_AWS.equals(productionEndpointType)) {
                            String awsSecretKey = (String) productionEndpointSecurity.get(
                                    APIConstants.ENDPOINT_SECURITY_AWS_SECRET_KEY);
                            if (StringUtils.isNotEmpty(awsSecretKey)) {
                                productionEndpointSecurity.put(APIConstants.ENDPOINT_SECURITY_AWS_SECRET_KEY,
                                        new String(cryptoUtil.base64DecodeAndDecrypt(awsSecretKey)));
                            }
                        }
                        endpointSecurity.put(APIConstants.OAuthConstants.ENDPOINT_SECURITY_PRODUCTION,
                                productionEndpointSecurity);
                        endpointConfigJson.put(APIConstants.ENDPOINT_SECURITY, endpointSecurity);
                    }
                    if (endpointSecurity.get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_SANDBOX) != null) {
                        JSONObject sandboxEndpointSecurity = (JSONObject) endpointSecurity
                                .get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_SANDBOX);
                        String sandboxEndpointType = (String) sandboxEndpointSecurity
                                .get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_TYPE);
                        if (sandboxEndpointSecurity
                                .get(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS) != null) {
                            Object customParametersObject = sandboxEndpointSecurity
                                    .get(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS);
                            JSONObject customParameters;
                            if (customParametersObject instanceof JSONObject) {
                                customParameters = (JSONObject) customParametersObject;
                            } else {
                                customParameters = (JSONObject) parser.parse(customParametersObject.toString());
                            }
                            decryptCustomOauthParameters(customParameters, cryptoUtil);
                            sandboxEndpointSecurity.put(
                                    APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS, customParameters);
                        }
                        if (APIConstants.OAuthConstants.OAUTH.equals(sandboxEndpointType)) {
                            String clientSecret = (String) sandboxEndpointSecurity
                                    .get(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET);
                            if (StringUtils.isNotEmpty(clientSecret)) {
                                sandboxEndpointSecurity.put(APIConstants
                                                .OAuthConstants.OAUTH_CLIENT_SECRET,
                                        new String(cryptoUtil.base64DecodeAndDecrypt(clientSecret)));
                            }
                        }
                        if (APIConstants.ENDPOINT_SECURITY_TYPE_API_KEY.equals(sandboxEndpointType)) {
                            String apiKeyValue = (String) sandboxEndpointSecurity
                                    .get(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE);
                            if (StringUtils.isNotEmpty(apiKeyValue)) {
                                sandboxEndpointSecurity.put(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE,
                                        new String(cryptoUtil.base64DecodeAndDecrypt(apiKeyValue)));
                            }
                        }
                        if (APIConstants.ENDPOINT_SECURITY_TYPE_AWS.equals(sandboxEndpointType)) {
                            String awsSecretKey = (String) sandboxEndpointSecurity
                                    .get(APIConstants.ENDPOINT_SECURITY_AWS_SECRET_KEY);
                            if (StringUtils.isNotEmpty(awsSecretKey)) {
                                sandboxEndpointSecurity.put(APIConstants.ENDPOINT_SECURITY_AWS_SECRET_KEY,
                                        new String(cryptoUtil.base64DecodeAndDecrypt(awsSecretKey)));
                            }
                        }
                        endpointSecurity.put(APIConstants.OAuthConstants.ENDPOINT_SECURITY_SANDBOX,
                                sandboxEndpointSecurity);
                        endpointConfigJson.put(APIConstants.ENDPOINT_SECURITY, endpointSecurity);
                    }
                    JSONObject jsonObject = handleEndpointSecurity(model,
                            (JSONObject) endpointConfigJson.get(APIConstants.ENDPOINT_SECURITY), preserveCredentials);
                    endpointConfigJson.put(APIConstants.ENDPOINT_SECURITY, jsonObject);
                }
                dto.setEndpointConfig(endpointConfigJson);
            } catch (ParseException e) {
                //logs the error and continues as this is not a blocker
                log.error("Cannot convert endpoint configurations when setting endpoint for API. " +
                        "API ID = " + model.getId(), e);
            } catch (CryptoException e) {
                log.error("Error while decrypting client credentials or secret parameters for API: "
                        + model.getId(), e);
            }
        }
        dto.setHasThumbnail(!StringUtils.isBlank(model.getThumbnailUrl()));
        List<MediationPolicyDTO> mediationPolicies = new ArrayList<>();
        String inMedPolicyName = model.getInSequence();
        if (inMedPolicyName != null && !inMedPolicyName.isEmpty()) {
            String type = APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN;
            Mediation mediation = model.getInSequenceMediation();
            String mediationPolicyUUID = (mediation != null) ? mediation.getUuid() : null;
            boolean sharedStatus = (mediation != null) ? mediation.isGlobal() : false;

            MediationPolicyDTO inMedPolicy = new MediationPolicyDTO();
            inMedPolicy.setName(inMedPolicyName);
            inMedPolicy.setType(type.toUpperCase());
            inMedPolicy.setShared(sharedStatus);
            inMedPolicy.setId(mediationPolicyUUID);
            mediationPolicies.add(inMedPolicy);
        }
        String outMedPolicyName = model.getOutSequence();
        if (outMedPolicyName != null && !outMedPolicyName.isEmpty()) {
            String type = APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT;
            Mediation mediation = model.getOutSequenceMediation();
            String mediationPolicyUUID = (mediation != null) ? mediation.getUuid() : null;
            boolean sharedStatus = (mediation != null) ? mediation.isGlobal() : false;
            MediationPolicyDTO outMedPolicy = new MediationPolicyDTO();
            outMedPolicy.setName(outMedPolicyName);
            outMedPolicy.setType(type.toUpperCase());
            outMedPolicy.setShared(sharedStatus);
            outMedPolicy.setId(mediationPolicyUUID);
            mediationPolicies.add(outMedPolicy);
        }
        String faultSequenceName = model.getFaultSequence();
        if (faultSequenceName != null && !faultSequenceName.isEmpty()) {
            String type = APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT;
            Mediation mediation = model.getFaultSequenceMediation();
            String mediationPolicyUUID = (mediation != null) ? mediation.getUuid() : null;
            boolean sharedStatus = (mediation != null) ? mediation.isGlobal() : false;
            MediationPolicyDTO faultMedPolicy = new MediationPolicyDTO();
            faultMedPolicy.setName(faultSequenceName);
            faultMedPolicy.setType(type.toUpperCase());
            faultMedPolicy.setShared(sharedStatus);
            faultMedPolicy.setId(mediationPolicyUUID);
            mediationPolicies.add(faultMedPolicy);
        }
        dto.setMediationPolicies(mediationPolicies);
        dto.setLifeCycleStatus(model.getStatus());
        if (model.getApiPolicies() != null) {
            dto.setApiPolicies(OperationPolicyMappingUtil.fromOperationPolicyListToDTO(model.getApiPolicies(),
                    model.getUuid(), preserveCredentials));
        }
        String subscriptionAvailability = model.getSubscriptionAvailability();
        if (subscriptionAvailability != null) {
            dto.setSubscriptionAvailability(mapSubscriptionAvailabilityFromAPItoDTO(subscriptionAvailability));
        }
        if (model.getSubscriptionAvailableTenants() != null) {
            dto.setSubscriptionAvailableTenants(Arrays.asList(model.getSubscriptionAvailableTenants().split(",")));
        }
        String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(model.getId()
                .getProviderName()));
        boolean isAsyncAPI = APIDTO.TypeEnum.WS.toString().equals(model.getType())
                || APIDTO.TypeEnum.WEBSUB.toString().equals(model.getType())
                || APIDTO.TypeEnum.SSE.toString().equals(model.getType())
                || APIDTO.TypeEnum.ASYNC.toString().equals(model.getType());
        //Get Swagger definition which has URL templates, scopes and resource details
        model.getId().setUuid(model.getUuid());
        if (!isAsyncAPI) {
            // Get from swagger definition
            List<APIOperationsDTO> apiOperationsDTO = null;
            String apiSwaggerDefinition;
            if (model.getSwaggerDefinition() != null) {
                apiSwaggerDefinition = model.getSwaggerDefinition();
            } else {
                apiSwaggerDefinition = apiProvider.getOpenAPIDefinition(model.getUuid(), tenantDomain);
            }
            // We will fetch operations from the swagger definition and not from the AM_API_URL_MAPPING table:
            // table entries may have API level throttling tiers listed in case API level throttling is selected
            // for the API. This will lead the x-throttling-tiers of API definition to get overwritten.
            // (wso2/product-apim#11240)
            apiOperationsDTO = getOperationsFromSwaggerDef(model, apiSwaggerDefinition, preserveCredentials);
            // since the operation details goes missing after fetching operations list from the swagger
            // definition, we have to set them back from the original API model.
            setOperationPoliciesToOperationsDTO(model, apiOperationsDTO, preserveCredentials);
            dto.setOperations(apiOperationsDTO);
            List<ScopeDTO> scopeDTOS = getScopesFromSwagger(apiSwaggerDefinition);
            dto.setScopes(getAPIScopesFromScopeDTOs(scopeDTOS, apiProvider));
        } else {
            // Get from asyncapi definition
            List<APIOperationsDTO> apiOperationsDTO = getOperationsFromAPI(model, preserveCredentials);
            dto.setOperations(apiOperationsDTO);
            String asyncAPIDefinition;
            if (model.getAsyncApiDefinition() != null) {
                asyncAPIDefinition = model.getAsyncApiDefinition();
            } else {
                asyncAPIDefinition = apiProvider
                        .getAsyncAPIDefinition(model.getId().getUUID(), tenantDomain);
            }
            if (asyncAPIDefinition != null) {
                List<ScopeDTO> scopeDTOS = getScopesFromAsyncAPI(asyncAPIDefinition);
                dto.setScopes(getAPIScopesFromScopeDTOs(scopeDTOS, apiProvider));
            }
        }
        Set<String> apiTags = model.getTags();
        List<String> tagsToReturn = new ArrayList<>();
        tagsToReturn.addAll(apiTags);
        dto.setTags(tagsToReturn);
        Set<org.wso2.carbon.apimgt.api.model.Tier> apiTiers = model.getAvailableTiers();
        List<String> tiersToReturn = new ArrayList<>();
        for (org.wso2.carbon.apimgt.api.model.Tier tier : apiTiers) {
            tiersToReturn.add(tier.getName());
        }
        dto.setPolicies(tiersToReturn);
        dto.setApiThrottlingPolicy(model.getApiLevelPolicy());

        if (APIUtil.isOrganizationAccessControlEnabled() && model.getAvailableTiersForOrganizations() != null) {
            Set<OrganizationTiers> organizationAPITiers = model.getAvailableTiersForOrganizations();
            List<OrganizationPoliciesDTO> organizationPolicies = new ArrayList<>();

            for (OrganizationTiers organizationTiers : organizationAPITiers) {
                OrganizationPoliciesDTO organizationPoliciesDTO = getOrganizationPoliciesDTO(organizationTiers);
                organizationPolicies.add(organizationPoliciesDTO);
            }
            dto.setOrganizationPolicies(organizationPolicies);
        }

        //APIs created with type set to "NULL" will be considered as "HTTP"
        if (model.getType() == null || model.getType().toLowerCase().equals("null")) {
            dto.setType(APIDTO.TypeEnum.HTTP);
        } else {
            dto.setType(APIDTO.TypeEnum.fromValue(model.getType()));
        }
        if (!APIConstants.APITransportType.WS.toString().equals(model.getType())) {
            if (StringUtils.isEmpty(model.getTransports())) {
                List<String> transports = new ArrayList<>();
                transports.add(APIConstants.HTTPS_PROTOCOL);

                dto.setTransport(transports);
            } else {
                dto.setTransport(Arrays.asList(model.getTransports().split(",")));
            }
        }
        if (StringUtils.isEmpty(model.getTransports())) {
            dto.setVisibility(APIDTO.VisibilityEnum.PUBLIC);
        }
        if (model.getVisibility() != null) {
            dto.setVisibility(mapVisibilityFromAPItoDTO(model.getVisibility()));
        } else {
            dto.setVisibility(APIDTO.VisibilityEnum.PUBLIC);
        }
        if (model.getVisibleRoles() != null) {
            dto.setVisibleRoles(Arrays.asList(model.getVisibleRoles().split(",")));
        }
        if (model.getVisibleTenants() != null) {
            dto.setVisibleRoles(Arrays.asList(model.getVisibleTenants().split(",")));
        }

        if (model.getVisibleOrganizations() != null && !model.getVisibleOrganizations().isEmpty()) {
            dto.setVisibleOrganizations(Arrays.asList(model.getVisibleOrganizations().split(",")));
        } else {
            dto.setVisibleOrganizations(new ArrayList<>(List.of(APIConstants.VISIBLE_ORG_NONE)));
        }

        if (model.getAdditionalProperties() != null) {
            JSONObject additionalProperties = model.getAdditionalProperties();
            List<APIInfoAdditionalPropertiesDTO> additionalPropertiesList = new ArrayList<>();
            Map<String, APIInfoAdditionalPropertiesMapDTO> additionalPropertiesMap = new HashMap<>();
            for (Object propertyKey : additionalProperties.keySet()) {
                APIInfoAdditionalPropertiesDTO additionalPropertiesDTO = new APIInfoAdditionalPropertiesDTO();
                APIInfoAdditionalPropertiesMapDTO apiInfoAdditionalPropertiesMapDTO =
                        new APIInfoAdditionalPropertiesMapDTO();
                String key = (String) propertyKey;
                int index = key.lastIndexOf(APIConstants.API_RELATED_CUSTOM_PROPERTIES_SURFIX);
                additionalPropertiesDTO.setValue((String) additionalProperties.get(key));
                apiInfoAdditionalPropertiesMapDTO.setValue((String) additionalProperties.get(key));
                if (index > 0) {
                    additionalPropertiesDTO.setName(key.substring(0, index));
                    apiInfoAdditionalPropertiesMapDTO.setName(key.substring(0, index));
                    additionalPropertiesDTO.setDisplay(true);
                } else {
                    additionalPropertiesDTO.setName(key);
                    apiInfoAdditionalPropertiesMapDTO.setName(key);
                    additionalPropertiesDTO.setDisplay(false);
                }
                apiInfoAdditionalPropertiesMapDTO.setDisplay(false);
                additionalPropertiesMap.put(key, apiInfoAdditionalPropertiesMapDTO);
                additionalPropertiesList.add(additionalPropertiesDTO);
            }
            dto.setAdditionalProperties(additionalPropertiesList);
            dto.setAdditionalPropertiesMap(additionalPropertiesMap);
        }

        if (model.getImplementation() != null) {
            dto.setEndpointImplementationType(APIDTO.EndpointImplementationTypeEnum.valueOf(model.getImplementation()));
        }

        dto.setAccessControl(APIConstants.API_RESTRICTED_VISIBILITY.equals(model.getAccessControl()) ?
                APIDTO.AccessControlEnum.RESTRICTED :
                APIDTO.AccessControlEnum.NONE);
        if (model.getAccessControlRoles() != null) {
            dto.setAccessControlRoles(Arrays.asList(model.getAccessControlRoles().split(",")));
        }
        APIBusinessInformationDTO apiBusinessInformationDTO = new APIBusinessInformationDTO();
        apiBusinessInformationDTO.setBusinessOwner(model.getBusinessOwner());
        apiBusinessInformationDTO.setBusinessOwnerEmail(model.getBusinessOwnerEmail());
        apiBusinessInformationDTO.setTechnicalOwner(model.getTechnicalOwner());
        apiBusinessInformationDTO.setTechnicalOwnerEmail(model.getTechnicalOwnerEmail());
        dto.setBusinessInformation(apiBusinessInformationDTO);
        APICorsConfigurationDTO apiCorsConfigurationDTO = new APICorsConfigurationDTO();
        CORSConfiguration corsConfiguration = model.getCorsConfiguration();
        if (corsConfiguration == null) {
            corsConfiguration = APIUtil.getDefaultCorsConfiguration();
        }
        List<String> accessControlAllowOrigins = corsConfiguration.getAccessControlAllowOrigins();
        List<String> accessControlAllowHeaders = corsConfiguration.getAccessControlAllowHeaders();
        List<String> accessControlAllowMethods = corsConfiguration.getAccessControlAllowMethods();
        if (accessControlAllowOrigins == null) {
            apiCorsConfigurationDTO.setAccessControlAllowOrigins(Collections.emptyList());
        } else {
            apiCorsConfigurationDTO.setAccessControlAllowOrigins(accessControlAllowOrigins);
        }
        if (accessControlAllowHeaders == null) {
            apiCorsConfigurationDTO.setAccessControlAllowHeaders(Collections.emptyList());
        } else {
            apiCorsConfigurationDTO.setAccessControlAllowHeaders(accessControlAllowHeaders);
        }
        if (accessControlAllowMethods == null) {
            apiCorsConfigurationDTO.setAccessControlAllowMethods(Collections.emptyList());
        } else {
            apiCorsConfigurationDTO.setAccessControlAllowMethods(accessControlAllowMethods);
        }
        apiCorsConfigurationDTO.setCorsConfigurationEnabled(corsConfiguration.isCorsConfigurationEnabled());
        apiCorsConfigurationDTO.setAccessControlAllowCredentials(corsConfiguration.isAccessControlAllowCredentials());
        dto.setCorsConfiguration(apiCorsConfigurationDTO);

        WebsubSubscriptionConfigurationDTO websubSubscriptionConfigurationDTO
                = new WebsubSubscriptionConfigurationDTO();
        WebsubSubscriptionConfiguration websubSubscriptionConfiguration = model.getWebsubSubscriptionConfiguration();
        if (websubSubscriptionConfiguration == null) {
            websubSubscriptionConfiguration = APIUtil.getDefaultWebsubSubscriptionConfiguration();
        }
        websubSubscriptionConfigurationDTO.setEnable(websubSubscriptionConfiguration.isEnable());
        websubSubscriptionConfigurationDTO.setSecret(websubSubscriptionConfiguration.getSecret());
        websubSubscriptionConfigurationDTO.setSigningAlgorithm(websubSubscriptionConfiguration.getSigningAlgorithm());
        websubSubscriptionConfigurationDTO.setSignatureHeader(websubSubscriptionConfiguration.getSignatureHeader());
        dto.setWebsubSubscriptionConfiguration(websubSubscriptionConfigurationDTO);

        if (model.getWsdlUrl() != null) {
            WSDLInfoDTO wsdlInfoDTO = getWsdlInfoDTO(model);
            dto.setWsdlInfo(wsdlInfoDTO);
        }
        dto.setWsdlUrl(model.getWsdlUrl());
        setEndpointSecurityFromModel(model, ()
                -> (Map) dto.getEndpointConfig(), dto::setEndpointConfig, preserveCredentials);
        setMaxTpsFromModelToApiDTO(model, dto);

        dto.setAuthorizationHeader(model.getAuthorizationHeader());
        dto.setApiKeyHeader(model.getApiKeyHeader());
        if (model.getApiSecurity() != null) {
            dto.setSecurityScheme(Arrays.asList(model.getApiSecurity().split(",")));
        }
        if (null != model.getLastUpdated()) {
            Date lastUpdateDate = model.getLastUpdated();
            Timestamp timeStamp = new Timestamp(lastUpdateDate.getTime());
            dto.setLastUpdatedTime(String.valueOf(timeStamp));
            dto.setLastUpdatedTimestamp(String.valueOf(timeStamp.getTime()));
        }
        if (null != model.getCreatedTime()) {
            Date created = new Date(Long.parseLong(model.getCreatedTime()));
            Timestamp timeStamp = new Timestamp(created.getTime());
            dto.setCreatedTime(String.valueOf(timeStamp.getTime()));
        }
        dto.setWorkflowStatus(model.getWorkflowStatus());

        List<APICategory> apiCategories = model.getApiCategories();
        List<String> categoryNameList = new ArrayList<>();
        if (apiCategories != null && !apiCategories.isEmpty()) {
            for (APICategory category : apiCategories) {
                categoryNameList.add(category.getName());
            }
        }
        dto.setCategories(categoryNameList);
        dto.setKeyManagers(model.getKeyManagers());

        if (model.getAudience() != null) {
            dto.setAudience(AudienceEnum.valueOf(model.getAudience()));
        }

        if (model.getAudiences() != null) {
            Set<String> audiences = model.getAudiences();
            if (audiences.contains(APIConstants.ALL_AUDIENCES) && audiences.size() > 1) {
                audiences.remove(APIConstants.ALL_AUDIENCES);
            }
            dto.setAudiences(new ArrayList<>(model.getAudiences()));
        }

        String gatewayVendor = StringUtils.toRootLowerCase(model.getGatewayVendor());
        dto.setGatewayVendor(gatewayVendor);
        dto.setGatewayType(model.getGatewayType());
        dto.setEgress(model.isEgress() == 1); //true - 1, false - 0

        if (model.getGatewayVendor() == null) {
            dto.setGatewayVendor(APIConstants.WSO2_GATEWAY_ENVIRONMENT);
        }

        if (model.getAsyncTransportProtocols() != null) {
            dto.setAsyncTransportProtocols(Arrays.asList(model.getAsyncTransportProtocols().split(",")));
        }
        SubtypeConfigurationDTO subtypeConfigurationDTO = new SubtypeConfigurationDTO();
        subtypeConfigurationDTO.setSubtype(model.getSubtype());
        if (model.getAiConfiguration() != null) {
            subtypeConfigurationDTO.setSubtype(model.getSubtype());
            subtypeConfigurationDTO.setConfiguration(new Gson().toJson(model.getAiConfiguration()));
            dto.setSubtypeConfiguration(subtypeConfigurationDTO);
        }
        dto.setSubtypeConfiguration(subtypeConfigurationDTO);

        // Set primary endpoints
        dto.setPrimaryProductionEndpointId(model.getPrimaryProductionEndpointId());
        dto.setPrimarySandboxEndpointId(model.getPrimarySandboxEndpointId());
        dto.setInitiatedFromGateway(model.isInitiatedFromGateway());
        return dto;
    }

    /**
     * Converts an API model to a MCPServerDTO.
     *
     * @param model API model to be converted
     * @return MCPServerDTO representation of the API model
     * @throws APIManagementException if an error occurs during conversion
     */
    public static MCPServerDTO fromAPItoMCPServerDTO(API model) throws APIManagementException {

        return fromAPItoMCPServerDTO(model, false, null);
    }

    /**
     * Converts an API model to a MCPServerDTO.
     *
     * @param model API model to be converted
     * @return MCPServerDTO representation of the API model
     * @throws APIManagementException if an error occurs during conversion
     */
    public static MCPServerDTO fromAPItoMCPServerDTO(API model, APIProvider apiProvider)
            throws APIManagementException {

        return fromAPItoMCPServerDTO(model, false, apiProvider);
    }

    /**
     * Converts an API model to a MCPServerDTO.
     *
     * @param model               API model to be converted
     * @param preserveCredentials whether to preserve credentials in the endpoint config
     * @param apiProviderParam    APIProvider instance, if null, the logged-in user provider will be used
     * @return MCPServerDTO representation of the API model
     * @throws APIManagementException if an error occurs during conversion
     */
    public static MCPServerDTO fromAPItoMCPServerDTO(API model, boolean preserveCredentials,
                                                     APIProvider apiProviderParam)
            throws APIManagementException {

        APIProvider apiProvider;
        if (apiProviderParam != null) {
            apiProvider = apiProviderParam;
        } else {
            apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        }
        MCPServerDTO dto = new MCPServerDTO();
        dto.setName(model.getId().getApiName());
        dto.setDisplayName(model.getDisplayName() != null ? model.getDisplayName() : model.getId().getApiName());
        dto.setVersion(model.getId().getVersion());
        String providerName = model.getId().getProviderName();
        dto.setProvider(APIUtil.replaceEmailDomainBack(providerName));
        dto.setId(model.getUUID());
        String context = model.getContextTemplate();
        if (context.endsWith(FORWARD_SLASH + RestApiConstants.API_VERSION_PARAM)) {
            context = context.replace(FORWARD_SLASH + RestApiConstants.API_VERSION_PARAM, EMPTY_STRING);
        }
        dto.setContext(context);
        dto.setCreatedTime(model.getCreatedTime());
        if (model.getLastUpdated() != null) {
            dto.setLastUpdatedTime(Long.toString(model.getLastUpdated().getTime()));
        }
        dto.setDescription(model.getDescription());
        dto.setIsDefaultVersion(model.isDefaultVersion());
        dto.setIsRevision(model.isRevision());
        dto.setRevisionedMCPServerId(model.getRevisionedApiId());
        dto.setRevisionId(model.getRevisionId());
        dto.setEnableSchemaValidation(model.isEnabledSchemaValidation());
        String endpointConfig = model.getEndpointConfig();
        if (!StringUtils.isBlank(endpointConfig)) {
            try {
                JSONParser parser = new JSONParser();
                JSONObject endpointConfigJson = (JSONObject) parser.parse(endpointConfig);
                // AWS Lambda: set secret key based on preserveCredentials
                if (APIConstants.ENDPOINT_TYPE_AWSLAMBDA.equalsIgnoreCase(
                        (String) endpointConfigJson.get(APIConstants.API_ENDPOINT_CONFIG_PROTOCOL_TYPE))) {
                    handleAWSCredentials(endpointConfigJson, preserveCredentials);
                }
                CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
                if (endpointConfigJson.get(APIConstants.ENDPOINT_SECURITY) != null) {
                    JSONObject endpointSecurity = (JSONObject) endpointConfigJson.get(APIConstants.ENDPOINT_SECURITY);
                    if (endpointSecurity.get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_PRODUCTION) != null) {
                        JSONObject productionEndpointSecurity = (JSONObject) endpointSecurity
                                .get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_PRODUCTION);
                        String productionEndpointType = (String) productionEndpointSecurity
                                .get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_TYPE);
                        if (productionEndpointSecurity
                                .get(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS) != null) {
                            String customParametersString = (String) productionEndpointSecurity
                                    .get(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS);
                            JSONObject customParameters = (JSONObject) parser.parse(customParametersString);
                            decryptCustomOauthParameters(customParameters, cryptoUtil);
                            productionEndpointSecurity.put(
                                    APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS, customParameters);
                        }

                        if (APIConstants.OAuthConstants.OAUTH.equals(productionEndpointType)) {
                            String clientSecret = (String) productionEndpointSecurity
                                    .get(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET);
                            if (StringUtils.isNotEmpty(clientSecret)) {
                                productionEndpointSecurity.put(APIConstants
                                                .OAuthConstants.OAUTH_CLIENT_SECRET,
                                        new String(cryptoUtil.base64DecodeAndDecrypt(clientSecret)));
                            }
                        }
                        if (APIConstants.ENDPOINT_SECURITY_TYPE_API_KEY.equals(productionEndpointType)) {
                            String apiKeyValue = (String) productionEndpointSecurity
                                    .get(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE);
                            if (StringUtils.isNotEmpty(apiKeyValue)) {
                                productionEndpointSecurity.put(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE,
                                        new String(cryptoUtil.base64DecodeAndDecrypt(apiKeyValue)));
                            }
                        }
                        endpointSecurity.put(APIConstants.OAuthConstants.ENDPOINT_SECURITY_PRODUCTION,
                                productionEndpointSecurity);
                        endpointConfigJson.put(APIConstants.ENDPOINT_SECURITY, endpointSecurity);
                    }
                    if (endpointSecurity.get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_SANDBOX) != null) {
                        JSONObject sandboxEndpointSecurity = (JSONObject) endpointSecurity
                                .get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_SANDBOX);
                        String sandboxEndpointType = (String) sandboxEndpointSecurity
                                .get(APIConstants.OAuthConstants.ENDPOINT_SECURITY_TYPE);
                        if (sandboxEndpointSecurity
                                .get(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS) != null) {
                            String customParametersString = (String) sandboxEndpointSecurity
                                    .get(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS);
                            JSONObject customParameters = (JSONObject) parser.parse(customParametersString);
                            decryptCustomOauthParameters(customParameters, cryptoUtil);
                            sandboxEndpointSecurity.put(
                                    APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS, customParameters);
                        }
                        if (APIConstants.OAuthConstants.OAUTH.equals(sandboxEndpointType)) {
                            String clientSecret = (String) sandboxEndpointSecurity
                                    .get(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET);
                            if (StringUtils.isNotEmpty(clientSecret)) {
                                sandboxEndpointSecurity.put(APIConstants
                                                .OAuthConstants.OAUTH_CLIENT_SECRET,
                                        new String(cryptoUtil.base64DecodeAndDecrypt(clientSecret)));
                            }
                        }
                        if (APIConstants.ENDPOINT_SECURITY_TYPE_API_KEY.equals(sandboxEndpointType)) {
                            String apiKeyValue = (String) sandboxEndpointSecurity
                                    .get(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE);
                            if (StringUtils.isNotEmpty(apiKeyValue)) {
                                sandboxEndpointSecurity.put(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE,
                                        new String(cryptoUtil.base64DecodeAndDecrypt(apiKeyValue)));
                            }
                        }
                        endpointSecurity.put(APIConstants.OAuthConstants.ENDPOINT_SECURITY_SANDBOX,
                                sandboxEndpointSecurity);
                        endpointConfigJson.put(APIConstants.ENDPOINT_SECURITY, endpointSecurity);
                    }
                    JSONObject jsonObject = handleEndpointSecurity(model,
                            (JSONObject) endpointConfigJson.get(APIConstants.ENDPOINT_SECURITY), preserveCredentials);
                    endpointConfigJson.put(APIConstants.ENDPOINT_SECURITY, jsonObject);
                }
                dto.setEndpointConfig(endpointConfigJson);
            } catch (ParseException e) {
                //logs the error and continues as this is not a blocker
                log.error("Cannot convert endpoint configurations when setting endpoint for API. " +
                        "API ID = " + model.getId(), e);
            } catch (CryptoException e) {
                log.error("Error while decrypting client credentials or secret parameters for API: "
                        + model.getId(), e);
            }
        }
        dto.setHasThumbnail(!StringUtils.isBlank(model.getThumbnailUrl()));
        dto.setLifeCycleStatus(model.getStatus());
        if (model.getApiPolicies() != null) {
            dto.setMcpServerPolicies(
                    OperationPolicyMappingUtil.fromMCPServerOperationPolicyListToDTO(model.getApiPolicies()));
        }
        String subscriptionAvailability = model.getSubscriptionAvailability();
        if (subscriptionAvailability != null) {
            dto.setSubscriptionAvailability(mapSubscriptionAvailabilityFromMCPServerToDTO(subscriptionAvailability));
        }
        if (model.getSubscriptionAvailableTenants() != null) {
            dto.setSubscriptionAvailableTenants(Arrays.asList(model.getSubscriptionAvailableTenants().split(",")));
        }
        String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(model.getId()
                .getProviderName()));
        model.getId().setUuid(model.getUuid());
        List<MCPServerOperationDTO> mcpServerOperationDTOList = null;
        String apiSwaggerDefinition;
        if (model.getSwaggerDefinition() != null) {
            apiSwaggerDefinition = model.getSwaggerDefinition();
        } else {
            apiSwaggerDefinition = apiProvider.getOpenAPIDefinition(model.getUuid(), tenantDomain);
        }
        mcpServerOperationDTOList = getMCPServerOperationsFromAPI(model);
        dto.setOperations(mcpServerOperationDTOList);
        List<ScopeDTO> scopeDTOS = getScopesFromSwagger(apiSwaggerDefinition);
        dto.setScopes(getMCPServerScopesFromScopeDTOs(scopeDTOS, apiProvider));
        Set<String> apiTags = model.getTags();
        List<String> tagsToReturn = new ArrayList<>();
        tagsToReturn.addAll(apiTags);
        dto.setTags(tagsToReturn);
        Set<org.wso2.carbon.apimgt.api.model.Tier> apiTiers = model.getAvailableTiers();
        List<String> tiersToReturn = new ArrayList<>();
        for (org.wso2.carbon.apimgt.api.model.Tier tier : apiTiers) {
            tiersToReturn.add(tier.getName());
        }
        dto.setPolicies(tiersToReturn);
        dto.setThrottlingPolicy(model.getApiLevelPolicy());

        if (APIUtil.isOrganizationAccessControlEnabled() && model.getAvailableTiersForOrganizations() != null) {
            Set<OrganizationTiers> organizationAPITiers = model.getAvailableTiersForOrganizations();
            List<OrganizationPoliciesDTO> organizationPolicies = new ArrayList<>();

            for (OrganizationTiers organizationTiers : organizationAPITiers) {
                OrganizationPoliciesDTO organizationPoliciesDTO = getOrganizationPoliciesDTO(organizationTiers);
                organizationPolicies.add(organizationPoliciesDTO);
            }
            dto.setOrganizationPolicies(organizationPolicies);
        }
        if (!APIConstants.APITransportType.WS.toString().equals(model.getType())) {
            if (StringUtils.isEmpty(model.getTransports())) {
                List<String> transports = new ArrayList<>();
                transports.add(APIConstants.HTTPS_PROTOCOL);

                dto.setTransport(transports);
            }
            dto.setTransport(Arrays.asList(model.getTransports().split(",")));
        }
        if (StringUtils.isEmpty(model.getTransports())) {
            dto.setVisibility(MCPServerDTO.VisibilityEnum.PUBLIC);
        }
        dto.setVisibility(mapVisibilityFromMCPServerToDTO(model.getVisibility()));
        if (model.getVisibleRoles() != null) {
            dto.setVisibleRoles(Arrays.asList(model.getVisibleRoles().split(",")));
        }
        if (model.getVisibleTenants() != null) {
            dto.setVisibleRoles(Arrays.asList(model.getVisibleTenants().split(",")));
        }

        if (model.getVisibleOrganizations() != null && !model.getVisibleOrganizations().isEmpty()) {
            dto.setVisibleOrganizations(Arrays.asList(model.getVisibleOrganizations().split(",")));
        } else {
            dto.setVisibleOrganizations(new ArrayList<>(List.of(APIConstants.VISIBLE_ORG_NONE)));
        }

        if (model.getAdditionalProperties() != null) {
            JSONObject additionalProperties = model.getAdditionalProperties();
            List<APIInfoAdditionalPropertiesDTO> additionalPropertiesList = new ArrayList<>();
            Map<String, APIInfoAdditionalPropertiesMapDTO> additionalPropertiesMap = new HashMap<>();
            for (Object propertyKey : additionalProperties.keySet()) {
                APIInfoAdditionalPropertiesDTO additionalPropertiesDTO = new APIInfoAdditionalPropertiesDTO();
                APIInfoAdditionalPropertiesMapDTO apiInfoAdditionalPropertiesMapDTO =
                        new APIInfoAdditionalPropertiesMapDTO();
                String key = (String) propertyKey;
                int index = key.lastIndexOf(APIConstants.API_RELATED_CUSTOM_PROPERTIES_SURFIX);
                additionalPropertiesDTO.setValue((String) additionalProperties.get(key));
                apiInfoAdditionalPropertiesMapDTO.setValue((String) additionalProperties.get(key));
                if (index > 0) {
                    additionalPropertiesDTO.setName(key.substring(0, index));
                    apiInfoAdditionalPropertiesMapDTO.setName(key.substring(0, index));
                    additionalPropertiesDTO.setDisplay(true);
                } else {
                    additionalPropertiesDTO.setName(key);
                    apiInfoAdditionalPropertiesMapDTO.setName(key);
                    additionalPropertiesDTO.setDisplay(false);
                }
                apiInfoAdditionalPropertiesMapDTO.setDisplay(false);
                additionalPropertiesMap.put(key, apiInfoAdditionalPropertiesMapDTO);
                additionalPropertiesList.add(additionalPropertiesDTO);
            }
            dto.setAdditionalPropertiesMap(additionalPropertiesMap);
        }

        dto.setAccessControl(APIConstants.API_RESTRICTED_VISIBILITY.equals(model.getAccessControl()) ?
                MCPServerDTO.AccessControlEnum.RESTRICTED :
                MCPServerDTO.AccessControlEnum.NONE);
        if (model.getAccessControlRoles() != null) {
            dto.setAccessControlRoles(Arrays.asList(model.getAccessControlRoles().split(",")));
        }
        APIBusinessInformationDTO apiBusinessInformationDTO = new APIBusinessInformationDTO();
        apiBusinessInformationDTO.setBusinessOwner(model.getBusinessOwner());
        apiBusinessInformationDTO.setBusinessOwnerEmail(model.getBusinessOwnerEmail());
        apiBusinessInformationDTO.setTechnicalOwner(model.getTechnicalOwner());
        apiBusinessInformationDTO.setTechnicalOwnerEmail(model.getTechnicalOwnerEmail());
        dto.setBusinessInformation(apiBusinessInformationDTO);
        APICorsConfigurationDTO apiCorsConfigurationDTO = new APICorsConfigurationDTO();
        CORSConfiguration corsConfiguration = model.getCorsConfiguration();
        if (corsConfiguration == null) {
            corsConfiguration = APIUtil.getDefaultCorsConfiguration();
        }
        List<String> accessControlAllowOrigins = corsConfiguration.getAccessControlAllowOrigins();
        List<String> accessControlAllowHeaders = corsConfiguration.getAccessControlAllowHeaders();
        List<String> accessControlAllowMethods = corsConfiguration.getAccessControlAllowMethods();
        if (accessControlAllowOrigins == null) {
            apiCorsConfigurationDTO.setAccessControlAllowOrigins(Collections.emptyList());
        } else {
            apiCorsConfigurationDTO.setAccessControlAllowOrigins(accessControlAllowOrigins);
        }
        if (accessControlAllowHeaders == null) {
            apiCorsConfigurationDTO.setAccessControlAllowHeaders(Collections.emptyList());
        } else {
            apiCorsConfigurationDTO.setAccessControlAllowHeaders(accessControlAllowHeaders);
        }
        if (accessControlAllowMethods == null) {
            apiCorsConfigurationDTO.setAccessControlAllowMethods(Collections.emptyList());
        } else {
            apiCorsConfigurationDTO.setAccessControlAllowMethods(accessControlAllowMethods);
        }
        apiCorsConfigurationDTO.setCorsConfigurationEnabled(corsConfiguration.isCorsConfigurationEnabled());
        apiCorsConfigurationDTO.setAccessControlAllowCredentials(corsConfiguration.isAccessControlAllowCredentials());
        dto.setCorsConfiguration(apiCorsConfigurationDTO);

        setEndpointSecurityFromModel(model, ()
                -> (Map) dto.getEndpointConfig(), dto::setEndpointConfig, preserveCredentials);

        setMaxTpsFromModelToMcpServerDTO(model, dto);

        dto.setAuthorizationHeader(model.getAuthorizationHeader());
        dto.setApiKeyHeader(model.getApiKeyHeader());
        if (model.getApiSecurity() != null) {
            dto.setSecurityScheme(Arrays.asList(model.getApiSecurity().split(",")));
        }
        if (null != model.getLastUpdated()) {
            Date lastUpdateDate = model.getLastUpdated();
            Timestamp timeStamp = new Timestamp(lastUpdateDate.getTime());
            dto.setLastUpdatedTime(String.valueOf(timeStamp));
            dto.setLastUpdatedTimestamp(String.valueOf(timeStamp.getTime()));
        }
        if (null != model.getCreatedTime()) {
            Date created = new Date(Long.parseLong(model.getCreatedTime()));
            Timestamp timeStamp = new Timestamp(created.getTime());
            dto.setCreatedTime(String.valueOf(timeStamp.getTime()));
        }
        dto.setWorkflowStatus(model.getWorkflowStatus());

        List<APICategory> apiCategories = model.getApiCategories();
        List<String> categoryNameList = new ArrayList<>();
        if (apiCategories != null && !apiCategories.isEmpty()) {
            for (APICategory category : apiCategories) {
                categoryNameList.add(category.getName());
            }
        }
        dto.setCategories(categoryNameList);
        dto.setKeyManagers(model.getKeyManagers());

        if (model.getAudiences() != null) {
            Set<String> audiences = model.getAudiences();
            if (audiences.contains(APIConstants.ALL_AUDIENCES) && audiences.size() > 1) {
                audiences.remove(APIConstants.ALL_AUDIENCES);
            }
            dto.setAudiences(new ArrayList<>(model.getAudiences()));
        }

        String gatewayVendor = StringUtils.toRootLowerCase(model.getGatewayVendor());
        dto.setGatewayVendor(gatewayVendor);
        dto.setGatewayType(model.getGatewayType());
        if (model.getGatewayVendor() == null) {
            dto.setGatewayVendor(APIConstants.WSO2_GATEWAY_ENVIRONMENT);
        }

        SubtypeConfigurationDTO subtypeConfigurationDTO = new SubtypeConfigurationDTO();
        subtypeConfigurationDTO.setSubtype(model.getSubtype());
        dto.setSubtypeConfiguration(subtypeConfigurationDTO);
        dto.setInitiatedFromGateway(model.isInitiatedFromGateway());
        String protocolVersion = model.getMetadata() != null
                ? model.getMetadata().get(APIConstants.MCP.PROTOCOL_VERSION_KEY) : null;
        if (protocolVersion != null) {
            dto.setProtocolVersion(protocolVersion);
        }
        return dto;
    }

    private static List<ScopeDTO> getScopesFromAsyncAPI(String asyncAPIDefinition) throws APIManagementException {

        String asyncApiVersion = AsyncApiParserUtil.getAsyncApiVersion(asyncAPIDefinition);
        AbstractAsyncApiParser asyncApiParser;
        AsyncApiParseOptions options =  PublisherCommonUtils.getParserOptionsFromConfig();
        try {
            asyncApiParser = AsyncApiParserFactory.getAsyncApiParser(asyncApiVersion, options);
        } catch (IllegalArgumentException | UnsupportedOperationException e) {
            throw new APIManagementException("Unsupported AsyncAPI version: " + asyncApiVersion, e);
        }
        Set<Scope> scopes = asyncApiParser.getScopes(asyncAPIDefinition);
        if (scopes == null || scopes.isEmpty()) {
            return Collections.emptyList();
        }
        List<ScopeDTO> scopeDTOS = new ArrayList<>();
        for (Scope aScope : scopes) {
            ScopeDTO scopeDTO = new ScopeDTO();
            scopeDTO.setName(aScope.getKey());
            scopeDTO.setDisplayName(aScope.getName());
            scopeDTO.setDescription(aScope.getDescription());
            String roles = aScope.getRoles();
            if (roles == null || roles.isEmpty()) {
                scopeDTO.setBindings(Collections.emptyList());
            } else {
                scopeDTO.setBindings(Arrays.asList((roles).split(",")));
            }
            scopeDTOS.add(scopeDTO);
        }
        return scopeDTOS;
    }

    /**
     * Retrieves the WSDL info from the API model.
     *
     * @param model API
     * @return WSDL info
     */
    public static WSDLInfoDTO getWsdlInfoDTO(API model) {

        if (model.getWsdlUrl() == null) {
            return null;
        }
        String wsdlRegistryUri = model.getWsdlUrl().toLowerCase();
        WSDLInfoDTO wsdlInfoDTO = new WSDLInfoDTO();
        if (wsdlRegistryUri.endsWith(APIConstants.ZIP_FILE_EXTENSION)) {
            wsdlInfoDTO.setType(WSDLInfoDTO.TypeEnum.ZIP);
        } else if (wsdlRegistryUri.endsWith(APIConstants.WSDL_EXTENSION)) {
            wsdlInfoDTO.setType(WSDLInfoDTO.TypeEnum.WSDL);
        } else {
            log.warn("Unrecognized WSDL type in WSDL url: " + model.getWsdlUrl());
        }
        return wsdlInfoDTO;
    }

    /**
     * Maps a visibility string from the internal API model to the corresponding {@link APIDTO.VisibilityEnum}.
     *
     * @param visibility the internal visibility string
     * @return the matching {@link APIDTO.VisibilityEnum}, or null if no match is found
     */
    private static APIDTO.VisibilityEnum mapVisibilityFromAPItoDTO(String visibility) {

        String key = mapVisibilityFromAPItoKey(visibility);
        if (key == null) {
            return null;
        }
        return APIDTO.VisibilityEnum.valueOf(key);
    }

    /**
     * Maps a visibility string from the internal API model to the corresponding {@link MCPServerDTO.VisibilityEnum}.
     *
     * @param visibility the internal visibility string
     * @return the matching {@link MCPServerDTO.VisibilityEnum}, or null if no match is found
     */
    private static MCPServerDTO.VisibilityEnum mapVisibilityFromMCPServerToDTO(String visibility) {

        String key = mapVisibilityFromAPItoKey(visibility);
        if (key == null) {
            return null;
        }
        return MCPServerDTO.VisibilityEnum.valueOf(key);
    }

    /**
     * Maps a visibility string from the internal API model to the corresponding key used in DTO enums.
     *
     * @param visibility the internal visibility string
     * @return the corresponding key as a string, or null if the value is unrecognized
     */
    private static String mapVisibilityFromAPItoKey(String visibility) {

        switch (visibility) {
            case APIConstants.API_GLOBAL_VISIBILITY:
                return APIConstants.PUBLIC;
            case APIConstants.API_PRIVATE_VISIBILITY:
                return APIConstants.PRIVATE;
            case APIConstants.API_RESTRICTED_VISIBILITY:
                return APIConstants.RESTRICTED;
            default:
                return null;
        }
    }

    /**
     * Maps a subscription availability string from the internal API model to the corresponding
     * {@link APIDTO.SubscriptionAvailabilityEnum}.
     *
     * @param subscriptionAvailability the internal subscription availability string
     * @return the matching {@link APIDTO.SubscriptionAvailabilityEnum}, or null if no match is found
     */
    private static APIDTO.SubscriptionAvailabilityEnum mapSubscriptionAvailabilityFromAPItoDTO(
            String subscriptionAvailability) {

        String key = mapSubscriptionAvailabilityToKey(subscriptionAvailability);
        if (key == null) {
            return null;
        }
        return APIDTO.SubscriptionAvailabilityEnum.valueOf(key);
    }

    /**
     * Maps a subscription availability string from the internal API model to the corresponding
     * {@link MCPServerDTO.SubscriptionAvailabilityEnum}.
     *
     * @param subscriptionAvailability the internal subscription availability string
     * @return the matching {@link MCPServerDTO.SubscriptionAvailabilityEnum}, or null if no match is found
     */
    private static MCPServerDTO.SubscriptionAvailabilityEnum mapSubscriptionAvailabilityFromMCPServerToDTO(
            String subscriptionAvailability) {

        String key = mapSubscriptionAvailabilityToKey(subscriptionAvailability);
        if (key == null) {
            return null;
        }
        return MCPServerDTO.SubscriptionAvailabilityEnum.valueOf(key);
    }

    /**
     * Converts the internal subscription availability string to its corresponding key used in DTO enums.
     *
     * @param subscriptionAvailability the internal subscription availability string
     * @return the corresponding key as a string, or null if the value is unrecognized
     */
    private static String mapSubscriptionAvailabilityToKey(String subscriptionAvailability) {

        switch (subscriptionAvailability) {
            case APIConstants.SUBSCRIPTION_TO_CURRENT_TENANT:
                return APIConstants.CURRENT_TENANT;
            case APIConstants.SUBSCRIPTION_TO_ALL_TENANTS:
                return APIConstants.ALL_TENANTS;
            case APIConstants.SUBSCRIPTION_TO_SPECIFIC_TENANTS:
                return APIConstants.SPECIFIC_TENANTS;
            default:
                return null;
        }
    }

    /**
     * Sets the endpoint security configurations from the API model to any DTO with endpoint config accessors.
     *
     * @param api                 API model containing endpoint security information
     * @param getEndpointConfig   Supplier to get the current endpoint configuration map
     * @param setEndpointConfig   Consumer to set the updated endpoint configuration map
     * @param preserveCredentials whether to preserve credentials in the endpoint config
     * @throws APIManagementException if an error occurs while setting endpoint security
     */
    public static void setEndpointSecurityFromModel(API api,
                                                    Supplier<Map> getEndpointConfig,
                                                    Consumer<Map> setEndpointConfig,
                                                    boolean preserveCredentials) throws APIManagementException {

        if (!api.isEndpointSecured()) {
            return;
        }

        JSONObject endpointSecurityObject = new JSONObject();
        endpointSecurityObject.put(APIConstants.ENDPOINT_SECURITY_ENABLED, true);
        endpointSecurityObject.put(APIConstants.ENDPOINT_SECURITY_TYPE,
                APIConstants.ENDPOINT_SECURITY_TYPE_BASIC.toUpperCase());
        endpointSecurityObject.put(APIConstants.ENDPOINT_SECURITY_USERNAME, api.getEndpointUTUsername());

        String tenantDomain =
                MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(api.getId().getProviderName()));
        if (checkEndpointSecurityPasswordEnabled(tenantDomain) || preserveCredentials) {
            endpointSecurityObject.put(APIConstants.ENDPOINT_SECURITY_PASSWORD, api.getEndpointUTPassword());
        } else {
            endpointSecurityObject.put(APIConstants.ENDPOINT_SECURITY_PASSWORD, "");
        }

        if (api.isEndpointAuthDigest()) {
            endpointSecurityObject.put(APIConstants.ENDPOINT_SECURITY_TYPE,
                    APIConstants.ENDPOINT_SECURITY_TYPE_DIGEST.toUpperCase());
        }

        JSONObject endpointSecurityModel = new JSONObject();
        endpointSecurityModel.put(APIConstants.ENDPOINT_SECURITY_PRODUCTION, endpointSecurityObject);
        endpointSecurityModel.put(APIConstants.ENDPOINT_SECURITY_SANDBOX, endpointSecurityObject);

        Map endpointConfig = getEndpointConfig.get();
        if (!endpointConfig.containsKey(APIConstants.ENDPOINT_SECURITY)) {
            endpointConfig.put(APIConstants.ENDPOINT_SECURITY, endpointSecurityModel);
            setEndpointConfig.accept(endpointConfig);
        }
    }

    /**
     * This method used to check whether the config for exposing endpoint security password when getting API is enabled
     * or not in tenant-config.json in registry.
     *
     * @return boolean as config enabled or not
     * @throws APIManagementException
     */
    private static boolean checkEndpointSecurityPasswordEnabled(String tenantDomainName) throws APIManagementException {

        JSONObject apiTenantConfig;
        apiTenantConfig = APIUtil.getTenantConfig(tenantDomainName);
        if (apiTenantConfig != null) {
            Object value = apiTenantConfig.get(APIConstants.API_TENANT_CONF_EXPOSE_ENDPOINT_PASSWORD);
            if (value != null) {
                return Boolean.parseBoolean(value.toString());
            }
        }
        return false;
    }

    /**
     * Populates the Max TPS configuration in an {@link APIDTO} from the given {@link API} model.
     * Preserves the same logic and behavior as the previous implementation, including
     * handling of token-based throttling configuration if present.
     *
     * @param api    the API model containing Max TPS values
     * @param apiDto the APIDTO to populate
     */
    private static void setMaxTpsFromModelToApiDTO(API api, APIDTO apiDto) {

        APIMaxTpsDTO built = buildApiMaxTpsDtoFromModel(api);
        if (built != null) {
            apiDto.setMaxTps(built);
        }
    }

    /**
     * Populates the Max TPS configuration in an {@link MCPServerDTO} from the given {@link API} model.
     * Uses the common builder and converts the {@link APIMaxTpsDTO} to {@link MaxTpsDTO}
     * since MCPServerDTO does not support token-based throttling.
     *
     * @param api          the API model containing Max TPS values
     * @param mcpServerDto the MCPServerDTO to populate
     */
    private static void setMaxTpsFromModelToMcpServerDTO(API api, MCPServerDTO mcpServerDto) {

        APIMaxTpsDTO built = buildApiMaxTpsDtoFromModel(api);
        if (built != null) {
            mcpServerDto.setMaxTps(toMaxTpsDTO(built));
        }
    }

    /**
     * Builds an {@link APIMaxTpsDTO} from the given {@link API} model.
     * Reads production/sandbox TPS values, their time units, and optionally the
     * token-based throttling configuration from the model. If both TPS values are blank
     * or a number conversion error occurs, returns {@code null}.
     *
     * @param api the API model to read from
     * @return a populated {@link APIMaxTpsDTO} or {@code null} if no values found
     */
    private static APIMaxTpsDTO buildApiMaxTpsDtoFromModel(API api) {

        if (api == null || (StringUtils.isBlank(api.getProductionMaxTps())
                && StringUtils.isBlank(api.getSandboxMaxTps()))) {
            return null;
        }
        APIMaxTpsDTO maxTpsDTO = new APIMaxTpsDTO();
        try {
            if (StringUtils.isNotBlank(api.getProductionMaxTps())) {
                maxTpsDTO.setProduction(Long.parseLong(api.getProductionMaxTps()));
            }
            if (StringUtils.isNotBlank(api.getSandboxMaxTps())) {
                maxTpsDTO.setSandbox(Long.parseLong(api.getSandboxMaxTps()));
            }
            if (StringUtils.isNotBlank(api.getProductionTimeUnit())) {
                maxTpsDTO.setProductionTimeUnit(
                        APIMaxTpsDTO.ProductionTimeUnitEnum.valueOf(
                                convertFromMilliseconds(api.getProductionTimeUnit())));
            }
            if (StringUtils.isNotBlank(api.getSandboxTimeUnit())) {
                maxTpsDTO.setSandboxTimeUnit(
                        APIMaxTpsDTO.SandboxTimeUnitEnum.valueOf(convertFromMilliseconds(api.getSandboxTimeUnit())));
            }
            BackendThrottlingConfiguration backendConfig = api.getBackendThrottlingConfiguration();
            if (backendConfig != null
                    && backendConfig.getTokenBasedThrottlingConfiguration() != null
                    && backendConfig.getTokenBasedThrottlingConfiguration().isTokenBasedThrottlingEnabled()) {
                APIMaxTpsTokenBasedThrottlingConfigurationDTO tokenCfg =
                        buildThrottlingConfigurationDTO(backendConfig.getTokenBasedThrottlingConfiguration());
                maxTpsDTO.setTokenBasedThrottlingConfiguration(tokenCfg);
            }
            return maxTpsDTO;
        } catch (NumberFormatException e) {
            log.error("Cannot convert to Long format when setting maxTps for API", e);
            return null;
        }
    }

    /**
     * Converts an {@link APIMaxTpsDTO} to a {@link MaxTpsDTO} for use in MCPServerDTO.
     * This excludes token-based throttling configuration, as MCPServerDTO
     * does not support it.
     *
     * @param source the APIMaxTpsDTO to convert
     * @return the converted MaxTpsDTO or {@code null} if the source is null
     */
    private static MaxTpsDTO toMaxTpsDTO(APIMaxTpsDTO source) {

        if (source == null) {
            return null;
        }
        MaxTpsDTO target = new MaxTpsDTO();
        target.setProduction(source.getProduction());
        target.setSandbox(source.getSandbox());
        if (source.getProductionTimeUnit() != null) {
            target.setProductionTimeUnit(
                    MaxTpsDTO.ProductionTimeUnitEnum.valueOf(source.getProductionTimeUnit().name()));
        }
        if (source.getSandboxTimeUnit() != null) {
            target.setSandboxTimeUnit(MaxTpsDTO.SandboxTimeUnitEnum.valueOf(source.getSandboxTimeUnit().name()));
        }
        return target;
    }

    /**
     * Return the REST API DTO representation of API Lifecycle state information.
     *
     * @param apiLCData API lifecycle state information
     * @return REST API DTO representation of API Lifecycle state information
     */
    public static LifecycleStateDTO fromLifecycleModelToDTO(Map<String, Object> apiLCData,
                                                            boolean apiOlderVersionExist, String apiType) {

        LifecycleStateDTO lifecycleStateDTO = new LifecycleStateDTO();

        String currentState = (String) apiLCData.get(APIConstants.LC_STATUS);
        lifecycleStateDTO.setState(currentState);

        String[] nextStates = (String[]) apiLCData.get(APIConstants.LC_NEXT_STATES);
        if (nextStates != null) {
            List<LifecycleStateAvailableTransitionsDTO> transitionDTOList = new ArrayList<>();
            for (String state : nextStates) {
                LifecycleStateAvailableTransitionsDTO transitionDTO = new LifecycleStateAvailableTransitionsDTO();
                transitionDTO.setEvent(state);
                //todo: Set target state properly
                transitionDTO.setTargetState(EMPTY_STRING);
                transitionDTOList.add(transitionDTO);
            }
            lifecycleStateDTO.setAvailableTransitions(transitionDTOList);
        }

        List checkListItems = (List) apiLCData.get(APIConstants.LC_CHECK_ITEMS);
        if (checkListItems != null) {
            List<LifecycleStateCheckItemsDTO> checkItemsDTOList = new ArrayList<>();
            for (Object checkListItemObj : checkListItems) {
                CheckListItem checkListItem = (CheckListItem) checkListItemObj;
                if (!apiOlderVersionExist && (checkListItem.getName().equals(APIConstants.DEPRECATE_CHECK_LIST_ITEM)
                        || checkListItem.getName().equals(APIConstants.RESUBSCRIBE_CHECK_LIST_ITEM))) {
                    continue;
                }

                LifecycleStateCheckItemsDTO checkItemsDTO = new LifecycleStateCheckItemsDTO();
                if (APIConstants.API_PRODUCT.equals(apiType)) {
                    checkItemsDTO.setName(checkListItem.getName().replace(APIConstants.API_IDENTIFIER_TYPE,
                            APIConstants.API_PRODUCT_IDENTIFIER_TYPE));
                } else {
                    checkItemsDTO.setName(checkListItem.getName());
                }
                checkItemsDTO.setValue(Boolean.getBoolean(checkListItem.getValue()));
                //todo: Set targets properly
                checkItemsDTO.setRequiredStates(new ArrayList<>());

                checkItemsDTOList.add(checkItemsDTO);
            }
            lifecycleStateDTO.setCheckItems(checkItemsDTOList);
        }
        return lifecycleStateDTO;
    }

    /**
     * Return the REST API DTO representation of API Lifecycle history information.
     *
     * @param lifeCycleEvents API lifecycle history information
     * @return REST API DTO representation of API Lifecycle history information
     */
    public static LifecycleHistoryDTO fromLifecycleHistoryModelToDTO(List<LifeCycleEvent> lifeCycleEvents) {

        LifecycleHistoryDTO historyDTO = new LifecycleHistoryDTO();
        historyDTO.setCount(lifeCycleEvents.size());
        for (LifeCycleEvent event : lifeCycleEvents) {
            LifecycleHistoryItemDTO historyItemDTO = new LifecycleHistoryItemDTO();
            historyItemDTO.setPostState(event.getNewStatus());
            historyItemDTO.setPreviousState(event.getOldStatus());
            historyItemDTO.setUser(event.getUserId());

            String updatedTime = RestApiCommonUtil.getRFC3339Date(event.getDate());
            historyItemDTO.setUpdatedTime(updatedTime);
            historyDTO.getList().add(historyItemDTO);
        }
        return historyDTO;
    }

    /**
     * This method returns URI templates according to the given list of operations.
     *
     * @param operations List operations
     * @return URI Templates
     * @throws APIManagementException
     */
    public static Set<URITemplate> getURITemplates(API model, List<APIOperationsDTO> operations)
            throws APIManagementException {

        boolean isHttpVerbDefined = false;
        Set<URITemplate> uriTemplates = new LinkedHashSet<>();

        if (operations == null || operations.isEmpty()) {
            operations = getDefaultOperationsList(model.getType());
        }

        for (APIOperationsDTO operation : operations) {
            URITemplate template = new URITemplate();
            String uriTempVal = operation.getTarget();
            String httpVerb = operation.getVerb();

            if (StringUtils.isEmpty(uriTempVal)) {
                throw new APIManagementException("Resource URI template value (target) is not specified",
                        ExceptionCodes.RESOURCE_URI_TEMPLATE_NOT_DEFINED);
            }

            assignScopes(model, operation.getScopes(), template);

            if (operation.getAmznResourceName() != null) {
                template.setAmznResourceName(operation.getAmznResourceName());
            }

            if (StringUtils.isEmpty(httpVerb)) {
                throw new APIManagementException(
                        "Operation type/http method is not specified for the operation/resource " + uriTempVal,
                        ExceptionCodes.from(ExceptionCodes.OPERATION_OR_RESOURCE_TYPE_OR_METHOD_NOT_DEFINED,
                                uriTempVal));
            }

            if (APIConstants.SUPPORTED_METHODS.contains(httpVerb.toLowerCase())
                    || APIConstants.GRAPHQL_SUPPORTED_METHOD_LIST.contains(httpVerb.toUpperCase())
                    || APIConstants.WEBSUB_SUPPORTED_METHOD_LIST.contains(httpVerb.toUpperCase())
                    || APIConstants.SSE_SUPPORTED_METHOD_LIST.contains(httpVerb.toUpperCase())
                    || APIConstants.WS_SUPPORTED_METHOD_LIST.contains(httpVerb.toUpperCase())) {

                isHttpVerbDefined = true;
                String authType = mapOASToInternalAuthType(operation.getAuthType());
                setCommonTemplateFields(template, uriTempVal, httpVerb, authType,
                        operation.getThrottlingPolicy(), operation.getOperationPolicies());

                uriTemplates.add(template);
            } else {
                handleUnsupportedHttpVerb(model.getType(), httpVerb, uriTempVal);
            }

            if (!isHttpVerbDefined) {
                handleMissingHttpVerb(model.getType(), uriTempVal);
            }
        }
        return uriTemplates;
    }

    /**
     * Throws an {@link APIManagementException} when the given HTTP verb or operation type
     * is not supported for the specified API type.
     *
     * @param apiType     API type (e.g., GraphQL, WebSub, SSE, WS, REST)
     * @param httpVerb    provided HTTP verb or operation type
     * @param uriTemplate URI template or operation target
     * @throws APIManagementException if the verb is unsupported for the API type
     */
    private static void handleUnsupportedHttpVerb(String apiType, String httpVerb, String uriTemplate)
            throws APIManagementException {

        final String errorMessageEnd =
                " operation Type '" + httpVerb + "' provided for operation '" + uriTemplate + "' is invalid";

        if (APIConstants.GRAPHQL_API.equals(apiType)
                || APIConstants.API_TYPE_WEBSUB.equals(apiType)
                || APIConstants.API_TYPE_SSE.equals(apiType)
                || APIConstants.API_TYPE_WS.equals(apiType)) {
            throw new APIManagementException("The " + apiType + errorMessageEnd,
                    ExceptionCodes.from(ExceptionCodes.OPERATION_TYPE_INVALID, apiType, httpVerb, uriTemplate));
        }

        throw new APIManagementException(
                "The HTTP method '" + httpVerb + "' provided for resource '" + uriTemplate + "' is invalid",
                ExceptionCodes.from(ExceptionCodes.HTTP_METHOD_INVALID, httpVerb, uriTemplate));
    }


    /**
     * Handles the case where an HTTP verb is missing for a given API type and URI template.
     *
     * @param apiType      The type of the API (e.g., GraphQL, WebSub, SSE, MCP, etc.)
     * @param uriTemplate  The URI template that is missing the HTTP verb
     * @throws APIManagementException if an error occurs while handling the missing HTTP verb
     */
    private static void handleMissingHttpVerb(String apiType, String uriTemplate) throws APIManagementException {
        if (APIConstants.GRAPHQL_API.equals(apiType)) {
            handleException("Operation '" + uriTemplate + "' has global parameters without Operation Type");
        } else if (APIConstants.API_TYPE_WEBSUB.equals(apiType)
                || APIConstants.API_TYPE_SSE.equals(apiType)) {
            handleException("Topic '" + uriTemplate + "' has global parameters without Operation Type");
        } else if (APIConstants.API_TYPE_MCP.equals(apiType)) {
            handleException("Tool '" + uriTemplate + "' has global parameters without Operation Type");
        } else {
            handleException("Resource '" + uriTemplate + "' has global parameters without HTTP methods");
        }
    }

    /**
     * This method returns URI templates according to the given list of MCP server operations.
     *
     * @param model      API model
     * @param operations List of MCP server operations
     * @return URI Templates
     * @throws APIManagementException if an error occurs while processing the operations
     */
    public static Set<URITemplate> getURITemplatesForMCPServers(API model, List<MCPServerOperationDTO> operations)
            throws APIManagementException {

        Set<URITemplate> uriTemplates = new LinkedHashSet<>();

        for (MCPServerOperationDTO operation : operations) {
            URITemplate template = new URITemplate();
            String uriTempVal = operation.getTarget();
            String verb = operation.getFeature().toString();

            assignScopes(model, operation.getScopes(), template);

            if (StringUtils.isEmpty(verb)) {
                throw new APIManagementException(
                        "Operation type/http method is not specified for the operation/resource " + uriTempVal,
                        ExceptionCodes.from(ExceptionCodes.OPERATION_OR_RESOURCE_TYPE_OR_METHOD_NOT_DEFINED,
                                uriTempVal));
            }

            String authType = mapOASToInternalAuthType(operation.getAuthType());
            setCommonTemplateFields(template, uriTempVal, verb, authType,
                    operation.getThrottlingPolicy(), operation.getOperationPolicies());

            template.setSchemaDefinition(operation.getSchemaDefinition());
            template.setDescription(operation.getDescription());

            if (operation.getBackendOperationMapping() != null) {
                template.setBackendOperationMapping(OperationPolicyMappingUtil
                        .fromDTOToBackendOperationMapping(operation.getBackendOperationMapping()));
            }
            if (operation.getApiOperationMapping() != null) {
                template.setAPIOperationMapping(OperationPolicyMappingUtil
                        .fromDTOToAPIOperationMapping(operation.getApiOperationMapping()));
            }
            uriTemplates.add(template);
        }
        return uriTemplates;
    }

    private static void assignScopes(API model, List<String> scopeList, URITemplate template) {
        if (scopeList == null) {
            return;
        }
        for (String scopeKey : scopeList) {
            for (Scope definedScope : model.getScopes()) {
                if (definedScope.getKey().equalsIgnoreCase(scopeKey)) {
                    template.setScopes(definedScope);
                    template.setScope(definedScope);
                    break;
                }
            }
        }
    }

    private static String mapOASToInternalAuthType(String authType) {
        if (APIConstants.OASResourceAuthTypes.APPLICATION_OR_APPLICATION_USER.equals(authType)) {
            return APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN;
        } else if (APIConstants.OASResourceAuthTypes.APPLICATION_USER.equals(authType)) {
            return APIConstants.AUTH_APPLICATION_USER_LEVEL_TOKEN;
        } else if (APIConstants.OASResourceAuthTypes.NONE.equals(authType)) {
            return APIConstants.AUTH_NO_AUTHENTICATION;
        } else if (APIConstants.OASResourceAuthTypes.APPLICATION.equals(authType)) {
            return APIConstants.AUTH_APPLICATION_LEVEL_TOKEN;
        }
        return APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN;
    }

    /**
     * Sets common fields for a URITemplate.
     *
     * @param template            the URITemplate to set fields on
     * @param uriTemplate         the URI template string
     * @param httpVerb            the HTTP verb (method)
     * @param authType            the authentication type
     * @param throttlingPolicy    the throttling policy
     * @param operationPolicies   the operation policies DTO
     */
    private static void setCommonTemplateFields(URITemplate template, String uriTemplate, String httpVerb,
                                                String authType, String throttlingPolicy,
                                                APIOperationPoliciesDTO operationPolicies) {

        template.setThrottlingTier(throttlingPolicy);
        template.setThrottlingTiers(throttlingPolicy);
        template.setUriTemplate(uriTemplate);
        template.setHTTPVerb(httpVerb.toUpperCase());
        template.setHttpVerbs(httpVerb.toUpperCase());
        template.setAuthType(authType);
        template.setAuthTypes(authType);

        if (operationPolicies != null) {
            template.setOperationPolicies(
                    OperationPolicyMappingUtil.fromDTOToAPIOperationPoliciesList(operationPolicies));
        }
    }

    /**
     * This method returns graphQL operations with the old data.
     *
     * @param uriTemplates uriTemplates
     * @param operations   operations
     * @return operations
     */
    public static List<APIOperationsDTO> getOperationListWithOldData(Set<URITemplate> uriTemplates,
                                                                     List<APIOperationsDTO> operations,
                                                                     int tenantId) throws APIManagementException {

        for (APIOperationsDTO operation : operations) {
            for (URITemplate uriTemplate : uriTemplates) {
                if (operation.getVerb().equalsIgnoreCase(uriTemplate.getHTTPVerb()) &&
                        operation.getTarget().equalsIgnoreCase(uriTemplate.getUriTemplate())) {
                    operation.setThrottlingPolicy(uriTemplate.getThrottlingTier());
                    operation.setAuthType(uriTemplate.getAuthType());
                    operation.setScopes(uriTemplate.retrieveAllScopes().stream().map(Scope::getKey).collect(
                            Collectors.toList()));
                }
                if (operation.getThrottlingPolicy() == null) {
                    operation.setThrottlingPolicy(APIUtil.getDefaultAPILevelPolicy(tenantId));
                }
            }
        }
        return operations;
    }

    /**
     * Extracts a set of {@link Scope} objects from an {@link APIDTO}.
     *
     * @param apiDTO the API DTO containing scope definitions
     * @return a set of {@link Scope} objects constructed from the DTO
     */
    public static Set<Scope> getScopes(APIDTO apiDTO) {

        return getScopesFromList(apiDTO.getScopes());
    }

    /**
     * Extracts a set of {@link Scope} objects from an {@link MCPServerDTO}.
     *
     * @param apiDTO the MCP server DTO containing scope definitions
     * @return a set of {@link Scope} objects constructed from the DTO
     */
    public static Set<Scope> getScopes(MCPServerDTO apiDTO) {

        return getMCPServerScopesFromList(apiDTO.getScopes());
    }

    /**
     * Converts a list of {@link APIScopeDTO} objects into a set of internal {@link Scope} objects.
     *
     * @param apiScopeDTOs the list of API scope DTOs
     * @return a set of {@link Scope} objects, or an empty set if the input is null
     */
    private static Set<Scope> getScopesFromList(List<APIScopeDTO> apiScopeDTOs) {

        Set<Scope> scopeSet = new LinkedHashSet<>();
        if (apiScopeDTOs == null) {
            return scopeSet;
        }
        for (APIScopeDTO apiScopeDTO : apiScopeDTOs) {
            Scope scope = new Scope();
            ScopeDTO scopeDTO = apiScopeDTO.getScope();
            scope.setKey(scopeDTO.getName());
            scope.setName(scopeDTO.getDisplayName());
            scope.setDescription(scopeDTO.getDescription());
            scope.setRoles(String.join(",", scopeDTO.getBindings()));
            scopeSet.add(scope);
        }
        return scopeSet;
    }

    /**
     * Converts a list of {@link MCPServerScopeDTO} objects into a set of internal {@link Scope} objects.
     *
     * @param mcpServerScopeDTOList the list of MCP server scope DTOs
     * @return a set of {@link Scope} objects, or an empty set if the input is null
     */
    private static Set<Scope> getMCPServerScopesFromList(List<MCPServerScopeDTO> mcpServerScopeDTOList) {

        Set<Scope> scopeSet = new LinkedHashSet<>();
        if (mcpServerScopeDTOList == null) {
            return scopeSet;
        }
        for (MCPServerScopeDTO apiScopeDTO : mcpServerScopeDTOList) {
            Scope scope = new Scope();
            ScopeDTO scopeDTO = apiScopeDTO.getScope();
            scope.setKey(scopeDTO.getName());
            scope.setName(scopeDTO.getDisplayName());
            scope.setDescription(scopeDTO.getDescription());
            scope.setRoles(String.join(",", scopeDTO.getBindings()));
            scopeSet.add(scope);
        }
        return scopeSet;
    }

    /**
     * This method returns the oauth scopes according to the given list of scopes.
     *
     * @param apiProductDTO list of scopes
     * @return scope set
     */
    private static Set<Scope> getScopes(APIProductDTO apiProductDTO) {

        Set<Scope> scopeSet = new LinkedHashSet<>();
        for (APIScopeDTO apiScopeDTO : apiProductDTO.getScopes()) {
            Scope scope = new Scope();
            ScopeDTO scopeDTO = apiScopeDTO.getScope();
            scope.setKey(scopeDTO.getName());
            scope.setName(scopeDTO.getDisplayName());
            scope.setDescription(scopeDTO.getDescription());
            scope.setRoles(String.join(",", scopeDTO.getBindings()));
            scopeSet.add(scope);
        }
        return scopeSet;
    }
//
//    /**
//     * This method returns endpoints according to the given endpoint config
//     *
//     * @param endpoints endpoints given
//     * @return String endpoint config
//     */
//    public static String getEndpointConfigString(List<APIEndpointDTO> endpoints) {
//        //todo improve this logic to support multiple endpoints such as failorver and load balance
//        StringBuilder sb = new StringBuilder();
//        if (endpoints != null && endpoints.size() > 0) {
//            sb.append("{");
//            for (APIEndpointDTO endpoint : endpoints) {
//                sb.append("\EMPTY_STRING)
//                        .append(endpoint.getType())
//                        .append("\": {\"url\":\EMPTY_STRING)
//                        .append(endpoint.getInline().getEndpointConfig().getList().get(0).getUrl())
//                        .append("\",\"timeout\":\EMPTY_STRING)
//                        .append(endpoint.getInline().getEndpointConfig().getList().get(0).getTimeout())
//                        .append("\"},");
//            }
//            sb.append("\"endpoint_type\" : \EMPTY_STRING)
//                    .append(endpoints.get(0).getInline().getType())//assuming all the endpoints are same type
//                    .append("\"}\n");
//        }
//        return sb.toString();
//    }

//    private static EndpointEndpointConfigDTO getEndpointEndpointConfigDTO(EndpointEndpointConfig
//    endpointEndpointConfig) {
//
//        //map to EndpointEndpointConfig model to EndpointEndpointConfigDTO
//        EndpointEndpointConfigDTO endpointEndpointConfigDTO = new EndpointEndpointConfigDTO();
//        switch (endpointEndpointConfig.getEndpointType()) {
//            case SINGLE:
//                endpointEndpointConfigDTO.setEndpointType(EndpointEndpointConfigDTO.EndpointTypeEnum.SINGLE);
//            case LOAD_BALANCED:
//                endpointEndpointConfigDTO.setEndpointType(EndpointEndpointConfigDTO.EndpointTypeEnum.LOAD_BALANCED);
//            case FAIL_OVER:
//                endpointEndpointConfigDTO.setEndpointType(EndpointEndpointConfigDTO.EndpointTypeEnum.FAIL_OVER);
//        }
//        List<EndpointConfigDTO> endpointConfigDTOList = new ArrayList<>();
//        for (EndpointConfig endpointConfig : endpointEndpointConfig.getList()) {
//            EndpointConfigDTO endpointConfigDTO = new EndpointConfigDTO();
//            endpointConfigDTO.setUrl(endpointConfig.getUrl());
//            endpointConfigDTO.setTimeout(endpointConfig.getTimeout());
//
//            //map EndpointConfigAttributes model to EndpointConfigAttributesDTO
//            List<EndpointConfigAttributesDTO> endpointConfigAttributesList = new ArrayList<>();
//            for (EndpointConfigAttributes endpointConfigAttributes : endpointConfig.getAttributes()) {
//                EndpointConfigAttributesDTO endpointConfigAttributeDTO = new EndpointConfigAttributesDTO();
//                endpointConfigAttributeDTO.setName(endpointConfigAttributes.getName());
//                endpointConfigAttributeDTO.setValue(endpointConfigAttributes.getValue());
//                endpointConfigAttributesList.add(endpointConfigAttributeDTO);
//            }
//            endpointConfigDTO.setAttributes(endpointConfigAttributesList);
//            endpointConfigDTOList.add(endpointConfigDTO);
//        }
//        endpointEndpointConfigDTO.setList(endpointConfigDTOList);
//        return endpointEndpointConfigDTO;
//    }
//
//    /**
//     * This method converts Endpoint:EndpontConfig DTO to corresponding model
//     *
//     * @param apiEndpointDTO1 egiven endpoint config
//     * @param type            given endpoint type  SINGLE,  LOAD_BALANCED,  FAIL_OVER
//     * @return EndpointConfig model
//     */
//    private EndpointEndpointConfig getEndpointEndpointConfigModel(EndpointEndpointConfigDTO apiEndpointDTO1,
//                                                                  EndpointEndpointConfig.EndpointTypeEnum type) {
//
//        //mapping properties in EndpointConfigDTO to EndpointConfig model
//        List<EndpointConfig> configList = new ArrayList<>();
//        for (EndpointConfigDTO endpointConfigDTO : apiEndpointDTO1.getList()) {
//            EndpointConfig endpointConfig1 = new EndpointConfig();
//            endpointConfig1.setUrl(endpointConfigDTO.getUrl());
//            endpointConfig1.setTimeout(endpointConfigDTO.getTimeout());
//
//            //mapping attributes in EndpointConfigAttributesDTO to EndpointConfigAttributes model
//            List<EndpointConfigAttributes> endpointConfigAttributesList = new ArrayList<>();
//            for (EndpointConfigAttributesDTO endpointConfigAttributesDTO : endpointConfigDTO.getAttributes()) {
//                EndpointConfigAttributes endpointConfigAttribute = new EndpointConfigAttributes();
//                endpointConfigAttribute.setName(endpointConfigAttributesDTO.getName());
//                endpointConfigAttribute.setValue(endpointConfigAttributesDTO.getValue());
//
//                endpointConfigAttributesList.add(endpointConfigAttribute);
//            }
//
//            endpointConfig1.setAttributes(endpointConfigAttributesList);
//            configList.add(endpointConfig1);
//        }
//
//        //mapping properties in EndpointEndpointConfigDTO to EndpointEndpointConfig model
//        EndpointEndpointConfig endpointConfig = new EndpointEndpointConfig();
//        endpointConfig.setEndpointType(type);
//        endpointConfig.setList(configList);
//
//        return endpointConfig;
//
//    }

    /**
     * This method returns api security scheme as a comma seperated string.
     *
     * @param securitySchemes api security scheme
     * @return comma seperated string of api security schemes
     */
    public static String getSecurityScheme(List<String> securitySchemes) {

        if (securitySchemes == null || securitySchemes.size() <= 0) {
            return EMPTY_STRING;
        }
        StringBuilder apiSecurityScheme = new StringBuilder();
        if (securitySchemes != null) {
            for (String scheme : securitySchemes) {
                apiSecurityScheme.append(scheme).append(",");
            }
            apiSecurityScheme.deleteCharAt(apiSecurityScheme.length() - 1);
        }
        return apiSecurityScheme.toString();
    }

    public static OpenAPIDefinitionValidationResponseDTO getOpenAPIDefinitionValidationResponseFromModel(
            APIDefinitionValidationResponse model, boolean returnContent) {

        OpenAPIDefinitionValidationResponseDTO responseDTO = new OpenAPIDefinitionValidationResponseDTO();
        responseDTO.setIsValid(model.isValid());

        if (model.isValid()) {
            APIDefinitionValidationResponse.Info modelInfo = model.getInfo();
            if (modelInfo != null) {
                OpenAPIDefinitionValidationResponseInfoDTO infoDTO =
                        new OpenAPIDefinitionValidationResponseInfoDTO();
                infoDTO.setOpenAPIVersion(modelInfo.getOpenAPIVersion());
                infoDTO.setName(modelInfo.getName());
                infoDTO.setVersion(modelInfo.getVersion());
                infoDTO.setContext(modelInfo.getContext());
                infoDTO.setDescription(modelInfo.getDescription());
                infoDTO.setEndpoints(modelInfo.getEndpoints());
                List<APIOperationsDTO> apiOperationsDTO = new ArrayList<>();
                if (modelInfo.getUriTemplates() != null && !modelInfo.getUriTemplates().isEmpty()) {
                    for (URITemplate uriTemplate : modelInfo.getUriTemplates()) {
                        APIOperationsDTO apiOperations = new APIOperationsDTO();
                        apiOperations.setVerb(uriTemplate.getHTTPVerb());
                        apiOperations.setTarget(uriTemplate.getUriTemplate());
                        apiOperationsDTO.add(apiOperations);
                    }
                    infoDTO.setOperations(apiOperationsDTO);
                }
                responseDTO.setInfo(infoDTO);
            }
            if (returnContent) {
                responseDTO.setContent(model.getContent());
            }
        } else {
            responseDTO.setErrors(getErrorListItemsDTOsFromErrorHandlers(model.getErrorItems()));
        }
        return responseDTO;
    }

    public static AsyncAPISpecificationValidationResponseDTO getAsyncAPISpecificationValidationResponseFromModel(
            APIDefinitionValidationResponse model, boolean returnContent) throws APIManagementException {

        AsyncAPISpecificationValidationResponseDTO responseDTO = new AsyncAPISpecificationValidationResponseDTO();
        responseDTO.setIsValid(model.isValid());

        if (model.isValid()) {
            APIDefinitionValidationResponse.Info modelInfo = model.getInfo();
            if (modelInfo != null) {
                AsyncAPISpecificationValidationResponseInfoDTO infoDTO =
                        new AsyncAPISpecificationValidationResponseInfoDTO();
                infoDTO.setAsyncAPIVersion(modelInfo.getOpenAPIVersion());
                infoDTO.setName(modelInfo.getName());
                infoDTO.setVersion(modelInfo.getVersion());
                infoDTO.setContext(modelInfo.getContext());
                infoDTO.setDescription(modelInfo.getDescription());
                infoDTO.setEndpoints(modelInfo.getEndpoints());
                infoDTO.setProtocol(model.getProtocol());

                Map<String, APIDefinition> apiDefinitionMap = APIUtil.getApiDefinitionParsersMap();
                apiDefinitionMap.remove(APIConstants.WSO2_GATEWAY_ENVIRONMENT);
                if (!apiDefinitionMap.isEmpty()) {
                    for (Map.Entry<String, APIDefinition> apiDefinitionEntry : apiDefinitionMap.entrySet()) {
                        APIDefinition apiParser = apiDefinitionEntry.getValue();
                        String gatewayVendor = apiParser.getVendorFromExtensionWithError(model.getContent());
                        if (gatewayVendor != null) {
                            infoDTO.setGatewayVendor(gatewayVendor);
                            break;
                        }
                    }
                    infoDTO.asyncTransportProtocols(AsyncApiParserUtil.getTransportProtocolsForAsyncAPI
                            (model.getContent()));
                }
                // Set default value
                if (infoDTO.getGatewayVendor() == null) {
                    infoDTO.setGatewayVendor(APIConstants.WSO2_GATEWAY_ENVIRONMENT);
                }
                responseDTO.setInfo(infoDTO);
            }
            if (returnContent) {
                responseDTO.setContent(model.getContent());
            }
        } else {
            responseDTO.setErrors(getErrorListItemsDTOsFromErrorHandlers(model.getErrorItems()));
        }
        return responseDTO;
    }

    public static List<ErrorListItemDTO> getErrorListItemsDTOsFromErrorHandlers(List<ErrorHandler> errorHandlers) {

        List<ErrorListItemDTO> errorListItemDTOs = new ArrayList<>();
        for (ErrorHandler handler : errorHandlers) {
            ErrorListItemDTO dto = new ErrorListItemDTO();
            dto.setCode(handler.getErrorCode() + EMPTY_STRING);
            dto.setMessage(handler.getErrorMessage());
            dto.setDescription(handler.getErrorDescription());
            errorListItemDTOs.add(dto);
        }
        return errorListItemDTOs;
    }

    public static List<ErrorListItemDTO> getErrorListItemsDTOsFromErrorHandler(ErrorHandler error) {

        List<ErrorListItemDTO> errorListItemDTOs = new ArrayList<>();
        ErrorListItemDTO dto = new ErrorListItemDTO();
        dto.setCode(error.getErrorCode() + EMPTY_STRING);
        dto.setMessage(error.getErrorMessage());
        dto.setDescription(error.getErrorDescription());
        errorListItemDTOs.add(dto);
        return errorListItemDTOs;
    }

    /**
     * Get the ErrorDTO from a list of ErrorListItemDTOs. The first item in the list is set as the main error.
     *
     * @param errorListItemDTOs A list of ErrorListItemDTO objects
     * @return ErrorDTO from a list of ErrorListItemDTOs
     */
    public static ErrorDTO getErrorDTOFromErrorListItems(List<ErrorListItemDTO> errorListItemDTOs) {

        ErrorDTO errorDTO = new ErrorDTO();
        for (int i = 0; i < errorListItemDTOs.size(); i++) {
            if (i == 0) {
                ErrorListItemDTO elementAt0 = errorListItemDTOs.get(0);
                errorDTO.setCode(Long.parseLong(elementAt0.getCode()));
                errorDTO.setMoreInfo(EMPTY_STRING);
                errorDTO.setMessage(elementAt0.getMessage());
                errorDTO.setDescription(elementAt0.getDescription());
            } else {
                org.wso2.carbon.apimgt.rest.api.common.dto.ErrorListItemDTO errorListItemDTO
                        = new org.wso2.carbon.apimgt.rest.api.common.dto.ErrorListItemDTO();
                errorListItemDTO.setCode(errorListItemDTOs.get(i).getCode() + EMPTY_STRING);
                errorListItemDTO.setMessage(errorListItemDTOs.get(i).getMessage());
                errorListItemDTO.setDescription(errorListItemDTOs.get(i).getDescription());
                errorDTO.getError().add(errorListItemDTO);
            }
        }
        return errorDTO;
    }

    /**
     * Returns workflow state DTO from the provided information.
     *
     * @param lifecycleStateDTO   Lifecycle state DTO
     * @param stateChangeResponse workflow response from API lifecycle change
     * @return workflow state DTO
     */
    public static WorkflowResponseDTO toWorkflowResponseDTO(LifecycleStateDTO lifecycleStateDTO,
                                                            APIStateChangeResponse stateChangeResponse) {

        WorkflowResponseDTO workflowResponseDTO = new WorkflowResponseDTO();

        if (WorkflowStatus.APPROVED.toString().equals(stateChangeResponse.getStateChangeStatus())) {
            workflowResponseDTO.setWorkflowStatus(WorkflowResponseDTO.WorkflowStatusEnum.APPROVED);
        } else if (WorkflowStatus.CREATED.toString().equals(stateChangeResponse.getStateChangeStatus())) {
            workflowResponseDTO.setWorkflowStatus(WorkflowResponseDTO.WorkflowStatusEnum.CREATED);
        } else if ((WorkflowStatus.REGISTERED.toString().equals(stateChangeResponse.getStateChangeStatus()))) {
            workflowResponseDTO.setWorkflowStatus(WorkflowResponseDTO.WorkflowStatusEnum.REGISTERED);
        } else if ((WorkflowStatus.REJECTED.toString().equals(stateChangeResponse.getStateChangeStatus()))) {
            workflowResponseDTO.setWorkflowStatus(WorkflowResponseDTO.WorkflowStatusEnum.REJECTED);
        } else {
            log.error("Unrecognized state : " + stateChangeResponse.getStateChangeStatus());
            workflowResponseDTO.setWorkflowStatus(WorkflowResponseDTO.WorkflowStatusEnum.CREATED);
        }

        workflowResponseDTO.setJsonPayload(stateChangeResponse.getWorkflowResponse().getJSONPayload());
        workflowResponseDTO.setLifecycleState(lifecycleStateDTO);
        return workflowResponseDTO;
    }

    /**
     * Returns a set of operations from a API.
     *
     * @param api                 API object
     * @param preserveCredentials Condition to preserve secret parameter values
     * @return a set of operations from a given swagger definition
     * @throws APIManagementException if error occurred while handling the secret parameters
     */
    private static List<APIOperationsDTO> getOperationsFromAPI(API api, boolean preserveCredentials)
            throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Getting operations from API. API UUID: " + api.getUuid() + ", Type: " + api.getType());
        }
        Set<URITemplate> uriTemplates = api.getUriTemplates();
        List<APIOperationsDTO> operationsDTOList = new ArrayList<>();
        for (URITemplate uriTemplate : uriTemplates) {
            APIOperationsDTO operationsDTO = getOperationFromURITemplate(uriTemplate, api.getUuid(),
                    preserveCredentials);

            if (api.getType().equals(APIConstants.API_TYPE_WS)) {
                Map<String, String> wsUriMappings = api.getWsUriMapping();
                if (wsUriMappings != null) {
                    String wsUriMapping = wsUriMappings.get(operationsDTO.getVerb() + "_" + operationsDTO.getTarget());
                    if (wsUriMapping != null) {
                        operationsDTO.setUriMapping(wsUriMapping);
                    }
                }
            }
            operationsDTOList.add(operationsDTO);
        }
        return operationsDTOList;
    }

    /**
     * Returns a set of operations from a API for MCP server.
     *
     * @param api API object
     * @return a set of operations from a given swagger definition
     */
    private static List<MCPServerOperationDTO> getMCPServerOperationsFromAPI(API api) {

        Set<URITemplate> uriTemplates = api.getUriTemplates();
        List<MCPServerOperationDTO> operationsDTOList = new ArrayList<>();
        for (URITemplate uriTemplate : uriTemplates) {
            MCPServerOperationDTO operationsDTO = getMCPServerOperationFromURITemplate(uriTemplate);
            operationsDTOList.add(operationsDTO);
        }
        operationsDTOList.sort(Comparator.comparing(MCPServerOperationDTO::getTarget,
                Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
        return operationsDTOList;
    }

    /**
     * Returns a set of operations from a API
     * Returns a set of operations from a given swagger definition
     *
     * @param api                 API object
     * @param swaggerDefinition   Swagger definition
     * @param preserveCredentials Condition to preserve secret parameter values
     * @return a set of operations from a given swagger definition
     * @throws APIManagementException error while trying to retrieve URI templates of the given API
     */
    private static List<APIOperationsDTO> getOperationsFromSwaggerDef(API api, String swaggerDefinition,
            boolean preserveCredentials) throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Extracting operations from swagger definition for API UUID: " + api.getUuid());
        }
        APIDefinition apiDefinition = OASParserUtil.getOASParser(swaggerDefinition);
        Set<URITemplate> uriTemplates;
        if (APIConstants.GRAPHQL_API.equals(api.getType())) {
            uriTemplates = api.getUriTemplates();
        } else {
            uriTemplates = apiDefinition.getURITemplates(swaggerDefinition);
        }

        List<APIOperationsDTO> operationsDTOList = new ArrayList<>();
        if (!StringUtils.isEmpty(swaggerDefinition)) {
            for (URITemplate uriTemplate : uriTemplates) {
                APIOperationsDTO operationsDTO = getOperationFromURITemplate(uriTemplate, api.getUuid(),
                        preserveCredentials);
                operationsDTOList.add(operationsDTO);
            }
        }
        return operationsDTOList;
    }

    /**
     * Reads the operationPolicies from the API object passed in, and sets them back to the API Operations DTO
     *
     * @param api                 API object
     * @param apiOperationsDTO    List of API Operations DTO
     * @param preserveCredentials Condition to preserve secret parameter values
     * @throws APIManagementException if error occurred while handling the secret parameters
     */
    private static void setOperationPoliciesToOperationsDTO(API api, List<APIOperationsDTO> apiOperationsDTO,
            boolean preserveCredentials) throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug("Setting operation policies to operations DTO for API UUID: " + api.getUuid());
        }

        Set<URITemplate> uriTemplates = api.getUriTemplates();

        Map<String, URITemplate> uriTemplateMap = new HashMap<>();
        for (URITemplate uriTemplate : uriTemplates) {
            String key = uriTemplate.getUriTemplate() + ":" + uriTemplate.getHTTPVerb();
            uriTemplateMap.put(key, uriTemplate);
        }

        for (APIOperationsDTO operationsDTO : apiOperationsDTO) {
            String key = operationsDTO.getTarget() + ":" + operationsDTO.getVerb();
            if (uriTemplateMap.get(key) != null) {
                List<OperationPolicy> operationPolicies = uriTemplateMap.get(key).getOperationPolicies();
                if (operationPolicies != null && !operationPolicies.isEmpty()) {
                    if (log.isDebugEnabled()) {
                        log.debug("Found " + operationPolicies.size() + " operation policies for " + key);
                    }
                    operationsDTO.setOperationPolicies(
                            OperationPolicyMappingUtil.fromOperationPolicyListToDTO(operationPolicies, api.getUuid(),
                                    preserveCredentials));
                }
            }
        }
    }

    /**
     * Maps the authentication type from URITemplate to OpenAPI specification format.
     *
     * @param authType the authentication type from URITemplate
     * @return the mapped authentication type in OpenAPI specification format
     */
    private static String mapInternalToOASAuthType(String authType) {
        if (APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN.equals(authType)) {
            return APIConstants.OASResourceAuthTypes.APPLICATION_OR_APPLICATION_USER;
        } else if (APIConstants.AUTH_APPLICATION_USER_LEVEL_TOKEN.equals(authType)) {
            return APIConstants.OASResourceAuthTypes.APPLICATION_USER;
        } else if (APIConstants.AUTH_NO_AUTHENTICATION.equals(authType)) {
            return APIConstants.OASResourceAuthTypes.NONE;
        } else if (APIConstants.AUTH_APPLICATION_LEVEL_TOKEN.equals(authType)) {
            return APIConstants.OASResourceAuthTypes.APPLICATION;
        }
        return APIConstants.OASResourceAuthTypes.APPLICATION_OR_APPLICATION_USER;
    }

    /**
     * Extracts the scopes used by the URITemplate.
     *
     * @param uriTemplate the URITemplate from which to extract scopes
     * @return a list of scope keys used by the URITemplate
     */
    private static List<String> extractScopes(URITemplate uriTemplate) {
        return uriTemplate.retrieveAllScopes()
                .stream()
                .map(Scope::getKey)
                .collect(Collectors.toList());
    }

    /**
     * Extracts the product IDs used by the URITemplate.
     *
     * @param uriTemplate the URITemplate from which to extract product IDs
     * @return a list of product IDs used by the URITemplate
     */
    private static List<String> extractUsedProductIds(URITemplate uriTemplate) {
        return uriTemplate.retrieveUsedByProducts()
                .stream()
                .map(APIProductIdentifier::getUUID)
                .collect(Collectors.toList());
    }

    /**
     * Populates common fields for API operation DTO from URITemplate.
     *
     * @param dto                 the APIOperationsDTO to populate
     * @param uriTemplate         the URITemplate containing the data
     * @param apiUuid             the UUID of the API
     * @param preserveCredentials whether to preserve secret parameter values
     * @throws APIManagementException if an error occurs while handling secret parameters
     */
    private static void populateCommonOperationFields(APIOperationsDTO dto, URITemplate uriTemplate, String apiUuid,
            boolean preserveCredentials) throws APIManagementException {
        dto.setId(EMPTY_STRING);
        dto.setAuthType(mapInternalToOASAuthType(uriTemplate.getAuthType()));
        dto.setTarget(uriTemplate.getUriTemplate());
        dto.setScopes(extractScopes(uriTemplate));
        dto.setOperationPolicies(OperationPolicyMappingUtil
                .fromOperationPolicyListToDTO(uriTemplate.getOperationPolicies(), apiUuid, preserveCredentials));
        dto.setThrottlingPolicy(uriTemplate.getThrottlingTier());
        List<String> usedProductIds = extractUsedProductIds(uriTemplate);
        if (!usedProductIds.isEmpty()) {
            dto.setUsedProductIds(usedProductIds);
        }
    }

    /**
     * Populates common fields for MCP server operation DTO from URITemplate.
     *
     * @param dto         the MCPServerOperationDTO to populate
     * @param uriTemplate the URITemplate containing the data
     */
    private static void populateCommonOperationFields(MCPServerOperationDTO dto, URITemplate uriTemplate) {
        dto.setId(EMPTY_STRING);
        dto.setAuthType(mapInternalToOASAuthType(uriTemplate.getAuthType()));
        dto.setTarget(uriTemplate.getUriTemplate());
        dto.setScopes(extractScopes(uriTemplate));
        dto.setOperationPolicies(OperationPolicyMappingUtil
                .fromOperationPolicyListToDTO(uriTemplate.getOperationPolicies()));
        dto.setThrottlingPolicy(uriTemplate.getThrottlingTier());
    }

    /**
     * Maps a BackendOperationMapping to a BackendOperationMappingDTO.
     *
     * @param mapping BackendOperationMapping object
     * @return BackendOperationMappingDTO object
     */
    private static BackendOperationMappingDTO mapBackendOperationMapping(BackendOperationMapping mapping) {

        BackendOperationDTO operationDTO = new BackendOperationDTO();
        operationDTO.setVerb(
                OperationPolicyMappingUtil.toSupportedHTTPVerbEnum(mapping.getBackendOperation().getVerb()));
        operationDTO.setTarget(mapping.getBackendOperation().getTarget());

        BackendOperationMappingDTO mappingDTO = new BackendOperationMappingDTO();
        mappingDTO.setBackendId(mapping.getBackendId());
        mappingDTO.setBackendOperation(operationDTO);
        return mappingDTO;
    }

    /**
     * Maps an APIOperationMapping to an APIOperationMappingDTO.
     *
     * @param mapping APIOperationMapping object
     * @return APIOperationMappingDTO object
     */
    private static APIOperationMappingDTO mapApiOperationMapping(APIOperationMapping mapping) {

        BackendOperationDTO operationDTO = new BackendOperationDTO();
        operationDTO.setVerb(
                OperationPolicyMappingUtil.toSupportedHTTPVerbEnum(mapping.getBackendOperation().getVerb()));
        operationDTO.setTarget(mapping.getBackendOperation().getTarget());

        APIOperationMappingDTO mappingDTO = new APIOperationMappingDTO();
        mappingDTO.setApiId(mapping.getApiUuid());
        mappingDTO.setApiName(mapping.getApiName());
        mappingDTO.setApiVersion(mapping.getApiVersion());
        mappingDTO.setApiContext(mapping.getApiContext());
        mappingDTO.setBackendOperation(operationDTO);
        return mappingDTO;
    }

    /**
     * Returns an API operation DTO from a URITemplate.
     *
     * @param uriTemplate         URITemplate object
     * @param apiUuid             API UUID
     * @param preserveCredentials Condition to preserve secret parameter values
     * @return APIOperationsDTO object
     * @throws APIManagementException if an error occurs while handling secret parameters
     */
    private static APIOperationsDTO getOperationFromURITemplate(URITemplate uriTemplate, String apiUuid,
            boolean preserveCredentials) throws APIManagementException {
        APIOperationsDTO dto = new APIOperationsDTO();
        populateCommonOperationFields(dto, uriTemplate, apiUuid, preserveCredentials);
        dto.setVerb(uriTemplate.getHTTPVerb());
        return dto;
    }

    /**
     * Returns a MCP server operation DTO from a URITemplate.
     *
     * @param uriTemplate URITemplate object
     * @return MCPServerOperationDTO object
     */
    private static MCPServerOperationDTO getMCPServerOperationFromURITemplate(URITemplate uriTemplate) {
        MCPServerOperationDTO dto = new MCPServerOperationDTO();
        populateCommonOperationFields(dto, uriTemplate);
        dto.setFeature(MCPServerOperationDTO.FeatureEnum.fromValue(uriTemplate.getHTTPVerb()));
        dto.setDescription(uriTemplate.getDescription());
        dto.setSchemaDefinition(uriTemplate.getSchemaDefinition());

        if (uriTemplate.getBackendOperationMapping() != null) {
            dto.setBackendOperationMapping(mapBackendOperationMapping(uriTemplate.getBackendOperationMapping()));
        }
        if (uriTemplate.getAPIOperationMapping() != null) {
            dto.setApiOperationMapping(mapApiOperationMapping(uriTemplate.getAPIOperationMapping()));
        }
        return dto;
    }

    /**
     * Returns a default operations list with wildcard resources and http verbs.
     *
     * @return a default operations list
     */
    private static List<APIOperationsDTO> getDefaultOperationsList(String apiType) {

        List<APIOperationsDTO> operationsDTOs = new ArrayList<>();
        String[] supportedMethods;

        if (apiType.equals(APIConstants.GRAPHQL_API)) {
            supportedMethods = APIConstants.GRAPHQL_SUPPORTED_METHODS;
        } else if (apiType.equals(APIConstants.API_TYPE_SOAP)) {
            supportedMethods = APIConstants.SOAP_DEFAULT_METHODS;
        } else if (apiType.equals(APIConstants.API_TYPE_WEBSUB)) {
            supportedMethods = APIConstants.WEBSUB_SUPPORTED_METHODS;
        } else if (apiType.equals(APIConstants.API_TYPE_SSE)) {
            supportedMethods = APIConstants.SSE_SUPPORTED_METHODS;
        } else if (apiType.equals(APIConstants.API_TYPE_WS)) {
            supportedMethods = APIConstants.WS_SUPPORTED_METHODS;
        } else {
            supportedMethods = APIConstants.HTTP_DEFAULT_METHODS;
        }

        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        String defaultThrottlingPolicy = APIUtil.getDefaultThrottlingPolicy(tenantDomain);

        for (String verb : supportedMethods) {
            APIOperationsDTO operationsDTO = new APIOperationsDTO();
            if (apiType.equals((APIConstants.API_TYPE_WEBSUB))) {
                operationsDTO.setTarget(APIConstants.WEBSUB_DEFAULT_TOPIC_NAME);
            } else {
                operationsDTO.setTarget("/*");
            }
            operationsDTO.setVerb(verb);
            operationsDTO.setThrottlingPolicy(defaultThrottlingPolicy);
            operationsDTO.setAuthType(APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN);
            operationsDTOs.add(operationsDTO);
        }
        return operationsDTOs;
    }

    public static APIProductListDTO fromAPIProductListtoDTO(List<APIProduct> productList) {

        APIProductListDTO listDto = new APIProductListDTO();
        List<APIProductInfoDTO> list = new ArrayList<APIProductInfoDTO>();
        for (APIProduct apiProduct : productList) {
            APIProductInfoDTO productDto = new APIProductInfoDTO();
            productDto.setName(apiProduct.getId().getName());
            productDto.setProvider(APIUtil.replaceEmailDomainBack(apiProduct.getId().getProviderName()));
            String context = apiProduct.getContextTemplate();
            if (context.endsWith(FORWARD_SLASH + RestApiConstants.API_VERSION_PARAM)) {
                context = context.replace(FORWARD_SLASH + RestApiConstants.API_VERSION_PARAM, EMPTY_STRING);
            }
            productDto.setContext(context);
            productDto.setDescription(apiProduct.getDescription());
            productDto.setState(apiProduct.getState());
            productDto.setVersion(apiProduct.getId().getVersion());
            productDto.setId(apiProduct.getUuid());
            productDto.setHasThumbnail(!StringUtils.isBlank(apiProduct.getThumbnailUrl()));
            if (apiProduct.getApiSecurity() != null) {
                productDto.setSecurityScheme(Arrays.asList(apiProduct.getApiSecurity().split(",")));
            }
            if (apiProduct.getCreatedTime() != null) {
                productDto.setCreatedTime(String.valueOf(apiProduct.getCreatedTime().getTime()));
            }
            if (apiProduct.getLastUpdated() != null) {
                productDto.setUpdatedTime(String.valueOf(apiProduct.getLastUpdated().getTime()));
            }
            if (apiProduct.getAudiences() != null) {
                productDto.setAudiences(new ArrayList<>(apiProduct.getAudiences()));
            }
            productDto.setBusinessOwner(apiProduct.getBusinessOwner());
            productDto.setBusinessOwnerEmail(apiProduct.getBusinessOwnerEmail());
            productDto.setTechnicalOwner(apiProduct.getTechnicalOwner());
            productDto.setTechnicalOwnerEmail(apiProduct.getTechnicalOwnerEmail());
            productDto.setMonetizedInfo(apiProduct.isMonetizationEnabled());
            productDto.setDisplayName(
                    apiProduct.getDisplayName() != null ? apiProduct.getDisplayName() : apiProduct.getId().getName());
            productDto.setEgress(apiProduct.isEgress() == 1);

            list.add(productDto);
        }

        listDto.setList(list);
        listDto.setCount(list.size());
        return listDto;
    }

    /**
     * @deprecated Use {@link #fromAPIProducttoDTO(APIProduct, boolean)} instead.
     */
    @UsedByMigrationClient
    @Deprecated
    public static APIProductDTO fromAPIProducttoDTO(APIProduct product) throws APIManagementException {
        return fromAPIProducttoDTO(product, false);
    }

    /**
     * Converts an APIProduct to APIProductDTO.
     *
     * @param product             APIProduct object
     * @param preserveCredentials Condition to preserve secret parameter values
     * @return APIProductDTO object
     * @throws APIManagementException if an error occurs while converting to dto
     */
    public static APIProductDTO fromAPIProducttoDTO(APIProduct product, boolean preserveCredentials)
            throws APIManagementException {
        if (log.isDebugEnabled()) {
            log.debug(
                    "Converting APIProduct to DTO. Product UUID: " + product.getUuid()
                            + ", preserveCredentials: " + preserveCredentials);
        }

        APIProductDTO productDto = new APIProductDTO();
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        productDto.setName(product.getId().getName());
        productDto.setDisplayName(
                product.getDisplayName() != null ? product.getDisplayName() : product.getId().getName());
        productDto.setDisplayName((product.getDisplayName() != null && !product.getDisplayName().trim().isEmpty())
                ? product.getDisplayName() : product.getId().getName());
        productDto.setProvider(APIUtil.replaceEmailDomainBack(product.getId().getProviderName()));
        productDto.setId(product.getUuid());
        productDto.setVersion(product.getId().getVersion());
        productDto.setDescription(product.getDescription());
        productDto.setApiType(APIProductDTO.ApiTypeEnum.fromValue(APIConstants.AuditLogConstants.API_PRODUCT));
        productDto.setAuthorizationHeader(product.getAuthorizationHeader());
        productDto.setApiKeyHeader(product.getApiKeyHeader());
        productDto.setState(product.getState());
        String context = product.getContextTemplate();
        if (context.endsWith(FORWARD_SLASH + RestApiConstants.API_VERSION_PARAM)) {
            context = context.replace(FORWARD_SLASH + RestApiConstants.API_VERSION_PARAM, EMPTY_STRING);
        }
        productDto.setContext(context);
        productDto.setIsDefaultVersion(product.isDefaultVersion());
        if (product.getGatewayVendor() == null) {
            productDto.setGatewayVendor(APIConstants.WSO2_GATEWAY_ENVIRONMENT);
        } else {
            productDto.setGatewayVendor(product.getGatewayVendor());
        }
        productDto.setHasThumbnail(!StringUtils.isBlank(product.getThumbnailUrl()));

        Set<String> apiTags = product.getTags();
        List<String> tagsToReturn = new ArrayList<>(apiTags);
        productDto.setTags(tagsToReturn);

        productDto.setEnableSchemaValidation(product.isEnabledSchemaValidation());

        productDto.setIsRevision(product.isRevision());
        productDto.setRevisionedApiProductId(product.getRevisionedApiProductId());
        productDto.setRevisionId(product.getRevisionId());
        productDto.setEgress(product.isEgress() == 1);

        if (product.getAudiences() != null) {
            Set<String> audiences = product.getAudiences();
            if (audiences.contains(APIConstants.ALL_AUDIENCES) && audiences.size() > 1) {
                audiences.remove(APIConstants.ALL_AUDIENCES);
            }
            productDto.setAudiences(new ArrayList<>(product.getAudiences()));
        }

        if (APIConstants.ENABLED.equals(product.getResponseCache())) {
            productDto.setResponseCachingEnabled(Boolean.TRUE);
        } else {
            productDto.setResponseCachingEnabled(Boolean.FALSE);
        }

        productDto.setCacheTimeout(product.getCacheTimeout());
        APIProductBusinessInformationDTO businessInformation = new APIProductBusinessInformationDTO();
        businessInformation.setBusinessOwner(product.getBusinessOwner());
        businessInformation.setBusinessOwnerEmail(product.getBusinessOwnerEmail());
        businessInformation.setTechnicalOwner(product.getTechnicalOwner());
        businessInformation.setTechnicalOwnerEmail(product.getTechnicalOwnerEmail());
        productDto.setBusinessInformation(businessInformation);

        APICorsConfigurationDTO apiCorsConfigurationDTO = new APICorsConfigurationDTO();
        CORSConfiguration corsConfiguration = product.getCorsConfiguration();
        if (corsConfiguration == null) {
            corsConfiguration = APIUtil.getDefaultCorsConfiguration();
        }
        apiCorsConfigurationDTO
                .setAccessControlAllowOrigins(corsConfiguration.getAccessControlAllowOrigins());
        apiCorsConfigurationDTO
                .setAccessControlAllowHeaders(corsConfiguration.getAccessControlAllowHeaders());
        apiCorsConfigurationDTO
                .setAccessControlAllowMethods(corsConfiguration.getAccessControlAllowMethods());
        apiCorsConfigurationDTO.setCorsConfigurationEnabled(corsConfiguration.isCorsConfigurationEnabled());
        apiCorsConfigurationDTO.setAccessControlAllowCredentials(corsConfiguration.isAccessControlAllowCredentials());
        productDto.setCorsConfiguration(apiCorsConfigurationDTO);

        productDto.setWorkflowStatus(product.getWorkflowStatus());

        //Aggregate API resources to each relevant API.
        Map<String, ProductAPIDTO> aggregatedAPIs = new HashMap<String, ProductAPIDTO>();
        List<APIProductResource> resources = product.getProductResources();
        for (APIProductResource apiProductResource : resources) {
            String uuid = apiProductResource.getApiId();
            if (aggregatedAPIs.containsKey(uuid)) {
                ProductAPIDTO productAPI = aggregatedAPIs.get(uuid);
                URITemplate template = apiProductResource.getUriTemplate();
                List<APIOperationsDTO> operations = productAPI.getOperations();
                APIOperationsDTO operation = getOperationFromURITemplate(template, uuid, preserveCredentials);
                operations.add(operation);
            } else {
                ProductAPIDTO productAPI = new ProductAPIDTO();
                productAPI.setApiId(uuid);
                productAPI.setName(apiProductResource.getApiName());
                productAPI.setVersion(apiProductResource.getApiIdentifier().getVersion());
                List<APIOperationsDTO> operations = new ArrayList<APIOperationsDTO>();
                URITemplate template = apiProductResource.getUriTemplate();

                APIOperationsDTO operation = getOperationFromURITemplate(template, uuid, preserveCredentials);
                operations.add(operation);

                productAPI.setOperations(operations);
                aggregatedAPIs.put(uuid, productAPI);
            }
        }
        productDto.setApis(new ArrayList<>(aggregatedAPIs.values()));
        String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(product.getId()
                .getProviderName()));
        String apiSwaggerDefinition = apiProvider.getOpenAPIDefinition(product.getUuid(), tenantDomain);
        List<ScopeDTO> scopeDTOS = getScopesFromSwagger(apiSwaggerDefinition);
        productDto.setScopes(getAPIScopesFromScopeDTOs(scopeDTOS));

        String subscriptionAvailability = product.getSubscriptionAvailability();
        if (subscriptionAvailability != null) {
            productDto.setSubscriptionAvailability(
                    mapSubscriptionAvailabilityFromAPIProducttoDTO(subscriptionAvailability));
        }

        if (product.getSubscriptionAvailableTenants() != null) {
            productDto.setSubscriptionAvailableTenants(
                    Arrays.asList(product.getSubscriptionAvailableTenants().split(",")));
        }

        Set<org.wso2.carbon.apimgt.api.model.Tier> apiTiers = product.getAvailableTiers();
        List<String> tiersToReturn = new ArrayList<>();
        for (org.wso2.carbon.apimgt.api.model.Tier tier : apiTiers) {
            tiersToReturn.add(tier.getName());
        }
        productDto.setPolicies(tiersToReturn);

        productDto.setApiThrottlingPolicy(product.getProductLevelPolicy());

        if (product.getVisibility() != null) {
            productDto.setVisibility(mapVisibilityFromAPIProducttoDTO(product.getVisibility()));
        }

        if (product.getVisibleRoles() != null) {
            productDto.setVisibleRoles(Arrays.asList(product.getVisibleRoles().split(",")));
        }

        if (product.getVisibleTenants() != null) {
            productDto.setVisibleTenants(Arrays.asList(product.getVisibleTenants().split(",")));
        }

        productDto.setAccessControl(APIConstants.API_RESTRICTED_VISIBILITY.equals(product.getAccessControl()) ?
                APIProductDTO.AccessControlEnum.RESTRICTED :
                APIProductDTO.AccessControlEnum.NONE);
        if (product.getAccessControlRoles() != null) {
            productDto.setAccessControlRoles(Arrays.asList(product.getAccessControlRoles().split(",")));
        }

        if (StringUtils.isEmpty(product.getTransports())) {
            List<String> transports = new ArrayList<>();
            transports.add(APIConstants.HTTPS_PROTOCOL);

            productDto.setTransport(transports);
        } else {
            productDto.setTransport(Arrays.asList(product.getTransports().split(",")));
        }

        if (product.getAdditionalProperties() != null) {
            JSONObject additionalProperties = product.getAdditionalProperties();
            List<APIInfoAdditionalPropertiesDTO> additionalPropertiesList = new ArrayList<>();
            Map<String, APIInfoAdditionalPropertiesMapDTO> additionalPropertiesMap = new HashMap<>();
            for (Object propertyKey : additionalProperties.keySet()) {
                APIInfoAdditionalPropertiesDTO additionalPropertiesDTO = new APIInfoAdditionalPropertiesDTO();
                APIInfoAdditionalPropertiesMapDTO apiInfoAdditionalPropertiesMapDTO =
                        new APIInfoAdditionalPropertiesMapDTO();
                String key = (String) propertyKey;
                int index = key.lastIndexOf(APIConstants.API_RELATED_CUSTOM_PROPERTIES_SURFIX);
                additionalPropertiesDTO.setValue((String) additionalProperties.get(key));
                apiInfoAdditionalPropertiesMapDTO.setValue((String) additionalProperties.get(key));
                if (index > 0) {
                    additionalPropertiesDTO.setName(key.substring(0, index));
                    apiInfoAdditionalPropertiesMapDTO.setName(key.substring(0, index));
                    additionalPropertiesDTO.setDisplay(true);
                } else {
                    additionalPropertiesDTO.setName(key);
                    apiInfoAdditionalPropertiesMapDTO.setName(key);
                    additionalPropertiesDTO.setDisplay(false);
                }
                apiInfoAdditionalPropertiesMapDTO.setDisplay(false);
                additionalPropertiesMap.put(key, apiInfoAdditionalPropertiesMapDTO);
                additionalPropertiesList.add(additionalPropertiesDTO);
            }
            productDto.setAdditionalPropertiesMap(additionalPropertiesMap);
            productDto.setAdditionalProperties(additionalPropertiesList);
        }
        if (product.getApiSecurity() != null) {
            productDto.setSecurityScheme(Arrays.asList(product.getApiSecurity().split(",")));
        }

        List<APICategory> apiCategories = product.getApiCategories();
        List<String> categoryNameList = new ArrayList<>();
        if (apiCategories != null && !apiCategories.isEmpty()) {
            for (APICategory category : apiCategories) {
                categoryNameList.add(category.getName());
            }
        }
        productDto.setCategories(categoryNameList);

        if (null != product.getLastUpdated()) {
            Date lastUpdateDate = product.getLastUpdated();
            Timestamp timeStamp = new Timestamp(lastUpdateDate.getTime());
            productDto.setLastUpdatedTime(String.valueOf(timeStamp));
            productDto.setLastUpdatedTimestamp(String.valueOf(timeStamp.getTime()));
        }
        if (null != product.getCreatedTime()) {
            Date createdTime = product.getCreatedTime();
            Timestamp timeStamp = new Timestamp(createdTime.getTime());
            productDto.setCreatedTime(String.valueOf(timeStamp.getTime()));
        }

        return productDto;
    }

    /**
     * This method converts OrganizationTiers object to OrganizationPoliciesDTO.
     * @param organizationTiers OrganizationTiers object
     * @return
     */
    private static OrganizationPoliciesDTO getOrganizationPoliciesDTO(OrganizationTiers organizationTiers) {
        OrganizationPoliciesDTO organizationPoliciesDTO = new OrganizationPoliciesDTO();
        Set<Tier> apiTiers = organizationTiers.getTiers();
        List<String> tiersToReturn = new ArrayList<>();
        for (Tier tier : apiTiers) {
            tiersToReturn.add(tier.getName());
        }
        organizationPoliciesDTO.setPolicies(tiersToReturn);
        organizationPoliciesDTO.setOrganizationID(organizationTiers.getOrganizationID());
        return organizationPoliciesDTO;
    }

    private static APIProductDTO.SubscriptionAvailabilityEnum mapSubscriptionAvailabilityFromAPIProducttoDTO(
            String subscriptionAvailability) {

        switch (subscriptionAvailability) {
            case APIConstants.SUBSCRIPTION_TO_CURRENT_TENANT:
                return APIProductDTO.SubscriptionAvailabilityEnum.CURRENT_TENANT;
            case APIConstants.SUBSCRIPTION_TO_ALL_TENANTS:
                return APIProductDTO.SubscriptionAvailabilityEnum.ALL_TENANTS;
            case APIConstants.SUBSCRIPTION_TO_SPECIFIC_TENANTS:
                return APIProductDTO.SubscriptionAvailabilityEnum.SPECIFIC_TENANTS;
            default:
                return null; // how to handle this?
        }

    }

    private static APIProductDTO.VisibilityEnum mapVisibilityFromAPIProducttoDTO(String visibility) {

        switch (visibility) { //public, private,controlled, restricted
            case APIConstants.API_GLOBAL_VISIBILITY:
                return APIProductDTO.VisibilityEnum.PUBLIC;
            case APIConstants.API_PRIVATE_VISIBILITY:
                return APIProductDTO.VisibilityEnum.PRIVATE;
            case APIConstants.API_RESTRICTED_VISIBILITY:
                return APIProductDTO.VisibilityEnum.RESTRICTED;
            default:
                return null; // how to handle this?
        }
    }

    @UsedByMigrationClient
    public static APIProduct fromDTOtoAPIProduct(APIProductDTO dto, String provider)
            throws APIManagementException {

        APIProduct product = new APIProduct();
        APIProductIdentifier id = new APIProductIdentifier(APIUtil.replaceEmailDomain(provider), dto.getName(),
                dto.getVersion());
        product.setID(id);
        product.setUuid(dto.getId());
        product.setDescription(dto.getDescription());
        product.setDisplayName((dto.getDisplayName() != null && !dto.getDisplayName().trim().isEmpty())
                ? dto.getDisplayName() : dto.getName());

        String context = dto.getContext();
        final String originalContext = context;

        if (context.endsWith(FORWARD_SLASH + RestApiConstants.API_VERSION_PARAM)) {
            context = context.replace(FORWARD_SLASH + RestApiConstants.API_VERSION_PARAM, EMPTY_STRING);
        }

        context = context.startsWith(FORWARD_SLASH) ? context : (FORWARD_SLASH + context);
        String providerDomain = MultitenantUtils.getTenantDomain(provider);
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(providerDomain) && dto.getId() == null
                && !context.contains("/t/" + providerDomain)) {
            //Create tenant aware context for API
            context = "/t/" + providerDomain + context;
        }

        // This is to support the pluggable version strategy
        // if the context does not contain any {version} segment, we use the default version strategy.
        context = checkAndSetVersionParam(context);
        product.setContextTemplate(context);

        context = updateContextWithVersion(dto.getVersion(), originalContext, context);
        product.setContext(context);

        product.setType(APIConstants.API_PRODUCT_IDENTIFIER_TYPE.replaceAll("\\s", EMPTY_STRING));
        if (dto.isIsDefaultVersion() != null) {
            product.setDefaultVersion(dto.isIsDefaultVersion());
        }

        List<String> apiProductTags = dto.getTags();
        Set<String> tagsToReturn = new HashSet<>(apiProductTags);
        product.addTags(tagsToReturn);

        if (dto.isEnableSchemaValidation() != null) {
            product.setEnableSchemaValidation(dto.isEnableSchemaValidation());
        }
        product.setEnableStore(true);
        if (dto.isResponseCachingEnabled() != null && dto.isResponseCachingEnabled()) {
            product.setResponseCache(APIConstants.ENABLED);
        } else {
            product.setResponseCache(APIConstants.DISABLED);
        }
        if (dto.getCacheTimeout() != null) {
            product.setCacheTimeout(dto.getCacheTimeout());
        } else {
            product.setCacheTimeout(APIConstants.API_RESPONSE_CACHE_TIMEOUT);
        }

        if (dto.getBusinessInformation() != null) {
            product.setBusinessOwner(dto.getBusinessInformation().getBusinessOwner());
            product.setBusinessOwnerEmail(dto.getBusinessInformation().getBusinessOwnerEmail());
            product.setTechnicalOwner(dto.getBusinessInformation().getTechnicalOwner());
            product.setTechnicalOwnerEmail(dto.getBusinessInformation().getTechnicalOwnerEmail());
        }

        if (dto.getAudiences() != null) {
            List<String> audiences = dto.getAudiences();
            if (audiences.isEmpty()) {
                audiences.add(APIConstants.ALL_AUDIENCES);
            }
            product.setAudiences(new HashSet<>(audiences));
        }

        if (dto.getVisibility() != null) {
            product.setVisibility(mapVisibilityFromDTOtoAPIProduct(dto.getVisibility()));
        }
        if (dto.getVisibleRoles() != null) {
            String visibleRoles = StringUtils.join(dto.getVisibleRoles(), ',');
            product.setVisibleRoles(visibleRoles);
        }
        if (dto.getVisibleTenants() != null) {
            String visibleTenants = StringUtils.join(dto.getVisibleTenants(), ',');
            product.setVisibleTenants(visibleTenants);
        }

        List<String> accessControlRoles = dto.getAccessControlRoles();
        if (accessControlRoles == null || accessControlRoles.isEmpty()) {
            product.setAccessControl(APIConstants.NO_ACCESS_CONTROL);
            product.setAccessControlRoles("null");
        } else {
            product.setAccessControlRoles(StringUtils.join(accessControlRoles, ',').toLowerCase());
            product.setAccessControl(APIConstants.API_RESTRICTED_VISIBILITY);
        }

        // Set Tiers without considering organizations
        Set<Tier> apiTiers = new HashSet<>();
        List<String> tiersFromDTO = dto.getPolicies();
        for (String tier : tiersFromDTO) {
            apiTiers.add(new Tier(tier));
        }
        product.setAvailableTiers(apiTiers);
        product.setProductLevelPolicy(dto.getApiThrottlingPolicy());

        product.setGatewayVendor(dto.getGatewayVendor());

        if (dto.getSubscriptionAvailability() != null) {
            product.setSubscriptionAvailability(
                    mapSubscriptionAvailabilityFromDTOtoAPIProduct(dto.getSubscriptionAvailability()));
        }

        List<APIInfoAdditionalPropertiesDTO> additionalProperties = dto.getAdditionalProperties();
        if (additionalProperties != null) {
            for (APIInfoAdditionalPropertiesDTO property : additionalProperties) {
                if (property.isDisplay()) {
                    product.addProperty(property.getName() + APIConstants.API_RELATED_CUSTOM_PROPERTIES_SURFIX, property
                            .getValue());
                } else {
                    product.addProperty(property.getName(), property.getValue());
                }
            }
        }
        if (dto.getSubscriptionAvailableTenants() != null) {
            product.setSubscriptionAvailableTenants(StringUtils.join(dto.getSubscriptionAvailableTenants(), ","));
        }

        String transports = StringUtils.join(dto.getTransport(), ',');
        product.setTransports(transports);


        List<APIProductResource> productResources = new ArrayList<APIProductResource>();

        Set<String> verbResourceCombo = new HashSet<>();
        for (ProductAPIDTO res : dto.getApis()) {
            List<APIOperationsDTO> productAPIOperationsDTO = res.getOperations();
            for (APIOperationsDTO resourceItem : productAPIOperationsDTO) {

                if (!verbResourceCombo.add(resourceItem.getVerb() + resourceItem.getTarget())) {
                    throw new APIManagementException("API Product resource: " + resourceItem.getTarget() +
                            ", with verb: " + resourceItem.getVerb() + " , is duplicated for id " + id,
                            ExceptionCodes.from(ExceptionCodes.API_PRODUCT_DUPLICATE_RESOURCE,
                                    resourceItem.getTarget(), resourceItem.getVerb()));
                }

                URITemplate template = new URITemplate();
                template.setHTTPVerb(resourceItem.getVerb());
                template.setHttpVerbs(resourceItem.getVerb());
                template.setResourceURI(resourceItem.getTarget());
                template.setUriTemplate(resourceItem.getTarget());
                template.setOperationPolicies(OperationPolicyMappingUtil
                        .fromDTOToAPIOperationPoliciesList(resourceItem.getOperationPolicies()));

                APIProductResource resource = new APIProductResource();
                resource.setApiId(res.getApiId());
                resource.setUriTemplate(template);
                productResources.add(resource);
            }
        }

        Set<Scope> scopes = getScopes(dto);
        product.setScopes(scopes);

        APICorsConfigurationDTO apiCorsConfigurationDTO = dto.getCorsConfiguration();
        CORSConfiguration corsConfiguration;
        if (apiCorsConfigurationDTO != null) {
            corsConfiguration =
                    new CORSConfiguration(apiCorsConfigurationDTO.isCorsConfigurationEnabled(),
                            apiCorsConfigurationDTO.getAccessControlAllowOrigins(),
                            apiCorsConfigurationDTO.isAccessControlAllowCredentials(),
                            apiCorsConfigurationDTO.getAccessControlAllowHeaders(),
                            apiCorsConfigurationDTO.getAccessControlAllowMethods());

        } else {
            corsConfiguration = APIUtil.getDefaultCorsConfiguration();
        }
        product.setCorsConfiguration(corsConfiguration);

        product.setProductResources(productResources);
        product.setApiSecurity(getSecurityScheme(dto.getSecurityScheme()));
        product.setAuthorizationHeader(dto.getAuthorizationHeader());
        product.setApiKeyHeader(dto.getApiKeyHeader());
        product.setEgress(dto.isEgress() ? 1 : 0);
        if (product.getApiKeyHeader() == null) {
            product.setApiKeyHeader(APIConstants.API_KEY_HEADER_DEFAULT);
        }

        //attach api categories to API model
        setAPICategoriesToModel(dto, product, provider);
        return product;
    }

    private static String mapVisibilityFromDTOtoAPIProduct(APIProductDTO.VisibilityEnum visibility) {

        switch (visibility) {
            case PUBLIC:
                return APIConstants.API_GLOBAL_VISIBILITY;
            case PRIVATE:
                return APIConstants.API_PRIVATE_VISIBILITY;
            case RESTRICTED:
                return APIConstants.API_RESTRICTED_VISIBILITY;
            default:
                return null; // how to handle this?
        }
    }

    private static String mapSubscriptionAvailabilityFromDTOtoAPIProduct(
            APIProductDTO.SubscriptionAvailabilityEnum subscriptionAvailability) {

        switch (subscriptionAvailability) {
            case CURRENT_TENANT:
                return APIConstants.SUBSCRIPTION_TO_CURRENT_TENANT;
            case ALL_TENANTS:
                return APIConstants.SUBSCRIPTION_TO_ALL_TENANTS;
            case SPECIFIC_TENANTS:
                return APIConstants.SUBSCRIPTION_TO_SPECIFIC_TENANTS;
            default:
                return APIConstants.SUBSCRIPTION_TO_CURRENT_TENANT; // default to current tenant
        }

    }

    /**
     * Converts a List object of API resource paths into a DTO.
     *
     * @param resourcePathList List of API resource paths
     * @param limit            maximum number of API resource paths to be returned
     * @param offset           starting index
     * @return ResourcePathListDTO object containing ResourcePathDTOs
     */
    public static ResourcePathListDTO fromResourcePathListToDTO(List<ResourcePath> resourcePathList, int limit,
                                                                int offset) {

        ResourcePathListDTO resourcePathListDTO = new ResourcePathListDTO();
        List<ResourcePathDTO> resourcePathDTOs = new ArrayList<ResourcePathDTO>();

        //identifying the proper start and end indexes
        int size = resourcePathList.size();
        int start = offset < size && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = offset + limit - 1 <= size - 1 ? offset + limit - 1 : size - 1;

        for (int i = start; i <= end; i++) {
            ResourcePath path = resourcePathList.get(i);
            ResourcePathDTO dto = new ResourcePathDTO();
            dto.setId(path.getId());
            dto.setResourcePath(path.getResourcePath());
            dto.setHttpVerb(path.getHttpVerb());
            resourcePathDTOs.add(dto);
        }

        resourcePathListDTO.setCount(resourcePathDTOs.size());
        resourcePathListDTO.setList(resourcePathDTOs);
        return resourcePathListDTO;
    }

    /**
     * Sets pagination urls for a ResourcePathListDTO object.
     *
     * @param resourcePathListDTO ResourcePathListDTO object to which pagination urls need to be set
     * @param offset              starting index
     * @param limit               max number of returned objects
     * @param size                max offset
     */
    public static void setPaginationParamsForAPIResourcePathList(ResourcePathListDTO resourcePathListDTO, int offset,
                                                                 int limit, int size) {
        //acquiring pagination parameters and setting pagination urls
        Map<String, Integer> paginatedParams = RestApiCommonUtil.getPaginationParams(offset, limit, size);
        String paginatedPrevious = EMPTY_STRING;
        String paginatedNext = EMPTY_STRING;

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = RestApiCommonUtil
                    .getResourcePathPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT));
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = RestApiCommonUtil
                    .getResourcePathPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT));
        }

        PaginationDTO paginationDTO = CommonMappingUtil
                .getPaginationDTO(limit, offset, size, paginatedNext, paginatedPrevious);
        resourcePathListDTO.setPagination(paginationDTO);
    }

    /**
     * Sets pagination urls for a APIProductListDTO object given pagination parameters and url parameters.
     *
     * @param apiProductListDTO a APIProductListDTO object
     * @param query             search condition
     * @param limit             max number of objects returned
     * @param offset            starting index
     * @param size              max offset
     */
    public static void setPaginationParams(APIProductListDTO apiProductListDTO, String query, int offset, int limit,
                                           int size) {

        //acquiring pagination parameters and setting pagination urls
        Map<String, Integer> paginatedParams = RestApiCommonUtil.getPaginationParams(offset, limit, size);
        String paginatedPrevious = EMPTY_STRING;
        String paginatedNext = EMPTY_STRING;

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = RestApiCommonUtil
                    .getAPIProductPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT), query);
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = RestApiCommonUtil
                    .getAPIProductPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT), query);
        }

        PaginationDTO paginationDTO = CommonMappingUtil
                .getPaginationDTO(limit, offset, size, paginatedNext, paginatedPrevious);
        apiProductListDTO.setPagination(paginationDTO);
    }

    /**
     * Returns the APIProductIdentifier given the uuid.
     *
     * @param productId             API Product uuid
     * @param requestedTenantDomain tenant domain of the API
     * @return APIProductIdentifier which represents the given id
     * @throws APIManagementException
     */
    public static APIProductIdentifier getAPIProductIdentifierFromUUID(String productId, String requestedTenantDomain)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        APIProduct product = apiProvider.getAPIProductbyUUID(productId, requestedTenantDomain);
        return product.getId();
    }

    /**
     * Converts a WSDL validation response model to DTO.
     *
     * @param validationResponse validation response model
     * @return Converted WSDL validation response model to DTO
     */
    public static WSDLValidationResponseDTO fromWSDLValidationResponseToDTO(WSDLValidationResponse validationResponse) {

        WSDLValidationResponseDTO wsdlValidationResponseDTO = new WSDLValidationResponseDTO();
        WSDLInfo wsdlInfo;
        if (validationResponse.isValid()) {
            wsdlValidationResponseDTO.setIsValid(true);
            wsdlInfo = validationResponse.getWsdlInfo();
            WSDLValidationResponseWsdlInfoDTO wsdlInfoDTO = new WSDLValidationResponseWsdlInfoDTO();
            wsdlInfoDTO.setVersion(wsdlInfo.getVersion());
            List<WSDLValidationResponseWsdlInfoEndpointsDTO> endpointsDTOList =
                    fromEndpointsMapToWSDLValidationResponseEndpointsDTO(wsdlInfo.getEndpoints());
            wsdlInfoDTO.setEndpoints(endpointsDTOList);
            wsdlValidationResponseDTO.setWsdlInfo(wsdlInfoDTO);
        } else {
            wsdlValidationResponseDTO.setIsValid(false);
            wsdlValidationResponseDTO.setErrors(getErrorListItemsDTOsFromErrorHandler(validationResponse.getError()));
        }
        return wsdlValidationResponseDTO;
    }

    /**
     * Converts the provided WSDL endpoint map to REST API DTO.
     *
     * @param endpoints endpoint map
     * @return converted map to DTO
     */
    private static List<WSDLValidationResponseWsdlInfoEndpointsDTO>
    fromEndpointsMapToWSDLValidationResponseEndpointsDTO(Map<String, String> endpoints) {

        List<WSDLValidationResponseWsdlInfoEndpointsDTO> endpointsDTOList = new ArrayList<>();
        for (String endpointName : endpoints.keySet()) {
            WSDLValidationResponseWsdlInfoEndpointsDTO endpointDTO = new WSDLValidationResponseWsdlInfoEndpointsDTO();
            endpointDTO.setName(endpointName);
            endpointDTO.setLocation(endpoints.get(endpointName));
            endpointsDTOList.add(endpointDTO);
        }
        return endpointsDTOList;
    }

    /**
     * Extract scopes from the swagger.
     *
     * @param swagger swagger document
     * @return list of scopes
     * @throws APIManagementException throw if parsing exception occur
     */
    private static List<ScopeDTO> getScopesFromSwagger(String swagger) throws APIManagementException {

        APIDefinition apiDefinition = OASParserUtil.getOASParser(swagger);
        Set<Scope> scopes = apiDefinition.getScopes(swagger);
        List<ScopeDTO> scopeDTOS = new ArrayList<>();
        for (Scope aScope : scopes) {
            ScopeDTO scopeDTO = new ScopeDTO();
            scopeDTO.setName(aScope.getKey());
            scopeDTO.setDisplayName(aScope.getName());
            scopeDTO.setDescription(aScope.getDescription());
            String roles = aScope.getRoles();
            if (roles == null || roles.isEmpty()) {
                scopeDTO.setBindings(Collections.emptyList());
            } else {
                scopeDTO.setBindings(Arrays.asList((roles).split(",")));
            }
            scopeDTOS.add(scopeDTO);
        }
        return scopeDTOS;
    }

    /**
     * Convert ScopeDTO List to APIScopesDTO List adding the attribute 'isShared'.
     *
     * @param scopeDTOS ScopeDTO List
     * @return APIScopeDTO List
     * @throws APIManagementException if an error occurs while converting ScopeDTOs to APIScopeDTOs
     */
    private static List<APIScopeDTO> getAPIScopesFromScopeDTOs(List<ScopeDTO> scopeDTOS) throws APIManagementException {

        List<APIScopeDTO> apiScopeDTOS = new ArrayList<>();
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        Set<String> allSharedScopeKeys = apiProvider.getAllSharedScopeKeys(tenantDomain);
        scopeDTOS.forEach(scopeDTO -> {
            APIScopeDTO apiScopeDTO = new APIScopeDTO();
            apiScopeDTO.setScope(scopeDTO);
            apiScopeDTO.setShared(allSharedScopeKeys.contains(scopeDTO.getName()) ? Boolean.TRUE : Boolean.FALSE);
            apiScopeDTOS.add(apiScopeDTO);
        });
        return apiScopeDTOS;
    }

    /**
     * Convert ScopeDTO List to APIScopesDTO List adding the attribute 'isShared'.
     *
     * @param scopeDTOS ScopeDTO List
     * @return APIScopeDTO List
     * @throws APIManagementException if an error occurs while converting ScopeDTOs to APIScopeDTOs
     */
    private static List<APIScopeDTO> getAPIScopesFromScopeDTOs(List<ScopeDTO> scopeDTOS, APIProvider apiProvider)
            throws APIManagementException {

        List<APIScopeDTO> apiScopeDTOS = new ArrayList<>();
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        Set<String> allSharedScopeKeys = apiProvider.getAllSharedScopeKeys(tenantDomain);
        scopeDTOS.forEach(scopeDTO -> {
            APIScopeDTO apiScopeDTO = new APIScopeDTO();
            apiScopeDTO.setScope(scopeDTO);
            apiScopeDTO.setShared(allSharedScopeKeys.contains(scopeDTO.getName()) ? Boolean.TRUE : Boolean.FALSE);
            apiScopeDTOS.add(apiScopeDTO);
        });
        return apiScopeDTOS;
    }

    private static List<MCPServerScopeDTO> getMCPServerScopesFromScopeDTOs(List<ScopeDTO> scopeDTOS,
                                                                           APIProvider apiProvider)
            throws APIManagementException {

        List<MCPServerScopeDTO> mcpServerScopeDTOS = new ArrayList<>();
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        Set<String> allSharedScopeKeys = apiProvider.getAllSharedScopeKeys(tenantDomain);
        scopeDTOS.forEach(scopeDTO -> {
            MCPServerScopeDTO apiScopeDTO = new MCPServerScopeDTO();
            apiScopeDTO.setScope(scopeDTO);
            apiScopeDTO.setShared(allSharedScopeKeys.contains(scopeDTO.getName()) ? Boolean.TRUE : Boolean.FALSE);
            mcpServerScopeDTOS.add(apiScopeDTO);
        });
        return mcpServerScopeDTOS;
    }

    public static APIIdentifier getAPIIdentifierFromApiId(String apiId) throws APIManagementException {
        //if apiId contains -AT-, that need to be replaced before splitting
        apiId = APIUtil.replaceEmailDomainBack(apiId);
        String[] apiIdDetails = apiId.split(RestApiConstants.API_ID_DELIMITER);

        if (apiIdDetails.length < 3) {
            throw new APIManagementException("Provided API identifier '" + apiId + "' is invalid",
                    ExceptionCodes.from(ExceptionCodes.INVALID_API_IDENTIFIER, apiId));
        }

        // apiId format: provider-apiName-version
        String providerName = null;
        try {
            providerName = URLDecoder.decode(apiIdDetails[0], "UTF-8");
        } catch (UnsupportedEncodingException e) {
            String errorMsg = "Couldn't decode value providerName: " + providerName;
            throw new APIManagementException(errorMsg, e);
        }
        String apiName = null;
        try {
            apiName = URLDecoder.decode(apiIdDetails[1], "UTF-8");
        } catch (UnsupportedEncodingException e) {
            String errorMsg = "Couldn't decode value apiName : " + apiName;
            throw new APIManagementException(errorMsg, e);
        }
        String version = null;
        try {
            version = URLDecoder.decode(apiIdDetails[2], "UTF-8");
        } catch (UnsupportedEncodingException e) {
            String errorMsg = "Couldn't decode value version : " + version;
            throw new APIManagementException(errorMsg, e);
        }
        String providerNameEmailReplaced = APIUtil.replaceEmailDomain(providerName);
        return new APIIdentifier(providerNameEmailReplaced, apiName, version);
    }

    /**
     * Returns the API given the uuid or the id in {provider}-{api}-{version} format.
     *
     * @param apiId                 uuid or the id in {provider}-{api}-{version} format
     * @param organization organization of the API
     * @return API which represents the given id
     * @throws APIManagementException
     */
    public static API getAPIFromApiIdOrUUID(String apiId, String organization)
            throws APIManagementException {

        API api;
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        api = apiProvider.getAPIbyUUID(apiId, organization);
        return api;
    }

    /**
     * Returns shared status of the mediation policy.
     *
     * @param policyName   mediation sequence name
     * @param resourcePath registry resource path
     * @return true, if the mediation policy is a shared resource(global policy)
     */
    private static boolean getSharedStatus(String resourcePath, String policyName) {

        if (null != resourcePath && resourcePath.contains(APIConstants.API_CUSTOM_SEQUENCE_LOCATION)) {
            return true;
        }
        return false;
    }

    /**
     * Set API categories to API or APIProduct based on the instance type of the DTO object passes.
     *
     * @param dto   APIDTO or APIProductDTO
     * @param model API or APIProduct
     */
    private static void setAPICategoriesToModel(Object dto, Object model, String provider) {

        List<String> apiCategoryNames = new ArrayList<>();
        if (dto instanceof APIDTO) {
            APIDTO apiDTO = (APIDTO) dto;
            apiCategoryNames = apiDTO.getCategories();
        } else if (dto instanceof  MCPServerDTO) {
            MCPServerDTO mcpServerDTO = (MCPServerDTO) dto;
            apiCategoryNames = mcpServerDTO.getCategories();
         } else {
            APIProductDTO apiProductDTO = (APIProductDTO) dto;
            apiCategoryNames = apiProductDTO.getCategories();
        }
        List<APICategory> apiCategories = new ArrayList<>();
        for (String categoryName : apiCategoryNames) {
            APICategory category = new APICategory();
            category.setName(categoryName);
            apiCategories.add(category);
        }
        if (model instanceof API) {
            ((API) model).setApiCategories(apiCategories);
        } else {
            ((APIProduct) model).setApiCategories(apiCategories);
        }
    }

    private static JSONObject handleEndpointSecurity(API api, JSONObject endpointSecurity, boolean preserveCredentials)
            throws APIManagementException {

        String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(api.getId()
                .getProviderName()));
        if (checkEndpointSecurityPasswordEnabled(tenantDomain) || preserveCredentials) {
            return endpointSecurity;
        }
        return handleEndpointSecurity(endpointSecurity);
    }

    private static JSONObject handleEndpointSecurity(JSONObject endpointSecurity, String organization,
            boolean preserveCredentials) throws APIManagementException {
        if (checkEndpointSecurityPasswordEnabled(organization) || preserveCredentials) {
            return endpointSecurity;
        }
        return handleEndpointSecurity(endpointSecurity);
    }

    private static JSONObject handleEndpointSecurity(JSONObject endpointSecurity) throws APIManagementException {

        try {
            JSONObject endpointSecurityElement = new JSONObject();
            endpointSecurityElement.putAll(endpointSecurity);
            if (endpointSecurityElement.get(APIConstants.ENDPOINT_SECURITY_SANDBOX) != null) {
                JSONObject sandboxEndpointSecurity = new JSONObject(
                        (Map) endpointSecurityElement.get(APIConstants.ENDPOINT_SECURITY_SANDBOX));
                if (APIConstants.ENDPOINT_SECURITY_TYPE_OAUTH.equalsIgnoreCase((String) sandboxEndpointSecurity
                        .get(APIConstants.ENDPOINT_SECURITY_TYPE))) {
                    sandboxEndpointSecurity.put(APIConstants.ENDPOINT_SECURITY_CLIENT_SECRET, StringUtils.EMPTY);
                }
                if (sandboxEndpointSecurity.get(APIConstants.ENDPOINT_SECURITY_PASSWORD) != null) {
                    sandboxEndpointSecurity.put(APIConstants.ENDPOINT_SECURITY_PASSWORD, StringUtils.EMPTY);
                }
                if (sandboxEndpointSecurity.get(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE) != null) {
                    sandboxEndpointSecurity.put(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE, StringUtils.EMPTY);
                }
                if (sandboxEndpointSecurity.get(APIConstants.ENDPOINT_SECURITY_AWS_SECRET_KEY) != null) {
                    sandboxEndpointSecurity.put(APIConstants.ENDPOINT_SECURITY_AWS_SECRET_KEY, StringUtils.EMPTY);
                }
                Object customParamsObj =
                        sandboxEndpointSecurity.get(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS);
                if (customParamsObj != null) {
                    if (customParamsObj instanceof String) {
                        customParamsObj = new JSONParser().parse(customParamsObj.toString());
                    } else if (customParamsObj instanceof Map) {
                        customParamsObj = new JSONObject((Map) customParamsObj);
                    }
                    maskSecretCustomParameters((JSONObject) customParamsObj);
                    sandboxEndpointSecurity.put(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS,
                            customParamsObj);
                }
                endpointSecurityElement.put(APIConstants.ENDPOINT_SECURITY_SANDBOX, sandboxEndpointSecurity);
            }
            if (endpointSecurityElement.get(APIConstants.ENDPOINT_SECURITY_PRODUCTION) != null) {
                JSONObject productionEndpointSecurity = new JSONObject(
                        (Map) endpointSecurityElement.get(APIConstants.ENDPOINT_SECURITY_PRODUCTION));
                if (APIConstants.ENDPOINT_SECURITY_TYPE_OAUTH.equalsIgnoreCase((String) productionEndpointSecurity
                        .get(APIConstants.ENDPOINT_SECURITY_TYPE))) {
                    productionEndpointSecurity.put(APIConstants.ENDPOINT_SECURITY_CLIENT_SECRET, StringUtils.EMPTY);
                }
                if (productionEndpointSecurity.get(APIConstants.ENDPOINT_SECURITY_PASSWORD) != null) {
                    productionEndpointSecurity.put(APIConstants.ENDPOINT_SECURITY_PASSWORD, StringUtils.EMPTY);
                }
                if (productionEndpointSecurity.get(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE) != null) {
                    productionEndpointSecurity.put(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE, StringUtils.EMPTY);
                }
                if (productionEndpointSecurity.get(APIConstants.ENDPOINT_SECURITY_AWS_SECRET_KEY) != null) {
                    productionEndpointSecurity.put(APIConstants.ENDPOINT_SECURITY_AWS_SECRET_KEY, StringUtils.EMPTY);
                }
                Object customParamsObj =
                        productionEndpointSecurity.get(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS);
                if (customParamsObj != null) {
                    if (customParamsObj instanceof String) {
                        customParamsObj = new JSONParser().parse(customParamsObj.toString());
                    } else if (customParamsObj instanceof Map) {
                        customParamsObj = new JSONObject((Map) customParamsObj);
                    }
                    maskSecretCustomParameters((JSONObject) customParamsObj);
                    productionEndpointSecurity.put(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS,
                            customParamsObj);
                }
                endpointSecurityElement.put(APIConstants.ENDPOINT_SECURITY_PRODUCTION, productionEndpointSecurity);
            }
            return endpointSecurityElement;
        } catch (ParseException e) {
            String errorMsg = "Error while parsing custom OAuth parameters in endpoint security configuration.";
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e);
        } catch (ClassCastException e) {
            String errorMsg = "Error while handling custom OAuth parameters; expected a JSON object.";
            log.error(errorMsg, e);
            throw new APIManagementException(errorMsg, e);
        }
    }

    /**
     * Decrypts sensitive information in the endpoint security configuration,
     * including client secrets and custom OAuth parameters for sandbox and production endpoints.
     *
     * @param endpointSecurity JSONObject representing the endpoint security configuration.
     * @return JSONObject with decrypted values where applicable.
     */
    private static JSONObject handleEndpointSecurityDecrypt(JSONObject endpointSecurity) throws APIManagementException {

        JSONObject endpointSecurityElement = new JSONObject();
        endpointSecurityElement.putAll(endpointSecurity);
        CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();

        try {
            decryptSecurity(
                    endpointSecurityElement,
                    APIConstants.ENDPOINT_SECURITY_SANDBOX,
                    cryptoUtil
            );

            decryptSecurity(
                    endpointSecurityElement,
                    APIConstants.ENDPOINT_SECURITY_PRODUCTION,
                    cryptoUtil
            );
        } catch (CryptoException e) {
            log.error("Error while decrypting client credentials in endpoint security configuration.", e);
        } catch (ParseException e) {
            log.error("Error while parsing custom OAuth parameters in endpoint security configuration.", e);
        }
        return endpointSecurityElement;
    }

    /**
     * Decrypts sensitive fields in a specific endpoint security section (sandbox or production).
     *
     * @param endpointSecurityElement The main endpoint security JSON object.
     * @param sectionKey              Either 'sandbox' or 'production'.
     * @param cryptoUtil              Utility for cryptographic operations.
     * @throws CryptoException if decryption fails.
     * @throws ParseException  if custom parameters cannot be parsed.
     */
    private static void decryptSecurity(JSONObject endpointSecurityElement, String sectionKey, CryptoUtil cryptoUtil)
            throws CryptoException, ParseException, APIManagementException {

        Object sectionObj = endpointSecurityElement.get(sectionKey);

        // In endpoint retrieval path, the sectionObj is a Map. In API/MCP retrieval path, it is a JSONObject.
        // Using instanceof Map to handle both cases.
        if (sectionObj instanceof Map) {
            @SuppressWarnings("unchecked")
            JSONObject deploymentStage = new JSONObject((Map<String, Object>) sectionObj);
            String apiKeyValue = (String) deploymentStage.get(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE);
            if (StringUtils.isNotEmpty(apiKeyValue)) {
                try {
                    boolean isEncrypted = cryptoUtil.base64DecodeAndIsSelfContainedCipherText(apiKeyValue);
                    String decryptedApiKeyValue;
                    if (isEncrypted) {
                        decryptedApiKeyValue = new String(cryptoUtil.base64DecodeAndDecrypt(apiKeyValue));
                    } else {
                        decryptedApiKeyValue = apiKeyValue;
                    }
                    deploymentStage.put(APIConstants.ENDPOINT_SECURITY_API_KEY_VALUE, decryptedApiKeyValue);
                } catch (CryptoException e) {
                    throw new APIManagementException("Error while decrypting value", e);
                }
            }
            String clientSecret = (String) deploymentStage.get(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET);
            if (StringUtils.isNotEmpty(clientSecret)) {
                deploymentStage.put(APIConstants.OAuthConstants.OAUTH_CLIENT_SECRET,
                        new String(cryptoUtil.base64DecodeAndDecrypt(clientSecret)));
            }
            String awsSecretKeyValue =
                    (String) deploymentStage.get(APIConstants.ENDPOINT_SECURITY_AWS_SECRET_KEY);
            if (StringUtils.isNotEmpty(awsSecretKeyValue)) {
                deploymentStage.put(APIConstants.ENDPOINT_SECURITY_AWS_SECRET_KEY,
                        new String(cryptoUtil.base64DecodeAndDecrypt(awsSecretKeyValue)));
            }

            // In the API/MCP retrieval path, custom parameters are stored as a String.
            // In the AI API endpoint retrieval path, custom parameters are received as a LinkedHashMap.
            // No need to handle that case, since AI APIs do not currently support OAuth endpoint security.
            if (deploymentStage.get(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS) instanceof String) {
                String customParametersString = (String) deploymentStage.get(
                        APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS);
                if (StringUtils.isNotEmpty(customParametersString)) {
                    JSONParser parser = new JSONParser();
                    JSONObject customParameters = (JSONObject) parser.parse(customParametersString);
                    decryptCustomOauthParameters(customParameters, cryptoUtil);
                    deploymentStage.put(APIConstants.OAuthConstants.OAUTH_CUSTOM_PARAMETERS, customParameters);
                }
            }

            endpointSecurityElement.put(sectionKey, deploymentStage);
        }
    }

    /**
     * Set AWS Secret Key based on preserveCredentials state
     *
     * @param awsEndpointConfig   Endpoint configuration of the API
     * @param preserveCredentials Condition to preserve credentials
     * @return Updated endpoint config
     */
    private static JSONObject handleAWSCredentials(JSONObject awsEndpointConfig, boolean preserveCredentials) {

        if (StringUtils.isNotEmpty((String) awsEndpointConfig.get(APIConstants.AMZN_SECRET_KEY))) {
            if (!preserveCredentials) {
                awsEndpointConfig.put(APIConstants.AMZN_SECRET_KEY, APIConstants.AWS_SECRET_KEY);
                return awsEndpointConfig;
            } else {
                String secretKey = (String) awsEndpointConfig.get(APIConstants.AMZN_SECRET_KEY);
                // Decrypting the key since CTL project goes between environments which have different encryption keys.
                try {
                    CryptoUtil cryptoUtil = CryptoUtil.getDefaultCryptoUtil();
                    String decryptedSecret = new String(cryptoUtil.base64DecodeAndDecrypt(secretKey),
                            APIConstants.DigestAuthConstants.CHARSET);
                    awsEndpointConfig.put(APIConstants.AMZN_SECRET_KEY, decryptedSecret);
                    return awsEndpointConfig;
                } catch (CryptoException | UnsupportedEncodingException e) {
                    log.error("Error while decrypting the Amazon key", e);
                }
            }
        }
        return awsEndpointConfig;
    }

    /**
     * Decrypts the secret custom oauth parameters in the customParameters.
     *
     * @param customParameters The JSON object containing custom OAuth parameters.
     * @param cryptoUtil       Utility for handling decryption logic (including base64 decoding).
     * @throws CryptoException If an error occurs during the decryption process.
     */
    private static void decryptCustomOauthParameters(JSONObject customParameters, CryptoUtil cryptoUtil)
            throws CryptoException {
        if (customParameters == null || customParameters.isEmpty()) {
            return;
        }

        for (Object valObj : customParameters.values()) {
            // If value is an extended custom parameter object
            if (valObj instanceof JSONObject) {
                JSONObject valueObj = (JSONObject) valObj;
                if (Boolean.TRUE.equals(valueObj.get(APIConstants.OAuthConstants.CUSTOM_PARAMETERS_SECURED))) {
                    String encryptedValue = (String) valueObj.get(APIConstants.OAuthConstants.CUSTOM_PARAMETERS_VALUE);
                    if (StringUtils.isNotEmpty(encryptedValue)) {
                        String decryptedValue = new String(cryptoUtil.base64DecodeAndDecrypt(encryptedValue));
                        valueObj.put(APIConstants.OAuthConstants.CUSTOM_PARAMETERS_VALUE, decryptedValue);
                    }
                }
            }
        }
    }

    /**
     * Masks (clears) the values of secret custom OAuth parameters in the given endpoint security configuration.
     *
     * @param customParams The JSON object that includes custom parameters.
     */
    private static void maskSecretCustomParameters(JSONObject customParams) {
        for (Object valObj : customParams.values()) {
            // If value is an extended custom parameter object
            if (valObj instanceof JSONObject) {
                JSONObject valueObj = (JSONObject) valObj;
                if (Boolean.TRUE.equals((valueObj.get(APIConstants.OAuthConstants.CUSTOM_PARAMETERS_SECURED)))) {
                    valueObj.put(APIConstants.OAuthConstants.CUSTOM_PARAMETERS_VALUE, "");
                }
            }
        }
    }

    public static APIRevisionDTO fromAPIRevisiontoDTO(APIRevision model) throws APIManagementException {

        APIRevisionDTO apiRevisionDTO = new APIRevisionDTO();
        apiRevisionDTO.setId(model.getRevisionUUID());
        String key = "Revision " + model.getId();
        apiRevisionDTO.setDisplayName(key);
        apiRevisionDTO.setDescription(model.getDescription());
        if (model.getCreatedTime() != null) {
            try {
                apiRevisionDTO.setCreatedTime(parseStringToDate(model.getCreatedTime()));
            } catch (java.text.ParseException e) {
                throw new APIManagementException("Error while parsing the created time:" + model.getCreatedTime(), e);
            }
        }
        APIRevisionAPIInfoDTO apiRevisionAPIInfoDTO = new APIRevisionAPIInfoDTO();
        apiRevisionAPIInfoDTO.setId(model.getApiUUID());
        apiRevisionDTO.setApiInfo(apiRevisionAPIInfoDTO);
        List<APIRevisionDeploymentDTO> apiRevisionDeploymentDTOS = new ArrayList<>();
        if (model.getApiRevisionDeploymentList() != null) {
            for (APIRevisionDeployment apiRevisionDeployment : model.getApiRevisionDeploymentList()) {
                apiRevisionDeploymentDTOS.add(fromAPIRevisionDeploymenttoDTO(apiRevisionDeployment));
            }
        }
        apiRevisionDTO.setDeploymentInfo(apiRevisionDeploymentDTOS);
        return apiRevisionDTO;
    }

    public static APIRevisionListDTO fromListAPIRevisiontoDTO(List<APIRevision> apiRevisionList)
            throws APIManagementException {

        APIRevisionListDTO apiRevisionListDTO = new APIRevisionListDTO();
        List<APIRevisionDTO> apiRevisionDTOS = new ArrayList<>();
        for (APIRevision apiRevision : apiRevisionList) {
            apiRevisionDTOS.add(fromAPIRevisiontoDTO(apiRevision));
        }
        apiRevisionListDTO.setCount(apiRevisionList.size());
        apiRevisionListDTO.setList(apiRevisionDTOS);
        return apiRevisionListDTO;
    }

    /**
     * Get API Revision Deployment DTO from API Revision Deployment model
     *
     * @param model API Revision Deployment
     * @return API Revision Deployment DTO
     * @throws APIManagementException If error in deployment time
     */
    public static APIRevisionDeploymentDTO fromAPIRevisionDeploymenttoDTO(APIRevisionDeployment model)
            throws APIManagementException {

        APIRevisionDeploymentDTO apiRevisionDeploymentDTO = new APIRevisionDeploymentDTO();
        apiRevisionDeploymentDTO.setName(model.getDeployment());
        apiRevisionDeploymentDTO.setVhost(model.getVhost());
        if (model.getStatus() != null) {
            apiRevisionDeploymentDTO.setStatus(
                    APIRevisionDeploymentDTO.StatusEnum.valueOf(model.getStatus().toString()));
        } else {
            apiRevisionDeploymentDTO.setStatus(null);
        }
        if (model.getRevisionUUID() != null) {
            apiRevisionDeploymentDTO.setRevisionUuid(model.getRevisionUUID());
        }
        apiRevisionDeploymentDTO.setDisplayOnDevportal(model.isDisplayOnDevportal());
        if (model.getDeployedTime() != null) {
            try {
                apiRevisionDeploymentDTO.setDeployedTime(parseStringToDate(model.getDeployedTime()));
            } catch (java.text.ParseException e) {
                throw new APIManagementException("Error while parsing the deployed time:" + model.getDeployedTime(), e);
            }
        }
        if (model.getSuccessDeployedTime() != null) {
            try {
                apiRevisionDeploymentDTO.setSuccessDeployedTime(parseStringToDate(model.getSuccessDeployedTime()));
            } catch (java.text.ParseException e) {
                throw new APIManagementException("Error while parsing the successfully deployed time:"
                        + model.getSuccessDeployedTime(), e);
            }
        }
        apiRevisionDeploymentDTO.setLiveGatewayCount(model.getLiveGatewayCount());
        apiRevisionDeploymentDTO.setDeployedGatewayCount(model.getDeployedGatewayCount());
        apiRevisionDeploymentDTO.setFailedGatewayCount(model.getFailedGatewayCount());

        return apiRevisionDeploymentDTO;
    }

    public static APIEndpointListDTO fromAPIEndpointListToDTO(List<APIEndpointInfo> apiEndpoints, String organization,
            boolean preserveCredentials) throws APIManagementException {
        APIEndpointListDTO apiEndpointListDTO = new APIEndpointListDTO();
        List<APIEndpointDTO> apiEndpointDTOs = new ArrayList<>();
        for (APIEndpointInfo apiEndpoint : apiEndpoints) {
            apiEndpointDTOs.add(fromAPIEndpointToDTO(apiEndpoint, organization, preserveCredentials));
        }
        apiEndpointListDTO.setCount(apiEndpointDTOs.size());
        apiEndpointListDTO.setList(apiEndpointDTOs);
        return apiEndpointListDTO;
    }

    public static APIEndpointDTO fromAPIEndpointToDTO(APIEndpointInfo apiEndpoint, String organization,
            boolean preserveCredentials) throws APIManagementException {

        APIEndpointDTO apiEndpointDTO = new APIEndpointDTO();
        apiEndpointDTO.setId(apiEndpoint.getId());
        apiEndpointDTO.setName(apiEndpoint.getName());
        apiEndpointDTO.setDeploymentStage(apiEndpoint.getDeploymentStage());

        Map<String, Object> endpointConfig = apiEndpoint.getEndpointConfig();
        Map endpointSecurityMap = (Map) endpointConfig.get(APIConstants.ENDPOINT_SECURITY);
        if (endpointSecurityMap != null && !endpointSecurityMap.isEmpty()) {
            JSONObject endpointSecurity = new JSONObject(endpointSecurityMap);
            endpointSecurity = handleEndpointSecurityDecrypt(endpointSecurity);
            endpointSecurity = handleEndpointSecurity(endpointSecurity, organization, preserveCredentials);
            endpointConfig.put(APIConstants.ENDPOINT_SECURITY, endpointSecurity);
        }
        apiEndpointDTO.setEndpointConfig(endpointConfig);

        return apiEndpointDTO;
    }

    /**
     * Converts a {@link Backend} entity to a {@link Backend}, updating
     * its endpoint security details as needed.
     *
     * @param endpoint            the {@link Backend} entity
     * @param organization        the organization identifier
     * @param preserveCredentials whether to keep sensitive credentials in the config
     * @return a {@link BackendDTO} representing the backend endpoint
     * @throws APIManagementException if JSON processing or security handling fails
     */
    public static BackendDTO fromBackendAPIToDTO(Backend endpoint, String organization, boolean preserveCredentials)
            throws APIManagementException {

        BackendDTO dto = new BackendDTO();
        if (endpoint == null) {
            return dto;
        }
        dto.setId(endpoint.getId());
        dto.setName(endpoint.getName());
        String endpointConfigString = endpoint.getEndpointConfig();
        if (endpointConfigString != null && !endpointConfigString.isEmpty()) {
            try {
                JSONParser parser = new JSONParser();
                JSONObject endpointConfigJsonObj = (JSONObject) parser.parse(endpointConfigString);
                Object securityObj = endpointConfigJsonObj.get(APIConstants.ENDPOINT_SECURITY);
                if (securityObj instanceof JSONObject) {
                    JSONObject endpointSecurityJson = (JSONObject) securityObj;
                    if (!endpointSecurityJson.isEmpty()) {
                        endpointSecurityJson = handleEndpointSecurity(endpointSecurityJson, organization,
                                preserveCredentials);
                        if (preserveCredentials) {
                            endpointSecurityJson = handleEndpointSecurityDecrypt(endpointSecurityJson);
                        }
                        endpointConfigJsonObj.put(APIConstants.ENDPOINT_SECURITY, endpointSecurityJson);
                    }
                }
                String updatedConfigJson = endpointConfigJsonObj.toJSONString();
                dto.setEndpointConfig(updatedConfigJson);
            } catch (ParseException e) {
                String msg = "Error while processing endpoint configuration JSON.";
                handleException(msg, e);
            }
        }

        dto.setDefinition(endpoint.getDefinition());
        return dto;
    }

    public static APIEndpointInfo fromDTOtoAPIEndpoint(APIEndpointDTO apiEndpointDTO, String organization)
            throws APIManagementException {
        APIEndpointInfo apiEndpoint = new APIEndpointInfo();
        apiEndpoint.setId(apiEndpointDTO.getId());
        apiEndpoint.setName(apiEndpointDTO.getName());
        apiEndpoint.setDeploymentStage(apiEndpointDTO.getDeploymentStage());
        try {
            HashMap endpointConfigHashMap = (HashMap) apiEndpointDTO.getEndpointConfig();
            apiEndpoint.setEndpointConfig(endpointConfigHashMap);
        } catch (ClassCastException e) {
            throw new APIManagementException("Endpoint Config is missing of API Endpoint.",
                    ExceptionCodes.ERROR_MISSING_ENDPOINT_CONFIG_OF_API_ENDPOINT_API);
        }
        return apiEndpoint;
    }

    public static ApiEndpointValidationResponseDTO fromEndpointValidationToDTO(
            APIEndpointValidationDTO endpointValidationDTO) {
        ApiEndpointValidationResponseDTO apiEndpointValidationResponseDTO = new ApiEndpointValidationResponseDTO();
        apiEndpointValidationResponseDTO.setStatusCode(endpointValidationDTO.getStatusCode());
        apiEndpointValidationResponseDTO.setStatusMessage(endpointValidationDTO.getStatusMessage());
        apiEndpointValidationResponseDTO.setError(endpointValidationDTO.getError());

        return apiEndpointValidationResponseDTO;
    }

    private static Date parseStringToDate(String time) throws java.text.ParseException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return dateFormat.parse(time);
    }
}
