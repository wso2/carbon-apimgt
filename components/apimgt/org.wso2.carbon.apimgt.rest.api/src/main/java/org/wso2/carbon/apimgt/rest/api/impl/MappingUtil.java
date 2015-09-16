/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.impl;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.rest.api.model.API;
import org.wso2.carbon.apimgt.rest.api.model.Sequence;
import org.wso2.carbon.apimgt.rest.api.model.Tag;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class MappingUtil {

    protected static API fromAPItoDTO(org.wso2.carbon.apimgt.api.model.API model) throws APIManagementException {

        APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(model.getId().getProviderName());

        API dto = new API();
        dto.setName(model.getId().getApiName());
        dto.setVersion(model.getId().getVersion());
        dto.setProvider(model.getId().getProviderName());
        dto.setContext(model.getContext());
        dto.setDescription(model.getDescription());

        dto.setIsDefaultVersion(model.isDefaultVersion());
        dto.setResponseCaching(model.getResponseCache());
        dto.setCacheTimeout(model.getCacheTimeout());
        dto.setDestinationStatsEnabled(model.getDestinationStatsEnabled());

        List<Sequence> sequences = null;

        String inSequenceName = model.getInSequence();
        if (inSequenceName != null && !inSequenceName.isEmpty()) {
            Sequence inSequence = new Sequence();
            inSequence.setName(inSequenceName);
            inSequence.setType("IN");
            sequences.add(inSequence);
        }

        String outSequenceName = model.getOutSequence();
        if (outSequenceName != null && !outSequenceName.isEmpty()) {
            Sequence outSequence = new Sequence();
            outSequence.setName(outSequenceName);
            outSequence.setType("OUT");
            sequences.add(outSequence);
        }

        String faultSequenceName = model.getFaultSequence();
        if (faultSequenceName != null && !faultSequenceName.isEmpty()) {
            Sequence faultSequence = new Sequence();
            faultSequence.setName(faultSequenceName);
            faultSequence.setType("FAULT");
            sequences.add(faultSequence);
        }

        dto.setSequences(sequences);

        dto.setStatus(model.getStatus().getStatus());
        dto.setSubscriptionAvailability(API.SubscriptionAvailabilityEnum.valueOf(model.getSubscriptionAvailability()));
        //do we need to put validity checks? - specific_tenants
        if (model.getSubscriptionAvailableTenants() != null) {
            dto.setSubscriptionAvailableTenants(Arrays.asList(model.getSubscriptionAvailableTenants().split(",")));
        }

        //Get Swagger definition which has URL templates, scopes and resource details
        String apiSwaggerDefinition;

        apiSwaggerDefinition = apiProvider.getSwagger20Definition(model.getId());

        dto.setSwagger(apiSwaggerDefinition);

        Set<String> apiTags = model.getTags();
        List<Tag> tagsToReturn = new ArrayList();
        for (String tag : apiTags) {
            Tag newTag = new Tag();
            newTag.setName(tag);
            tagsToReturn.add(newTag);
        }
        dto.setTags(tagsToReturn);

        Set<org.wso2.carbon.apimgt.api.model.Tier> apiTiers = model.getAvailableTiers();
        List<String> tiersToReturn = new ArrayList();
        for (org.wso2.carbon.apimgt.api.model.Tier tier : apiTiers) {
            tiersToReturn.add(tier.getName());
        }
        dto.setTiers(tiersToReturn);

        dto.setTransport(Arrays.asList(model.getTransports().split(",")));
        //dto.setType("");   //how to get type?
        dto.setVisibility(API.VisibilityEnum.valueOf(model.getVisibility()));
        //do we need to put validity checks? - restricted
        if (model.getVisibleRoles() != null) {
            dto.setVisibleRoles(Arrays.asList(model.getVisibleRoles().split(",")));
        }
        //do we need to put validity checks? - controlled
        if (model.getVisibleTenants() != null) {
            dto.setVisibleRoles(Arrays.asList(model.getVisibleTenants().split(",")));
        }

        return dto;
    }

    protected static org.wso2.carbon.apimgt.api.model.API fromDTOtoAPI(API dto) throws APIManagementException {

        APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(dto.getProvider());

        APIIdentifier apiId = new APIIdentifier(dto.getName(), dto.getVersion(), dto.getProvider());
        org.wso2.carbon.apimgt.api.model.API model = new org.wso2.carbon.apimgt.api.model.API(apiId);

        model.setContext(dto.getContext());  //context should change if tenant
        model.setDescription(dto.getDescription());

        model.setAsDefaultVersion(dto.getIsDefaultVersion());
        model.setResponseCache(dto.getResponseCaching());
        model.setCacheTimeout(dto.getCacheTimeout());
        model.setDestinationStatsEnabled(dto.getDestinationStatsEnabled());

        if (dto.getSequences() != null) {
            List<Sequence> sequences = dto.getSequences();

            //validate whether provided sequences are available
            for (Sequence sequence : sequences) {
                if (APIConstants.API_CUSTOM_SEQUENCE_TYPE_IN.equalsIgnoreCase(sequence.getType())) {
                    model.setInSequence(sequence.getName());
                } else if (APIConstants.API_CUSTOM_SEQUENCE_TYPE_OUT.equalsIgnoreCase(sequence.getType())) {
                    model.setOutSequence(sequence.getName());
                } else {
                    model.setFaultSequence(sequence.getName());
                }
            }
        }

        model.setSubscriptionAvailability(dto.getSubscriptionAvailability().name());
        //do we need to put validity checks? - specific_tenants
        if (dto.getSubscriptionAvailableTenants() != null) {
            model.setSubscriptionAvailableTenants(dto.getSubscriptionAvailableTenants().toString());
        }

        //Get Swagger definition which has URL templates, scopes and resource details
        String apiSwaggerDefinition;

        apiSwaggerDefinition = apiProvider.getSwagger20Definition(model.getId());

        dto.setSwagger(apiSwaggerDefinition);

        Set<String> apiTags = model.getTags();
        List<Tag> tagsToReturn = new ArrayList();
        for (String tag : apiTags) {
            Tag newTag = new Tag();
            newTag.setName(tag);
            tagsToReturn.add(newTag);
        }
        dto.setTags(tagsToReturn);

        Set<org.wso2.carbon.apimgt.api.model.Tier> apiTiers = model.getAvailableTiers();
        List<String> tiersToReturn = new ArrayList();
        for (org.wso2.carbon.apimgt.api.model.Tier tier : apiTiers) {
            tiersToReturn.add(tier.getName());
        }
        dto.setTiers(tiersToReturn);

        dto.setTransport(Arrays.asList(model.getTransports().split(",")));
        //dto.setType("");   //how to get type?
        dto.setVisibility(API.VisibilityEnum.valueOf(model.getVisibility()));
        //do we need to put validity checks? - restricted
        if (model.getVisibleRoles() != null) {
            dto.setVisibleRoles(Arrays.asList(model.getVisibleRoles().split(",")));
        }
        //do we need to put validity checks? - controlled
        if (model.getVisibleTenants() != null) {
            dto.setVisibleRoles(Arrays.asList(model.getVisibleTenants().split(",")));
        }

        return model;

    }
}
