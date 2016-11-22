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
     * Converts a List object of Documents into a DTO
     *
     * @param documentations List of Documentations
     * @param limit          maximum number of APIs returns
     * @param offset         starting index
     * @return DocumentListDTO object containing Document DTOs
     */
    public static DocumentListDTO fromDocumentationListToDTO(List<DocumentInfo> documentations, int offset,
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

    /** Converts a APIM core Document object into corresponding REST API Document DTO object
     *
     * @param documentation Documentation object
     * @return a new DocumentDTO object corresponding to given Documentation object
     */
    public static DocumentDTO fromDocumentationToDTO(DocumentInfo documentation) {
        DocumentDTO documentDTO = new DocumentDTO();
        documentDTO.setDocumentId(documentation.getId());
        documentDTO.setName(documentation.getName());
        documentDTO.setSummary(documentation.getSummary());
        documentDTO.setType(DocumentDTO.TypeEnum.valueOf(documentation.getType().toString()));
        documentDTO.setOtherTypeName(documentation.getOtherType());
        if (documentation.getSourceType() != null)
            documentDTO.setSourceType(DocumentDTO.SourceTypeEnum.valueOf(documentation.getSourceType().toString()));
        documentDTO.setSourceUrl(documentation.getSourceURL());
        return documentDTO;
    }
}
