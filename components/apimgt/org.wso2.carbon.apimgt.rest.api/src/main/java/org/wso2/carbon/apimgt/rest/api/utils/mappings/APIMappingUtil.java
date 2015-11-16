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
import org.wso2.carbon.apimgt.rest.api.dto.*;
import org.wso2.carbon.apimgt.rest.api.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
            inSequence.setType(APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN);
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

        // This is to support the new Pluggable version strategy
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

    public static APIListDTO fromAPIListToDTO (List<API> apiList) {
        APIListDTO apiListDTO = new APIListDTO();
        List<APIInfoDTO> apiInfoDTOs = apiListDTO.getList();
        if (apiInfoDTOs == null) {
            apiInfoDTOs = new ArrayList<>();
            apiListDTO.setList(apiInfoDTOs);
        }
        for (API api : apiList) {
            apiInfoDTOs.add(fromAPIToInfoDTO(api));
        }
        apiListDTO.setCount(apiList.size());
        return apiListDTO;
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
        apiInfoDTO.setType(null); //todo
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
