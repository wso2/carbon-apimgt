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

package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.api.model.APIRating;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Comment;
import org.wso2.carbon.apimgt.api.model.CommentList;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DocumentationContent;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.GraphqlComplexityInfo;
import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.GraphqlSchemaType;
import org.wso2.carbon.apimgt.api.model.webhooks.Topic;
import org.wso2.carbon.apimgt.impl.APIClientGenerationException;
import org.wso2.carbon.apimgt.impl.APIClientGenerationManager;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.definitions.GraphQLSchemaDefinition;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.ApisApiService;


import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashMap;
import java.util.Arrays;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.CommentMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.DocumentationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.GraphqlQueryAnalysisMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.AsyncAPIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestAPIStoreUtils;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

public class ApisApiServiceImpl implements ApisApiService {

    private static final Log log = LogFactory.getLog(ApisApiServiceImpl.class);

    @Override
    public Response apisGet(Integer limit, Integer offset, String xWSO2Tenant, String query, String ifNoneMatch,
            MessageContext messageContext) {
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query == null ? "" : query;
        APIListDTO apiListDTO = new APIListDTO();
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            String username = RestApiCommonUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);

            //revert content search back to normal search by name to avoid doc result complexity and to comply with REST api practices
            if (query.startsWith(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + ":")) {
                query = query
                        .replace(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + ":", APIConstants.NAME_TYPE_PREFIX + ":");
            }

            Map allMatchedApisMap = apiConsumer.searchPaginatedAPIs(query, organization, offset,
                    limit, null, null);
            

            Set<Object> sortedSet = (Set<Object>) allMatchedApisMap.get("apis"); // This is a SortedSet
            ArrayList<Object> allMatchedApis = new ArrayList<>(sortedSet);

            apiListDTO = APIMappingUtil.fromAPIListToDTO(allMatchedApis, organization);
            //Add pagination section in the response
            Object totalLength = allMatchedApisMap.get("length");
            Integer totalAvailableAPis = 0;
            if (totalLength != null) {
                totalAvailableAPis = (Integer) totalLength;
            }

            APIMappingUtil
                    .setPaginationParams(apiListDTO, query, offset, limit, totalAvailableAPis);

            return Response.ok().entity(apiListDTO).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.rootCauseMessageMatches(e, "start index seems to be greater than the limit count")) {
                //this is not an error of the user as he does not know the total number of apis available. Thus sends
                //  an empty response
                apiListDTO.setCount(0);
                apiListDTO.setPagination(new PaginationDTO());
                return Response.ok().entity(apiListDTO).build();
            } else {
                String errorMessage = "Error while retrieving APIs";
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } 
        return null;
    }

    @Override
    public Response apisApiIdGet(String apiId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext)
            throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        return Response.ok().entity(getAPIByAPIId(apiId, organization)).build();
    }


    /**
     * Get complexity details of a given API
     *
     * @param apiId          apiId
     * @param messageContext message context
     * @return Response with complexity details of the GraphQL API
     */
    @Override
    public Response apisApiIdGraphqlPoliciesComplexityGet(String apiId, MessageContext messageContext) {
        try {
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            API api = apiConsumer.getLightweightAPIByUUID(apiId, organization);
            if (APIConstants.GRAPHQL_API.equals(api.getType())) {
                GraphqlComplexityInfo graphqlComplexityInfo = apiConsumer.getComplexityDetails(apiId);
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

    @Override
    public Response apisApiIdGraphqlPoliciesComplexityTypesGet(String apiId, MessageContext messageContext) throws APIManagementException {
        GraphQLSchemaDefinition graphql = new GraphQLSchemaDefinition();
        try {
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            API api = apiConsumer.getLightweightAPIByUUID(apiId, organization);
            if (APIConstants.GRAPHQL_API.equals(api.getType())) {
                String schemaContent = apiConsumer.getGraphqlSchemaDefinition(apiId,organization);
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


    @Override
    public Response apisApiIdGraphqlSchemaGet(String apiId, String ifNoneMatch, String xWSO2Tenant, MessageContext messageContext) {
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
            // keep this line to check the existence of the api
            apiConsumer.getLightweightAPIByUUID(apiId, organization);
            String graphQLSchema = apiConsumer.getGraphqlSchemaDefinition(apiId, organization);
            return Response.ok().entity(graphQLSchema).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    @Override
    public Response addCommentToAPI(String apiId, PostRequestBodyDTO postRequestBodyDTO, String replyTo,
                                    MessageContext messageContext) throws APIManagementException {
        String username = RestApiCommonUtil.getLoggedInUsername();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        try {
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
            ApiTypeWrapper apiTypeWrapper = apiConsumer.getAPIorAPIProductByUUID(apiId, organization);
            Comment comment = new Comment();
            comment.setText(postRequestBodyDTO.getContent());
            comment.setCategory(postRequestBodyDTO.getCategory());
            comment.setParentCommentID(replyTo);
            comment.setEntryPoint("DEVPORTAL");
            comment.setUser(username);
            comment.setApiId(apiId);
            String createdCommentId = apiConsumer.addComment(apiId, comment, username);
            Comment createdComment = apiConsumer.getComment(apiTypeWrapper, createdCommentId, 0, 0);
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

    /**
     * Retrieves the async api specification document of an API
     *
     * @param apiId           API identifier
     * @param environmentName Name of the gateway environment
     * @param ifNoneMatch     If-None-Match header value
     * @param xWSO2Tenant     Requested tenant domain for cross tenant invocations
     * @param messageContext  CXF message context
     * @return                Async API Specification document of the API for the given cluster or gateway environment
     */
    @Override
    public Response apisApiIdAsyncApiSpecificationGet(String apiId, String environmentName, String ifNoneMatch,
                                                      String xWSO2Tenant, MessageContext messageContext)
            throws APIManagementException {
            try {
                String organization = RestApiUtil.getValidatedOrganization(messageContext);
                APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
                String asyncApiSpecification = apiConsumer.getAsyncAPIDefinition(apiId, organization);

                return Response.ok().entity(asyncApiSpecification).header("Content-Disposition",
                        "attachment; filename=\"" + "async_api.json" + "\"" ).build();

            } catch (APIManagementException e) {
                if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                    RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_API, apiId, e, log);
                } else if (RestApiUtil.isDueToResourceNotFound(e)) {
                    RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
                } else {
                    String errorMessage = "Error while retrieving Async API Specification of API : " + apiId;
                    RestApiUtil.handleInternalServerError(errorMessage, e, log);
                }
            }
            return null;
    }

    @Override
    public Response getAllCommentsOfAPI(String apiId, String xWSO2Tenant, Integer limit, Integer offset,
                                        Boolean includeCommenterInfo, MessageContext messageContext)
            throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        try {
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
            ApiTypeWrapper apiTypeWrapper = apiConsumer.getAPIorAPIProductByUUID(apiId, organization);
            String parentCommentID = null;
            CommentList comments = apiConsumer.getComments(apiTypeWrapper, parentCommentID, limit, offset);
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
    public Response getCommentOfAPI(String commentId, String apiId, String xWSO2Tenant, String ifNoneMatch,
                                    Boolean includeCommenterInfo, Integer replyLimit, Integer replyOffset,
                                    MessageContext messageContext) throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        try {
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
            ApiTypeWrapper apiTypeWrapper = apiConsumer.getAPIorAPIProductByUUID(apiId, organization);
            Comment comment = apiConsumer.getComment(apiTypeWrapper, commentId, replyLimit, replyOffset);

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
    public Response getRepliesOfComment(String commentId, String apiId, String xWSO2Tenant, Integer limit,
                                        Integer offset, String ifNoneMatch, Boolean includeCommenterInfo,
                                        MessageContext messageContext) throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        try {
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
            ApiTypeWrapper apiTypeWrapper = apiConsumer.getAPIorAPIProductByUUID(apiId, organization);
            CommentList comments = apiConsumer.getComments(apiTypeWrapper, commentId, limit, offset);
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
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        try {
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
            ApiTypeWrapper apiTypeWrapper = apiConsumer.getAPIorAPIProductByUUID(apiId, organization);
            Comment comment = apiConsumer.getComment(apiTypeWrapper, commentId, 0, 0);
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
                        if (apiConsumer.editComment(apiTypeWrapper, commentId, comment)) {
                            Comment editedComment = apiConsumer.getComment(apiTypeWrapper, commentId, 0, 0);
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
    public Response deleteComment(String commentId, String apiId, String ifMatch, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        String username = RestApiCommonUtil.getLoggedInUsername();
        try {
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
            ApiTypeWrapper apiTypeWrapper = apiConsumer.getAPIorAPIProductByUUID(apiId, organization);
            Comment comment = apiConsumer.getComment(apiTypeWrapper, commentId, 0, 0);
            if (comment != null) {
                String[] tokenScopes = (String[]) PhaseInterceptorChain.getCurrentMessage().getExchange()
                        .get(RestApiConstants.USER_REST_API_SCOPES);
                if (Arrays.asList(tokenScopes).contains("apim:admin") || comment.getUser().equals(username)) {
                    if (apiConsumer.deleteComment(apiTypeWrapper, commentId)) {
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

    @Override
    public Response apisApiIdDocumentsDocumentIdContentGet(String apiId, String documentId, String xWSO2Tenant,
            String ifNoneMatch, MessageContext messageContext) {
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();

            DocumentationContent docContent = apiConsumer.getDocumentationContent(apiId, documentId, organization);
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
            if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
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

    @Override
    public Response apisApiIdDocumentsDocumentIdGet(String apiId, String documentId, String xWSO2Tenant,
            String ifModifiedSince, MessageContext messageContext) {
        Documentation documentation;
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            String username = RestApiCommonUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);

            if (!RestAPIStoreUtils.isUserAccessAllowedForAPIByUUID(apiId, organization)) {
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_API, apiId, log);
            }

            documentation = apiConsumer.getDocumentation(apiId, documentId, organization);
            if (null != documentation) {
                DocumentDTO documentDTO = DocumentationMappingUtil.fromDocumentationToDTO(documentation);
                return Response.ok().entity(documentDTO).build();
            } else {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_DOCUMENTATION, documentId, log);
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                RestApiUtil.handleInternalServerError("Error while getting API " + apiId, e, log);
            }
        }
        return null;
    }

    @Override
    public Response apisApiIdDocumentsGet(String apiId, Integer limit, Integer offset, String xWSO2Tenant,
            String ifNoneMatch, MessageContext messageContext) {
        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            String username = RestApiCommonUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);

            //this will fail if user doesn't have access to the API or the API does not exist
            //APIIdentifier apiIdentifier = APIMappingUtil.getAPIIdentifierFromUUID(apiId, organization);

            //List<Documentation> documentationList = apiConsumer.getAllDocumentation(apiIdentifier, username);
            List<Documentation> documentationList = apiConsumer.getAllDocumentation(apiId, organization);
            DocumentListDTO documentListDTO = DocumentationMappingUtil
                    .fromDocumentationListToDTO(documentationList, offset, limit);

            //todo : set total count properly
            DocumentationMappingUtil
                    .setPaginationParams(documentListDTO, apiId, offset, limit, documentationList.size());
            return Response.ok().entity(documentListDTO).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                RestApiUtil.handleInternalServerError("Error while getting API " + apiId, e, log);
            }
        } /*catch (UnsupportedEncodingException e) {
            String errorMessage = "Error while Decoding apiId" + apiId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }*/
        return null;
    }

    @Override
    public Response apisApiIdRatingsGet(String id, Integer limit, Integer offset, String xWSO2Tenant,
            MessageContext messageContext) {
        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            String username = RestApiCommonUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
            apiConsumer.checkAPIVisibility(id, organization);
            float avgRating = apiConsumer.getAverageAPIRating(id);
            int userRating = 0;
            if (!APIConstants.WSO2_ANONYMOUS_USER.equals(username)) {
                userRating = apiConsumer.getUserRating(id, username);
            }
            List<RatingDTO> ratingDTOList = new ArrayList<>();
            JSONArray array = apiConsumer.getAPIRatings(id);
            for (int i = 0; i < array.size(); i++) {
                JSONObject obj = (JSONObject) array.get(i);
                RatingDTO ratingDTO = APIMappingUtil.fromJsonToRatingDTO(obj);
                ratingDTO.setApiId(id);
                ratingDTOList.add(ratingDTO);
            }
            RatingListDTO ratingListDTO = APIMappingUtil.fromRatingListToDTO(ratingDTOList, offset, limit);
            ratingListDTO.setUserRating(userRating);
            ratingListDTO.setAvgRating(String.valueOf(avgRating));
            APIMappingUtil.setRatingPaginationParams(ratingListDTO, id, offset, limit, ratingDTOList.size());
            return Response.ok().entity(ratingListDTO).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(
                        RestApiConstants.RESOURCE_RATING + " for " + RestApiConstants.RESOURCE_API, id, e, log);
            } else {
                RestApiUtil.handleInternalServerError("Error while retrieving ratings for API " + id, e, log);
            }
        }
        return null;
    }

    /**
     * Rest api implementation to downloading the client sdk for given api in given sdk language.
     *
     * @param apiId : The id of the api.
     * @param language : Preferred sdk language.
     * @param messageContext : messageContext
     * @return : The sdk as a zip archive.
     */
    @Override
    public Response apisApiIdSdksLanguageGet(String apiId, String language, String xWSO2Tenant,
            MessageContext messageContext) throws APIManagementException {

        if (StringUtils.isEmpty(apiId) || StringUtils.isEmpty(language)) {
            String message = "Error generating the SDK. API id or language should not be empty";
            RestApiUtil.handleBadRequest(message, log);
        }
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APIDTO api = getAPIByAPIId(apiId, organization);
        APIClientGenerationManager apiClientGenerationManager = new APIClientGenerationManager();
        Map<String, String> sdkArtifacts;
        String swaggerDefinition = api.getApiDefinition();
        if (api != null) {
            try {
                sdkArtifacts = apiClientGenerationManager.generateSDK(language, api.getName(), api.getVersion(),
                        swaggerDefinition);
                //Create the sdk response.
                File sdkFile = new File(sdkArtifacts.get("zipFilePath"));
                return Response.ok(sdkFile, MediaType.APPLICATION_OCTET_STREAM_TYPE).header("Content-Disposition",
                        "attachment; filename=\"" + sdkArtifacts.get("zipFileName") + "\"" ).build();
            } catch (APIClientGenerationException e) {
                String message = "Error generating client sdk for api: " + api.getName() + " for language: " + language;
                RestApiUtil.handleInternalServerError(message, e, log);
            }
        }
        String message = "Could not find an API for ID " + apiId;
        RestApiUtil.handleResourceNotFoundError(message, log);
        return null;
    }

    /**
     * Retrieves the swagger document of an API
     *
     * @param apiId API identifier
     * @param environmentName name of the gateway environment
     * @param ifNoneMatch If-None-Match header value
     * @param xWSO2Tenant requested tenant domain for cross tenant invocations
     * @param messageContext CXF message context
     * @return Swagger document of the API for the given cluster or gateway environment
     */
    @Override
    public Response apisApiIdSwaggerGet(String apiId, String environmentName,
            String ifNoneMatch, String xWSO2Tenant, String xWSO2TenantQ, MessageContext messageContext) {
        try {
            String organization;
            if (StringUtils.isNotEmpty(xWSO2TenantQ) && StringUtils.isEmpty(xWSO2Tenant)) {
                organization = RestApiUtil.getRequestedTenantDomain(xWSO2TenantQ);
            } else {
                organization = RestApiUtil.getValidatedOrganization(messageContext);
            }
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();

            API api = apiConsumer.getLightweightAPIByUUID(apiId, organization);
            if (api.getUuid() == null) {
                api.setUuid(apiId);
            }

            if (api.getSwaggerDefinition() != null) {
                api.setSwaggerDefinition(APIUtil.removeInterceptorsFromSwagger(
                        APIUtil.removeXMediationScriptsFromSwagger(api.getSwaggerDefinition())));
            } else {
                api.setSwaggerDefinition(apiConsumer.getOpenAPIDefinition(apiId, organization));
            }

            // gets the first available environment if environment is not provided
            if (StringUtils.isEmpty(environmentName)) {
                Map<String, Environment> existingEnvironments = APIUtil.getEnvironments(organization);

                // find a valid environment name from API
                // gateway environment may be invalid due to inconsistent state of the API
                // example: publish an API and later rename gateway environment from configurations
                //          then the old gateway environment name becomes invalid
                for (String environmentNameOfApi : api.getEnvironments()) {
                    if (existingEnvironments.get(environmentNameOfApi) != null) {
                        environmentName = environmentNameOfApi;
                        break;
                    }
                }

                // if all environment of API are invalid or there are no environments (i.e. empty)
                if (StringUtils.isEmpty(environmentName)) {
                    // if there are no environments in the API, take a random environment from the existing ones.
                    // This is to make sure the swagger doesn't have invalid endpoints
                    if (!existingEnvironments.keySet().isEmpty()) {
                        environmentName = existingEnvironments.keySet().iterator().next();
                    }
                }
            }

            String apiSwagger = null;
            if (StringUtils.isNotEmpty(environmentName)) {
                try {
                    apiSwagger = apiConsumer.getOpenAPIDefinitionForEnvironment(api, environmentName);
                } catch (APIManagementException e) {
                    // handle gateway not found exception otherwise pass it
                    if (RestApiUtil.isDueToResourceNotFound(e)) {
                        RestApiUtil.handleResourceNotFoundError(
                                "Gateway environment '" + environmentName + "' not found", e, log);
                        return null;
                    }
                    throw e;
                }
            } else {
                apiSwagger = api.getSwaggerDefinition();
            }

            return Response.ok().entity(apiSwagger).header("Content-Disposition",
                    "attachment; filename=\"" + "swagger.json" + "\"" ).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving swagger of API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    @Override
    public Response apisApiIdThumbnailGet(String apiId, String xWSO2Tenant, String ifNoneMatch, MessageContext messageContext) {
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();

            //this will fail if user does not have access to the API or the API does not exist
            apiConsumer.getLightweightAPIByUUID(apiId, organization);
            ResourceFile thumbnailResource = apiConsumer.getIcon(apiId, organization);

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
            } else {
                String errorMessage = "Error while retrieving thumbnail of API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } 
        return null;
    }

    @Override
    public Response apisApiIdTopicsGet(String apiId, String xWSO2Tenant, MessageContext messageContext) throws APIManagementException {
        if (org.apache.commons.lang.StringUtils.isNotEmpty(apiId)) {
            String username = RestApiCommonUtil.getLoggedInUsername();
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            Set<Topic> topics;
            try {
                APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
                ApiTypeWrapper apiTypeWrapper = apiConsumer.getAPIorAPIProductByUUID(apiId, organization);
                TopicListDTO topicListDTO;
                if (apiTypeWrapper.isAPIProduct()) {
                    topics = apiConsumer.getTopics(apiTypeWrapper.getApiProduct().getUuid());
                } else {
                    topics = apiConsumer.getTopics(apiTypeWrapper.getApi().getUuid());
                }
                topicListDTO = AsyncAPIMappingUtil.fromTopicListToDTO(topics);
                return Response.ok().entity(topicListDTO).build();
            } catch (APIManagementException e) {
                if (RestApiUtil.isDueToResourceNotFound(e) || RestApiUtil.isDueToAuthorizationFailure(e)) {
                    RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
                } else {
                    RestApiUtil.handleInternalServerError("Failed to get topics of Async API " + apiId, e, log);
                }
            }
        } else {
            RestApiUtil.handleBadRequest("API Id is missing in request", log);
        }
        return null;
    }

    @Override
    public Response apisApiIdUserRatingPut(String id, RatingDTO body, String xWSO2Tenant,
            MessageContext messageContext) {
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            int rating = 0;
            String username = RestApiCommonUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
            //this will fail if user doesn't have access to the API or the API does not exist
            apiConsumer.checkAPIVisibility(id, organization);

            if (body != null) {
                rating = body.getRating();
            }
            switch (rating) {
                //Below case 0[Rate 0] - is to remove ratings from a user
                case 0: {
                    apiConsumer.rateAPI(id, APIRating.RATING_ZERO, username);
                    break;
                }
                case 1: {
                    apiConsumer.rateAPI(id, APIRating.RATING_ONE, username);
                    break;
                }
                case 2: {
                    apiConsumer.rateAPI(id, APIRating.RATING_TWO, username);
                    break;
                }
                case 3: {
                    apiConsumer.rateAPI(id, APIRating.RATING_THREE, username);
                    break;
                }
                case 4: {
                    apiConsumer.rateAPI(id, APIRating.RATING_FOUR, username);
                    break;
                }
                case 5: {
                    apiConsumer.rateAPI(id, APIRating.RATING_FIVE, username);
                    break;
                }
                default: {
                    RestApiUtil.handleBadRequest("Provided API Rating is not in the range from 1 to 5", log);
                }
            }
            JSONObject obj = apiConsumer.getUserRatingInfo(id, username);
            RatingDTO ratingDTO = new RatingDTO();
            if (obj != null && !obj.isEmpty()) {
                ratingDTO = APIMappingUtil.fromJsonToRatingDTO(obj);
                ratingDTO.setApiId(id);
            }
            return Response.ok().entity(ratingDTO).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        RestApiConstants.RESOURCE_RATING + " for " + RestApiConstants.RESOURCE_API, id, e, log);
            } else if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(
                        RestApiConstants.RESOURCE_RATING + " for " + RestApiConstants.RESOURCE_API, id, e, log);
            } else {
                RestApiUtil
                        .handleInternalServerError("Error while adding/updating user rating for API " + id, e, log);
            }
        }
        return null;
    }

    @Override
    public Response apisApiIdUserRatingGet(String id, String xWSO2Tenant, String ifNoneMatch,
            MessageContext messageContext) {
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            String username = RestApiCommonUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
            //this will fail if user doesn't have access to the API or the API does not exist
            apiConsumer.checkAPIVisibility(id, organization);
            JSONObject obj = apiConsumer.getUserRatingInfo(id, username);
            RatingDTO ratingDTO = new RatingDTO();
            if (obj != null && !obj.isEmpty()) {
                ratingDTO = APIMappingUtil.fromJsonToRatingDTO(obj);
                ratingDTO.setApiId(id);
            }
            return Response.ok().entity(ratingDTO).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        RestApiConstants.RESOURCE_RATING + " for " + RestApiConstants.RESOURCE_API, id, e, log);
            } else if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(
                        RestApiConstants.RESOURCE_RATING + " for " + RestApiConstants.RESOURCE_API, id, e, log);
            } else {
                RestApiUtil.handleInternalServerError("Error while retrieving user rating for API " + id, e, log);
            }
        }
        return null;
    }

    @Override
    public Response apisApiIdUserRatingDelete(String apiId, String xWSO2Tenant, String ifMatch,
            MessageContext messageContext) {
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            String username = RestApiCommonUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
            //this will fail if user doesn't have access to the API or the API does not exist
            apiConsumer.checkAPIVisibility(apiId, organization);
            apiConsumer.removeAPIRating(apiId, username);
            return Response.ok().build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        RestApiConstants.RESOURCE_RATING + " for " + RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(
                        RestApiConstants.RESOURCE_RATING + " for " + RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                RestApiUtil.handleInternalServerError("Error while deleting user rating for API " + apiId, e, log);
            }
        }
        return null;
    }

    @Override
    public Response getWSDLOfAPI(String apiId, String environmentName, String ifNoneMatch,
                                 String xWSO2Tenant, MessageContext messageContext) throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
        API api = apiConsumer.getLightweightAPIByUUID(apiId, organization);
        APIIdentifier apiIdentifier = api.getId();

        List<Environment> environments = APIUtil.getEnvironmentsOfAPI(api);
        if (environments != null && environments.size() > 0) {
            if (StringUtils.isEmpty(environmentName)) {
                environmentName = api.getEnvironments().iterator().next();
            }

            Environment selectedEnvironment = null;
            for (Environment environment: environments) {
               if (environment.getName().equals(environmentName)) {
                   selectedEnvironment = environment;
                   break;
               }
            }

            if (selectedEnvironment == null) {
                throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.INVALID_GATEWAY_ENVIRONMENT,
                        environmentName));
            }
            ResourceFile wsdl = apiConsumer.getWSDL(api, selectedEnvironment.getName(), selectedEnvironment.getType(),
                    organization);

            return RestApiUtil.getResponseFromResourceFile(apiIdentifier.toString(), wsdl);
        } else {
            throw new APIManagementException(ExceptionCodes.from(ExceptionCodes.NO_GATEWAY_ENVIRONMENTS_ADDED,
                    apiIdentifier.toString()));
        }
    }

    @Override
    public Response apisApiIdSubscriptionPoliciesGet(String apiId, String xWSO2Tenant, String ifNoneMatch,
                                                     MessageContext messageContext) throws APIManagementException {
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        APIDTO apiInfo = getAPIByAPIId(apiId, organization);
        List<Tier> availableThrottlingPolicyList = new ThrottlingPoliciesApiServiceImpl()
                .getThrottlingPolicyList(ThrottlingPolicyDTO.PolicyLevelEnum.SUBSCRIPTION.toString(), organization);

        if (apiInfo != null ) {
            List<APITiersDTO> apiTiers = apiInfo.getTiers();
            if (apiTiers != null && !apiTiers.isEmpty()) {
                List<Tier> apiThrottlingPolicies = new ArrayList<>();
                for (Tier policy : availableThrottlingPolicyList) {
                    for (APITiersDTO apiTier :apiTiers) {
                        if (apiTier.getTierName().equalsIgnoreCase(policy.getName())) {
                            apiThrottlingPolicies.add(policy);
                        }
                    }
                }
                return Response.ok().entity(apiThrottlingPolicies).build();
            }
        }
        return null;
    }

    private APIDTO getAPIByAPIId(String apiId, String organization) {
        try {
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
            ApiTypeWrapper api = apiConsumer.getAPIorAPIProductByUUID(apiId, organization);
            String status = api.getStatus();

            // Extracting clicked API name by the user, for the recommendation system
            String userName = RestApiCommonUtil.getLoggedInUsername();
            apiConsumer.publishClickedAPI(api, userName, organization);

            if (APIConstants.PUBLISHED.equals(status) || APIConstants.PROTOTYPED.equals(status)
                            || APIConstants.DEPRECATED.equals(status)) {

                return APIMappingUtil.fromAPItoDTO(api, organization);
            } else {
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_API, apiId, log);
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_API, apiId, e, log);
            } else {
                String errorMessage = "Error while retrieving API : " + apiId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
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
        return errorMessage != null && errorMessage.contains(APIConstants.UN_AUTHORIZED_ERROR_MESSAGE);
    }
}
