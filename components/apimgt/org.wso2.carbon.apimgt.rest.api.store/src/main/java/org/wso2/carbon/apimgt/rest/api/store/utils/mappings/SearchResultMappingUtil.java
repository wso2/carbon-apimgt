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

package org.wso2.carbon.apimgt.rest.api.store.utils.mappings;

import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.DocumentSearchResultDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.SearchResultDTO;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.Map;

public class SearchResultMappingUtil {

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
        apiResultDTO.setProvider(apiId.getProviderName());
        String context = api.getContextTemplate();
        if (context.endsWith("/" + RestApiConstants.API_VERSION_PARAM)) {
            context = context.replace("/" + RestApiConstants.API_VERSION_PARAM, "");
        }
        apiResultDTO.setContext(context);
        apiResultDTO.setType(SearchResultDTO.TypeEnum.API);
        apiResultDTO.setDescription(api.getDescription());
        apiResultDTO.setStatus(api.getStatus());
        apiResultDTO.setThumbnailUri(api.getThumbnailUrl());
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

        resultListDTO.setNext(paginatedNext);
        resultListDTO.setPrevious(paginatedPrevious);
    }

}
