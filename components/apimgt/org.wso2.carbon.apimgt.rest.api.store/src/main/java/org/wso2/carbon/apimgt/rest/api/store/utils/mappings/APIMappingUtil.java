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

package org.wso2.carbon.apimgt.rest.api.store.utils.mappings;

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.ArrayList;
import java.util.Arrays;
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
        dto.setStatus(model.getStatus().getStatus());

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

        //business info and thumbnail still missing
        return dto;
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
        return apiInfoDTO;
    }

    public static DocumentDTO fromDocumentationtoDTO(Documentation doc){
        DocumentDTO d = new DocumentDTO();
        d.setDocumentId(doc.getId());
        d.setName(doc.getName());
        //d.setUrl(doc.getFilePath());
        d.setSummary(doc.getSummary());
        d.setType(DocumentDTO.TypeEnum.valueOf(doc.getType().toString()));
        //d.setUrl(doc.getFilePath());
        return d;
    }
}
