/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
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

import com.amazonaws.SdkClientException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.apache.cxf.jaxrs.ext.multipart.ContentDisposition;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.wso2.carbon.apimgt.api.*;
import org.wso2.carbon.apimgt.api.doc.model.APIResource;
import org.wso2.carbon.apimgt.api.dto.APIEndpointValidationDTO;
import org.wso2.carbon.apimgt.api.dto.CertificateInformationDTO;
import org.wso2.carbon.apimgt.api.dto.ClientCertificateDTO;
import org.wso2.carbon.apimgt.api.dto.EnvironmentPropertiesDTO;
import org.wso2.carbon.apimgt.api.dto.ImportedAPIDTO;
import org.wso2.carbon.apimgt.api.model.*;
import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.GraphqlComplexityInfo;
import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.GraphqlSchemaType;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.impl.GZIPUtils;
import org.wso2.carbon.apimgt.impl.ServiceCatalogImpl;
import org.wso2.carbon.apimgt.impl.certificatemgt.ResponseCode;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.definitions.*;
import org.wso2.carbon.apimgt.impl.dto.RuntimeArtifactDto;
import org.wso2.carbon.apimgt.impl.dto.WorkflowDTO;
import org.wso2.carbon.apimgt.impl.gatewayartifactsynchronizer.RuntimeArtifactGeneratorUtil;
import org.wso2.carbon.apimgt.impl.importexport.APIImportExportException;
import org.wso2.carbon.apimgt.impl.importexport.ExportFormat;
import org.wso2.carbon.apimgt.impl.importexport.ImportExportAPI;
import org.wso2.carbon.apimgt.impl.importexport.utils.APIImportExportUtil;
import org.wso2.carbon.apimgt.impl.importexport.utils.CommonUtil;
import org.wso2.carbon.apimgt.impl.restapi.CommonUtils;
import org.wso2.carbon.apimgt.impl.restapi.publisher.ApisApiServiceImplUtils;
import org.wso2.carbon.apimgt.impl.restapi.publisher.OperationPoliciesApiServiceImplUtils;
import org.wso2.carbon.apimgt.impl.utils.APIMWSDLReader;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.impl.utils.CertificateMgtUtils;
import org.wso2.carbon.apimgt.impl.workflow.WorkflowConstants;
import org.wso2.carbon.apimgt.impl.wsdl.model.WSDLValidationResponse;
import org.wso2.carbon.apimgt.impl.wsdl.util.SequenceUtils;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.ApisApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.RestApiPublisherUtils;
import org.wso2.carbon.apimgt.rest.api.util.exception.BadRequestException;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.core.util.CryptoException;
import org.wso2.carbon.core.util.CryptoUtil;
import org.wso2.carbon.utils.multitenancy.MultitenantConstants;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.util.*;

import static org.wso2.carbon.apimgt.api.ExceptionCodes.API_VERSION_ALREADY_EXISTS;

public class ApisApiServiceImpl implements ApisApiService {

    private static final Log log = LogFactory.getLog(ApisApiServiceImpl.class);
    private static final String API_PRODUCT_TYPE = "APIPRODUCT";

    @Override
    public Response getAllAPIs(Integer limit, Integer offset, String sortBy, String sortOrder, String xWSO2Tenant,
                               String query, String ifNoneMatch, String accept,
                               MessageContext messageContext) {

        List<API> allMatchedApis = new ArrayList<>();
        Object apiListDTO;

        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query == null ? "" : query;
        sortBy = sortBy != null ? sortBy : RestApiConstants.DEFAULT_SORT_CRITERION;
        sortOrder = sortOrder != null ? sortOrder : RestApiConstants.DESCENDING_SORT_ORDER;
        try {

            //revert content search back to normal search by name to avoid doc result complexity and to comply with REST api practices
            if (query.startsWith(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + ":")) {
                query = query
                        .replace(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + ":", APIConstants.NAME_TYPE_PREFIX + ":");
            }

            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

            String organization = RestApiUtil.getValidatedOrganization(messageContext);
//            boolean migrationMode = Boolean.getBoolean(RestApiConstants.MIGRATION_MODE);

            /*if (migrationMode) { // migration flow
                if (!StringUtils.isEmpty(targetTenantDomain)) {
                    tenantDomain = targetTenantDomain;
                }
                RestApiUtil.handleMigrationSpecificPermissionViolations(tenantDomain, username);
            }*/
            Map<String, Object> result;

            result = apiProvider.searchPaginatedAPIs(query, organization, offset, limit, sortBy, sortOrder);

            Set<API> apis = (Set<API>) result.get("apis");
            allMatchedApis.addAll(apis);

            apiListDTO = APIMappingUtil.fromAPIListToDTO(allMatchedApis);

            //Add pagination section in the response
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
                            .header("Content-Disposition", "attachment").
                                    header("Content-Encoding", "gzip").build();
                } catch (APIManagementException e) {
                    RestApiUtil.handleInternalServerError(e.getMessage(), e, log);
                }
            } else {
                return Response.ok().entity(apiListDTO).build();
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving APIs";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response getSequenceBackendData(String apiId, MessageContext messageContext) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        //validate if api exists
        CommonUtils.validateAPIExistence(apiId);
        List<SequenceBackendData> data = apiProvider.getAllSequenceBackendsByAPIUUID(apiId);
        if (data == null) {
            throw new APIMgtResourceNotFoundException(
                    "Couldn't retrieve an existing Sequence Backend for API " + apiId,
                    ExceptionCodes.from(ExceptionCodes.CUSTOM_BACKEND_NOT_FOUND, apiId));
        }
        SequenceBackendListDTO respObj = APIMappingUtil.fromSequenceDataToDTO(data);
        return Response.ok().entity(respObj).build();
    }

    @Override
    public Response getSequenceBackendContent(String type, String apiId, MessageContext messageContext) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        CommonUtils.validateAPIExistence(apiId);

        SequenceBackendData data = apiProvider.getCustomBackendByAPIUUID(apiId, type);
        if (data == null) {
            throw new APIMgtResourceNotFoundException(
                    "Couldn't retrieve an existing Sequence Backend for API: " + apiId,
                    ExceptionCodes.from(ExceptionCodes.CUSTOM_BACKEND_NOT_FOUND, apiId));
        }
        File file = RestApiPublisherUtils.exportCustomBackendData(data.getSequence(), data.getName());
        return Response.ok(file)
                .header(RestApiConstants.HEADER_CONTENT_DISPOSITION, "attachment; filename=\"" + file.getName() + "\"")
                .build();
    }

    @Override
    public Response sequenceBackendDelete(String type, String apiId, MessageContext messageContext) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        //validate if api exists
        CommonUtils.validateAPIExistence(apiId);
        apiProvider.deleteCustomBackendByID(apiId, type);
        return Response.ok().build();
    }

    @Override
    public Response createAPI(APIDTO body, String oasVersion, MessageContext messageContext)
            throws APIManagementException{
        URI createdApiUri;
        APIDTO createdApiDTO;
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            API createdApi = PublisherCommonUtils
                    .addAPIWithGeneratedSwaggerDefinition(body, oasVersion, RestApiCommonUtil.getLoggedInUsername(),
                            organization);
            createdApiDTO = APIMappingUtil.fromAPItoDTO(createdApi);
            //This URI used to set the location header of the POST response
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
        }
        return null;
    }

    @Override
    public Response getAPI(String apiId, String xWSO2Tenant, String ifNoneMatch,
            MessageContext messageContext) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APIDTO apiToReturn = getAPIByID(apiId, apiProvider, organization);
        return Response.ok().entity(apiToReturn).build();
    }

    @Override
    public Response sequenceBackendUpdate(String apiId, InputStream sequenceInputStream,
            Attachment sequenceDetail, String type, MessageContext messageContext) throws APIManagementException {
        String username = RestApiCommonUtil.getLoggedInUsername();
        APIProvider apiProvider = RestApiCommonUtil.getProvider(username);
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        API api = apiProvider.getAPIbyUUID(apiId, organization);
        api.setOrganization(organization);
        MultivaluedMap<String, String> headers = sequenceDetail.getHeaders();
        String contentDecomp = headers.getFirst("Content-Disposition");

        PublisherCommonUtils.updateCustomBackend(api, apiProvider, type, sequenceInputStream, contentDecomp);
        return Response.ok().build();
    }

    @Override
    public Response addCommentToAPI(String apiId, PostRequestBodyDTO postRequestBodyDTO, String replyTo, MessageContext
            messageContext) throws APIManagementException {
        String username = RestApiCommonUtil.getLoggedInUsername();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, organization);
            Comment comment = ApisApiServiceImplUtils
                    .createComment(postRequestBodyDTO.getContent(), postRequestBodyDTO.getCategory(),
                            replyTo, username, apiId);
            String createdCommentId = apiProvider.addComment(apiId, comment, username);
            Comment createdComment = apiProvider.getComment(apiTypeWrapper, createdCommentId, 0, 0);
            CommentDTO commentDTO = CommentMappingUtil.fromCommentToDTO(createdComment);

            String uriString = RestApiConstants.RESOURCE_PATH_APIS + "/" + apiId +
                    RestApiConstants.RESOURCE_PATH_COMMENTS + "/" + createdCommentId;
            URI uri = new URI(uriString);
            return Response.created(uri).entity(commentDTO).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                RestApiUtil.handleInternalServerError("Failed to add comment to the API " + apiId, e, log);
            }
        } catch (URISyntaxException e) {
            throw new APIManagementException("Error while retrieving comment content location for API " + apiId);
        }
        return null;
    }

    @Override
    public Response getAllCommentsOfAPI(String apiId, String xWSO2Tenant, Integer limit, Integer offset, Boolean
            includeCommenterInfo, MessageContext messageContext) throws APIManagementException {
        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, requestedTenantDomain);
            String parentCommentID = null;
            CommentList comments = apiProvider.getComments(apiTypeWrapper, parentCommentID, limit, offset);
            CommentListDTO commentDTO = CommentMappingUtil.fromCommentListToDTO(comments, includeCommenterInfo);

            String uriString = RestApiConstants.RESOURCE_PATH_APIS + "/" + apiId +
                    RestApiConstants.RESOURCE_PATH_COMMENTS;
            URI uri = new URI(uriString);
            return Response.ok(uri).entity(commentDTO).build();

        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                RestApiUtil.handleInternalServerError("Failed to get comments of API " + apiId, e, log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving comments content location for API " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response getCommentOfAPI(String commentId, String apiId, String xWSO2Tenant, String ifNoneMatch, Boolean
            includeCommenterInfo, Integer replyLimit, Integer replyOffset, MessageContext messageContext) throws
            APIManagementException {
        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, requestedTenantDomain);
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
                String uriString = RestApiConstants.RESOURCE_PATH_APIS + "/" + apiId +
                        RestApiConstants.RESOURCE_PATH_COMMENTS + "/" + commentId;
                URI uri = new URI(uriString);
                return Response.ok(uri).entity(commentDTO).build();
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_COMMENTS,
                        String.valueOf(commentId), log);
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving comment for API : " + apiId + "with comment ID " + commentId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving comment content location : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response getRepliesOfComment(String commentId, String apiId, String xWSO2Tenant, Integer limit, Integer
            offset, String ifNoneMatch, Boolean includeCommenterInfo, MessageContext messageContext) throws
            APIManagementException {
        String requestedTenantDomain = RestApiUtil.getRequestedTenantDomain(xWSO2Tenant);
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, requestedTenantDomain);
            CommentList comments = apiProvider.getComments(apiTypeWrapper, commentId, limit, offset);
            CommentListDTO commentDTO = CommentMappingUtil.fromCommentListToDTO(comments, includeCommenterInfo);

            String uriString = RestApiConstants.RESOURCE_PATH_APIS + "/" + apiId +
                    RestApiConstants.RESOURCE_PATH_COMMENTS;
            URI uri = new URI(uriString);
            return Response.ok(uri).entity(commentDTO).build();

        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                RestApiUtil.handleInternalServerError("Failed to get comments of API " + apiId, e, log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving comments content location for API " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response editCommentOfAPI(String commentId, String apiId, PatchRequestBodyDTO patchRequestBodyDTO,
                                     MessageContext messageContext) throws APIManagementException {
        String username = RestApiCommonUtil.getLoggedInUsername();
        String requestedTenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, requestedTenantDomain);
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

                            String uriString = RestApiConstants.RESOURCE_PATH_APIS + "/" + apiId +
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
            String errorMessage = "Error while retrieving comment content location for API " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response deleteComment(String commentId, String apiId, String ifMatch, MessageContext messageContext) throws
            APIManagementException {
        String requestedTenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        String username = RestApiCommonUtil.getLoggedInUsername();
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, requestedTenantDomain);
            Comment comment = apiProvider.getComment(apiTypeWrapper, commentId, 0, 0);
            if (comment != null) {
                String[] tokenScopes = (String[]) PhaseInterceptorChain.getCurrentMessage().getExchange()
                        .get(RestApiConstants.USER_REST_API_SCOPES);
                if (Arrays.asList(tokenScopes).contains(RestApiConstants.ADMIN_SCOPE) || comment.getUser().equals(username)) {
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
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                String errorMessage = "Error while deleting comment " + commentId + "for API " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * Get complexity details of a given API
     *
     * @param apiId          apiId
     * @param messageContext message context
     * @return Response with complexity details of the GraphQL API
     */

    @Override
    public Response getGraphQLPolicyComplexityOfAPI(String apiId, MessageContext messageContext)
            throws APIManagementException {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            API api = apiProvider.getAPIbyUUID(apiId, organization);
            if (APIConstants.GRAPHQL_API.equals(api.getType())) {
                String currentApiUuid;
                // Resolve whether an API or a corresponding revision
                APIRevision apiRevision = apiProvider.checkAPIUUIDIsARevisionUUID(apiId);
                if (apiRevision != null && apiRevision.getApiUUID() != null) {
                    currentApiUuid = apiRevision.getApiUUID();
                } else {
                    currentApiUuid = apiId;
                }
                GraphqlComplexityInfo graphqlComplexityInfo = apiProvider.getComplexityDetails(currentApiUuid);
                GraphQLQueryComplexityInfoDTO graphQLQueryComplexityInfoDTO =
                        GraphqlQueryAnalysisMappingUtil.fromGraphqlComplexityInfotoDTO(graphqlComplexityInfo);
                return Response.ok().entity(graphQLQueryComplexityInfoDTO).build();
            } else {
                throw new APIManagementException(ExceptionCodes.API_NOT_GRAPHQL);
            }
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need
            // to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while retrieving complexity details of API : " + apiId, e, log);
            } else {
                String msg = "Error while retrieving complexity details of API " + apiId;
                RestApiUtil.handleInternalServerError(msg, e, log);
            }
        }
        return null;
    }

    /**
     * Update complexity details of a given API
     *
     * @param apiId          apiId
     * @param body           GraphQLQueryComplexityInfo DTO as request body
     * @param messageContext message context
     * @return Response
     */

    @Override
    public Response updateGraphQLPolicyComplexityOfAPI(String apiId, GraphQLQueryComplexityInfoDTO body,
                                                       MessageContext messageContext) throws APIManagementException {
        try {
            if (StringUtils.isBlank(apiId)) {
                String errorMessage = "API ID cannot be empty or null.";
                RestApiUtil.handleBadRequest(errorMessage, log);
            }

            //validate if api exists
            APIInfo apiInfo = CommonUtils.validateAPIExistence(apiId);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(apiInfo.getStatus().toString());

            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            API existingAPI = apiProvider.getAPIbyUUID(apiId, organization);
            String schema = apiProvider.getGraphqlSchemaDefinition(apiId, organization);

            GraphqlComplexityInfo graphqlComplexityInfo =
                    GraphqlQueryAnalysisMappingUtil.fromDTOtoValidatedGraphqlComplexityInfo(body, schema);
            if (APIConstants.GRAPHQL_API.equals(existingAPI.getType())) {
                apiProvider.addOrUpdateComplexityDetails(apiId, graphqlComplexityInfo);
                return Response.ok().build();
            } else {
                throw new APIManagementException(ExceptionCodes.API_NOT_GRAPHQL);
            }
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need
            // to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while updating complexity details of API : " + apiId, e, log);
            } else {
                String errorMessage = "Error while updating complexity details of API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    @Override
    public Response updateTopics(String apiId, TopicListDTO topicListDTO, String ifMatch, MessageContext messageContext)
            throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        //validate if api exists
        CommonUtils.validateAPIExistence(apiId);
        API existingAPI = apiProvider.getAPIbyUUID(apiId, organization);
        API updatedAPI = apiProvider.getAPIbyUUID(apiId, organization);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(updatedAPI.getStatus());

        Set<URITemplate> uriTemplates = updatedAPI.getUriTemplates();
        uriTemplates.clear();

        for (TopicDTO topicDTO : topicListDTO.getList()) {
            uriTemplates.add(ApisApiServiceImplUtils.createUriTemplate(topicDTO.getName(), topicDTO.getMode()));
        }
        updatedAPI.setUriTemplates(uriTemplates);

        // TODO: Add scopes
        updatedAPI.setOrganization(organization);
        try {
            apiProvider.updateAPI(updatedAPI, existingAPI);
        } catch (FaultGatewaysException e) {
            String errorMessage = "Error while updating API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return Response.ok().build();
    }

    /**
     * Get GraphQL Schema of given API
     *
     * @param apiId          apiId
     * @param accept
     * @param ifNoneMatch    If--Match header value
     * @param messageContext message context
     * @return Response with GraphQL Schema
     */
    @Override
    public Response getAPIGraphQLSchema(String apiId, String accept, String ifNoneMatch,
                                        MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            //this will fail if user does not have access to the API or the API does not exist
            APIIdentifier apiIdentifier;
            if (ApiMgtDAO.getInstance().checkAPIUUIDIsARevisionUUID(apiId) != null) {
                apiIdentifier = APIMappingUtil.getAPIInfoFromUUID(apiId,organization).getId();
            } else {
                apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
            }
            String schemaContent = apiProvider.getGraphqlSchemaDefinition(apiId, organization);
            GraphQLSchemaDTO dto = new GraphQLSchemaDTO();
            dto.setSchemaDefinition(schemaContent);
            dto.setName(apiIdentifier.getProviderName() + APIConstants.GRAPHQL_SCHEMA_PROVIDER_SEPERATOR +
                    apiIdentifier.getApiName() + apiIdentifier.getVersion() + APIConstants.GRAPHQL_SCHEMA_FILE_EXTENSION);
            return Response.ok().entity(dto).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need
            // to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil
                        .handleAuthorizationFailure("Authorization failure while retrieving schema of API: " + apiId, e,
                                log);
            } else {
                String errorMessage = "Error while retrieving schema of API: " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * Update GraphQL Schema
     *
     * @param apiId            api Id
     * @param schemaDefinition graphQL schema definition
     * @param ifMatch
     * @param messageContext
     * @return
     */
    @Override
    public Response updateAPIGraphQLSchema(String apiId, String schemaDefinition, String ifMatch,
                                           MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            //validate if api exists
            CommonUtils.validateAPIExistence(apiId);
            API originalAPI = apiProvider.getAPIbyUUID(apiId, organization);
            originalAPI.setOrganization(organization);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(originalAPI.getStatus());
            PublisherCommonUtils.addGraphQLSchema(originalAPI, schemaDefinition, apiProvider);
            APIDTO modifiedAPI = APIMappingUtil.fromAPItoDTO(originalAPI);
            return Response.ok().entity(modifiedAPI.getOperations()).build();
        } catch (APIManagementException | FaultGatewaysException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need
            // to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil
                        .handleAuthorizationFailure("Authorization failure while retrieving schema of API: " + apiId, e,
                                log);
            } else {
                String errorMessage = "Error while uploading schema of the API: " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    @Override
    public Response updateAPI(String apiId, APIDTO body, String ifMatch, MessageContext messageContext)
            throws APIManagementException {
        String[] tokenScopes =
                (String[]) PhaseInterceptorChain.getCurrentMessage().getExchange()
                        .get(RestApiConstants.USER_REST_API_SCOPES);
        String username = RestApiCommonUtil.getLoggedInUsername();
        boolean isWSAPI = APIDTO.TypeEnum.WS.equals(body.getType());

        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            //validate if api exists
            CommonUtils.validateAPIExistence(apiId);
            if (!PublisherCommonUtils.validateEndpointConfigs(body)) {
                throw new APIManagementException("Invalid endpoint configs detected",
                        ExceptionCodes.INVALID_ENDPOINT_CONFIG);
            }
            // validate web socket api endpoint configurations
            if (isWSAPI && !PublisherCommonUtils.isValidWSAPI(body)) {
                throw new APIManagementException("Endpoint URLs should be valid web socket URLs",
                        ExceptionCodes.INVALID_ENDPOINT_URL);
            }

            // validate custom properties
            org.json.simple.JSONArray customProperties = APIUtil.getCustomProperties(username);
            List<String> errorProperties = PublisherCommonUtils.validateMandatoryProperties(customProperties, body);
            if (!errorProperties.isEmpty()) {
                String errorString = " : " + String.join(", ", errorProperties);
                RestApiUtil.handleBadRequest(
                        ExceptionCodes.ERROR_WHILE_UPDATING_MANDATORY_PROPERTIES.getErrorMessage() + errorString,
                        ExceptionCodes.ERROR_WHILE_UPDATING_MANDATORY_PROPERTIES.getErrorCode(), log);
            }

            // validate sandbox and production endpoints
            if (!PublisherCommonUtils.validateEndpoints(body)) {
                throw new APIManagementException("Invalid/Malformed endpoint URL(s) detected",
                        ExceptionCodes.INVALID_ENDPOINT_URL);
            }

            APIProvider apiProvider = RestApiCommonUtil.getProvider(username);
            API originalAPI = apiProvider.getAPIbyUUID(apiId, organization);
            originalAPI.setOrganization(organization);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(originalAPI.getStatus());
            API updatedApi = PublisherCommonUtils.updateApi(originalAPI, body, apiProvider, tokenScopes);
            return Response.ok().entity(APIMappingUtil.fromAPItoDTO(updatedApi)).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need
            // to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                if (e.getErrorHandler()
                        .getErrorCode() == ExceptionCodes.GLOBAL_MEDIATION_POLICIES_NOT_FOUND.getErrorCode()) {
                    RestApiUtil.handleResourceNotFoundError(e.getErrorHandler().getErrorDescription(), e, log);
                } else {
                    RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
                }
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("Authorization failure while updating API : " + apiId, e, log);
            } else {
                throw e;
            }
        } catch (FaultGatewaysException e) {
            String errorMessage = "Error while updating API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (CryptoException e) {
            String errorMessage = "Error while encrypting the secret key of API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (ParseException e) {
            String errorMessage = "Error while parsing endpoint config of API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * When the API is Published or Deprecated, only the users with scope "apim:api_import_export", "apim:api_publish", "apim:admin" will be allowed for
     * updating/deleting APIs or its sub-resources.
     *
     * @param status Status of the API which is currently created (current state)
     * @throws APIManagementException if update is not allowed
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
     * Get all types and fields of the GraphQL Schema of a given API
     *
     * @param apiId          apiId
     * @param messageContext message context
     * @return Response with all the types and fields found within the schema definition
     */
    @Override public Response getGraphQLPolicyComplexityTypesOfAPI(String apiId, MessageContext messageContext) {
        GraphQLSchemaDefinition graphql = new GraphQLSchemaDefinition();
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIIdentifier apiIdentifier;
            if (ApiMgtDAO.getInstance().checkAPIUUIDIsARevisionUUID(apiId) != null) {
                apiIdentifier = APIMappingUtil.getAPIInfoFromUUID(apiId,organization).getId();
            } else {
                apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
            }
            API api = apiProvider.getAPIbyUUID(apiId, organization);
            if (APIConstants.GRAPHQL_API.equals(api.getType())) {
                String schemaContent = apiProvider.getGraphqlSchemaDefinition(apiId, organization);
                List<GraphqlSchemaType> typeList = graphql.extractGraphQLTypeList(schemaContent);
                GraphQLSchemaTypeListDTO graphQLSchemaTypeListDTO =
                        GraphqlQueryAnalysisMappingUtil.fromGraphqlSchemaTypeListtoDTO(typeList);
                return Response.ok().entity(graphQLSchemaTypeListDTO).build();
            } else {
                throw new APIManagementException(ExceptionCodes.API_NOT_GRAPHQL);
            }
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need
            // to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while retrieving types and fields of API : " + apiId, e, log);
            } else {
                String msg = "Error while retrieving types and fields of the schema of API " + apiId;
                RestApiUtil.handleInternalServerError(msg, e, log);
            }
        }
        return null;
    }

    // AWS Lambda: rest api operation to get ARNs
    @Override
    public Response getAmazonResourceNamesOfAPI(String apiId, MessageContext messageContext) {
        JSONObject arns = new JSONObject();
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            API api = apiProvider.getAPIbyUUID(apiId, organization);
            arns = ApisApiServiceImplUtils.getAmazonResourceNames(api);
            if (arns != null) {
                return Response.ok().entity(arns.toString()).build();
            }
        } catch (SdkClientException e) {
            if (e.getCause() instanceof UnknownHostException) {
                arns.put("error", "No internet connection to connect the given access method.");
                log.error("No internet connection to connect the given access method of API : " + apiId, e);
                return Response.serverError().entity(arns.toString()).build();
            } else {
                arns.put("error", "Unable to access Lambda functions under the given access method.");
                log.error("Unable to access Lambda functions under the given access method of API : " + apiId, e);
                return Response.serverError().entity(arns.toString()).build();
            }
        } catch (ParseException e) {
            String errorMessage = "Error while parsing endpoint config of the API: " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (CryptoException e) {
            String errorMessage = "Error while decrypting the secret key of the API: " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving the API: " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Method to retrieve Security Audit Report
     * @param apiId API ID of the API
     * @param accept Accept header string
     * @param messageContext Message Context string
     * @return Response object of Security Audit
     */
    @Override
    public Response getAuditReportOfAPI(String apiId, String accept, MessageContext messageContext) {
        try {
            String username = RestApiCommonUtil.getLoggedInUsername();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIProvider apiProvider = RestApiCommonUtil.getProvider(username);
            API api = apiProvider.getAPIbyUUID(apiId, organization);
            String apiDefinition = apiProvider.getOpenAPIDefinition(apiId, organization);
            // Get configuration file, retrieve API token and collection id
            JSONObject securityAuditPropertyObject = apiProvider.getSecurityAuditAttributesFromConfig(username);
            JSONObject responseJson = ApisApiServiceImplUtils
                    .getAuditReport(api, securityAuditPropertyObject, apiDefinition, organization);
            if (!responseJson.isEmpty()) {
                AuditReportDTO auditReportDTO = new AuditReportDTO();
                auditReportDTO.setReport((String) responseJson.get("decodedReport"));
                auditReportDTO.setGrade((String) responseJson.get("grade"));
                auditReportDTO.setNumErrors((Integer) responseJson.get("numErrors"));
                auditReportDTO.setExternalApiId((String) responseJson.get("auditUuid"));
                return Response.ok().entity(auditReportDTO).build();
            }
        } catch (IOException e) {
            RestApiUtil.handleInternalServerError("Error occurred while getting "
                    + "HttpClient instance", e, log);
        } catch (ParseException e) {
            RestApiUtil.handleInternalServerError("API Definition String "
                    + "could not be parsed into JSONObject.", e, log);
        } catch (APIManagementException e) {
            String errorMessage = "Error while Auditing API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response getAPIClientCertificateContentByAlias(String apiId, String alias,
                                                               MessageContext messageContext) {
        return getAPIClientCertificateContentByKeyTypeAndAlias(apiId, alias, APIConstants.API_KEY_TYPE_PRODUCTION,
                messageContext);
    }

    @Override
    public Response getAPIClientCertificateContentByKeyTypeAndAlias(String apiId, String alias, String keyType,
                                                                    MessageContext messageContext) {
        String organization = null;
        String certFileName = alias + ".crt";

        //validate the input for key type
        validateKeyType(keyType);

        try {
            organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, organization);
            ClientCertificateDTO clientCertificateDTO = CertificateRestApiUtils.preValidateClientCertificate(alias,
                    keyType, apiTypeWrapper, organization);
            if (clientCertificateDTO != null) {
                Object certificate = CertificateRestApiUtils
                        .getDecodedCertificate(clientCertificateDTO.getCertificate());
                Response.ResponseBuilder responseBuilder = Response.ok().entity(certificate);
                responseBuilder.header(RestApiConstants.HEADER_CONTENT_DISPOSITION,
                        "attachment; filename=\"" + certFileName + "\"");
                responseBuilder.header(RestApiConstants.HEADER_CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM);
                return responseBuilder.build();
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError(
                    "Error while retrieving the client certificate with alias " + alias + " for the tenant "
                            + organization, e, log);
        }
        return null;
    }

    @Override
    public Response deleteAPIClientCertificateByAlias(String alias, String apiId,
                                                           MessageContext messageContext) {
        return deleteAPIClientCertificateByKeyTypeAndAlias(APIConstants.API_KEY_TYPE_PRODUCTION, alias, apiId,
                messageContext);
    }

    @Override
    public Response deleteAPIClientCertificateByKeyTypeAndAlias(String keyType, String alias, String apiId,
                                                                MessageContext messageContext) {
        String organization = null;

        //validate the input for key type
        validateKeyType(keyType);

        try {
            organization = RestApiUtil.getValidatedOrganization(messageContext);
            //validate if api exists
            CommonUtils.validateAPIExistence(apiId);

            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, organization);
            apiTypeWrapper.setOrganization(organization);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(apiTypeWrapper.getStatus());

            ClientCertificateDTO clientCertificateDTO = CertificateRestApiUtils.preValidateClientCertificate(alias,
                    keyType, apiTypeWrapper, organization);
            int responseCode = apiProvider
                    .deleteClientCertificate(RestApiCommonUtil.getLoggedInUsername(), apiTypeWrapper, alias, keyType);
            if (responseCode == ResponseCode.SUCCESS.getResponseCode()) {

                if (log.isDebugEnabled()) {
                    log.debug(String.format("The client certificate of type %s which belongs to tenant : %s represented by the "
                            + "alias : %s is deleted successfully", keyType, organization, alias));
                }
                return Response.ok().entity("The " + keyType + " type certificate for alias '" + alias
                        + "' deleted successfully.").build();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Failed to delete the %s type client certificate which belongs to tenant :"
                            + " %s represented by the alias : %s.", keyType, organization, alias));
                }
                RestApiUtil.handleInternalServerError(
                        "Error while deleting the " + keyType + " type client certificate for alias '"
                                + alias + "'.", log);
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError(
                    "Error while deleting the " + keyType + " type client certificate with alias "
                            + alias + " for the tenant " + organization, e, log);
        }
        return null;
    }

    @Override
    public Response getAPIClientCertificateByAlias(String alias, String apiId,
                                                        MessageContext messageContext) {
        return getAPIClientCertificateByKeyTypeAndAlias(APIConstants.API_KEY_TYPE_PRODUCTION, alias, apiId,
                messageContext);
    }

    @Override
    public Response getAPIClientCertificateByKeyTypeAndAlias(String keyType, String alias, String apiId,
                                                             MessageContext messageContext) {
        String organization = null;

        //validate the input for key type
        validateKeyType(keyType);

        CertificateMgtUtils certificateMgtUtils = CertificateMgtUtils.getInstance();
        try {
            organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, organization);
            ClientCertificateDTO clientCertificateDTO = CertificateRestApiUtils.preValidateClientCertificate(alias,
                    keyType, apiTypeWrapper, organization);
            CertificateInformationDTO certificateInformationDTO = certificateMgtUtils
                    .getCertificateInfo(clientCertificateDTO.getCertificate());
            if (certificateInformationDTO != null) {
                CertificateInfoDTO certificateInfoDTO = CertificateMappingUtil
                        .fromCertificateInformationToDTO(certificateInformationDTO);
                return Response.ok().entity(certificateInfoDTO).build();
            } else {
                RestApiUtil.handleResourceNotFoundError("Certificate is empty for alias " + alias, log);
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError(
                    "Error while retrieving the client certificate with alias " + alias + " for the tenant "
                            + organization, e, log);
        }
        return null;
    }

    @Override
    public Response updateAPIClientCertificateByAlias(String alias, String apiId,
                                                      InputStream certificateInputStream,
                                                      Attachment certificateDetail, String tier,
                                                      MessageContext messageContext) {
        return updateAPIClientCertificateByKeyTypeAndAlias(APIConstants.API_KEY_TYPE_PRODUCTION, alias, apiId,
                certificateInputStream, certificateDetail, tier, messageContext);
    }

    @Override
    public Response updateAPIClientCertificateByKeyTypeAndAlias(String keyType, String alias, String apiId,
        InputStream certificateInputStream, Attachment certificateDetail, String tier, MessageContext messageContext) {
        try {
            //validate the input for key type
            validateKeyType(keyType);

            //validate if api exists
            CommonUtils.validateAPIExistence(apiId);

            ContentDisposition contentDisposition;
            String fileName;
            String base64EncodedCert = null;
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, organization);
            apiTypeWrapper.setOrganization(organization);

            String userName = RestApiCommonUtil.getLoggedInUsername();
            int tenantId = APIUtil.getInternalOrganizationId(organization);
            ClientCertificateDTO clientCertificateDTO = CertificateRestApiUtils.preValidateClientCertificate(alias,
                    keyType, apiTypeWrapper, organization);
            if (certificateDetail != null) {
                contentDisposition = certificateDetail.getContentDisposition();
                fileName = contentDisposition.getParameter(RestApiConstants.CONTENT_DISPOSITION_FILENAME);
                if (StringUtils.isNotBlank(fileName)) {
                    base64EncodedCert = CertificateRestApiUtils.generateEncodedCertificate(certificateInputStream);
                }
            }
            if (StringUtils.isEmpty(base64EncodedCert) && StringUtils.isEmpty(tier)) {
                return Response.ok().entity("Client Certificate is not updated for alias " + alias).build();
            }
            int responseCode = apiProvider
                    .updateClientCertificate(base64EncodedCert, alias, apiTypeWrapper, tier, keyType.toUpperCase(),
                            tenantId, organization);

            if (ResponseCode.SUCCESS.getResponseCode() == responseCode) {
                ClientCertMetadataDTO clientCertMetadataDTO = new ClientCertMetadataDTO();
                clientCertMetadataDTO.setAlias(alias);
                clientCertMetadataDTO.setApiId(apiTypeWrapper.getUuid());
                clientCertMetadataDTO.setTier(clientCertificateDTO.getTierName());
                URI updatedCertUri = new URI(RestApiConstants.CLIENT_CERTS_BASE_PATH + keyType + "?alias=" + alias);

                return Response.ok(updatedCertUri).entity(clientCertMetadataDTO).build();
            } else if (ResponseCode.INTERNAL_SERVER_ERROR.getResponseCode() == responseCode) {
                RestApiUtil.handleInternalServerError(
                        "Error while updating the client certificate for the alias " + alias + " due to an internal "
                                + "server error", log);
            } else if (ResponseCode.CERTIFICATE_NOT_FOUND.getResponseCode() == responseCode) {
                RestApiUtil.handleResourceNotFoundError("", log);
            } else if (ResponseCode.CERTIFICATE_EXPIRED.getResponseCode() == responseCode) {
                RestApiUtil.handleBadRequest(
                        "Error while updating the client certificate for the alias " + alias + " Certificate Expired.",
                        log);
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError(
                    "Error while updating the client certificate for the alias " + alias + " due to an internal "
                            + "server error", e, log);
        } catch (IOException e) {
            RestApiUtil
                    .handleInternalServerError("Error while encoding client certificate for the alias " + alias, e,
                            log);
        } catch (URISyntaxException e) {
            RestApiUtil.handleInternalServerError(
                    "Error while generating the resource location URI for alias '" + alias + "'", e, log);
        }
        return null;
    }

    @Override
    public Response getAPIClientCertificates(String apiId, Integer limit, Integer offset, String alias,
                                                   MessageContext messageContext) {
        return getAPIClientCertificatesByKeyType(APIConstants.API_KEY_TYPE_PRODUCTION, apiId, limit,offset, alias,
                messageContext);
    }

    @Override
    public Response getAPIClientCertificatesByKeyType(String keyType, String apiId, Integer limit, Integer offset,
                                                      String alias, MessageContext messageContext) {
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        List<ClientCertificateDTO> certificates = new ArrayList<>();

        //validate the input for key type
        validateKeyType(keyType);

        String query = CertificateRestApiUtils.buildQueryString("alias", alias, "apiId", apiId);

        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            int tenantId = APIUtil.getInternalOrganizationId(organization);
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            int totalCount = apiProvider.getClientCertificateCount(tenantId, keyType);
            if (totalCount > 0) {
                APIIdentifier apiIdentifier = null;
                if (StringUtils.isNotEmpty(apiId)) {
                    API api = apiProvider.getAPIbyUUID(apiId, organization);
                    apiIdentifier = api.getId();
                }
                certificates = apiProvider.searchClientCertificates(tenantId, alias, keyType,
                        apiIdentifier, organization);
            }

            ClientCertificatesDTO certificatesDTO = CertificateRestApiUtils
                    .getPaginatedClientCertificates(certificates, limit, offset, keyType, query);
            PaginationDTO paginationDTO = new PaginationDTO();
            paginationDTO.setLimit(limit);
            paginationDTO.setOffset(offset);
            paginationDTO.setTotal(totalCount);
            certificatesDTO.setPagination(paginationDTO);
            return Response.status(Response.Status.OK).entity(certificatesDTO).build();
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while retrieving the client certificates.", e, log);
        }
        return null;
    }

    private void validateKeyType(String keyType) {
        if (!(keyType.equalsIgnoreCase(APIConstants.API_KEY_TYPE_PRODUCTION) ||
                keyType.equalsIgnoreCase(APIConstants.API_KEY_TYPE_SANDBOX))) {
            RestApiUtil.handleBadRequest("The key type is invalid. It should be either PRODUCTION or SANDBOX",
                    log);
        }
    }

    @Override
    public Response addAPIClientCertificate(String apiId, InputStream certificateInputStream,
                                            Attachment certificateDetail, String alias, String tier,
                                            MessageContext messageContext) {
        return addAPIClientCertificateOfGivenKeyType(APIConstants.API_KEY_TYPE_PRODUCTION, apiId,
                certificateInputStream, certificateDetail, alias, tier, messageContext);
    }

    @Override
    public Response addAPIClientCertificateOfGivenKeyType(String keyType, String apiId,
      InputStream certificateInputStream, Attachment certificateDetail, String alias, String tier,
                                                          MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            ContentDisposition contentDisposition = certificateDetail.getContentDisposition();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            String fileName = contentDisposition.getParameter(RestApiConstants.CONTENT_DISPOSITION_FILENAME);

            //validate the input for key type
            validateKeyType(keyType);

            if (StringUtils.isEmpty(alias) || StringUtils.isEmpty(apiId)) {
                RestApiUtil.handleBadRequest("The alias and/ or apiId should not be empty", log);
            }
            if (StringUtils.isBlank(fileName)) {
                RestApiUtil.handleBadRequest(
                        "Certificate addition failed. Proper Certificate file should be provided", log);
            }
            //validate if api exists
            CommonUtils.validateAPIExistence(apiId);

            ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, organization);
            apiTypeWrapper.setOrganization(organization);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(apiTypeWrapper.getStatus());

            String userName = RestApiCommonUtil.getLoggedInUsername();
            String base64EncodedCert = CertificateRestApiUtils.generateEncodedCertificate(certificateInputStream);
            int responseCode = apiProvider
                    .addClientCertificate(userName, apiTypeWrapper, base64EncodedCert, alias, tier,
                            keyType.toUpperCase(), organization);
            if (log.isDebugEnabled()) {
                log.debug(String.format("Add certificate operation response code : %d", responseCode));
            }
            if (ResponseCode.SUCCESS.getResponseCode() == responseCode) {
                ClientCertMetadataDTO certificateDTO = new ClientCertMetadataDTO();
                certificateDTO.setAlias(alias);
                certificateDTO.setApiId(apiId);
                certificateDTO.setTier(tier);
                URI createdCertUri = new URI(RestApiConstants.CLIENT_CERTS_BASE_PATH + keyType + "?alias=" + alias);
                return Response.created(createdCertUri).entity(certificateDTO).build();
            } else if (ResponseCode.INTERNAL_SERVER_ERROR.getResponseCode() == responseCode) {
                RestApiUtil.handleInternalServerError(
                        "Internal server error while adding the client certificate to " + "API " + apiId, log);
            } else if (ResponseCode.ALIAS_EXISTS_IN_TRUST_STORE.getResponseCode() == responseCode) {
                RestApiUtil.handleResourceAlreadyExistsError("The alias '" + alias +
                        "' already exists in the trust store for " + keyType + " key type.", log);
            } else if (ResponseCode.CERTIFICATE_EXPIRED.getResponseCode() == responseCode) {
                RestApiUtil.handleBadRequest(
                        "Error while adding the certificate to the API " + apiId + ". " + "Certificate Expired.", log);
            }
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError(
                    "APIManagement exception while adding the certificate to the API " + apiId + " due to an internal "
                            + "server error", e, log);
        } catch (IOException e) {
            RestApiUtil.handleInternalServerError(
                    "IOException while generating the encoded certificate for the API " + apiId, e, log);
        } catch (URISyntaxException e) {
            RestApiUtil.handleInternalServerError(
                    "Error while generating the resource location URI for alias '" + alias + "'", e, log);
        }
        return null;
    }

    /**
     * Delete API
     *
     * @param apiId   API Id
     * @param ifMatch If-Match header value
     * @return Status of API Deletion
     */
    @Override
    public Response deleteAPI(String apiId, String ifMatch, MessageContext messageContext) {
        try {
            String username = RestApiCommonUtil.getLoggedInUsername();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIProvider apiProvider = RestApiCommonUtil.getProvider(username);

            boolean isAPIExistDB = false;
            APIManagementException error = null;
            APIInfo apiInfo = null;
            try {
                //validate if api exists
                apiInfo = CommonUtils.validateAPIExistence(apiId);
                isAPIExistDB = true;
            } catch (APIManagementException e) {
                log.error("Error while validating API existence for deleting API " + apiId + " on organization "
                        + organization);
                error = e;
            }

            if (isAPIExistDB) {
                //validate API update operation permitted based on the LC state
                validateAPIOperationsPerLC(apiInfo.getStatus().toString());

                try {
                    //check if the API has subscriptions
                    //Todo : need to optimize this check. This method seems too costly to check if subscription exists
                    List<SubscribedAPI> apiUsages = apiProvider.getAPIUsageByAPIId(apiId, organization);
                    if (apiUsages != null && !apiUsages.isEmpty()) {
                        List<SubscribedAPI> filteredUsages = new ArrayList<>();
                        for (SubscribedAPI usage:apiUsages) {
                            String subsCreatedStatus = usage.getSubCreatedStatus();
                            if (!APIConstants.SubscriptionCreatedStatus.UN_SUBSCRIBE.equals(subsCreatedStatus)) {
                                filteredUsages.add(usage);
                            }
                        }
                        if (!filteredUsages.isEmpty()) {
                            RestApiUtil.handleConflict("Cannot remove the API " + apiId + " as active subscriptions exist", log);
                        }
                    }
                } catch (APIManagementException e) {
                    log.error("Error while checking active subscriptions for deleting API " + apiId + " on organization "
                            + organization);
                    error = e;
                }

                try {
                    List<APIResource> usedProductResources = apiProvider.getUsedProductResources(apiId);

                    if (!usedProductResources.isEmpty()) {
                        RestApiUtil.handleConflict("Cannot remove the API because following resource paths " +
                                usedProductResources.toString() + " are used by one or more API Products", log);
                    }
                } catch (APIManagementException e) {
                    log.error("Error while checking API products using same resources for deleting API " + apiId +
                            " on organization " + organization);
                    error = e;
                }
            }

            // Delete the API
            boolean isDeleted = false;
            try {
                apiProvider.deleteAPI(apiId, organization);
                isDeleted = true;
            } catch (APIManagementException e) {
                log.error("Error while deleting API " + apiId + "on organization " + organization, e);
            }

            if (error != null) {
                throw error;
            } else if (!isDeleted) {
                RestApiUtil.handleInternalServerError("Error while deleting API : " + apiId + " on organization "
                        + organization, log);
                return null;
            }
            return Response.ok().build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("Authorization failure while deleting API : " + apiId, e, log);
            } else {
                String errorMessage = "Error while deleting API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * Get resources of an API that are reused by API Products
     *
     * @param api API
     * @return List of resources reused by API Products
     */
    private List<APIResource> getUsedProductResources(API api) {
        List<APIResource> usedProductResources = new ArrayList<>();
        Set<URITemplate> uriTemplates = api.getUriTemplates();

        for (URITemplate uriTemplate : uriTemplates) {
            // If existing URITemplate is used by any API Products
            if (!uriTemplate.retrieveUsedByProducts().isEmpty()) {
                APIResource apiResource = new APIResource(uriTemplate.getHTTPVerb(), uriTemplate.getUriTemplate());
                usedProductResources.add(apiResource);
            }
        }

        return usedProductResources;
    }

    /**
     * Retrieves the content of a document
     *
     * @param apiId       API identifier
     * @param documentId  document identifier
     * @param ifNoneMatch If-None-Match header value
     * @return Content of the document/ either inline/file or source url as a redirection
     */
    @Override
    public Response getAPIDocumentContentByDocumentId(String apiId, String documentId,
                                                           String ifNoneMatch, MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);

            DocumentationContent docContent = apiProvider.getDocumentationContent(apiId, documentId, organization);
            if (docContent == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_DOCUMENTATION, documentId);
                return null;
            }

            // gets the content depending on the type of the document
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
            // Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
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
     * Add content to a document. Content can be inline or File
     *
     * @param apiId         API identifier
     * @param documentId    document identifier
     * @param inputStream   file input stream
     * @param fileDetail    file details as Attachment
     * @param inlineContent inline content for the document
     * @param ifMatch       If-match header value
     * @return updated document as DTO
     */
    @Override
    public Response addAPIDocumentContent(String apiId, String documentId, String ifMatch,
            InputStream inputStream, Attachment fileDetail, String inlineContent, MessageContext messageContext) {
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            //validate if api exists
            APIInfo apiInfo = CommonUtils.validateAPIExistence(apiId);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(apiInfo.getStatus().toString());
            if (inputStream != null && inlineContent != null) {
                RestApiUtil.handleBadRequest("Only one of 'file' and 'inlineContent' should be specified", log);
            }

            //retrieves the document and send 404 if not found
            Documentation documentation = apiProvider.getDocumentation(apiId, documentId, organization);
            if (documentation == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_DOCUMENTATION, documentId, log);
                return null;
            }

            //add content depending on the availability of either input stream or inline content
            if (inputStream != null) {
                if (!documentation.getSourceType().equals(Documentation.DocumentSourceType.FILE)) {
                    RestApiUtil.handleBadRequest("Source type of document " + documentId + " is not FILE", log);
                }
                String filename = fileDetail.getContentDisposition().getFilename();
                if (APIUtil.isSupportedFileType(filename)) {
                    RestApiPublisherUtils.attachFileToDocument(apiId, documentation, inputStream, fileDetail, organization);
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
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while adding content to the document: " + documentId + " of API "
                                + apiId, e, log);
            } else {
                RestApiUtil.handleInternalServerError("Failed to add content to the document " + documentId, e, log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving document content location : " + documentId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } finally {
            IOUtils.closeQuietly(inputStream);
        }
        return null;
    }

    /**
     * Deletes an existing document of an API
     *
     * @param apiId      API identifier
     * @param documentId document identifier
     * @param ifMatch    If-match header value
     * @return 200 response if deleted successfully
     */
    @Override
    public Response deleteAPIDocument(String apiId, String documentId, String ifMatch,
                                                       MessageContext messageContext) {
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
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
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

    @Override
    public Response getAPIDocumentByDocumentId(String apiId, String documentId, String ifNoneMatch,
                                                    MessageContext messageContext) {
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
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while retrieving document : " + documentId + " of API " + apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving document : " + documentId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * Updates an existing document of an API
     *
     * @param apiId      API identifier
     * @param documentId document identifier
     * @param body       updated document DTO
     * @param ifMatch    If-match header value
     * @return updated document DTO as response
     */
    @Override
    public Response updateAPIDocument(String apiId, String documentId, DocumentDTO body,
                                                    String ifMatch, MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            //validate if api exists
            APIInfo apiInfo = CommonUtils.validateAPIExistence(apiId);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(apiInfo.getStatus().toString());

            String sourceUrl = body.getSourceUrl();
            Documentation oldDocument = apiProvider.getDocumentation(apiId, documentId, organization);

            //validation checks for existence of the document
            if (body.getType() == null) {
                throw new BadRequestException();
            }
            if (oldDocument == null) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_DOCUMENTATION, documentId, log);
                return null;
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
            newDocumentation.setFilePath(oldDocument.getFilePath());
            newDocumentation.setId(documentId);
            newDocumentation = apiProvider.updateDocumentation(apiId, newDocumentation, organization);

            return Response.ok().entity(DocumentationMappingUtil.fromDocumentationToDTO(newDocumentation)).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while updating document : " + documentId + " of API " + apiId, e, log);
            } else {
                String errorMessage = "Error while updating the document " + documentId + " for API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * Returns all the documents of the given API identifier that matches to the search condition
     *
     * @param apiId       API identifier
     * @param limit       max number of records returned
     * @param offset      starting index
     * @param ifNoneMatch If-None-Match header value
     * @return matched documents as a list if DocumentDTOs
     */
    @Override
    public Response getAPIDocuments(String apiId, Integer limit, Integer offset, String ifNoneMatch,
                                          MessageContext messageContext) {
        // do some magic!
        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            //this will fail if user does not have access to the API or the API does not exist
            //APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, organization);
            //List<Documentation> allDocumentation = apiProvider.getAllDocumentation(apiIdentifier);
            List<Documentation> allDocumentation = apiProvider.getAllDocumentation(apiId, organization);
            DocumentListDTO documentListDTO = DocumentationMappingUtil.fromDocumentationListToDTO(allDocumentation,
                    offset, limit);
            DocumentationMappingUtil
                    .setPaginationParams(documentListDTO, apiId, offset, limit, allDocumentation.size());
            return Response.ok().entity(documentListDTO).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
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
     * Add a documentation to an API
     *
     * @param apiId api identifier
     * @param body  Documentation DTO as request body
     * @return created document DTO as response
     */
    @Override
    public Response addAPIDocument(String apiId, DocumentDTO body, String ifMatch, MessageContext messageContext)
            throws APIManagementException {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            //validate if api exists
            APIInfo apiInfo = CommonUtils.validateAPIExistence(apiId);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(apiInfo.getStatus().toString());
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            Documentation documentation = PublisherCommonUtils.addDocumentationToAPI(body, apiId, organization);
            DocumentDTO newDocumentDTO = DocumentationMappingUtil.fromDocumentationToDTO(documentation);
            String uriString = RestApiConstants.RESOURCE_PATH_DOCUMENTS_DOCUMENT_ID
                    .replace(RestApiConstants.APIID_PARAM, apiId)
                    .replace(RestApiConstants.DOCUMENTID_PARAM, documentation.getId());
            URI uri = new URI(uriString);
            return Response.created(uri).entity(newDocumentDTO).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil
                        .handleAuthorizationFailure("Authorization failure while adding documents of API : " + apiId, e,
                                log);
            } else {
                throw new APIManagementException(
                        "Error while adding a new document to API " + apiId + " : " + e.getMessage(), e);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving location for document " + body.getName() + " of API " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Get external store list which the given API is already published to.
     * @param apiId API Identifier
     * @param ifNoneMatch If-None-Match header value
     * @param messageContext CXF Message Context
     * @return External Store list of published API
     */
    @Override
    public Response getAllPublishedExternalStoresByAPI(String apiId, String ifNoneMatch, MessageContext messageContext)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        Set<APIStore> publishedStores = apiProvider.getPublishedExternalAPIStores(apiId);
        APIExternalStoreListDTO apiExternalStoreListDTO =
                ExternalStoreMappingUtil.fromAPIExternalStoreCollectionToDTO(publishedStores);
        return Response.ok().entity(apiExternalStoreListDTO).build();
    }

    /**
     * Gets generated scripts
     *
     * @param apiId  API Id
     * @param ifNoneMatch If-None-Match header value
     * @param messageContext message context
     * @return list of policies of generated sample payload
     * @throws APIManagementException
     */
    @Override
    public Response getGeneratedMockScriptsOfAPI(String apiId, String ifNoneMatch, MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        API originalAPI = apiProvider.getAPIbyUUID(apiId, organization);
        String apiDefinition = apiProvider.getOpenAPIDefinition(apiId, organization);
        Map<String, Object> examples = OASParserUtil.generateExamples(apiDefinition);
        List<APIResourceMediationPolicy> policies = (List<APIResourceMediationPolicy>) examples.get(APIConstants.MOCK_GEN_POLICY_LIST);
        return Response.ok().entity(APIMappingUtil.fromMockPayloadsToListDTO(policies)).build();
    }

    /**
     * Retrieves the WSDL meta information of the given API. The API must be a SOAP API.
     *
     * @param apiId Id of the API
     * @param messageContext CXF Message Context
     * @return WSDL meta information of the API
     * @throws APIManagementException when error occurred while retrieving API WSDL meta info.
     *  eg: when API doesn't exist, API exists but it is not a SOAP API.
     */
    @Override
    public Response getWSDLInfoOfAPI(String apiId, MessageContext messageContext)
            throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        API api = apiProvider.getLightweightAPIByUUID(apiId, organization);
        WSDLInfoDTO wsdlInfoDTO = APIMappingUtil.getWsdlInfoDTO(api);
        if (wsdlInfoDTO == null) {
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.NO_WSDL_AVAILABLE_FOR_API,
                            api.getId().getApiName(), api.getId().getVersion()));
        } else {
            return Response.ok().entity(wsdlInfoDTO).build();
        }
    }

    /**
     * Retrieves API Lifecycle history information
     *
     * @param apiId API Id
     * @param ifNoneMatch If-None-Match header value
     * @return API Lifecycle history information
     */
    @Override
    public Response getAPILifecycleHistory(String apiId, String ifNoneMatch, MessageContext messageContext) {
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
            return Response.ok().entity(PublisherCommonUtils.getLifecycleHistoryDTO(api.getUuid(), apiProvider)).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("Authorization failure while retrieving the lifecycle " +
                        "events of API : " + apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving the lifecycle events of API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * Retrieves API Lifecycle state information
     *
     * @param apiId API Id
     * @param ifNoneMatch If-None-Match header value
     * @return API Lifecycle state information
     */
    @Override
    public Response getAPILifecycleState(String apiId, String ifNoneMatch, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        LifecycleStateDTO lifecycleStateDTO = getLifecycleState(apiId, organization);
        return Response.ok().entity(lifecycleStateDTO).build();
    }

    /**
     * Retrieves API Lifecycle state information
     *
     * @param apiId API Id
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
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("Authorization failure while deleting API : " + apiId, e, log);
            } else {
                throw e;
            }
        }
        return null;
    }

    @Override
    public Response deleteAPILifecycleStatePendingTasks(String apiId, MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            APIIdentifier apiIdentifierFromTable = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
            if (apiIdentifierFromTable == null) {
                throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API with API UUID: " + apiId,
                        ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND, apiId));
            }
            apiProvider.deleteWorkflowTask(apiIdentifierFromTable);
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while deleting task ";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Get API monetization status and monetized tier to billing plan mapping
     *
     * @param apiId API ID
     * @param messageContext message context
     * @return API monetization status and monetized tier to billing plan mapping
     */
    @Override
    public Response getAPIMonetization(String apiId, MessageContext messageContext) {

        try {
            if (StringUtils.isBlank(apiId)) {
                String errorMessage = "API ID cannot be empty or null when retrieving monetized plans.";
                RestApiUtil.handleBadRequest(errorMessage, log);
            }
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            String uuid;
            APIRevision apiRevision = ApiMgtDAO.getInstance().checkAPIUUIDIsARevisionUUID(apiId);
            if (apiRevision != null && apiRevision.getApiUUID() != null) {
                uuid = apiRevision.getApiUUID();
            } else {
                uuid = apiId;
            }
            API api = apiProvider.getAPIbyUUID(apiId, organization);
            Monetization monetizationImplementation = apiProvider.getMonetizationImplClass();
            Map<String, String> monetizedPoliciesToPlanMapping = monetizationImplementation.
                    getMonetizedPoliciesToPlanMapping(api);
            APIMonetizationInfoDTO monetizationInfoDTO = APIMappingUtil.getMonetizedTiersDTO
                    (uuid, organization, monetizedPoliciesToPlanMapping);
            return Response.ok().entity(monetizationInfoDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Failed to retrieve monetized plans for API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (MonetizationException e) {
            String errorMessage = "Failed to fetch monetized plans of API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return Response.serverError().build();
    }

    /**
     * Monetize (enable or disable) for a given API
     *
     * @param apiId API ID
     * @param body request body
     * @param messageContext message context
     * @return monetizationDTO
     */
    @Override
    public Response addAPIMonetization(String apiId, APIMonetizationInfoDTO body, MessageContext messageContext) {
        try {
            if (StringUtils.isBlank(apiId)) {
                String errorMessage = "API ID cannot be empty or null when configuring monetization.";
                RestApiUtil.handleBadRequest(errorMessage, log);
            }
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
            if (apiIdentifier == null) {
                throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API with API UUID: "
                        + apiId, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND,
                        apiId));
            }
            API api = apiProvider.getAPIbyUUID(apiId, organization);
            if (!APIConstants.PUBLISHED.equalsIgnoreCase(api.getStatus())) {
                String errorMessage = "API " + apiIdentifier.getApiName() +
                        " should be in published state to configure monetization.";
                RestApiUtil.handleBadRequest(errorMessage, log);
            }
            //set the monetization status
            boolean monetizationEnabled = body.isEnabled();
            api.setMonetizationStatus(monetizationEnabled);
            //clear the existing properties related to monetization
            api.getMonetizationProperties().clear();
            Map<String, String> monetizationProperties = body.getProperties();
            if (MapUtils.isNotEmpty(monetizationProperties)) {
                String errorMessage = RestApiPublisherUtils.validateMonetizationProperties(monetizationProperties);
                if (!errorMessage.isEmpty()) {
                    RestApiUtil.handleBadRequest(errorMessage, log);
                }
                for (Map.Entry<String, String> currentEntry : monetizationProperties.entrySet()) {
                    api.addMonetizationProperty(currentEntry.getKey(), currentEntry.getValue());
                }
            }
            Monetization monetizationImplementation = apiProvider.getMonetizationImplClass();
            HashMap monetizationDataMap = new Gson().fromJson(api.getMonetizationProperties().toString(), HashMap.class);
            boolean isMonetizationStateChangeSuccessful = false;
            if (MapUtils.isEmpty(monetizationDataMap)) {
                String errorMessage = "Monetization is not configured. Monetization data is empty for "
                        + apiIdentifier.getApiName();
                RestApiUtil.handleBadRequest(errorMessage, log);
            }
            try {
                if (monetizationEnabled) {
                    isMonetizationStateChangeSuccessful = monetizationImplementation.enableMonetization
                            (organization, api, monetizationDataMap);
                } else {
                    isMonetizationStateChangeSuccessful = monetizationImplementation.disableMonetization
                            (organization, api, monetizationDataMap);
                }
            } catch (MonetizationException e) {
                String errorMessage = "Error while changing monetization status for API ID : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
            if (isMonetizationStateChangeSuccessful) {
                apiProvider.configureMonetizationInAPIArtifact(api);
                APIMonetizationInfoDTO monetizationInfoDTO = APIMappingUtil.getMonetizationInfoDTO(apiId, organization);
                return Response.ok().entity(monetizationInfoDTO).build();
            } else {
                String errorMessage = "Unable to change monetization status for API : " + apiId;
                RestApiUtil.handleBadRequest(errorMessage, log);
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while configuring monetization for API ID : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return Response.serverError().build();
    }

    /**
     * Add an API specific operation policy
     *
     * @param apiId                                  UUID of the API
     * @param policySpecFileInputStream              Input stream of the policy specification file
     * @param policySpecFileDetail                   Operation policy specification
     * @param synapsePolicyDefinitionFileInputStream Input stream of the synapse policy definition file
     * @param synapsePolicyDefinitionFileDetail      Synapse definition of the operation policy
     * @param ccPolicyDefinitionFileInputStream      Input stream of the choreo connect policy definition file
     * @param ccPolicyDefinitionFileDetail           Choreo connect definition of the operation policy
     * @param messageContext                         message context
     * @return Added Operation operation policy DTO as response
     */
    @Override
    public Response addAPISpecificOperationPolicy(String apiId, InputStream policySpecFileInputStream,
                                                  Attachment policySpecFileDetail,
                                                  InputStream synapsePolicyDefinitionFileInputStream,
                                                  Attachment synapsePolicyDefinitionFileDetail,
                                                  InputStream ccPolicyDefinitionFileInputStream,
                                                  Attachment ccPolicyDefinitionFileDetail,
                                                  MessageContext messageContext) {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);

            //validate if api exists
            CommonUtils.validateAPIExistence(apiId);
            String jsonContent = "";
            OperationPolicyDefinition synapseDefinition = null;
            OperationPolicyDefinition ccPolicyDefinition = null;
            OperationPolicySpecification policySpecification;
            if (policySpecFileInputStream != null) {
                jsonContent = RestApiPublisherUtils.readInputStream(policySpecFileInputStream, policySpecFileDetail);

                String fileName = policySpecFileDetail.getDataHandler().getName();
                if (fileName.endsWith(APIConstants.YAML_FILE_EXTENSION) ||
                        fileName.endsWith(APIConstants.YML_FILE_EXTENSION)) {
                    jsonContent = CommonUtil.yamlToJson(jsonContent);
                }

                policySpecification = APIUtil.getValidatedOperationPolicySpecification(jsonContent);

                OperationPolicyData operationPolicyData = OperationPoliciesApiServiceImplUtils
                        .prepareOperationPolicyData(policySpecification, organization, apiId);

                if (synapsePolicyDefinitionFileInputStream != null) {
                    String synapsePolicyDefinition =
                            RestApiPublisherUtils.readInputStream(synapsePolicyDefinitionFileInputStream,
                                    synapsePolicyDefinitionFileDetail);
                    synapseDefinition = new OperationPolicyDefinition();
                    OperationPoliciesApiServiceImplUtils
                            .preparePolicyDefinition(operationPolicyData, synapseDefinition,
                                    synapsePolicyDefinition, OperationPolicyDefinition.GatewayType.Synapse);
                }

                if (ccPolicyDefinitionFileInputStream != null) {
                    String choreoConnectPolicyDefinition = RestApiPublisherUtils
                            .readInputStream(ccPolicyDefinitionFileInputStream, ccPolicyDefinitionFileDetail);
                    ccPolicyDefinition = new OperationPolicyDefinition();
                    OperationPoliciesApiServiceImplUtils
                            .preparePolicyDefinition(operationPolicyData, ccPolicyDefinition,
                                    choreoConnectPolicyDefinition, OperationPolicyDefinition.GatewayType.ChoreoConnect);
                }

                OperationPolicyData existingPolicy =
                        apiProvider.getAPISpecificOperationPolicyByPolicyName(policySpecification.getName(),
                                policySpecification.getVersion(), apiId, null, organization, false);
                String policyID;
                if (existingPolicy == null) {
                    policyID = apiProvider.addAPISpecificOperationPolicy(apiId, operationPolicyData, organization);
                    if (log.isDebugEnabled()) {
                        log.debug("An API specific operation policy has been added for the API " + apiId +
                                " with id " + policyID);
                    }
                    APIUtil.logAuditMessage(APIConstants.AuditLogConstants.OPERATION_POLICY, policyID,
                            APIConstants.AuditLogConstants.CREATED, RestApiCommonUtil.getLoggedInUsername());
                } else {
                    throw new APIManagementException("An API specific operation policy found for the same name.");
                }
                operationPolicyData.setPolicyId(policyID);
                OperationPolicyDataDTO operationPolicyDataDTO = OperationPolicyMappingUtil
                        .fromOperationPolicyDataToDTO(operationPolicyData);
                URI createdPolicyUri = new URI(RestApiConstants.REST_API_PUBLISHER_VERSION
                        + RestApiConstants.RESOURCE_PATH_APIS + "/" + apiId + "/"
                        + RestApiConstants.RESOURCE_PATH_OPERATION_POLICIES + "/" + policyID);
                return Response.created(createdPolicyUri).entity(operationPolicyDataDTO).build();
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while adding an API specific operation policy." + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (Exception e) {
            RestApiUtil.handleInternalServerError("An error has occurred while adding an API specific " +
                    "operation policy", e, log);
        }
        return null;
    }

    /**
     * Get the list of all API specific operation policies for a given API
     *
     * @param apiId          API UUID
     * @param limit          max number of records returned
     * @param offset         starting index
     * @param messageContext message context
     * @return A list of operation policies available for the API
     */
    @Override
    public Response getAllAPISpecificOperationPolicies(String apiId, Integer limit, Integer offset, String query,
                                                       MessageContext messageContext) {

        try {
            CommonUtils.validateAPIExistence(apiId);
            limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
            offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);

            // Lightweight API specific operation policy includes the policy ID and the policy specification.
            // Since policy definition is bit bulky, we don't query the definition unnecessarily.
            List<OperationPolicyData> sharedOperationPolicyLIst = apiProvider
                    .getAllAPISpecificOperationPolicies(apiId, organization);
            OperationPolicyDataListDTO policyListDTO = OperationPolicyMappingUtil
                    .fromOperationPolicyDataListToDTO(sharedOperationPolicyLIst, offset, limit);
            return Response.ok().entity(policyListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage =
                    "Error while retrieving the list of all API specific operation policies." + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (Exception e) {
            RestApiUtil.handleInternalServerError("An error has occurred while getting the list of API specific " +
                    " operation policies", e, log);
        }
        return null;
    }

    /**
     * Get the API specific operation policy specification by providing the policy ID
     *
     * @param apiId             API UUID
     * @param operationPolicyId UUID of the operation policy
     * @param messageContext    message context
     * @return Operation policy DTO as response
     */
    @Override
    public Response getOperationPolicyForAPIByPolicyId(String apiId, String operationPolicyId,
                                                       MessageContext messageContext) {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);

            //validate whether api exists or not
            CommonUtils.validateAPIExistence(apiId);

            OperationPolicyData existingPolicy =
                    apiProvider.getAPISpecificOperationPolicyByPolicyId(operationPolicyId, apiId, organization, false);
            if (existingPolicy != null) {
                OperationPolicyDataDTO policyDataDTO =
                        OperationPolicyMappingUtil.fromOperationPolicyDataToDTO(existingPolicy);
                return Response.ok().entity(policyDataDTO).build();
            } else {
                throw new APIMgtResourceNotFoundException("Couldn't retrieve an existing operation policy with ID: "
                        + operationPolicyId + " for API " + apiId,
                        ExceptionCodes.from(ExceptionCodes.OPERATION_POLICY_NOT_FOUND, operationPolicyId));
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_PATH_OPERATION_POLICIES,
                        operationPolicyId, e, log);
            } else {
                String errorMessage =
                        "Error while getting an API specific operation policy with ID :" + operationPolicyId
                                + " for API " + apiId + " " + e.getMessage();
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (Exception e) {
            RestApiUtil.handleInternalServerError("An Error has occurred while getting the operation policy with ID " +
                    operationPolicyId + "operation policy for API " + apiId, e, log);
        }
        return null;
    }

    /**
     * Download the operation policy specification and definition for a given API specific policy
     *
     * @param apiId             API UUID
     * @param operationPolicyId UUID of the operation policy
     * @param messageContext    message context
     * @return A zip file containing both (if exists) operation policy specification and policy definition
     */
    @Override
    public Response getAPISpecificOperationPolicyContentByPolicyId(String apiId, String operationPolicyId,
                                                                   MessageContext messageContext) {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);

            //validate if api exists
            CommonUtils.validateAPIExistence(apiId);

            OperationPolicyData policyData =
                    apiProvider.getAPISpecificOperationPolicyByPolicyId(operationPolicyId, apiId, organization, true);
            if (policyData != null) {
                File file = RestApiPublisherUtils.exportOperationPolicyData(policyData, ExportFormat.YAML.name());
                return Response.ok(file).header(RestApiConstants.HEADER_CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getName() + "\"").build();
            } else {
                throw new APIMgtResourceNotFoundException("Couldn't retrieve an existing operation policy with ID: "
                        + operationPolicyId + " for API " + apiId,
                        ExceptionCodes.from(ExceptionCodes.OPERATION_POLICY_NOT_FOUND, operationPolicyId));
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_PATH_OPERATION_POLICIES,
                        operationPolicyId, e, log);
            } else {
                String errorMessage =
                        "Error while getting an API specific operation policy with ID :" + operationPolicyId
                                + " for API " + apiId + " " + e.getMessage();
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (Exception e) {
            RestApiUtil.handleInternalServerError("An Error has occurred while exporting the API specific" +
                    " operation policy with ID" + operationPolicyId + " for API " + apiId, e, log);
        }
        return null;
    }

    /**
     * Delete API specific operation policy by providing the policy ID
     *
     * @param apiId             API UUID
     * @param operationPolicyId UUID of the operation policy
     * @param messageContext    message context
     * @return A zip file containing both (if exists) operation policy specification and policy definition
     */
    @Override
    public Response deleteAPISpecificOperationPolicyByPolicyId(String apiId, String operationPolicyId,
                                                               MessageContext messageContext) {

        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

            //validate if api exists
            CommonUtils.validateAPIExistence(apiId);
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            OperationPolicyData existingPolicy =
                    apiProvider.getAPISpecificOperationPolicyByPolicyId(operationPolicyId, apiId, organization, false);
            if (existingPolicy != null) {
                apiProvider.deleteOperationPolicyById(operationPolicyId, organization);

                if (log.isDebugEnabled()) {
                    log.debug("The operation policy " + operationPolicyId + " has been deleted from the the API "
                            + apiId);
                }
                APIUtil.logAuditMessage(APIConstants.AuditLogConstants.OPERATION_POLICY, operationPolicyId,
                        APIConstants.AuditLogConstants.DELETED, RestApiCommonUtil.getLoggedInUsername());
                return Response.ok().build();
            } else {
                throw new APIMgtResourceNotFoundException("Couldn't retrieve an existing operation policy with ID: "
                        + operationPolicyId + " for API " + apiId,
                        ExceptionCodes.from(ExceptionCodes.OPERATION_POLICY_NOT_FOUND, operationPolicyId));
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_PATH_OPERATION_POLICIES,
                        operationPolicyId, e, log);
            } else {
                String errorMessage =
                        "Error while deleting the API specific operation policy with ID :" + operationPolicyId
                                + " for API " + apiId + " " + e.getMessage();
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (Exception e) {
            RestApiUtil.handleInternalServerError("An error has occurred while deleting the API specific " +
                    " operation policy with ID" + operationPolicyId + " for API " + apiId, e, log);
        }
        return null;
    }


    /**
     * Publish API to given external stores.
     *
     * @param apiId API Id
     * @param externalStoreIds  External Store Ids
     * @param ifMatch   If-match header value
     * @param messageContext CXF Message Context
     * @return Response of published external store list
     */
    @Override
    public Response publishAPIToExternalStores(String apiId, String externalStoreIds, String ifMatch,
                                                         MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        API api = null;
        List<String> externalStoreIdList;
        if (externalStoreIds != null) {
            externalStoreIdList = Arrays.asList(externalStoreIds.split("\\s*,\\s*"));
        } else {
            externalStoreIdList = new ArrayList<>();
        }
        try {
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
            if (apiIdentifier == null) {
                throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API with API UUID: "
                        + apiId, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND,
                        apiId));
            }
            api = apiProvider.getAPIbyUUID(apiId, organization);
            api.setOrganization(organization);
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                String errorMessage = "Error while getting API: " + apiId;
                log.error(errorMessage, e);
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        if (apiProvider.publishToExternalAPIStores(api, externalStoreIdList)) {
            Set<APIStore> publishedStores = apiProvider.getPublishedExternalAPIStores(api.getUuid());
            APIExternalStoreListDTO apiExternalStoreListDTO =
                    ExternalStoreMappingUtil.fromAPIExternalStoreCollectionToDTO(publishedStores);
            return Response.ok().entity(apiExternalStoreListDTO).build();
        }
        return Response.serverError().build();
    }

    /**
     * Get the resource policies(inflow/outflow).
     *
     * @param apiId           API ID
     * @param sequenceType    sequence type('in' or 'out')
     * @param resourcePath    api resource path
     * @param verb            http verb
     * @param ifNoneMatch     If-None-Match header value
     * @return json response of the resource policies according to the resource path
     */
    @Override
    public Response getAPIResourcePolicies(String apiId, String sequenceType, String resourcePath,
            String verb, String ifNoneMatch, MessageContext messageContext) {
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIProvider provider = RestApiCommonUtil.getLoggedInUserProvider();
            API api = provider.getLightweightAPIByUUID(apiId, organization);
            if (APIConstants.API_TYPE_SOAPTOREST.equals(api.getType())) {
                if (StringUtils.isEmpty(sequenceType) || !(RestApiConstants.IN_SEQUENCE.equals(sequenceType)
                        || RestApiConstants.OUT_SEQUENCE.equals(sequenceType))) {
                    String errorMessage = "Sequence type should be either of the values from 'in' or 'out'";
                    RestApiUtil.handleBadRequest(errorMessage, log);
                }
                String resourcePolicy = SequenceUtils.getRestToSoapConvertedSequence(api, sequenceType);
                if (StringUtils.isEmpty(resourcePath) && StringUtils.isEmpty(verb)) {
                    ResourcePolicyListDTO resourcePolicyListDTO = APIMappingUtil
                            .fromResourcePolicyStrToDTO(resourcePolicy);
                    return Response.ok().entity(resourcePolicyListDTO).build();
                }
                if (StringUtils.isNotEmpty(resourcePath) && StringUtils.isNotEmpty(verb)) {
                    JSONObject sequenceObj = (JSONObject) new JSONParser().parse(resourcePolicy);
                    JSONObject resultJson = new JSONObject();
                    String key = resourcePath + "_" + verb;
                    JSONObject sequenceContent = (JSONObject) sequenceObj.get(key);
                    if (sequenceContent == null) {
                        String errorMessage = "Cannot find any resource policy for Resource path : " + resourcePath +
                                " with type: " + verb;
                        RestApiUtil.handleResourceNotFoundError(errorMessage, log);
                    }
                    resultJson.put(key, sequenceObj.get(key));
                    ResourcePolicyListDTO resourcePolicyListDTO = APIMappingUtil
                            .fromResourcePolicyStrToDTO(resultJson.toJSONString());
                    return Response.ok().entity(resourcePolicyListDTO).build();
                } else if (StringUtils.isEmpty(resourcePath)) {
                    String errorMessage = "Resource path cannot be empty for the defined verb: " + verb;
                    RestApiUtil.handleBadRequest(errorMessage, log);
                } else if (StringUtils.isEmpty(verb)) {
                    String errorMessage = "HTTP verb cannot be empty for the defined resource path: " + resourcePath;
                    RestApiUtil.handleBadRequest(errorMessage, log);
                }
            } else {
                String errorMessage = "The provided api with id: " + apiId + " is not a soap to rest converted api.";
                RestApiUtil.handleBadRequest(errorMessage, log);
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving the API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (ParseException e) {
            String errorMessage = "Error while retrieving the resource policies for the API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Get the resource policy given the resource id.
     *
     * @param apiId           API ID
     * @param resourcePolicyId      resource policy id
     * @param ifNoneMatch     If-None-Match header value
     * @return json response of the resource policy for the resource id given
     */
    @Override
    public Response getAPIResourcePoliciesByPolicyId(String apiId, String resourcePolicyId,
            String ifNoneMatch, MessageContext messageContext) {
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIProvider provider = RestApiCommonUtil.getLoggedInUserProvider();
            API api = provider.getLightweightAPIByUUID(apiId, organization);
            if (APIConstants.API_TYPE_SOAPTOREST.equals(api.getType())) {
                if (StringUtils.isEmpty(resourcePolicyId)) {
                    String errorMessage = "Resource id should not be empty to update a resource policy.";
                    RestApiUtil.handleBadRequest(errorMessage, log);
                }
                String policyContent = SequenceUtils.getResourcePolicyFromRegistryResourceId(api, resourcePolicyId);
                ResourcePolicyInfoDTO resourcePolicyInfoDTO = APIMappingUtil
                        .fromResourcePolicyStrToInfoDTO(policyContent);
                return Response.ok().entity(resourcePolicyInfoDTO).build();
            } else {
                String errorMessage = "The provided api with id: " + apiId + " is not a soap to rest converted api.";
                RestApiUtil.handleBadRequest(errorMessage, log);
            }
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving the API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Update the resource policies(inflow/outflow) given the resource id.
     *
     * @param apiId  API ID
     * @param resourcePolicyId resource policy id
     * @param body resource policy content
     * @param ifMatch If-Match header value
     * @return json response of the updated sequence content
     */
    @Override
    public Response updateAPIResourcePoliciesByPolicyId(String apiId, String resourcePolicyId,
            ResourcePolicyInfoDTO body, String ifMatch, MessageContext messageContext) {
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIProvider provider = RestApiCommonUtil.getLoggedInUserProvider();
            API api = provider.getAPIbyUUID(apiId, organization);
            if (api == null) {
                throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API with API UUID: "
                        + apiId, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND,
                        apiId));
            }
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(api.getStatus());

            if (APIConstants.API_TYPE_SOAPTOREST.equals(api.getType())) {
                if (StringUtils.isEmpty(resourcePolicyId)) {
                    String errorMessage = "Resource id should not be empty to update a resource policy.";
                    RestApiUtil.handleBadRequest(errorMessage, log);
                }
                boolean isValidSchema = RestApiPublisherUtils.validateXMLSchema(body.getContent());
                if (isValidSchema) {
                    List<SOAPToRestSequence> sequence = api.getSoapToRestSequences();
                    for (SOAPToRestSequence soapToRestSequence : sequence) {
                        if (soapToRestSequence.getUuid().equals(resourcePolicyId)) {
                            soapToRestSequence.setContent(body.getContent());
                            break;
                        }
                    }
                    API originalAPI = provider.getAPIbyUUID(apiId, organization);
                    provider.updateAPI(api, originalAPI);
                    SequenceUtils.updateResourcePolicyFromRegistryResourceId(api.getId(), resourcePolicyId,
                            body.getContent());
                    String updatedPolicyContent = SequenceUtils
                            .getResourcePolicyFromRegistryResourceId(api, resourcePolicyId);
                    ResourcePolicyInfoDTO resourcePolicyInfoDTO = APIMappingUtil
                            .fromResourcePolicyStrToInfoDTO(updatedPolicyContent);
                    return Response.ok().entity(resourcePolicyInfoDTO).build();
                } else {
                    String errorMessage =
                            "Error while validating the resource policy xml content for the API : " + apiId;
                    RestApiUtil.handleInternalServerError(errorMessage, log);
                }
            } else {
                String errorMessage = "The provided api with id: " + apiId + " is not a soap to rest converted api.";
                RestApiUtil.handleBadRequest(errorMessage, log);
            }
        } catch (APIManagementException | FaultGatewaysException e) {
            String errorMessage = "Error while retrieving the API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Get total revenue for a given API from all its' subscriptions
     *
     * @param apiId API ID
     * @param messageContext message context
     * @return revenue data for a given API
     */
    @Override
    public Response getAPIRevenue(String apiId, MessageContext messageContext) {

        if (StringUtils.isBlank(apiId)) {
            String errorMessage = "API ID cannot be empty or null when getting revenue details.";
            RestApiUtil.handleBadRequest(errorMessage, log);
        }
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            Monetization monetizationImplementation = apiProvider.getMonetizationImplClass();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);

            API api = apiProvider.getAPIbyUUID(apiId, organization);
            if (!APIConstants.PUBLISHED.equalsIgnoreCase(api.getStatus())) {
                String errorMessage = "API " + api.getId().getName() +
                        " should be in published state to get total revenue.";
                RestApiUtil.handleBadRequest(errorMessage, log);
            }
            Map<String, String> revenueUsageData = monetizationImplementation.getTotalRevenue(api, apiProvider);
            APIRevenueDTO apiRevenueDTO = new APIRevenueDTO();
            apiRevenueDTO.setProperties(revenueUsageData);
            return Response.ok().entity(apiRevenueDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Failed to retrieve revenue data for API ID : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (MonetizationException e) {
            String errorMessage = "Failed to get current revenue data for API ID : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Retrieves the swagger document of an API
     *
     * @param apiId           API identifier
     * @param ifNoneMatch     If-None-Match header value
     * @return Swagger document of the API
     */
    @Override
    public Response getAPISwagger(String apiId, String ifNoneMatch, MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            //this will fail if user does not have access to the API or the API does not exist
            API api = apiProvider.getAPIbyUUID(apiId, organization);
            api.setOrganization(organization);
            String updatedDefinition = RestApiCommonUtil.retrieveSwaggerDefinition(apiId, api, apiProvider);
            return Response.ok().entity(updatedDefinition).header("Content-Disposition",
                    "attachment; filename=\"" + "swagger.json" + "\"" ).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil
                        .handleAuthorizationFailure("Authorization failure while retrieving swagger of API : " + apiId,
                                e, log);
            } else {
                String errorMessage = "Error while retrieving swagger of API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }
    /**
     * Updates the swagger definition of an existing API
     *
     * @param apiId             API identifier
     * @param apiDefinition     Swagger definition
     * @param url               Swagger definition URL
     * @param fileInputStream   Swagger definition input file content
     * @param fileDetail        file meta information as Attachment
     * @param ifMatch           If-match header value
     * @return updated swagger document of the API
     */
    @Override
    public Response updateAPISwagger(String apiId, String ifMatch, String apiDefinition, String url,
                                     InputStream fileInputStream, Attachment fileDetail,MessageContext messageContext) {
        try {
            String updatedSwagger;
            //validate if api exists
            APIInfo apiInfo = CommonUtils.validateAPIExistence(apiId);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(apiInfo.getStatus());
            String organization = RestApiUtil.getValidatedOrganization(messageContext);

            //Handle URL and file based definition imports
            if(url != null || fileInputStream != null) {
                // Validate and retrieve the OpenAPI definition
                Map validationResponseMap = validateOpenAPIDefinition(url, fileInputStream, fileDetail, null,
                        true, false);
                APIDefinitionValidationResponse validationResponse =
                        (APIDefinitionValidationResponse) validationResponseMap .get(RestApiConstants.RETURN_MODEL);
                if (!validationResponse.isValid()) {
                    RestApiUtil.handleBadRequest(validationResponse.getErrorItems(), log);
                }
                updatedSwagger = PublisherCommonUtils.updateSwagger(apiId, validationResponse, false, organization);
            } else {
                updatedSwagger = updateSwagger(apiId, apiDefinition, organization);
            }
            return Response.ok().entity(updatedSwagger).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need
            // to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while updating swagger definition of API: " + apiId, e, log);
            } else {
                String errorMessage = "Error while updating the swagger definition of the API: " + apiId + " - "
                        + e.getMessage();
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (FaultGatewaysException e) {
            String errorMessage = "Error while updating API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * update swagger definition of the given api. The swagger will be validated before updating.
     *
     * @param apiId API Id
     * @param apiDefinition swagger definition
     * @param organization  Organization Identifier
     * @return updated swagger definition
     * @throws APIManagementException when error occurred updating swagger
     * @throws FaultGatewaysException when error occurred publishing API to the gateway
     */
    private String updateSwagger(String apiId, String apiDefinition, String organization)
            throws APIManagementException, FaultGatewaysException {
        APIDefinitionValidationResponse response = OASParserUtil
                .validateAPIDefinition(apiDefinition, true);
        if (!response.isValid()) {
            RestApiUtil.handleBadRequest(response.getErrorItems(), log);
        }
        return PublisherCommonUtils.updateSwagger(apiId, response, false, organization);
    }

    /**
     * Retrieves the thumbnail image of an API specified by API identifier
     *
     * @param apiId           API Id
     * @param ifNoneMatch     If-None-Match header value
     * @param messageContext If-Modified-Since header value
     * @return Thumbnail image of the API
     */
    @Override
    public Response getAPIThumbnail(String apiId, String ifNoneMatch, MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            //this will fail if user does not have access to the API or the API does not exist
            //APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, tenantDomain);
            ResourceFile thumbnailResource = apiProvider.getIcon(apiId, organization);

            if (thumbnailResource != null) {
                return Response
                        .ok(thumbnailResource.getContent(), MediaType.valueOf(thumbnailResource.getContentType()))
                        .build();
            } else {
                return Response.noContent().build();
            }
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while retrieving thumbnail of API : " + apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving thumbnail of API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    @Override
    public Response updateAPIThumbnail(String apiId, InputStream fileInputStream, Attachment fileDetail,
            String ifMatch, MessageContext messageContext) {
        ByteArrayInputStream inputStream = null;
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

            //validate if api exists
            APIInfo apiInfo = CommonUtils.validateAPIExistence(apiId);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(apiInfo.getStatus().toString());

            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            String fileName = fileDetail.getDataHandler().getName();
            String extension = FilenameUtils.getExtension(fileName);
            if (!RestApiConstants.ALLOWED_THUMBNAIL_EXTENSIONS.contains(extension.toLowerCase())) {
                RestApiUtil.handleBadRequest(
                        "Unsupported Thumbnail File Extension. Supported extensions are .jpg, .png, .jpeg, .svg "
                                + "and .gif", log);
            }
            inputStream = (ByteArrayInputStream) RestApiPublisherUtils.validateThumbnailContent(fileInputStream);
            String fileMediaType = RestApiPublisherUtils.getMediaType(inputStream, fileDetail);
            PublisherCommonUtils.updateThumbnail(inputStream, fileMediaType, apiProvider, apiId, organization);
            String uriString = RestApiConstants.RESOURCE_PATH_THUMBNAIL.replace(RestApiConstants.APIID_PARAM, apiId);
            URI uri = new URI(uriString);
            FileInfoDTO infoDTO = new FileInfoDTO();
            infoDTO.setRelativePath(uriString);
            infoDTO.setMediaType(fileMediaType);
            return Response.created(uri).entity(infoDTO).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the
            // existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil
                        .handleAuthorizationFailure("Authorization failure while adding thumbnail for API : " + apiId,
                                e, log);
            } else {
                String errorMessage = "Error while updating thumbnail of API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (URISyntaxException | IOException e) {
            String errorMessage = "Error while updating thumbnail of API: " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } finally {
            IOUtils.closeQuietly(fileInputStream);
            IOUtils.closeQuietly(inputStream);
        }
        return null;
    }

    @Override
    public Response validateAPI(String query, String ifNoneMatch, MessageContext messageContext) {

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
                default: // API version validation.
                    isSearchArtifactExists = apiProvider.isContextExist(queryTokens[1], organization);
                    break;
                }

            } else { // consider the query as api name
                isSearchArtifactExists =
                        apiProvider.isApiNameExist(query, organization) ||
                                apiProvider.isApiNameWithDifferentCaseExist(query, organization);
            }
        } catch(APIManagementException e){
            RestApiUtil.handleInternalServerError("Error while checking the api existence", e, log);
        }
        return isSearchArtifactExists ? Response.status(Response.Status.OK).build() :
                Response.status(Response.Status.NOT_FOUND).build();
    }

    @Override
    public Response validateDocument(String apiId, String name, String ifMatch, MessageContext messageContext) {
        if (StringUtils.isEmpty(name) || StringUtils.isEmpty(apiId)) {
            RestApiUtil.handleBadRequest("API Id and/ or document name should not be empty", log);
        }
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
            if (apiIdentifier == null) {
                throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API with API UUID: "
                        + apiId, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND,
                        apiId));
            }
            return apiProvider.isDocumentationExist(apiId, name, organization) ? Response.status(Response.Status.OK).build() :
                    Response.status(Response.Status.NOT_FOUND).build();

        } catch(APIManagementException e){
            RestApiUtil.handleInternalServerError("Error while checking the api existence", e, log);
        }
        return Response.status(Response.Status.NOT_FOUND).build();
    }

    @Override
    public Response validateEndpoint(String endpointUrl, String apiId, MessageContext messageContext) {

        ApiEndpointValidationResponseDTO apiEndpointValidationResponseDTO = new ApiEndpointValidationResponseDTO();
        apiEndpointValidationResponseDTO.setError("");
        try {
            APIEndpointValidationDTO apiEndpointValidationDTO = ApisApiServiceImplUtils.sendHttpHEADRequest(endpointUrl);
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

    @Override
    public Response getAPIResourcePaths(String apiId, Integer limit, Integer offset, String ifNoneMatch,
            MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
            if (apiIdentifier == null) {
                throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API with API UUID: "
                        + apiId, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND,
                        apiId));
            }
            List<ResourcePath> apiResourcePaths = apiProvider.getResourcePathsOfAPI(apiIdentifier);

            ResourcePathListDTO dto = APIMappingUtil.fromResourcePathListToDTO(apiResourcePaths, limit, offset);
            APIMappingUtil.setPaginationParamsForAPIResourcePathList(dto, offset, limit, apiResourcePaths.size());
            return Response.ok().entity(dto).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while retrieving resource paths of API : " + apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving resource paths of API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * Validate API Definition and retrieve as the response
     *
     * @param url URL of the OpenAPI definition
     * @param fileInputStream InputStream for the provided file
     * @param fileDetail File meta-data
     * @param returnContent Whether to return the definition content
     * @param inlineApiDefinition Swagger API definition String
     * @param messageContext CXF message context
     * @return API Definition validation response
     */
    @Override
    public Response validateOpenAPIDefinition(Boolean returnContent, String url, InputStream fileInputStream,
            Attachment fileDetail, String inlineApiDefinition, MessageContext messageContext) {

        // Validate and retrieve the OpenAPI definition
        Map validationResponseMap = null;
        try {
            validationResponseMap = validateOpenAPIDefinition(url, fileInputStream, fileDetail, inlineApiDefinition,
                    returnContent, false);
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error occurred while validating API Definition", e, log);
        }

        OpenAPIDefinitionValidationResponseDTO validationResponseDTO = (OpenAPIDefinitionValidationResponseDTO) validationResponseMap
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
     * Importing an OpenAPI definition and create an API
     *
     * @param fileInputStream InputStream for the provided file
     * @param fileDetail File meta-data
     * @param url URL of the OpenAPI definition
     * @param additionalProperties API object (json) including additional properties like name, version, context
     * @param inlineApiDefinition Swagger API definition String
     * @param messageContext CXF message context
     * @return API Import using OpenAPI definition response
     * @throws APIManagementException when error occurs while importing the OpenAPI definition
     */
    @Override
    public Response importOpenAPIDefinition(InputStream fileInputStream, Attachment fileDetail, String url,
                                            String additionalProperties, String inlineApiDefinition,
                                            MessageContext messageContext) throws APIManagementException {
        // validate 'additionalProperties' json
        if (StringUtils.isBlank(additionalProperties)) {
            throw new APIManagementException("'additionalProperties' is required and should not be null",
                    ExceptionCodes.ADDITIONAL_PROPERTIES_CANNOT_BE_NULL);
        }

        // Convert the 'additionalProperties' json into an APIDTO object
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

        // validate sandbox and production endpoints
        if (!PublisherCommonUtils.validateEndpoints(apiDTOFromProperties)) {
            throw new APIManagementException("Invalid/Malformed endpoint URL(s) detected",
                    ExceptionCodes.INVALID_ENDPOINT_URL);
        }

        try {
            LinkedHashMap endpointConfig = (LinkedHashMap) apiDTOFromProperties.getEndpointConfig();

            // OAuth 2.0 backend protection: API Key and API Secret encryption
            PublisherCommonUtils
                    .encryptEndpointSecurityOAuthCredentials(endpointConfig, CryptoUtil.getDefaultCryptoUtil(),
                            StringUtils.EMPTY, StringUtils.EMPTY, apiDTOFromProperties);
            PublisherCommonUtils
                    .encryptEndpointSecurityApiKeyCredentials(endpointConfig, CryptoUtil.getDefaultCryptoUtil(),
                            StringUtils.EMPTY, StringUtils.EMPTY, apiDTOFromProperties);

            // Import the API and Definition
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIDTO createdApiDTO = importOpenAPIDefinition(fileInputStream, url, inlineApiDefinition,
                    apiDTOFromProperties, fileDetail, null, organization);
            if (createdApiDTO != null) {
                // This URI used to set the location header of the POST response
                URI createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + createdApiDTO.getId());
                return Response.created(createdApiUri).entity(createdApiDTO).build();
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving API location : " + apiDTOFromProperties.getProvider() + "-" +
                    apiDTOFromProperties.getName() + "-" + apiDTOFromProperties.getVersion();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (CryptoException e) {
            String errorMessage =
                    "Error while encrypting the secret key of API : " + apiDTOFromProperties.getProvider() + "-"
                            + apiDTOFromProperties.getName() + "-" + apiDTOFromProperties.getVersion();
            throw new APIManagementException(errorMessage, e,
                    ExceptionCodes.from(ExceptionCodes.ENDPOINT_SECURITY_CRYPTO_EXCEPTION, errorMessage));
        }
        return null;
    }

    /**
     * Validate a provided WSDL definition via a URL or a file/zip
     *
     * @param url WSDL URL
     * @param fileInputStream file/zip input stream
     * @param fileDetail file/zip details
     * @param messageContext messageContext object
     * @return WSDL validation response
     * @throws APIManagementException when error occurred during validation
     */
    @Override
    public Response validateWSDLDefinition(String url, InputStream fileInputStream, Attachment fileDetail,
                                           MessageContext messageContext) throws APIManagementException {
        Map<String, Object> validationResponseMap = validateWSDL(url, fileInputStream, fileDetail, false);

        WSDLValidationResponseDTO validationResponseDTO =
                (WSDLValidationResponseDTO)validationResponseMap.get(RestApiConstants.RETURN_DTO);
        return Response.ok().entity(validationResponseDTO).build();
    }

    /**
     * Validate the provided input parameters and returns the validation response DTO (for REST API)
     *  and the intermediate model as a Map
     *
     * @param url WSDL url
     * @param fileInputStream file data stream
     * @param fileDetail file details
     * @param isServiceAPI is service api condition
     * @return the validation response DTO (for REST API) and the intermediate model as a Map
     * @throws APIManagementException if error occurred during validation of the WSDL
     */
    private Map<String, Object> validateWSDL(String url, InputStream fileInputStream, Attachment fileDetail, Boolean isServiceAPI)
            throws APIManagementException {
        handleInvalidParams(fileInputStream, fileDetail, url, null, isServiceAPI);
        WSDLValidationResponseDTO responseDTO;
        WSDLValidationResponse validationResponse = new WSDLValidationResponse();

        if (url != null) {
            try {
                URL wsdlUrl = new URL(url);
                validationResponse = APIMWSDLReader.validateWSDLUrl(wsdlUrl);
            } catch (MalformedURLException e) {
                RestApiUtil.handleBadRequest("Invalid/Malformed URL : " + url, log);
            }
        } else if (fileInputStream != null && !isServiceAPI) {
            String filename = fileDetail.getContentDisposition().getFilename();
            try {
                if (filename.endsWith(".zip")) {
                    validationResponse = APIMWSDLReader.extractAndValidateWSDLArchive(fileInputStream);
                } else if (filename.endsWith(".wsdl")) {
                    validationResponse = APIMWSDLReader.validateWSDLFile(fileInputStream);
                } else {
                    RestApiUtil.handleBadRequest("Unsupported extension type of file: " + filename, log);
                }
            } catch (APIManagementException e) {
                String errorMessage = "Internal error while validating the WSDL from file:" + filename;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } else if (fileInputStream != null) {
            try {
                validationResponse = APIMWSDLReader.validateWSDLFile(fileInputStream);
            } catch (APIManagementException e) {
                String errorMessage = "Internal error while validating the WSDL definition input stream";
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }

        responseDTO = APIMappingUtil.fromWSDLValidationResponseToDTO(validationResponse);

        Map<String, Object> response = new HashMap<>();
        response.put(RestApiConstants.RETURN_MODEL, validationResponse);
        response.put(RestApiConstants.RETURN_DTO, responseDTO);

        return response;
    }

    /**
     * Import a WSDL file/url or an archive and create an API. The API can be a SOAP or REST depending on the
     * provided implementationType.
     *
     * @param fileInputStream file input stream
     * @param fileDetail file details
     * @param url WSDL url
     * @param additionalProperties API object (json) including additional properties like name, version, context
     * @param implementationType SOAP or SOAPTOREST
     * @return Created API's payload
     * @throws APIManagementException when error occurred during the operation
     */
    @Override
    public Response importWSDLDefinition(InputStream fileInputStream, Attachment fileDetail, String url,
            String additionalProperties, String implementationType, MessageContext messageContext)
            throws APIManagementException {
        try {
            WSDLValidationResponse validationResponse = validateWSDLAndReset(fileInputStream, fileDetail, url);

            if (StringUtils.isEmpty(implementationType)) {
                implementationType = APIDTO.TypeEnum.SOAP.toString();
            }

            boolean isSoapToRestConvertedAPI = APIDTO.TypeEnum.SOAPTOREST.toString().equals(implementationType);
            boolean isSoapAPI = APIDTO.TypeEnum.SOAP.toString().equals(implementationType);

            APIDTO additionalPropertiesAPI = null;
            APIDTO createdApiDTO;
            URI createdApiUri;

            // Minimum requirement name, version, context and endpointConfig.
            additionalPropertiesAPI = new ObjectMapper().readValue(additionalProperties, APIDTO.class);
            APIUtil.validateCharacterLengthOfAPIParams(additionalPropertiesAPI.getName(),
                    additionalPropertiesAPI.getVersion(), additionalPropertiesAPI.getContext(),
                    RestApiCommonUtil.getLoggedInUsername());
            try {
                APIUtil.validateAPIContext(additionalPropertiesAPI.getContext(), additionalPropertiesAPI.getName());
            } catch (APIManagementException e) {
                throw new APIManagementException(e.getMessage(),
                        ExceptionCodes.from(ExceptionCodes.API_CONTEXT_MALFORMED_EXCEPTION, e.getMessage()));
            }
            String username = RestApiCommonUtil.getLoggedInUsername();
            additionalPropertiesAPI.setProvider(username);
            additionalPropertiesAPI.setType(APIDTO.TypeEnum.fromValue(implementationType));
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            API apiToAdd = PublisherCommonUtils
                    .prepareToCreateAPIByDTO(additionalPropertiesAPI, RestApiCommonUtil.getLoggedInUserProvider(),
                            username, organization);
            apiToAdd.setWsdlUrl(url);
            API createdApi = null;
            if (isSoapAPI) {
                createdApi = importSOAPAPI(validationResponse.getWsdlProcessor().getWSDL(), fileDetail, url,
                        apiToAdd, organization, null);
            } else if (isSoapToRestConvertedAPI) {
                String wsdlArchiveExtractedPath = null;
                if (validationResponse.getWsdlArchiveInfo() != null) {
                    wsdlArchiveExtractedPath = validationResponse.getWsdlArchiveInfo().getLocation()
                            + File.separator + APIConstants.API_WSDL_EXTRACTED_DIRECTORY;
                }
                createdApi = importSOAPToRESTAPI(validationResponse.getWsdlProcessor().getWSDL(), fileDetail, url,
                        wsdlArchiveExtractedPath, apiToAdd, organization);
            } else {
                RestApiUtil.handleBadRequest("Invalid implementationType parameter", log);
            }
            createdApiDTO = APIMappingUtil.fromAPItoDTO(createdApi);
            //This URI used to set the location header of the POST response
            createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + createdApiDTO.getId());
            return Response.created(createdApiUri).entity(createdApiDTO).build();
        } catch (IOException | URISyntaxException e) {
            RestApiUtil.handleInternalServerError("Error occurred while importing WSDL", e, log);
        }
        return null;
    }

    /**
     * Validates the provided WSDL and reset the streams as required
     *
     * @param fileInputStream file input stream
     * @param fileDetail file details
     * @param url WSDL url
     * @throws APIManagementException when error occurred during the operation
     */
    private WSDLValidationResponse validateWSDLAndReset(InputStream fileInputStream, Attachment fileDetail, String url)
            throws APIManagementException {
        Map<String, Object> validationResponseMap = validateWSDL(url, fileInputStream, fileDetail, false);
        WSDLValidationResponse validationResponse =
                (WSDLValidationResponse)validationResponseMap.get(RestApiConstants.RETURN_MODEL);

        if (validationResponse.getWsdlInfo() == null) {
            // Validation failure
            RestApiUtil.handleBadRequest(validationResponse.getError(), log);
        }
        return validationResponse;
    }

    /**
     * Import an API from WSDL as a SOAP API
     *
     * @param fileInputStream file data as input stream
     * @param fileDetail file details
     * @param url URL of the WSDL
     * @param apiToAdd API object to be added to the system (which is not added yet)
     * @param organization Organization
     * @param service service
     * @return API added api
     */
    private API importSOAPAPI(InputStream fileInputStream, Attachment fileDetail, String url, API apiToAdd,
                              String organization, ServiceEntry service) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

            //adding the api
            apiProvider.addAPI(apiToAdd);

            if (StringUtils.isNotBlank(url)) {
                apiToAdd.setWsdlUrl(url);
                apiProvider.addWSDLResource(apiToAdd.getUuid(), null, url, organization);
            } else if (fileDetail != null && fileInputStream != null) {
                PublisherCommonUtils
                        .addWsdl(fileDetail.getContentType().toString(), fileInputStream, apiToAdd, apiProvider,
                                organization);
            } else if (service != null && fileInputStream == null) {
                RestApiUtil.handleBadRequest("Error while importing WSDL to create a SOAP API", log);
            } else if (service != null) {
                PublisherCommonUtils.addWsdl(RestApiConstants.APPLICATION_OCTET_STREAM,
                        fileInputStream, apiToAdd, apiProvider, organization);
            }

            //add the generated swagger definition to SOAP
            final String soapOperation = RestApiPublisherUtils.getSOAPOperation();
            String apiDefinition = ApisApiServiceImplUtils.generateSOAPAPIDefinition(apiToAdd, soapOperation);
            apiProvider.saveSwaggerDefinition(apiToAdd, apiDefinition, organization);
            //Retrieve the newly added API to send in the response payload
            return apiProvider.getAPIbyUUID(apiToAdd.getUuid(), organization);
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error while importing WSDL to create a SOAP API", e, log);
        }
        return null;
    }


    /**
     * Import an API from WSDL as a SOAP-to-REST API
     *
     * @param fileInputStream file data as input stream
     * @param fileDetail file details
     * @param url URL of the WSDL
     * @param apiToAdd API object to be added to the system (which is not added yet)
     * @param organization  Organization Identifier
     * @return API added api
     */
    private API importSOAPToRESTAPI(InputStream fileInputStream, Attachment fileDetail, String url,
                                    String wsdlArchiveExtractedPath, API apiToAdd, String organization) throws APIManagementException {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            //adding the api
            API createdApi = apiProvider.addAPI(apiToAdd);
            String filename = null;
            if (fileDetail != null) {
                filename = fileDetail.getContentDisposition().getFilename();
            }

            String swaggerStr = ApisApiServiceImplUtils.getSwaggerString(fileInputStream, url, wsdlArchiveExtractedPath, filename);
            String updatedSwagger = updateSwagger(createdApi.getUUID(), swaggerStr, organization);
            return PublisherCommonUtils
                    .updateAPIBySettingGenerateSequencesFromSwagger(updatedSwagger, createdApi, apiProvider,
                            organization);
        } catch (FaultGatewaysException | IOException e) {
            throw new APIManagementException("Error while importing WSDL to create a SOAP-to-REST API", e);
        }
    }

    /**
     * Retrieve the WSDL of an API
     *
     * @param apiId UUID of the API
     * @param ifNoneMatch If-None-Match header value
     * @return the WSDL of the API (can be a file or zip archive)
     * @throws APIManagementException when error occurred while trying to retrieve the WSDL
     */
    @Override
    public Response getWSDLOfAPI(String apiId, String ifNoneMatch, MessageContext messageContext)
            throws APIManagementException {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            //this will fail if user does not have access to the API or the API does not exist
            //APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, organization);
            ResourceFile resource = apiProvider.getWSDL(apiId, organization);
            return RestApiUtil.getResponseFromResourceFile(resource.getName(), resource);
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need
            // to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil
                        .handleAuthorizationFailure("Authorization failure while retrieving wsdl of API: "
                                        + apiId, e, log);
            } else {
                throw e;
            }
        }
        return null;
    }

    /**
     * Update the WSDL of an API
     *
     * @param apiId UUID of the API
     * @param fileInputStream file data as input stream
     * @param fileDetail file details
     * @param url URL of the WSDL
     * @return 200 OK response if the operation is successful. 400 if the provided inputs are invalid. 500 if a server
     *  error occurred.
     * @throws APIManagementException when error occurred while trying to retrieve the WSDL
     */
    @Override
    public Response updateWSDLOfAPI(String apiId, String ifMatch, InputStream fileInputStream, Attachment fileDetail,
                                    String url, MessageContext messageContext) throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        //validate if api exists
        APIInfo apiInfo = CommonUtils.validateAPIExistence(apiId);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        WSDLValidationResponse validationResponse = validateWSDLAndReset(fileInputStream, fileDetail, url);
        if (StringUtils.isNotBlank(url)) {
            apiProvider.addWSDLResource(apiId, null, url, organization);
        } else {
            String contentType = fileDetail.getContentType().toString();
            ByteArrayInputStream wsdl = validationResponse.getWsdlProcessor().getWSDL();
            ResourceFile wsdlResource = ApisApiServiceImplUtils.getWSDLResource(wsdl, contentType);
            apiProvider.addWSDLResource(apiId, wsdlResource, null, organization);
        }
        return Response.ok().build();
    }

    @Override
    public Response changeAPILifecycle(String action, String apiId, String lifecycleChecklist, String ifMatch,
                                       MessageContext messageContext) throws APIManagementException{

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
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while updating the lifecycle of API " + apiId, e, log);
            } else {
                throw e;
            }
        }
        return null;
    }

    @Override
    public Response createNewAPIVersion(String newVersion, String apiId, Boolean defaultVersion,
                String serviceVersion, MessageContext messageContext) throws APIManagementException {
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
                    throw new APIManagementException("No matching service version found", ExceptionCodes.SERVICE_VERSION_NOT_FOUND);
                }
            }
            if (StringUtils.isNotEmpty(serviceVersion) && !serviceVersion
                    .equals(existingAPI.getServiceInfo("version"))) {
                APIDTO apidto = createAPIDTO(existingAPI, newVersion);
                if (ServiceEntry.DefinitionType.OAS2.equals(service.getDefinitionType()) || ServiceEntry
                        .DefinitionType.OAS3.equals(service.getDefinitionType())) {
                    newVersionedApi = importOpenAPIDefinition(service.getEndpointDef(), null, null, apidto,
                            null, service, organization);
                } else if (ServiceEntry.DefinitionType.ASYNC_API.equals(service.getDefinitionType())) {
                    newVersionedApi = importAsyncAPISpecification(service.getEndpointDef(), null, apidto,
                            null, service, organization);
                }
            } else {
                API versionedAPI = apiProvider.createNewAPIVersion(apiId, newVersion, defaultVersion, organization);
                if (APIConstants.API_TYPE_SOAPTOREST.equals(versionedAPI.getType())) {
                    updateSwagger(versionedAPI.getUuid(), versionedAPI.getSwaggerDefinition(), organization);
                }
                newVersionedApi = APIMappingUtil.fromAPItoDTO(versionedAPI);
            }
            //This URI used to set the location header of the POST response
            newVersionedApiUri =
                    new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + newVersionedApi.getId());
            return Response.created(newVersionedApiUri).entity(newVersionedApi).build();
        } catch (APIManagementException e) {
            if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("Authorization failure while copying API : " + apiId, e, log);
            } else {
                throw e;
            }
        } catch (URISyntaxException | FaultGatewaysException e) {
            String errorMessage = "Error while retrieving API location of " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Exports an API from API Manager for a given API using the ApiId. ID. Meta information, API icon, documentation,
     * WSDL and sequences are exported. This service generates a zipped archive which contains all the above mentioned
     * resources for a given API.
     *
     * @param apiId              UUID of an API
     * @param name               Name of the API that needs to be exported
     * @param version            Version of the API that needs to be exported
     * @param providerName       Provider name of the API that needs to be exported
     * @param format             Format of output documents. Can be YAML or JSON
     * @param preserveStatus     Preserve API status on export
     * @param gatewayEnvironment Gateway environment of the API to be exported
     * @return API export response as an archive
     */
    @Override public Response exportAPI(String apiId, String name, String version, String revisionNum,
                                        String providerName, String format, Boolean preserveStatus,
                                        Boolean exportLatestRevision, String gatewayEnvironment,
                                        MessageContext messageContext)
            throws APIManagementException {

        if (StringUtils.isEmpty(gatewayEnvironment)) {
            //If not specified status is preserved by default
            preserveStatus = preserveStatus == null || preserveStatus;

            // Default export format is YAML
            ExportFormat exportFormat = StringUtils.isNotEmpty(format) ?
                    ExportFormat.valueOf(format.toUpperCase()) :
                    ExportFormat.YAML;
            try {
                String organization = RestApiUtil.getValidatedOrganization(messageContext);
                ImportExportAPI importExportAPI = APIImportExportUtil.getImportExportAPI();
                File file = importExportAPI
                        .exportAPI(apiId, name, version, revisionNum, providerName, preserveStatus, exportFormat,
                                Boolean.TRUE, Boolean.FALSE, exportLatestRevision, StringUtils.EMPTY, organization);
                return Response.ok(file).header(RestApiConstants.HEADER_CONTENT_DISPOSITION,
                        "attachment; filename=\"" + file.getName() + "\"").build();
            } catch (APIImportExportException e) {
                throw new APIManagementException("Error while exporting " + RestApiConstants.RESOURCE_API, e);
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

    @Override
    public Response generateInternalAPIKey(String apiId, MessageContext messageContext) throws APIManagementException {

        String userName = RestApiCommonUtil.getLoggedInUsername();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APIProvider apiProvider = APIManagerFactory.getInstance().getAPIProvider(userName);
        String token = apiProvider.generateApiKey(apiId, organization);
        APIKeyDTO apiKeyDTO = new APIKeyDTO();
        apiKeyDTO.setApikey(token);
        apiKeyDTO.setValidityTime(60 * 1000);
        return Response.ok().entity(apiKeyDTO).build();
    }

    /**
     * Import a GraphQL Schema
     * @param type APIType
     * @param fileInputStream input file
     * @param fileDetail file Detail
     * @param additionalProperties api object as string format
     * @param ifMatch If--Match header value
     * @param messageContext messageContext
     * @return Response with GraphQL API
     */
    @Override
    public Response importGraphQLSchema(String ifMatch, String type, InputStream fileInputStream,
                                Attachment fileDetail, String additionalProperties, MessageContext messageContext) {
        APIDTO additionalPropertiesAPI = null;
        String schema = "";

        try {
            if (fileInputStream == null || StringUtils.isBlank(additionalProperties)) {
                String errorMessage = "GraphQL schema and api details cannot be empty.";
                RestApiUtil.handleBadRequest(errorMessage, log);
            } else {
                schema = IOUtils.toString(fileInputStream, RestApiConstants.CHARSET);
            }

            if (!StringUtils.isBlank(additionalProperties) && !StringUtils.isBlank(schema) && log.isDebugEnabled()) {
                log.debug("Deseriallizing additionalProperties: " + additionalProperties + "/n"
                        + "importing schema: " + schema);
            }

            additionalPropertiesAPI = new ObjectMapper().readValue(additionalProperties, APIDTO.class);
            APIUtil.validateCharacterLengthOfAPIParams(additionalPropertiesAPI.getName(),
                    additionalPropertiesAPI.getVersion(), additionalPropertiesAPI.getContext(),
                    RestApiCommonUtil.getLoggedInUsername());
            APIUtil.validateAPIContext(additionalPropertiesAPI.getContext(), additionalPropertiesAPI.getName());
            additionalPropertiesAPI.setType(APIDTO.TypeEnum.GRAPHQL);
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            API apiToAdd = PublisherCommonUtils.prepareToCreateAPIByDTO(additionalPropertiesAPI, apiProvider,
                    RestApiCommonUtil.getLoggedInUsername(), organization);


            //Save swagger definition of graphQL
            String apiDefinition = ApisApiServiceImplUtils.getApiDefinition(apiToAdd);
            apiToAdd.setSwaggerDefinition(apiDefinition);

            //adding the api
            API createdApi = apiProvider.addAPI(apiToAdd);

            apiProvider.saveGraphqlSchemaDefinition(createdApi.getUuid(), schema, organization);

            APIDTO createdApiDTO = APIMappingUtil.fromAPItoDTO(createdApi);

            //This URI used to set the location header of the POST response
            URI createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + createdApiDTO.getId());
            return Response.created(createdApiUri).entity(createdApiDTO).build();
        } catch (APIManagementException e) {
            if (e.getMessage().contains(ExceptionCodes.API_CONTEXT_MALFORMED_EXCEPTION.getErrorMessage())) {
                RestApiUtil.handleBadRequest(e.getMessage(), e, log);
            }
            String errorMessage = "Error while adding new API : " + additionalPropertiesAPI.getProvider() + "-" +
                additionalPropertiesAPI.getName() + "-" + additionalPropertiesAPI.getVersion() + " - " + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving API location : " + additionalPropertiesAPI.getProvider() + "-"
                    + additionalPropertiesAPI.getName() + "-" + additionalPropertiesAPI.getVersion();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
    } catch (IOException e) {
            String errorMessage = "Error while retrieving content from file : " + additionalPropertiesAPI.getProvider()
                    + "-" + additionalPropertiesAPI.getName() + "-" + additionalPropertiesAPI.getVersion();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
    }
        return null;
    }

    /**
     * Import an API by uploading an archive file. All relevant API data will be included upon the creation of
     * the API. Depending on the choice of the user, provider of the imported API will be preserved or modified.
     *
     * @param fileInputStream  Input stream from the REST request
     * @param fileDetail       File details as Attachment
     * @param preserveProvider User choice to keep or replace the API provider
     * @param overwrite        Whether to update the API or not. This is used when updating already existing APIs.
     * @return API import response
     * @throws APIManagementException when error occurred while trying to import the API
     */
    @Override public Response importAPI(InputStream fileInputStream, Attachment fileDetail,
                                        Boolean preserveProvider, Boolean rotateRevision, Boolean overwrite,
                                        Boolean preservePortalConfigurations,String accept,
                                        MessageContext messageContext) throws APIManagementException {
        // Check whether to update. If not specified, default value is false.
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
        ImportedAPIDTO importedAPIDTO = importExportAPI.importAPI(fileInputStream, preserveProvider, rotateRevision, overwrite,
                preservePortalConfigurations, tokenScopes, organization);
        if (RestApiConstants.APPLICATION_JSON.equals(accept) && importedAPIDTO != null) {
            ImportAPIResponseDTO responseDTO = new ImportAPIResponseDTO().id(importedAPIDTO.getApi().getUuid())
                    .revision(importedAPIDTO.getRevision());
            return Response.ok().entity(responseDTO).build();
        }
        return Response.status(Response.Status.OK).entity("API imported successfully.").build();
    }

    /**
     * Validate graphQL Schema
     * @param fileInputStream  input file
     * @param fileDetail file Detail
     * @param messageContext messageContext
     * @return Validation response
     */
    @Override
    public Response validateGraphQLSchema(InputStream fileInputStream, Attachment fileDetail,
                                          MessageContext messageContext) {

        GraphQLValidationResponseDTO validationResponse = new GraphQLValidationResponseDTO();
        String filename = fileDetail.getContentDisposition().getFilename();

        try {
            String schema = IOUtils.toString(fileInputStream, RestApiConstants.CHARSET);
            validationResponse = PublisherCommonUtils.validateGraphQLSchema(filename, schema);
        } catch (IOException | APIManagementException e) {
            validationResponse.setIsValid(false);
            validationResponse.setErrorMessage(e.getMessage());
        }
        return Response.ok().entity(validationResponse).build();
    }

    /**
     * Generates Mock response examples for Inline prototyping
     * of a swagger
     *
     * @param apiId API Id
     * @param ifNoneMatch If-None-Match header value
     * @param messageContext message context
     * @return apiDefinition
     * @throws APIManagementException
     */
    @Override
    public Response generateMockScripts(String apiId, String ifNoneMatch, MessageContext messageContext) throws APIManagementException {
        APIIdentifier apiIdentifierFromTable = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
        if (apiIdentifierFromTable == null) {
            throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API with API UUID: "
                    + apiId, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND,
                    apiId));
        }
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        API originalAPI = apiProvider.getAPIbyUUID(apiId, organization);

        String apiDefinition = apiProvider.getOpenAPIDefinition(apiId, organization);
        apiDefinition = String.valueOf(OASParserUtil.generateExamples(apiDefinition).get(APIConstants.SWAGGER));
        apiProvider.saveSwaggerDefinition(originalAPI, apiDefinition, organization);
        return Response.ok().entity(apiDefinition).build();
    }

    @Override
    public Response getAPISubscriptionPolicies(String apiId, String xWSO2Tenant, String ifNoneMatch, Boolean isAiApi,
            MessageContext messageContext) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APIDTO apiInfo = getAPIByID(apiId, apiProvider, organization);
        List<Tier> availableThrottlingPolicyList = new ThrottlingPoliciesApiServiceImpl().getThrottlingPolicyList(
                ThrottlingPolicyDTO.PolicyLevelEnum.SUBSCRIPTION.toString(), true, isAiApi);

        if (apiInfo != null) {
            List<String> apiPolicies = apiInfo.getPolicies();
            List<Tier> apiThrottlingPolicies = ApisApiServiceImplUtils.filterAPIThrottlingPolicies(apiPolicies,
                    availableThrottlingPolicyList);
            return Response.ok().entity(apiThrottlingPolicies).build();
        }
        return null;
    }

    private APIDTO getAPIByID(String apiId, APIProvider apiProvider, String organization) {
        try {
            API api = apiProvider.getAPIbyUUID(apiId, organization);
            api.setOrganization(organization);
            return APIMappingUtil.fromAPItoDTO(api, apiProvider);
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need
            // to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure("User is not authorized to access the API", e, log);
            } else {
                String errorMessage = "Error while retrieving API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
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

    /**
     * Validate the provided OpenAPI definition (via file or url) and return a Map with the validation response
     * information.
     *
     * @param url OpenAPI definition url
     * @param fileInputStream file as input stream
     * @param apiDefinition Swagger API definition String
     * @param returnContent whether to return the content of the definition in the response DTO
     * @return Map with the validation response information. A value with key 'dto' will have the response DTO
     *  of type OpenAPIDefinitionValidationResponseDTO for the REST API. A value with key 'model' will have the
     *  validation response of type APIDefinitionValidationResponse coming from the impl level.
     */
    private Map validateOpenAPIDefinition(String url, InputStream fileInputStream, Attachment fileDetail,
            String apiDefinition, Boolean returnContent, Boolean isServiceAPI) throws APIManagementException {
        //validate inputs
        handleInvalidParams(fileInputStream, fileDetail, url, apiDefinition, isServiceAPI);
        String fileName = null;

        OpenAPIDefinitionValidationResponseDTO responseDTO;
        APIDefinitionValidationResponse validationResponse = new APIDefinitionValidationResponse();
        if (fileDetail != null) {
            fileName = fileDetail.getContentDisposition().getFilename();
        }
        validationResponse = ApisApiServiceImplUtils.validateOpenAPIDefinition(url, fileInputStream, apiDefinition,fileName, returnContent);
        responseDTO = APIMappingUtil.getOpenAPIDefinitionValidationResponseFromModel(validationResponse,
                returnContent);

        Map response = new HashMap();
        response.put(RestApiConstants.RETURN_MODEL, validationResponse);
        response.put(RestApiConstants.RETURN_DTO, responseDTO);
        return response;
    }

    /**
     * Validate API import definition/validate definition parameters
     *
     * @param fileInputStream file content stream
     * @param url             URL of the definition
     * @param apiDefinition   Swagger API definition String
     */
    private void handleInvalidParams(InputStream fileInputStream, Attachment fileDetail, String url,
                                     String apiDefinition, Boolean isServiceAPI) {

        String msg = "";
        boolean isFileSpecified = (fileInputStream != null && fileDetail != null &&
                fileDetail.getContentDisposition() != null && fileDetail.getContentDisposition().getFilename() != null)
                || (fileInputStream != null && isServiceAPI);
        if (url == null && !isFileSpecified && apiDefinition == null) {
            msg = "One out of 'file' or 'url' or 'inline definition' should be specified";
        }

        boolean isMultipleSpecificationGiven = (isFileSpecified && url != null) || (isFileSpecified &&
                apiDefinition != null) || (apiDefinition != null && url != null);
        if (isMultipleSpecificationGiven) {
            msg = "Only one of 'file', 'url', and 'inline definition' should be specified";
        }

        if (StringUtils.isNotBlank(msg)) {
            RestApiUtil.handleBadRequest(msg, log);
        }
    }

    /**
     * To check whether a particular exception is due to access control restriction.
     *
     * @param e Exception object.
     * @return true if the the exception is caused due to authorization failure.
     */
    private boolean isAuthorizationFailure(Exception e) {
        String errorMessage = e.getMessage();
        return errorMessage != null && errorMessage.contains(APIConstants.UN_AUTHORIZED_ERROR_MESSAGE);
    }

    /**
     * Retrieve available revisions of an API
     *
     * @param apiId UUID of the API
     * @param query Search query string
     * @param messageContext    message context object
     * @return response containing list of API revisions
     */
    @Override
    public Response getAPIRevisions(String apiId, String query, MessageContext messageContext) {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            APIRevisionListDTO apiRevisionListDTO;
            List<APIRevision> apiRevisions = apiProvider.getAPIRevisions(apiId);
            List<APIRevision> apiRevisionsList = ApisApiServiceImplUtils.filterAPIRevisionsByDeploymentStatus(query, apiRevisions);
            apiRevisionListDTO = APIMappingUtil.fromListAPIRevisiontoDTO(apiRevisionsList);
            return Response.ok().entity(apiRevisionListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while adding retrieving API Revision for api id : " + apiId + " - " + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Create a new API revision
     *
     * @param apiId             UUID of the API
     * @param apIRevisionDTO    API object that needs to be added
     * @param messageContext    message context object
     * @return response containing newly created APIRevision object
     */
    @Override
    public Response createAPIRevision(String apiId, APIRevisionDTO apIRevisionDTO, MessageContext messageContext)
            throws APIManagementException {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);

            //validate if api exists
            APIInfo apiInfo = CommonUtils.validateAPIExistence(apiId);

            //validate whether the API is advertise only
            APIDTO apiDto = getAPIByID(apiId, apiProvider, organization);
            if (apiDto != null && apiDto.getAdvertiseInfo() != null && apiDto.getAdvertiseInfo().isAdvertised()) {
                throw new APIManagementException(
                        "Creating API Revisions is not supported for third party APIs: " + apiId,
                        ExceptionCodes.from(ExceptionCodes.THIRD_PARTY_API_REVISION_CREATION_UNSUPPORTED, apiId));
            }

            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(apiInfo.getStatus().toString());

            APIRevision apiRevision = new APIRevision();
            apiRevision.setApiUUID(apiId);
            apiRevision.setDescription(apIRevisionDTO.getDescription());
            //adding the api revision
            String revisionId = apiProvider.addAPIRevision(apiRevision, organization);

            //Retrieve the newly added APIRevision to send in the response payload
            APIRevision createdApiRevision = apiProvider.getAPIRevision(revisionId);
            APIRevisionDTO createdApiRevisionDTO = APIMappingUtil.fromAPIRevisiontoDTO(createdApiRevision);
            //This URI used to set the location header of the POST response
            URI createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS
                    + "/" + createdApiRevisionDTO.getApiInfo().getId() + "/"
                    + RestApiConstants.RESOURCE_PATH_REVISIONS + "/" + createdApiRevisionDTO.getId());
            return Response.created(createdApiUri).entity(createdApiRevisionDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while adding new API Revision for API : " + apiId;
            if ((e.getErrorHandler()
                    .getErrorCode() == ExceptionCodes.THIRD_PARTY_API_REVISION_CREATION_UNSUPPORTED.getErrorCode())
                    || (e.getErrorHandler().getErrorCode() == ExceptionCodes.MAXIMUM_REVISIONS_REACHED.getErrorCode()))
            {
                throw e;
            } else {
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving created revision API location for API : "
                    + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Retrieve a revision of an API
     *
     * @param apiId             UUID of the API
     * @param revisionId     Revision ID of the API
     * @param messageContext    message context object
     * @return response containing APIRevision object
     */
    @Override
    public Response getAPIRevision(String apiId, String revisionId, MessageContext messageContext) {
        // remove errorObject and add implementation code!
        ErrorDTO errorObject = new ErrorDTO();
        Response.Status status = Response.Status.NOT_IMPLEMENTED;
        errorObject.setCode((long) status.getStatusCode());
        errorObject.setMessage(status.toString());
        errorObject.setDescription("The requested resource has not been implemented");
        return Response.status(status).entity(errorObject).build();
    }

    /**
     * Delete a revision of an API
     *
     * @param apiId             UUID of the API
     * @param revisionId     Revision ID of the API
     * @param messageContext    message context object
     * @return response with 204 status code and no content
     */
    @Override
    public Response deleteAPIRevision(String apiId, String revisionId, MessageContext messageContext)
            throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        //validate if api exists
        APIInfo apiInfo = CommonUtils.validateAPIExistence(apiId);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        apiProvider.deleteAPIRevision(apiId, revisionId, organization);
        List<APIRevision> apiRevisions = apiProvider.getAPIRevisions(apiId);
        APIRevisionListDTO apiRevisionListDTO = APIMappingUtil.fromListAPIRevisiontoDTO(apiRevisions);
        return Response.ok().entity(apiRevisionListDTO).build();
    }

    /**
     * Deploy a revision
     *
     * @param apiId             UUID of the API
     * @param revisionId     Revision ID of the API
     * @param messageContext    message context object
     * @return response with 200 status code
     */
    @Override
    public Response deployAPIRevision(String apiId, String revisionId,
                                      List<APIRevisionDeploymentDTO> apIRevisionDeploymentDTOList,
                                      MessageContext messageContext) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        if (revisionId.isEmpty()) {
            return Response.status(Response.Status.BAD_REQUEST).entity("Revision Id is not provided").build();
        }

        //validate if api exists
        APIInfo apiInfo = CommonUtils.validateAPIExistence(apiId);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        //validate whether the API is advertise only
        APIDTO apiDto = getAPIByID(apiId, apiProvider, organization);

        // Cannot deploy an API with custom sequence to the APK gateway
        Map endpointConfigMap = (Map) apiDto.getEndpointConfig();
        if (endpointConfigMap != null && !APIConstants.WSO2_SYNAPSE_GATEWAY.equals(apiDto.getGatewayType())
                && APIConstants.ENDPOINT_TYPE_SEQUENCE.equals(
                endpointConfigMap.get(APIConstants.API_ENDPOINT_CONFIG_PROTOCOL_TYPE))) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity("Cannot Deploy an API with a Custom Sequence to APK Gateway: " + apiId).build();
        }
        // Reject the request if API lifecycle is 'RETIRED'.
        if (apiDto.getLifeCycleStatus().equals(APIConstants.RETIRED)) {
            String errorMessage = "Deploying API Revisions is not supported for retired APIs. ApiId: " + apiId;
            throw new APIManagementException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.RETIRED_API_REVISION_DEPLOYMENT_UNSUPPORTED, apiId));
        }
        if (apiDto != null && apiDto.getAdvertiseInfo() != null && Boolean.TRUE.equals(
                apiDto.getAdvertiseInfo().isAdvertised())) {
            String errorMessage = "Deploying API Revisions is not supported for third party APIs: " + apiId;
            throw new APIManagementException(errorMessage,
                    ExceptionCodes.from(ExceptionCodes.THIRD_PARTY_API_REVISION_DEPLOYMENT_UNSUPPORTED, apiId));
        }

        Map<String, Environment> environments = APIUtil.getEnvironments(organization);
        List<APIRevisionDeployment> apiRevisionDeployments = new ArrayList<>();
        for (APIRevisionDeploymentDTO apiRevisionDeploymentDTO : apIRevisionDeploymentDTOList) {
            String environment = apiRevisionDeploymentDTO.getName();
            Boolean displayOnDevportal = apiRevisionDeploymentDTO.isDisplayOnDevportal();
            String vhost = apiRevisionDeploymentDTO.getVhost();
            APIRevisionDeployment apiRevisionDeployment = ApisApiServiceImplUtils.mapAPIRevisionDeploymentWithValidation(revisionId,
                    environments, environment, displayOnDevportal, vhost, true);
            apiRevisionDeployments.add(apiRevisionDeployment);
        }
        apiProvider.deployAPIRevision(apiId, revisionId, apiRevisionDeployments, organization);
        List<APIRevisionDeployment> apiRevisionDeploymentsResponse = apiProvider.getAPIRevisionsDeploymentList(apiId);
        List<APIRevisionDeploymentDTO> apiRevisionDeploymentDTOS = new ArrayList<>();
        for (APIRevisionDeployment apiRevisionDeployment : apiRevisionDeploymentsResponse) {
            apiRevisionDeploymentDTOS.add(APIMappingUtil.fromAPIRevisionDeploymenttoDTO(apiRevisionDeployment));
        }
        Response.Status status = Response.Status.CREATED;
        return Response.status(status).entity(apiRevisionDeploymentDTOS).build();
    }

    /**
     * Get revision deployment list
     *
     * @param apiId             UUID of the API
     * @param messageContext    message context object
     * @return response with 200 status code
     */
    @Override
    public Response getAPIRevisionDeployments(String apiId, MessageContext messageContext) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        List<APIRevisionDeployment> apiRevisionDeploymentsList = apiProvider.getAPIRevisionsDeploymentList(apiId);

        List<APIRevisionDeploymentDTO> apiRevisionDeploymentDTOS = new ArrayList<>();
        for (APIRevisionDeployment apiRevisionDeployment : apiRevisionDeploymentsList) {
            apiRevisionDeploymentDTOS.add(APIMappingUtil.fromAPIRevisionDeploymenttoDTO(apiRevisionDeployment));
        }
        return Response.ok().entity(apiRevisionDeploymentDTOS).build();
    }

    @Override
    public Response undeployAPIRevision(String apiId, String revisionId, String revisionNum, Boolean allEnvironments,
                                        List<APIRevisionDeploymentDTO> apIRevisionDeploymentDTOList,
                                        MessageContext messageContext) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        //validate if api exists
        APIInfo apiInfo = CommonUtils.validateAPIExistence(apiId);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        String organization = RestApiUtil.getValidatedOrganization(messageContext);

        if (revisionId == null && revisionNum != null) {
            revisionId = apiProvider.getAPIRevisionUUID(revisionNum, apiId);
            if (revisionId == null) {
                throw new APIManagementException(
                        "No revision found for revision number " + revisionNum + " of API with UUID " + apiId,
                        ExceptionCodes.from(ExceptionCodes.REVISION_NOT_FOUND_FOR_REVISION_NUMBER, revisionNum));
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
                APIRevisionDeployment apiRevisionDeployment = ApisApiServiceImplUtils.mapAPIRevisionDeploymentWithValidation(revisionId,
                        environments, environment, displayOnDevportal, vhost, false);
                apiRevisionDeployments.add(apiRevisionDeployment);
            }
        }
        apiProvider.undeployAPIRevisionDeployment(apiId, revisionId, apiRevisionDeployments, organization);
        List<APIRevisionDeployment> apiRevisionDeploymentsResponse = apiProvider.getAPIRevisionDeploymentList(revisionId);
        List<APIRevisionDeploymentDTO> apiRevisionDeploymentDTOS = new ArrayList<>();
        for (APIRevisionDeployment apiRevisionDeployment : apiRevisionDeploymentsResponse) {
            apiRevisionDeploymentDTOS.add(APIMappingUtil.fromAPIRevisionDeploymenttoDTO(apiRevisionDeployment));
        }
        Response.Status status = Response.Status.CREATED;
        return Response.status(status).entity(apiRevisionDeploymentDTOS).build();
    }

    /**
     * Restore a revision to the working copy of the API
     *
     * @param apiId          UUID of the API
     * @param revisionId  Revision ID of the API
     * @param messageContext message context object
     * @return response with 200 status code
     */
    @Override
    public Response restoreAPIRevision(String apiId, String revisionId, MessageContext messageContext)
            throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        //validate if api exists
        APIInfo apiInfo = CommonUtils.validateAPIExistence(apiId);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        apiProvider.restoreAPIRevision(apiId, revisionId, organization);
        APIDTO apiToReturn = getAPIByID(apiId, apiProvider, organization);
        Response.Status status = Response.Status.CREATED;
        return Response.status(status).entity(apiToReturn).build();
    }

    /**
     * Delete a pending task for API revision deployment
     *
     * @param apiId          Id of the API
     * @param revisionId     Id of the revision
     * @param envName        Name of the gateway
     * @param messageContext CXF message context
     * @return 200 response if deleted successfully
     */
    @Override public Response deleteAPIRevisionDeploymentPendingTask(String apiId, String revisionId, String envName,
            MessageContext messageContext) {
        try {
            String environment = "environment";
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            APIIdentifier apiIdentifierFromTable = APIMappingUtil.getAPIIdentifierFromUUID(apiId);
            if (apiIdentifierFromTable == null) {
                throw new APIMgtResourceNotFoundException("Couldn't retrieve existing API with API UUID: " + apiId,
                        ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND, apiId));
            }
            ApiMgtDAO apiMgtDAO = ApiMgtDAO.getInstance();

            List<WorkflowDTO> workflowDTOList = apiMgtDAO.retrieveAllWorkflowFromInternalReference(revisionId,
                    WorkflowConstants.WF_TYPE_AM_REVISION_DEPLOYMENT);
            String externalRef = null;
            for (WorkflowDTO workflowDTO : workflowDTOList) {
                if (envName.equals(workflowDTO.getMetadata(environment))) {
                    externalRef = workflowDTO.getExternalWorkflowReference();
                }
            }

            if (externalRef == null) {
                throw new APIMgtResourceNotFoundException(
                        "Couldn't retrieve existing API Revision with Revision Id: " + revisionId,
                        ExceptionCodes.from(ExceptionCodes.API_REVISION_NOT_FOUND, revisionId));
            }
            apiProvider.cleanupAPIRevisionDeploymentWorkflows(apiId, externalRef);
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while deleting task ";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response validateAsyncAPISpecification(Boolean returnContent, String url, InputStream fileInputStream, Attachment fileDetail, MessageContext messageContext) throws APIManagementException {
        //validate and retrieve the AsyncAPI specification
        Map<String, Object> validationResponseMap = null;
        try {
            validationResponseMap = validateAsyncAPISpecification(url, fileInputStream, fileDetail, returnContent,
                    false);
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error occurred while validating API Definition", e, log);
        }

        AsyncAPISpecificationValidationResponseDTO validationResponseDTO =
                (AsyncAPISpecificationValidationResponseDTO)validationResponseMap.get(RestApiConstants.RETURN_DTO);
        return Response.ok().entity(validationResponseDTO).build();
    }

    /**
     * Validate the provided AsyncAPI specification (via file or url) and return a Map with the validation response
     * information
     *
     * @param url AsyncAPI specification url
     * @param fileInputStream file as input stream
     * @param returnContent whether to return the content of the definition in the response DTO
     * @param isServiceAPI whether the request is to create API from a service in Service Catalog
     * @return Map with the validation response information. A value with key 'dto' will have the response DTO
     *  of type AsyncAPISpecificationValidationResponseDTO for the REST API. A value with the key 'model' will have the
     *  validation response of type APIDefinitionValidationResponse coming from the impl level
     */
    private Map<String, Object> validateAsyncAPISpecification(String url, InputStream fileInputStream, Attachment fileDetail,
                                                              Boolean returnContent, Boolean isServiceAPI) throws APIManagementException {
        //validate inputs
        handleInvalidParams(fileInputStream, fileDetail, url, null, isServiceAPI);

        AsyncAPISpecificationValidationResponseDTO responseDTO;
        APIDefinitionValidationResponse validationResponse = new APIDefinitionValidationResponse();

        if (url != null) {
            //validate URL
            validationResponse = AsyncApiParserUtil.validateAsyncAPISpecificationByURL(url, returnContent);
        } else if (fileInputStream != null) {
            //validate file
            String fileName = fileDetail != null ? fileDetail.getContentDisposition().getFilename() : StringUtils.EMPTY;
            String schemaToBeValidated = ApisApiServiceImplUtils.getSchemaToBeValidated(fileInputStream, isServiceAPI, fileName);
            validationResponse = AsyncApiParserUtil.validateAsyncAPISpecification(schemaToBeValidated, returnContent);
        }

        responseDTO = APIMappingUtil.getAsyncAPISpecificationValidationResponseFromModel(validationResponse, returnContent);

        Map<String, Object> response = new HashMap<>();
        response.put(RestApiConstants.RETURN_MODEL, validationResponse);
        response.put(RestApiConstants.RETURN_DTO, responseDTO);
        return response;
    }

    /**
     * Importing and AsyncAPI Specification and create and API
     *
     * @param fileInputStream InputStream for the provided file
     * @param fileDetail File meta-data
     * @param url URL of the AsyncAPI Specification
     * @param additionalProperties API object (json) including additional properties like name, version, context
     * @param messageContext CXF message context
     * @return API import using AsyncAPI specification response
     */
    @Override
    public Response importAsyncAPISpecification(InputStream fileInputStream, Attachment fileDetail, String url, String additionalProperties, MessageContext messageContext) throws APIManagementException {
        // validate 'additionalProperties' json
        if (StringUtils.isBlank(additionalProperties)) {
            RestApiUtil.handleBadRequest("'additionalProperties' is required and should not be null", log);
        }

        // Convert the 'additionalProperties' json into an APIDTO object
        ObjectMapper objectMapper = new ObjectMapper();
        APIDTO apiDTOFromProperties;
        try {
            apiDTOFromProperties = objectMapper.readValue(additionalProperties, APIDTO.class);
            if (apiDTOFromProperties.getType() == null) {
                RestApiUtil.handleBadRequest("Required property protocol is not specified for the Async API", log);
            }
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
            throw RestApiUtil.buildBadRequestException("Error while parsing 'additionalProperties'", e);
        }

        // validate whether ASYNC APIs created without advertise only enabled
        if (APIDTO.TypeEnum.ASYNC.equals(apiDTOFromProperties.getType()) &&
                (apiDTOFromProperties.getAdvertiseInfo() == null ||
                        !apiDTOFromProperties.getAdvertiseInfo().isAdvertised())) {
            RestApiUtil.handleBadRequest("ASYNC type APIs only can be created as third party APIs", log);
        }

        //validate websocket url and change transport types
        if (PublisherCommonUtils.isValidWSAPI(apiDTOFromProperties)){
            ArrayList<String> websocketTransports = new ArrayList<>();
            websocketTransports.add(APIConstants.WS_PROTOCOL);
            websocketTransports.add(APIConstants.WSS_PROTOCOL);
            apiDTOFromProperties.setTransport(websocketTransports);
        }

        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIDTO createdAPIDTO = importAsyncAPISpecification(fileInputStream, url, apiDTOFromProperties, fileDetail,
                    null, organization);
            URI createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + createdAPIDTO.getId());
            return Response.created(createdApiUri).entity(createdAPIDTO).build();
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving API location : " + apiDTOFromProperties.getProvider() + "-" +
                    apiDTOFromProperties.getName() + "-" + apiDTOFromProperties.getVersion();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response apisApiIdAsyncapiGet(String apiId, String ifNoneMatch, MessageContext messageContext) throws APIManagementException {
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);

            API api = apiProvider.getAPIbyUUID(apiId, organization);
            api.setOrganization(organization);
            String updatedDefinition = RestApiCommonUtil.retrieveAsyncAPIDefinition(api, apiProvider);
            return Response.ok().entity(updatedDefinition).header("Content-Disposition",
                    "attachment; fileNme=\"" + "asyncapi.json" + "\"").build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant acessing APIs. Sends 404, since we don't need to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.
                        handleAuthorizationFailure("Authorization failre while retrieving AsyncAPI of API : " + apiId,
                                e, log);
            } else {
                String errorMessage = "Error while retrieving AsyncAPI for API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    @Override
    public Response apisApiIdAsyncapiPut(String apiId, String ifMatch, String apiDefinition, String url,
            InputStream fileInputStream, Attachment fileDetail, MessageContext messageContext)
            throws APIManagementException {
        try {
            String updatedAsyncAPIDefinition;
            String organization = RestApiUtil.getValidatedOrganization(messageContext);

            //validate if api exists
            APIInfo apiInfo = CommonUtils.validateAPIExistence(apiId);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(apiInfo.getStatus().toString());

            //Handle URL and file based definition imports
            if (url != null || fileInputStream != null) {
                //Validate and retrieve the AsyncAPI definition
                Map<String, Object> validationResponseMap = validateAsyncAPISpecification(url, fileInputStream,
                        fileDetail, true, false);
                APIDefinitionValidationResponse validationResponse =
                        (APIDefinitionValidationResponse) validationResponseMap.get(RestApiConstants.RETURN_MODEL);
                if (!validationResponse.isValid()) {
                    RestApiUtil.handleBadRequest(validationResponse.getErrorItems(), log);
                }
                updatedAsyncAPIDefinition = PublisherCommonUtils.updateAsyncAPIDefinition(apiId, validationResponse,
                        organization);
            } else {
                updatedAsyncAPIDefinition = updateAsyncAPIDefinition(apiId, apiDefinition, organization);
            }
            return Response.ok().entity(updatedAsyncAPIDefinition).build();
        } catch (APIManagementException e) {
            //Auth failure occurs when cross tenant accessing APIs. Sends 404, since we don't need
            // to expose the existence of the resource
            if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (isAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        "Authorization failure while updating AsyncAPI definition of API: " + apiId, e, log);
            } else {
                String errorMessage = "Error while updating the AsyncAPI definition of the API: " + apiId + " - "
                        + e.getMessage();
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (FaultGatewaysException e) {
            String errorMessage = "Error while updating API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * update AsyncAPI definition of the given API. The AsyncAPI will be validated before updating.
     *
     * @param apiId API Id
     * @param apiDefinition AsyncAPI definition
     * @param organization organization of the API
     * @return updated AsyncAPI definition
     * @throws APIManagementException when error occurred updating AsyncAPI
     * @throws FaultGatewaysException when error occurred publishing API to the gateway
     */
    private String updateAsyncAPIDefinition(String apiId, String apiDefinition, String organization)
            throws APIManagementException, FaultGatewaysException {
        APIDefinitionValidationResponse response = AsyncApiParserUtil
                .validateAsyncAPISpecification(apiDefinition, true);
        if (!response.isValid()) {
            RestApiUtil.handleBadRequest(response.getErrorItems(), log);
        }
        return PublisherCommonUtils.updateAsyncAPIDefinition(apiId, response, organization);
    }

    @Override
    public Response importServiceFromCatalog(String serviceKey, APIDTO apiDto, MessageContext messageContext) {
        if (StringUtils.isEmpty(serviceKey)) {
            RestApiUtil.handleBadRequest("Required parameter serviceKey is missing", log);
        }
        try {
            String username = RestApiCommonUtil.getLoggedInUsername();
            int tenantId = APIUtil.getTenantId(username);
            ServiceCatalogImpl serviceCatalog = new ServiceCatalogImpl();
            ServiceEntry service = serviceCatalog.getServiceByKey(serviceKey, tenantId);
            if (service == null) {
                RestApiUtil.handleResourceNotFoundError("Service", serviceKey, log);
            }
            APIUtil.validateAPIContext(apiDto.getContext(), apiDto.getName());
            APIDTO createdApiDTO = null;
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            if (ServiceEntry.DefinitionType.OAS2.equals(service.getDefinitionType()) ||
                    ServiceEntry.DefinitionType.OAS3.equals(service.getDefinitionType())) {
                createdApiDTO = importOpenAPIDefinition(service.getEndpointDef(), null, null, apiDto, null, service,
                        organization);
            } else if (ServiceEntry.DefinitionType.ASYNC_API.equals(service.getDefinitionType())) {
                createdApiDTO = importAsyncAPISpecification(service.getEndpointDef(), null, apiDto, null, service,
                        organization);
            } else if (ServiceEntry.DefinitionType.WSDL1.equals(service.getDefinitionType())) {
                apiDto.setProvider(RestApiCommonUtil.getLoggedInUsername());
                apiDto.setType(APIDTO.TypeEnum.fromValue("SOAP"));
                API apiToAdd = PublisherCommonUtils.prepareToCreateAPIByDTO(apiDto,
                        RestApiCommonUtil.getLoggedInUserProvider(), username, organization);
                apiToAdd.setServiceInfo("key", service.getServiceKey());
                apiToAdd.setServiceInfo("md5", service.getMd5());
                apiToAdd.setEndpointConfig(PublisherCommonUtils.constructEndpointConfigForService(service
                        .getServiceUrl(), null));
                API api = importSOAPAPI(service.getEndpointDef(), null, null,
                        apiToAdd, organization, service);
                createdApiDTO = APIMappingUtil.fromAPItoDTO(api);
            }
            if (createdApiDTO != null) {
                URI createdApiUri = new URI(RestApiConstants.RESOURCE_PATH_APIS + "/" + createdApiDTO.getId());
                return Response.created(createdApiUri).entity(createdApiDTO).build();
            } else {
                RestApiUtil.handleBadRequest("Unsupported definition type provided. Cannot create API " +
                        "using the service type " + service.getDefinitionType().name(), log);
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError("Service", serviceKey, e, log);
            } else if (e.getMessage().contains(ExceptionCodes.API_CONTEXT_MALFORMED_EXCEPTION.getErrorMessage())) {
                RestApiUtil.handleBadRequest(e.getMessage(), e, log);
            } else if (e.getMessage().contains("duplicate API context")) {
                RestApiUtil.handleBadRequest(e.getMessage(), e, log);
            } else {
                String errorMessage = "Error while creating API using Service with Id : " + serviceKey
                        + " from Service Catalog";
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving API location : " + apiDto.getName() + "-"
                    + apiDto.getVersion();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response reimportServiceFromCatalog(String apiId, MessageContext messageContext)
            throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String username = RestApiCommonUtil.getLoggedInUsername();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        int tenantId = APIUtil.getTenantId(username);
        try {

            //validate if api exists
            APIInfo apiInfo = CommonUtils.validateAPIExistence(apiId);
            //validate API update operation permitted based on the LC state
            validateAPIOperationsPerLC(apiInfo.getStatus().toString());

            API api = apiProvider.getLightweightAPIByUUID(apiId, organization);
            API originalAPI = apiProvider.getAPIbyUUID(apiId, organization);
            String serviceKey = apiProvider.retrieveServiceKeyByApiId(originalAPI.getId().getId(), tenantId);
            ServiceCatalogImpl serviceCatalog = new ServiceCatalogImpl();
            ServiceEntry service = serviceCatalog.getServiceByKey(serviceKey, tenantId);
            JSONObject serviceInfo = ApisApiServiceImplUtils.getServiceInfo(service);
            api.setServiceInfo(serviceInfo);
            api.setUriTemplates(originalAPI.getUriTemplates());
            Map validationResponseMap = new HashMap();
            if (ServiceEntry.DefinitionType.OAS2.equals(service.getDefinitionType()) ||
                    ServiceEntry.DefinitionType.OAS3.equals(service.getDefinitionType())) {
                validationResponseMap = validateOpenAPIDefinition(null, service.getEndpointDef(), null, null,
                        true, true);
            } else if (ServiceEntry.DefinitionType.ASYNC_API.equals(service.getDefinitionType())) {
                validationResponseMap = validateAsyncAPISpecification(null, service.getEndpointDef(),
                        null, true, true);
            } else if (!ServiceEntry.DefinitionType.WSDL1.equals(service.getDefinitionType())) {
                RestApiUtil.handleBadRequest("Unsupported definition type provided. Cannot re-import service to " +
                        "API using the service type " + service.getDefinitionType(), log);
            }
            APIDefinitionValidationResponse validationAPIResponse = null;
            if (ServiceEntry.DefinitionType.WSDL1.equals(service.getDefinitionType())) {
                PublisherCommonUtils.addWsdl(RestApiConstants.APPLICATION_OCTET_STREAM,
                        service.getEndpointDef(), api, apiProvider, organization);
            } else {
                validationAPIResponse =
                        (APIDefinitionValidationResponse) validationResponseMap.get(RestApiConstants.RETURN_MODEL);
                if (!validationAPIResponse.isValid()) {
                    RestApiUtil.handleBadRequest(validationAPIResponse.getErrorItems(), log);
                }
            }
            String protocol = (validationAPIResponse != null ? validationAPIResponse.getProtocol() : "" );
            if (!APIConstants.API_TYPE_WEBSUB.equalsIgnoreCase(protocol)) {
                api.setEndpointConfig(PublisherCommonUtils.constructEndpointConfigForService(service.getServiceUrl(),
                        protocol));
            }
            API updatedApi = apiProvider.updateAPI(api, originalAPI);
            if (validationAPIResponse != null) {
                PublisherCommonUtils.updateAPIDefinition(apiId, validationAPIResponse, service, organization);
            }
            return Response.ok().entity(APIMappingUtil.fromAPItoDTO(updatedApi)).build();
        } catch (APIManagementException e) {
            if (ExceptionCodes.MISSING_PROTOCOL_IN_ASYNC_API_DEFINITION.getErrorCode() == e.getErrorHandler()
                    .getErrorCode()) {
                RestApiUtil.handleBadRequest("Missing protocol in the Service Definition", log);
            } else if (ExceptionCodes.UNSUPPORTED_PROTOCOL_SPECIFIED_IN_ASYNC_API_DEFINITION.getErrorCode() ==
                    e.getErrorHandler().getErrorCode()) {
                RestApiUtil.handleBadRequest("Unsupported protocol specified in the Service Definition. Protocol " +
                        "should be either sse or websub or ws", log);
            }
            RestApiUtil.handleInternalServerError("Error while retrieving the service key of the service " +
                    "associated with API with id " + apiId, log);
        } catch (FaultGatewaysException e) {
            String errorMessage = "Error while updating API : " + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    private APIDTO importOpenAPIDefinition(InputStream definition, String definitionUrl, String inlineDefinition,
                                           APIDTO apiDTOFromProperties, Attachment fileDetail, ServiceEntry service,
                                           String organization) throws APIManagementException {
        // Validate and retrieve the OpenAPI definition
        Map validationResponseMap = null;
        boolean isServiceAPI = false;

        if (service != null) {
            isServiceAPI = true;
        }
        try {
            validationResponseMap = validateOpenAPIDefinition(definitionUrl, definition, fileDetail, inlineDefinition,
                    true, isServiceAPI);
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error occurred while validating API Definition", e, log);
        }

        OpenAPIDefinitionValidationResponseDTO validationResponseDTO =
                (OpenAPIDefinitionValidationResponseDTO) validationResponseMap.get(RestApiConstants.RETURN_DTO);
        APIDefinitionValidationResponse validationResponse =
                (APIDefinitionValidationResponse) validationResponseMap.get(RestApiConstants.RETURN_MODEL);

        if (!validationResponseDTO.isIsValid()) {
            ErrorDTO errorDTO = APIMappingUtil.getErrorDTOFromErrorListItems(validationResponseDTO.getErrors());
            throw RestApiUtil.buildBadRequestException(errorDTO);
        }

        // Only HTTP or WEBHOOK type APIs should be allowed
        if (!(APIDTO.TypeEnum.HTTP.equals(apiDTOFromProperties.getType())
                || APIDTO.TypeEnum.WEBHOOK.equals(apiDTOFromProperties.getType()))) {
            throw RestApiUtil.buildBadRequestException(
                    "The API's type is not supported when importing an OpenAPI definition");
        }
        // Import the API and Definition
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        // Add description from definition if it is not defined by user
        if (validationResponseDTO.getInfo().getDescription() != null
                && apiDTOFromProperties.getDescription() == null) {
            apiDTOFromProperties.setDescription(validationResponse.getInfo().getDescription());
        }
        if (isServiceAPI) {
            apiDTOFromProperties.setType(PublisherCommonUtils.getAPIType(service.getDefinitionType(), null));
        }
        API apiToAdd = PublisherCommonUtils.prepareToCreateAPIByDTO(apiDTOFromProperties, apiProvider,
                RestApiCommonUtil.getLoggedInUsername(), organization);
        boolean syncOperations = apiDTOFromProperties.getOperations().size() > 0;
        API addedAPI = ApisApiServiceImplUtils.importAPIDefinition(apiToAdd, apiProvider, organization,
                service, validationResponse, isServiceAPI, syncOperations);
        return APIMappingUtil.fromAPItoDTO(addedAPI);
    }

    private APIDTO importAsyncAPISpecification(InputStream definition, String definitionUrl, APIDTO apiDTOFromProperties,
                                           Attachment fileDetail, ServiceEntry service, String organization) {
        //validate and retrieve the AsyncAPI specification
        Map<String, Object> validationResponseMap = null;
        boolean isServiceAPI = false;
        try {
            if (service != null) {
                isServiceAPI = true;
            }
            validationResponseMap = validateAsyncAPISpecification(definitionUrl, definition, fileDetail, true,
                    isServiceAPI);
        } catch (APIManagementException e) {
            RestApiUtil.handleInternalServerError("Error occurred while validating API Definition", e, log);
        }

        AsyncAPISpecificationValidationResponseDTO validationResponseDTO =
                (AsyncAPISpecificationValidationResponseDTO) validationResponseMap.get(RestApiConstants.RETURN_DTO);
        APIDefinitionValidationResponse validationResponse =
                (APIDefinitionValidationResponse) validationResponseMap.get(RestApiConstants.RETURN_MODEL);

        if (!validationResponseDTO.isIsValid()) {
            ErrorDTO errorDTO = APIMappingUtil.getErrorDTOFromErrorListItems(validationResponseDTO.getErrors());
            throw RestApiUtil.buildBadRequestException(errorDTO);
        }
        //Import the API and Definition
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            API api = PublisherCommonUtils.importAsyncAPIWithDefinition(validationResponse, isServiceAPI,
                    apiDTOFromProperties, service, organization, apiProvider);
            return APIMappingUtil.fromAPItoDTO(api);
        } catch (APIManagementException e) {
            String errorMessage = "Error while adding new API : " + apiDTOFromProperties.getProvider() + "-" +
                    apiDTOFromProperties.getName() + "-" + apiDTOFromProperties.getVersion() + " - " + e.getMessage();
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    @Override
    public Response updateAPIDeployment(String apiId, String deploymentId, APIRevisionDeploymentDTO
            apIRevisionDeploymentDTO, MessageContext messageContext) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();

        //validate if api exists
        APIInfo apiInfo = CommonUtils.validateAPIExistence(apiId);
        //validate API update operation permitted based on the LC state
        validateAPIOperationsPerLC(apiInfo.getStatus().toString());

        String revisionId = apIRevisionDeploymentDTO.getRevisionUuid();
        String vhost = apIRevisionDeploymentDTO.getVhost();
        Boolean displayOnDevportal = apIRevisionDeploymentDTO.isDisplayOnDevportal();
        String decodedDeploymentName = ApisApiServiceImplUtils.getDecodedDeploymentName(deploymentId);
        APIRevisionDeployment apiRevisionDeployment = ApisApiServiceImplUtils.mapApiRevisionDeployment(revisionId, vhost,
                displayOnDevportal, decodedDeploymentName);
        apiProvider.updateAPIDisplayOnDevportal(apiId, revisionId, apiRevisionDeployment);
        APIRevisionDeployment apiRevisionDeploymentsResponse = apiProvider.
                getAPIRevisionDeployment(decodedDeploymentName, revisionId);
        APIRevisionDeploymentDTO apiRevisionDeploymentDTO = APIMappingUtil.
                fromAPIRevisionDeploymenttoDTO(apiRevisionDeploymentsResponse);
        Response.Status status = Response.Status.OK;

        return Response.status(status).entity(apiRevisionDeploymentDTO).build();
    }

    @Override
    public Response apisApiIdEnvironmentsEnvIdKeysGet(String apiId, String envId, MessageContext messageContext)
            throws APIManagementException {
        // validate api UUID
        CommonUtils.validateAPIExistence(apiId);
        // validate environment UUID
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        validateEnvironment(organization, envId);

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        // get properties
        EnvironmentPropertiesDTO properties = apiProvider.getEnvironmentSpecificAPIProperties(apiId, envId);
        // convert to string to remove null values
        String jsonContent = new Gson().toJson(properties);

        return Response.ok().entity(jsonContent).build();
    }

    @Override
    public Response apisApiIdEnvironmentsEnvIdKeysPut(String apiId, String envId, Map<String, String> requestBody,
            MessageContext messageContext) throws APIManagementException {
        // validate api UUID
        CommonUtils.validateAPIExistence(apiId);
        // validate environment UUID
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        validateEnvironment(organization, envId);

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        // adding properties

        EnvironmentPropertiesDTO properties = ApisApiServiceImplUtils.generateEnvironmentPropertiesDTO(requestBody);
        apiProvider.addEnvironmentSpecificAPIProperties(apiId, envId, properties);
        // get properties
        properties = apiProvider.getEnvironmentSpecificAPIProperties(apiId, envId);
        // convert to string to remove null values
        String jsonContent = new Gson().toJson(properties);

        return Response.ok().entity(jsonContent).build();
    }

    private void validateEnvironment(String organization, String envId) throws APIManagementException {
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        // if apiProvider.getEnvironment(organization, envId) return null, it will throw an exception
        apiProvider.getEnvironment(organization, envId);
    }

}
