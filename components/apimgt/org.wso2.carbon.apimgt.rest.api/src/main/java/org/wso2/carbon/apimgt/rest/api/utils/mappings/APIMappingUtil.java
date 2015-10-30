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

package org.wso2.carbon.apimgt.rest.api.utils.mappings;

import org.apache.commons.lang.StringUtils;
import org.wso2.carbon.apimgt.api.APIDefinition;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.URITemplate;
import org.wso2.carbon.apimgt.api.model.Scope;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DocumentationType;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.definitions.APIDefinitionFromSwagger20;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.dto.SequenceDTO;
import org.wso2.carbon.apimgt.rest.api.utils.RestApiUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class is used as a util class to map corresponding DTOs from REST API side with
 * attributes in org.wso2.carbon.apimgt.api.model.API and
 * org.wso2.carbon.apimgt.api.model.Documentation
 */
public class APIMappingUtil {

    public static APIIdentifier getAPIIdentifier(String apiId){
        String[] apiIdDetails = apiId.split(RestApiConstants.API_ID_DELIMITER);
        String providerName = apiIdDetails[0];
        String apiName = apiIdDetails[1];
        String version = apiIdDetails[2];
        String providerNameEmailReplaced = APIUtil.replaceEmailDomain(providerName);
        return new APIIdentifier(providerNameEmailReplaced, apiName, version);
    }

    /**
     * Map API backend model with DTO
     *
     * @param model API backend model
     * @return Corresponding mapping DTO for the input model
     * @throws APIManagementException If an errors occurs while mapping model to DTO
     */
    public static APIDTO fromAPItoDTO(API model) throws APIManagementException {

        APIProvider apiProvider = RestApiUtil.getProvider();

        APIDTO dto = new APIDTO();
        dto.setName(model.getId().getApiName());
        dto.setVersion(model.getId().getVersion());
        dto.setProvider(model.getId().getProviderName());
        dto.setId(model.getUUID());
        dto.setContext(model.getContext());
        dto.setDescription(model.getDescription());
        dto.setIsDefaultVersion(model.isDefaultVersion());
        dto.setResponseCaching(model.getResponseCache());
        dto.setCacheTimeout(model.getCacheTimeout());
        dto.setDestinationStatsEnabled(model.getDestinationStatsEnabled());

        List<SequenceDTO> sequences = null;

        String inSequenceName = model.getInSequence();
        if (!StringUtils.isEmpty(inSequenceName)) {
            SequenceDTO inSequence = new SequenceDTO();
            inSequence.setName(inSequenceName);
            inSequence.setType("IN");
            sequences.add(inSequence);
        }

        String outSequenceName = model.getOutSequence();
        if (!StringUtils.isEmpty(outSequenceName)) {
            SequenceDTO outSequence = new SequenceDTO();
            outSequence.setName(outSequenceName);
            outSequence.setType("OUT");
            sequences.add(outSequence);
        }

        String faultSequenceName = model.getFaultSequence();
        if (!StringUtils.isEmpty(faultSequenceName)) {
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

        if (model.getSubscriptionAvailableTenants() != null) {
            dto.setSubscriptionAvailableTenants(Arrays.asList(model.getSubscriptionAvailableTenants().split(",")));
        }

        //Swagger definition will contain details about URI Templates, scopes and resources
        String apiSwaggerDefinition = apiProvider.getSwagger20Definition(model.getId());

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
        if (model.getVisibleRoles() != null) {
            dto.setVisibleRoles(Arrays.asList(model.getVisibleRoles().split(",")));
        }

        if (model.getVisibleTenants() != null) {
            dto.setVisibleRoles(Arrays.asList(model.getVisibleTenants().split(",")));
        }

        return dto;
    }

    /**
     * Map API DTO to backend model
     *
     * @param dto API DTO
     * @return Corresponding mapping API backend model to input DTO
     * @throws APIManagementException If an error occurs while mapping DTO to model
     */
    public static API fromDTOtoAPI(APIDTO dto) throws APIManagementException {

        APIDefinition definitionFromSwagger20 = new APIDefinitionFromSwagger20();

        APIIdentifier apiId = new APIIdentifier(dto.getProvider(), dto.getName(), dto.getVersion());
        org.wso2.carbon.apimgt.api.model.API model = new org.wso2.carbon.apimgt.api.model.API(apiId);

        model.setContext(dto.getContext());
        model.setContextTemplate(dto.getContext());
        model.setDescription(dto.getDescription());
        model.setStatus(APIStatus.CREATED);
        model.setAsDefaultVersion(dto.getIsDefaultVersion());
        model.setResponseCache(dto.getResponseCaching());
        model.setCacheTimeout(dto.getCacheTimeout());
        model.setDestinationStatsEnabled(dto.getDestinationStatsEnabled());

        if (dto.getSequences() != null) {
            List<SequenceDTO> sequences = dto.getSequences();

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

        return model;

    }

    /**
     * Map API visibility from Enum in the DTO to String in the backend
     *
     * @param visibility Enum containing the visibility value
     * @return String Corresponding APIConstant which maps with DTO visibility enum
     * APIManagementException If mapping between backend and DTO level visibility values does not exist
     */
    private static String mapVisibilityFromDTOtoAPI(APIDTO.VisibilityEnum visibility) throws APIManagementException {
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
                throw new APIManagementException("No mapping backend value for " + visibility.name());
        }
    }

    /**
     * Map API visibility from backend API constant to visibility enum in DTO
     *
     * @param visibility API Constant containing visibility value
     * @return VisibilityEnum Corresponding enum which maps with backend visibility value
     * APIManagementException If mapping between backend and DTO level visibility values does not exist
     */
    private static APIDTO.VisibilityEnum mapVisibilityFromAPItoDTO(String visibility) throws APIManagementException {
        switch (visibility) {
            case APIConstants.API_GLOBAL_VISIBILITY :
                return APIDTO.VisibilityEnum.PUBLIC;
            case APIConstants.API_PRIVATE_VISIBILITY :
                return APIDTO.VisibilityEnum.PRIVATE;
            case APIConstants.API_RESTRICTED_VISIBILITY :
                return APIDTO.VisibilityEnum.RESTRICTED;
            case APIConstants.API_CONTROLLED_VISIBILITY :
                return APIDTO.VisibilityEnum.CONTROLLED;
            default:
                throw new APIManagementException("No DTO level mapping exist for " + visibility);
        }
    }

    /**
     * Map API subscription availability from backend API constant to enum in DTO
     *
     * @param subscriptionAvailability Backend constant value
     * @return SubscriptionAvailabilityEnum Corresponding enum which maps with backend value
     * APIManagementException If mapping between backend and DTO level subscription values does not exist
     */
    private static APIDTO.SubscriptionAvailabilityEnum mapSubscriptionAvailabilityFromAPItoDTO(
        String subscriptionAvailability) throws APIManagementException {

        switch (subscriptionAvailability) {
            case APIConstants.SUBSCRIPTION_TO_CURRENT_TENANT :
                return APIDTO.SubscriptionAvailabilityEnum.CURRENT_TENANT;
            case APIConstants.SUBSCRIPTION_TO_ALL_TENANTS :
                return APIDTO.SubscriptionAvailabilityEnum.ALL_TENANTS;
            case APIConstants.SUBSCRIPTION_TO_SPECIFIC_TENANTS :
                return APIDTO.SubscriptionAvailabilityEnum.SPECIFIC_TENANTS;
            default:
                throw new APIManagementException("No mapping DTO level value exist for " + subscriptionAvailability);
        }

    }

    /**
     * Map API subscription availability from Enum in the DTO to String in the backend
     *
     * @param subscriptionAvailability Enum containing the subscription availability value
     * @return String Corresponding mapping value for enum in the DTO
     * APIManagementException If mapping between backend and DTO level subscription values does not exist
     */
    private static String mapSubscriptionAvailabilityFromDTOtoAPI(
        APIDTO.SubscriptionAvailabilityEnum subscriptionAvailability) throws APIManagementException {
        switch (subscriptionAvailability) {
            case CURRENT_TENANT:
                return APIConstants.SUBSCRIPTION_TO_CURRENT_TENANT;
            case ALL_TENANTS:
                return APIConstants.SUBSCRIPTION_TO_ALL_TENANTS;
            case SPECIFIC_TENANTS:
                return APIConstants.SUBSCRIPTION_TO_SPECIFIC_TENANTS;
            default:
                throw new APIManagementException("No mapping backend value for " + subscriptionAvailability.name());
        }

    }


    public static DocumentDTO fromDocumentationtoDTO(Documentation doc){
        DocumentDTO d = new DocumentDTO();
        d.setDocumentId(doc.getId());
        d.setName(doc.getName());
        //d.setUrl(doc.getFilePath());
        d.setSummary(doc.getSummary());
        d.setType(DocumentDTO.TypeEnum.valueOf(doc.getType().toString()));
        //d.setUrl(doc.getFilePath());
        d.setVisibility(DocumentDTO.VisibilityEnum.valueOf(doc.getVisibility().toString()));
        return d;
    }

    public static Documentation fromDTOtoDocumentation(DocumentDTO dto){
        Documentation doc = new Documentation(DocumentationType.valueOf(dto.getType().toString()) ,dto.getName());
        doc.setSummary(dto.getSummary());
        String visibility = dto.getVisibility().toString();
        /*
        TO-DO following statement will never reach as .tostring will retunr you NPE. Please check logic
        if (visibility == null){
            visibility = APIConstants.DOC_API_BASED_VISIBILITY;
        }*/
        doc.setVisibility(Documentation.DocumentVisibility.valueOf(visibility));
        doc.setSourceType(Documentation.DocumentSourceType.INLINE);
        return doc;
    }
}
