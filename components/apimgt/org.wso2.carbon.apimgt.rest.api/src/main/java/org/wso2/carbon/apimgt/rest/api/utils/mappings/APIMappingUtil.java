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
        //validation required
        String[] apiIdDetails = apiId.split(RestApiConstants.API_ID_DELIMITER);
        String providerName = apiIdDetails[0];
        String apiName = apiIdDetails[1];
        String version = apiIdDetails[2];
        String providerNameEmailReplaced = APIUtil.replaceEmailDomain(providerName);
        return new APIIdentifier(providerNameEmailReplaced, apiName, version);
    }

    public static APIDTO fromAPItoDTO(org.wso2.carbon.apimgt.api.model.API model) throws APIManagementException {

        APIProvider apiProvider = RestApiUtil.getProvider(model.getId().getProviderName());

        APIDTO dto = new APIDTO();
        dto.setName(model.getId().getApiName());
        dto.setVersion(model.getId().getVersion());
        dto.setProvider(model.getId().getProviderName());
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
        //dto.setSubscriptionAvailability(API.SubscriptionAvailabilityEnum.valueOf(model.getSubscriptionAvailability()));
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
        //dto.setVisibility(API.VisibilityEnum.valueOf(model.getVisibility()));
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

    public static org.wso2.carbon.apimgt.api.model.API fromDTOtoAPI(APIDTO dto) throws APIManagementException {

        APIProvider apiProvider = RestApiUtil.getProvider(dto.getProvider());
        APIDefinition definitionFromSwagger20 = new APIDefinitionFromSwagger20();

        APIIdentifier apiId = new APIIdentifier(dto.getProvider(), dto.getName(), dto.getVersion());
        org.wso2.carbon.apimgt.api.model.API model = new org.wso2.carbon.apimgt.api.model.API(apiId);

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
            model.setSubscriptionAvailability(dto.getSubscriptionAvailability().name());
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
        model.setVisibility("Public");
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
