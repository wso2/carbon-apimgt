/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.rest.api.store.mappings;

import org.wso2.carbon.apimgt.core.models.DocumentInfo;
import org.wso2.carbon.apimgt.rest.api.store.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.DocumentListDTO;

import java.util.ArrayList;
import java.util.List;

public class DocumentationMappingUtil {
    /**
     * Converts a List object of ArtifactResourceMetaData into a DTO
     *
     * @param documentInfoList List of ArtifactResourceMetaData
     * @param limit          maximum number of APIs returns
     * @param offset         starting index
     * @return DocumentListDTO object containing Document DTOs
     */
    public static DocumentListDTO fromDocumentationListToDTO(List<DocumentInfo> documentInfoList, int offset,
            int limit) {
        DocumentListDTO documentListDTO = new DocumentListDTO();
        List<DocumentDTO> documentDTOs = documentListDTO.getList();
        if (documentDTOs == null) {
            documentDTOs = new ArrayList<>();
            documentListDTO.setList(documentDTOs);
        }

        //add the required range of objects to be returned
        int start = offset < documentInfoList.size() && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = offset + limit - 1 <= documentInfoList.size() - 1 ? offset + limit - 1 : documentInfoList.size() - 1;
        for (int i = start; i <= end; i++) {
            documentDTOs.add(fromDocumentationToDTO(documentInfoList.get(i)));
        }
        documentListDTO.setCount(documentDTOs.size());
        return documentListDTO;
    }

    /** Converts an ArtifactResourceMetaData object into corresponding REST API Document DTO object
     *
     * @param documentInfo DocumentInfo object
     * @return a new DocumentDTO object corresponding to given ArtifactResourceMetaData object
     */
    public static DocumentDTO fromDocumentationToDTO(DocumentInfo documentInfo) {
        DocumentDTO documentDTO = new DocumentDTO();
        documentDTO.setDocumentId(documentInfo.getId());
        documentDTO.setName(documentInfo.getName());
        documentDTO.setSummary(documentInfo.getSummary());
        documentDTO.setType(DocumentDTO.TypeEnum.valueOf(documentInfo.getType().toString()));
        documentDTO.setOtherTypeName(documentInfo.getOtherType());
        documentDTO.setSourceType(DocumentDTO.SourceTypeEnum.valueOf(documentInfo.getSourceType().toString()));
        documentDTO.setSourceUrl(documentInfo.getSourceURL());

        return documentDTO;
    }
}
