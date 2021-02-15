/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 *
 */

package org.wso2.carbon.apimgt.rest.api.store.v1.mappings;

import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PaginationDTO;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class is responsible for mapping APIM core documentation related objects into REST API documentation
 * related DTOs
 */
public class DocumentationMappingUtil {

    /**
     * Converts a APIM core Document object into corresponding REST API Document DTO object
     *
     * @param documentation Documentation object
     * @return a new DocumentDTO object corresponding to given Documentation object
     */
    public static DocumentDTO fromDocumentationToDTO(Documentation documentation) {

        DocumentDTO documentDTO = new DocumentDTO();
        documentDTO.setDocumentId(documentation.getId());
        documentDTO.setName(documentation.getName());
        documentDTO.setSummary(documentation.getSummary());
        documentDTO.setType(DocumentDTO.TypeEnum.valueOf(documentation.getType().toString()));
        documentDTO.setOtherTypeName(documentation.getOtherTypeName());
        if (documentation.getSourceType() != null) {
            documentDTO.setSourceType(DocumentDTO.SourceTypeEnum.valueOf(documentation.getSourceType().toString()));
        }
        documentDTO.setSourceUrl(documentation.getSourceUrl());
        return documentDTO;
    }

    /**
     * Converts a List object of Documents into a DTO
     *
     * @param documentations List of Documentations
     * @param limit          maximum number of APIs returns
     * @param offset         starting index
     * @return DocumentListDTO object containing Document DTOs
     */
    public static DocumentListDTO fromDocumentationListToDTO(List<Documentation> documentations, int offset,
                                                             int limit) {

        DocumentListDTO documentListDTO = new DocumentListDTO();
        List<DocumentDTO> documentDTOs = documentListDTO.getList();
        if (documentDTOs == null) {
            documentDTOs = new ArrayList<>();
            documentListDTO.setList(documentDTOs);
        }

        //add the required range of objects to be returned
        int start = offset < documentations.size() && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = offset + limit - 1 <= documentations.size() - 1 ? offset + limit - 1 : documentations.size() - 1;
        for (int i = start; i <= end; i++) {
            documentDTOs.add(fromDocumentationToDTO(documentations.get(i)));
        }
        documentListDTO.setCount(documentDTOs.size());
        return documentListDTO;
    }

    /**
     * Sets pagination urls for a DocumentListDTO object given pagination parameters and url parameters
     *
     * @param documentListDTO a DocumentListDTO object
     * @param limit           max number of objects returned
     * @param offset          starting index
     * @param size            max offset
     */
    public static void setPaginationParams(DocumentListDTO documentListDTO, String apiId, int offset,
                                           int limit, int size) {

        //acquiring pagination parameters and setting pagination urls
        Map<String, Integer> paginatedParams = RestApiCommonUtil.getPaginationParams(offset, limit, size);
        String paginatedPrevious = "";
        String paginatedNext = "";

        if (paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET) != null) {
            paginatedPrevious = RestApiCommonUtil
                    .getDocumentationPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_PREVIOUS_LIMIT), apiId);
        }

        if (paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET) != null) {
            paginatedNext = RestApiCommonUtil
                    .getDocumentationPaginatedURL(paginatedParams.get(RestApiConstants.PAGINATION_NEXT_OFFSET),
                            paginatedParams.get(RestApiConstants.PAGINATION_NEXT_LIMIT), apiId);
        }

        PaginationDTO paginationDTO = CommonMappingUtil
                .getPaginationDTO(limit, offset, size, paginatedNext, paginatedPrevious);
        documentListDTO.setPagination(paginationDTO);
    }

}
