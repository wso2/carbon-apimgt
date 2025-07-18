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
import org.json.simple.parser.ParseException;
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
import org.wso2.carbon.apimgt.api.model.BackendEndpoint;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DocumentationContent;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.OrganizationInfo;
import org.wso2.carbon.apimgt.api.model.ServiceEntry;
import org.wso2.carbon.apimgt.api.model.SubscribedAPI;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.governance.api.model.APIMGovernableState;
import org.wso2.carbon.apimgt.governance.api.model.ArtifactType;
import org.wso2.carbon.apimgt.impl.APIConstants;
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
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.DocumentationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.PublisherCommonUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionDeploymentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIRevisionListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APISubtypeConfigurationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ApiEndpointValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.BackendEndpointDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorListItemDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ImportAPIResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LifecycleStateDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OpenAPIDefinitionValidationResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OrganizationPoliciesDTO;
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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import static org.wso2.carbon.apimgt.api.ExceptionCodes.API_VERSION_ALREADY_EXISTS;

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
        query = query == null ? "type:MCP" : "type:MCP " + query;
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
     * Retrieves the MCP server API details for a given API UUID.
     * Validates that the API is of type MCP and applies organization-specific visibility and policy filters.
     *
     * @param apiId          the UUID of the API
     * @param xWSO2Tenant    the tenant identifier from request header
     * @param ifNoneMatch    the ETag header value for conditional requests
     * @param messageContext the message context of the request
     * @return a {@link Response} containing the MCP server API details
     * @throws APIManagementException if an error occurs during API retrieval or authorization
     */
    @Override
    public Response getMCPServer(String apiId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        OrganizationInfo organizationInfo = RestApiUtil.getOrganizationInfo(messageContext);
        APIDTO apiToReturn = null;
        try {
            API api = apiProvider.getAPIbyUUID(apiId, organization);
            if (APIConstants.API_TYPE_MCP.equals(api.getType())) {
                api.setOrganization(organization);
                apiToReturn = APIMappingUtil.fromAPItoDTO(api, apiProvider);
            } else {
                String errorMessage = "Error while retrieving MCP server : " + apiId + ". Incorrect API type. " +
                        "Expected type: " + APIConstants.API_TYPE_MCP + ", but found: " + api.getType();
                RestApiUtil.handleBadRequest(errorMessage, log);
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("User is not authorized to access the MCP server",
                        e, log);
            } else {
                String errorMessage = "Error while retrieving MCP server : " + apiId;
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
     * @param apiId
     * @param documentId
     * @param accept
     * @param ifNoneMatch
     * @param messageContext
     * @return
     * @throws APIManagementException
     */
    @Override
    public Response getMCPServerDocument(String apiId, String documentId, String accept, String ifNoneMatch,
                                         MessageContext messageContext) throws APIManagementException {

        Documentation documentation;
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);

            documentation = apiProvider.getDocumentation(apiId, documentId, organization);
            if (documentation == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_DOCUMENTATION, documentId, log);
            }

            DocumentDTO documentDTO = DocumentationMappingUtil.fromDocumentationToDTO(documentation);
            return Response.ok().entity(documentDTO).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("Authorization failure while retrieving document : "
                        + documentId + " of API " + apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving document : " + documentId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * @param apiId
     * @param documentId
     * @param accept
     * @param ifNoneMatch
     * @param messageContext
     * @return
     * @throws APIManagementException
     */
    @Override
    public Response getMCPServerDocumentContent(String apiId, String documentId, String accept, String ifNoneMatch,
                                                MessageContext messageContext) throws APIManagementException {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);

            DocumentationContent docContent = apiProvider.getDocumentationContent(apiId, documentId, organization);
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
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while retrieving document : " + documentId + " of API " + apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving document " + documentId + " of the API " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving source URI location of " + documentId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * @param apiId
     * @param limit
     * @param offset
     * @param accept
     * @param ifNoneMatch
     * @param messageContext
     * @return
     * @throws APIManagementException
     */
    @Override
    public Response getMCPServerDocuments(String apiId, Integer limit, Integer offset, String accept,
                                          String ifNoneMatch, MessageContext messageContext)
            throws APIManagementException {

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            List<Documentation> allDocumentation = apiProvider.getAllDocumentation(apiId, organization);
            DocumentListDTO documentListDTO = DocumentationMappingUtil.fromDocumentationListToDTO(allDocumentation,
                    offset, limit);
            DocumentationMappingUtil
                    .setPaginationParams(documentListDTO, apiId, offset, limit, allDocumentation.size());
            return Response.ok().entity(documentListDTO).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while retrieving documents of API : " + apiId, e, log);
            } else {
                String msg = "Error while retrieving documents of API " + apiId;
                RestApiUtil.handleInternalServerError(msg, e, log);
            }
        }
        return null;
    }

    /**
     * Retrieves a specific MCP server endpoint for the given API.
     *
     * @param apiId          UUID of the API.
     * @param endpointId     UUID of the backend endpoint.
     * @param messageContext Message context with request details.
     * @return HTTP Response containing the backend endpoint DTO.
     * @throws APIManagementException if an error occurs while retrieving the endpoint.
     */
    @Override
    public Response getMCPServerEndpoint(String apiId, String endpointId, MessageContext messageContext)
            throws APIManagementException {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            CommonUtils.validateAPIExistence(apiId);

            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            BackendEndpoint backendEndpoint = apiProvider.getMCPServerEndpoint(apiId, endpointId);
            BackendEndpointDTO backendEndpointDTO = APIMappingUtil.fromBackendEndpointToDTO(backendEndpoint,
                    organization, false);
            return Response.ok().entity(backendEndpointDTO).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while retrieving resource paths of API : " + apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving endpoint of API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * Retrieves all MCP server endpoints for the given API.
     *
     * @param apiId          UUID of the API.
     * @param messageContext Message context with request details.
     * @return HTTP Response containing a list of backend endpoint DTOs.
     * @throws APIManagementException if an error occurs while retrieving the endpoints.
     */
    @Override
    public Response getMCPServerEndpoints(String apiId, MessageContext messageContext) throws APIManagementException {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            CommonUtils.validateAPIExistence(apiId);
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            List<BackendEndpoint> backendEndpointList = apiProvider.getMCPServerEndpoints(apiId);
            List<BackendEndpointDTO> backendEndpointDTOList = new ArrayList<>();
            for (BackendEndpoint endpoint : backendEndpointList) {
                try {
                    BackendEndpointDTO dto = APIMappingUtil.fromBackendEndpointToDTO(endpoint, organization, false);
                    backendEndpointDTOList.add(dto);
                } catch (APIManagementException e) {
                    String errorMessage = "Error while mapping backend endpoint to DTO for MCP Server: " + apiId;
                    RestApiUtil.handleInternalServerError(errorMessage, e, log);
                }
            }
            return Response.ok().entity(backendEndpointDTOList).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while retrieving endpoints of MCP server: " + apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving endpoints of MCP server: " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * @param apiId
     * @param ifNoneMatch
     * @param messageContext
     * @return
     * @throws APIManagementException
     */
    @Override
    public Response getMCPServerLifecycleHistory(String apiId, String ifNoneMatch, MessageContext messageContext)
            throws APIManagementException {

        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            API api;
            APIRevision apiRevision = apiProvider.checkAPIUUIDIsARevisionUUID(apiId);
            if (apiRevision != null && apiRevision.getApiUUID() != null) {
                api = apiProvider.getAPIbyUUID(apiRevision.getApiUUID(), organization);
            } else {
                api = apiProvider.getAPIbyUUID(apiId, organization);
            }
            return Response.ok().entity(PublisherCommonUtils.getLifecycleHistoryDTO(api.getUuid(), apiProvider))
                    .build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("Authorization failure while retrieving the lifecycle "
                        + "events of API : " + apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving the lifecycle events of API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * @param apiId
     * @param ifNoneMatch
     * @param messageContext
     * @return
     * @throws APIManagementException
     */
    @Override
    public Response getMCPServerLifecycleState(String apiId, String ifNoneMatch, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        LifecycleStateDTO lifecycleStateDTO = getLifecycleState(apiId, organization);
        return Response.ok().entity(lifecycleStateDTO).build();
    }

    /**
     * Deletes a specific MCP server revision.
     *
     * @param apiId          UUID of the API.
     * @param revisionId     UUID of the revision to be deleted.
     * @param messageContext Message context with request details.
     * @return HTTP Response indicating the result of the deletion operation.
     * @throws APIManagementException if an error occurs while deleting the revision.
     */
    @Override
    public Response getMCPServerRevision(String apiId, String revisionId, MessageContext messageContext)
            throws APIManagementException {

        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }

    /**
     * Retrieves all revisions of a specific MCP server.
     *
     * @param apiId          UUID of the API.
     * @param query          Query string to filter revisions.
     * @param messageContext Message context with request details.
     * @return HTTP Response containing a list of APIRevisionListDTO objects.
     * @throws APIManagementException if an error occurs while retrieving the revisions.
     */
    @Override
    public Response getMCPServerRevisions(String apiId, String query, MessageContext messageContext)
            throws APIManagementException {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            APIRevisionListDTO apiRevisionListDTO;
            List<APIRevision> apiRevisions = apiProvider.getAPIRevisions(apiId);
            List<APIRevision> apiRevisionsList = ApisApiServiceImplUtils.filterAPIRevisionsByDeploymentStatus(query,
                    apiRevisions);
            apiRevisionListDTO = APIMappingUtil.fromListAPIRevisiontoDTO(apiRevisionsList);
            return Response.ok().entity(apiRevisionListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage =
                    "Error while retrieving API Revision for MCP server: " + apiId + " - " + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * @param apiId
     * @param xWSO2Tenant
     * @param ifNoneMatch
     * @param isAiApi
     * @param organizationID
     * @param messageContext
     * @return
     * @throws APIManagementException
     */
    @Override
    public Response getMCPServerSubscriptionPolicies(String apiId, String xWSO2Tenant, String ifNoneMatch,
                                                     Boolean isAiApi, String organizationID,
                                                     MessageContext messageContext) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        API api = apiProvider.getAPIbyUUID(apiId, organization);
        api.setOrganization(organization);
        APIDTO apiInfo = APIMappingUtil.fromAPItoDTO(api, apiProvider);
        List<Tier> availableThrottlingPolicyList = new ThrottlingPoliciesApiServiceImpl().getThrottlingPolicyList(
                ThrottlingPolicyDTO.PolicyLevelEnum.SUBSCRIPTION.toString(), true, isAiApi);

        if (apiInfo != null) {
            List<String> apiPolicies =
                    RestApiPublisherUtils.getSubscriptionPoliciesForOrganization(apiInfo, organizationID);
            List<Tier> apiThrottlingPolicies = ApisApiServiceImplUtils.filterAPIThrottlingPolicies(apiPolicies,
                    availableThrottlingPolicyList);
            return Response.ok().entity(apiThrottlingPolicies).build();
        }
        return null;
    }

    /**
     * @param apiId
     * @param accept
     * @param ifNoneMatch
     * @param messageContext
     * @return
     * @throws APIManagementException
     */
    @Override
    public Response getMCPServerSwagger(String apiId, String accept, String ifNoneMatch, MessageContext messageContext)
            throws APIManagementException {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            //this will fail if user does not have access to the API or the API does not exist
            API api = apiProvider.getAPIbyUUID(apiId, organization);
            api.setOrganization(organization);
            String updatedDefinition = RestApiCommonUtil.retrieveSwaggerDefinition(apiId, api, apiProvider);
            return Response.ok().entity(updatedDefinition).header("Content-Disposition",
                    "attachment; filename=\"" + "swagger.json" + "\"").build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil
                        .handleAuthorizationFailure("Authorization failure while retrieving swagger of API :"
                                + apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving swagger of API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * @param fileInputStream
     * @param fileDetail
     * @param preserveProvider
     * @param rotateRevision
     * @param overwrite
     * @param preservePortalConfigurations
     * @param dryRun
     * @param accept
     * @param messageContext
     * @return
     * @throws APIManagementException
     */
    @Override
    public Response importMCPServer(InputStream fileInputStream, Attachment fileDetail, Boolean preserveProvider,
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

        if (dryRun) {
            String dryRunResults = PublisherCommonUtils
                    .checkGovernanceComplianceDryRun(fileInputStream, organization);
            return Response.ok(dryRunResults, MediaType.APPLICATION_JSON).build();
        }
        ImportedAPIDTO
                importedAPIDTO = importExportAPI.importAPI(fileInputStream, preserveProvider, rotateRevision, overwrite,
                preservePortalConfigurations, tokenScopes, organization);
        if (RestApiConstants.APPLICATION_JSON.equals(accept) && importedAPIDTO != null) {
            ImportAPIResponseDTO responseDTO = new ImportAPIResponseDTO().id(importedAPIDTO.getApi().getUuid())
                    .revision(importedAPIDTO.getRevision());
            return Response.ok().entity(responseDTO).build();
        }
        return Response.status(Response.Status.OK).entity("API imported successfully.").build();
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
     * @return Response containing the created APIDTO or an error response
     * @throws APIManagementException if an error occurs during import or validation
     */
    public Response importMCPServerDefinition(InputStream fileInputStream, Attachment fileDetail, String url,
                                              String additionalProperties, MessageContext messageContext)
            throws APIManagementException {

        if (StringUtils.isBlank(additionalProperties)) {
            throw new APIManagementException("'additionalProperties' is required and should not be null",
                    ExceptionCodes.ADDITIONAL_PROPERTIES_CANNOT_BE_NULL);
        }
        ObjectMapper objectMapper = new ObjectMapper();
        APIDTO apiDTOFromProperties;
        try {
            apiDTOFromProperties = objectMapper.readValue(additionalProperties, APIDTO.class);
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
        populateDefaultValuesForMCPServer(apiDTOFromProperties, APIConstants.API_SUBTYPE_DIRECT_ENDPOINT);
        if (!PublisherCommonUtils.validateEndpoints(apiDTOFromProperties)) {
            throw new APIManagementException("Invalid/Malformed endpoint URL(s) detected",
                    ExceptionCodes.INVALID_ENDPOINT_URL);
        }
        try {
            LinkedHashMap endpointConfig = (LinkedHashMap) apiDTOFromProperties.getEndpointConfig();
            PublisherCommonUtils
                    .encryptEndpointSecurityOAuthCredentials(endpointConfig, CryptoUtil.getDefaultCryptoUtil(),
                            StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY,
                            apiDTOFromProperties);
            PublisherCommonUtils
                    .encryptEndpointSecurityApiKeyCredentials(endpointConfig, CryptoUtil.getDefaultCryptoUtil(),
                            StringUtils.EMPTY, StringUtils.EMPTY, apiDTOFromProperties);

            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIDTO createdApiDTO = RestApiPublisherUtils.importOpenAPIDefinition(fileInputStream, url, null,
                    apiDTOFromProperties, fileDetail, null, organization);
            if (createdApiDTO != null) {
                URI createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + createdApiDTO.getId());
                return Response.created(createdApiUri).entity(createdApiDTO).build();
            }
        } catch (URISyntaxException e) {
            String errorMessage =
                    "Error while retrieving MCP server location: " + apiDTOFromProperties.getProvider() + "-" +
                            apiDTOFromProperties.getName() + "-" + apiDTOFromProperties.getVersion();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (CryptoException e) {
            String errorMessage =
                    "Error while encrypting the secret key of MCP server: " + apiDTOFromProperties.getProvider() + "-"
                            + apiDTOFromProperties.getName() + "-" + apiDTOFromProperties.getVersion();
            throw new APIManagementException(errorMessage, e,
                    ExceptionCodes.from(ExceptionCodes.ENDPOINT_SECURITY_CRYPTO_EXCEPTION, errorMessage));
        } catch (ParseException e) {
            String errorMessage = "Error while parsing the endpoint configuration of MCP server: "
                    + apiDTOFromProperties.getProvider() + "-" + apiDTOFromProperties.getName() + "-"
                    + apiDTOFromProperties.getVersion();
            throw new APIManagementException(errorMessage, e);
        }
        return null;
    }

    private void populateDefaultValuesForMCPServer(APIDTO apiDTOFromProperties, String subtype) {

        apiDTOFromProperties.setType(APIDTO.TypeEnum.MCP);
        APISubtypeConfigurationDTO subtypeConfiguration = new APISubtypeConfigurationDTO();
        subtypeConfiguration.setSubtype(subtype);
        apiDTOFromProperties.setSubtypeConfiguration(subtypeConfiguration);
    }

    /**
     * Restores an MCP server API revision to the specified API.
     *
     * @param apiId          UUID of the API to which the revision should be restored.
     * @param revisionId     UUID of the revision to restore.
     * @param messageContext Message context with request details.
     * @return HTTP Response containing the restored API DTO.
     * @throws APIManagementException if an error occurs while restoring the revision.
     */
    @Override
    public Response restoreMCPServerRevision(String apiId, String revisionId, MessageContext messageContext)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APIInfo apiInfo = CommonUtils.validateAPIExistence(apiId);
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        apiProvider.restoreAPIRevision(apiId, revisionId, organization);

        API api = apiProvider.getAPIbyUUID(apiId, organization);
        api.setOrganization(organization);
        APIDTO apiToReturn = APIMappingUtil.fromAPItoDTO(api, apiProvider);

        Response.Status status = Response.Status.CREATED;
        return Response.status(status).entity(apiToReturn).build();
    }

    /**
     * Undeploys a specific revision of an MCP server.
     *
     * @param apiId                        UUID of the API.
     * @param revisionId                   UUID of the revision to be undeployed.
     * @param revisionNumber               Revision number for the API.
     * @param allEnvironments              Flag indicating whether to undeploy from all environments.
     * @param apIRevisionDeploymentDTOList List of APIRevisionDeploymentDTO objects for undeployment.
     * @param messageContext               Message context of the request.
     * @return HTTP Response containing the updated list of APIRevisionDeploymentDTO objects or an error response.
     * @throws APIManagementException if an error occurs while undeploying the revision.
     */
    @Override
    public Response undeployMCPServerRevision(String apiId, String revisionId, String revisionNumber,
                                              Boolean allEnvironments,
                                              List<APIRevisionDeploymentDTO> apIRevisionDeploymentDTOList,
                                              MessageContext messageContext)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        APIInfo apiInfo = CommonUtils.validateAPIExistence(apiId);
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        if (revisionId == null && revisionNumber != null) {
            revisionId = apiProvider.getAPIRevisionUUID(revisionNumber, apiId);
            if (revisionId == null) {
                throw new APIManagementException(
                        "No revision found for revision number " + revisionNumber + " of MCP server with UUID " + apiId,
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
        apiProvider.undeployAPIRevisionDeployment(apiId, revisionId, apiRevisionDeployments, organization);
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
     * @param apiId
     * @param documentDTO
     * @param messageContext
     * @return
     * @throws APIManagementException
     */
    @Override
    public Response addMCPServerDocument(String apiId, DocumentDTO documentDTO, MessageContext messageContext)
            throws APIManagementException {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            //validate if api exists
            APIInfo apiInfo = CommonUtils.validateAPIExistence(apiId);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(apiInfo.getStatus().toString());
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            Documentation documentation = PublisherCommonUtils.addDocumentationToAPI(documentDTO, apiId, organization);
            DocumentDTO newDocumentDTO = DocumentationMappingUtil.fromDocumentationToDTO(documentation);
            String uriString = RestApiConstants.RESOURCE_PATH_DOCUMENTS_DOCUMENT_ID
                    .replace(RestApiConstants.APIID_PARAM, apiId)
                    .replace(RestApiConstants.DOCUMENTID_PARAM, documentation.getId());
            URI uri = new URI(uriString);
            return Response.created(uri).entity(newDocumentDTO).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil
                        .handleAuthorizationFailure("Authorization failure while adding documents of API : " + apiId, e,
                                log);
            } else {
                throw new APIManagementException(
                        "Error while adding a new document to API " + apiId + " : " + e.getMessage(), e);
            }
        } catch (URISyntaxException e) {
            String errorMessage =
                    "Error while retrieving location for document " + documentDTO.getName() + " of API " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * @param apiId
     * @param documentId
     * @param ifMatch
     * @param fileInputStream
     * @param fileDetail
     * @param inlineContent
     * @param messageContext
     * @return
     * @throws APIManagementException
     */
    @Override
    public Response addMCPServerDocumentContent(String apiId, String documentId, String ifMatch,
                                                InputStream fileInputStream, Attachment fileDetail,
                                                String inlineContent, MessageContext messageContext)
            throws APIManagementException {

        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            //validate if api exists
            APIInfo apiInfo = CommonUtils.validateAPIExistence(apiId);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(apiInfo.getStatus().toString());
            if (fileInputStream != null && inlineContent != null) {
                RestApiUtil.handleBadRequest("Only one of 'file' and 'inlineContent' should be specified", log);
            }

            //retrieves the document and send 404 if not found
            Documentation documentation = apiProvider.getDocumentation(apiId, documentId, organization);
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
                    RestApiPublisherUtils.attachFileToDocument(apiId, documentation, fileInputStream, fileDetail,
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
                        .addDocumentationContent(documentation, apiProvider, apiId, documentId, organization,
                                inlineContent);
            } else {
                RestApiUtil.handleBadRequest("Either 'file' or 'inlineContent' should be specified", log);
            }

            //retrieving the updated doc and the URI
            Documentation updatedDoc = apiProvider.getDocumentation(apiId, documentId, organization);
            DocumentDTO documentDTO = DocumentationMappingUtil.fromDocumentationToDTO(updatedDoc);
            String uriString = RestApiConstants.RESOURCE_PATH_DOCUMENT_CONTENT
                    .replace(RestApiConstants.APIID_PARAM, apiId)
                    .replace(RestApiConstants.DOCUMENTID_PARAM, documentId);
            URI uri = new URI(uriString);
            return Response.created(uri).entity(documentDTO).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while adding content to the document: " + documentId + " of API "
                                + apiId, e, log);
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
     * @param action
     * @param apiId
     * @param lifecycleChecklist
     * @param ifMatch
     * @param messageContext
     * @return
     * @throws APIManagementException
     */
    @Override
    public Response changeMCPServerLifecycle(String action, String apiId, String lifecycleChecklist, String ifMatch,
                                             MessageContext messageContext) throws APIManagementException {

        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            ApiTypeWrapper apiWrapper = new ApiTypeWrapper(apiProvider.getAPIbyUUID(apiId, organization));
            APIStateChangeResponse stateChangeResponse = PublisherCommonUtils.changeApiOrApiProductLifecycle(action,
                    apiWrapper, lifecycleChecklist, organization);

            //returns the current lifecycle state
            LifecycleStateDTO stateDTO = getLifecycleState(apiId, organization); // todo try to prevent this call

            WorkflowResponseDTO workflowResponseDTO = APIMappingUtil
                    .toWorkflowResponseDTO(stateDTO, stateChangeResponse);
            return Response.ok().entity(workflowResponseDTO).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while updating the lifecycle of API " + apiId, e, log);
            } else {
                throw e;
            }
        }
        return null;
    }

    /**
     * @param body
     * @param openAPIVersion
     * @param messageContext
     * @return
     * @throws APIManagementException
     */
    @Override
    public Response createMCPServer(APIDTO body, String openAPIVersion, MessageContext messageContext)
            throws APIManagementException {

        URI createdApiUri;
        APIDTO createdApiDTO;
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            OrganizationInfo orgInfo = RestApiUtil.getOrganizationInfo(messageContext);
            populateDefaultValuesForMCPServer(body, body.getSubtypeConfiguration().getSubtype());
            API createdApi = PublisherCommonUtils
                    .addAPIWithGeneratedSwaggerDefinition(body, openAPIVersion, RestApiCommonUtil.getLoggedInUsername(),
                            organization, orgInfo);
            createdApiDTO = APIMappingUtil.fromAPItoDTO(createdApi);
            createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + createdApiDTO.getId());
            return Response.created(createdApiUri).entity(createdApiDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving API location : " + body.getProvider() + "-" +
                    body.getName() + "-" + body.getVersion();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (CryptoException e) {
            String errorMessage = "Error while encrypting the secret key of API : " + body.getProvider() + "-" +
                    body.getName() + "-" + body.getVersion() + " - " + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (ParseException e) {
            String errorMessage = "Error while parsing the endpoint configuration of API : " + body.getProvider() +
                    "-" + body.getName() + "-" + body.getVersion() + " - " + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Creates a new revision for an MCP server.
     * Validates the API existence, checks governance compliance, and adds the new revision.
     *
     * @param apiId          UUID of the API to create a revision for.
     * @param apIRevisionDTO DTO containing the revision details.
     * @param messageContext Message context of the request.
     * @return HTTP Response containing the created APIRevisionDTO or an error response.
     * @throws APIManagementException if an error occurs while creating the revision.
     */
    @Override
    public Response createMCPServerRevision(String apiId, APIRevisionDTO apIRevisionDTO, MessageContext messageContext)
            throws APIManagementException {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);

            APIInfo apiInfo = CommonUtils.validateAPIExistence(apiId);
            API api = apiProvider.getAPIbyUUID(apiId, organization);
            api.setOrganization(organization);
            APIDTO apiDto = APIMappingUtil.fromAPItoDTO(api, apiProvider);

            if (apiDto != null && apiDto.getAdvertiseInfo() != null && apiDto.getAdvertiseInfo().isAdvertised()) {
                throw new APIManagementException(
                        "Creating API Revisions is not supported for third party APIs: " + apiId,
                        ExceptionCodes.from(ExceptionCodes.THIRD_PARTY_API_REVISION_CREATION_UNSUPPORTED, apiId));
            }
            validateAPIOperationsPerLC(apiInfo.getStatus().toString());
            APIRevision apiRevision = new APIRevision();
            apiRevision.setApiUUID(apiId);
            apiRevision.setDescription(apIRevisionDTO.getDescription());
            Map<String, String> complianceResult = PublisherCommonUtils.checkGovernanceComplianceSync(apiId,
                    APIMGovernableState.API_DEPLOY, ArtifactType.API, organization, null, null);

            if (!complianceResult.isEmpty()
                    && complianceResult.get(APIConstants.GOVERNANCE_COMPLIANCE_KEY) != null
                    && !Boolean.parseBoolean(complianceResult.get(APIConstants.GOVERNANCE_COMPLIANCE_KEY))) {
                throw new APIComplianceException(
                        complianceResult.get(APIConstants.GOVERNANCE_COMPLIANCE_ERROR_MESSAGE));
            }
            String revisionId = apiProvider.addAPIRevision(apiRevision, organization);
            APIRevision createdApiRevision = apiProvider.getAPIRevision(revisionId);
            APIRevisionDTO createdApiRevisionDTO = APIMappingUtil.fromAPIRevisiontoDTO(createdApiRevision);
            URI createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS
                    + "/" + createdApiRevisionDTO.getApiInfo().getId() + "/"
                    + RestApiConstants.RESOURCE_PATH_REVISIONS + "/" + createdApiRevisionDTO.getId());
            PublisherCommonUtils.checkGovernanceComplianceAsync(apiId, APIMGovernableState.API_DEPLOY,
                    ArtifactType.API, organization);
            return Response.created(createdApiUri).entity(createdApiRevisionDTO).build();
        } catch (APIManagementException e) {
            if (e instanceof APIComplianceException) {
                throw e;
            }
            String errorMessage = "Error while adding new revision for MCP server: " + apiId;
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
                    + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * @param newVersion
     * @param apiId
     * @param defaultVersion
     * @param serviceVersion
     * @param messageContext
     * @return
     * @throws APIManagementException
     */
    @Override
    public Response createNewMCPServerVersion(String newVersion, String apiId, Boolean defaultVersion,
                                              String serviceVersion, MessageContext messageContext)
            throws APIManagementException {

        URI newVersionedApiUri;
        APIDTO newVersionedApi = new APIDTO();
        ServiceEntry service = new ServiceEntry();
        try {
            APIIdentifier apiIdentifierFromTable = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
            if (apiIdentifierFromTable == null) {
                throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API with API UUID: "
                        + apiId, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND,
                        apiId));
            }
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            int tenantId = APIUtil.getInternalOrganizationId(organization);
            API existingAPI = apiProvider.getAPIbyUUID(apiId, organization);
            if (existingAPI == null) {
                throw new APIMgtResourceNotFoundException("API not found for id " + apiId,
                        ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND, apiId));
            }
            //Get all existing versions of API
            Set<String> apiVersions = apiProvider.getAPIVersions(apiIdentifierFromTable.getProviderName(),
                    apiIdentifierFromTable.getApiName(), organization);
            if (apiVersions.contains(newVersion)) {
                throw new APIMgtResourceAlreadyExistsException(
                        "Version " + newVersion + " exists for api " + existingAPI.getId().getApiName(),
                        ExceptionCodes.from(API_VERSION_ALREADY_EXISTS, newVersion, existingAPI.getId().getApiName()));
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
                APIDTO apidto = createAPIDTO(existingAPI, newVersion);
                if (ServiceEntry.DefinitionType.OAS2.equals(service.getDefinitionType()) || ServiceEntry
                        .DefinitionType.OAS3.equals(service.getDefinitionType())) {
                    newVersionedApi =
                            RestApiPublisherUtils.importOpenAPIDefinition(service.getEndpointDef(), null, null, apidto,
                                    null, service, organization);
                }
            } else {
                API versionedAPI = apiProvider.createNewAPIVersion(apiId, newVersion, defaultVersion, organization);
                newVersionedApi = APIMappingUtil.fromAPItoDTO(versionedAPI);
            }
            //This URI used to set the location header of the POST response
            newVersionedApiUri =
                    new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + newVersionedApi.getId());
            PublisherCommonUtils.checkGovernanceComplianceAsync(newVersionedApi.getId(), APIMGovernableState.API_CREATE,
                    ArtifactType.API, organization);
            return Response.created(newVersionedApiUri).entity(newVersionedApi).build();
        } catch (APIManagementException e) {
            if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("Authorization failure while copying API : " + apiId, e, log);
            } else {
                throw e;
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving API location of " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Deletes a specific MCP server by its UUID.
     * Validates the API existence, checks for active subscriptions, and deletes the API.
     *
     * @param apiId          UUID of the API to be deleted.
     * @param ifMatch        ETag value for optimistic concurrency control.
     * @param messageContext Message context of the request.
     * @return HTTP Response indicating the result of the deletion operation.
     * @throws APIManagementException if an error occurs while deleting the MCP server.
     */
    @Override
    public Response deleteMCPServer(String apiId, String ifMatch, MessageContext messageContext)
            throws APIManagementException {

        try {
            String username = RestApiCommonUtil.getLoggedInUsername();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIProvider apiProvider = RestApiCommonUtil.getProvider(username);

            boolean isAPIExistDB = false;
            APIManagementException error = null;
            APIInfo apiInfo = null;
            try {
                apiInfo = CommonUtils.validateAPIExistence(apiId);
                if (APIConstants.API_TYPE_MCP.equals(apiInfo.getApiType())) {
                    isAPIExistDB = true;
                } else {
                    String errorMessage = "Error while validating MCP server existence for deleting API : " + apiId +
                            " on organization " + organization + ". API is not of type MCP";
                    RestApiUtil.handleBadRequest(errorMessage, log);
                }
            } catch (APIManagementException e) {
                log.error("Error while validating API existence for deleting MCP server " + apiId + " on organization "
                        + organization);
                error = e;
            }

            if (isAPIExistDB) {
                validateAPIOperationsPerLC(apiInfo.getStatus().toString());
                try {
                    List<SubscribedAPI> apiUsages = apiProvider.getAPIUsageByAPIId(apiId, organization);
                    if (apiUsages != null && !apiUsages.isEmpty()) {
                        List<SubscribedAPI> filteredUsages = new ArrayList<>();
                        for (SubscribedAPI usage : apiUsages) {
                            String subsCreatedStatus = usage.getSubCreatedStatus();
                            if (!APIConstants.SubscriptionCreatedStatus.UN_SUBSCRIBE.equals(subsCreatedStatus)) {
                                filteredUsages.add(usage);
                            }
                        }
                        if (!filteredUsages.isEmpty()) {
                            RestApiUtil.handleConflict("Cannot remove the MCP server: " + apiId
                                    + " as active subscriptions exist", log);
                        }
                    }
                } catch (APIManagementException e) {
                    log.error("Error while checking active subscriptions for deleting MCP server "
                            + apiId + " on organization " + organization);
                    error = e;
                }
            }
            boolean isDeleted = false;
            try {
                apiProvider.deleteAPI(apiId, organization);
                isDeleted = true;
            } catch (APIManagementException e) {
                log.error("Error while deleting MCP server: " + apiId + "on organization " + organization, e);
            }

            if (error != null) {
                throw error;
            } else if (!isDeleted) {
                RestApiUtil.handleInternalServerError("Error while deleting MCP server: " + apiId
                        + " on organization " + organization, log);
                return null;
            }
            PublisherCommonUtils.clearArtifactComplianceInfo(apiId, RestApiConstants.RESOURCE_MCP, organization);
            return Response.ok().build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("Authorization failure while deleting MCP server: "
                        + apiId, e, log);
            } else {
                String errorMessage = "Error while deleting MCP server : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * @param apiId
     * @param documentId
     * @param ifMatch
     * @param messageContext
     * @return
     * @throws APIManagementException
     */
    @Override
    public Response deleteMCPServerDocument(String apiId, String documentId, String ifMatch,
                                            MessageContext messageContext) throws APIManagementException {

        Documentation documentation;
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);

            //validate if api exists
            APIInfo apiInfo = CommonUtils.validateAPIExistence(apiId);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(apiInfo.getStatus().toString());

            //this will fail if user does not have access to the API or the API does not exist
            //APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, organization);
            documentation = apiProvider.getDocumentation(apiId, documentId, organization);
            if (documentation == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_DOCUMENTATION, documentId, log);
            }
            apiProvider.removeDocumentation(apiId, documentId, organization);
            return Response.ok().build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while deleting : " + documentId + " of API " + apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * Deletes a specific revision of an MCP server.
     * Validates the API existence, checks governance compliance, and deletes the revision.
     *
     * @param apiId          UUID of the API.
     * @param revisionId     UUID of the revision to be deleted.
     * @param messageContext Message context of the request.
     * @return HTTP Response containing the updated list of APIRevisionDTO objects or an error response.
     * @throws APIManagementException if an error occurs while deleting the revision.
     */
    @Override
    public Response deleteMCPServerRevision(String apiId, String revisionId, MessageContext messageContext)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APIInfo apiInfo = CommonUtils.validateAPIExistence(apiId);
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        apiProvider.deleteAPIRevision(apiId, revisionId, organization);
        List<APIRevision> apiRevisions = apiProvider.getAPIRevisions(apiId);
        APIRevisionListDTO apiRevisionListDTO = APIMappingUtil.fromListAPIRevisiontoDTO(apiRevisions);
        return Response.ok().entity(apiRevisionListDTO).build();
    }

    /**
     * Deploys a specific revision of an MCP server to the specified environments.
     * Validates the API existence, checks governance compliance, and deploys the revision.
     *
     * @param apiId                        UUID of the API.
     * @param revisionId                   UUID of the revision to be deployed.
     * @param apIRevisionDeploymentDTOList List of APIRevisionDeploymentDTO objects for deployment.
     * @param messageContext               Message context of the request.
     * @return HTTP Response containing the updated list of APIRevisionDeploymentDTO objects or an error response.
     * @throws APIManagementException if an error occurs while deploying the revision.
     */
    @Override
    public Response deployMCPServerRevision(String apiId, String revisionId,
                                            List<APIRevisionDeploymentDTO> apIRevisionDeploymentDTOList,
                                            MessageContext messageContext)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        if (revisionId.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Revision Id is not provided").build();
        }
        APIInfo apiInfo = CommonUtils.validateAPIExistence(apiId);
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        API api = apiProvider.getAPIbyUUID(apiId, organization);
        api.setOrganization(organization);
        APIDTO apiDto = APIMappingUtil.fromAPItoDTO(api, apiProvider);

        if (apiDto.getLifeCycleStatus().equals(APIConstants.RETIRED)) {
            String errorMessage = "Deploying MCP server revisions is not supported for retired APIs. ApiId: " + apiId;
            throw new APIManagementException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.RETIRED_API_REVISION_DEPLOYMENT_UNSUPPORTED, apiId));
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
        Map<String, String> complianceResult = PublisherCommonUtils.checkGovernanceComplianceSync(apiId,
                APIMGovernableState.API_DEPLOY, ArtifactType.API, organization, null, null);
        if (!complianceResult.isEmpty()
                && complianceResult.get(APIConstants.GOVERNANCE_COMPLIANCE_KEY) != null
                && !Boolean.parseBoolean(complianceResult.get(APIConstants.GOVERNANCE_COMPLIANCE_KEY))) {
            throw new APIComplianceException(complianceResult.get(APIConstants.GOVERNANCE_COMPLIANCE_ERROR_MESSAGE));
        }
        apiProvider.deployAPIRevision(apiId, revisionId, apiRevisionDeployments, organization);
        List<APIRevisionDeployment> apiRevisionDeploymentsResponse = apiProvider.getAPIRevisionsDeploymentList(apiId);
        List<APIRevisionDeploymentDTO> apiRevisionDeploymentDTOS = new ArrayList<>();
        for (APIRevisionDeployment apiRevisionDeployment : apiRevisionDeploymentsResponse) {
            apiRevisionDeploymentDTOS.add(APIMappingUtil.fromAPIRevisionDeploymenttoDTO(apiRevisionDeployment));
        }
        Response.Status status = Response.Status.CREATED;
        PublisherCommonUtils.checkGovernanceComplianceAsync(apiId, APIMGovernableState.API_DEPLOY,
                ArtifactType.API, organization);
        return Response.status(status).entity(apiRevisionDeploymentDTOS).build();
    }

    /**
     * @param apiId
     * @param name
     * @param version
     * @param revisionNumber
     * @param providerName
     * @param format
     * @param preserveStatus
     * @param latestRevision
     * @param gatewayEnvironment
     * @param preserveCredentials
     * @param messageContext
     * @return
     * @throws APIManagementException
     */
    @Override
    public Response exportMCPServer(String apiId, String name, String version, String revisionNumber,
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
                        .exportAPI(apiId, name, version, revisionNumber, providerName, preserveStatus, exportFormat,
                                Boolean.TRUE, preserveCredentials, latestRevision, StringUtils.EMPTY,
                                organization);
                return Response.ok(file).header(RestApiConstants.HEADER_CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getName() + "\"").build();
            } catch (APIImportExportException e) {
                throw new APIManagementException("Error while exporting " + RestApiConstants.RESOURCE_MCP, e);
            }
        } else {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            if (StringUtils.isEmpty(apiId) && (StringUtils.isNotEmpty(name) && StringUtils.isNotEmpty(version))) {
                APIIdentifier apiIdentifier = new APIIdentifier(providerName, name, version);
                apiId = APIUtil.getUUIDFromIdentifier(apiIdentifier, organization);
                if (StringUtils.isEmpty(apiId)) {
                    throw new APIManagementException("API not found for the given name: " + name + ", and version : "
                            + version, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND, name + "-" + version));
                }
            }
            RuntimeArtifactDto runtimeArtifactDto = null;
            if (StringUtils.isNotEmpty(organization)) {
                runtimeArtifactDto = RuntimeArtifactGeneratorUtil.generateRuntimeArtifact(apiId, gatewayEnvironment,
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
            throw new APIManagementException("No API Artifacts", ExceptionCodes.NO_API_ARTIFACT_FOUND);
        }
    }

    /**
     * Updates an existing MCP server with the provided APIDTO.
     * Validates the API existence, checks governance compliance, and updates the API.
     *
     * @param apiId          UUID of the API to be updated.
     * @param body           APIDTO containing the updated API details.
     * @param ifMatch        ETag value for optimistic concurrency control.
     * @param messageContext Message context of the request.
     * @return HTTP Response containing the updated APIDTO or an error response.
     * @throws APIManagementException if an error occurs while updating the MCP server.
     */
    @Override
    public Response updateMCPServer(String apiId, APIDTO body, String ifMatch, MessageContext messageContext)
            throws APIManagementException {

        String[] tokenScopes =
                (String[]) PhaseInterceptorChain.getCurrentMessage().getExchange()
                        .get(RestApiConstants.USER_REST_API_SCOPES);
        String username = RestApiCommonUtil.getLoggedInUsername();
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            OrganizationInfo organizationInfo = RestApiUtil.getOrganizationInfo(messageContext);
            CommonUtils.validateAPIExistence(apiId);
            if (!PublisherCommonUtils.validateEndpointConfigs(body)) {
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

            if (!PublisherCommonUtils.validateEndpoints(body)) {
                throw new APIManagementException("Invalid/Malformed endpoint URL(s) detected",
                        ExceptionCodes.INVALID_ENDPOINT_URL);
            }

            APIProvider apiProvider = RestApiCommonUtil.getProvider(username);
            API originalAPI = apiProvider.getAPIbyUUID(apiId, organization);
            originalAPI.setOrganization(organization);

            validateAPIOperationsPerLC(originalAPI.getStatus());
            Map<String, String> complianceResult = PublisherCommonUtils
                    .checkGovernanceComplianceSync(originalAPI.getUuid(), APIMGovernableState.API_UPDATE,
                            ArtifactType.API, originalAPI.getOrganization(),
                            null, null);
            if (!complianceResult.isEmpty()
                    && complianceResult.get(APIConstants.GOVERNANCE_COMPLIANCE_KEY) != null
                    && !Boolean.parseBoolean(complianceResult.get(APIConstants.GOVERNANCE_COMPLIANCE_KEY))) {
                throw new APIComplianceException(
                        complianceResult.get(APIConstants.GOVERNANCE_COMPLIANCE_ERROR_MESSAGE));
            }

            API updatedApi = PublisherCommonUtils.updateApi(originalAPI, body, apiProvider, tokenScopes,
                    organizationInfo);

            PublisherCommonUtils.checkGovernanceComplianceAsync(originalAPI.getUuid(), APIMGovernableState.API_UPDATE,
                    ArtifactType.API, originalAPI.getOrganization());
            return Response.ok().entity(APIMappingUtil.fromAPItoDTO(updatedApi)).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                if (e.getErrorHandler()
                        .getErrorCode() == ExceptionCodes.GLOBAL_MEDIATION_POLICIES_NOT_FOUND.getErrorCode()) {
                    RestApiUtil.handleResourceNotFoundError(e.getErrorHandler().getErrorDescription(), e, log);
                } else {
                    RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP, apiId, e, log);
                }
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("Authorization failure while updating MCP server: "
                        + apiId, e, log);
            } else {
                throw e;
            }
        } catch (FaultGatewaysException e) {
            String errorMessage = "Error while updating MCP server: " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (CryptoException e) {
            String errorMessage = "Error while encrypting the secret key of MCP server: " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (ParseException e) {
            String errorMessage = "Error while parsing endpoint config of MCP server: " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * @param apiId
     * @param documentId
     * @param documentDTO
     * @param ifMatch
     * @param messageContext
     * @return
     * @throws APIManagementException
     */
    @Override
    public Response updateMCPServerDocument(String apiId, String documentId, DocumentDTO documentDTO, String ifMatch,
                                            MessageContext messageContext) throws APIManagementException {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            //validate if api exists
            APIInfo apiInfo = CommonUtils.validateAPIExistence(apiId);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(apiInfo.getStatus().toString());

            String sourceUrl = documentDTO.getSourceUrl();
            Documentation oldDocument = apiProvider.getDocumentation(apiId, documentId, organization);

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
                    apiProvider.isAnotherOverviewDocumentationExist(apiId, documentId, documentDTO.getOtherTypeName(),
                            organization)) {
                RestApiUtil.handleBadRequest("Requested other document type _overview already exists", log);
                return null;
            }

            //overriding some properties
            documentDTO.setName(oldDocument.getName());

            Documentation newDocumentation = DocumentationMappingUtil.fromDTOtoDocumentation(documentDTO);
            newDocumentation.setFilePath(oldDocument.getFilePath());
            newDocumentation.setId(documentId);
            newDocumentation = apiProvider.updateDocumentation(apiId, newDocumentation, organization);

            return Response.ok().entity(DocumentationMappingUtil.fromDocumentationToDTO(newDocumentation)).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while updating document : " + documentId + " of API "
                                + apiId, e, log);
            } else {
                String errorMessage = "Error while updating the document " + documentId + " for API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * Retrieves a specific endpoint of an MCP server by its ID.
     * Validates the API existence and retrieves the endpoint details.
     *
     * @param apiId          UUID of the API.
     * @param endpointId     ID of the endpoint to be retrieved.
     * @param messageContext Message context of the request.
     * @return HTTP Response containing the BackendEndpointDTO or an error response.
     * @throws APIManagementException if an error occurs while retrieving the endpoint.
     */
    @Override
    public Response updateMCPServerEndpoint(String apiId, String endpointId, BackendEndpointDTO backendEndpointDTO,
                                            MessageContext messageContext) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        CommonUtils.validateAPIExistence(apiId);
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        BackendEndpoint backendEndpoint = apiProvider.getMCPServerEndpoint(apiId, endpointId);

        if (backendEndpoint == null) {
            RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP, apiId, log);
        } else {
            backendEndpoint.setEndpointConfig(backendEndpointDTO.getEndpointConfig());
        }
        apiProvider.updateMCPServerEndpoint(apiId, backendEndpoint);
        return Response.ok().entity(APIMappingUtil.fromBackendEndpointToDTO(backendEndpoint, organization,
                false)).build();
    }

    /**
     * @param query
     * @param ifNoneMatch
     * @param messageContext
     * @return
     * @throws APIManagementException
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

    /**
     * @param endpointUrl
     * @param apiId
     * @param messageContext
     * @return
     * @throws APIManagementException
     */
    @Override
    public Response validateMCPServerEndpoint(String endpointUrl, String apiId, MessageContext messageContext)
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
            RestApiUtil.handleInternalServerError("Error while testing the validity of API endpoint url " +
                    "existence", e, log);
        }
        return Response.status(Response.Status.OK).entity(apiEndpointValidationResponseDTO).build();
    }

    /**
     * @param returnContent
     * @param url
     * @param fileInputStream
     * @param fileDetail
     * @param inlineAPIDefinition
     * @param messageContext
     * @return
     * @throws APIManagementException
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
            if (RestApiConstants.PUBLISHER_SCOPE.equals(scope)
                    || RestApiConstants.API_IMPORT_EXPORT_SCOPE.equals(scope)
                    || RestApiConstants.API_MANAGE_SCOPE.equals(scope)
                    || RestApiConstants.ADMIN_SCOPE.equals(scope)) {
                updatePermittedForPublishedDeprecated = true;
                break;
            }
        }
        if (!updatePermittedForPublishedDeprecated && (
                APIConstants.PUBLISHED.equals(status)
                        || APIConstants.DEPRECATED.equals(status))) {
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.API_UPDATE_FORBIDDEN_PER_LC, status));
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
     * @param apiId        API Id
     * @param organization organization
     * @return API Lifecycle state information
     */
    private LifecycleStateDTO getLifecycleState(String apiId, String organization) throws APIManagementException {

        try {
            APIIdentifier apiIdentifier;
            if (ApiMgtDAO.getInstance().checkAPIUUIDIsARevisionUUID(apiId) != null) {
                apiIdentifier = APIMappingUtil.getAPIInfoFromUUID(apiId, organization).getId();
                apiIdentifier.setUuid(apiId);
            } else {
                apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
            }
            if (apiIdentifier == null) {
                throw new APIManagementException("Error while getting the api identifier for the API:" +
                        apiId, ExceptionCodes.from(ExceptionCodes.INVALID_API_ID, apiId));
            }
            return PublisherCommonUtils.getLifecycleStateInformation(apiIdentifier, organization);
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("Authorization failure while deleting API : " + apiId, e, log);
            } else {
                throw e;
            }
        }
        return null;
    }

    private APIDTO createAPIDTO(API existingAPI, String newVersion) {

        APIDTO apidto = new APIDTO();
        apidto.setName(existingAPI.getId().getApiName());
        apidto.setContext(existingAPI.getContextTemplate());
        apidto.setVersion(newVersion);
        return apidto;
    }
}
