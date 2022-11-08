/*
 * Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.apk.apimgt.rest.api.backoffice.v1.common.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.APIProvider;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.apk.apimgt.api.model.Documentation;
import org.wso2.apk.apimgt.api.model.DocumentationContent;
import org.wso2.apk.apimgt.impl.utils.APIUtil;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.common.utils.BackofficeAPIUtils;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.common.utils.mappings.DocumentationMappingUtil;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.common.utils.mappings.PublisherCommonUtils;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.DocumentDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.DocumentListDTO;
import org.wso2.apk.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.apk.apimgt.rest.api.common.RestApiConstants;

import java.io.InputStream;
import java.util.List;

public class ApiDocumentsAPICommonImpl {

    private ApiDocumentsAPICommonImpl() {

    }

    private static final Log log = LogFactory.getLog(ApiDocumentsAPICommonImpl.class);

    public static String addAPIDocumentContent(String apiId, String documentId, InputStream inputStream,
                                                    String inlineContent, String organization, String fileName,
                                                    String mediaType) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        if (inputStream != null && inlineContent != null) {
            throw new APIManagementException("Only one of 'file' and 'inlineContent' should be specified",
                    ExceptionCodes.INVALID_PARAMETERS_PROVIDED);
        }

        //retrieves the document and send 404 if not found
        Documentation documentation = apiProvider.getDocumentation(apiId, documentId, organization);

        //add content depending on the availability of either input stream or inline content
        if (inputStream != null) {
            if (!documentation.getSourceType().equals(Documentation.DocumentSourceType.FILE)) {
                throw new APIManagementException("Source type of document " + documentId + " is not FILE",
                        ExceptionCodes.INVALID_DOCUMENT_CONTENT_DATA);
            }
            if (APIUtil.isSupportedFileType(fileName)) {
                PublisherCommonUtils.attachFileToDocument(apiId, documentation, inputStream, fileName, mediaType,
                        organization);
            } else {
                throw new APIManagementException("Unsupported extension type of document file: " + fileName,
                        ExceptionCodes.UNSUPPORTED_DOC_EXTENSION);
            }
        } else if (inlineContent != null) {
            if (!documentation.getSourceType().equals(Documentation.DocumentSourceType.INLINE) &&
                    !documentation.getSourceType().equals(Documentation.DocumentSourceType.MARKDOWN)) {
                throw new APIManagementException("Source type of document " + documentId + " is not INLINE " +
                        "or MARKDOWN", ExceptionCodes.INVALID_DOCUMENT_CONTENT_DATA);
            }
            PublisherCommonUtils
                    .addDocumentationContent(documentation, apiProvider, apiId, documentId, organization,
                            inlineContent);
        } else {
            throw new APIManagementException("Either 'file' or 'inlineContent' should be specified",
                    ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }

        //retrieving the updated doc and the URI
        Documentation updatedDoc = apiProvider.getDocumentation(apiId, documentId, organization);
        return BackofficeAPIUtils.getJsonFromDTO(DocumentationMappingUtil.fromDocumentationToDTO(updatedDoc));
    }

    public static DocumentationContent getAPIDocumentContentByDocumentId(String apiId, String documentId,
                                                                         String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        return apiProvider.getDocumentationContent(apiId, documentId, organization);
    }

    public static void deleteAPIDocument(String apiId, String documentId, String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        apiProvider.getDocumentation(apiId, documentId, organization);
        apiProvider.removeDocumentation(apiId, documentId, organization);
    }

    public static String getAPIDocumentByDocumentId(String apiId, String documentId, String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        Documentation documentation = apiProvider.getDocumentation(apiId, documentId, organization);

        return BackofficeAPIUtils.getJsonFromDTO(DocumentationMappingUtil.fromDocumentationToDTO(documentation));
    }

    public static String updateAPIDocument(String apiId, String documentId, DocumentDTO body, String organization)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        String sourceUrl = body.getSourceUrl();
        Documentation oldDocument = apiProvider.getDocumentation(apiId, documentId, organization);

        if (body.getType() == DocumentDTO.TypeEnum.OTHER && StringUtils.isBlank(body.getOtherTypeName())) {
            //check otherTypeName for not null if doc type is OTHER
            throw new APIManagementException("otherTypeName cannot be empty if type is OTHER.",
                    ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }
        if (body.getSourceType() == DocumentDTO.SourceTypeEnum.URL &&
                (StringUtils.isBlank(sourceUrl) || !RestApiCommonUtil.isURL(sourceUrl))) {
            throw new APIManagementException("Invalid document sourceUrl Format",
                    ExceptionCodes.from(ExceptionCodes.DOCUMENT_INVALID_SOURCE_TYPE, documentId));
        }

        //overriding some properties
        body.setName(oldDocument.getName());

        Documentation newDocumentation = DocumentationMappingUtil.fromDTOtoDocumentation(body);
        newDocumentation.setFilePath(oldDocument.getFilePath());
        newDocumentation.setId(documentId);
        newDocumentation = apiProvider.updateDocumentation(apiId, newDocumentation, organization);

        return BackofficeAPIUtils.getJsonFromDTO(DocumentationMappingUtil.fromDocumentationToDTO(newDocumentation));
    }

    public static String getAPIDocuments(String apiId, Integer limit, Integer offset, String organization)
            throws APIManagementException {

        RestApiCommonUtil.validateAPIExistence(apiId);

        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        //this will fail if user does not have access to the API or the API does not exist

        List<Documentation> allDocumentation = apiProvider.getAllDocumentation(apiId, organization);
        DocumentListDTO documentListDTO = DocumentationMappingUtil.fromDocumentationListToDTO(allDocumentation,
                offset, limit);
        DocumentationMappingUtil
                .setPaginationParams(documentListDTO, apiId, offset, limit, allDocumentation.size());
        return BackofficeAPIUtils.getJsonFromDTO(documentListDTO);
    }

    public static String addAPIDocument(String apiId, DocumentDTO body, String organization)
            throws APIManagementException {

        Documentation documentation = PublisherCommonUtils.addDocumentationToAPI(body, apiId, organization);
        return BackofficeAPIUtils.getJsonFromDTO(DocumentationMappingUtil.fromDocumentationToDTO(documentation));
    }

    public static void validateDocument(String apiId, String name, String organization) throws APIManagementException {

        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(apiId)) {
            throw new APIManagementException("API Id and/ or document name should not be empty",
                    ExceptionCodes.PARAMETER_NOT_PROVIDED);
        }
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        RestApiCommonUtil.validateAPIExistence(apiId);
        boolean documentationExist = apiProvider.isDocumentationExist(apiId, name, organization);
        if (!documentationExist) {
            throw new APIManagementException(ExceptionCodes.RESOURCE_NOT_FOUND);
        }
    }

}
