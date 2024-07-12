/*
*  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
*
*    http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/

package org.wso2.carbon.apimgt.rest.api.store.v1.mappings;

import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.DocumentSearchResultDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PaginationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SearchResultDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APISearchResultDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SearchResultListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIBusinessInformationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AdvertiseInfoDTO;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;

import java.util.Map;

public class SearchResultMappingUtil {

    /**
     * Converts a API or Product into a DTO
     *
     * @param api api or Product
     * @return APISearchResultDTO
     */
    public static APISearchResultDTO fromAPIToAPIResultDTO(Object api) {
        if (api instanceof APIProduct) {
            return fromAPIToAPIResultDTO((APIProduct) api);
        }
        return fromAPIToAPIResultDTO((API) api);
    }
    /**
     * Get API result representation for content search
     * @param api API
     * @return
     */
    public static APISearchResultDTO fromAPIToAPIResultDTO(API api) {
        APISearchResultDTO apiResultDTO = new APISearchResultDTO();
        apiResultDTO.setId(api.getUUID());
        APIIdentifier apiId = api.getId();
        apiResultDTO.setName(apiId.getApiName());
        apiResultDTO.setVersion(apiId.getVersion());
        apiResultDTO.setProvider(APIUtil.replaceEmailDomainBack(apiId.getProviderName()));
        String context = api.getContextTemplate();
        if (context.endsWith("/" + RestApiConstants.API_VERSION_PARAM)) {
            context = context.replace("/" + RestApiConstants.API_VERSION_PARAM, "");
        }
        apiResultDTO.setContext(context);
        apiResultDTO.setAvgRating(String.valueOf(api.getRating()));
        APIBusinessInformationDTO apiBusinessInformationDTO = new APIBusinessInformationDTO();
        apiBusinessInformationDTO.setBusinessOwner(api.getBusinessOwner());
        apiBusinessInformationDTO.setBusinessOwnerEmail(api.getBusinessOwnerEmail());
        apiBusinessInformationDTO.setTechnicalOwner(api.getTechnicalOwner());
        apiBusinessInformationDTO.setTechnicalOwnerEmail(api.getTechnicalOwnerEmail());
        apiResultDTO.setBusinessInformation(apiBusinessInformationDTO);
        apiResultDTO.setType(SearchResultDTO.TypeEnum.API);
        apiResultDTO.setTransportType(api.getType());
        apiResultDTO.setDescription(api.getDescription());
        apiResultDTO.setStatus(api.getStatus());
        apiResultDTO.setThumbnailUri(api.getThumbnailUrl());
        AdvertiseInfoDTO advertiseInfoDTO = new AdvertiseInfoDTO();
        advertiseInfoDTO.setAdvertised(api.isAdvertiseOnly());
        apiResultDTO.setAdvertiseInfo(advertiseInfoDTO);
        apiResultDTO.setMonetizedInfo(api.isMonetizationEnabled());
        return apiResultDTO;
    }

    /**
     * Get API result representation for content search
     * @param apiProduct API product
     * @return APISearchResultDTO
     */
    public static APISearchResultDTO fromAPIToAPIResultDTO(APIProduct apiProduct) {
        APISearchResultDTO apiResultDTO = new APISearchResultDTO();
        apiResultDTO.setId(apiProduct.getUuid());
        APIProductIdentifier apiId = apiProduct.getId();
        apiResultDTO.setName(apiId.getName());
        apiResultDTO.setVersion(apiId.getVersion());
        apiResultDTO.setProvider(APIUtil.replaceEmailDomainBack(apiId.getProviderName()));
        String context = apiProduct.getContextTemplate();
        if (context.endsWith("/" + RestApiConstants.API_VERSION_PARAM)) {
            context = context.replace("/" + RestApiConstants.API_VERSION_PARAM, "");
        }
        apiResultDTO.setContext(context);
        apiResultDTO.setAvgRating(String.valueOf(apiProduct.getRating()));
        APIBusinessInformationDTO apiBusinessInformationDTO = new APIBusinessInformationDTO();
        apiBusinessInformationDTO.setBusinessOwner(apiProduct.getBusinessOwner());
        apiBusinessInformationDTO.setBusinessOwnerEmail(apiProduct.getBusinessOwnerEmail());
        apiBusinessInformationDTO.setTechnicalOwner(apiProduct.getTechnicalOwner());
        apiBusinessInformationDTO.setTechnicalOwnerEmail(apiProduct.getTechnicalOwnerEmail());
        apiResultDTO.setBusinessInformation(apiBusinessInformationDTO);
        apiResultDTO.setType(SearchResultDTO.TypeEnum.APIPRODUCT);
        apiResultDTO.setTransportType(apiProduct.getType());
        apiResultDTO.setDescription(apiProduct.getDescription());
        apiResultDTO.setStatus(apiProduct.getState());
        apiResultDTO.setThumbnailUri(apiProduct.getThumbnailUrl());
        return apiResultDTO;
    }

    /**
     * Get Document result representation for content search
     * @param document
     * @return
     */
    public static DocumentSearchResultDTO fromDocumentationToDocumentResultDTO(Documentation document, API api) {
        DocumentSearchResultDTO docResultDTO = new DocumentSearchResultDTO();
        docResultDTO.setId(document.getId());
        docResultDTO.setName(document.getName());
        docResultDTO.setDocType(DocumentSearchResultDTO.DocTypeEnum.valueOf(document.getType().toString()));
        docResultDTO.setType(SearchResultDTO.TypeEnum.DOC);
        docResultDTO.setSummary(document.getSummary());
        docResultDTO.setVisibility(DocumentSearchResultDTO.VisibilityEnum.valueOf(document.getVisibility().toString()));
        docResultDTO.setSourceType(DocumentSearchResultDTO.SourceTypeEnum.valueOf(document.getSourceType().toString()));
        docResultDTO.setOtherTypeName(document.getOtherTypeName());
        APIIdentifier apiId = api.getId();
        docResultDTO.setApiName(apiId.getApiName());
        docResultDTO.setApiVersion(apiId.getVersion());
        docResultDTO.setApiProvider(apiId.getProviderName());
        docResultDTO.setApiUUID(api.getUUID());
        return docResultDTO;
    }

    /**
     * Sets pagination urls for a SearchResultListDTO object given pagination parameters and url parameters
     *
     * @param resultListDTO a SearchResultListDTO object
     * @param query      search condition
     * @param limit      max number of objects returned
     * @param offset     starting index
     * @param size       max offset
     */
    public static void setPaginationParams(SearchResultListDTO resultListDTO, String query, int offset, int limit, int size) {

        //acquiring pagination parameters and setting pagination urls
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

        PaginationDTO paginationDTO = new PaginationDTO();
        paginationDTO.setOffset(offset);
        paginationDTO.setLimit(limit);
        paginationDTO.setTotal(size);
        paginationDTO.setNext(paginatedNext);
        paginationDTO.setPrevious(paginatedPrevious);
        resultListDTO.setPagination(paginationDTO);
    }

}
