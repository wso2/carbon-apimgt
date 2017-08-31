/*
 *
 *  Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * /
 */

package org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.CORSConfiguration;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.definitions.APIDefinitionFromSwagger20;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIBusinessInformationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APICorsConfigurationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIEndpointSecurityDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIMaxTpsDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.SequenceDTO;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class APIMappingUtil {

    private static final Log log = LogFactory.getLog(APIMappingUtil.class);

    public static APIIdentifier getAPIIdentifierFromApiId(String apiId) {
        //if apiId contains -AT-, that need to be replaced before splitting
        apiId = APIUtil.replaceEmailDomainBack(apiId);
        String[] apiIdDetails = apiId.split(RestApiConstants.API_ID_DELIMITER);

        if (apiIdDetails.length < 3) {
            RestApiUtil.handleBadRequest("Provided API identifier '" + apiId + "' is invalid", log);
        }

        // apiId format: provider-apiName-version
        String providerName = apiIdDetails[0];
        String apiName = apiIdDetails[1];
        String version = apiIdDetails[2];
        String providerNameEmailReplaced = APIUtil.replaceEmailDomain(providerName);
        return new APIIdentifier(providerNameEmailReplaced, apiName, version);
    }

    /**
     * Returns the APIIdentifier given the uuid or the id in {provider}-{api}-{version} format
     *
     * @param apiId                 uuid or the id in {provider}-{api}-{version} format
     * @param requestedTenantDomain tenant domain of the API
     * @return APIIdentifier which represents the given id
     * @throws APIManagementException
     */
    public static APIIdentifier getAPIIdentifierFromApiIdOrUUID(String apiId, String requestedTenantDomain)
            throws APIManagementException {
        return getAPIInfoFromApiIdOrUUID(apiId, requestedTenantDomain).getId();
    }

    /**
     * Returns an API with minimal info given the uuid or the id in {provider}-{api}-{version} format
     *
     * @param apiId                 uuid or the id in {provider}-{api}-{version} format
     * @param requestedTenantDomain tenant domain of the API
     * @return API which represents the given id
     * @throws APIManagementException
     */
    public static API getAPIInfoFromApiIdOrUUID(String apiId, String requestedTenantDomain)
            throws APIManagementException {
        API api;
        APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
        if (RestApiUtil.isUUID(apiId)) {
            api = apiProvider.getLightweightAPIByUUID(apiId, requestedTenantDomain);
        } else {
            APIIdentifier apiIdentifier = getAPIIdentifierFromApiId(apiId);

            //Checks whether the logged in user's tenant and the API's tenant is equal
            RestApiUtil.validateUserTenantWithAPIIdentifier(apiIdentifier);

            api = apiProvider.getLightweightAPI(apiIdentifier);
        }
        return api;
    }

    /**
     * Returns the API given the uuid or the id in {provider}-{api}-{version} format
     *
     * @param apiId                 uuid or the id in {provider}-{api}-{version} format
     * @param requestedTenantDomain tenant domain of the API
     * @return API which represents the given id
     * @throws APIManagementException
     */
    public static API getAPIFromApiIdOrUUID(String apiId, String requestedTenantDomain)
            throws APIManagementException {
        API api;
        APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
        if (RestApiUtil.isUUID(apiId)) {
            api = apiProvider.getAPIbyUUID(apiId, requestedTenantDomain);
        } else {
            APIIdentifier apiIdentifier = getAPIIdentifierFromApiId(apiId);

            //Checks whether the logged in user's tenant and the API's tenant is equal
            RestApiUtil.validateUserTenantWithAPIIdentifier(apiIdentifier);

            api = apiProvider.getAPI(apiIdentifier);
        }
        return api;
    }

    public static APIDTO fromAPItoDTO(API model) throws APIManagementException {

        APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();

        APIDTO dto = new APIDTO();
        dto.setName(model.getId().getApiName());
        dto.setVersion(model.getId().getVersion());
        String providerName = model.getId().getProviderName();
        dto.setProvider(APIUtil.replaceEmailDomainBack(providerName));
        dto.setId(model.getUUID());
        String context = model.getContextTemplate();
        if (context.endsWith("/" + RestApiConstants.API_VERSION_PARAM)) {
            context = context.replace("/" + RestApiConstants.API_VERSION_PARAM, "");
        }
        dto.setContext(context);
        dto.setDescription(model.getDescription());

        dto.setIsDefaultVersion(model.isDefaultVersion());
        dto.setResponseCaching(model.getResponseCache());
        dto.setCacheTimeout(model.getCacheTimeout());
        dto.setEndpointConfig(model.getEndpointConfig());
        if (!StringUtils.isBlank(model.getThumbnailUrl())) {
            dto.setThumbnailUri(getThumbnailUri(model.getUUID()));
        }
        List<SequenceDTO> sequences = new ArrayList<>();

        String inSequenceName = model.getInSequence();
        if (inSequenceName != null && !inSequenceName.isEmpty()) {
            String type = APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN;
            boolean sharedStatus = getSharedStatus(inSequenceName,type,dto);
            String uuid = getSequenceId(inSequenceName,type,dto);
            SequenceDTO inSequence = new SequenceDTO();
            inSequence.setName(inSequenceName);
            inSequence.setType(type);
            inSequence.setShared(sharedStatus);
            inSequence.setId(uuid);
            sequences.add(inSequence);
        }

        String outSequenceName = model.getOutSequence();
        if (outSequenceName != null && !outSequenceName.isEmpty()) {
            String type = APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT;
            boolean sharedStatus = getSharedStatus(outSequenceName,type,dto);
            String uuid = getSequenceId(outSequenceName,type,dto);
            SequenceDTO outSequence = new SequenceDTO();
            outSequence.setName(outSequenceName);
            outSequence.setType(type);
            outSequence.setShared(sharedStatus);
            outSequence.setId(uuid);
            sequences.add(outSequence);
        }

        String faultSequenceName = model.getFaultSequence();
        if (faultSequenceName != null && !faultSequenceName.isEmpty()) {
            String type = APIConstants.API_CUSTOM_SEQUENCE_TYPE_FAULT;
            boolean sharedStatus = getSharedStatus(faultSequenceName,type,dto);
            String uuid = getSequenceId(faultSequenceName,type,dto);
            SequenceDTO faultSequence = new SequenceDTO();
            faultSequence.setName(faultSequenceName);
            faultSequence.setType(type);
            faultSequence.setShared(sharedStatus);
            faultSequence.setId(uuid);
            sequences.add(faultSequence);
        }

        dto.setSequences(sequences);

        dto.setStatus(model.getStatus().getStatus());

        String subscriptionAvailability = model.getSubscriptionAvailability();
        if (subscriptionAvailability != null) {
            dto.setSubscriptionAvailability(mapSubscriptionAvailabilityFromAPItoDTO(subscriptionAvailability));
        }

        if (model.getSubscriptionAvailableTenants() != null) {
            dto.setSubscriptionAvailableTenants(Arrays.asList(model.getSubscriptionAvailableTenants().split(",")));
        }

        //Get Swagger definition which has URL templates, scopes and resource details
        String apiSwaggerDefinition;

        apiSwaggerDefinition = apiProvider.getSwagger20Definition(model.getId());

        dto.setApiDefinition(apiSwaggerDefinition);

        Set<String> apiTags = model.getTags();
        List<String> tagsToReturn = new ArrayList<>();
        tagsToReturn.addAll(apiTags);
        dto.setTags(tagsToReturn);

        Set<org.wso2.carbon.apimgt.api.model.Tier> apiTiers = model.getAvailableTiers();
        List<String> tiersToReturn = new ArrayList<>();
        for (org.wso2.carbon.apimgt.api.model.Tier tier : apiTiers) {
            tiersToReturn.add(tier.getName());
        }
        dto.setTiers(tiersToReturn);
        dto.setType(APIDTO.TypeEnum.valueOf(model.getType()));

        if (!model.getType().equals(APIConstants.APIType.WS)) {
            dto.setTransport(Arrays.asList(model.getTransports().split(",")));
        }
        dto.setVisibility(mapVisibilityFromAPItoDTO(model.getVisibility()));

        if (model.getVisibleRoles() != null) {
            dto.setVisibleRoles(Arrays.asList(model.getVisibleRoles().split(",")));
        }

        if (model.getVisibleTenants() != null) {
            dto.setVisibleRoles(Arrays.asList(model.getVisibleTenants().split(",")));
        }

        APIBusinessInformationDTO apiBusinessInformationDTO = new APIBusinessInformationDTO();
        apiBusinessInformationDTO.setBusinessOwner(model.getBusinessOwner());
        apiBusinessInformationDTO.setBusinessOwnerEmail(model.getBusinessOwnerEmail());
        apiBusinessInformationDTO.setTechnicalOwner(model.getTechnicalOwner());
        apiBusinessInformationDTO.setTechnicalOwnerEmail(model.getTechnicalOwnerEmail());
        dto.setBusinessInformation(apiBusinessInformationDTO);
        String gatewayEnvironments = StringUtils.join(model.getEnvironments(),",");
        dto.setGatewayEnvironments(gatewayEnvironments);
        APICorsConfigurationDTO apiCorsConfigurationDTO = new APICorsConfigurationDTO();
        CORSConfiguration corsConfiguration = model.getCorsConfiguration();
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
        dto.setCorsConfiguration(apiCorsConfigurationDTO);
        dto.setWsdlUri(model.getWsdlUrl());
        setEndpointSecurityFromModelToApiDTO(model, dto);
        setMaxTpsFromModelToApiDTO(model, dto);

        return dto;
    }

    /**
     * Returns uuid of the specified mediation sequence
     *
     * @param sequenceName mediation sequence name
     * @param direction    in/out/fault
     * @param dto          APIDTO contains details of the exporting API
     * @return UUID of sequence or null
     */
    private static String getSequenceId(String sequenceName, String direction,
                                        APIDTO dto) {
        APIIdentifier apiIdentifier = new APIIdentifier(dto.getProvider(), dto.getName(),
                dto.getVersion());
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        try {
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager().
                    getTenantId(tenantDomain);
            return APIUtil.getMediationSequenceUuid(sequenceName, tenantId, direction, apiIdentifier);

        } catch (UserStoreException e) {
            log.error("Error occurred while reading tenant information ", e);

        } catch (APIManagementException e) {
            log.error("Error occurred while getting the uuid of the mediation sequence", e);
        }

        return null;
    }

    /**
     * Returns shared status of the mediation policy
     *
     * @param sequenceName mediation sequence name
     * @param sequenceType in/out/faul
     * @param dto          APIDTO contains details of the exporting API
     * @return true, if the mediation sequnce is a shared resource
     */
    private static boolean getSharedStatus(String sequenceName, String sequenceType, APIDTO dto) {
        String tenantDomain = RestApiUtil.getLoggedInUserTenantDomain();
        try {
            int tenantId = ServiceReferenceHolder.getInstance().getRealmService().getTenantManager()
                    .getTenantId(tenantDomain);
            APIIdentifier apiIdentifier = new APIIdentifier(dto.getProvider(), dto.getName(),
                    dto.getVersion());
            if (APIUtil.isPerAPISequence(sequenceName, tenantId, apiIdentifier,
                    sequenceType)) {
                return true;
            }
        } catch (UserStoreException e) {
            log.error("Error occurred while reading tenant information ", e);
        } catch (APIManagementException e) {
            log.error("Error occurred while checking the shared status of the mediation sequence", e);
        }
        return false;
    }

    public static API fromDTOtoAPI(APIDTO dto, String provider) throws APIManagementException {

        APIDefinition definitionFromSwagger20 = new APIDefinitionFromSwagger20();

        String providerEmailDomainReplaced = APIUtil.replaceEmailDomain(provider);

        // The provider name that is coming from the body is not honored for now.
        // Later we can use it by checking admin privileges of the user.
        APIIdentifier apiId = new APIIdentifier(providerEmailDomainReplaced, dto.getName(), dto.getVersion());
        API model = new API(apiId);

        String context = dto.getContext();
        final String originalContext = context;

        if (context.endsWith("/" + RestApiConstants.API_VERSION_PARAM)) {
            context = context.replace("/" + RestApiConstants.API_VERSION_PARAM, "");
        }

        context = context.startsWith("/") ? context : ("/" + context);
        String providerDomain = MultitenantUtils.getTenantDomain(provider);
        if (!MultitenantConstants.SUPER_TENANT_DOMAIN_NAME.equalsIgnoreCase(providerDomain)) {
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
        model.setEndpointConfig(dto.getEndpointConfig());
        model.setWsdlUrl(dto.getWsdlUri());
        model.setType(dto.getType().toString());
        model.setThumbnailUrl(dto.getThumbnailUri());

        if (dto.getStatus() != null) {
            model.setStatus(mapStatusFromDTOToAPI(dto.getStatus()));
        }
        model.setAsDefaultVersion(dto.getIsDefaultVersion());
        model.setResponseCache(dto.getResponseCaching());
        if (dto.getCacheTimeout() != null) {
            model.setCacheTimeout(dto.getCacheTimeout());
        } else {
            model.setCacheTimeout(APIConstants.API_RESPONSE_CACHE_TIMEOUT);
        }

        if (dto.getSequences() != null) {
            List<SequenceDTO> sequences = dto.getSequences();

            //validate whether provided sequences are available
            for (SequenceDTO sequence : sequences) {
                if (APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN.equalsIgnoreCase(sequence.getType())) {
                    model.setInSequence(sequence.getName());
                } else if (APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT.equalsIgnoreCase(sequence.getType())) {
                    model.setOutSequence(sequence.getName());
                } else {
                    model.setFaultSequence(sequence.getName());
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

        if (dto.getApiDefinition() != null) {
            String apiSwaggerDefinition = dto.getApiDefinition();
            //URI Templates
            Set<URITemplate> uriTemplates = definitionFromSwagger20.getURITemplates(model, apiSwaggerDefinition);
            model.setUriTemplates(uriTemplates);

            // scopes
            Set<Scope> scopes = definitionFromSwagger20.getScopes(apiSwaggerDefinition);
            model.setScopes(scopes);

        }

        if (dto.getTags() != null) {
            Set<String> apiTags = new HashSet<>(dto.getTags());
            model.addTags(apiTags);
        }

        Set<Tier> apiTiers = new HashSet<>();
        List<String> tiersFromDTO = dto.getTiers();
        for (String tier : tiersFromDTO) {
            apiTiers.add(new Tier(tier));
        }
        model.addAvailableTiers(apiTiers);

        String transports = StringUtils.join(dto.getTransport(), ',');
        model.setTransports(transports);
        model.setVisibility(mapVisibilityFromDTOtoAPI(dto.getVisibility()));
        if (dto.getVisibleRoles() != null) {
            String visibleRoles = StringUtils.join(dto.getVisibleRoles(), ',');
            model.setVisibleRoles(visibleRoles);
        }

        if (dto.getVisibleTenants() != null) {
            String visibleTenants = StringUtils.join(dto.getVisibleTenants(), ',');
            model.setVisibleTenants(visibleTenants);
        }

        APIBusinessInformationDTO apiBusinessInformationDTO = dto.getBusinessInformation();
        if (apiBusinessInformationDTO != null) {
            model.setBusinessOwner(apiBusinessInformationDTO.getBusinessOwner());
            model.setBusinessOwnerEmail(apiBusinessInformationDTO.getBusinessOwnerEmail());
            model.setTechnicalOwner(apiBusinessInformationDTO.getTechnicalOwner());
            model.setTechnicalOwnerEmail(apiBusinessInformationDTO.getTechnicalOwnerEmail());
        }
        if (!StringUtils.isBlank(dto.getGatewayEnvironments())) {
            String gatewaysString = dto.getGatewayEnvironments();
            model.setEnvironments(APIUtil.extractEnvironmentsForAPI(gatewaysString));
        } else if (dto.getGatewayEnvironments() != null) {
            //this means the provided gatewayEnvironments is "" (empty)
            model.setEnvironments(APIUtil.extractEnvironmentsForAPI(APIConstants.API_GATEWAY_NONE));
        }
        APICorsConfigurationDTO apiCorsConfigurationDTO = dto.getCorsConfiguration();
        CORSConfiguration corsConfiguration;
        if (apiCorsConfigurationDTO != null) {
            corsConfiguration =
                    new CORSConfiguration(apiCorsConfigurationDTO.getCorsConfigurationEnabled(),
                                          apiCorsConfigurationDTO.getAccessControlAllowOrigins(),
                                          apiCorsConfigurationDTO.getAccessControlAllowCredentials(),
                                          apiCorsConfigurationDTO.getAccessControlAllowHeaders(),
                                          apiCorsConfigurationDTO.getAccessControlAllowMethods());

        } else {
            corsConfiguration = APIUtil.getDefaultCorsConfiguration();
        }
        model.setCorsConfiguration(corsConfiguration);
        setEndpointSecurityFromApiDTOToModel(dto, model);
        setMaxTpsFromApiDTOToModel(dto, model);

        return model;
    }

    /**
     * Converts a List object of APIs into a DTO
     *
     * @param apiList List of APIs
     * @param limit   maximum number of APIs returns
     * @param offset  starting index
     * @return APIListDTO object containing APIDTOs
     */
    public static APIListDTO fromAPIListToDTO(List<API> apiList, int offset, int limit) {
        APIListDTO apiListDTO = new APIListDTO();
        List<APIInfoDTO> apiInfoDTOs = apiListDTO.getList();
        if (apiInfoDTOs == null) {
            apiInfoDTOs = new ArrayList<>();
            apiListDTO.setList(apiInfoDTOs);
        }

        //add the required range of objects to be returned
        int start = offset < apiList.size() && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = offset + limit - 1 <= apiList.size() - 1 ? offset + limit - 1 : apiList.size() - 1;
        for (int i = start; i <= end; i++) {
            apiInfoDTOs.add(fromAPIToInfoDTO(apiList.get(i)));
        }
        apiListDTO.setCount(apiInfoDTOs.size());
        return apiListDTO;
    }

    /**
     * Sets pagination urls for a APIListDTO object given pagination parameters and url parameters
     *
     * @param apiListDTO a APIListDTO object
     * @param query      search condition
     * @param limit      max number of objects returned
     * @param offset     starting index
     * @param size       max offset
     */
    public static void setPaginationParams(APIListDTO apiListDTO, String query, int offset, int limit, int size) {

        //acquiring pagination parameters and setting pagination urls
        Map<String, Integer> paginatedParams = RestApiUtil.getPaginationParams(offset, limit, size);
        String paginatedPrevious = "";
        String paginatedNext = "";

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = RestApiUtil
                    .getAPIPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT), query);
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = RestApiUtil
                    .getAPIPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT), query);
        }

        apiListDTO.setNext(paginatedNext);
        apiListDTO.setPrevious(paginatedPrevious);
    }

    /**
     * Creates a minimal DTO representation of an API object
     *
     * @param api API object
     * @return a minimal representation DTO
     */
    public static APIInfoDTO fromAPIToInfoDTO(API api) {
        APIInfoDTO apiInfoDTO = new APIInfoDTO();
        apiInfoDTO.setDescription(api.getDescription());
        String context = api.getContextTemplate();
        if (context.endsWith("/" + RestApiConstants.API_VERSION_PARAM)) {
            context = context.replace("/" + RestApiConstants.API_VERSION_PARAM, "");
        }
        apiInfoDTO.setContext(context);
        apiInfoDTO.setId(api.getUUID());
        APIIdentifier apiId = api.getId();
        apiInfoDTO.setName(apiId.getApiName());
        apiInfoDTO.setVersion(apiId.getVersion());
        String providerName = api.getId().getProviderName();
        apiInfoDTO.setProvider(APIUtil.replaceEmailDomainBack(providerName));
        apiInfoDTO.setStatus(api.getStatus().toString());
        if (!StringUtils.isBlank(api.getThumbnailUrl())) {
            apiInfoDTO.setThumbnailUri(getThumbnailUri(api.getUUID()));
        }
        return apiInfoDTO;
    }

    private static void setEndpointSecurityFromApiDTOToModel (APIDTO dto, API api) {
        APIEndpointSecurityDTO securityDTO = dto.getEndpointSecurity();
        if (dto.getEndpointSecurity() != null && securityDTO.getType() != null) {
            api.setEndpointSecured(true);
            api.setEndpointUTUsername(securityDTO.getUsername());
            api.setEndpointUTPassword(securityDTO.getPassword());
            if (APIEndpointSecurityDTO.TypeEnum.digest.equals(securityDTO.getType())) {
                api.setEndpointAuthDigest(true);
            }
        }
    }

    private static void setEndpointSecurityFromModelToApiDTO(API api, APIDTO dto) {
        if (api.isEndpointSecured()) {
            APIEndpointSecurityDTO securityDTO = new APIEndpointSecurityDTO();
            securityDTO.setType(APIEndpointSecurityDTO.TypeEnum.basic); //set default as basic
            securityDTO.setUsername(api.getEndpointUTUsername());
            securityDTO.setPassword(api.getEndpointUTPassword());
            if (api.isEndpointAuthDigest()) {
                securityDTO.setType(APIEndpointSecurityDTO.TypeEnum.digest);
            }
            dto.setEndpointSecurity(securityDTO);
        }
    }

    private static void setMaxTpsFromApiDTOToModel(APIDTO dto, API api) {
        APIMaxTpsDTO maxTpsDTO = dto.getMaxTps();
        if (maxTpsDTO != null) {
            if (maxTpsDTO.getProduction() != null) {
                api.setProductionMaxTps(maxTpsDTO.getProduction().toString());
            }
            if (maxTpsDTO.getSandbox() != null) {
                api.setSandboxMaxTps(maxTpsDTO.getSandbox().toString());
            }
        }
    }

    private static void setMaxTpsFromModelToApiDTO(API api, APIDTO dto) {
        if (StringUtils.isBlank(api.getProductionMaxTps()) && StringUtils.isBlank(api.getSandboxMaxTps())) {
            return;
        }
        APIMaxTpsDTO maxTpsDTO = new APIMaxTpsDTO();
        try {
            if (!StringUtils.isBlank(api.getProductionMaxTps())) {
                maxTpsDTO.setProduction(Long.parseLong(api.getProductionMaxTps()));
            }
            if (!StringUtils.isBlank(api.getSandboxMaxTps())) {
                maxTpsDTO.setSandbox(Long.parseLong(api.getSandboxMaxTps()));
            }
            dto.setMaxTps(maxTpsDTO);
        } catch (NumberFormatException e) {
            //logs the error and continues as this is not a blocker
            log.error("Cannot convert to Long format when setting maxTps for API", e);
        }
    }

    private static APIStatus mapStatusFromDTOToAPI(String apiStatus) {
        // switch case statements are not working as APIStatus.<STATUS>.toString() or APIStatus.<STATUS>.getStatus()
        //  is not a constant
        if (apiStatus.equals(APIStatus.BLOCKED.toString())) {
            return APIStatus.BLOCKED;
        } else if (apiStatus.equals(APIStatus.CREATED.toString())) {
            return APIStatus.CREATED;
        } else if (apiStatus.equals(APIStatus.PUBLISHED.toString())) {
            return APIStatus.PUBLISHED;
        } else if (apiStatus.equals(APIStatus.DEPRECATED.toString())) {
            return APIStatus.DEPRECATED;
        } else if (apiStatus.equals(APIStatus.PROTOTYPED.toString())) {
            return APIStatus.PROTOTYPED;
        } else {
            return null; // how to handle this?
        }
    }

    private static String mapVisibilityFromDTOtoAPI(APIDTO.VisibilityEnum visibility) {
        switch (visibility) {
            case PUBLIC:
                return APIConstants.API_GLOBAL_VISIBILITY;
            case PRIVATE:
                return APIConstants.API_PRIVATE_VISIBILITY;
            case RESTRICTED:
                return APIConstants.API_RESTRICTED_VISIBILITY;
            case CONTROLLED:
                return APIConstants.API_CONTROLLED_VISIBILITY;
            default:
                return null; // how to handle this?
        }
    }

    private static APIDTO.VisibilityEnum mapVisibilityFromAPItoDTO(String visibility) {
        switch (visibility) { //public, private,controlled, restricted
            case APIConstants.API_GLOBAL_VISIBILITY :
                return APIDTO.VisibilityEnum.PUBLIC;
            case APIConstants.API_PRIVATE_VISIBILITY :
                return APIDTO.VisibilityEnum.PRIVATE;
            case APIConstants.API_RESTRICTED_VISIBILITY :
                return APIDTO.VisibilityEnum.RESTRICTED;
            case APIConstants.API_CONTROLLED_VISIBILITY :
                return APIDTO.VisibilityEnum.CONTROLLED;
            default:
                return null; // how to handle this?
        }
    }

    private static APIDTO.SubscriptionAvailabilityEnum mapSubscriptionAvailabilityFromAPItoDTO(
            String subscriptionAvailability) {

        switch (subscriptionAvailability) {
            case APIConstants.SUBSCRIPTION_TO_CURRENT_TENANT :
                return APIDTO.SubscriptionAvailabilityEnum.current_tenant;
            case APIConstants.SUBSCRIPTION_TO_ALL_TENANTS :
                return APIDTO.SubscriptionAvailabilityEnum.all_tenants;
            case APIConstants.SUBSCRIPTION_TO_SPECIFIC_TENANTS :
                return APIDTO.SubscriptionAvailabilityEnum.specific_tenants;
            default:
                return null; // how to handle this?
        }

    }

    private static String mapSubscriptionAvailabilityFromDTOtoAPI(
            APIDTO.SubscriptionAvailabilityEnum subscriptionAvailability) {
        switch (subscriptionAvailability) {
            case current_tenant:
                return APIConstants.SUBSCRIPTION_TO_CURRENT_TENANT;
            case all_tenants:
                return APIConstants.SUBSCRIPTION_TO_ALL_TENANTS;
            case specific_tenants:
                return APIConstants.SUBSCRIPTION_TO_SPECIFIC_TENANTS;
            default:
                return null; // how to handle this? 500 or 400
        }

    }

    private static String updateContextWithVersion(String version, String contextVal, String context) {
        // This condition should not be true for any occasion but we keep it so that there are no loopholes in
        // the flow.
        if (version == null) {
            // context template patterns - /{version}/foo or /foo/{version}
            // if the version is null, then we remove the /{version} part from the context
            context = contextVal.replace("/" + RestApiConstants.API_VERSION_PARAM, "");
        } else {
            context = context.replace(RestApiConstants.API_VERSION_PARAM, version);
        }
        return context;
    }

    private static String checkAndSetVersionParam(String context) {
        // This is to support the new Pluggable version strategy
        // if the context does not contain any {version} segment, we use the default version strategy.
        if (!context.contains(RestApiConstants.API_VERSION_PARAM)) {
            if (!context.endsWith("/")) {
                context = context + "/";
            }
            context = context + RestApiConstants.API_VERSION_PARAM;
        }
        return context;
    }

    private static String getThumbnailUri (String uuid) {
        return RestApiConstants.RESOURCE_PATH_THUMBNAIL.replace(RestApiConstants.APIID_PARAM, uuid);
    }
}
