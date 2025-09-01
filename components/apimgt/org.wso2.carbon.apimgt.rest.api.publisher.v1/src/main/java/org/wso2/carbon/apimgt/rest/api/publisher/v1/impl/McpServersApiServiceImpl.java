/*
 * Copyright (c) 2025, WSO2 LLC (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.APIConstants.UnifiedSearchConstants;
import org.wso2.carbon.apimgt.api.APIComplianceException;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIMgtResourceAlreadyExistsException;
import org.wso2.carbon.apimgt.api.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.FaultGatewaysException;
import org.wso2.carbon.apimgt.api.dto.APIEndpointValidationDTO;
import org.wso2.carbon.apimgt.api.dto.ImportedAPIDTO;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIInfo;
import org.wso2.carbon.apimgt.api.model.APIRevision;
import org.wso2.carbon.apimgt.api.model.APIRevisionDeployment;
import org.wso2.carbon.apimgt.api.model.APIStateChangeResponse;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Backend;
import org.wso2.carbon.apimgt.api.model.Comment;
import org.wso2.carbon.apimgt.api.model.CommentList;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DocumentationContent;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.OrganizationInfo;
import org.wso2.carbon.apimgt.api.model.ServiceEntry;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.APIDTOTypeWrapper;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.GZIPUtils;
import org.wso2.carbon.apimgt.impl.ServiceCatalogImpl;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.RuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.RuntimeArtifactGeneratorUtil;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.ExportFormat;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportAPI;
import org.wso2.carbon.apimgt.impl.importexport.utils.APIImportExportUtil;
import org.wso2.carbon.apimgt.impl.restapi.CommonUtils;
import org.wso2.carbon.apimgt.impl.restapi.publisher.ApisApiServiceImplUtils;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.CommentMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.DocumentationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.PublisherCommonUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIKeyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionDeploymentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ApiEndpointValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.BackendDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CommentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CommentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CommentRequestDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorListItemDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ImportAPIResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleStateDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MCPServerDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MCPServerProxyRequestDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MCPServerValidationRequestDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MCPServerValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OpenAPIDefinitionValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OrganizationPoliciesDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SecurityInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SubtypeConfigurationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThrottlingPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.WorkflowResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.RestApiPublisherUtils;
import org.wso2.carbon.apimgt.rest.api.util.exception.BadRequestException;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

/**
 * Implementation of the MCP Servers API service.
 * This class provides methods to manage and retrieve information about MCP servers,
 * including listing, retrieving details, handling documents, and managing revisions.
 */
public class McpServersApiServiceImpl implements McpServersApiService {

    private static final Log log = LogFactory.getLog(McpServersApiServiceImpl.class);

    /**
     * Retrieves a paginated list of all MCP servers matching the given search criteria.
     * Handles query adjustments, pagination, and optional gzip compression of the response payload.
     *
     * @param limit          the maximum number of MCP servers to return
     * @param offset         the starting index for pagination
     * @param xWSO2Tenant    the tenant identifier from request header
     * @param query          the search query string to filter MCP servers
     * @param ifNoneMatch    the ETag header value for conditional requests
     * @param accept         the requested content type (e.g. gzip or JSON)
     * @param messageContext the message context of the request
     * @return a {@link Response} containing the list of MCP servers or an error response
     * @throws APIManagementException if an error occurs while retrieving MCP server data
     */
    @Override
    public Response getAllMCPServers(Integer limit, Integer offset, String xWSO2Tenant, String query,
                                     String ifNoneMatch, String accept, MessageContext messageContext)
            throws APIManagementException {

        List<API> allMatchedApis = new ArrayList<>();
        Object apiListDTO;

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        if (query == null || query.isEmpty()) {
            query = UnifiedSearchConstants.QUERY_API_TYPE_MCP;
        } else {
            query = query + " " + UnifiedSearchConstants.QUERY_API_TYPE_MCP;
        }
        try {
            if (query.startsWith(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + ":")) {
                query = query.replace(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + ":",
                        APIConstants.NAME_TYPE_PREFIX + ":");
            }
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            Map<String, Object> result;
            result = apiProvider.searchPaginatedAPIs(query, organization, offset, limit);
            Set<API> apis = (Set<API>) result.get("apis");
            allMatchedApis.addAll(apis);
            apiListDTO = APIMappingUtil.fromAPIListToDTO(allMatchedApis);
            Object totalLength = result.get("length");
            Integer length = 0;
            if (totalLength != null) {
                length = (Integer) totalLength;
            }
            APIMappingUtil.setPaginationParams(apiListDTO, query, offset, limit, length);
            if (APIConstants.APPLICATION_GZIP.equals(accept)) {
                try {
                    File zippedResponse = GZIPUtils.constructZippedResponse(apiListDTO);
                    return Response.ok().entity(zippedResponse)
                            .header("Content-Disposition", "attachment")
                            .header("Content-Encoding", "gzip").build();
                } catch (APIManagementException e) {
                    RestApiUtil.handleInternalServerError(e.getMessage(), e, log);
                }
            } else {
                return Response.ok().entity(apiListDTO).build();
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving MCP servers with query : " + query;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Retrieves a specific comment associated with an MCP server.
     * Supports pagination for replies and can include commenter information.
     *
     * @param commentId            the ID of the comment to retrieve
     * @param mcpServerId          the UUID of the MCP server
     * @param xWSO2Tenant          the tenant identifier from request header
     * @param ifNoneMatch          the ETag header value for conditional requests
     * @param includeCommenterInfo whether to include commenter information in the response
     * @param replyLimit           maximum number of replies to return
     * @param replyOffset          starting index for pagination of replies
     * @param messageContext       the message context of the request
     * @return a {@link Response} containing the comment details or an error response
     * @throws APIManagementException if an error occurs while retrieving the comment
     */
    @Override
    public Response getCommentOfMCPServer(String commentId, String mcpServerId, String xWSO2Tenant, String ifNoneMatch,
                                          Boolean includeCommenterInfo, Integer replyLimit, Integer replyOffset,
                                          MessageContext messageContext) throws APIManagementException {

        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(mcpServerId, requestedTenantDomain);
            Comment comment = apiProvider.getComment(apiTypeWrapper, commentId, replyLimit, replyOffset);

            if (comment != null) {
                CommentDTO commentDTO;
                if (includeCommenterInfo) {
                    Map<String, Map<String, String>> userClaimsMap = CommentMappingUtil
                            .retrieveUserClaims(comment.getUser(), new HashMap<>());
                    commentDTO = CommentMappingUtil.fromCommentToDTOWithUserInfo(comment, userClaimsMap);
                } else {
                    commentDTO = CommentMappingUtil.fromCommentToDTO(comment);
                }
                String uriString = RestApiConstants.RESOURCE_PATH_MCP_SERVERS + "/" + mcpServerId +
                        RestApiConstants.RESOURCE_PATH_COMMENTS + "/" + commentId;
                URI uri = new URI(uriString);
                return Response.ok(uri).entity(commentDTO).build();
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_COMMENTS,
                        String.valueOf(commentId), log);
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId, e, log);
            } else if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId, e, log);
            } else {
                String errorMessage =
                        "Error while retrieving comment for MCP Server : " + mcpServerId + "with comment ID " +
                                commentId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving comment content location : " + mcpServerId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Retrieves the MCP server API details for a given API UUID.
     * Validates that the API is of type MCP and applies organization-specific visibility and policy filters.
     *
     * @param mcpServerId    the UUID of the MCP Server
     * @param xWSO2Tenant    the tenant identifier from request header
     * @param ifNoneMatch    the ETag header value for conditional requests
     * @param messageContext the message context of the request
     * @return a {@link Response} containing the MCP server API details
     * @throws APIManagementException if an error occurs during API retrieval or authorization
     */
    @Override
    public Response getMCPServer(String mcpServerId, String xWSO2Tenant, String ifNoneMatch,
                                 MessageContext messageContext)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        OrganizationInfo organizationInfo = RestApiUtil.getOrganizationInfo(messageContext);
        MCPServerDTO apiToReturn = null;
        try {
            API api = apiProvider.getAPIbyUUID(mcpServerId, organization);
            if (APIConstants.API_TYPE_MCP.equals(api.getType())) {
                api.setOrganization(organization);
                apiToReturn = APIMappingUtil.fromAPItoMCPServerDTO(api, apiProvider);
            } else {
                String errorMessage = "Error while retrieving MCP server : " + mcpServerId + ". Incorrect API type. " +
                        "Expected type: " + APIConstants.API_TYPE_MCP + ", but found: " + api.getType();
                RestApiUtil.handleBadRequest(errorMessage, log);
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("User is not authorized to access the MCP server",
                        e, log);
            } else {
                String errorMessage = "Error while retrieving MCP server : " + mcpServerId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        if (apiToReturn.getVisibleOrganizations() != null && organizationInfo != null
                && organizationInfo.getOrganizationId() != null) {
            List<String> orglist = apiToReturn.getVisibleOrganizations();
            ArrayList<String> newOrgList = new ArrayList<String>(orglist);
            newOrgList.remove(organizationInfo.getOrganizationId());
            newOrgList.remove(organization);
            if (newOrgList.isEmpty()) {
                newOrgList.add(APIConstants.VISIBLE_ORG_NONE);
            }
            apiToReturn.setVisibleOrganizations(newOrgList);
            List<OrganizationPoliciesDTO> organizationPolicies = apiToReturn.getOrganizationPolicies();
            if (organizationPolicies != null) {
                organizationPolicies.removeIf(tier
                        -> tier.getOrganizationID().equals(organizationInfo.getOrganizationId()));
                apiToReturn.setOrganizationPolicies(organizationPolicies);
            }
        } else {
            apiToReturn.setVisibleOrganizations(Collections.singletonList(APIConstants.VISIBLE_ORG_NONE));
        }
        return Response.ok().entity(apiToReturn).build();
    }

    /**
     * Retrieves the OpenAPI definition of a specific MCP server.
     * Validates the API existence and retrieves the updated OpenAPI definition.
     *
     * @param mcpServerId    UUID of the MCP server.
     * @param accept         The requested content type (e.g. JSON or plain text).
     * @param ifNoneMatch    The ETag header value for conditional requests.
     * @param messageContext Message context with request details.
     * @return HTTP Response containing the OpenAPI definition or an error response.
     * @throws APIManagementException if an error occurs while retrieving the OpenAPI definition.
     */
    @Override
    public Response getMCPServerDocument(String mcpServerId, String documentId, String accept, String ifNoneMatch,
                                         MessageContext messageContext) throws APIManagementException {

        Documentation documentation;
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);

            documentation = apiProvider.getDocumentation(mcpServerId, documentId, organization);
            if (documentation == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_DOCUMENTATION, documentId, log);
            }

            DocumentDTO documentDTO = DocumentationMappingUtil.fromDocumentationToDTO(documentation);
            return Response.ok().entity(documentDTO).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("Authorization failure while retrieving document : "
                        + documentId + " of MCP Server " + mcpServerId, e, log);
            } else {
                String errorMessage = "Error while retrieving document : " + documentId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * Retrieves the content of a specific document associated with an MCP server.
     * Handles different content types (file, inline, URL) and returns appropriate responses.
     *
     * @param mcpServerId    UUID of the MCP server.
     * @param documentId     UUID of the document.
     * @param accept         The requested content type (e.g. JSON or plain text).
     * @param ifNoneMatch    The ETag header value for conditional requests.
     * @param messageContext Message context with request details.
     * @return HTTP Response containing the document content or an error response.
     * @throws APIManagementException if an error occurs while retrieving the document content.
     */
    @Override
    public Response getMCPServerDocumentContent(String mcpServerId, String documentId, String accept,
                                                String ifNoneMatch,
                                                MessageContext messageContext) throws APIManagementException {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);

            DocumentationContent docContent =
                    apiProvider.getDocumentationContent(mcpServerId, documentId, organization);
            if (docContent == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_DOCUMENTATION, documentId);
                return null;
            }

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
                        .header(RestApiConstants.HEADER_CONTENT_TYPE, APIConstants.DOCUMENTATION_INLINE_CONTENT_TYPE)
                        .build();
            } else if (docContent.getSourceType().equals(DocumentationContent.ContentSourceType.URL)) {
                String sourceUrl = docContent.getTextContent();
                return Response.seeOther(new URI(sourceUrl)).build();
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while retrieving document : " + documentId + " of MCP Server " +
                                mcpServerId, e, log);
            } else {
                String errorMessage =
                        "Error while retrieving document " + documentId + " of the MCP Server " + mcpServerId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving source URI location of " + documentId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Retrieves all documents associated with a specific MCP server.
     * Supports pagination and returns a list of documentation DTOs.
     *
     * @param mcpServerId    UUID of the MCP server.
     * @param limit          Maximum number of documents to return.
     * @param offset         Starting index for pagination.
     * @param accept         The requested content type (e.g. JSON).
     * @param ifNoneMatch    The ETag header value for conditional requests.
     * @param messageContext Message context with request details.
     * @return HTTP Response containing the list of documents or an error response.
     * @throws APIManagementException if an error occurs while retrieving the documents.
     */
    @Override
    public Response getMCPServerDocuments(String mcpServerId, Integer limit, Integer offset, String accept,
                                          String ifNoneMatch, MessageContext messageContext)
            throws APIManagementException {

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            List<Documentation> allDocumentation = apiProvider.getAllDocumentation(mcpServerId, organization);
            DocumentListDTO documentListDTO = DocumentationMappingUtil.fromDocumentationListToDTO(allDocumentation,
                    offset, limit);
            DocumentationMappingUtil
                    .setPaginationParams(documentListDTO, mcpServerId, offset, limit, allDocumentation.size());
            return Response.ok().entity(documentListDTO).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while retrieving documents of MCP Server : " + mcpServerId, e, log);
            } else {
                String msg = "Error while retrieving documents of MCP Server " + mcpServerId;
                RestApiUtil.handleInternalServerError(msg, e, log);
            }
        }
        return null;
    }

    /**
     * Retrieves a specific MCP server endpoint for the given API.
     *
     * @param mcpServerId    UUID of the API.
     * @param backendApiId   UUID of the backend API.
     * @param messageContext Message context with request details.
     * @return HTTP Response containing the backend endpoint DTO.
     * @throws APIManagementException if an error occurs while retrieving the endpoint.
     */
    @Override
    public Response getMCPServerBackend(String mcpServerId, String backendApiId, MessageContext messageContext)
            throws APIManagementException {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            CommonUtils.validateAPIExistence(mcpServerId);

            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            Backend backend = apiProvider.getMCPServerBackend(mcpServerId, backendApiId, organization);
            BackendDTO backendAPIDTO = APIMappingUtil.fromBackendAPIToDTO(backend, organization, false);
            return Response.ok().entity(backendAPIDTO).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while retrieving resource paths of MCP Server : " + mcpServerId, e, log);
            } else {
                String errorMessage = "Error while retrieving endpoint of MCP Server : " + mcpServerId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * Retrieves all MCP server endpoints for the given API.
     *
     * @param mcpServerId    UUID of the API.
     * @param messageContext Message context with request details.
     * @return HTTP Response containing a list of backend endpoint DTOs.
     * @throws APIManagementException if an error occurs while retrieving the endpoints.
     */
    @Override
    public Response getMCPServerBackends(String mcpServerId, MessageContext messageContext)
            throws APIManagementException {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            CommonUtils.validateAPIExistence(mcpServerId);
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            List<Backend> backendList = apiProvider.getMCPServerBackends(mcpServerId, organization);
            List<BackendDTO> backendAPIDTOList = new ArrayList<>();
            for (Backend endpoint : backendList) {
                try {
                    BackendDTO dto = APIMappingUtil.fromBackendAPIToDTO(endpoint, organization, false);
                    backendAPIDTOList.add(dto);
                } catch (APIManagementException e) {
                    String errorMessage = "Error while mapping backend endpoint to DTO for MCP Server: " + mcpServerId;
                    RestApiUtil.handleInternalServerError(errorMessage, e, log);
                }
            }
            return Response.ok().entity(backendAPIDTOList).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while retrieving endpoints of MCP server: " + mcpServerId, e, log);
            } else {
                String errorMessage = "Error while retrieving endpoints of MCP server: " + mcpServerId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * Retrieves the lifecycle history of a specific MCP server.
     *
     * @param mcpServerId    UUID of the MCP server.
     * @param ifNoneMatch    The ETag header value for conditional requests.
     * @param messageContext Message context with request details.
     * @return HTTP Response containing the lifecycle history DTO.
     * @throws APIManagementException if an error occurs while retrieving the lifecycle history.
     */
    @Override
    public Response getMCPServerLifecycleHistory(String mcpServerId, String ifNoneMatch, MessageContext messageContext)
            throws APIManagementException {

        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            API api;
            APIRevision apiRevision = apiProvider.checkAPIUUIDIsARevisionUUID(mcpServerId);
            if (apiRevision != null && apiRevision.getApiUUID() != null) {
                api = apiProvider.getAPIbyUUID(apiRevision.getApiUUID(), organization);
            } else {
                api = apiProvider.getAPIbyUUID(mcpServerId, organization);
            }
            return Response.ok().entity(PublisherCommonUtils.getLifecycleHistoryDTO(api.getUuid(), apiProvider))
                    .build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("Authorization failure while retrieving the lifecycle "
                        + "events of MCP Server : " + mcpServerId, e, log);
            } else {
                String errorMessage = "Error while retrieving the lifecycle events of MCP Server : " + mcpServerId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * Retrieves the lifecycle state of a specific MCP server.
     *
     * @param mcpServerId    UUID of the MCP server.
     * @param ifNoneMatch    The ETag header value for conditional requests.
     * @param messageContext Message context with request details.
     * @return HTTP Response containing the lifecycle state DTO.
     * @throws APIManagementException if an error occurs while retrieving the lifecycle state.
     */
    @Override
    public Response getMCPServerLifecycleState(String mcpServerId, String ifNoneMatch, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        LifecycleStateDTO lifecycleStateDTO = getLifecycleState(mcpServerId, organization);
        return Response.ok().entity(lifecycleStateDTO).build();
    }

    /**
     * Deletes a specific MCP server revision.
     *
     * @param mcpServerId    UUID of the API.
     * @param revisionId     UUID of the revision to be deleted.
     * @param messageContext Message context with request details.
     * @return HTTP Response indicating the result of the deletion operation.
     * @throws APIManagementException if an error occurs while deleting the revision.
     */
    @Override
    public Response getMCPServerRevision(String mcpServerId, String revisionId, MessageContext messageContext)
            throws APIManagementException {

        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }

    /**
     * Retrieves the list of deployments for a specific MCP server revision.
     *
     * @param mcpServerId    UUID of the MCP server.
     * @param messageContext Message context with request details.
     * @return HTTP Response containing a list of APIRevisionDeploymentDTO objects.
     * @throws APIManagementException if an error occurs while retrieving the deployments.
     */
    @Override
    public Response getMCPServerRevisionDeployments(String mcpServerId, MessageContext messageContext)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        List<APIRevisionDeployment> apiRevisionDeploymentsList = apiProvider.getAPIRevisionsDeploymentList(mcpServerId);

        List<APIRevisionDeploymentDTO> apiRevisionDeploymentDTOS = new ArrayList<>();
        for (APIRevisionDeployment apiRevisionDeployment : apiRevisionDeploymentsList) {
            apiRevisionDeploymentDTOS.add(APIMappingUtil.fromAPIRevisionDeploymenttoDTO(apiRevisionDeployment));
        }
        return Response.ok().entity(apiRevisionDeploymentDTOS).build();
    }

    /**
     * Retrieves all revisions of a specific MCP server.
     *
     * @param mcpServerId    UUID of the API.
     * @param query          Query string to filter revisions.
     * @param messageContext Message context with request details.
     * @return HTTP Response containing a list of APIRevisionListDTO objects.
     * @throws APIManagementException if an error occurs while retrieving the revisions.
     */
    @Override
    public Response getMCPServerRevisions(String mcpServerId, String query, MessageContext messageContext)
            throws APIManagementException {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            APIRevisionListDTO apiRevisionListDTO;
            List<APIRevision> apiRevisions = apiProvider.getAPIRevisions(mcpServerId);
            List<APIRevision> apiRevisionsList = ApisApiServiceImplUtils.filterAPIRevisionsByDeploymentStatus(query,
                    apiRevisions);
            apiRevisionListDTO = APIMappingUtil.fromListAPIRevisiontoDTO(apiRevisionsList);
            return Response.ok().entity(apiRevisionListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage =
                    "Error while retrieving API Revision for MCP server: " + mcpServerId + " - " + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Retrieves the subscription policies for a specific MCP server.
     * Validates the API existence and retrieves the available throttling policies.
     *
     * @param mcpServerId    UUID of the MCP server.
     * @param xWSO2Tenant    The tenant identifier from request header.
     * @param ifNoneMatch    The ETag header value for conditional requests.
     * @param isAiApi        Indicates if the API is an AI API.
     * @param organizationID The organization ID for which to retrieve policies.
     * @param messageContext Message context of the request.
     * @return Response containing the list of available throttling policies for the MCP server.
     * @throws APIManagementException if an error occurs while retrieving subscription policies.
     */
    @Override
    public Response getMCPServerSubscriptionPolicies(String mcpServerId, String xWSO2Tenant, String ifNoneMatch,
                                                     Boolean isAiApi, String organizationID,
                                                     MessageContext messageContext) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        API api = apiProvider.getAPIbyUUID(mcpServerId, organization);
        api.setOrganization(organization);
        MCPServerDTO apiInfo = APIMappingUtil.fromAPItoMCPServerDTO(api, apiProvider);
        List<Tier> availableThrottlingPolicyList = new ThrottlingPoliciesApiServiceImpl().getThrottlingPolicyList(
                ThrottlingPolicyDTO.PolicyLevelEnum.SUBSCRIPTION.toString(), true, isAiApi);

        List<String> apiPolicies =
                RestApiPublisherUtils.getSubscriptionPoliciesForOrganization(new APIDTOTypeWrapper(apiInfo),
                        organizationID);
        List<Tier> apiThrottlingPolicies = ApisApiServiceImplUtils.filterAPIThrottlingPolicies(apiPolicies,
                availableThrottlingPolicyList);
        return Response.ok().entity(apiThrottlingPolicies).build();
    }

    /**
     * Retrieves the replies of a specific comment associated with an MCP server.
     * Supports pagination and can include commenter information.
     *
     * @param commentId            the ID of the comment to retrieve replies for
     * @param mcpServerId          the UUID of the MCP server
     * @param xWSO2Tenant          the tenant identifier from request header
     * @param limit                maximum number of replies to return
     * @param offset               starting index for pagination of replies
     * @param ifNoneMatch          the ETag header value for conditional requests
     * @param includeCommenterInfo whether to include commenter information in the response
     * @param messageContext       the message context of the request
     * @return a {@link Response} containing the list of replies or an error response
     * @throws APIManagementException if an error occurs while retrieving the replies
     */
    @Override
    public Response getRepliesOfCommentOfMCPServer(String commentId, String mcpServerId, String xWSO2Tenant,
                                                   Integer limit, Integer offset, String ifNoneMatch,
                                                   Boolean includeCommenterInfo, MessageContext messageContext)
            throws APIManagementException {

        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(mcpServerId, requestedTenantDomain);
            CommentList comments = apiProvider.getComments(apiTypeWrapper, commentId, limit, offset);
            CommentListDTO commentDTO = CommentMappingUtil.fromCommentListToDTO(comments, includeCommenterInfo);

            String uriString = RestApiConstants.RESOURCE_PATH_MCP_SERVERS + "/" + mcpServerId +
                    RestApiConstants.RESOURCE_PATH_COMMENTS;
            URI uri = new URI(uriString);
            return Response.ok(uri).entity(commentDTO).build();

        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId, e, log);
            } else {
                RestApiUtil.handleInternalServerError("Failed to get comments of MCP Server " + mcpServerId, e, log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving comments content location for MCP Server " + mcpServerId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Imports an MCP server from a file input stream.
     * Validates the input parameters and performs the import operation.
     *
     * @param fileInputStream              InputStream of the file to be imported
     * @param fileDetail                   Attachment details of the file
     * @param preserveProvider             Whether to preserve the provider information
     * @param rotateRevision               Whether to rotate the revision
     * @param overwrite                    Whether to overwrite existing API
     * @param preservePortalConfigurations Whether to preserve portal configurations
     * @param dryRun                       Whether to perform a dry run without actual import
     * @param accept                       The requested content type (e.g. JSON or plain text)
     * @param messageContext               Message context of the request
     * @return Response containing the result of the import operation
     * @throws APIManagementException if an error occurs during import or validation
     */
    @Override
    public Response importMCPServer(InputStream fileInputStream, Attachment fileDetail,
                                    Boolean preserveProvider,
                                    Boolean rotateRevision, Boolean overwrite, Boolean preservePortalConfigurations,
                                    Boolean dryRun, String accept, MessageContext messageContext)
            throws APIManagementException {

        overwrite = overwrite != null && overwrite;

        // Check if the URL parameter value is specified, otherwise the default value is true.
        preserveProvider = preserveProvider == null || preserveProvider;
        if (preservePortalConfigurations == null) {
            preservePortalConfigurations = false;
        }
        accept = accept != null ? accept : RestApiConstants.TEXT_PLAIN;
        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        String[] tokenScopes = (String[]) PhaseInterceptorChain.getCurrentMessage().getExchange()
                .get(RestApiConstants.USER_REST_API_SCOPES);
        ImportExportAPI importExportAPI = APIImportExportUtil.getImportExportAPI();
        ImportedAPIDTO
                importedAPIDTO = importExportAPI.importMCPServer(fileInputStream, preserveProvider, rotateRevision,
                overwrite, preservePortalConfigurations, tokenScopes, organization);
        if (RestApiConstants.APPLICATION_JSON.equals(accept) && importedAPIDTO != null) {
            ImportAPIResponseDTO responseDTO = new ImportAPIResponseDTO().id(importedAPIDTO.getApi().getUuid())
                    .revision(importedAPIDTO.getRevision());
            return Response.ok().entity(responseDTO).build();
        }
        return Response.status(Response.Status.OK).entity("MCP Server imported successfully.").build();
    }

    /**
     * Create a new MCP server using the backend API's OpenAPI definition.
     * Validates the provided additional properties and endpoint configurations.
     *
     * @param fileInputStream      InputStream of the OpenAPI file to be imported
     * @param fileDetail           Attachment details of the OpenAPI file
     * @param url                  URL of the OpenAPI definition
     * @param additionalProperties JSON string containing additional properties for the API
     * @param messageContext       Message context of the request
     * @return Response containing the created MCPServerDTO or an error response
     * @throws APIManagementException if an error occurs during import or validation
     */
    public Response createMCPServerFromOpenAPI(InputStream fileInputStream, Attachment fileDetail, String url,
                                               String additionalProperties, MessageContext messageContext)
            throws APIManagementException {

        if (StringUtils.isBlank(additionalProperties)) {
            throw new APIManagementException("'additionalProperties' is required and should not be null",
                    ExceptionCodes.ADDITIONAL_PROPERTIES_CANNOT_BE_NULL);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        MCPServerDTO apiDTOFromProperties;
        try {
            apiDTOFromProperties = objectMapper.readValue(additionalProperties, MCPServerDTO.class);
            APIUtil.validateCharacterLengthOfAPIParams(apiDTOFromProperties.getName(),
                    apiDTOFromProperties.getVersion(), apiDTOFromProperties.getContext(),
                    RestApiCommonUtil.getLoggedInUsername());
            try {
                APIUtil.validateAPIContext(apiDTOFromProperties.getContext(), apiDTOFromProperties.getName());
            } catch (APIManagementException e) {
                throw new APIManagementException(e.getMessage(),
                        ExceptionCodes.from(ExceptionCodes.API_CONTEXT_MALFORMED_EXCEPTION, e.getMessage()));
            }
        } catch (IOException e) {
            throw new APIManagementException("Error while parsing 'additionalProperties'", e,
                    ExceptionCodes.ADDITIONAL_PROPERTIES_PARSE_ERROR);
        }
        populateDefaultValuesForMCPServer(apiDTOFromProperties, APIConstants.API_SUBTYPE_DIRECT_BACKEND);

        APIDTOTypeWrapper dtoWrapper = new APIDTOTypeWrapper(apiDTOFromProperties);
        if (!PublisherCommonUtils.validateEndpoints(dtoWrapper)) {
            throw new APIManagementException("Invalid/Malformed endpoint URL(s) detected",
                    ExceptionCodes.INVALID_ENDPOINT_URL);
        }
        try {
            Map endpointConfig = (LinkedHashMap) dtoWrapper.getEndpointConfig();
            PublisherCommonUtils
                    .encryptEndpointSecurityOAuthCredentials(endpointConfig, CryptoUtil.getDefaultCryptoUtil(),
                            StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
                            dtoWrapper);
            PublisherCommonUtils
                    .encryptEndpointSecurityApiKeyCredentials(endpointConfig, CryptoUtil.getDefaultCryptoUtil(),
                            StringUtils.EMPTY, StringUtils.EMPTY, dtoWrapper);

            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            MCPServerDTO createdApiDTO = RestApiPublisherUtils.importDefinitionForMCPServers(fileInputStream,
                    url, null, dtoWrapper, fileDetail, null, organization, null);
            URI createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_MCP_SERVERS + "/" + createdApiDTO.getId());
            return Response.created(createdApiUri).entity(createdApiDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage =
                    "Error while retrieving MCP server location: " + dtoWrapper.getProvider() + "-" +
                            dtoWrapper.getName() + "-" + dtoWrapper.getVersion();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (CryptoException e) {
            String errorMessage =
                    "Error while encrypting the secret key of MCP server: " + dtoWrapper.getProvider() + "-"
                            + dtoWrapper.getName() + "-" + dtoWrapper.getVersion();
            throw new APIManagementException(errorMessage, e,
                    ExceptionCodes.from(ExceptionCodes.ENDPOINT_SECURITY_CRYPTO_EXCEPTION, errorMessage));
        } catch (ParseException e) {
            String errorMessage = "Error while parsing the endpoint configuration of MCP server: "
                    + dtoWrapper.getProvider() + "-" + dtoWrapper.getName() + "-"
                    + dtoWrapper.getVersion();
            throw new APIManagementException(errorMessage, e);
        }
        return null;
    }

    /**
     * Creates a new MCP server proxy using the provided request DTO.
     * Validates the request and extracts necessary parameters for the MCP server.
     *
     * @param mcPServerProxyRequest The request DTO containing MCP server details.
     * @param messageContext        Message context of the request.
     * @return Response containing the created MCPServerDTO or an error response.
     * @throws APIManagementException if an error occurs during creation or validation.
     */
    public Response createMCPServerProxy(MCPServerProxyRequestDTO mcPServerProxyRequest, MessageContext messageContext)
            throws APIManagementException {

        if (mcPServerProxyRequest == null) {
            throw new APIManagementException("Request body cannot be null",
                    ExceptionCodes.MCP_REQUEST_BODY_CANNOT_BE_NULL);
        }
        String url = StringUtils.trimToEmpty(mcPServerProxyRequest.getUrl());
        MCPServerDTO mcpServerDTO = mcPServerProxyRequest.getAdditionalProperties();
        SecurityInfoDTO securityInfoDTO = mcPServerProxyRequest.getSecurityInfo();

        populateDefaultValuesForMCPServer(mcpServerDTO, APIConstants.API_SUBTYPE_SERVER_PROXY);

        APIDTOTypeWrapper dtoWrapper = new APIDTOTypeWrapper(mcpServerDTO);
        if (!PublisherCommonUtils.validateEndpoints(dtoWrapper)) {
            throw new APIManagementException("Invalid/Malformed endpoint URL(s) detected",
                    ExceptionCodes.INVALID_ENDPOINT_URL);
        }
        try {
            Map endpointConfig = (LinkedHashMap) dtoWrapper.getEndpointConfig();
            PublisherCommonUtils
                    .encryptEndpointSecurityOAuthCredentials(endpointConfig, CryptoUtil.getDefaultCryptoUtil(),
                            StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
                            dtoWrapper);
            PublisherCommonUtils
                    .encryptEndpointSecurityApiKeyCredentials(endpointConfig, CryptoUtil.getDefaultCryptoUtil(),
                            StringUtils.EMPTY, StringUtils.EMPTY, dtoWrapper);

            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            MCPServerDTO createdApiDTO = RestApiPublisherUtils.importDefinitionForMCPServers(null, url,
                    null, dtoWrapper, null, null, organization, securityInfoDTO);
            URI createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_MCP_SERVERS + "/" + createdApiDTO.getId());
            return Response.created(createdApiUri).entity(createdApiDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage =
                    "Error while retrieving MCP server location: " + dtoWrapper.getProvider() + "-" +
                            dtoWrapper.getName() + "-" + dtoWrapper.getVersion();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (CryptoException e) {
            String errorMessage =
                    "Error while encrypting the secret key of MCP server: " + dtoWrapper.getProvider() + "-"
                            + dtoWrapper.getName() + "-" + dtoWrapper.getVersion();
            throw new APIManagementException(errorMessage, e,
                    ExceptionCodes.from(ExceptionCodes.ENDPOINT_SECURITY_CRYPTO_EXCEPTION, errorMessage));
        } catch (ParseException e) {
            String errorMessage = "Error while parsing the endpoint configuration of MCP server: "
                    + dtoWrapper.getProvider() + "-" + dtoWrapper.getName() + "-"
                    + dtoWrapper.getVersion();
            throw new APIManagementException(errorMessage, e);
        }
        return null;
    }

    /**
     * Populates default values for the MCP server API DTO.
     *
     * @param apiDTOFromProperties The MCPServerDTO to populate.
     * @param subtype              The subtype of the MCP server.
     */
    private void populateDefaultValuesForMCPServer(MCPServerDTO apiDTOFromProperties, String subtype) {

        SubtypeConfigurationDTO subtypeConfiguration = new SubtypeConfigurationDTO();
        subtypeConfiguration.setSubtype(subtype);
        apiDTOFromProperties.setSubtypeConfiguration(subtypeConfiguration);
    }

    /**
     * Restores an MCP server API revision to the specified API.
     *
     * @param mcpServerId    UUID of the API to which the revision should be restored.
     * @param revisionId     UUID of the revision to restore.
     * @param messageContext Message context with request details.
     * @return HTTP Response containing the restored API DTO.
     * @throws APIManagementException if an error occurs while restoring the revision.
     */
    @Override
    public Response restoreMCPServerRevision(String mcpServerId, String revisionId, MessageContext messageContext)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APIInfo apiInfo = CommonUtils.validateAPIExistence(mcpServerId);
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        apiProvider.restoreAPIRevision(mcpServerId, revisionId, organization);

        API api = apiProvider.getAPIbyUUID(mcpServerId, organization);
        api.setOrganization(organization);
        MCPServerDTO apiToReturn = APIMappingUtil.fromAPItoMCPServerDTO(api, apiProvider);

        Response.Status status = Response.Status.CREATED;
        return Response.status(status).entity(apiToReturn).build();
    }

    /**
     * Undeploys a specific revision of an MCP server.
     *
     * @param mcpServerId                  UUID of the API.
     * @param revisionId                   UUID of the revision to be undeployed.
     * @param revisionNumber               Revision number for the API.
     * @param allEnvironments              Flag indicating whether to undeploy from all environments.
     * @param apIRevisionDeploymentDTOList List of APIRevisionDeploymentDTO objects for undeployment.
     * @param messageContext               Message context of the request.
     * @return HTTP Response containing the updated list of APIRevisionDeploymentDTO objects or an error response.
     * @throws APIManagementException if an error occurs while undeploying the revision.
     */
    @Override
    public Response undeployMCPServerRevision(String mcpServerId, String revisionId, String revisionNumber,
                                              Boolean allEnvironments,
                                              List<APIRevisionDeploymentDTO> apIRevisionDeploymentDTOList,
                                              MessageContext messageContext)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        APIInfo apiInfo = CommonUtils.validateAPIExistence(mcpServerId);
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        if (revisionId == null && revisionNumber != null) {
            revisionId = apiProvider.getAPIRevisionUUID(revisionNumber, mcpServerId);
            if (revisionId == null) {
                throw new APIManagementException(
                        "No revision found for revision number " + revisionNumber + " of MCP server with UUID " +
                                mcpServerId,
                        ExceptionCodes.from(ExceptionCodes.REVISION_NOT_FOUND_FOR_REVISION_NUMBER, revisionNumber));
            }
        }
        Map<String, Environment> environments = APIUtil.getEnvironments(organization);
        List<APIRevisionDeployment> apiRevisionDeployments = new ArrayList<>();
        if (allEnvironments) {
            apiRevisionDeployments = apiProvider.getAPIRevisionDeploymentList(revisionId);
        } else {
            for (APIRevisionDeploymentDTO apiRevisionDeploymentDTO : apIRevisionDeploymentDTOList) {
                Boolean displayOnDevportal = apiRevisionDeploymentDTO.isDisplayOnDevportal();
                String vhost = apiRevisionDeploymentDTO.getVhost();
                String environment = apiRevisionDeploymentDTO.getName();
                APIRevisionDeployment apiRevisionDeployment =
                        ApisApiServiceImplUtils.mapAPIRevisionDeploymentWithValidation(revisionId,
                                environments, environment, displayOnDevportal, vhost, false);
                apiRevisionDeployments.add(apiRevisionDeployment);
            }
        }
        apiProvider.undeployAPIRevisionDeployment(mcpServerId, revisionId, apiRevisionDeployments, organization);
        List<APIRevisionDeployment> apiRevisionDeploymentsResponse =
                apiProvider.getAPIRevisionDeploymentList(revisionId);
        List<APIRevisionDeploymentDTO> apiRevisionDeploymentDTOS = new ArrayList<>();
        for (APIRevisionDeployment apiRevisionDeployment : apiRevisionDeploymentsResponse) {
            apiRevisionDeploymentDTOS.add(APIMappingUtil.fromAPIRevisionDeploymenttoDTO(apiRevisionDeployment));
        }
        Response.Status status = Response.Status.CREATED;
        return Response.status(status).entity(apiRevisionDeploymentDTOS).build();
    }

    /**
     * Adds a comment to an MCP server.
     *
     * @param mcpServerId    UUID of the MCP server.
     * @param body           Request body containing the comment details.
     * @param replyTo        ID of the comment being replied to, if applicable.
     * @param messageContext Message context of the request.
     * @return HTTP Response containing the created CommentDTO or an error response.
     * @throws APIManagementException if an error occurs while adding the comment.
     */
    @Override
    public Response addCommentToMCPServer(String mcpServerId, CommentRequestDTO body, String replyTo,
                                          MessageContext messageContext) throws APIManagementException {

        String username = RestApiCommonUtil.getLoggedInUsername();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(mcpServerId, organization);
            Comment comment = ApisApiServiceImplUtils
                    .createComment(body.getContent(), body.getCategory(),
                            replyTo, username, mcpServerId);
            String createdCommentId = apiProvider.addComment(mcpServerId, comment, username);
            Comment createdComment = apiProvider.getComment(apiTypeWrapper, createdCommentId, 0, 0);
            CommentDTO commentDTO = CommentMappingUtil.fromCommentToDTO(createdComment);

            String uriString = RestApiConstants.RESOURCE_PATH_MCP_SERVERS + "/" + mcpServerId +
                    RestApiConstants.RESOURCE_PATH_COMMENTS + "/" + createdCommentId;
            URI uri = new URI(uriString);
            return Response.created(uri).entity(commentDTO).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId, e, log);
            } else {
                RestApiUtil.handleInternalServerError("Failed to add comment to the MCP Server " + mcpServerId, e, log);
            }
        } catch (URISyntaxException e) {
            throw new APIManagementException(
                    "Error while retrieving comment content location for MCP Server " + mcpServerId);
        }
        return null;
    }

    /**
     * Adds a new document to an MCP server.
     *
     * @param mcpServerId    UUID of the MCP server.
     * @param documentDTO    Document details to be added.
     * @param messageContext Message context of the request.
     * @return HTTP Response containing the created DocumentDTO or an error response.
     * @throws APIManagementException if an error occurs while adding the document.
     */
    @Override
    public Response addMCPServerDocument(String mcpServerId, DocumentDTO documentDTO, MessageContext messageContext)
            throws APIManagementException {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            //validate if api exists
            APIInfo apiInfo = CommonUtils.validateAPIExistence(mcpServerId);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(apiInfo.getStatus().toString());
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            Documentation documentation =
                    PublisherCommonUtils.addDocumentationToAPI(documentDTO, mcpServerId, organization);
            DocumentDTO newDocumentDTO = DocumentationMappingUtil.fromDocumentationToDTO(documentation);
            String uriString = RestApiConstants.RESOURCE_PATH_DOCUMENTS_DOCUMENT_ID
                    .replace(RestApiConstants.MCP_SERVER_ID_PARAM, mcpServerId)
                    .replace(RestApiConstants.DOCUMENTID_PARAM, documentation.getId());
            URI uri = new URI(uriString);
            return Response.created(uri).entity(newDocumentDTO).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil
                        .handleAuthorizationFailure(
                                "Authorization failure while adding documents of MCP Server: " + mcpServerId, e,
                                log);
            } else {
                throw new APIManagementException(
                        "Error while adding a new document to MCP Server " + mcpServerId + " : " + e.getMessage(), e);
            }
        } catch (URISyntaxException e) {
            String errorMessage =
                    "Error while retrieving location for document " + documentDTO.getName() + " of MCP Server " +
                            mcpServerId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Adds content to an existing MCP server document.
     *
     * @param mcpServerId     UUID of the MCP server.
     * @param documentId      UUID of the document to which content should be added.
     * @param ifMatch         ETag value for optimistic concurrency control.
     * @param fileInputStream InputStream of the file to be added as content.
     * @param fileDetail      Attachment details of the file.
     * @param inlineContent   Inline content to be added to the document.
     * @param messageContext  Message context of the request.
     * @return HTTP Response containing the updated DocumentDTO or an error response.
     * @throws APIManagementException if an error occurs while adding content to the document.
     */
    @Override
    public Response addMCPServerDocumentContent(String mcpServerId, String documentId, String ifMatch,
                                                InputStream fileInputStream, Attachment fileDetail,
                                                String inlineContent, MessageContext messageContext)
            throws APIManagementException {

        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            //validate if api exists
            APIInfo apiInfo = CommonUtils.validateAPIExistence(mcpServerId);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(apiInfo.getStatus().toString());
            if (fileInputStream != null && inlineContent != null) {
                RestApiUtil.handleBadRequest("Only one of 'file' and 'inlineContent' should be specified", log);
            }

            //retrieves the document and send 404 if not found
            Documentation documentation = apiProvider.getDocumentation(mcpServerId, documentId, organization);
            if (documentation == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_DOCUMENTATION, documentId, log);
                return null;
            }

            //add content depending on the availability of either input stream or inline content
            if (fileInputStream != null) {
                if (!documentation.getSourceType().equals(Documentation.DocumentSourceType.FILE)) {
                    RestApiUtil.handleBadRequest("Source type of document " + documentId + " is not FILE", log);
                }
                String filename = fileDetail.getContentDisposition().getFilename();
                if (APIUtil.isSupportedFileType(filename)) {
                    RestApiPublisherUtils.attachFileToDocument(mcpServerId, documentation, fileInputStream, fileDetail,
                            organization);
                } else {
                    RestApiUtil.handleBadRequest("Unsupported extension type of document file: " + filename, log);
                }
            } else if (inlineContent != null) {
                if (!documentation.getSourceType().equals(Documentation.DocumentSourceType.INLINE) &&
                        !documentation.getSourceType().equals(Documentation.DocumentSourceType.MARKDOWN)) {
                    RestApiUtil.handleBadRequest("Source type of document " + documentId + " is not INLINE " +
                            "or MARKDOWN", log);
                }
                PublisherCommonUtils
                        .addDocumentationContent(documentation, apiProvider, mcpServerId, documentId, organization,
                                inlineContent);
            } else {
                RestApiUtil.handleBadRequest("Either 'file' or 'inlineContent' should be specified", log);
            }

            //retrieving the updated doc and the URI
            Documentation updatedDoc = apiProvider.getDocumentation(mcpServerId, documentId, organization);
            DocumentDTO documentDTO = DocumentationMappingUtil.fromDocumentationToDTO(updatedDoc);
            String uriString = RestApiConstants.RESOURCE_PATH_DOCUMENT_CONTENT
                    .replace(RestApiConstants.MCP_SERVER_ID_PARAM, mcpServerId)
                    .replace(RestApiConstants.DOCUMENTID_PARAM, documentId);
            URI uri = new URI(uriString);
            return Response.created(uri).entity(documentDTO).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while adding content to the document: " + documentId + " of MCP Server "
                                + mcpServerId, e, log);
            } else if (e.getErrorHandler() != ExceptionCodes.INTERNAL_ERROR) {
                throw e;
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

    /**
     * Changes the lifecycle state of an MCP server.
     *
     * @param action             The action to perform on the lifecycle (e.g., "promote", "demote").
     * @param mcpServerId        UUID of the MCP server.
     * @param lifecycleChecklist JSON string containing the lifecycle checklist.
     * @param ifMatch            ETag value for optimistic concurrency control.
     * @param messageContext     Message context of the request.
     * @return HTTP Response containing the updated lifecycle state or an error response.
     * @throws APIManagementException if an error occurs while changing the lifecycle state.
     */
    @Override
    public Response changeMCPServerLifecycle(String action, String mcpServerId, String lifecycleChecklist,
                                             String ifMatch,
                                             MessageContext messageContext) throws APIManagementException {

        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            ApiTypeWrapper apiWrapper = new ApiTypeWrapper(apiProvider.getAPIbyUUID(mcpServerId, organization));
            APIStateChangeResponse stateChangeResponse = PublisherCommonUtils.changeApiOrApiProductLifecycle(action,
                    apiWrapper, lifecycleChecklist, organization);
            LifecycleStateDTO stateDTO = getLifecycleState(mcpServerId, organization);
            WorkflowResponseDTO workflowResponseDTO = APIMappingUtil
                    .toWorkflowResponseDTO(stateDTO, stateChangeResponse);
            return Response.ok().entity(workflowResponseDTO).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while updating the lifecycle of MCP Server " + mcpServerId, e, log);
            } else {
                throw e;
            }
        }
        return null;
    }

    /**
     * Creates a new MCP server.
     * Validates the API existence and adds the new API with generated Swagger
     * definition.
     *
     * @param body           DTO containing the details of the MCP server to be created.
     * @param openAPIVersion OpenAPI version for the MCP server.
     * @param messageContext Message context of the request.
     * @return HTTP Response containing the created MCPServerDTO or an error response.
     * @throws APIManagementException if an error occurs while creating the MCP server.
     */
    @Override
    public Response createMCPServerFromAPI(MCPServerDTO body, String openAPIVersion, MessageContext messageContext)
            throws APIManagementException {

        URI createdApiUri;
        MCPServerDTO createdApiDTO;
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            OrganizationInfo orgInfo = RestApiUtil.getOrganizationInfo(messageContext);
            populateDefaultValuesForMCPServer(body, APIConstants.API_SUBTYPE_EXISTING_API);
            API createdApi = PublisherCommonUtils
                    .addAPIWithGeneratedSwaggerDefinition(new APIDTOTypeWrapper(body), openAPIVersion,
                            RestApiCommonUtil.getLoggedInUsername(), organization, orgInfo);
            createdApiDTO = APIMappingUtil.fromAPItoMCPServerDTO(createdApi);
            createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_MCP_SERVERS + "/" + createdApiDTO.getId());
            return Response.created(createdApiUri).entity(createdApiDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving MCP Server location : " + body.getProvider() + "-" +
                    body.getName() + "-" + body.getVersion();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (CryptoException e) {
            String errorMessage = "Error while encrypting the secret key of MCP Server : " + body.getProvider() + "-" +
                    body.getName() + "-" + body.getVersion() + " - " + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (ParseException e) {
            String errorMessage =
                    "Error while parsing the endpoint configuration of MCP Server : " + body.getProvider() +
                            "-" + body.getName() + "-" + body.getVersion() + " - " + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Creates a new revision for an MCP server.
     * Validates the API existence and adds the new revision.
     *
     * @param mcpServerId    UUID of the MCP Server to create a revision for.
     * @param apIRevisionDTO DTO containing the revision details.
     * @param messageContext Message context of the request.
     * @return HTTP Response containing the created APIRevisionDTO or an error response.
     * @throws APIManagementException if an error occurs while creating the revision.
     */
    @Override
    public Response createMCPServerRevision(String mcpServerId, APIRevisionDTO apIRevisionDTO,
                                            MessageContext messageContext)
            throws APIManagementException {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);

            APIInfo apiInfo = CommonUtils.validateAPIExistence(mcpServerId);
            API api = apiProvider.getAPIbyUUID(mcpServerId, organization);
            api.setOrganization(organization);

            validateAPIOperationsPerLC(apiInfo.getStatus().toString());
            APIRevision apiRevision = new APIRevision();
            apiRevision.setApiUUID(mcpServerId);
            apiRevision.setDescription(apIRevisionDTO.getDescription());
            String revisionId = apiProvider.addAPIRevision(apiRevision, organization);
            APIRevision createdApiRevision = apiProvider.getAPIRevision(revisionId);
            APIRevisionDTO createdApiRevisionDTO = APIMappingUtil.fromAPIRevisiontoDTO(createdApiRevision);
            URI createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_MCP_SERVERS
                    + "/" + createdApiRevisionDTO.getApiInfo().getId() + "/"
                    + RestApiConstants.RESOURCE_PATH_REVISIONS + "/" + createdApiRevisionDTO.getId());
            return Response.created(createdApiUri).entity(createdApiRevisionDTO).build();
        } catch (APIManagementException e) {
            if (e instanceof APIComplianceException) {
                throw e;
            }
            String errorMessage = "Error while adding new revision for MCP server: " + mcpServerId;
            if ((e.getErrorHandler()
                    .getErrorCode() == ExceptionCodes.THIRD_PARTY_API_REVISION_CREATION_UNSUPPORTED.getErrorCode())
                    ||
                    (e.getErrorHandler().getErrorCode() == ExceptionCodes.MAXIMUM_REVISIONS_REACHED.getErrorCode())) {
                throw e;
            } else {
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving created revision location for MCP server: "
                    + mcpServerId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Creates a new version of an existing MCP server.
     * Validates the API existence, checks for existing versions, and creates the new version.
     *
     * @param newVersion     The new version to be created.
     * @param mcpServerId    UUID of the MCP Server to create a new version for.
     * @param defaultVersion Indicates if this is the default version.
     * @param serviceVersion Version of the service to be associated with the MCP server.
     * @param messageContext Message context of the request.
     * @return HTTP Response containing the created MCPServerDTO or an error response.
     * @throws APIManagementException if an error occurs while creating the new version.
     */
    @Override
    public Response createNewMCPServerVersion(String newVersion, String mcpServerId, Boolean defaultVersion,
                                              String serviceVersion, MessageContext messageContext)
            throws APIManagementException {

        URI newVersionedApiUri;
        MCPServerDTO newVersionedApi = new MCPServerDTO();
        ServiceEntry service = new ServiceEntry();
        try {
            APIIdentifier mcpServerIdentifierFromTable =
                    APIMappingUtil.getAPIIdentifierFromUUID(mcpServerId);
            if (mcpServerIdentifierFromTable == null) {
                throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API with API UUID: "
                        + mcpServerId, ExceptionCodes.from(ExceptionCodes.MCP_SERVER_NOT_FOUND,
                        mcpServerId));
            }
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            int tenantId = APIUtil.getInternalOrganizationId(organization);
            API existingAPI = apiProvider.getAPIbyUUID(mcpServerId, organization);
            if (existingAPI == null) {
                throw new APIMgtResourceNotFoundException("MCP Server not found for id " + mcpServerId,
                        ExceptionCodes.from(ExceptionCodes.MCP_SERVER_NOT_FOUND, mcpServerId));
            }
            //Get all existing versions of API
            Set<String> apiVersions = apiProvider.getAPIVersions(mcpServerIdentifierFromTable.getProviderName(),
                    mcpServerIdentifierFromTable.getApiName(), organization);
            if (apiVersions.contains(newVersion)) {
                throw new APIMgtResourceAlreadyExistsException(
                        "Version " + newVersion + " exists for api " + existingAPI.getId().getApiName(),
                        ExceptionCodes.from(ExceptionCodes.MCP_SERVER_VERSION_ALREADY_EXISTS, newVersion,
                                existingAPI.getId().getApiName()));
            }
            if (StringUtils.isNotEmpty(serviceVersion)) {
                String serviceName = existingAPI.getServiceInfo("name");
                ServiceCatalogImpl serviceCatalog = new ServiceCatalogImpl();
                service = serviceCatalog.getServiceByNameAndVersion(serviceName, serviceVersion, tenantId);
                if (service == null) {
                    throw new APIManagementException("No matching service version found",
                            ExceptionCodes.SERVICE_VERSION_NOT_FOUND);
                }
            }
            if (StringUtils.isNotEmpty(serviceVersion) && !serviceVersion
                    .equals(existingAPI.getServiceInfo("version"))) {
                MCPServerDTO apidto = createMCPServerDTO(existingAPI, newVersion);
                if (ServiceEntry.DefinitionType.OAS2.equals(service.getDefinitionType()) || ServiceEntry
                        .DefinitionType.OAS3.equals(service.getDefinitionType())) {
                    newVersionedApi =
                            RestApiPublisherUtils.importDefinitionForMCPServers(service.getEndpointDef(), null,
                                    null, new APIDTOTypeWrapper(apidto), null, service, organization, null);
                }
            } else {
                API versionedAPI =
                        apiProvider.createNewAPIVersion(mcpServerId, newVersion, defaultVersion, organization);
                newVersionedApi = APIMappingUtil.fromAPItoMCPServerDTO(versionedAPI);
            }
            //This URI used to set the location header of the POST response
            newVersionedApiUri =
                    new URI(RestApiConstants.RESOURCE_PATH_MCP_SERVERS + "/" + newVersionedApi.getId());
            return Response.created(newVersionedApiUri).entity(newVersionedApi).build();
        } catch (APIManagementException e) {
            if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("Authorization failure while copying API : " + mcpServerId, e,
                        log);
            } else {
                throw e;
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving MCP Server location of " + mcpServerId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Deletes a specific comment from an MCP server.
     * Validates the comment existence, checks user permissions, and deletes the comment.
     *
     * @param commentId      UUID of the comment to be deleted.
     * @param mcpServerId    UUID of the MCP server.
     * @param ifMatch        ETag value for optimistic concurrency control.
     * @param messageContext Message context of the request.
     * @return HTTP Response indicating the result of the deletion operation.
     * @throws APIManagementException if an error occurs while deleting the comment.
     */
    @Override
    public Response deleteCommentOfMCPServer(String commentId, String mcpServerId, String ifMatch,
                                             MessageContext messageContext) throws APIManagementException {

        String requestedTenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        String username = RestApiCommonUtil.getLoggedInUsername();
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(mcpServerId, requestedTenantDomain);
            Comment comment = apiProvider.getComment(apiTypeWrapper, commentId, 0, 0);
            if (comment != null) {
                String[] tokenScopes = (String[]) PhaseInterceptorChain.getCurrentMessage().getExchange()
                        .get(RestApiConstants.USER_REST_API_SCOPES);
                if (Arrays.asList(tokenScopes).contains(RestApiConstants.ADMIN_SCOPE) ||
                        comment.getUser().equals(username)) {
                    if (apiProvider.deleteComment(apiTypeWrapper, commentId)) {
                        JSONObject obj = new JSONObject();
                        obj.put("id", commentId);
                        obj.put("message", "The comment has been deleted");
                        return Response.ok(obj).type(MediaType.APPLICATION_JSON).build();
                    } else {
                        return Response.status(405, "Method Not Allowed").type(MediaType
                                .APPLICATION_JSON).build();
                    }
                } else {
                    return Response.status(403, "Forbidden").type(MediaType.APPLICATION_JSON).build();
                }
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_COMMENTS,
                        String.valueOf(commentId), log);
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId, e, log);
            } else if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId, e, log);
            } else {
                String errorMessage = "Error while deleting comment " + commentId + "for MCP Server " + mcpServerId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * Deletes a specific MCP server by its UUID.
     * Validates the API existence, checks for active subscriptions, and deletes the API.
     *
     * @param mcpServerId    UUID of the API to be deleted.
     * @param ifMatch        ETag value for optimistic concurrency control.
     * @param messageContext Message context of the request.
     * @return HTTP Response indicating the result of the deletion operation.
     * @throws APIManagementException if an error occurs while deleting the MCP server.
     */
    @Override
    public Response deleteMCPServer(String mcpServerId, String ifMatch, MessageContext messageContext)
            throws APIManagementException {

        try {
            String username = RestApiCommonUtil.getLoggedInUsername();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIProvider apiProvider = RestApiCommonUtil.getProvider(username);

            boolean isAPIExistDB = false;
            APIManagementException error = null;
            APIInfo apiInfo = null;
            try {
                apiInfo = CommonUtils.validateAPIExistence(mcpServerId);
                if (APIConstants.API_TYPE_MCP.equals(apiInfo.getApiType())) {
                    isAPIExistDB = true;
                } else {
                    String errorMessage =
                            "Error while validating MCP server existence for deleting API : " + mcpServerId +
                                    " on organization " + organization + ". API is not of type MCP";
                    RestApiUtil.handleBadRequest(errorMessage, log);
                }
            } catch (APIManagementException e) {
                log.error("Error while validating API existence for deleting MCP server " + mcpServerId +
                        " on organization "
                        + organization);
                error = e;
            }

            if (isAPIExistDB) {
                validateAPIOperationsPerLC(apiInfo.getStatus().toString());
                try {
                    List<SubscribedAPI> apiUsages = apiProvider.getAPIUsageByAPIId(mcpServerId, organization);
                    if (apiUsages != null && !apiUsages.isEmpty()) {
                        List<SubscribedAPI> filteredUsages = new ArrayList<>();
                        for (SubscribedAPI usage : apiUsages) {
                            String subsCreatedStatus = usage.getSubCreatedStatus();
                            if (!APIConstants.SubscriptionCreatedStatus.UN_SUBSCRIBE.equals(subsCreatedStatus)) {
                                filteredUsages.add(usage);
                            }
                        }
                        if (!filteredUsages.isEmpty()) {
                            RestApiUtil.handleConflict("Cannot remove the MCP server: " + mcpServerId
                                    + " as active subscriptions exist", log);
                        }
                    }
                } catch (APIManagementException e) {
                    log.error("Error while checking active subscriptions for deleting MCP server "
                            + mcpServerId + " on organization " + organization);
                    error = e;
                }
            }
            boolean isDeleted = false;
            try {
                apiProvider.deleteAPI(mcpServerId, organization);
                isDeleted = true;
            } catch (APIManagementException e) {
                log.error("Error while deleting MCP server: " + mcpServerId + "on organization " + organization, e);
            }

            if (error != null) {
                throw error;
            } else if (!isDeleted) {
                RestApiUtil.handleInternalServerError("Error while deleting MCP server: " + mcpServerId
                        + " on organization " + organization, log);
                return null;
            }
            PublisherCommonUtils.clearArtifactComplianceInfo(mcpServerId, RestApiConstants.RESOURCE_MCP_SERVER,
                    organization);
            return Response.ok().build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("Authorization failure while deleting MCP server: "
                        + mcpServerId, e, log);
            } else {
                String errorMessage = "Error while deleting MCP server : " + mcpServerId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * Deletes a specific document of an MCP server.
     * Validates the API existence and deletes the document.
     *
     * @param mcpServerId    UUID of the API.
     * @param documentId     UUID of the document to be deleted.
     * @param ifMatch        ETag value for optimistic concurrency control.
     * @param messageContext Message context of the request.
     * @return HTTP Response indicating the result of the deletion operation.
     * @throws APIManagementException if an error occurs while deleting the document.
     */
    @Override
    public Response deleteMCPServerDocument(String mcpServerId, String documentId, String ifMatch,
                                            MessageContext messageContext) throws APIManagementException {

        Documentation documentation;
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);

            //validate if api exists
            APIInfo apiInfo = CommonUtils.validateAPIExistence(mcpServerId);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(apiInfo.getStatus().toString());

            documentation = apiProvider.getDocumentation(mcpServerId, documentId, organization);
            if (documentation == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_DOCUMENTATION, documentId, log);
            }
            apiProvider.removeDocumentation(mcpServerId, documentId, organization);
            return Response.ok().build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while deleting : " + documentId + " of MCP Server " + mcpServerId, e,
                        log);
            } else {
                String errorMessage = "Error while retrieving MCP Server : " + mcpServerId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    @Override
    public Response deleteMCPServerLifecycleStatePendingTasks(String mcpServerId, MessageContext messageContext)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        APIIdentifier apiIdentifierFromTable = APIMappingUtil.getAPIIdentifierFromUUID(mcpServerId);
        if (apiIdentifierFromTable == null) {
            throw new APIMgtResourceNotFoundException(
                    "Couldn't retrieve existing MCP Server with API UUID: " + mcpServerId,
                    ExceptionCodes.from(ExceptionCodes.MCP_SERVER_NOT_FOUND, mcpServerId));
        }
        apiProvider.deleteWorkflowTask(apiIdentifierFromTable);
        return Response.ok().build();
    }

    /**
     * Deletes a specific revision of an MCP server.
     * Validates the API existence and deletes the revision.
     *
     * @param mcpServerId    UUID of the API.
     * @param revisionId     UUID of the revision to be deleted.
     * @param messageContext Message context of the request.
     * @return HTTP Response containing the updated list of APIRevisionDTO objects or an error response.
     * @throws APIManagementException if an error occurs while deleting the revision.
     */
    @Override
    public Response deleteMCPServerRevision(String mcpServerId, String revisionId, MessageContext messageContext)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APIInfo apiInfo = CommonUtils.validateAPIExistence(mcpServerId);
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        apiProvider.deleteAPIRevision(mcpServerId, revisionId, organization);
        List<APIRevision> apiRevisions = apiProvider.getAPIRevisions(mcpServerId);
        APIRevisionListDTO apiRevisionListDTO = APIMappingUtil.fromListAPIRevisiontoDTO(apiRevisions);
        return Response.ok().entity(apiRevisionListDTO).build();
    }

    @Override
    public Response deleteMCPServerRevisionDeploymentPendingTask(String mcpServerId, String revisionId, String envName,
                                                                 MessageContext messageContext)
            throws APIManagementException {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            APIIdentifier apiIdentifierFromTable = APIMappingUtil.getAPIIdentifierFromUUID(mcpServerId);
            if (apiIdentifierFromTable == null) {
                throw new APIMgtResourceNotFoundException(
                        "Couldn't retrieve existing MCP Server with UUID: " + mcpServerId,
                        ExceptionCodes.from(ExceptionCodes.MCP_SERVER_NOT_FOUND, mcpServerId));
            }
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();

            List<WorkflowDTO> workflowDTOList = apiMgtDAO.retrieveAllWorkflowFromInternalReference(revisionId,
                    WorkflowConstants.WF_TYPE_AM_REVISION_DEPLOYMENT);
            String externalRef = null;
            for (WorkflowDTO workflowDTO : workflowDTOList) {
                Object environment = workflowDTO.getMetadata(WorkflowConstants.ENVIRONMENT);
                if (StringUtils.equals(envName, environment != null ? environment.toString() : null)) {
                    externalRef = workflowDTO.getExternalWorkflowReference();
                    break;
                }
            }

            if (externalRef == null) {
                throw new APIMgtResourceNotFoundException(
                        "Couldn't retrieve existing MCP Server Revision with Revision Id: " + revisionId,
                        ExceptionCodes.from(ExceptionCodes.MCP_SERVER_REVISION_NOT_FOUND, revisionId));
            }
            apiProvider.cleanupAPIRevisionDeploymentWorkflows(mcpServerId, externalRef);
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while deleting task ";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Deploys a specific revision of an MCP server to the specified environments.
     * Validates the API existence and deploys the revision.
     *
     * @param mcpServerId                  UUID of the API.
     * @param revisionId                   UUID of the revision to be deployed.
     * @param apIRevisionDeploymentDTOList List of APIRevisionDeploymentDTO objects for deployment.
     * @param messageContext               Message context of the request.
     * @return HTTP Response containing the updated list of APIRevisionDeploymentDTO objects or an error response.
     * @throws APIManagementException if an error occurs while deploying the revision.
     */
    @Override
    public Response deployMCPServerRevision(String mcpServerId, String revisionId,
                                            List<APIRevisionDeploymentDTO> apIRevisionDeploymentDTOList,
                                            MessageContext messageContext)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        if (revisionId.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Revision Id is not provided").build();
        }
        APIInfo apiInfo = CommonUtils.validateAPIExistence(mcpServerId);
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        API api = apiProvider.getAPIbyUUID(mcpServerId, organization);
        api.setOrganization(organization);
        MCPServerDTO apiDto = APIMappingUtil.fromAPItoMCPServerDTO(api, apiProvider);

        if (apiDto.getLifeCycleStatus().equals(APIConstants.RETIRED)) {
            String errorMessage =
                    "Deploying MCP server revisions is not supported for retired APIs. mcpServerId: " + mcpServerId;
            throw new APIManagementException(errorMessage, ExceptionCodes.from(
                    ExceptionCodes.RETIRED_MCP_SERVER_REVISION_DEPLOYMENT_UNSUPPORTED, mcpServerId));
        }

        Map<String, Environment> environments = APIUtil.getEnvironments(organization);
        List<APIRevisionDeployment> apiRevisionDeployments = new ArrayList<>();
        for (APIRevisionDeploymentDTO apiRevisionDeploymentDTO : apIRevisionDeploymentDTOList) {
            String environment = apiRevisionDeploymentDTO.getName();
            Boolean displayOnDevportal = apiRevisionDeploymentDTO.isDisplayOnDevportal();
            String vhost = apiRevisionDeploymentDTO.getVhost();
            APIRevisionDeployment apiRevisionDeployment =
                    ApisApiServiceImplUtils.mapAPIRevisionDeploymentWithValidation(revisionId,
                            environments, environment, displayOnDevportal, vhost, true);
            apiRevisionDeployments.add(apiRevisionDeployment);
        }
        apiProvider.deployAPIRevision(mcpServerId, revisionId, apiRevisionDeployments, organization);
        List<APIRevisionDeployment> apiRevisionDeploymentsResponse =
                apiProvider.getAPIRevisionsDeploymentList(mcpServerId);
        List<APIRevisionDeploymentDTO> apiRevisionDeploymentDTOS = new ArrayList<>();
        for (APIRevisionDeployment apiRevisionDeployment : apiRevisionDeploymentsResponse) {
            apiRevisionDeploymentDTOS.add(APIMappingUtil.fromAPIRevisionDeploymenttoDTO(apiRevisionDeployment));
        }
        Response.Status status = Response.Status.CREATED;
        return Response.status(status).entity(apiRevisionDeploymentDTOS).build();
    }

    /**
     * Edits a specific comment of an MCP server.
     * Validates the comment existence, checks user permissions, and updates the comment.
     *
     * @param commentId           UUID of the comment to be edited.
     * @param mcpServerId         UUID of the MCP server.
     * @param patchRequestBodyDTO DTO containing the updated comment details.
     * @param messageContext      Message context of the request.
     * @return HTTP Response containing the updated CommentDTO or an error response.
     * @throws APIManagementException if an error occurs while editing the comment.
     */
    @Override
    public Response editCommentOfMCPServer(String commentId, String mcpServerId,
                                           CommentRequestDTO patchRequestBodyDTO, MessageContext messageContext)
            throws APIManagementException {

        String username = RestApiCommonUtil.getLoggedInUsername();
        String requestedTenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(mcpServerId, requestedTenantDomain);
            Comment comment = apiProvider.getComment(apiTypeWrapper, commentId, 0, 0);
            if (comment != null) {
                if (comment.getUser().equals(username)) {
                    boolean commentEdited = false;
                    if (patchRequestBodyDTO.getCategory() != null && !(patchRequestBodyDTO.getCategory().equals(comment
                            .getCategory()))) {
                        comment.setCategory(patchRequestBodyDTO.getCategory());
                        commentEdited = true;
                    }
                    if (patchRequestBodyDTO.getContent() != null && !(patchRequestBodyDTO.getContent().equals(comment
                            .getText()))) {
                        comment.setText(patchRequestBodyDTO.getContent());
                        commentEdited = true;
                    }
                    if (commentEdited) {
                        if (apiProvider.editComment(apiTypeWrapper, commentId, comment)) {
                            Comment editedComment = apiProvider.getComment(apiTypeWrapper, commentId, 0, 0);
                            CommentDTO commentDTO = CommentMappingUtil.fromCommentToDTO(editedComment);

                            String uriString = RestApiConstants.RESOURCE_PATH_MCP_SERVERS + "/" + mcpServerId +
                                    RestApiConstants.RESOURCE_PATH_COMMENTS + "/" + commentId;
                            URI uri = new URI(uriString);
                            return Response.ok(uri).entity(commentDTO).build();
                        }
                    } else {
                        return Response.ok().build();
                    }
                } else {
                    RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_COMMENTS, String.valueOf(commentId)
                            , log);
                }
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_COMMENTS,
                        String.valueOf(commentId), log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving comment content location for MCP Server " + mcpServerId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Exports an MCP server in the specified format.
     * If the gateway environment is not specified, it exports the MCP Server in the given format.
     * If the gateway environment is specified, it generates a runtime artifact for the MCP server.
     *
     * @param mcpServerId         UUID of the MCP server to be exported.
     * @param name                Name of the MCP server.
     * @param version             Version of the MCP server.
     * @param revisionNumber      Revision number of the MCP server.
     * @param providerName        Provider name of the MCP server.
     * @param format              Export format (YAML or ZIP).
     * @param preserveStatus      Whether to preserve the status of the API during export.
     * @param latestRevision      Whether to export the latest revision.
     * @param gatewayEnvironment  Gateway environment for runtime artifact generation.
     * @param preserveCredentials Whether to preserve credentials during export.
     * @param messageContext      Message context of the request.
     * @return HTTP Response containing the exported file or runtime artifact.
     * @throws APIManagementException if an error occurs while exporting the MCP server.
     */
    @Override
    public Response exportMCPServer(String mcpServerId, String name, String version, String revisionNumber,
                                    String providerName, String format, Boolean preserveStatus, Boolean latestRevision,
                                    String gatewayEnvironment, Boolean preserveCredentials,
                                    MessageContext messageContext) throws APIManagementException {

        if (StringUtils.isEmpty(gatewayEnvironment)) {
            preserveStatus = preserveStatus == null || preserveStatus;
            preserveCredentials = preserveCredentials != null && preserveCredentials;
            ExportFormat exportFormat = StringUtils.isNotEmpty(format) ?
                    ExportFormat.valueOf(format.toUpperCase()) :
                    ExportFormat.YAML;
            try {
                String organization = RestApiUtil.getValidatedOrganization(messageContext);
                ImportExportAPI importExportAPI = APIImportExportUtil.getImportExportAPI();
                File file = importExportAPI
                        .exportMCPServer(mcpServerId, name, version, revisionNumber, providerName, preserveStatus,
                                exportFormat, Boolean.TRUE, preserveCredentials, latestRevision, StringUtils.EMPTY,
                                organization);
                return Response.ok(file).header(RestApiConstants.HEADER_CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getName() + "\"").build();
            } catch (APIImportExportException e) {
                throw new APIManagementException("Error while exporting " + RestApiConstants.RESOURCE_MCP_SERVER, e);
            }
        } else {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            if (StringUtils.isEmpty(mcpServerId) && (StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(version))) {
                APIIdentifier mcpServerIdentifier = new APIIdentifier(providerName, name, version);
                mcpServerId = APIUtil.getUUIDFromIdentifier(mcpServerIdentifier, organization);
                if (StringUtils.isEmpty(mcpServerId)) {
                    throw new APIManagementException("API not found for the given name: " + name + ", and version : "
                            + version, ExceptionCodes.from(ExceptionCodes.MCP_SERVER_NOT_FOUND, name + "-" + version));
                }
            }
            RuntimeArtifactDto runtimeArtifactDto = null;
            if (StringUtils.isNotEmpty(organization)) {
                runtimeArtifactDto =
                        RuntimeArtifactGeneratorUtil.generateRuntimeArtifact(mcpServerId, gatewayEnvironment,
                                APIConstants.API_GATEWAY_TYPE_ENVOY, organization);
            }
            if (runtimeArtifactDto != null) {
                if (runtimeArtifactDto.isFile()) {
                    File artifact = (File) runtimeArtifactDto.getArtifact();
                    StreamingOutput streamingOutput = (outputStream) -> {
                        try {
                            Files.copy(artifact.toPath(), outputStream);
                        } finally {
                            Files.delete(artifact.toPath());
                        }
                    };
                    return Response.ok(streamingOutput).header(RestApiConstants.HEADER_CONTENT_DISPOSITION,
                            "attachment; filename=apis.zip").header(RestApiConstants.HEADER_CONTENT_TYPE,
                            APIConstants.APPLICATION_ZIP).build();
                }
            }
            throw new APIManagementException("No MCP Server Artifacts", ExceptionCodes.NO_MCP_SERVER_ARTIFACT_FOUND);
        }
    }

    /**
     * Generates an API key for the specified MCP server.
     *
     * @param mcpServerId    the MCP server’s identifier
     * @param messageContext context for organization validation
     * @return 200 OK with an APIKeyDTO (token + validity in ms)
     * @throws APIManagementException on validation or generation error
     */
    @Override
    public Response generateInternalAPIKeyMCPServer(String mcpServerId, MessageContext messageContext)
            throws APIManagementException {

        String userName = RestApiCommonUtil.getLoggedInUsername();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(userName);
        String token = apiProvider.generateApiKey(mcpServerId, organization);
        APIKeyDTO apiKeyDTO = new APIKeyDTO();
        apiKeyDTO.setApikey(token);
        apiKeyDTO.setValidityTime(60 * 1000);
        return Response.ok().entity(apiKeyDTO).build();
    }

    @Override
    public Response getAllCommentsOfMCPServer(String mcpServerId, String xWSO2Tenant, Integer limit, Integer offset,
                                              Boolean includeCommenterInfo, MessageContext messageContext)
            throws APIManagementException {

        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(mcpServerId, requestedTenantDomain);

            if (limit == null) {
                limit = RestApiConstants.PAGINATION_LIMIT_DEFAULT;
            }
            if (offset == null) {
                offset = RestApiConstants.PAGINATION_OFFSET_DEFAULT;
            }

            String parentCommentID = null;
            CommentList comments = apiProvider.getComments(apiTypeWrapper, parentCommentID, limit, offset);
            CommentListDTO commentDTO = CommentMappingUtil.fromCommentListToDTO(comments, includeCommenterInfo);

            String uriString = RestApiConstants.RESOURCE_PATH_MCP_SERVERS + "/" + mcpServerId +
                    RestApiConstants.RESOURCE_PATH_COMMENTS;
            URI uri = new URI(uriString);
            return Response.ok().contentLocation(uri).entity(commentDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving comments content location for MCP Server " + mcpServerId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Updates an existing MCP server with the provided MCPServerDTO.
     * Validates the API existence and updates the API.
     *
     * @param mcpServerId    UUID of the API to be updated.
     * @param body           MCPServerDTO containing the updated API details.
     * @param ifMatch        ETag value for optimistic concurrency control.
     * @param messageContext Message context of the request.
     * @return HTTP Response containing the updated MCPServerDTO or an error response.
     * @throws APIManagementException if an error occurs while updating the MCP server.
     */
    @Override
    public Response updateMCPServer(String mcpServerId, MCPServerDTO body, String ifMatch,
                                    MessageContext messageContext) throws APIManagementException {

        String[] tokenScopes =
                (String[]) PhaseInterceptorChain.getCurrentMessage().getExchange()
                        .get(RestApiConstants.USER_REST_API_SCOPES);
        String username = RestApiCommonUtil.getLoggedInUsername();
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            OrganizationInfo organizationInfo = RestApiUtil.getOrganizationInfo(messageContext);
            CommonUtils.validateAPIExistence(mcpServerId);
            if (!PublisherCommonUtils.validateEndpointConfigs(new APIDTOTypeWrapper(body))) {
                throw new APIManagementException("Invalid endpoint configs detected",
                        ExceptionCodes.INVALID_ENDPOINT_CONFIG);
            }

            org.json.simple.JSONArray customProperties = APIUtil.getCustomProperties(organization);
            List<String> errorProperties = PublisherCommonUtils.validateMandatoryProperties(customProperties, body);
            if (!errorProperties.isEmpty()) {
                String errorString = " : " + String.join(", ", errorProperties);
                RestApiUtil.handleBadRequest(
                        ExceptionCodes.ERROR_WHILE_UPDATING_MANDATORY_PROPERTIES.getErrorMessage() + errorString,
                        ExceptionCodes.ERROR_WHILE_UPDATING_MANDATORY_PROPERTIES.getErrorCode(), log);
            }

            if (!PublisherCommonUtils.validateEndpoints(new APIDTOTypeWrapper(body))) {
                throw new APIManagementException("Invalid/Malformed endpoint URL(s) detected",
                        ExceptionCodes.INVALID_ENDPOINT_URL);
            }

            APIProvider apiProvider = RestApiCommonUtil.getProvider(username);
            API originalAPI = apiProvider.getAPIbyUUID(mcpServerId, organization);
            originalAPI.setOrganization(organization);

            validateAPIOperationsPerLC(originalAPI.getStatus());

            API updatedApi =
                    PublisherCommonUtils.updateApi(originalAPI, new APIDTOTypeWrapper(body), apiProvider, tokenScopes,
                            organizationInfo);

            return Response.ok().entity(APIMappingUtil.fromAPItoMCPServerDTO(updatedApi)).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                if (e.getErrorHandler()
                        .getErrorCode() == ExceptionCodes.GLOBAL_MEDIATION_POLICIES_NOT_FOUND.getErrorCode()) {
                    RestApiUtil.handleResourceNotFoundError(e.getErrorHandler().getErrorDescription(), e, log);
                } else {
                    RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId, e, log);
                }
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("Authorization failure while updating MCP server: "
                        + mcpServerId, e, log);
            } else {
                throw e;
            }
        } catch (FaultGatewaysException e) {
            String errorMessage = "Error while updating MCP server: " + mcpServerId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (CryptoException e) {
            String errorMessage = "Error while encrypting the secret key of MCP server: " + mcpServerId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (ParseException e) {
            String errorMessage = "Error while parsing endpoint config of MCP server: " + mcpServerId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Updates a specific document of an MCP server.
     * Validates the API existence and updates the document.
     *
     * @param mcpServerId    UUID of the API.
     * @param documentId     UUID of the document to be updated.
     * @param documentDTO    DocumentDTO containing the updated document details.
     * @param ifMatch        ETag value for optimistic concurrency control.
     * @param messageContext Message context of the request.
     * @return HTTP Response containing the updated DocumentationDTO or an error response.
     * @throws APIManagementException if an error occurs while updating the document.
     */
    @Override
    public Response updateMCPServerDocument(String mcpServerId, String documentId, DocumentDTO documentDTO,
                                            String ifMatch,
                                            MessageContext messageContext) throws APIManagementException {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            //validate if api exists
            APIInfo apiInfo = CommonUtils.validateAPIExistence(mcpServerId);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(apiInfo.getStatus().toString());

            String sourceUrl = documentDTO.getSourceUrl();
            Documentation oldDocument = apiProvider.getDocumentation(mcpServerId, documentId, organization);

            //validation checks for existence of the document
            if (documentDTO.getType() == null) {
                throw new BadRequestException();
            }
            if (oldDocument == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_DOCUMENTATION, documentId, log);
                return null;
            }
            if (documentDTO.getType() == DocumentDTO.TypeEnum.OTHER &&
                    org.apache.commons.lang3.StringUtils.isBlank(documentDTO.getOtherTypeName())) {
                //check otherTypeName for not null if doc type is OTHER
                RestApiUtil.handleBadRequest("otherTypeName cannot be empty if type is OTHER.", log);
                return null;
            }
            if (documentDTO.getSourceType() == DocumentDTO.SourceTypeEnum.URL &&
                    (org.apache.commons.lang3.StringUtils.isBlank(sourceUrl) || !RestApiCommonUtil.isURL(sourceUrl))) {
                RestApiUtil.handleBadRequest("Invalid document sourceUrl Format", log);
                return null;
            }
            if (documentDTO.getType() == DocumentDTO.TypeEnum.OTHER
                    && documentDTO.getOtherTypeName() != null
                    &&
                    apiProvider.isAnotherOverviewDocumentationExist(mcpServerId, documentId,
                            documentDTO.getOtherTypeName(),
                            organization)) {
                RestApiUtil.handleBadRequest("Requested other document type _overview already exists", log);
                return null;
            }

            //overriding some properties
            documentDTO.setName(oldDocument.getName());

            Documentation newDocumentation = DocumentationMappingUtil.fromDTOtoDocumentation(documentDTO);
            newDocumentation.setFilePath(oldDocument.getFilePath());
            newDocumentation.setId(documentId);
            newDocumentation = apiProvider.updateDocumentation(mcpServerId, newDocumentation, organization);

            return Response.ok().entity(DocumentationMappingUtil.fromDocumentationToDTO(newDocumentation)).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while updating document : " + documentId + " of MCP Server " +
                                mcpServerId, e, log);
            } else {
                String errorMessage =
                        "Error while updating the document " + documentId + " for MCP Server : " + mcpServerId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * Updates the backend API of an MCP server.
     * Validates the API existence and updates the backend API.
     *
     * @param mcpServerId    UUID of the MCP Server.
     * @param backendApiId   UUID of the backend API to be updated.
     * @param backendAPIDTO  BackendDTO containing the updated backend API details.
     * @param messageContext Message context of the request.
     * @return HTTP Response containing the updated BackendDTO or an error response.
     * @throws APIManagementException if an error occurs while updating the backend API.
     */
    @Override
    public Response updateMCPServerBackend(String mcpServerId, String backendApiId, BackendDTO backendAPIDTO,
                                           MessageContext messageContext) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        CommonUtils.validateAPIExistence(mcpServerId);
        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        try {
            Backend oldBackend = apiProvider.getMCPServerBackend(mcpServerId, backendApiId, organization);
            if (oldBackend == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId, log);
            }
            Backend backend = new Backend(oldBackend);
            Object endpointConfigObj = backendAPIDTO.getEndpointConfig();
            if (endpointConfigObj == null) {
                RestApiUtil.handleBadRequest("Endpoint config cannot be null", log);
            }
            if (endpointConfigObj instanceof Map) {
                backend.setEndpointConfigFromMap((Map<String, Object>) endpointConfigObj);
            } else if (endpointConfigObj instanceof String) {
                JSONParser parser = new JSONParser();
                Object parsedEndpointConfig = parser.parse(endpointConfigObj.toString());
                backend.setEndpointConfigFromMap((Map<String, Object>) parsedEndpointConfig);
            } else {
                RestApiUtil.handleBadRequest("Endpoint config is not in correct format", log);
            }
            PublisherCommonUtils.updateMCPServerBackend(mcpServerId, oldBackend, backend, organization, apiProvider);
            return Response.ok().entity(APIMappingUtil.fromBackendAPIToDTO(backend, organization, false)).build();
        } catch (ParseException e) {
            RestApiUtil.handleBadRequest("Endpoint config is not in correct format", e, log);
        }
        return null;
    }

    /**
     * Updates the deployment details of a specific revision of an MCP server.
     * Validates the API existence and updates the deployment details.
     *
     * @param mcpServerId              UUID of the API.
     * @param deploymentId             ID of the deployment to be updated.
     * @param apIRevisionDeploymentDTO APIRevisionDeploymentDTO containing the updated deployment details.
     * @param messageContext           Message context of the request.
     * @return HTTP Response containing the updated APIRevisionDeploymentDTO or an error response.
     * @throws APIManagementException if an error occurs while updating the deployment details.
     */
    @Override
    public Response updateMCPServerDeployment(String mcpServerId, String deploymentId,
                                              APIRevisionDeploymentDTO apIRevisionDeploymentDTO,
                                              MessageContext messageContext) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        RestApiUtil.getValidatedOrganization(messageContext);

        APIInfo apiInfo = CommonUtils.validateAPIExistence(mcpServerId);
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        String revisionId = apIRevisionDeploymentDTO.getRevisionUuid();
        String vhost = apIRevisionDeploymentDTO.getVhost();
        Boolean displayOnDevportal = apIRevisionDeploymentDTO.isDisplayOnDevportal();
        String decodedDeploymentName = ApisApiServiceImplUtils.getDecodedDeploymentName(deploymentId);
        APIRevisionDeployment apiRevisionDeployment =
                ApisApiServiceImplUtils.mapApiRevisionDeployment(revisionId, vhost,
                        displayOnDevportal, decodedDeploymentName);
        apiProvider.updateAPIDisplayOnDevportal(mcpServerId, revisionId, apiRevisionDeployment);
        APIRevisionDeployment apiRevisionDeploymentsResponse = apiProvider.
                getAPIRevisionDeployment(decodedDeploymentName, revisionId);
        APIRevisionDeploymentDTO apiRevisionDeploymentDTO = APIMappingUtil.
                fromAPIRevisionDeploymenttoDTO(apiRevisionDeploymentsResponse);
        Response.Status status = Response.Status.OK;

        return Response.status(status).entity(apiRevisionDeploymentDTO).build();
    }

    /**
     * Validates whether the MCP server exists based on the provided query.
     * The query can be in the format "name:<apiName>" or "context:<apiContext>".
     *
     * @param query          The query to validate the MCP server existence.
     * @param ifNoneMatch    ETag value for optimistic concurrency control.
     * @param messageContext Message context of the request.
     * @return Response indicating whether the MCP server exists or not.
     * @throws APIManagementException if an error occurs while validating the MCP server.
     */
    @Override
    public Response validateMCPServer(String query, String ifNoneMatch, MessageContext messageContext)
            throws APIManagementException {

        boolean isSearchArtifactExists = false;
        if (StringUtils.isEmpty(query)) {
            RestApiUtil.handleBadRequest("The query should not be empty", log);
        }
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            if (query.contains(":")) {
                String[] queryTokens = query.split(":");
                switch (queryTokens[0]) {
                    case "name":
                        isSearchArtifactExists = apiProvider.isApiNameExist(queryTokens[1], organization) ||
                                apiProvider.isApiNameWithDifferentCaseExist(queryTokens[1], organization);
                        break;
                    case "context":
                    default:
                        isSearchArtifactExists = apiProvider.isContextExist(queryTokens[1], organization);
                        break;
                }
            } else {
                isSearchArtifactExists =
                        apiProvider.isApiNameExist(query, organization) ||
                                apiProvider.isApiNameWithDifferentCaseExist(query, organization);
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while checking the api existence", e, log);
        }
        return isSearchArtifactExists ? Response.status(Response.Status.OK).build() :
                Response.status(Response.Status.NOT_FOUND).build();
    }

    @Override
    public Response validateMCPServerDocument(String mcpServerId, String name, String ifMatch,
                                              MessageContext messageContext) throws APIManagementException {

        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(mcpServerId)) {
            RestApiUtil.handleBadRequest("MCP Server Id and/or document name should not be empty", log);
        }
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(mcpServerId);
            if (apiIdentifier == null) {
                throw new APIMgtResourceNotFoundException("Couldn't retrieve existing MCP Server with UUID: "
                        + mcpServerId, ExceptionCodes.from(ExceptionCodes.MCP_SERVER_NOT_FOUND, mcpServerId));
            }
            return apiProvider.isDocumentationExist(mcpServerId, name, organization) ?
                    Response.status(Response.Status.OK).build() : Response.status(Response.Status.NOT_FOUND).build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while checking the api existence", e, log);
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    /**
     * Validates the MCP server endpoint by sending an HTTP HEAD request.
     * Returns a response with the validation results.
     *
     * @param endpointUrl    The URL of the MCP server endpoint to validate.
     * @param mcpServerId    The ID of the MCP server.
     * @param messageContext Message context of the request.
     * @return Response containing the validation results or an error response if validation fails.
     * @throws APIManagementException if an error occurs during validation.
     */
    @Override
    public Response validateMCPServerEndpoint(String endpointUrl, String mcpServerId, MessageContext messageContext)
            throws APIManagementException {

        ApiEndpointValidationResponseDTO apiEndpointValidationResponseDTO = new ApiEndpointValidationResponseDTO();
        apiEndpointValidationResponseDTO.setError("");
        try {
            APIEndpointValidationDTO apiEndpointValidationDTO =
                    ApisApiServiceImplUtils.sendHttpHEADRequest(endpointUrl);
            apiEndpointValidationResponseDTO = APIMappingUtil.fromEndpointValidationToDTO(apiEndpointValidationDTO);
            return Response.status(Response.Status.OK).entity(apiEndpointValidationResponseDTO).build();
        } catch (MalformedURLException e) {
            log.error("Malformed Url error occurred while sending the HEAD request to the given endpoint url:", e);
            apiEndpointValidationResponseDTO.setError(e.getMessage());
        } catch (Exception e) {
            RestApiUtil.handleInternalServerError("Error while testing the validity of MCP Server endpoint url " +
                    "existence", e, log);
        }
        return Response.status(Response.Status.OK).entity(apiEndpointValidationResponseDTO).build();
    }

    /**
     * Validates the OpenAPI definition of an MCP server.
     * This method checks the validity of the OpenAPI definition and returns a response with validation results.
     *
     * @param returnContent       Whether to return the content of the OpenAPI definition.
     * @param url                 The URL of the OpenAPI definition.
     * @param fileInputStream     Input stream of the OpenAPI definition file.
     * @param fileDetail          Attachment details of the OpenAPI definition file.
     * @param inlineAPIDefinition Inline OpenAPI definition as a string.
     * @param messageContext      Message context of the request.
     * @return Response containing validation results or an error response if validation fails.
     * @throws APIManagementException if an error occurs during validation.
     */
    @Override
    public Response validateOpenAPIDefinitionOfMCPServer(Boolean returnContent, String url, InputStream fileInputStream,
                                                         Attachment fileDetail, String inlineAPIDefinition,
                                                         MessageContext messageContext) throws APIManagementException {

        // Validate and retrieve the OpenAPI definition
        Map validationResponseMap = null;
        try {
            validationResponseMap = RestApiPublisherUtils.validateOpenAPIDefinition(url, fileInputStream, fileDetail,
                    inlineAPIDefinition,
                    returnContent, false);
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error occurred while validating API Definition", e, log);
        }

        OpenAPIDefinitionValidationResponseDTO validationResponseDTO =
                (OpenAPIDefinitionValidationResponseDTO) validationResponseMap
                        .get(RestApiConstants.RETURN_DTO);
        if (!validationResponseDTO.isIsValid()) {
            List<ErrorListItemDTO> errors = validationResponseDTO.getErrors();
            for (ErrorListItemDTO error : errors) {
                log.error("Error while parsing OpenAPI definition. Error code: " + error.getCode() + ". Error: "
                        + error.getDescription());
            }
        }
        return Response.ok().entity(validationResponseDTO).build();
    }

    /**
     * Validate a third-party MCP server and return the DTO result.
     *
     * @param dto            Request payload with URL and optional security info
     * @param messageContext Request context
     * @return 200 OK with validation result DTO
     * @throws APIManagementException On unexpected internal errors
     */
    public Response validateThirdPartyMCPServer(MCPServerValidationRequestDTO dto, MessageContext messageContext)
            throws APIManagementException {

        if (dto == null) {
            RestApiUtil.handleBadRequest("Request body cannot be empty.", log);
        }
        final String serverUrl = StringUtils.trimToEmpty(dto.getUrl());
        if (StringUtils.isBlank(serverUrl)) {
            RestApiUtil.handleBadRequest("MCP server URL cannot be empty.", log);
        }

        final String organization = RestApiUtil.getValidatedOrganization(messageContext);
        SecurityInfoDTO securityInfo = dto.getSecurityInfo();
        final boolean isSecure = securityInfo != null && Boolean.TRUE.equals(securityInfo.isIsSecure());

        if (isSecure && !StringUtils.startsWithIgnoreCase(serverUrl, "https://")) {
            RestApiUtil.handleBadRequest("Secure validation requires an HTTPS URL.", log);
        }

        if (securityInfo != null) {
            String header = StringUtils.trimToNull(securityInfo.getHeader());
            String value = StringUtils.trimToNull(securityInfo.getValue());

            if ((header == null) != (value == null)) {
                RestApiUtil.handleBadRequest("Provide both security header and value together.", log);
            }

            if ((header != null && (header.contains("\r") || header.contains("\n"))) ||
                    (value != null && (value.contains("\r") || value.contains("\n")))) {
                RestApiUtil.handleBadRequest("Security header/value must not contain CR or LF characters.", log);
            }

            securityInfo.setHeader(header);
            securityInfo.setValue(value);
        }

        MCPServerValidationResponseDTO result =
                PublisherCommonUtils.validateMCPServer(serverUrl, securityInfo, true, organization);

        return Response.ok(result).build();
    }

    /**
     * Validates whether the API operations are permitted based on the lifecycle state.
     * Throws an exception if the operation is not allowed for the given lifecycle state.
     *
     * @param status Lifecycle status of the API.
     * @throws APIManagementException if the operation is not permitted for the given lifecycle state.
     */
    private void validateAPIOperationsPerLC(String status) throws APIManagementException {

        boolean updatePermittedForPublishedDeprecated = false;
        String[] tokenScopes =
                (String[]) PhaseInterceptorChain.getCurrentMessage().getExchange()
                        .get(RestApiConstants.USER_REST_API_SCOPES);
        for (String scope : tokenScopes) {
            if (RestApiConstants.MCP_SERVER_PUBLISHER_SCOPE.equals(scope)
                    || RestApiConstants.MCP_SERVER_IMPORT_EXPORT_SCOPE.equals(scope)
                    || RestApiConstants.MCP_SERVER_MANAGE_SCOPE.equals(scope)
                    || RestApiConstants.ADMIN_SCOPE.equals(scope)) {
                updatePermittedForPublishedDeprecated = true;
                break;
            }
        }
        if (!updatePermittedForPublishedDeprecated
                && (APIConstants.PUBLISHED.equals(status) || APIConstants.DEPRECATED.equals(status))) {
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.MCP_SERVER_UPDATE_FORBIDDEN_PER_LC, status));
        }
    }

    /**
     * Checks if the exception is due to an authorization failure.
     * This is determined by checking if the error message contains the unauthorized error message.
     *
     * @param e Exception to check.
     * @return true if the exception is an authorization failure, false otherwise.
     */
    private boolean isAuthorizationFailure(Exception e) {

        String errorMessage = e.getMessage();
        return errorMessage != null && errorMessage.contains(APIConstants.UN_AUTHORIZED_ERROR_MESSAGE);
    }

    /**
     * Retrieves API Lifecycle state information
     *
     * @param mcpServerId  API Id
     * @param organization organization
     * @return API Lifecycle state information
     */
    private LifecycleStateDTO getLifecycleState(String mcpServerId, String organization) throws APIManagementException {

        try {
            APIIdentifier mcpServerIdentifier;
            if (ApiMgtDAO.getInstance().checkAPIUUIDIsARevisionUUID(mcpServerId) != null) {
                mcpServerIdentifier = APIMappingUtil.getAPIInfoFromUUID(mcpServerId, organization).getId();
                mcpServerIdentifier.setUuid(mcpServerId);
            } else {
                mcpServerIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(mcpServerId);
            }
            if (mcpServerIdentifier == null) {
                throw new APIManagementException("Error while getting the api identifier for the MCP Server:" +
                        mcpServerId, ExceptionCodes.from(ExceptionCodes.INVALID_MCP_SERVER_ID, mcpServerId));
            }
            return PublisherCommonUtils.getLifecycleStateInformation(mcpServerIdentifier, organization);
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while deleting MCP Server : " + mcpServerId, e,
                        log);
            } else {
                throw e;
            }
        }
        return null;
    }

    /**
     * Creates a new {@link MCPServerDTO} instance based on an existing {@link API} and a specified new version.
     *
     * @param existingAPI the existing API object to copy basic details from
     * @param newVersion  the new version string to assign to the created MCPServerDTO
     * @return a new {@link MCPServerDTO} with name, context, and version set
     */
    private MCPServerDTO createMCPServerDTO(API existingAPI, String newVersion) {

        MCPServerDTO apidto = new MCPServerDTO();
        apidto.setName(existingAPI.getId().getApiName());
        apidto.setContext(existingAPI.getContextTemplate());
        apidto.setVersion(newVersion);
        return apidto;
    }
}
