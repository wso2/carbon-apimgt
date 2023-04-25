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

package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductSearchResultDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APISearchResultDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentSearchResultDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.PaginationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SearchResultDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SearchResultListDTO;

import java.util.Map;

/**
 * This Class used to  map Rest api Search models to Data Models.
 */
public class SearchResultMappingUtil {

    private static final Log log = LogFactory.getLog(SearchResultMappingUtil.class);

    /**
     * Get API result representation for content search.
     *
     * @param api API
     * @return APISearchResultDTO
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
        apiResultDTO.setContextTemplate(api.getContextTemplate());
        apiResultDTO.setType(SearchResultDTO.TypeEnum.API);
        apiResultDTO.setTransportType(api.getType());
        apiResultDTO.setDescription(api.getDescription());
        apiResultDTO.setStatus(api.getStatus());
        apiResultDTO.setThumbnailUri(api.getThumbnailUrl());
        apiResultDTO.setAdvertiseOnly(api.isAdvertiseOnly());
        apiResultDTO.setHasThumbnail(!StringUtils.isBlank(api.getThumbnailUrl()));
        return apiResultDTO;
    }

    /**
     * Get API result representation for content search.
     *
     * @param apiProduct APIProduct
     * @return APIProductSearchResultDTO
     */
    public static APIProductSearchResultDTO fromAPIProductToAPIResultDTO(APIProduct apiProduct) {

        APIProductSearchResultDTO apiProductResultDTO = new APIProductSearchResultDTO();
        apiProductResultDTO.setId(apiProduct.getUuid());
        APIProductIdentifier apiproductId = apiProduct.getId();
        apiProductResultDTO.setName(apiproductId.getName());
        apiProductResultDTO.setVersion(apiproductId.getVersion());
        apiProductResultDTO.setProvider(APIUtil.replaceEmailDomainBack(apiproductId.getProviderName()));
        String context = apiProduct.getContextTemplate();
        if (context.endsWith("/" + RestApiConstants.API_VERSION_PARAM)) {
            context = context.replace("/" + RestApiConstants.API_VERSION_PARAM, "");
        }
        apiProductResultDTO.setContext(context);
        apiProductResultDTO.setType(SearchResultDTO.TypeEnum.APIPRODUCT);
        apiProductResultDTO.setDescription(apiProduct.getDescription());
        apiProductResultDTO.setStatus(apiProduct.getState());
        apiProductResultDTO.setThumbnailUri(apiProduct.getThumbnailUrl());
        apiProductResultDTO.setHasThumbnail(!StringUtils.isBlank(apiProduct.getThumbnailUrl()));
        return apiProductResultDTO;
    }

    /**
     * Get Document result representation for content search.
     *
     * @param document Api Document
     * @return DocumentSearchResultDTO
     */
    public static DocumentSearchResultDTO fromDocumentationToDocumentResultDTO(
            Documentation document, API api) throws APIManagementException {

        DocumentSearchResultDTO docResultDTO = new DocumentSearchResultDTO();
        docResultDTO.setId(document.getId());
        docResultDTO.setName(document.getName());
        docResultDTO.setDocType(DocumentSearchResultDTO.DocTypeEnum.valueOf(document.getType().toString()));
        docResultDTO.setType(SearchResultDTO.TypeEnum.DOC);
        docResultDTO.setSummary(document.getSummary());
        docResultDTO.associatedType(APIConstants.AuditLogConstants.API);
        docResultDTO.setVisibility(mapVisibilityFromDocumentToDTO(document.getVisibility()));
        docResultDTO.setSourceType(mapSourceTypeFromDocumentToDTO(document.getSourceType()));
        docResultDTO.setOtherTypeName(document.getOtherTypeName());
        APIIdentifier apiId = api.getId();
        docResultDTO.setApiName(apiId.getApiName());
        docResultDTO.setApiVersion(apiId.getVersion());
        docResultDTO.setApiProvider(APIUtil.replaceEmailDomainBack(apiId.getProviderName()));
        docResultDTO.setApiUUID(api.getUUID());
        return docResultDTO;
    }

    public static DocumentSearchResultDTO fromDocumentationToProductDocumentResultDTO(
            Documentation document, APIProduct apiProduct) throws APIManagementException {

        DocumentSearchResultDTO docResultDTO = new DocumentSearchResultDTO();
        docResultDTO.setId(document.getId());
        docResultDTO.setName(document.getName());
        docResultDTO.setDocType(DocumentSearchResultDTO.DocTypeEnum.valueOf(document.getType().toString()));
        docResultDTO.setType(SearchResultDTO.TypeEnum.DOC);
        docResultDTO.associatedType(APIConstants.AuditLogConstants.API_PRODUCT);
        docResultDTO.setSummary(document.getSummary());
        docResultDTO.setVisibility(mapVisibilityFromDocumentToDTO(document.getVisibility()));
        docResultDTO.setSourceType(mapSourceTypeFromDocumentToDTO(document.getSourceType()));
        docResultDTO.setOtherTypeName(document.getOtherTypeName());
        APIProductIdentifier apiId = apiProduct.getId();
        docResultDTO.setApiName(apiId.getName());
        docResultDTO.setApiVersion(apiId.getVersion());
        docResultDTO.setApiProvider(APIUtil.replaceEmailDomainBack(apiId.getProviderName()));
        docResultDTO.setApiUUID(apiProduct.getUuid());
        return docResultDTO;
    }

    /**
     * Sets pagination urls for a SearchResultListDTO object given pagination parameters and url parameters.
     *
     * @param resultListDTO a SearchResultListDTO object
     * @param query         search condition
     * @param limit         max number of objects returned
     * @param offset        starting index
     * @param size          max offset
     */
    public static void setPaginationParams(SearchResultListDTO resultListDTO, String query, int offset, int limit,
                                           int size) {

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
        paginationDTO.setNext(paginatedNext);
        paginationDTO.setPrevious(paginatedPrevious);
        paginationDTO.setOffset(offset);
        paginationDTO.setLimit(limit);
        paginationDTO.setTotal(size);
        resultListDTO.setPagination(paginationDTO);
    }

    public static DocumentSearchResultDTO.SourceTypeEnum mapSourceTypeFromDocumentToDTO
            (Documentation.DocumentSourceType sourceType) throws APIManagementException {

        switch (sourceType) {
            case URL:
                return DocumentSearchResultDTO.SourceTypeEnum.URL;
            case FILE:
                return DocumentSearchResultDTO.SourceTypeEnum.FILE;
            case INLINE:
                return DocumentSearchResultDTO.SourceTypeEnum.INLINE;
            case MARKDOWN:
                return DocumentSearchResultDTO.SourceTypeEnum.MARKDOWN;
            default:
                throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.DOCUMENT_INVALID_SOURCE_TYPE,
                        sourceType.toString()));
        }
    }

    public static DocumentSearchResultDTO.VisibilityEnum mapVisibilityFromDocumentToDTO
            (Documentation.DocumentVisibility visibility) throws APIManagementException {

        switch (visibility) {
            case API_LEVEL:
                return DocumentSearchResultDTO.VisibilityEnum.API_LEVEL;
            case OWNER_ONLY:
                return DocumentSearchResultDTO.VisibilityEnum.OWNER_ONLY;
            case PRIVATE:
                return DocumentSearchResultDTO.VisibilityEnum.PRIVATE;
            default:
                throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.DOCUMENT_INVALID_VISIBILITY,
                        visibility.toString()));
        }
    }
}
