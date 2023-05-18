/*
 * Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.APIProduct;
import org.wso2.carbon.apimgt.api.model.APIProductIdentifier;
import org.wso2.carbon.apimgt.api.model.APIStateChangeResponse;
import org.wso2.carbon.apimgt.api.model.APIStatus;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DocumentationContent;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.APIRevision;
import org.wso2.carbon.apimgt.api.model.APIRevisionDeployment;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.ExportFormat;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportAPI;
import org.wso2.carbon.apimgt.impl.importexport.utils.APIImportExportUtil;
import org.wso2.carbon.apimgt.impl.restapi.publisher.ApiProductsApiServiceImplUtils;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.ApiProductsApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.DocumentationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.PublisherCommonUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIProductListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.FileInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionDeploymentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleStateDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.WorkflowResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.RestApiPublisherUtils;
import org.wso2.carbon.apimgt.rest.api.util.exception.BadRequestException;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantUtils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.wso2.carbon.apimgt.impl.APIConstants.DOCUMENTATION_INLINE_CONTENT_TYPE;
import static org.wso2.carbon.apimgt.impl.APIConstants.UN_AUTHORIZED_ERROR_MESSAGE;

public class ApiProductsApiServiceImpl implements ApiProductsApiService {
    private static final Log log = LogFactory.getLog(ApiProductsApiServiceImpl.class);

    @Override public Response deleteAPIProduct(String apiProductId, String ifMatch,
            MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String username = RestApiCommonUtil.getLoggedInUsername();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIProductIdentifier apiProductIdentifier = APIMappingUtil.getAPIProductIdentifierFromUUID(apiProductId, organization);
            if (log.isDebugEnabled()) {
                log.debug("Delete API Product request: Id " + apiProductId + " by " + username);
            }
            APIProduct apiProduct = apiProvider.getAPIProductbyUUID(apiProductId, organization);
            if (apiProduct == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API_PRODUCT, apiProductId, log);
            } else {
                boolean isAPIPublishedOrDeprecated = APIStatus.PUBLISHED.getStatus().equals(apiProduct.getState()) ||
                        APIStatus.DEPRECATED.getStatus().equals(apiProduct.getState());
                List<SubscribedAPI> apiUsages = apiProvider.getAPIProductUsageByAPIProductId(apiProductIdentifier);
                if (isAPIPublishedOrDeprecated && (apiUsages != null && apiUsages.size() > 0)) {
                    RestApiUtil.handleConflict("Cannot remove the API " + apiProductIdentifier + " as active subscriptions exist", log);
                }
                apiProduct.setOrganization(organization);
            }
            apiProvider.deleteAPIProduct(apiProduct);
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while deleting API Product : " + apiProductId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response getAPIProductDocumentContent(String apiProductId,
            String documentId, String accept, String ifNoneMatch, MessageContext messageContext) {
        Documentation documentation;
        try {
            String username = RestApiCommonUtil.getLoggedInUsername();
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);

            //this will fail if user does not have access to the API Product or the API Product does not exist
            APIProductIdentifier productIdentifier = APIMappingUtil.getAPIProductIdentifierFromUUID(apiProductId, organization);
            //documentation = apiProvider.getProductDocumentation(documentId, tenantDomain);
            DocumentationContent docContent = apiProvider.getDocumentationContent(apiProductId, documentId, organization);
            if (docContent == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_PRODUCT_DOCUMENTATION, documentId, log);
                return null;
            }

            //gets the content depending on the type of the document
            if (docContent.getSourceType().equals(DocumentationContent.ContentSourceType.FILE)) {
                String contentType = docContent.getResourceFile().getContentType();
                contentType = contentType == null ? RestApiConstants.APPLICATION_OCTET_STREAM : contentType;
                String name = docContent.getResourceFile().getName();
                return Response.ok(docContent.getResourceFile().getContent())
                        .header(RestApiConstants.HEADER_CONTENT_TYPE, contentType)
                        .header(RestApiConstants.HEADER_CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\"")
                        .build();
            } else if (docContent.getSourceType().equals(DocumentationContent.ContentSourceType.INLINE)
                    || docContent.getSourceType().equals(DocumentationContent.ContentSourceType.MARKDOWN)) {
                String content = docContent.getTextContent();
                return Response.ok(content)
                        .header(RestApiConstants.HEADER_CONTENT_TYPE, DOCUMENTATION_INLINE_CONTENT_TYPE)
                        .build();
            } else if (docContent.getSourceType().equals(DocumentationContent.ContentSourceType.URL)) {
                String sourceUrl = docContent.getTextContent();
                return Response.seeOther(new URI(sourceUrl)).build();
            }
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_PRODUCT_DOCUMENTATION, apiProductId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while retrieving document : " + documentId + " of API Product " + apiProductId, e, log);
            } else {
                String errorMessage = "Error while retrieving document " + documentId + " of the API Product" + apiProductId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving source URI location of " + documentId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response addAPIProductDocumentContent(String apiProductId, String documentId,
                              String ifMatch, InputStream fileInputStream, Attachment fileDetail, String inlineContent,
                                                                          MessageContext messageContext) {
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            APIProduct product = apiProvider.getAPIProductbyUUID(apiProductId, organization);
            APIProductIdentifier productIdentifier = product.getId();
            if (fileInputStream != null && inlineContent != null) {
                RestApiUtil.handleBadRequest("Only one of 'file' and 'inlineContent' should be specified", log);
            }

            //retrieves the document and send 404 if not found
            Documentation documentation = apiProvider.getDocumentation(apiProductId, documentId, organization);
            if (documentation == null) {
                RestApiUtil
                        .handleResourceNotFoundError(RestApiConstants.RESOURCE_PRODUCT_DOCUMENTATION, documentId, log);
                return null;
            }

            //add content depending on the availability of either input stream or inline content
            if (fileInputStream != null) {
                if (!documentation.getSourceType().equals(Documentation.DocumentSourceType.FILE)) {
                    RestApiUtil.handleBadRequest("Source type of product document " + documentId + " is not FILE", log);
                }
                RestApiPublisherUtils
                        .attachFileToProductDocument(apiProductId, documentation, fileInputStream, fileDetail,
                                organization);
            } else if (inlineContent != null) {
                if (!documentation.getSourceType().equals(Documentation.DocumentSourceType.INLINE) && !documentation
                        .getSourceType().equals(Documentation.DocumentSourceType.MARKDOWN)) {
                    RestApiUtil.handleBadRequest(
                            "Source type of product document " + documentId + " is not INLINE " + "or MARKDOWN", log);
                }
                PublisherCommonUtils
                        .addDocumentationContent(documentation, apiProvider, apiProductId, documentId, organization,
                                inlineContent);
            } else {
                RestApiUtil.handleBadRequest("Either 'file' or 'inlineContent' should be specified", log);
            }

            //retrieving the updated doc and the URI
            Documentation updatedDoc = apiProvider.getDocumentation(apiProductId, documentId, organization);
            DocumentDTO documentDTO = DocumentationMappingUtil.fromDocumentationToDTO(updatedDoc);
            String uriString = RestApiConstants.RESOURCE_PATH_PRODUCT_DOCUMENT_CONTENT
                    .replace(RestApiConstants.APIPRODUCTID_PARAM, apiProductId)
                    .replace(RestApiConstants.DOCUMENTID_PARAM, documentId);
            URI uri = new URI(uriString);
            return Response.created(uri).entity(documentDTO).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API_PRODUCT, apiProductId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while adding content to the document: " + documentId + " of API Product "
                                + apiProductId, e, log);
            } else {
                RestApiUtil.handleInternalServerError("Failed to add content to the document " + documentId, e, log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving document content location : " + documentId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } finally {
            IOUtils.closeQuietly(fileInputStream);
        }
        return null;
    }

    @Override
    public Response deleteAPIProductDocument(String apiProductId, String documentId,
            String ifMatch, MessageContext messageContext) {
        Documentation documentation;
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);

            //this will fail if user does not have access to the API Product or the API Product does not exist
            APIProductIdentifier productIdentifier = APIMappingUtil
                    .getAPIProductIdentifierFromUUID(apiProductId, organization);
            documentation = apiProvider.getDocumentation(apiProductId, documentId, organization);
            if (documentation == null) {
                RestApiUtil
                        .handleResourceNotFoundError(RestApiConstants.RESOURCE_PRODUCT_DOCUMENTATION, documentId, log);
            }
            apiProvider.removeDocumentation(apiProductId, documentId, organization);
            return Response.ok().build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing API Products. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API_PRODUCT, apiProductId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while deleting : " + documentId + " of API Product " + apiProductId, e,
                        log);
            } else {
                String errorMessage = "Error while retrieving API Product : " + apiProductId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    @Override
    public Response getAPIProductDocument(String apiProductId, String documentId,
            String accept, String ifNoneMatch, MessageContext messageContext) {
        Documentation documentation;
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            documentation = apiProvider.getDocumentation(apiProductId, documentId, organization);
            APIMappingUtil.getAPIProductIdentifierFromUUID(apiProductId, organization);
            if (documentation == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_PRODUCT_DOCUMENTATION, documentId, log);
            }

            DocumentDTO documentDTO = DocumentationMappingUtil.fromDocumentationToDTO(documentation);
            return Response.ok().entity(documentDTO).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing API Products. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API_PRODUCT, apiProductId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while retrieving document : " + documentId + " of API Product "
                                + apiProductId, e, log);
            } else {
                String errorMessage = "Error while retrieving document : " + documentId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    @Override
    public Response updateAPIProductDocument(String apiProductId, String documentId,
            DocumentDTO body, String ifMatch, MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            String sourceUrl = body.getSourceUrl();
            Documentation oldDocument = apiProvider.getDocumentation(apiProductId, documentId, organization);

            //validation checks for existence of the document
            if (oldDocument == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_PRODUCT_DOCUMENTATION, documentId, log);
                return null;
            }
            if (body.getType() == null) {
                throw new BadRequestException();
            }
            if (body.getType() == DocumentDTO.TypeEnum.OTHER && org.apache.commons.lang3.StringUtils.isBlank(body.getOtherTypeName())) {
                //check otherTypeName for not null if doc type is OTHER
                RestApiUtil.handleBadRequest("otherTypeName cannot be empty if type is OTHER.", log);
                return null;
            }
            if (body.getSourceType() == DocumentDTO.SourceTypeEnum.URL &&
                    (org.apache.commons.lang3.StringUtils.isBlank(sourceUrl) || !RestApiCommonUtil.isURL(sourceUrl))) {
                RestApiUtil.handleBadRequest("Invalid document sourceUrl Format", log);
                return null;
            }

            //overriding some properties
            body.setName(oldDocument.getName());

            Documentation newDocumentation = DocumentationMappingUtil.fromDTOtoDocumentation(body);
            //this will fail if user does not have access to the API or the API does not exist
            APIProductIdentifier apiIdentifier = APIMappingUtil.getAPIProductIdentifierFromUUID(apiProductId, organization);
            newDocumentation.setFilePath(oldDocument.getFilePath());
            newDocumentation.setId(oldDocument.getId());
            apiProvider.updateDocumentation(apiProductId, newDocumentation, organization);

            //retrieve the updated documentation
            newDocumentation = apiProvider.getDocumentation(apiProductId, documentId, organization);
            return Response.ok().entity(DocumentationMappingUtil.fromDocumentationToDTO(newDocumentation)).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API_PRODUCT, apiProductId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while updating document : " + documentId + " of API Product " + apiProductId, e, log);
            } else {
                String errorMessage = "Error while updating the document " + documentId + " for API Product : " + apiProductId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    @Override
    public Response getAPIProductDocuments(String apiProductId, Integer limit, Integer offset,
            String accept, String ifNoneMatch, MessageContext messageContext) {

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            //this will fail if user does not have access to the API Product or the API Product does not exist
            APIProductIdentifier productIdentifier = APIMappingUtil.getAPIProductIdentifierFromUUID(apiProductId, organization);
            List<Documentation> allDocumentation = apiProvider.getAllDocumentation(apiProductId, organization);
            DocumentListDTO documentListDTO = DocumentationMappingUtil.fromDocumentationListToDTO(allDocumentation,
                    offset, limit);
            DocumentationMappingUtil
                    .setPaginationParams(documentListDTO, apiProductId, offset, limit, allDocumentation.size());
            return Response.ok().entity(documentListDTO).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API_PRODUCT, apiProductId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while retrieving documents of API Product : " + apiProductId, e, log);
            } else {
                String msg = "Error while retrieving documents of API Product " + apiProductId;
                RestApiUtil.handleInternalServerError(msg, e, log);
            }
        }
        return null;
    }

    @Override
    public Response addAPIProductDocument(String apiProductId, DocumentDTO body,
            MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            if (body.getType() == null) {
                throw new BadRequestException();
            }
            if (body.getType() == DocumentDTO.TypeEnum.OTHER && org.apache.commons.lang3.StringUtils.isBlank(body.getOtherTypeName())) {
                //check otherTypeName for not null if doc type is OTHER
                RestApiUtil.handleBadRequest("otherTypeName cannot be empty if type is OTHER.", log);
            }
            String sourceUrl = body.getSourceUrl();
            if (body.getSourceType() == DocumentDTO.SourceTypeEnum.URL &&
                    (org.apache.commons.lang3.StringUtils.isBlank(sourceUrl) || !RestApiCommonUtil.isURL(sourceUrl))) {
                RestApiUtil.handleBadRequest("Invalid document sourceUrl Format", log);
            }
            Documentation documentation = DocumentationMappingUtil.fromDTOtoDocumentation(body);
            String documentName = body.getName();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            //this will fail if user does not have access to the API Product or the API Product does not exist
            APIProductIdentifier productIdentifier = APIMappingUtil.getAPIProductIdentifierFromUUID(apiProductId, organization);
            if (apiProvider.isDocumentationExist(apiProductId, documentName, organization)) {
                String errorMessage = "Requested document '" + documentName + "' already exists";
                RestApiUtil.handleResourceAlreadyExistsError(errorMessage, log);
            }
            documentation = apiProvider.addDocumentation(apiProductId, documentation, organization);

            DocumentDTO newDocumentDTO = DocumentationMappingUtil.fromDocumentationToDTO(documentation);
            String uriString = RestApiConstants.RESOURCE_PATH_PRODUCT_DOCUMENTS_DOCUMENT_ID
                    .replace(RestApiConstants.APIPRODUCTID_PARAM, apiProductId)
                    .replace(RestApiConstants.DOCUMENTID_PARAM, documentation.getId());
            URI uri = new URI(uriString);
            return Response.created(uri).entity(newDocumentDTO).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing API Products. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API_PRODUCT, apiProductId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil
                        .handleAuthorizationFailure("Authorization failure while adding documents of API : " + apiProductId, e,
                                log);
            } else {
                String errorMessage = "Error while adding the document for API : " + apiProductId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving location for document " + body.getName() + " of API " + apiProductId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override public Response getAPIProduct(String apiProductId, String accept, String ifNoneMatch,
            MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String username = RestApiCommonUtil.getLoggedInUsername();
            String tenantDomain = MultitenantUtils.getTenantDomain(APIUtil.replaceEmailDomainBack(username));
            if (log.isDebugEnabled()) {
                log.debug("API Product request: Id " +apiProductId + " by " + username);
            }
            APIProduct apiProduct = apiProvider.getAPIProductbyUUID(apiProductId, tenantDomain);
            if (apiProduct == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API_PRODUCT, apiProductId, log);
            }

            APIProductDTO createdApiProductDTO = getAPIProductByID(apiProductId, apiProvider);
            return Response.ok().entity(createdApiProductDTO).build();
        } catch (APIManagementException e) {
            if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("User is not authorized to access the API", e, log);
            } else {
                String errorMessage = "Error while retrieving API Product from Id  : " + apiProductId ;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    @Override
    public Response getIsAPIProductOutdated(String apiProductId, String accept, String ifNoneMatch,
                                                         MessageContext messageContext) throws APIManagementException {
        return null;
    }

    @Override
    public Response updateAPIProduct(String apiProductId, APIProductDTO body, String ifMatch,
            MessageContext messageContext) {
        try {
            String username = RestApiCommonUtil.getLoggedInUsername();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiCommonUtil.getProvider(username);
            APIProduct retrievedProduct = apiProvider.getAPIProductbyUUID(apiProductId, tenantDomain);
            if (retrievedProduct == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API_PRODUCT, apiProductId, log);
            }
            APIProduct updatedProduct = PublisherCommonUtils.updateApiProduct(retrievedProduct, body, apiProvider, username, tenantDomain);
            APIProductDTO updatedProductDTO = getAPIProductByID(apiProductId, apiProvider);
            return Response.ok().entity(updatedProductDTO).build();
        } catch (APIManagementException | FaultGatewaysException e) {
            if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("User is not authorized to access the API", e, log);
            } else {
                String errorMessage = "Error while updating API Product : " + apiProductId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    @Override public Response getAPIProductSwagger(String apiProductId, String accept, String ifNoneMatch,
            MessageContext messageContext) {
        try {
            String username = RestApiCommonUtil.getLoggedInUsername();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            APIProvider apiProvider = RestApiCommonUtil.getProvider(username);
            APIProduct retrievedProduct = apiProvider.getAPIProductbyUUID(apiProductId, tenantDomain);
            if (retrievedProduct == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API_PRODUCT, apiProductId, log);
            }
            String apiSwagger = apiProvider.getOpenAPIDefinition(apiProductId, tenantDomain);
            
            if (StringUtils.isEmpty(apiSwagger)) {
                apiSwagger = "";
            }
            return Response.ok().entity(apiSwagger).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving API Product from Id  : " + apiProductId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response getAPIProductThumbnail(String apiProductId, String accept,
            String ifNoneMatch, MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            //this will fail if user does not have access to the API or the API does not exist
            APIProductIdentifier productIdentifier = APIMappingUtil
                    .getAPIProductIdentifierFromUUID(apiProductId, tenantDomain);
            ResourceFile thumbnailResource = apiProvider.getIcon(apiProductId, tenantDomain);

            if (thumbnailResource != null) {
                return Response
                        .ok(thumbnailResource.getContent(), MediaType.valueOf(thumbnailResource.getContentType()))
                        .build();
            } else {
                return Response.noContent().build();
            }
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing API Products. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API_PRODUCT, apiProductId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while retrieving thumbnail of API Product : " + apiProductId, e, log);
            } else {
                String errorMessage = "Error while retrieving thumbnail of API Product : " + apiProductId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    @Override
    public Response updateAPIProductThumbnail(String apiProductId, InputStream fileInputStream,
            Attachment fileDetail, String ifMatch, MessageContext messageContext) {
        ByteArrayInputStream inputStream = null;
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            String fileName = fileDetail.getDataHandler().getName();
            String extension = FilenameUtils.getExtension(fileName);
            if (!RestApiConstants.ALLOWED_THUMBNAIL_EXTENSIONS.contains(extension.toLowerCase())) {
                RestApiUtil.handleBadRequest(
                        "Unsupported Thumbnail File Extension. Supported extensions are .jpg, .png, .jpeg, .svg "
                                + "and .gif", log);
            }
            inputStream = (ByteArrayInputStream) RestApiPublisherUtils.validateThumbnailContent(fileInputStream);
            String fileMediaType = RestApiPublisherUtils.getMediaType(inputStream, fileDetail);

            //this will fail if user does not have access to the API or the API does not exist
            APIProduct apiProduct = apiProvider.getAPIProductbyUUID(apiProductId, tenantDomain);
            ResourceFile apiImage = new ResourceFile(inputStream, fileMediaType);
            apiProvider.setThumbnailToAPI(apiProductId, apiImage, tenantDomain);
            /*
            String thumbPath = APIUtil.getProductIconPath(apiProduct.getId());
            String thumbnailUrl = apiProvider.addProductResourceFile(apiProduct.getId(), thumbPath, apiImage);
            apiProduct.setThumbnailUrl(APIUtil.prependTenantPrefix(thumbnailUrl, apiProduct.getId().getProviderName()));
            APIUtil.setResourcePermissions(apiProduct.getId().getProviderName(), null, null, thumbPath);

            //need to set product resource mappings before updating product, otherwise existing mappings will be lost
            List<APIProductResource> resources = apiProvider.getResourcesOfAPIProduct(apiProduct.getId());
            apiProduct.setProductResources(resources);
            apiProvider.updateAPIProduct(apiProduct);
            */
            String uriString = RestApiConstants.RESOURCE_PATH_THUMBNAIL
                    .replace(RestApiConstants.APIID_PARAM, apiProductId);
            URI uri = new URI(uriString);
            FileInfoDTO infoDTO = new FileInfoDTO();
            infoDTO.setRelativePath(uriString);
            infoDTO.setMediaType(apiImage.getContentType());
            return Response.created(uri).entity(infoDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while updating API Product : " + apiProductId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (URISyntaxException | IOException e) {
            String errorMessage = "Error while updating thumbnail location of API Product : " + apiProductId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } finally {
            IOUtils.closeQuietly(fileInputStream);
            IOUtils.closeQuietly(inputStream);
        }
        return null;
    }

    /**
     * Exports an API Product from API Manager. Meta information, API Product icon, documentation, client certificates
     * and dependent APIs are exported. This service generates a zipped archive which contains all the above mentioned
     * resources for a given API Product.
     *
     * @param name           Name of the API Product that needs to be exported
     * @param version        Version of the API Product that needs to be exported
     * @param providerName   Provider name of the API Product that needs to be exported
     * @param format         Format of output documents. Can be YAML or JSON
     * @param preserveStatus Preserve API Product status on export
     * @param messageContext Message Context
     * @return Zipped file containing exported API Product
     * @throws APIManagementException
     */
    @Override
    public Response exportAPIProduct(String name, String version, String providerName, String revisionNumber,
                                     String format, Boolean preserveStatus, Boolean exportLatestRevision,
                                     MessageContext messageContext) throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        //If not specified status is preserved by default
        preserveStatus = preserveStatus == null || preserveStatus;

        // Default export format is YAML
        ExportFormat exportFormat = StringUtils.isNotEmpty(format) ? ExportFormat.valueOf(format.toUpperCase()) :
                ExportFormat.YAML;
        ImportExportAPI importExportAPI = APIImportExportUtil.getImportExportAPI();
        try {
            File file = importExportAPI
                    .exportAPIProduct(null, name, version, providerName, revisionNumber, exportFormat, preserveStatus,
                            true, true, exportLatestRevision, organization);
            return Response.ok(file)
                    .header(RestApiConstants.HEADER_CONTENT_DISPOSITION, "attachment; filename=\""
                            + file.getName() + "\"")
                    .build();
        } catch (APIImportExportException e) {
            throw new APIManagementException("Error while exporting " + RestApiConstants.RESOURCE_API_PRODUCT, e);
        }
    }

    @Override
    public Response getAllAPIProducts(Integer limit, Integer offset, String query, String accept,
            String ifNoneMatch, MessageContext messageContext) {

        List<APIProduct> allMatchedProducts = new ArrayList<>();
        APIProductListDTO apiProductListDTO;

        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query == null ? "" : query;

        try {
            String username = RestApiCommonUtil.getLoggedInUsername();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            if (log.isDebugEnabled()) {
                log.debug("API Product list request by " + username);
            }
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            Map<String, Object> result = apiProvider.searchPaginatedAPIProducts(query, organization, offset, limit);

            Set<APIProduct> apiProducts = (Set<APIProduct>) result.get("products");
            allMatchedProducts.addAll(apiProducts);

            apiProductListDTO = APIMappingUtil.fromAPIProductListtoDTO(allMatchedProducts);

            //Add pagination section in the response
            Object totalLength = result.get("length");
            Integer length = 0;
            if (totalLength != null) {
                length = (Integer) totalLength;
            }
            APIMappingUtil.setPaginationParams(apiProductListDTO, query, offset, limit, length);

            return Response.ok().entity(apiProductListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving API Products ";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Import an API Product by uploading an archive file. All relevant API Product data will be included upon the creation of
     * the API Product. Depending on the choice of the user, provider of the imported API Product will be preserved or modified.
     *
     * @param fileInputStream     UploadedInputStream input stream from the REST request
     * @param fileDetail          File details as Attachment
     * @param preserveProvider    User choice to keep or replace the API Product provider
     * @param rotateRevision      If the maximum revision number reached, undeploy the earliest revision and create a
     *                            new revision
     * @param importAPIs          Whether to import the dependent APIs or not.
     * @param overwriteAPIProduct Whether to update the API Product or not. This is used when updating already existing API Products.
     * @param overwriteAPIs       Whether to update the dependent APIs or not. This is used when updating already existing dependent APIs of an API Product.
     * @param messageContext      Message Context
     * @return API Product import response
     * @throws APIManagementException
     */
    @Override public Response importAPIProduct(InputStream fileInputStream, Attachment fileDetail,
            Boolean preserveProvider, Boolean rotateRevision, Boolean importAPIs, Boolean overwriteAPIProduct,
            Boolean overwriteAPIs, MessageContext messageContext) throws APIManagementException {
        // If importAPIs flag is not set, the default value is false
        importAPIs = importAPIs == null ? false : importAPIs;

        // Check if the URL parameter value is specified, otherwise the default value is true.
        preserveProvider = preserveProvider == null || preserveProvider;

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        String[] tokenScopes = (String[]) PhaseInterceptorChain.getCurrentMessage().getExchange()
                .get(RestApiConstants.USER_REST_API_SCOPES);
        ImportExportAPI importExportAPI = APIImportExportUtil.getImportExportAPI();

        // Validate if the USER_REST_API_SCOPES is not set in AbstractOAuthAuthenticator when scopes are validated
        // If the user need to import dependent APIs and the user has the required scope for that, allow the user to do it
        if (tokenScopes == null) {
            RestApiUtil.handleInternalServerError("Error occurred while importing the API Product", log);
            return null;
        } else {
            Boolean isRequiredScopesAvailable = Arrays.asList(tokenScopes)
                    .contains(RestApiConstants.API_IMPORT_EXPORT_SCOPE);
            if (!isRequiredScopesAvailable) {
                log.info("Since the user does not have required scope: " + RestApiConstants.API_IMPORT_EXPORT_SCOPE
                        + ", importAPIs will be set to false");
            }
            importAPIs = importAPIs && isRequiredScopesAvailable;
        }

        // Check whether to update the API Product. If not specified, default value is false.
        overwriteAPIProduct = overwriteAPIProduct == null ? false : overwriteAPIProduct;

        // Check whether to update the dependent APIs. If not specified, default value is false.
        overwriteAPIs = overwriteAPIs == null ? false : overwriteAPIs;

        // Check if the URL parameter value is specified, otherwise the default value is true.
        preserveProvider = preserveProvider == null || preserveProvider;
        importExportAPI.importAPIProduct(fileInputStream, preserveProvider, rotateRevision, overwriteAPIProduct,
                overwriteAPIs, importAPIs, tokenScopes, organization);
        return Response.status(Response.Status.OK).entity("API Product imported successfully.").build();
    }

    @Override public Response createAPIProduct(APIProductDTO body, MessageContext messageContext) throws
            APIManagementException {
        String provider = body.getProvider();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        try {
            APIProduct createdProduct = PublisherCommonUtils.addAPIProductWithGeneratedSwaggerDefinition(body,
                    RestApiCommonUtil.getLoggedInUsername(), organization);
            APIProductDTO createdApiProductDTO = APIMappingUtil.fromAPIProducttoDTO(createdProduct);
            URI createdApiProductUri = new URI(
                    RestApiConstants.RESOURCE_PATH_API_PRODUCTS + "/" + createdApiProductDTO.getId());
            return Response.created(createdApiProductUri).entity(createdApiProductDTO).build();

        } catch (APIManagementException | FaultGatewaysException e) {
            String errorMessage = "Error while adding new API Product : " + provider + "-" + body.getName()
                    + " - " + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving API Product location : " + provider + "-"
                    + body.getName();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * To check whether a particular exception is due to access control restriction.
     *
     * @param e Exception object.
     * @return true if the the exception is caused due to authorization failure.
     */
    private boolean isAuthorizationFailure(Exception e) {
        String errorMessage = e.getMessage();
        return errorMessage != null && errorMessage.contains(UN_AUTHORIZED_ERROR_MESSAGE);
    }

    @Override
    public Response createAPIProductRevision(String apiProductId, APIRevisionDTO apIRevisionDTO,
                                             MessageContext messageContext) throws APIManagementException {
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            APIRevision apiRevision = new APIRevision();
            apiRevision.setApiUUID(apiProductId);
            apiRevision.setDescription(apIRevisionDTO.getDescription());
            //adding the api revision
            String revisionId = apiProvider.addAPIProductRevision(apiRevision, organization);

            //Retrieve the newly added APIRevision to send in the response payload
            APIRevision createdApiRevision = apiProvider.getAPIRevision(revisionId);
            APIRevisionDTO createdApiRevisionDTO = APIMappingUtil.fromAPIRevisiontoDTO(createdApiRevision);
            //This URI used to set the location header of the POST response
            URI createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_API_PRODUCTS
                    + "/" + createdApiRevisionDTO.getApiInfo().getId() + "/"
                    + RestApiConstants.RESOURCE_PATH_REVISIONS + "/" + createdApiRevisionDTO.getId());
            return Response.created(createdApiUri).entity(createdApiRevisionDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while adding new API Revision for API Product: " + apiProductId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving created revision API location for API Product: "
                    + apiProductId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response deleteAPIProductRevision(String apiProductId, String revisionId,
                                             MessageContext messageContext) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        apiProvider.deleteAPIProductRevision(apiProductId, revisionId, organization);
        List<APIRevision> apiRevisions = apiProvider.getAPIRevisions(apiProductId);
        APIRevisionListDTO apiRevisionListDTO = APIMappingUtil.fromListAPIRevisiontoDTO(apiRevisions);
        return Response.ok().entity(apiRevisionListDTO).build();
    }

    @Override
    public Response deployAPIProductRevision(String apiProductId, String revisionId,
                                             List<APIRevisionDeploymentDTO> apIRevisionDeploymentDTO,
                                             MessageContext messageContext) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        Map<String, Environment> environments = APIUtil.getEnvironments(organization);
        List<APIRevisionDeployment> apiRevisionDeployments = new ArrayList<>();
        for (APIRevisionDeploymentDTO apiRevisionDeploymentDTO : apIRevisionDeploymentDTO) {
            APIRevisionDeployment apiRevisionDeployment = new APIRevisionDeployment();
            apiRevisionDeployment.setRevisionUUID(revisionId);
            String environment = apiRevisionDeploymentDTO.getName();
            if (environments.get(environment) == null) {
                RestApiUtil.handleBadRequest("Gateway environment not found: " + environment, log);
            }
            apiRevisionDeployment.setDeployment(environment);
            apiRevisionDeployment.setVhost(apiRevisionDeploymentDTO.getVhost());
            if (StringUtils.isEmpty(apiRevisionDeploymentDTO.getVhost())) {
                // vhost is only required when deploying an revision, not required when un-deploying a revision
                // since the same scheme 'APIRevisionDeployment' is used for deploy and undeploy, handle it here.
                RestApiUtil.handleBadRequest(
                        "Required field 'vhost' not found in deployment", log
                );
            }
            apiRevisionDeployment.setDisplayOnDevportal(apiRevisionDeploymentDTO.isDisplayOnDevportal());
            apiRevisionDeployments.add(apiRevisionDeployment);
        }
        apiProvider.deployAPIProductRevision(apiProductId, revisionId, apiRevisionDeployments);
        List<APIRevisionDeployment> apiRevisionDeploymentsResponse = apiProvider.getAPIRevisionDeploymentList(revisionId);
        List<APIRevisionDeploymentDTO> apiRevisionDeploymentDTOS = new ArrayList<>();
        for (APIRevisionDeployment apiRevisionDeployment : apiRevisionDeploymentsResponse) {
            apiRevisionDeploymentDTOS.add(APIMappingUtil.fromAPIRevisionDeploymenttoDTO(apiRevisionDeployment));
        }
        Response.Status status = Response.Status.CREATED;
        return Response.status(status).entity(apiRevisionDeploymentDTOS).build();
    }

    @Override
    public Response getAPIProductRevision(String apiProductId, String revisionId,
                                          MessageContext messageContext) throws APIManagementException {
        // remove errorObject and add implementation code!
        org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorObject = new ErrorDTO();
        Response.Status status = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }

    @Override
    public Response getAPIProductRevisionDeployments(String apiProductId,
                                                     MessageContext messageContext) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        List<APIRevisionDeployment> apiRevisionDeploymentsList = new ArrayList<>();
        List<APIRevision> apiRevisions = apiProvider.getAPIRevisions(apiProductId);
        for (APIRevision apiRevision : apiRevisions) {
            List<APIRevisionDeployment> apiRevisionDeploymentsResponse =
                    apiProvider.getAPIRevisionDeploymentList(apiRevision.getRevisionUUID());
            for (APIRevisionDeployment apiRevisionDeployment : apiRevisionDeploymentsResponse) {
                apiRevisionDeploymentsList.add(apiRevisionDeployment);
            }
        }
        List<APIRevisionDeploymentDTO> apiRevisionDeploymentDTOS = new ArrayList<>();
        for (APIRevisionDeployment apiRevisionDeployment : apiRevisionDeploymentsList) {
            apiRevisionDeploymentDTOS.add(APIMappingUtil.fromAPIRevisionDeploymenttoDTO(apiRevisionDeployment));
        }
        return Response.ok().entity(apiRevisionDeploymentDTOS).build();
    }

    @Override
    public Response getAPIProductRevisions(String apiProductId, String query,
                                           MessageContext messageContext) throws APIManagementException {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            APIRevisionListDTO apiRevisionListDTO;
            List<APIRevision> apiRevisions = apiProvider.getAPIRevisions(apiProductId);
            apiRevisionListDTO = APIMappingUtil.fromListAPIRevisiontoDTO(
                    ApiProductsApiServiceImplUtils.getAPIRevisionListDTO(query, apiRevisions));
            return Response.ok().entity(apiRevisionListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while adding retrieving API Revision for API Product id : " + apiProductId
                    + " - " + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response restoreAPIProductRevision(String apiProductId, String revisionId,
                                              MessageContext messageContext) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        apiProvider.restoreAPIProductRevision(apiProductId, revisionId, organization);
        APIProductDTO apiToReturn = getAPIProductByID(apiProductId, apiProvider);
        Response.Status status = Response.Status.CREATED;
        return Response.status(status).entity(apiToReturn).build();
    }

    @Override
    public Response undeployAPIProductRevision(String apiProductId, String revisionId, String revisionNumber,
                                               Boolean allEnvironments,
                                               List<APIRevisionDeploymentDTO> apIRevisionDeploymentDTO,
                                               MessageContext messageContext) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        if (revisionId == null && revisionNumber != null) {
            revisionId = apiProvider.getAPIRevisionUUID(revisionNumber, apiProductId);
            if (revisionId == null) {
                return Response.status(Response.Status.BAD_REQUEST).entity(null).build();
            }
        }
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        Map<String, Environment> environments = APIUtil.getEnvironments(organization);
        List<APIRevisionDeployment> apiRevisionDeployments = new ArrayList<>();
        if (allEnvironments) {
            apiRevisionDeployments = apiProvider.getAPIRevisionDeploymentList(revisionId);
        } else {
            for (APIRevisionDeploymentDTO apiRevisionDeploymentDTO : apIRevisionDeploymentDTO) {
                APIRevisionDeployment apiRevisionDeployment = new APIRevisionDeployment();
                apiRevisionDeployment.setRevisionUUID(revisionId);
                String environment = apiRevisionDeploymentDTO.getName();
                if (environments.get(environment) == null) {
                    RestApiUtil.handleBadRequest("Gateway environment not found: " + environment, log);
                }
                apiRevisionDeployment.setDeployment(environment);
                apiRevisionDeployment.setVhost(apiRevisionDeploymentDTO.getVhost());
                apiRevisionDeployment.setDisplayOnDevportal(apiRevisionDeploymentDTO.isDisplayOnDevportal());
                apiRevisionDeployments.add(apiRevisionDeployment);
            }
        }
        apiProvider.undeployAPIProductRevisionDeployment(apiProductId, revisionId, apiRevisionDeployments);
        List<APIRevisionDeployment> apiRevisionDeploymentsResponse = apiProvider.getAPIRevisionDeploymentList(revisionId);
        List<APIRevisionDeploymentDTO> apiRevisionDeploymentDTOS = new ArrayList<>();
        for (APIRevisionDeployment apiRevisionDeployment : apiRevisionDeploymentsResponse) {
            apiRevisionDeploymentDTOS.add(APIMappingUtil.fromAPIRevisionDeploymenttoDTO(apiRevisionDeployment));
        }
        Response.Status status = Response.Status.CREATED;
        return Response.status(status).entity(apiRevisionDeploymentDTOS).build();
    }

    private APIProductDTO getAPIProductByID(String apiProductId, APIProvider apiProvider) {
        try {
            String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
            APIProduct api = apiProvider.getAPIProductbyUUID(apiProductId, tenantDomain);
            return APIMappingUtil.fromAPIProducttoDTO(api);
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need
            // to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API_PRODUCT, apiProductId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("User is not authorized to access the API Product",
                        e, log);
            } else {
                String errorMessage = "Error while retrieving API Product : " + apiProductId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    @Override
    public Response updateAPIProductDeployment(String apiProductId, String deploymentId, APIRevisionDeploymentDTO
            apIRevisionDeploymentDTO, MessageContext messageContext) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String revisionId = apIRevisionDeploymentDTO.getRevisionUuid();
        String decodedDeploymentName;
        if (deploymentId != null) {
            try {
                decodedDeploymentName = new String(Base64.getUrlDecoder().decode(deploymentId),
                        StandardCharsets.UTF_8);
            } catch (IllegalArgumentException e) {
                throw new APIMgtResourceNotFoundException("deployment with " + deploymentId +
                        " not found", ExceptionCodes.from(ExceptionCodes.EXISTING_DEPLOYMENT_NOT_FOUND,
                        deploymentId));
            }
        } else {
            throw new APIMgtResourceNotFoundException("deployment id not found",
                    ExceptionCodes.from(ExceptionCodes.DEPLOYMENT_ID_NOT_FOUND));
        }
        APIRevisionDeployment apiRevisionDeployment = new APIRevisionDeployment();
        apiRevisionDeployment.setRevisionUUID(revisionId);
        apiRevisionDeployment.setDeployment(decodedDeploymentName);
        apiRevisionDeployment.setVhost(apIRevisionDeploymentDTO.getVhost());
        apiRevisionDeployment.setDisplayOnDevportal(apIRevisionDeploymentDTO.isDisplayOnDevportal());
        apiProvider.updateAPIProductDisplayOnDevportal(apiProductId, revisionId, apiRevisionDeployment);
        APIRevisionDeployment apiRevisionDeploymentsResponse = apiProvider.
                getAPIRevisionDeployment(decodedDeploymentName, revisionId);
        APIRevisionDeploymentDTO apiRevisionDeploymentDTO = APIMappingUtil.
                fromAPIRevisionDeploymenttoDTO(apiRevisionDeploymentsResponse);
        Response.Status status = Response.Status.OK;
        return Response.status(status).entity(apiRevisionDeploymentDTO).build();
    }

    @Override
    public Response changeAPIProductLifecycle(String action, String apiProductId, String lifecycleChecklist,
                                              String ifMatch, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        ApiTypeWrapper productWrapper = new ApiTypeWrapper(apiProvider.getAPIProductbyUUID(apiProductId, organization));
        APIStateChangeResponse stateChangeResponse = PublisherCommonUtils.changeApiOrApiProductLifecycle(action,
                productWrapper, lifecycleChecklist, organization);

        LifecycleStateDTO stateDTO = getLifecycleState(apiProductId, organization);
        WorkflowResponseDTO workflowResponseDTO = APIMappingUtil.toWorkflowResponseDTO(stateDTO, stateChangeResponse);
        return Response.ok().entity(workflowResponseDTO).build();
    }

    @Override
    public Response getAPIProductLifecycleHistory(String apiProductId, String ifNoneMatch,
                                                  MessageContext messageContext) {
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            APIProduct product;
            APIRevision apiRevision = ApiMgtDAO.getInstance().checkAPIUUIDIsARevisionUUID(apiProductId);
            if (apiRevision != null && apiRevision.getApiUUID() != null) {
                product = apiProvider.getAPIProductbyUUID(apiRevision.getApiUUID(), organization);
            } else {
                product = apiProvider.getAPIProductbyUUID(apiProductId, organization);
            }
            return Response.ok().entity(PublisherCommonUtils.getLifecycleHistoryDTO(product.getUuid(), apiProvider))
                    .build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API_PRODUCT, apiProductId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("Authorization failure while retrieving the " +
                        "lifecycle history of the API Product with id : " + apiProductId, e, log);
            } else {
                String errorMessage = "Error while retrieving the lifecycle history of  API Product with id : "
                        + apiProductId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    @Override
    public Response getAPIProductLifecycleState(String apiProductId, String ifNoneMatch,
                                                MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        LifecycleStateDTO lifecycleStateDTO = getLifecycleState(apiProductId, organization);
        return Response.ok().entity(lifecycleStateDTO).build();
    }

    private LifecycleStateDTO getLifecycleState(String apiProductId, String organization) {

        try {
            APIProductIdentifier productIdentifier = APIMappingUtil.getAPIProductIdentifierFromUUID(apiProductId,
                    organization);
            return PublisherCommonUtils.getLifecycleStateInformation(productIdentifier, organization);
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API_PRODUCT, apiProductId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("Authorization failure while retrieving the lifecycle " +
                        "state information of API Product with id : " + apiProductId, e, log);
            } else {
                String errorMessage = "Error while retrieving the lifecycle state information of the API Product with" +
                        " " + "id : " + apiProductId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    @Override
    public Response deleteAPIProductLifecycleStatePendingTasks(String apiProductId, MessageContext messageContext)
            throws APIManagementException {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            APIProductIdentifier productIdentifier = APIUtil.getAPIProductIdentifierFromUUID(apiProductId);
            if (productIdentifier == null) {
                throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API Product with UUID: "
                        + apiProductId, ExceptionCodes.from(ExceptionCodes.API_PRODUCT_NOT_FOUND, apiProductId));
            }
            apiProvider.deleteWorkflowTask(productIdentifier);
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while deleting task ";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
}
