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
import org.wso2.carbon.apimgt.api.model.*;
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

public class APIMappingUtil {

    public static APIIdentifier getAPIIdentifier(String apiId){
        String[] apiIdDetails = apiId.split(RestApiConstants.API_ID_DELIMITER);
        String providerName = apiIdDetails[0];
        String apiName = apiIdDetails[1];
        String version = apiIdDetails[2];
        String providerNameEmailReplaced = APIUtil.replaceEmailDomain(providerName);
        return new APIIdentifier(providerNameEmailReplaced, apiName, version);
    }

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

        //do we need to put validity checks? - specific_tenants
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
        //dto.setType("");   //how to get type?
        dto.setVisibility(mapVisibilityFromAPItoDTO(model.getVisibility()));
        //do we need to put validity checks? - restricted
        if (model.getVisibleRoles() != null) {
            dto.setVisibleRoles(Arrays.asList(model.getVisibleRoles().split(",")));
        }
        //do we need to put validity checks? - controlled
        if (model.getVisibleTenants() != null) {
            dto.setVisibleRoles(Arrays.asList(model.getVisibleTenants().split(",")));
        }

        //endpoint configs, business info and thumbnail still missing
        return dto;
    }

    public static API fromDTOtoAPI(APIDTO dto) throws APIManagementException {

        APIDefinition definitionFromSwagger20 = new APIDefinitionFromSwagger20();

        APIIdentifier apiId = new APIIdentifier(dto.getProvider(), dto.getName(), dto.getVersion());
        org.wso2.carbon.apimgt.api.model.API model = new org.wso2.carbon.apimgt.api.model.API(apiId);

        //if not supertenant append /t/wso2.com/ to context
        model.setContext(dto.getContext());  //context should change if tenant
        model.setContextTemplate(dto.getContext());
        model.setDescription(dto.getDescription());

        model.setStatus(APIStatus.CREATED);

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

        //do we need to put validity checks? - specific_tenants
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
        //dto.setType("");   //how to get type?
        model.setVisibility(mapVisibilityFromDTOtoAPI(dto.getVisibility()));
        if (dto.getVisibleRoles() != null) {
            String visibleRoles = StringUtils.join(dto.getVisibleRoles(), ',');
            model.setVisibleRoles(visibleRoles);
        }

        if (dto.getVisibleTenants() != null) {
            String visibleTenants = StringUtils.join(dto.getVisibleTenants(), ',');
            model.setVisibleRoles(visibleTenants);
        }

        //endpoint configs, business info and thumbnail requires mapping
        return model;

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
        if (visibility == null){
            visibility = APIConstants.DOC_API_BASED_VISIBILITY;
        }
        doc.setVisibility(Documentation.DocumentVisibility.valueOf(visibility));
        doc.setSourceType(Documentation.DocumentSourceType.INLINE);
        return doc;
    }
}
