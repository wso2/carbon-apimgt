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
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DocumentationType;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.definitions.APIDefinitionFromSwagger20;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIBusinessInformationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.SequenceDTO;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class APIMappingUtil {

    public static APIIdentifier getAPIIdentifierFromApiId(String apiId){
        String[] apiIdDetails = apiId.split(RestApiConstants.API_ID_DELIMITER);
        // apiId format: provider-apiName-version
        String providerName = apiIdDetails[0];
        String apiName = apiIdDetails[1];
        String version = apiIdDetails[2];
        String providerNameEmailReplaced = APIUtil.replaceEmailDomain(providerName);
        return new APIIdentifier(providerNameEmailReplaced, apiName, version);
    }

    public static APIIdentifier getAPIIdentifierFromApiIdOrUUID(String apiId, String requestedTenantDomain)
            throws APIManagementException {
        APIIdentifier apiIdentifier;
        if (RestApiUtil.isUUID(apiId)) {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
            apiIdentifier = apiProvider.getAPIInformationByUUID(apiId, requestedTenantDomain).getId();
        } else {
            apiIdentifier = getAPIIdentifierFromApiId(apiId);
        }
        return  apiIdentifier;
    }

    public static APIDTO fromAPItoDTO(API model) throws APIManagementException {

        APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();

        APIDTO dto = new APIDTO();
        dto.setName(model.getId().getApiName());
        dto.setVersion(model.getId().getVersion());
        String providerName = model.getId().getProviderName();
        dto.setProvider(APIUtil.replaceEmailDomainBack(providerName));
        dto.setId(model.getUUID());
        dto.setContext(model.getContextTemplate());
        dto.setDescription(model.getDescription());

        dto.setIsDefaultVersion(model.isDefaultVersion());
        dto.setResponseCaching(model.getResponseCache());
        dto.setCacheTimeout(model.getCacheTimeout());
        dto.setDestinationStatsEnabled(model.getDestinationStatsEnabled());
        dto.setEndpointConfig(model.getEndpointConfig());
        List<SequenceDTO> sequences = new ArrayList<>();

        String inSequenceName = model.getInSequence();
        if (inSequenceName != null && !inSequenceName.isEmpty()) {
            SequenceDTO inSequence = new SequenceDTO();
            inSequence.setName(inSequenceName);
            inSequence.setType("IN");
            sequences.add(inSequence);
        }

        String outSequenceName = model.getOutSequence();
        if (outSequenceName != null && !outSequenceName.isEmpty()) {
            SequenceDTO outSequence = new SequenceDTO();
            outSequence.setName(outSequenceName);
            outSequence.setType("OUT");
            sequences.add(outSequence);
        }

        String faultSequenceName = model.getFaultSequence();
        if (faultSequenceName != null && !faultSequenceName.isEmpty()) {
            SequenceDTO faultSequence = new SequenceDTO();
            faultSequence.setName(faultSequenceName);
            faultSequence.setType("FAULT");
            sequences.add(faultSequence);
        }

        dto.setSequences(sequences);

        dto.setStatus(model.getStatus().getStatus());

        String subscriptionAvailability = model.getSubscriptionAvailability();
        if (subscriptionAvailability != null) {
            dto.setSubscriptionAvailability(mapSubscriptionAvailabilityFromAPItoDTO(subscriptionAvailability));
        }

        //todo: do we need to put validity checks? - specific_tenants
        if (model.getSubscriptionAvailableTenants() != null) {
            dto.setSubscriptionAvailableTenants(Arrays.asList(model.getSubscriptionAvailableTenants().split(",")));
        }

        //Get Swagger definition which has URL templates, scopes and resource details
        String apiSwaggerDefinition;

        apiSwaggerDefinition = apiProvider.getSwagger20Definition(model.getId());

        dto.setApiDefinition(apiSwaggerDefinition);

        Set<String> apiTags = model.getTags();
        List<String> tagsToReturn = new ArrayList();
        tagsToReturn.addAll(apiTags);
        dto.setTags(tagsToReturn);

        Set<org.wso2.carbon.apimgt.api.model.Tier> apiTiers = model.getAvailableTiers();
        List<String> tiersToReturn = new ArrayList();
        for (org.wso2.carbon.apimgt.api.model.Tier tier : apiTiers) {
            tiersToReturn.add(tier.getName());
        }
        dto.setTiers(tiersToReturn);

        dto.setTransport(Arrays.asList(model.getTransports().split(",")));
        dto.setVisibility(mapVisibilityFromAPItoDTO(model.getVisibility()));
        //todo: do we need to put validity checks? - restricted
        if (model.getVisibleRoles() != null) {
            dto.setVisibleRoles(Arrays.asList(model.getVisibleRoles().split(",")));
        }
        //todo: do we need to put validity checks? - controlled
        if (model.getVisibleTenants() != null) {
            dto.setVisibleRoles(Arrays.asList(model.getVisibleTenants().split(",")));
        }

        APIBusinessInformationDTO apiBusinessInformationDTO = new APIBusinessInformationDTO();
        apiBusinessInformationDTO.setBusinessOwner(model.getBusinessOwner());
        apiBusinessInformationDTO.setBusinessOwnerEmail(model.getBusinessOwnerEmail());
        apiBusinessInformationDTO.setTechnicalOwner(model.getTechnicalOwner());
        apiBusinessInformationDTO.setTechnicalOwnerEmail(model.getTechnicalOwnerEmail());
        dto.setBusinessInformation(apiBusinessInformationDTO);
        
        //todo: thumbnail still missing
        return dto;
    }

    public static API fromDTOtoAPI(APIDTO dto) throws APIManagementException {

        APIDefinition definitionFromSwagger20 = new APIDefinitionFromSwagger20();

        String provider = dto.getProvider();
        String providerEmailDomainReplaced = APIUtil.replaceEmailDomain(provider);
        APIIdentifier apiId = new APIIdentifier(providerEmailDomainReplaced, dto.getName(), dto.getVersion());
        org.wso2.carbon.apimgt.api.model.API model = new org.wso2.carbon.apimgt.api.model.API(apiId);

        String context = dto.getContext();
        final String originalContext = context;
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
        model.setStatus(mapStatusFromDTOToAPI(dto.getStatus()));

        model.setAsDefaultVersion(dto.getIsDefaultVersion());
        model.setResponseCache(dto.getResponseCaching());
        model.setCacheTimeout(dto.getCacheTimeout());
        model.setDestinationStatsEnabled(dto.getDestinationStatsEnabled());

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
            model.setSubscriptionAvailability(mapSubscriptionAvailabilityFromDTOtoAPI(dto.getSubscriptionAvailability()));
        }

        //todo :do we need to put validity checks? - specific_tenants
        if (dto.getSubscriptionAvailableTenants() != null) {
            model.setSubscriptionAvailableTenants(dto.getSubscriptionAvailableTenants().toString());
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
            Set<String> apiTags = new HashSet<String>(dto.getTags());
            model.addTags(apiTags);
        }

        Set<org.wso2.carbon.apimgt.api.model.Tier> apiTiers = new HashSet<>();
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
            model.setVisibleRoles(visibleTenants);
        }

        APIBusinessInformationDTO apiBusinessInformationDTO = dto.getBusinessInformation();
        if (apiBusinessInformationDTO != null) {
            model.setBusinessOwner(apiBusinessInformationDTO.getBusinessOwner());
            model.setBusinessOwnerEmail(apiBusinessInformationDTO.getBusinessOwnerEmail());
            model.setTechnicalOwner(apiBusinessInformationDTO.getTechnicalOwner());
            model.setTechnicalOwnerEmail(apiBusinessInformationDTO.getTechnicalOwnerEmail());
        }

        //todo: thumbnail requires mapping
        return model;

    }

    /** Converts a List object of APIs into a DTO
     *
     * @param apiList List of APIs
     * @param limit maximum number of APIs returns
     * @param offset starting index
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

    /** Sets pagination urls for a APIListDTO object given pagination parameters and url parameters
     *
     * @param apiListDTO a APIListDTO object
     * @param query search condition
     * @param type value for the search condition
     * @param limit max number of objects returned
     * @param offset starting index
     * @param size max offset
     *
     */
    public static void setPaginationParams(APIListDTO apiListDTO, String query, String type, int offset,
            int limit, int size) {

        //acquiring pagination parameters and setting pagination urls
        Map<String, Integer> paginatedParams = RestApiUtil.getPaginationParams(offset, limit, size);
        String paginatedPrevious = "";
        String paginatedNext = "";

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = RestApiUtil
                    .getAPIPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT), query, type);
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = RestApiUtil
                    .getAPIPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT), query, type);
        }

        apiListDTO.setNext(paginatedNext);
        apiListDTO.setPrevious(paginatedPrevious);
    }

    public static APIInfoDTO fromAPIToInfoDTO(API api) {
        APIInfoDTO apiInfoDTO = new APIInfoDTO();
        apiInfoDTO.setDescription(api.getDescription());
        apiInfoDTO.setContext(api.getContextTemplate());
        apiInfoDTO.setId(api.getUUID());
        APIIdentifier apiId = api.getId();
        apiInfoDTO.setName(apiId.getApiName());
        apiInfoDTO.setVersion(apiId.getVersion());
        apiInfoDTO.setProvider(apiId.getProviderName());
        apiInfoDTO.setStatus(api.getStatus().toString());
        return apiInfoDTO;
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
        }else{
            context = context.replace(RestApiConstants.API_VERSION_PARAM, version);
        }
        return context;
    }

    private static String checkAndSetVersionParam(String context) {
        // This is to support the new Pluggable version strategy
        // if the context does not contain any {version} segment, we use the default version strategy.
        if(!context.contains(RestApiConstants.API_VERSION_PARAM)){
            if(!context.endsWith("/")){
                context = context + "/";
            }
            context = context + RestApiConstants.API_VERSION_PARAM;
        }
        return context;
    }
}
