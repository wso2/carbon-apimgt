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
package org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIMRegistryServiceImpl;
import org.wso2.carbon.apimgt.impl.definitions.APIDefinitionFromOpenAPISpec;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.governance.custom.lifecycles.checklist.util.CheckListItem;
import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.user.api.UserStoreException;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.*;

import static org.wso2.carbon.apimgt.impl.utils.APIUtil.handleException;

public class APIMappingUtil {

    private static final Log log = LogFactory.getLog(APIMappingUtil.class);

    public static API fromDTOtoAPI(APIDTO dto, String provider) throws APIManagementException {

//        APIDefinition apiDefinitionFromOpenAPISpec = new APIDefinitionFromOpenAPISpec();

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
        model.setEndpointConfig(getEndpointConfigString(dto.getEndpoint()));
        model.setWsdlUrl(dto.getWsdlUri());
        model.setType(dto.getType().toString());
//        model.setThumbnailUrl(dto.getThumbnailUri());todo

        if (dto.getLifeCycleStatus() != null) {
            model.setStatus((dto.getLifeCycleStatus() != null) ? dto.getLifeCycleStatus().toUpperCase() : null);
        }
        if (dto.getIsDefaultVersion() != null) {
            model.setAsDefaultVersion(dto.getIsDefaultVersion());
        }
        model.setResponseCache(dto.getResponseCaching());
        if (dto.getCacheTimeout() != null) {
            model.setCacheTimeout(dto.getCacheTimeout());
        } else {
            model.setCacheTimeout(APIConstants.API_RESPONSE_CACHE_TIMEOUT);
        }

/*        if (dto.getSequences() != null) { todo
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
        }*/

        if (dto.getSubscriptionAvailability() != null) {
            model.setSubscriptionAvailability(
                    mapSubscriptionAvailabilityFromDTOtoAPI(dto.getSubscriptionAvailability()));
        }

        if (dto.getSubscriptionAvailableTenants() != null) {
            model.setSubscriptionAvailableTenants(StringUtils.join(dto.getSubscriptionAvailableTenants(), ","));
        }
        // scopes
        Set<Scope> scopes = getScopes(dto);
        model.setScopes(scopes);

        //URI Templates
        Set<URITemplate> uriTemplates = getURITemplates(model, dto.getOperations());
        model.setUriTemplates(uriTemplates);

        if (dto.getTags() != null) {
            Set<String> apiTags = new HashSet<>(dto.getTags());
            model.addTags(apiTags);
        }

        Set<Tier> apiTiers = new HashSet<>();
        List<String> tiersFromDTO = dto.getPolicies();
        for (String tier : tiersFromDTO) {
            apiTiers.add(new Tier(tier));
        }
        model.addAvailableTiers(apiTiers);
        model.setApiLevelPolicy(dto.getApiPolicy());

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
            String visibleTenants = StringUtils.join(dto.getVisibleTenants(), ',');
            model.setVisibleTenants(visibleTenants);
        }

        List<String> accessControlRoles = dto.getAccessControlRoles();
        if (accessControlRoles == null || accessControlRoles.isEmpty()) {
            model.setAccessControl(APIConstants.NO_ACCESS_CONTROL);
            model.setAccessControlRoles("null");
        } else {
            model.setAccessControlRoles(StringUtils.join(accessControlRoles, ',').toLowerCase());
            model.setAccessControl(APIConstants.API_RESTRICTED_VISIBILITY);
        }

        Map<String, String> additionalProperties = dto.getAdditionalProperties();
        if (additionalProperties != null) {
            for (Map.Entry<String, String> entry : additionalProperties.entrySet()) {
                model.addProperty(entry.getKey(), entry.getValue());
            }
        }
        APIBusinessInformationDTO apiBusinessInformationDTO = dto.getBusinessInformation();
        if (apiBusinessInformationDTO != null) {
            model.setBusinessOwner(apiBusinessInformationDTO.getBusinessOwner());
            model.setBusinessOwnerEmail(apiBusinessInformationDTO.getBusinessOwnerEmail());
            model.setTechnicalOwner(apiBusinessInformationDTO.getTechnicalOwner());
            model.setTechnicalOwnerEmail(apiBusinessInformationDTO.getTechnicalOwnerEmail());
        }
        if (dto.getGatewayEnvironments().size() > 0) {
            List<String> gatewaysList = dto.getGatewayEnvironments();
            model.setEnvironments(APIUtil.extractEnvironmentsForAPI(gatewaysList));
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
        model.setAuthorizationHeader(dto.getAuthorizationHeader());
        model.setApiSecurity(getSecurityScheme(dto.getSecurityScheme()));
        return model;
    }

    /**
     * Returns the APIIdentifier given the uuid
     *
     * @param apiId                 API uuid
     * @param requestedTenantDomain tenant domain of the API
     * @return APIIdentifier which represents the given id
     * @throws APIManagementException
     */
    public static APIIdentifier getAPIIdentifierFromUUID(String apiId, String requestedTenantDomain)
            throws APIManagementException {
        return getAPIInfoFromUUID(apiId, requestedTenantDomain).getId();
    }

    /**
     * Returns an API with minimal info given the uuid.
     *
     * @param apiUUID                 API uuid
     * @param requestedTenantDomain tenant domain of the API
     * @return API which represents the given id
     * @throws APIManagementException
     */
    public static API getAPIInfoFromUUID(String apiUUID, String requestedTenantDomain)
            throws APIManagementException {
        API api;
        APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
        api = apiProvider.getLightweightAPIByUUID(apiUUID, requestedTenantDomain);
        return api;
    }

    /**
     * Converts a List object of APIs into a DTO
     *
     * @param apiList List of APIs
     * @param expand  defines whether APIListDTO should contain APIINFODTOs or APIDTOs
     * @return APIListDTO object containing APIDTOs
     */
    public static APIListDTO fromAPIListToDTO(List<API> apiList, boolean expand) throws APIManagementException {

        APIListDTO apiListDTO = new APIListDTO();
        List<APIInfoDTO> apiInfoDTOs = apiListDTO.getList();
        if (apiList != null && !expand) {
            for (API api : apiList) {
                apiInfoDTOs.add(fromAPIToInfoDTO(api));
            }
        }
        //todo: support expand
//        else if (apiList != null && expand) {
//            for (API api : apiList) {
//                apiInfoDTOs.add(fromAPItoDTO(api));
//            }
//        }
        apiListDTO.setCount(apiInfoDTOs.size());
        return apiListDTO;
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
        apiInfoDTO.setLifeCycleStatus(api.getStatus().toString());
        if (!StringUtils.isBlank(api.getThumbnailUrl())) {
            apiInfoDTO.setThumbnailUri(getThumbnailUri(api.getUUID()));
        }
        return apiInfoDTO;
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

        PaginationDTO paginationDTO = CommonMappingUtil
                .getPaginationDTO(limit, offset, size, paginatedNext, paginatedPrevious);
        apiListDTO.setPagination(paginationDTO);
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

    private static String getThumbnailUri(String uuid) {

        return RestApiConstants.RESOURCE_PATH_THUMBNAIL.replace(RestApiConstants.APIID_PARAM, uuid);
    }

    private static String mapVisibilityFromDTOtoAPI(APIDTO.VisibilityEnum visibility) {

        switch (visibility) {
            case PUBLIC:
                return APIConstants.API_GLOBAL_VISIBILITY;
            case PRIVATE:
                return APIConstants.API_PRIVATE_VISIBILITY;
            case RESTRICTED:
                return APIConstants.API_RESTRICTED_VISIBILITY;
//            case CONTROLLED: todo add to swagger
//                return APIConstants.API_CONTROLLED_VISIBILITY;
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

    private static void setEndpointSecurityFromApiDTOToModel(APIDTO dto, API api) {

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
        dto.setEndpoint(getAPIEndpointDTO(model.getEndpoint()));
      /*  if (!StringUtils.isBlank(model.getThumbnailUrl())) {todo
            dto.setThumbnailUri(getThumbnailUri(model.getUUID()));
        }*/
/*        List<SequenceDTO> sequences = new ArrayList<>();todo

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

        dto.setSequences(sequences);*/

        dto.setLifeCycleStatus(model.getStatus());

        String subscriptionAvailability = model.getSubscriptionAvailability();
        if (subscriptionAvailability != null) {
            dto.setSubscriptionAvailability(mapSubscriptionAvailabilityFromAPItoDTO(subscriptionAvailability));
        }

        if (model.getSubscriptionAvailableTenants() != null) {
            dto.setSubscriptionAvailableTenants(Arrays.asList(model.getSubscriptionAvailableTenants().split(",")));
        }

        //Get Swagger definition which has URL templates, scopes and resource details
        String apiSwaggerDefinition = apiProvider.getOpenAPIDefinition(model.getId());

        dto.setOperations(getOperationsFromSwaggerDef(apiSwaggerDefinition));

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
        dto.setApiPolicy(model.getApiLevelPolicy());

        //APIs created with type set to "NULL" will be considered as "HTTP"
        if (model.getType() == null || model.getType().toLowerCase().equals("null")) {
            dto.setType(APIDTO.TypeEnum.HTTP);
        } else {
            dto.setType(APIDTO.TypeEnum.valueOf(model.getType()));
        }

        if (!APIConstants.APIType.WS.equals(model.getType())) {
            if (StringUtils.isEmpty(model.getTransports())) {
                List<String> transports = new ArrayList<>();
                transports.add(APIConstants.HTTPS_PROTOCOL);

                dto.setTransport(transports);
            }
            dto.setTransport(Arrays.asList(model.getTransports().split(",")));
        }
        if (StringUtils.isEmpty(model.getTransports())) {
            dto.setVisibility(APIDTO.VisibilityEnum.PUBLIC);
        }
        dto.setVisibility(mapVisibilityFromAPItoDTO(model.getVisibility()));

        if (model.getVisibleRoles() != null) {
            dto.setVisibleRoles(Arrays.asList(model.getVisibleRoles().split(",")));
        }

        if (model.getVisibleTenants() != null) {
            dto.setVisibleRoles(Arrays.asList(model.getVisibleTenants().split(",")));
        }

        if (model.getAdditionalProperties() != null) {
            JSONObject additionalProperties = model.getAdditionalProperties();
            Map<String, String> additionalPropertiesMap = new HashMap<>();
            for (Object propertyKey : additionalProperties.keySet()) {
                String key = (String) propertyKey;
                additionalPropertiesMap.put(key, (String) additionalProperties.get(key));
            }
            dto.setAdditionalProperties(additionalPropertiesMap);
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
        List<String> environmentsList = new ArrayList<String>();
        environmentsList.addAll(model.getEnvironments());
        dto.setGatewayEnvironments(environmentsList);
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

        //setting micro-gateway labels if there are any
        if (model.getGatewayLabels() != null) {
            List<LabelDTO> labels = new ArrayList<>();
            List<Label> gatewayLabels = model.getGatewayLabels();
            for (Label label : gatewayLabels) {
                LabelDTO labelDTO = new LabelDTO();
                labelDTO.setName(label.getName());
//                labelDTO.setDescription(label.getDescription()); todo check for description
                labels.add(labelDTO);
            }
            dto.setLabels(labels);
        }
        dto.setAuthorizationHeader(model.getAuthorizationHeader());
        if (model.getApiSecurity() != null)
            dto.setSecurityScheme(Arrays.asList(model.getApiSecurity().split(",")));
        return dto;
    }

    private static APIDTO.VisibilityEnum mapVisibilityFromAPItoDTO(String visibility) {

        switch (visibility) { //public, private,controlled, restricted
            case APIConstants.API_GLOBAL_VISIBILITY:
                return APIDTO.VisibilityEnum.PUBLIC;
            case APIConstants.API_PRIVATE_VISIBILITY:
                return APIDTO.VisibilityEnum.PRIVATE;
            case APIConstants.API_RESTRICTED_VISIBILITY:
                return APIDTO.VisibilityEnum.RESTRICTED;
//            case APIConstants.API_CONTROLLED_VISIBILITY : todo add this to swagger
//                return APIDTO.VisibilityEnum.CONTROLLED;
            default:
                return null; // how to handle this?
        }
    }

    private static APIDTO.SubscriptionAvailabilityEnum mapSubscriptionAvailabilityFromAPItoDTO(
            String subscriptionAvailability) {

        switch (subscriptionAvailability) {
            case APIConstants.SUBSCRIPTION_TO_CURRENT_TENANT:
                return APIDTO.SubscriptionAvailabilityEnum.current_tenant;
            case APIConstants.SUBSCRIPTION_TO_ALL_TENANTS:
                return APIDTO.SubscriptionAvailabilityEnum.all_tenants;
            case APIConstants.SUBSCRIPTION_TO_SPECIFIC_TENANTS:
                return APIDTO.SubscriptionAvailabilityEnum.specific_tenants;
            default:
                return null; // how to handle this?
        }

    }

    private static void setEndpointSecurityFromModelToApiDTO(API api, APIDTO dto) throws APIManagementException {

        if (api.isEndpointSecured()) {
            APIEndpointSecurityDTO securityDTO = new APIEndpointSecurityDTO();
            securityDTO.setType(APIEndpointSecurityDTO.TypeEnum.basic); //set default as basic
            securityDTO.setUsername(api.getEndpointUTUsername());
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(api.getId()
                    .getProviderName()));
            if (checkEndpointSecurityPasswordEnabled(tenantDomain)) {
                securityDTO.setPassword(api.getEndpointUTPassword());
            } else {
                securityDTO.setPassword(""); //Do not expose password
            }
            if (api.isEndpointAuthDigest()) {
                securityDTO.setType(APIEndpointSecurityDTO.TypeEnum.digest);
            }
            dto.setEndpointSecurity(securityDTO);
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
        try {
            APIMRegistryServiceImpl apimRegistryService = new APIMRegistryServiceImpl();
            String content = apimRegistryService.getConfigRegistryResourceContent(tenantDomainName,
                    APIConstants.API_TENANT_CONF_LOCATION);
            if (content != null) {
                JSONParser parser = new JSONParser();
                apiTenantConfig = (JSONObject) parser.parse(content);
                if (apiTenantConfig != null) {
                    Object value = apiTenantConfig.get(APIConstants.API_TENANT_CONF_EXPOSE_ENDPOINT_PASSWORD);
                    if (value != null) {
                        return Boolean.parseBoolean(value.toString());
                    }
                }
            }
        } catch (UserStoreException e) {
            String msg = "UserStoreException thrown when getting API tenant config from registry while reading "
                    + "ExposeEndpointPassword config";
            throw new APIManagementException(msg, e);
        } catch (RegistryException e) {
            String msg = "RegistryException thrown when getting API tenant config from registry while reading "
                    + "ExposeEndpointPassword config";
            throw new APIManagementException(msg, e);
        } catch (ParseException e) {
            String msg = "ParseException thrown when parsing API tenant config from registry while reading "
                    + "ExposeEndpointPassword config";
            throw new APIManagementException(msg, e);
        }
        return false;
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

    /**
     * Return the REST API DTO representation of API Lifecycle state information
     * 
     * @param apiLCData API lifecycle state information
     * @return REST API DTO representation of API Lifecycle state information
     */
    public static LifecycleStateDTO fromLifecycleModelToDTO (Map<String, Object> apiLCData) {
        LifecycleStateDTO lifecycleStateDTO = new LifecycleStateDTO();
        
        String currentState = (String) apiLCData.get(APIConstants.LC_STATUS);
        lifecycleStateDTO.setState(currentState);
        
        String[] nextStates = (String[])apiLCData.get(APIConstants.LC_NEXT_STATES);
        if (nextStates != null) {
            List<LifecycleStateAvailableTransitionsDTO> transitionDTOList = new ArrayList<>();
            for (String state: nextStates) {
                LifecycleStateAvailableTransitionsDTO transitionDTO = new LifecycleStateAvailableTransitionsDTO();
                transitionDTO.setEvent(state);
                //todo: Set target state properly
                transitionDTO.setTargetState("");
                transitionDTOList.add(transitionDTO);
            }
            lifecycleStateDTO.setAvailableTransitions(transitionDTOList);
        }
        
        List checkListItems = (List)apiLCData.get(APIConstants.LC_CHECK_ITEMS);
        if (checkListItems != null) {
            List<LifecycleStateCheckItemsDTO> checkItemsDTOList = new ArrayList<>();
            for (Object checkListItemObj: checkListItems) {
                CheckListItem checkListItem = (CheckListItem)checkListItemObj;
                LifecycleStateCheckItemsDTO checkItemsDTO = new LifecycleStateCheckItemsDTO();
                checkItemsDTO.setName(checkListItem.getName());
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
     * Return the REST API DTO representation of API Lifecycle history information
     *
     * @param lifeCycleEvents API lifecycle history information
     * @return REST API DTO representation of API Lifecycle history information
     */
    public static LifecycleHistoryDTO fromLifecycleHistoryModelToDTO (List<LifeCycleEvent> lifeCycleEvents) {
        LifecycleHistoryDTO historyDTO = new LifecycleHistoryDTO();
        historyDTO.setCount(lifeCycleEvents.size());
        for (LifeCycleEvent event: lifeCycleEvents) {
            LifecycleHistoryItemDTO historyItemDTO = new LifecycleHistoryItemDTO();
            historyItemDTO.setPostState(event.getNewStatus());
            historyItemDTO.setPreviousState(event.getOldStatus());
            historyItemDTO.setUser(event.getUserId());
            
            String updatedTime = RestApiUtil.getRFC3339Date(event.getDate());
            historyItemDTO.setUpdatedTime(updatedTime);
            historyDTO.getList().add(historyItemDTO);
        }
        return historyDTO;
    }
    
    /**
     * This method returns URI templates according to the given list of operations
     *
     * @param operations List operations
     * @return URI Templates
     * @throws APIManagementException
     */
    public static Set<URITemplate> getURITemplates(API model, List<APIOperationsDTO> operations) throws APIManagementException {

        boolean isHttpVerbDefined = false;
        Set<URITemplate> uriTemplates = new LinkedHashSet<>();
        Set<Scope> scopesToAdd = new HashSet<>();

        if (operations != null) {
            for (APIOperationsDTO operation : operations) {
                URITemplate template = new URITemplate();

                String uriTempVal = operation.getUritemplate();
                //if url template is a custom attribute "^x-" ignore.
                if (uriTempVal.startsWith("x-") || uriTempVal.startsWith("X-")) {
                    continue;
                }
                String httpVerb = operation.getHttpVerb();
                List<String> scopeList = operation.getScopes();
                if (scopeList != null) {
                    for (String scopeKey : scopeList) {
                        for (Scope definedScope : model.getScopes()) {
                            if (definedScope.getKey().equalsIgnoreCase(scopeKey)) {
                                Scope scope = new Scope();
                                scope.setKey(scopeKey);
                                scope.setName(definedScope.getName());
                                scope.setDescription(definedScope.getDescription());
                                scope.setRoles(definedScope.getRoles());
                                template.setScopes(scope);
                                template.setScope(scope);

                            }
                        }
                    }
                }

                if (APIConstants.SWAGGER_SUMMARY.equals(httpVerb.toLowerCase())
                        || APIConstants.SWAGGER_DESCRIPTION.equals(httpVerb.toLowerCase())
                        || APIConstants.SWAGGER_SERVERS.equals(httpVerb.toLowerCase())
                        || APIConstants.PARAMETERS.equals(httpVerb.toLowerCase())
                        || httpVerb.startsWith("x-")
                        || httpVerb.startsWith("X-")) {
                    // openapi 3.x allow 'summary', 'description' and extensions in PathItem Object.
                    // which we are not interested at this point
                    continue;
                }
                //Only continue for supported operations
                else if (APIConstants.SUPPORTED_METHODS.contains(httpVerb.toLowerCase())) {
                    isHttpVerbDefined = true;
                    String authType = operation.getAuthType();
                    if ("Application & Application User".equals(authType)) {
                        authType = APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN;
                    } else if ("Application User".equals(authType)) {
                        authType = APIConstants.AUTH_APPLICATION_USER_LEVEL_TOKEN;
                    } else if ("None".equals(authType)) {
                        authType = APIConstants.AUTH_NO_AUTHENTICATION;
                    } else if ("Application".equals(authType)) {
                        authType = APIConstants.AUTH_APPLICATION_LEVEL_TOKEN;
                    } else {
                        authType = APIConstants.AUTH_APPLICATION_OR_USER_LEVEL_TOKEN;
                    }
                    template.setThrottlingTier(operation.getPolicy());
                    template.setThrottlingTiers(operation.getPolicy());
//                        template.setMediationScript((String) operation.get(APIConstants todo
//                                .SWAGGER_X_MEDIATION_SCRIPT));
//                        template.setMediationScripts(httpVerb.toUpperCase(), (String) operation.get( todo
//                                APIConstants.SWAGGER_X_MEDIATION_SCRIPT));
                    template.setUriTemplate(uriTempVal);
                    template.setHTTPVerb(httpVerb.toUpperCase());
                    template.setHttpVerbs(httpVerb.toUpperCase());
                    template.setAuthType(authType);
                    template.setAuthTypes(authType);

                    uriTemplates.add(template);
                } else {
                    handleException("The HTTP method '" + httpVerb + "' provided for resource '" + uriTempVal
                            + "' is invalid");
                }
//                    JSONObject path = (JSONObject) paths.get(uriTempVal);todo
                // Following code check is done to handle $ref objects supported by swagger spec
                // See field types supported by "Path Item Object" in swagger spec.
                   /* if (operation.("$ref")) {
                        log.info("Reference " + uriTempVal + " path object was ignored when generating URL template " +
                                "for api \"" + api.getId().getApiName() + '\"');
                        continue;
                    }*/

                if (!isHttpVerbDefined) {
                    handleException("Resource '" + uriTempVal + "' has global parameters without " +
                            "HTTP methods");
                }
            }
        }

        return uriTemplates;
    }

    /**
     * This method returns the oauth scopes according to the given list of scopes
     *
     * @param apiDTO list of scopes
     * @return scope set
     */
    public static Set<Scope> getScopes(APIDTO apiDTO) {

        Set<Scope> scopeSet = new LinkedHashSet<>();
        for (ScopeDTO scopeDTO : apiDTO.getScopes()) {
            Scope scope = new Scope();
            scope.setKey(scopeDTO.getName());
            scope.setName(scopeDTO.getName());
            scope.setDescription(scopeDTO.getDescription());
            scope.setRoles(String.join(",", scopeDTO.getBindings().getValues()));
            scopeSet.add(scope);
        }
        return scopeSet;
    }

    /**
     * This method returns endpoints according to the given endpoint config
     *
     * @param endpoints endpoints given
     * @return String endpoint config
     */
    public static String getEndpointConfigString(List<APIEndpointDTO> endpoints) {
        //todo improve this logic to support multiple endpoints such as failorver and load balance
        StringBuilder sb = new StringBuilder();
        if (endpoints != null && endpoints.size() > 0) {
            sb.append("{");
            for (APIEndpointDTO endpoint : endpoints) {
                sb.append("\"")
                        .append(endpoint.getType())
                        .append("\": {\"url\":\"")
                        .append(endpoint.getInline().getEndPointConfig().getList().get(0).getUrl())
                        .append("\",\"timeout\":\"")
                        .append(endpoint.getInline().getEndPointConfig().getList().get(0).getTimeout())
                        .append("\"},");
            }
            sb.append("\"endpoint_type\" : \"")
                    .append(endpoints.get(0).getType())//assuming all the endpoints are same type
                    .append("\"}\n");
        }
        return sb.toString();
    }

    private static EndPointEndPointConfigDTO getEndPointEndPointConfigDTO(EndPointEndPointConfig endPointEndPointConfig) {

        //map to EndPointEndPointConfig model to EndPointEndPointConfigDTO
        EndPointEndPointConfigDTO endPointEndPointConfigDTO = new EndPointEndPointConfigDTO();
        switch (endPointEndPointConfig.getEndpointType()) {
            case SINGLE:
                endPointEndPointConfigDTO.setEndpointType(EndPointEndPointConfigDTO.EndpointTypeEnum.SINGLE);
            case LOAD_BALANCED:
                endPointEndPointConfigDTO.setEndpointType(EndPointEndPointConfigDTO.EndpointTypeEnum.LOAD_BALANCED);
            case FAIL_OVER:
                endPointEndPointConfigDTO.setEndpointType(EndPointEndPointConfigDTO.EndpointTypeEnum.FAIL_OVER);
        }
        List<EndPointConfigDTO> endPointConfigDTOList = new ArrayList<>();
        for (EndpointConfig endpointConfig : endPointEndPointConfig.getList()) {
            EndPointConfigDTO endPointConfigDTO = new EndPointConfigDTO();
            endPointConfigDTO.setUrl(endpointConfig.getUrl());
            endPointConfigDTO.setIsPrimary(endpointConfig.getPrimary());
            endPointConfigDTO.setTimeout(endpointConfig.getTimeout());

            //map EndPointConfigAttributes model to EndPointConfigAttributesDTO
            List<EndPointConfigAttributesDTO> endPointConfigAttributesList = new ArrayList<>();
            for (EndPointConfigAttributes endPointConfigAttributes : endpointConfig.getAttributes()) {
                EndPointConfigAttributesDTO endPointConfigAttributeDTO = new EndPointConfigAttributesDTO();
                endPointConfigAttributeDTO.setName(endPointConfigAttributes.getName());
                endPointConfigAttributeDTO.setValue(endPointConfigAttributes.getValue());
                endPointConfigAttributesList.add(endPointConfigAttributeDTO);
            }
            endPointConfigDTO.setAttributes(endPointConfigAttributesList);
            endPointConfigDTOList.add(endPointConfigDTO);
        }
        endPointEndPointConfigDTO.setList(endPointConfigDTOList);
        return endPointEndPointConfigDTO;
    }

    /**
     * This method converts Endpoint:EndpontConfig DTO to corresponding model
     *
     * @param apiEndpointDTO1 egiven endpoint config
     * @param type            given endpoint type  SINGLE,  LOAD_BALANCED,  FAIL_OVER
     * @return EndPointConfig model
     */
    private EndPointEndPointConfig getEndPointEndPointConfigModel(EndPointEndPointConfigDTO apiEndpointDTO1,
                                                                  EndPointEndPointConfig.EndpointTypeEnum type) {

        //mapping properties in EndPointConfigDTO to EndpointConfig model
        List<EndpointConfig> configList = new ArrayList<>();
        for (EndPointConfigDTO endPointConfigDTO : apiEndpointDTO1.getList()) {
            EndpointConfig endpointConfig1 = new EndpointConfig();
            endpointConfig1.setUrl(endPointConfigDTO.getUrl());
            endpointConfig1.setTimeout(endPointConfigDTO.getTimeout());
            endpointConfig1.setPrimary(endPointConfigDTO.getIsPrimary());

            //mapping attributes in EndPointConfigAttributesDTO to EndPointConfigAttributes model
            List<EndPointConfigAttributes> endPointConfigAttributesList = new ArrayList<>();
            for (EndPointConfigAttributesDTO endPointConfigAttributesDTO : endPointConfigDTO.getAttributes()) {
                EndPointConfigAttributes endPointConfigAttribute = new EndPointConfigAttributes();
                endPointConfigAttribute.setName(endPointConfigAttributesDTO.getName());
                endPointConfigAttribute.setValue(endPointConfigAttributesDTO.getValue());

                endPointConfigAttributesList.add(endPointConfigAttribute);
            }

            endpointConfig1.setAttributes(endPointConfigAttributesList);
            configList.add(endpointConfig1);
        }

        //mapping properties in EndPointEndPointConfigDTO to EndPointEndPointConfig model
        EndPointEndPointConfig endpointConfig = new EndPointEndPointConfig();
        endpointConfig.setEndpointType(type);
        endpointConfig.setList(configList);

        return endpointConfig;

    }

    /**
     * This method returns api security scheme as a comma seperated string
     *
     * @param securitySchemes api security scheme
     * @return comma seperated string of api security schemes
     */
    public static String getSecurityScheme(List<String> securitySchemes) {

        if (securitySchemes == null || securitySchemes.size() <= 0) {
            return "";
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

    /**
     * This method converts APIEndpoint model to corresponding APIEndpointDTO object
     *
     * @param apiEndpointsList list of api endpoints
     * @return APIEndpointDTO List of apiEndpointDTO
     */
    public static List<APIEndpointDTO> getAPIEndpointDTO(List<APIEndpoint> apiEndpointsList) {

        List<APIEndpointDTO> apiEndpointDTOList = new ArrayList<>(apiEndpointsList.size());

        for (APIEndpoint apiEndpoint : apiEndpointsList) {
            APIEndpointDTO apiEndpointDTO = new APIEndpointDTO();
            Endpoint endpoint = apiEndpoint.getInline();
            EndpointSecurity endpointSecurity = endpoint.getEndpointSecurity();
            EndPointDTO endPointDTO = new EndPointDTO();

            EndPointEndpointSecurityDTO endPointEndpointSecurityDTO = new EndPointEndpointSecurityDTO();

            endPointEndpointSecurityDTO.setEnabled(endpointSecurity.getEnabled());
            endPointEndpointSecurityDTO.setPassword(endpointSecurity.getPassword());
            endPointEndpointSecurityDTO.setUsername(endpointSecurity.getUsername());
            endPointEndpointSecurityDTO.setType(endpointSecurity.getType());

            endPointDTO.setEndpointSecurity(endPointEndpointSecurityDTO);
            endPointDTO.setEndPointConfig(getEndPointEndPointConfigDTO(endpoint.getEndpointConfig()));
            endPointDTO.setId(endpoint.getId());
            endPointDTO.setMaxTps(endpoint.getMaxTps());
            endPointDTO.setName(endpoint.getName());
            endPointDTO.setType(endpoint.getType());

            apiEndpointDTO.setInline(endPointDTO);
            apiEndpointDTO.setKey(apiEndpoint.getKey());
            apiEndpointDTO.setType(apiEndpoint.getType());

            apiEndpointDTOList.add(apiEndpointDTO);
        }

        return apiEndpointDTOList;
    }

    private static List<APIOperationsDTO> getOperationsFromSwaggerDef(String swaggerDefinition) {
//todo
        List<APIOperationsDTO> operationsDTOList = new ArrayList<>();
        if (!StringUtils.isEmpty(swaggerDefinition)) {

        }

        return operationsDTOList;
    }
}