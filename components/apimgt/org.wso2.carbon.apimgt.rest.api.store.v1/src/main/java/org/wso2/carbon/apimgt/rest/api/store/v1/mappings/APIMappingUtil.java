/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.rest.api.store.v1.mappings;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APICategory;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProductResource;
import org.wso2.carbon.apimgt.api.model.APIRevisionDeployment;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Label;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.VHost;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerConfiguration;
import org.wso2.carbon.apimgt.impl.APIType;
import org.wso2.carbon.apimgt.impl.containermgt.ContainerBasedConstants;
import org.wso2.carbon.apimgt.impl.dto.Environment;
import org.wso2.carbon.apimgt.impl.internal.ServiceReferenceHolder;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.store.v1.utils.APIUtils;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class APIMappingUtil {

    public static APIDTO fromAPItoDTO(API model, String tenantDomain) throws APIManagementException {

        APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
        APIDTO dto = new APIDTO();
        dto.setName(model.getId().getApiName());
        dto.setVersion(model.getId().getVersion());
        String providerName = model.getId().getProviderName();
        dto.setProvider(APIUtil.replaceEmailDomainBack(providerName));
        dto.setId(model.getUUID());
        dto.setContext(model.getContext());
        dto.setDescription(model.getDescription());
        dto.setIsDefaultVersion(model.isDefaultVersion());
        dto.setLifeCycleStatus(model.getStatus());
        dto.setType(model.getType());
        dto.setAvgRating(String.valueOf(model.getRating()));

        Set<Scope> scopes = model.getScopes();
        Map<String, ScopeInfoDTO> uniqueScope = new HashMap<>();

        for (Scope scope : scopes) {
            if (!uniqueScope.containsKey(scope.getKey())) {
                ScopeInfoDTO scopeInfoDTO = new ScopeInfoDTO().
                        key(scope.getKey()).
                        name(scope.getName()).
                        description(scope.getDescription());
                if (StringUtils.isNotBlank(scope.getRoles())) {
                    scopeInfoDTO.roles(Arrays.asList(scope.getRoles().trim().split(",")));
                }
                uniqueScope.put(scope.getKey(), scopeInfoDTO);
            }
        }

        dto.setScopes(new ArrayList<>(uniqueScope.values()));

        if (null != model.getLastUpdated()) {
            Date lastUpdateDate = model.getLastUpdated();
            Timestamp timeStamp = new Timestamp(lastUpdateDate.getTime());
            dto.setLastUpdatedTime(String.valueOf(timeStamp));
        }

        String createdTimeStamp = model.getCreatedTime();
        if (null != createdTimeStamp) {
            Date date = new Date(Long.valueOf(createdTimeStamp));
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String dateFormatted = formatter.format(date);
            dto.setCreatedTime(dateFormatted);
        }

        String apiDefinition = null;
        if (model.isAsync()) {
            // for asyncAPI retrieve asyncapi.yml specification
            apiDefinition = apiConsumer.getAsyncAPIDefinition(model.getUuid(), tenantDomain);
        } else {
            // retrieve open API definition
            if (model.getSwaggerDefinition() != null) {
                apiDefinition = model.getSwaggerDefinition();
            } else {
                apiDefinition = apiConsumer.getOpenAPIDefinition(model.getUuid(), tenantDomain);
            }
        }
        dto.setApiDefinition(apiDefinition);

        if (APIConstants.APITransportType.GRAPHQL.toString().equals(model.getType())) {
            List<APIOperationsDTO> operationList = new ArrayList<>();
            for (URITemplate template : model.getUriTemplates()) {
                APIOperationsDTO operation = new APIOperationsDTO();
                operation.setTarget(template.getUriTemplate());
                operation.setVerb(template.getHTTPVerb());
                operationList.add(operation);
            }
            dto.setOperations(operationList);
        }

        Set<String> apiTags = model.getTags();
        List<String> tagsToReturn = new ArrayList<>();
        tagsToReturn.addAll(apiTags);
        dto.setTags(tagsToReturn);

        //set the monetization status of this API (enabled or disabled)
        APIMonetizationInfoDTO monetizationInfoDTO = new APIMonetizationInfoDTO();
        monetizationInfoDTO.enabled(model.getMonetizationStatus());
        dto.setMonetization(monetizationInfoDTO);

        Set<org.wso2.carbon.apimgt.api.model.Tier> apiTiers = model.getAvailableTiers();
        List<APITiersDTO> tiersToReturn = new ArrayList<>();
        int tenantId = 0;
        if (!StringUtils.isBlank(tenantDomain)) {
            tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        }
        Set<String> deniedTiers = apiConsumer.getDeniedTiers(tenantId);
        for (org.wso2.carbon.apimgt.api.model.Tier currentTier : apiTiers) {
            if (!deniedTiers.contains(currentTier.getName())) {
                APITiersDTO apiTiersDTO = new APITiersDTO();
                apiTiersDTO.setTierName(currentTier.getName());
                apiTiersDTO.setTierPlan(currentTier.getTierPlan());
                //monetization attributes are applicable only for commercial tiers
                if (APIConstants.COMMERCIAL_TIER_PLAN.equalsIgnoreCase(currentTier.getTierPlan())) {
                    APIMonetizationAttributesDTO monetizationAttributesDTO = new APIMonetizationAttributesDTO();
                    if (MapUtils.isNotEmpty(currentTier.getMonetizationAttributes())) {
                        Map<String, String> monetizationAttributes = currentTier.getMonetizationAttributes();
                        //check for the billing plan (fixed or price per request)
                        if (!StringUtils.isBlank(monetizationAttributes.get(APIConstants.Monetization.FIXED_PRICE))) {
                            monetizationAttributesDTO.setFixedPrice(monetizationAttributes.get
                                    (APIConstants.Monetization.FIXED_PRICE));
                        } else if (!StringUtils.isBlank(monetizationAttributes.get(
                                APIConstants.Monetization.PRICE_PER_REQUEST))) {
                            monetizationAttributesDTO.setPricePerRequest(monetizationAttributes.get
                                    (APIConstants.Monetization.PRICE_PER_REQUEST));
                        }
                        monetizationAttributesDTO.setCurrencyType(monetizationAttributes.get
                                (APIConstants.Monetization.CURRENCY) != null ? monetizationAttributes.get
                                (APIConstants.Monetization.CURRENCY) : StringUtils.EMPTY);
                        monetizationAttributesDTO.setBillingCycle(monetizationAttributes.get
                                (APIConstants.Monetization.BILLING_CYCLE) != null ? monetizationAttributes.get
                                (APIConstants.Monetization.BILLING_CYCLE) : StringUtils.EMPTY);
                    }
                    apiTiersDTO.setMonetizationAttributes(monetizationAttributesDTO);
                }
                tiersToReturn.add(apiTiersDTO);
            }
        }
        dto.setTiers(tiersToReturn);

        dto.setTransport(Arrays.asList(model.getTransports().split(",")));

        dto.setEndpointURLs(APIUtils.extractEndpointURLs(model, tenantDomain));

        dto.setIngressURLs(extractIngressURLs(model));

        APIBusinessInformationDTO apiBusinessInformationDTO = new APIBusinessInformationDTO();
        apiBusinessInformationDTO.setBusinessOwner(model.getBusinessOwner());
        apiBusinessInformationDTO.setBusinessOwnerEmail(model.getBusinessOwnerEmail());
        apiBusinessInformationDTO.setTechnicalOwner(model.getTechnicalOwner());
        apiBusinessInformationDTO.setTechnicalOwnerEmail(model.getTechnicalOwnerEmail());
        dto.setBusinessInformation(apiBusinessInformationDTO);

        if (!StringUtils.isBlank(model.getThumbnailUrl())) {
            dto.setHasThumbnail(true);
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

        dto.setWsdlUri(model.getWsdlUrl());


        if (model.getGatewayLabels() != null) {
            dto.setLabels(getLabelDetails(model.getGatewayLabels(), model.getContext()));
        }

        if (model.getEnvironmentList() != null) {
            List<String> environmentListToReturn = new ArrayList<>();
            environmentListToReturn.addAll(model.getEnvironmentList());
            dto.setEnvironmentList(environmentListToReturn);
        }

        dto.setAuthorizationHeader(model.getAuthorizationHeader());
        if (model.getApiSecurity() != null) {
            dto.setSecurityScheme(Arrays.asList(model.getApiSecurity().split(",")));
        }

        dto.setAdvertiseInfo(extractAdvertiseInfo(model));
        String apiTenant = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(model.getId()
                .getProviderName()));
        String subscriptionAvailability = model.getSubscriptionAvailability();
        String subscriptionAllowedTenants = model.getSubscriptionAvailableTenants();
        dto.setIsSubscriptionAvailable(isSubscriptionAvailable(apiTenant, subscriptionAvailability,
                subscriptionAllowedTenants));

        List<APICategory> apiCategories = model.getApiCategories();
        List<String> categoryNamesList = new ArrayList<>();
        if (apiCategories != null && !apiCategories.isEmpty()) {
            for (APICategory category : apiCategories) {
                categoryNamesList.add(category.getName());
            }
        }
        dto.setCategories(categoryNamesList);
        dto.setKeyManagers(model.getKeyManagers());

        return dto;
    }

    public static APIDTO fromAPItoDTO(APIProduct model, String tenantDomain) throws APIManagementException {
        APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
        APIDTO dto = new APIDTO();
        dto.setName(model.getId().getName());
        dto.setVersion(model.getId().getVersion());
        String providerName = model.getId().getProviderName();
        dto.setProvider(APIUtil.replaceEmailDomainBack(providerName));
        dto.setId(model.getUuid());
        dto.setContext(model.getContext());
        dto.setDescription(model.getDescription());
        dto.setLifeCycleStatus(model.getState());
        dto.setType(model.getType());
        dto.setAvgRating(String.valueOf(model.getRating()));

        /* todo: created and last updated times
        if (null != model.getLastUpdated()) {
            Date lastUpdateDate = model.getLastUpdated();
            Timestamp timeStamp = new Timestamp(lastUpdateDate.getTime());
            dto.setLastUpdatedTime(String.valueOf(timeStamp));
        }

        String createdTimeStamp = model.getCreatedTime();
        if (null != createdTimeStamp) {
            Date date = new Date(Long.valueOf(createdTimeStamp));
            DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            String dateFormatted = formatter.format(date);
            dto.setCreatedTime(dateFormatted);
        } */

        String apiDefinition = null;
        if (model.isAsync()) {
            // for asyncAPI retrieve asyncapi.yml specification
            apiDefinition = apiConsumer.getAsyncAPIDefinition(model.getUuid(), tenantDomain);
        } else {
            // retrieve open API definition
            if (model.getDefinition() != null) {
                apiDefinition = model.getDefinition();
            } else {
                apiDefinition = apiConsumer.getOpenAPIDefinition(model.getUuid(), tenantDomain);
            }
        }
        dto.setApiDefinition(apiDefinition);

        Set<String> apiTags = model.getTags();
        List<String> tagsToReturn = new ArrayList<>();
        tagsToReturn.addAll(apiTags);
        dto.setTags(tagsToReturn);

        Set<org.wso2.carbon.apimgt.api.model.Tier> apiTiers = model.getAvailableTiers();
        List<APITiersDTO> tiersToReturn = new ArrayList<>();

        int tenantId = 0;
        if (!StringUtils.isBlank(tenantDomain)) {
            tenantId = APIUtil.getTenantIdFromTenantDomain(tenantDomain);
        }

        //set the monetization status of this API (enabled or disabled)
        APIMonetizationInfoDTO monetizationInfoDTO = new APIMonetizationInfoDTO();
        monetizationInfoDTO.enabled(model.getMonetizationStatus());
        dto.setMonetization(monetizationInfoDTO);

        Set<String> deniedTiers = apiConsumer.getDeniedTiers(tenantId);
        for (org.wso2.carbon.apimgt.api.model.Tier currentTier : apiTiers) {
            if (!deniedTiers.contains(currentTier.getName())) {
                APITiersDTO apiTiersDTO = new APITiersDTO();
                apiTiersDTO.setTierName(currentTier.getName());
                apiTiersDTO.setTierPlan(currentTier.getTierPlan());
                //monetization attributes are applicable only for commercial tiers
                if (APIConstants.COMMERCIAL_TIER_PLAN.equalsIgnoreCase(currentTier.getTierPlan())) {
                    APIMonetizationAttributesDTO monetizationAttributesDTO = new APIMonetizationAttributesDTO();
                    if (MapUtils.isNotEmpty(currentTier.getMonetizationAttributes())) {
                        Map<String, String> monetizationAttributes = currentTier.getMonetizationAttributes();
                        //check the billing plan (fixed or price per request)
                        if (!StringUtils.isBlank(monetizationAttributes.get(APIConstants.Monetization.FIXED_PRICE))) {
                            monetizationAttributesDTO.setFixedPrice(monetizationAttributes.get
                                    (APIConstants.Monetization.FIXED_PRICE));
                        } else if (!StringUtils.isBlank(monetizationAttributes.get(
                                APIConstants.Monetization.PRICE_PER_REQUEST))) {
                            monetizationAttributesDTO.setPricePerRequest(monetizationAttributes.get
                                    (APIConstants.Monetization.PRICE_PER_REQUEST));
                        }
                        monetizationAttributesDTO.setCurrencyType(monetizationAttributes.get
                                (APIConstants.Monetization.CURRENCY) != null ? monetizationAttributes.get
                                (APIConstants.Monetization.CURRENCY) : StringUtils.EMPTY);
                        monetizationAttributesDTO.setBillingCycle(monetizationAttributes.get
                                (APIConstants.Monetization.BILLING_CYCLE) != null ? monetizationAttributes.get
                                (APIConstants.Monetization.BILLING_CYCLE) : StringUtils.EMPTY);
                    }
                    apiTiersDTO.setMonetizationAttributes(monetizationAttributesDTO);
                }
                tiersToReturn.add(apiTiersDTO);
            }
        }
        dto.setTiers(tiersToReturn);

        List<APIOperationsDTO> operationList = new ArrayList<>();
        Map<String, ScopeInfoDTO> uniqueScopes = new HashMap<>();
        for (APIProductResource productResource : model.getProductResources()) {
            URITemplate uriTemplate = productResource.getUriTemplate();
            APIOperationsDTO operation = new APIOperationsDTO();
            operation.setTarget(uriTemplate.getUriTemplate());
            operation.setVerb(uriTemplate.getHTTPVerb());
            operationList.add(operation);

            List<Scope> scopes = uriTemplate.retrieveAllScopes();
            for (Scope scope : scopes) {
                if (!uniqueScopes.containsKey(scope.getKey())) {
                    ScopeInfoDTO scopeInfoDTO = new ScopeInfoDTO().
                            key(scope.getKey()).
                            name(scope.getName()).
                            description(scope.getDescription());
                    if (StringUtils.isNotBlank(scope.getRoles())) {
                        scopeInfoDTO.roles(Arrays.asList(scope.getRoles().trim().split(",")));
                    }
                    uniqueScopes.put(scope.getKey(), scopeInfoDTO);
                }
            }
        }

        dto.setOperations(operationList);

        dto.setScopes(new ArrayList<>(uniqueScopes.values()));

        dto.setTransport(Arrays.asList(model.getTransports().split(",")));

        dto.setEndpointURLs(APIUtils.extractEndpointURLs(model, tenantDomain));

        APIBusinessInformationDTO apiBusinessInformationDTO = new APIBusinessInformationDTO();
        apiBusinessInformationDTO.setBusinessOwner(model.getBusinessOwner());
        apiBusinessInformationDTO.setBusinessOwnerEmail(model.getBusinessOwnerEmail());
        apiBusinessInformationDTO.setTechnicalOwner(model.getTechnicalOwner());
        apiBusinessInformationDTO.setTechnicalOwnerEmail(model.getTechnicalOwnerEmail());
        dto.setBusinessInformation(apiBusinessInformationDTO);

        if (!StringUtils.isBlank(model.getThumbnailUrl())) {
            dto.setHasThumbnail(true);
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


        if (model.getGatewayLabels() != null) {
            dto.setLabels(getLabelDetails(model.getGatewayLabels(), model.getContext()));
        }

        if (model.getEnvironments() != null) {
            List<String> environmentListToReturn = new ArrayList<>(model.getEnvironments());
            dto.setEnvironmentList(environmentListToReturn);
        }

        dto.setAuthorizationHeader(model.getAuthorizationHeader());
        if (model.getApiSecurity() != null) {
            dto.setSecurityScheme(Arrays.asList(model.getApiSecurity().split(",")));
        }

        //Since same APIInfoDTO is used for APIProduct in StoreUI set default AdvertisedInfo to the DTO
        AdvertiseInfoDTO advertiseInfoDTO = new AdvertiseInfoDTO();
        advertiseInfoDTO.setAdvertised(false);
        dto.setAdvertiseInfo(advertiseInfoDTO);
        String apiTenant = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(model.getId()
                .getProviderName()));
        String subscriptionAvailability = model.getSubscriptionAvailability();
        String subscriptionAllowedTenants = model.getSubscriptionAvailableTenants();
        dto.setIsSubscriptionAvailable(isSubscriptionAvailable(apiTenant, subscriptionAvailability,
                subscriptionAllowedTenants));
        return dto;
    }


    public static APIDTO fromAPItoDTO(ApiTypeWrapper model, String tenantDomain) throws APIManagementException {
        if (model.isAPIProduct()) {
            return fromAPItoDTO(model.getApiProduct(), tenantDomain);
        } else {
            return fromAPItoDTO(model.getApi(), tenantDomain);
        }
    }

    public static List<APIEndpointURLsDTO> fromAPIRevisionListToEndpointsList(
            APIDTO apidto,
            List<APIRevisionDeployment> revisionDeployments) throws APIManagementException {

        Map<String, Environment> environments = APIUtil.getEnvironments();
        return revisionDeployments.stream().filter(APIRevisionDeployment::isDisplayOnDevportal)
                .map(r -> fromAPIRevisionToEndpoints(apidto, r, environments))
                .collect(Collectors.toList());
    }

    private static APIEndpointURLsDTO fromAPIRevisionToEndpoints(APIDTO apidto,
                                                                 APIRevisionDeployment revisionDeployment,
                                                                 Map<String, Environment> environments) {
        // Deployed environment
        Environment environment = environments.get(revisionDeployment.getDeployment());
        // If there are any inconstancy (if VHost not found) use default VHost
        VHost defaultVhost = new VHost();
        defaultVhost.setHost(revisionDeployment.getVhost());
        defaultVhost.setHttpContext("");
        defaultVhost.setHttpsPort(APIConstants.HTTPS_PROTOCOL_PORT);
        defaultVhost.setHttpPort(APIConstants.HTTP_PROTOCOL_PORT);
        defaultVhost.setWsPort(APIConstants.WS_PROTOCOL_PORT);
        defaultVhost.setWssPort(APIConstants.WSS_PROTOCOL_PORT);

        // Deployed VHost
        VHost vHost;
        if (revisionDeployment.getVhost() == null && environment.getVhosts().size() > 0) {
            // VHost is NULL set first Vhost (set in deployment toml)
            vHost = environment.getVhosts().get(0);
        } else {
            vHost = environment.getVhosts().stream()
                    .filter(v -> StringUtils.equals(v.getHost(), revisionDeployment.getVhost()))
                    .findAny()
                    .orElse(defaultVhost);
        }

        APIEndpointURLsDTO apiEndpointURLsDTO = new APIEndpointURLsDTO();
        apiEndpointURLsDTO.setEnvironmentName(environment.getName());
        apiEndpointURLsDTO.setEnvironmentType(environment.getType());

        APIURLsDTO apiurLsDTO = new APIURLsDTO();
        String context = apidto.getContext();
        boolean isWs = apidto.getEndpointURLs().size() > 0
                && apidto.getEndpointURLs().get(0).getUrLs().getWs() != null;
        if (!isWs) {
            apiurLsDTO.setHttp(vHost.getHttpUrl() + context);
            apiurLsDTO.setHttps(vHost.getHttpsUrl() + context);
        } else {
            apiurLsDTO.setWs(vHost.getWsUrl() + context);
            apiurLsDTO.setWss(vHost.getWssUrl() + context);
        }
        apiEndpointURLsDTO.setUrLs(apiurLsDTO);

        APIDefaultVersionURLsDTO apiDefaultVersionURLsDTO = new APIDefaultVersionURLsDTO();
        if (apidto.isIsDefaultVersion() != null && apidto.isIsDefaultVersion()) {
            String defaultContext = context.replaceAll("/" + apidto.getVersion() + "$", "");
            if (!isWs) {
                apiDefaultVersionURLsDTO.setHttp(vHost.getHttpUrl() + defaultContext);
                apiDefaultVersionURLsDTO.setHttps(vHost.getHttpsUrl() + defaultContext);
            } else {
                apiDefaultVersionURLsDTO.setWs(vHost.getWsUrl() + defaultContext);
                apiDefaultVersionURLsDTO.setWss(vHost.getWssUrl() + defaultContext);
            }
        }
        apiEndpointURLsDTO.setDefaultVersionURLs(apiDefaultVersionURLsDTO);

        return apiEndpointURLsDTO;
    }

    /**
     * Returns an API with minimal info given the uuid.
     *
     * @param apiUUID               API uuid
     * @param requestedTenantDomain tenant domain of the API
     * @return API which represents the given id
     * @throws APIManagementException
     */
    public static API getAPIInfoFromUUID(String apiUUID, String requestedTenantDomain)
            throws APIManagementException {
        API api;
        String username = RestApiCommonUtil.getLoggedInUsername();
        APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
        api = apiConsumer.getLightweightAPIByUUID(apiUUID, requestedTenantDomain);
        return api;
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
     * Sets pagination urls for a APIListDTO object given pagination parameters and url parameters
     *
     * @param apiListDTO APIListDTO object to which pagination urls need to be set
     * @param query      query parameter
     * @param offset     starting index
     * @param limit      max number of returned objects
     * @param size       max offset
     */
    public static void setPaginationParams(APIListDTO apiListDTO, String query, int offset, int limit, int size) {
        Map<String, Integer> paginatedParams = RestApiCommonUtil.getPaginationParams(offset, limit, size);

        String paginatedPrevious = "";
        String paginatedNext = "";

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
        apiListDTO.setPagination(paginationDTO);
    }

    /**
     * Converts an API Set object into corresponding REST API DTO
     *
     * @param apiSet Set of API objects
     * @return APIListDTO object
     */
    public static APIListDTO fromAPISetToDTO(Set<API> apiSet) {
        APIListDTO apiListDTO = new APIListDTO();
        List<APIInfoDTO> apiInfoDTOs = apiListDTO.getList();
        if (apiInfoDTOs == null) {
            apiInfoDTOs = new ArrayList<>();
            apiListDTO.setList(apiInfoDTOs);
        }
        for (API api : apiSet) {
            apiInfoDTOs.add(fromAPIToInfoDTO(api));
        }
        apiListDTO.setCount(apiSet.size());

        return apiListDTO;
    }

    /**
     * Converts a JSONObject to corresponding RatingDTO
     *
     * @param obj JSON Object to be converted
     * @return RatingDTO object
     */
    public static RatingDTO fromJsonToRatingDTO(JSONObject obj) {
        RatingDTO ratingDTO = new RatingDTO();
        if (obj != null) {
            ratingDTO.setRatingId(String.valueOf(obj.get(APIConstants.RATING_ID)));
            ratingDTO.setRatedBy((String) obj.get(APIConstants.USERNAME));
            ratingDTO.setRating((Integer) obj.get(APIConstants.RATING));
        }
        return ratingDTO;
    }

    /**
     * Converts a List object of Ratings into a DTO
     *
     * @param ratings List of Ratings
     * @param limit   maximum number of ratings to be returned
     * @param offset  starting index
     * @return RatingListDTO object containing Rating DTOs
     */
    public static RatingListDTO fromRatingListToDTO(List<RatingDTO> ratings, int offset, int limit) {
        RatingListDTO ratingListDTO = new RatingListDTO();
        List<RatingDTO> ratingDTOs = ratingListDTO.getList();
        if (ratingDTOs == null) {
            ratingDTOs = new ArrayList<>();
            ratingListDTO.setList(ratingDTOs);
        }

        //add the required range of objects to be returned
        int start = offset < ratings.size() && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = offset + limit - 1 <= ratings.size() - 1 ? offset + limit - 1 : ratings.size() - 1;
        for (int i = start; i <= end; i++) {
            ratingDTOs.add(ratings.get(i));
        }
        ratingListDTO.setCount(ratingDTOs.size());
        return ratingListDTO;
    }

    /**
     * Sets pagination urls for a RatingListDTO object given pagination parameters and url parameters
     *
     * @param ratingListDTO a RatingListDTO object
     * @param limit         max number of objects returned
     * @param offset        starting index
     * @param size          max offset
     */
    public static void setRatingPaginationParams(RatingListDTO ratingListDTO, String apiId, int offset, int limit,
                                                 int size) {
        //acquiring pagination parameters and setting pagination urls
        Map<String, Integer> paginatedParams = RestApiCommonUtil.getPaginationParams(offset, limit, size);
        String paginatedPrevious = "";
        String paginatedNext = "";

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = RestApiCommonUtil
                    .getRatingPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT), apiId);
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = RestApiCommonUtil
                    .getRatingPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT), apiId);
        }

        PaginationDTO paginationDTO = CommonMappingUtil
                .getPaginationDTO(limit, offset, size, paginatedNext, paginatedPrevious);
        ratingListDTO.setPagination(paginationDTO);
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
     * Converts a List object of APIs into a DTO
     *
     * @param apiList List of APIs
     * @return APIListDTO object containing APIDTOs
     */
    public static APIListDTO fromAPIListToDTO(List<Object> apiList) {
        APIListDTO apiListDTO = new APIListDTO();
        List<APIInfoDTO> apiInfoDTOs = apiListDTO.getList();
        if (apiList != null) {
            for (Object api : apiList) {
                if (api instanceof API) {
                    apiInfoDTOs.add(fromAPIToInfoDTO((API) api));
                } else if (api instanceof APIProduct) {
                    apiInfoDTOs.add(fromAPIToInfoDTO((APIProduct) api));
                }
            }
        }
        apiListDTO.setCount(apiInfoDTOs.size());
        return apiListDTO;
    }

    /**
     * Creates a minimal DTO representation of an API object
     *
     * @param api API object
     * @return a minimal representation DTO
     */
    static APIInfoDTO fromAPIToInfoDTO(API api) {
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
        apiInfoDTO.setProvider(apiId.getProviderName());
        apiInfoDTO.setLifeCycleStatus(api.getStatus());
        apiInfoDTO.setType(api.getType());
        apiInfoDTO.setAvgRating(String.valueOf(api.getRating()));
        String providerName = api.getId().getProviderName();
        apiInfoDTO.setProvider(APIUtil.replaceEmailDomainBack(providerName));
        Set<Tier> throttlingPolicies = api.getAvailableTiers();
        List<String> throttlingPolicyNames = new ArrayList<>();
        for (Tier tier : throttlingPolicies) {
            throttlingPolicyNames.add(tier.getName());
        }
        apiInfoDTO.setThrottlingPolicies(throttlingPolicyNames);
        APIBusinessInformationDTO apiBusinessInformationDTO = new APIBusinessInformationDTO();
        apiBusinessInformationDTO.setBusinessOwner(api.getBusinessOwner());
        apiBusinessInformationDTO.setBusinessOwnerEmail(api.getBusinessOwnerEmail());
        apiBusinessInformationDTO.setTechnicalOwner(api.getTechnicalOwner());
        apiBusinessInformationDTO.setTechnicalOwnerEmail(api.getTechnicalOwnerEmail());
        apiInfoDTO.setBusinessInformation(apiBusinessInformationDTO);
        //        if (api.getScopes() != null) {
        //            apiInfoDTO.setScopes(getScopeInfoDTO(api.getScopes()));
        //        }
        if (!StringUtils.isBlank(api.getThumbnailUrl())) {
            apiInfoDTO.setThumbnailUri(api.getThumbnailUrl());
        }
        apiInfoDTO.setAdvertiseInfo(extractAdvertiseInfo(api));
        String apiTenant = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(api.getId()
                .getProviderName()));
        String subscriptionAvailability = api.getSubscriptionAvailability();
        String subscriptionAllowedTenants = api.getSubscriptionAvailableTenants();
        apiInfoDTO.setIsSubscriptionAvailable(isSubscriptionAvailable(apiTenant, subscriptionAvailability,
                subscriptionAllowedTenants));
        int free = 0, commercial = 0;
        for (Tier tier : throttlingPolicies) {
            if (RestApiConstants.FREE.equalsIgnoreCase(tier.getTierPlan())) {
                free = free + 1;
            } else if (RestApiConstants.COMMERCIAL.equalsIgnoreCase(tier.getTierPlan())) {
                commercial = commercial + 1;
            }
        }
        if (free > 0 && commercial == 0) {
            apiInfoDTO.setMonetizationLabel(RestApiConstants.FREE);
        } else if (free == 0 && commercial > 0) {
            apiInfoDTO.setMonetizationLabel(RestApiConstants.PAID);
        } else if (free > 0 && commercial > 0) {
            apiInfoDTO.setMonetizationLabel(RestApiConstants.FREEMIUM);
        }
        return apiInfoDTO;
    }

    /**
     * Creates a minimal DTO representation of an API Product object
     *
     * @param apiProduct API Product object
     * @return a minimal representation DTO
     */
    static APIInfoDTO fromAPIToInfoDTO(APIProduct apiProduct) {
        APIInfoDTO apiInfoDTO = new APIInfoDTO();
        apiInfoDTO.setDescription(apiProduct.getDescription());
        apiInfoDTO.setContext(apiProduct.getContext());
        apiInfoDTO.setId(apiProduct.getUuid());
        APIProductIdentifier apiId = apiProduct.getId();
        apiInfoDTO.setName(apiId.getName());
        apiInfoDTO.setVersion(apiId.getVersion());
        apiInfoDTO.setProvider(apiId.getProviderName());
        apiInfoDTO.setLifeCycleStatus(apiProduct.getState());
        apiInfoDTO.setType(APIType.API_PRODUCT.toString());
        apiInfoDTO.setAvgRating(String.valueOf(apiProduct.getRating()));
        String providerName = apiProduct.getId().getProviderName();
        apiInfoDTO.setProvider(APIUtil.replaceEmailDomainBack(providerName));
        Set<Tier> throttlingPolicies = apiProduct.getAvailableTiers();
        List<String> throttlingPolicyNames = new ArrayList<>();
        for (Tier tier : throttlingPolicies) {
            throttlingPolicyNames.add(tier.getName());
        }
        apiInfoDTO.setThrottlingPolicies(throttlingPolicyNames);
        APIBusinessInformationDTO apiBusinessInformationDTO = new APIBusinessInformationDTO();
        apiBusinessInformationDTO.setBusinessOwner(apiProduct.getBusinessOwner());
        apiBusinessInformationDTO.setBusinessOwnerEmail(apiProduct.getBusinessOwnerEmail());
        apiBusinessInformationDTO.setTechnicalOwner(apiProduct.getTechnicalOwner());
        apiBusinessInformationDTO.setTechnicalOwnerEmail(apiProduct.getTechnicalOwnerEmail());
        apiInfoDTO.setBusinessInformation(apiBusinessInformationDTO);

        if (!StringUtils.isBlank(apiProduct.getThumbnailUrl())) {
            apiInfoDTO.setThumbnailUri(apiProduct.getThumbnailUrl());
        }

        //Since same APIInfoDTO is used for listing APIProducts in StoreUI set default AdvertisedInfo to the DTO
        AdvertiseInfoDTO advertiseInfoDTO = new AdvertiseInfoDTO();
        advertiseInfoDTO.setAdvertised(false);
        apiInfoDTO.setAdvertiseInfo(advertiseInfoDTO);
        String apiTenant = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(apiProduct.getId()
                .getProviderName()));
        String subscriptionAvailability = apiProduct.getSubscriptionAvailability();
        String subscriptionAllowedTenants = apiProduct.getSubscriptionAvailableTenants();
        apiInfoDTO.setIsSubscriptionAvailable(isSubscriptionAvailable(apiTenant, subscriptionAvailability,
                subscriptionAllowedTenants));
        return apiInfoDTO;
    }

    /**
     * Extracts the API deployment environment details with ingress url for each cluster
     *
     * @param api API object
     * @return the API Deployment environments details
     * @throws APIManagementException error while extracting the information
     */
    private static List<APIIngressURLsDTO> extractIngressURLs(API api)
            throws APIManagementException {
        List<APIIngressURLsDTO> apiDeployedIngressURLs = new ArrayList<>();
        if (api.getDeploymentEnvironments() != null && !api.getDeploymentEnvironments().isEmpty()) {
            Set<org.wso2.carbon.apimgt.api.model.DeploymentEnvironments> selectedDeploymentEnvironments =
                    new HashSet<>(api.getDeploymentEnvironments());

            if (selectedDeploymentEnvironments != null && !selectedDeploymentEnvironments.isEmpty()) {
                for (org.wso2.carbon.apimgt.api.model.DeploymentEnvironments
                        deploymentEnvironment : selectedDeploymentEnvironments) {
                    APIIngressURLsDTO ingressURLDTO = new APIIngressURLsDTO();
                    JSONArray clusterConfigs = APIUtil.getAllClustersFromConfig();
                    for (Object clusterConfig : clusterConfigs) {
                        JSONObject clusterConf = (JSONObject) clusterConfig;
                        List<APIDeploymentClusterInfoDTO> clusterInfoArray = new ArrayList<>();
                        if (clusterConf.get(ContainerBasedConstants.TYPE).toString()
                                .equalsIgnoreCase(deploymentEnvironment.getType())) {
                            JSONArray containerMgtInfoArray = (JSONArray) (clusterConf
                                    .get(ContainerBasedConstants.CONTAINER_MANAGEMENT_INFO));
                            for (Object containerMgtInfoObj : containerMgtInfoArray) {
                                JSONObject containerMgtInfo = (JSONObject) containerMgtInfoObj;
                                APIDeploymentClusterInfoDTO apiDeploymentClusterInfoDTO =
                                        new APIDeploymentClusterInfoDTO();
                                if (deploymentEnvironment.getClusterNames().contains(containerMgtInfo
                                        .get(ContainerBasedConstants.CLUSTER_NAME).toString())) {
                                    apiDeploymentClusterInfoDTO.setClusterName(containerMgtInfo
                                            .get(ContainerBasedConstants.CLUSTER_NAME).toString());
                                    apiDeploymentClusterInfoDTO.setClusterDisplayName(containerMgtInfo
                                            .get(ContainerBasedConstants.DISPLAY_NAME).toString());
                                    if(((JSONObject) containerMgtInfo.get(ContainerBasedConstants.PROPERTIES))
                                            .get(ContainerBasedConstants.ACCESS_URL) != null){
                                        apiDeploymentClusterInfoDTO.setIngressURL(((JSONObject) containerMgtInfo
                                                .get(ContainerBasedConstants.PROPERTIES))
                                                .get(ContainerBasedConstants.ACCESS_URL).toString());
                                    }
                                    clusterInfoArray.add(apiDeploymentClusterInfoDTO);
                                }
                            }
                            if (!clusterInfoArray.isEmpty()) {
                                ingressURLDTO.setClusterDetails(clusterInfoArray);
                                ingressURLDTO.setDeploymentEnvironmentName(deploymentEnvironment.getType());
                            }

                        }
                    }
                    if (ingressURLDTO.getDeploymentEnvironmentName() != null && !ingressURLDTO.getClusterDetails()
                            .isEmpty()) {
                        apiDeployedIngressURLs.add(ingressURLDTO);
                    }
                }
            }
        }
        return apiDeployedIngressURLs;
    }

    /**
     * Returns label details of the API in REST API DTO format.
     *
     * @param gatewayLabels Gateway label details from the API model object
     * @param apiContext    API context
     * @return label details of the API in REST API DTO format
     */
    private static List<LabelDTO> getLabelDetails(List<Label> gatewayLabels, String apiContext) {
        List<LabelDTO> labels = new ArrayList<>();
        for (Label label : gatewayLabels) {
            LabelDTO labelDTO = new LabelDTO();
            labelDTO.setName(label.getName());
            labelDTO.setDescription(label.getDescription());
            for (String url : label.getAccessUrls()) {
                labelDTO.getAccessUrls().add(url + apiContext);
            }
            labels.add(labelDTO);
        }
        return labels;
    }

    //    /**
    //     * Creates a minimal scope DTO which will be a part of API Object
    //     *
    //     * @param scopes set
    //     * @return Scope DTO
    //     */
    //    public static List<ScopeInfoDTO> getScopeInfoDTO(Set<Scope> scopes) {
    //
    //        List<ScopeInfoDTO> scopeDto = new ArrayList<ScopeInfoDTO>();
    //        for (Scope scope : scopes) {
    //            ScopeInfoDTO scopeInfoDTO = new ScopeInfoDTO();
    //            scopeInfoDTO.setKey(scope.getKey());
    //            scopeInfoDTO.setName(scope.getName());
    //            if (scope.getRoles() != null) {
    //                scopeInfoDTO.setRoles(Arrays.asList(scope.getRoles().split(",")));
    //            }
    //            scopeDto.add(scopeInfoDTO);
    //        }
    //        return scopeDto;
    //    }


    /**
     * Maps external store advertise API properties to AdvertiseInfoDTO object.
     *
     * @param api API object
     * @return AdvertiseInfoDTO
     */
    public static AdvertiseInfoDTO extractAdvertiseInfo(API api) {
        AdvertiseInfoDTO advertiseInfoDTO = new AdvertiseInfoDTO();
        advertiseInfoDTO.setAdvertised(api.isAdvertiseOnly());
        advertiseInfoDTO.setOriginalDevPortalUrl(api.getRedirectURL());
        advertiseInfoDTO.setApiOwner(api.getApiOwner());
        return advertiseInfoDTO;
    }

    /**
     * Checks whether tenant is allowed to subscribe
     *
     * @param apiTenant                  Tenant of the API creator
     * @param subscriptionAvailability   Subscription availability
     * @param subscriptionAllowedTenants Subscription allowed tenants
     * @return subscriptionAllowed
     */
    private static boolean isSubscriptionAvailable(String apiTenant, String subscriptionAvailability,
                                                   String subscriptionAllowedTenants) {

        String userTenant = RestApiCommonUtil.getLoggedInUserTenantDomain();
        boolean subscriptionAllowed = false;
        if (!userTenant.equals(apiTenant)) {
            if (APIConstants.SUBSCRIPTION_TO_ALL_TENANTS.equals(subscriptionAvailability)) {
                subscriptionAllowed = true;
            } else if (APIConstants.SUBSCRIPTION_TO_SPECIFIC_TENANTS.equals(subscriptionAvailability)) {
                String allowedTenants[] = null;
                if (subscriptionAllowedTenants != null) {
                    allowedTenants = subscriptionAllowedTenants.split(",");
                    if (allowedTenants != null) {
                        for (String tenant : allowedTenants) {
                            if (tenant != null && tenant.trim().equals(userTenant)) {
                                subscriptionAllowed = true;
                                break;
                            }
                        }
                    }
                }
            }
        } else {
            subscriptionAllowed = true;
        }
        return subscriptionAllowed;
    }
}
