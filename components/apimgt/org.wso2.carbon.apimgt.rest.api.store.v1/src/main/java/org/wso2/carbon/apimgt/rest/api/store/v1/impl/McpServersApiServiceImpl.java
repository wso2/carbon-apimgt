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

package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.phase.PhaseInterceptorChain;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIRating;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Comment;
import org.wso2.carbon.apimgt.api.model.CommentList;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DocumentationContent;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.OrganizationInfo;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.impl.APIClientGenerationException;
import org.wso2.carbon.apimgt.impl.APIClientGenerationManager;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.store.v1.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.CommentDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.CommentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PatchRequestBodyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PostRequestBodyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.RatingDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.RatingListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ThrottlingPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.CommentMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.DocumentationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * Implementation of the McpServersApiService interface, providing methods to manage MCP servers,
 * including adding comments, ratings, and retrieving server details.
 */
public class McpServersApiServiceImpl implements McpServersApiService {

    private static final Log log = LogFactory.getLog(McpServersApiServiceImpl.class);

    /**
     * Adds a comment to a given MCP server by its ID. Supports adding both new comments and replies to existing
     * comments.
     *
     * @param mcpServerId        the UUID of the MCP server to which the comment is added
     * @param postRequestBodyDTO the comment content and metadata such as category
     * @param replyTo            the ID of the parent comment if this is a reply; null otherwise
     * @param messageContext     the message context containing request-related metadata
     * @return a {@link Response} with the created {@link CommentDTO} and Location header
     * @throws APIManagementException if an error occurs during comment creation or URI building
     */
    @Override
    public Response addCommentToMCPServer(String mcpServerId, PostRequestBodyDTO postRequestBodyDTO, String replyTo,
                                          MessageContext messageContext) throws APIManagementException {

        String username = RestApiCommonUtil.getLoggedInUsername();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        try {
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
            ApiTypeWrapper apiTypeWrapper = apiConsumer.getAPIorAPIProductByUUID(mcpServerId, organization);
            Comment comment = new Comment();
            comment.setText(postRequestBodyDTO.getContent());
            comment.setCategory(postRequestBodyDTO.getCategory());
            comment.setParentCommentID(replyTo);
            comment.setEntryPoint(APIConstants.CommentEntryPoint.DEVPORTAL.toString());
            comment.setUser(username);
            comment.setApiId(mcpServerId);
            String createdCommentId = apiConsumer.addComment(mcpServerId, comment, username);
            Comment createdComment = apiConsumer.getComment(apiTypeWrapper, createdCommentId, 0, 0);
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
     * Adds or updates a user rating for a given MCP server.
     * The rating must be an integer value between 0 and 5. A value of 0 resets the rating.
     *
     * @param mcpServerId    the UUID of the MCP server to be rated
     * @param ratingDTO      the rating value wrapped in a {@link RatingDTO}
     * @param xWSO2Tenant    the tenant header (not directly used here, may be validated upstream)
     * @param messageContext the message context containing request metadata
     * @return a {@link Response} with the updated {@link RatingDTO}
     * @throws APIManagementException if an error occurs while setting or retrieving the rating
     */
    @Override
    public Response addMCPServerRating(String mcpServerId, RatingDTO ratingDTO, String xWSO2Tenant,
                                       MessageContext messageContext) {

        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            int rating = 0;
            String username = RestApiCommonUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
            apiConsumer.checkAPIVisibility(mcpServerId, organization);

            if (ratingDTO != null) {
                rating = ratingDTO.getRating();
            }
            switch (rating) {
                case 0: {
                    apiConsumer.rateAPI(mcpServerId, APIRating.RATING_ZERO, username);
                    break;
                }
                case 1: {
                    apiConsumer.rateAPI(mcpServerId, APIRating.RATING_ONE, username);
                    break;
                }
                case 2: {
                    apiConsumer.rateAPI(mcpServerId, APIRating.RATING_TWO, username);
                    break;
                }
                case 3: {
                    apiConsumer.rateAPI(mcpServerId, APIRating.RATING_THREE, username);
                    break;
                }
                case 4: {
                    apiConsumer.rateAPI(mcpServerId, APIRating.RATING_FOUR, username);
                    break;
                }
                case 5: {
                    apiConsumer.rateAPI(mcpServerId, APIRating.RATING_FIVE, username);
                    break;
                }
                default: {
                    RestApiUtil.handleBadRequest("Provided API Rating is not in the range from 1 to 5", log);
                }
            }
            JSONObject obj = apiConsumer.getUserRatingInfo(mcpServerId, username);
            RatingDTO updatedRatingDTO = new RatingDTO();
            if (obj != null && !obj.isEmpty()) {
                updatedRatingDTO = APIMappingUtil.fromJsonToRatingDTO(obj);
                updatedRatingDTO.setApiId(mcpServerId);
            }
            return Response.ok().entity(updatedRatingDTO).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        RestApiConstants.RESOURCE_RATING + " for " + RestApiConstants.RESOURCE_MCP_SERVER,
                        mcpServerId, e, log);
            } else if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(
                        RestApiConstants.RESOURCE_RATING + " for " + RestApiConstants.RESOURCE_MCP_SERVER,
                        mcpServerId, e, log);
            } else {
                RestApiUtil
                        .handleInternalServerError("Error while adding/updating user rating for MCP Server "
                                + mcpServerId, e, log);
            }
        }
        return null;
    }

    /**
     * Deletes a specific comment of an MCP server if the user is the comment owner or has admin privileges.
     *
     * @param mcpServerId    the UUID of the MCP server from which the comment should be deleted
     * @param commentId      the UUID of the comment to be deleted
     * @param ifMatch        optional ETag for conditional deletion (currently unused)
     * @param messageContext the message context containing request metadata
     * @return a {@link Response} indicating success or failure of the deletion operation
     * @throws APIManagementException if an error occurs during comment retrieval or deletion
     */
    @Override
    public Response deleteCommentOfMCPServer(String mcpServerId, String commentId, String ifMatch,
                                             MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        String username = RestApiCommonUtil.getLoggedInUsername();
        try {
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
            ApiTypeWrapper apiTypeWrapper = apiConsumer.getAPIorAPIProductByUUID(mcpServerId, organization);
            Comment comment = apiConsumer.getComment(apiTypeWrapper, commentId, 0, 0);
            if (comment != null) {
                String[] tokenScopes = (String[]) PhaseInterceptorChain.getCurrentMessage().getExchange()
                        .get(RestApiConstants.USER_REST_API_SCOPES);
                if (Arrays.asList(tokenScopes).contains(RestApiConstants.ADMIN_SCOPE)
                        || comment.getUser().equals(username)) {
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
     * Deletes the logged-in user's rating for a specified MCP server.
     *
     * @param mcpServerId    the UUID of the MCP server whose rating is to be deleted
     * @param xWSO2Tenant    the tenant header (not directly used here, may be validated upstream)
     * @param ifMatch        optional ETag for conditional deletion (currently unused)
     * @param messageContext the message context containing request-related metadata
     * @return a {@link Response} indicating the result of the delete operation
     * @throws APIManagementException if an error occurs while validating visibility or deleting the rating
     */
    @Override
    public Response deleteMCPServerRating(String mcpServerId, String xWSO2Tenant, String ifMatch,
                                          MessageContext messageContext) {

        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            String username = RestApiCommonUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
            apiConsumer.checkAPIVisibility(mcpServerId, organization);
            apiConsumer.removeAPIRating(mcpServerId, username);
            return Response.ok().build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        RestApiConstants.RESOURCE_RATING + " for " + RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId,
                        e, log);
            } else if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(
                        RestApiConstants.RESOURCE_RATING + " for " + RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId,
                        e, log);
            } else {
                RestApiUtil.handleInternalServerError("Error while deleting user rating for MCP Server " + mcpServerId,
                        e, log);
            }
        }
        return null;
    }

    /**
     * Edits an existing comment of a given MCP server. The user must be the owner of the comment to edit it.
     *
     * @param mcpServerId         the UUID of the MCP server containing the comment
     * @param commentId           the UUID of the comment to be edited
     * @param patchRequestBodyDTO the content and category updates for the comment
     * @param messageContext      the message context containing request-related metadata
     * @return a {@link Response} with the updated {@link CommentDTO} and Location header
     * @throws APIManagementException if an error occurs during comment retrieval or update
     */
    @Override
    public Response editCommentOfMCPServer(String mcpServerId, String commentId,
                                           PatchRequestBodyDTO patchRequestBodyDTO, MessageContext messageContext)
            throws APIManagementException {

        String username = RestApiCommonUtil.getLoggedInUsername();
        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        try {
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
            ApiTypeWrapper apiTypeWrapper = apiConsumer.getAPIorAPIProductByUUID(mcpServerId, organization);
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
     * Retrieves all comments of a given MCP server, optionally including commenter information.
     *
     * @param mcpServerId          the UUID of the MCP server whose comments are to be retrieved
     * @param xWSO2Tenant          the tenant header (not directly used here, may be validated upstream)
     * @param limit                the maximum number of comments to retrieve (default is 10)
     * @param offset               the offset for pagination (default is 0)
     * @param includeCommenterInfo whether to include commenter information in the response
     * @param messageContext       the message context containing request-related metadata
     * @return a {@link Response} with a list of {@link CommentListDTO} and Location header
     * @throws APIManagementException if an error occurs during comment retrieval or URI building
     */
    @Override
    public Response getAllCommentsOfMCPServer(String mcpServerId, String xWSO2Tenant, Integer limit, Integer offset,
                                              Boolean includeCommenterInfo, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        try {
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
            ApiTypeWrapper apiTypeWrapper = apiConsumer.getAPIorAPIProductByUUID(mcpServerId, organization);
            String parentCommentID = null;
            CommentList comments = apiConsumer.getComments(apiTypeWrapper, parentCommentID, limit, offset);
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
     * Retrieves all MCP servers available in the system, with optional pagination and search capabilities.
     *
     * @param limit          the maximum number of MCP servers to retrieve (default is 10)
     * @param offset         the offset for pagination (default is 0)
     * @param xWSO2Tenant    the tenant header (not directly used here, may be validated upstream)
     * @param query          the search query to filter MCP servers by name or content
     * @param ifNoneMatch    optional ETag for conditional requests (not directly used here)
     * @param messageContext the message context containing request-related metadata
     * @return a {@link Response} with a list of {@link APIListDTO} containing MCP server details
     * @throws APIManagementException if an error occurs during MCP server retrieval or search
     */
    @Override
    public Response getAllMCPServers(Integer limit, Integer offset, String xWSO2Tenant, String query,
                                     String ifNoneMatch, MessageContext messageContext) {

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query == null ? StringUtils.EMPTY : query;
        APIListDTO apiListDTO = new APIListDTO();
        try {
            String superOrganization = RestApiUtil.getValidatedOrganization(messageContext);
            OrganizationInfo orgInfo = RestApiUtil.getOrganizationInfo(messageContext);
            orgInfo.setSuperOrganization(superOrganization);

            String username = RestApiCommonUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);

            //revert content search back to normal search by name to avoid doc result complexity and to comply with
            // REST api practices
            if (query.startsWith(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + ":")) {
                query = query
                        .replace(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + ":",
                                APIConstants.NAME_TYPE_PREFIX + ":");
            }

            Map allMatchedApisMap;
            if (APIUtil.isOrganizationAccessControlEnabled()) {
                allMatchedApisMap = apiConsumer.searchPaginatedAPIs(query, orgInfo, offset,
                        limit, null, null);
            } else {
                allMatchedApisMap = apiConsumer.searchPaginatedAPIs(query, superOrganization, offset,
                        limit);
            }

            Set<Object> sortedSet = (Set<Object>) allMatchedApisMap.get("apis"); // This is a SortedSet
            ArrayList<Object> allMatchedApis = new ArrayList<>(sortedSet);

            apiListDTO = APIMappingUtil.fromAPIListToDTO(allMatchedApis, superOrganization);
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

    /**
     * Retrieves a specific comment of an MCP server by its ID, optionally including commenter information.
     *
     * @param mcpServerId          the UUID of the MCP server containing the comment
     * @param commentId            the UUID of the comment to be retrieved
     * @param xWSO2Tenant          the tenant header (not directly used here, may be validated upstream)
     * @param ifNoneMatch          optional ETag for conditional requests (not directly used here)
     * @param includeCommenterInfo whether to include commenter information in the response
     * @param replyLimit           the maximum number of replies to retrieve (default is 0, meaning no replies)
     * @param replyOffset          the offset for pagination of replies (default is 0)
     * @param messageContext       the message context containing request-related metadata
     * @return a {@link Response} with the requested {@link CommentDTO} and Location header
     * @throws APIManagementException if an error occurs during comment retrieval or URI building
     */
    @Override
    public Response getCommentOfMCPServer(String mcpServerId, String commentId, String xWSO2Tenant, String ifNoneMatch,
                                          Boolean includeCommenterInfo, Integer replyLimit, Integer replyOffset,
                                          MessageContext messageContext) throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        try {
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
            ApiTypeWrapper apiTypeWrapper = apiConsumer.getAPIorAPIProductByUUID(mcpServerId, organization);
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
                String errorMessage = "Error while retrieving comment for API : " + mcpServerId + "with comment ID "
                        + commentId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        } catch (URISyntaxException e) {
            String errorMessage = "Error while retrieving comment content location : " + mcpServerId;
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }

    /**
     * Retrieves a specific MCP server by its ID, including its details and associated metadata.
     *
     * @param mcpServerId    the UUID of the MCP server to be retrieved
     * @param xWSO2Tenant    the tenant header (not directly used here, may be validated upstream)
     * @param ifNoneMatch    optional ETag for conditional requests (not directly used here)
     * @param messageContext the message context containing request-related metadata
     * @return a {@link Response} with the requested {@link APIDTO} and Location header
     * @throws APIManagementException if an error occurs during MCP server retrieval or URI building
     */
    @Override
    public Response getMCPServer(String mcpServerId, String xWSO2Tenant, String ifNoneMatch,
                                 MessageContext messageContext) throws APIManagementException {

        String superOrganization = RestApiUtil.getValidatedOrganization(messageContext);
        OrganizationInfo userOrgInfo = RestApiUtil.getOrganizationInfo(messageContext);
        userOrgInfo.setSuperOrganization(superOrganization);
        return Response.ok().entity(getMCPServerByMCPServerId(mcpServerId, superOrganization, userOrgInfo)).build();
    }

    /**
     * Retrieves the content of a specific document associated with an MCP server by its ID.
     * The content type and disposition headers are set based on the document's source type.
     *
     * @param mcpServerId    the UUID of the MCP server containing the document
     * @param documentId     the UUID of the document to be retrieved
     * @param xWSO2Tenant    the tenant header (not directly used here, may be validated upstream)
     * @param ifNoneMatch    optional ETag for conditional requests (not directly used here)
     * @param messageContext the message context containing request-related metadata
     * @return a {@link Response} with the document content and appropriate headers
     * @throws APIManagementException if an error occurs during document retrieval or URI building
     */
    @Override
    public Response getMCPServerDocument(String mcpServerId, String documentId, String xWSO2Tenant, String ifNoneMatch,
                                         MessageContext messageContext) throws APIManagementException {

        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();

            DocumentationContent docContent =
                    apiConsumer.getDocumentationContent(mcpServerId, documentId, organization);
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
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId, e, log);
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
     * Retrieves the content of a specific document associated with an MCP server by its ID.
     * The content type and disposition headers are set based on the document's source type.
     *
     * @param mcpServerId    the UUID of the MCP server containing the document
     * @param documentId     the UUID of the document to be retrieved
     * @param xWSO2Tenant    the tenant header (not directly used here, may be validated upstream)
     * @param ifNoneMatch    optional ETag for conditional requests (not directly used here)
     * @param messageContext the message context containing request-related metadata
     * @return a {@link Response} with the document content and appropriate headers
     * @throws APIManagementException if an error occurs during document retrieval or URI building
     */
    @Override
    public Response getMCPServerDocumentContent(String mcpServerId, String documentId, String xWSO2Tenant,
                                                String ifNoneMatch, MessageContext messageContext) {

        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();

            DocumentationContent docContent =
                    apiConsumer.getDocumentationContent(mcpServerId, documentId, organization);
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
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId, e, log);
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
     * Retrieves all documents associated with a specific MCP server, with optional pagination.
     *
     * @param mcpServerId    the UUID of the MCP server whose documents are to be retrieved
     * @param limit          the maximum number of documents to retrieve (default is 10)
     * @param offset         the offset for pagination (default is 0)
     * @param xWSO2Tenant    the tenant header (not directly used here, may be validated upstream)
     * @param ifNoneMatch    optional ETag for conditional requests (not directly used here)
     * @param messageContext the message context containing request-related metadata
     * @return a {@link Response} with a list of {@link DocumentListDTO} containing document details
     * @throws APIManagementException if an error occurs during document retrieval or pagination
     */
    @Override
    public Response getMCPServerDocuments(String mcpServerId, Integer limit, Integer offset, String xWSO2Tenant,
                                          String ifNoneMatch, MessageContext messageContext) {

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            String username = RestApiCommonUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
            List<Documentation> documentationList = apiConsumer.getAllDocumentation(mcpServerId, organization);
            DocumentListDTO documentListDTO = DocumentationMappingUtil
                    .fromDocumentationListToDTO(documentationList, offset, limit);

            DocumentationMappingUtil
                    .setPaginationParams(documentListDTO, mcpServerId, offset, limit, documentationList.size());
            return Response.ok().entity(documentListDTO).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId, e, log);
            } else if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId, e, log);
            } else {
                RestApiUtil.handleInternalServerError("Error while getting MCP Server " + mcpServerId, e, log);
            }
        }
        return null;
    }

    /**
     * Retrieves the rating information of a specific MCP server for the logged-in user.
     *
     * @param mcpServerId    the UUID of the MCP server whose rating is to be retrieved
     * @param xWSO2Tenant    the tenant header (not directly used here, may be validated upstream)
     * @param ifNoneMatch    optional ETag for conditional requests (not directly used here)
     * @param messageContext the message context containing request-related metadata
     * @return a {@link Response} with the {@link RatingDTO} containing user rating information
     * @throws APIManagementException if an error occurs while validating visibility or retrieving rating info
     */
    @Override
    public Response getMCPServerRating(String mcpServerId, String xWSO2Tenant, String ifNoneMatch,
                                       MessageContext messageContext) {

        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            String username = RestApiCommonUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
            //this will fail if user doesn't have access to the API or the API does not exist
            apiConsumer.checkAPIVisibility(mcpServerId, organization);
            JSONObject obj = apiConsumer.getUserRatingInfo(mcpServerId, username);
            RatingDTO ratingDTO = new RatingDTO();
            if (obj != null && !obj.isEmpty()) {
                ratingDTO = APIMappingUtil.fromJsonToRatingDTO(obj);
                ratingDTO.setApiId(mcpServerId);
            }
            return Response.ok().entity(ratingDTO).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(
                        RestApiConstants.RESOURCE_RATING + " for " + RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId,
                        e, log);
            } else if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(
                        RestApiConstants.RESOURCE_RATING + " for " + RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId,
                        e, log);
            } else {
                RestApiUtil.handleInternalServerError(
                        "Error while retrieving user rating for MCP Server " + mcpServerId, e, log);
            }
        }
        return null;
    }

    /**
     * Generates a client SDK for a specific MCP server in the requested programming language.
     *
     * @param mcpServerId    the UUID of the MCP server for which the SDK is to be generated
     * @param language       the programming language for which the SDK is to be generated
     * @param xWSO2Tenant    the tenant header (not directly used here, may be validated upstream)
     * @param messageContext the message context containing request-related metadata
     * @return a {@link Response} with the generated SDK file as an attachment
     * @throws APIManagementException if an error occurs during SDK generation or retrieval
     */
    @Override
    public Response getMCPServerSDK(String mcpServerId, String language, String xWSO2Tenant,
                                    MessageContext messageContext) throws APIManagementException {

        if (StringUtils.isEmpty(mcpServerId) || StringUtils.isEmpty(language)) {
            String message = "Error generating the SDK. API id or language should not be empty";
            RestApiUtil.handleBadRequest(message, log);
        }
        String superOrganization = RestApiUtil.getValidatedOrganization(messageContext);
        OrganizationInfo userOrgInfo = RestApiUtil.getOrganizationInfo(messageContext);
        userOrgInfo.setSuperOrganization(superOrganization);
        APIDTO api = getMCPServerByMCPServerId(mcpServerId, superOrganization, userOrgInfo);
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
                        "attachment; filename=\"" + sdkArtifacts.get("zipFileName") + "\"").build();
            } catch (APIClientGenerationException e) {
                String message =
                        "Error generating client sdk for MCP Server: " + api.getName() + " for language: " + language;
                RestApiUtil.handleInternalServerError(message, e, log);
            }
        }
        String message = "Could not find a MCP Server for ID " + mcpServerId;
        RestApiUtil.handleResourceNotFoundError(message, log);
        return null;
    }

    /**
     * Retrieves the available subscription policies for a specific MCP server.
     * The policies are filtered based on the tiers associated with the MCP server.
     *
     * @param mcpServerId    the UUID of the MCP server for which subscription policies are to be retrieved
     * @param xWSO2Tenant    the tenant header (not directly used here, may be validated upstream)
     * @param ifNoneMatch    optional ETag for conditional requests (not directly used here)
     * @param messageContext the message context containing request-related metadata
     * @return a {@link Response} with a list of available {@link Tier} objects representing subscription policies
     * @throws APIManagementException if an error occurs during policy retrieval or filtering
     */
    @Override
    public Response getMCPServerSubscriptionPolicies(String mcpServerId, String xWSO2Tenant, String ifNoneMatch,
                                                     MessageContext messageContext) throws APIManagementException {

        String superOrganization = RestApiUtil.getValidatedOrganization(messageContext);
        OrganizationInfo userOrgInfo = RestApiUtil.getOrganizationInfo(messageContext);
        userOrgInfo.setSuperOrganization(superOrganization);
        APIDTO apiInfo = getMCPServerByMCPServerId(mcpServerId, superOrganization, userOrgInfo);
        List<Tier> availableThrottlingPolicyList = new ThrottlingPoliciesApiServiceImpl()
                .getThrottlingPolicyList(ThrottlingPolicyDTO.PolicyLevelEnum.SUBSCRIPTION.toString(),
                        superOrganization);

        if (apiInfo != null) {
            List<APITiersDTO> apiTiers = apiInfo.getTiers();
            if (apiTiers != null && !apiTiers.isEmpty()) {
                List<Tier> apiThrottlingPolicies = new ArrayList<>();
                for (Tier policy : availableThrottlingPolicyList) {
                    for (APITiersDTO apiTier : apiTiers) {
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

    /**
     * Retrieves the Swagger definition of a specific MCP server, optionally filtered by environment and query
     * parameters.
     *
     * @param mcpServerId     the UUID of the MCP server whose Swagger definition is to be retrieved
     * @param environmentName the name of the environment for which the Swagger definition is requested (optional)
     * @param ifNoneMatch     optional ETag for conditional requests (not directly used here)
     * @param xWSO2Tenant     the tenant header (not directly used here, may be validated upstream)
     * @param xWSO2TenantQ    the tenant query parameter (not directly used here, may be validated upstream)
     * @param query           additional query parameters for filtering (optional)
     * @param messageContext  the message context containing request-related metadata
     * @return a {@link Response} with the Swagger definition as a JSON attachment
     * @throws APIManagementException if an error occurs during Swagger retrieval or processing
     */
    @Override
    public Response getMCPServerSwagger(String mcpServerId, String environmentName, String ifNoneMatch,
                                        String xWSO2Tenant, String xWSO2TenantQ, String query,
                                        MessageContext messageContext) {

        try {
            String organization;
            if (StringUtils.isNotEmpty(xWSO2TenantQ) && StringUtils.isEmpty(xWSO2Tenant)) {
                organization = RestApiUtil.getRequestedTenantDomain(xWSO2TenantQ);
            } else {
                organization = RestApiUtil.getValidatedOrganization(messageContext);
            }
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();

            API api = apiConsumer.getLightweightAPIByUUID(mcpServerId, organization);
            if (api.getUuid() == null) {
                api.setUuid(mcpServerId);
            }

            if (api.getSwaggerDefinition() != null) {
                api.setSwaggerDefinition(APIUtil.removeInterceptorsFromSwagger(
                        APIUtil.removeXMediationScriptsFromSwagger(api.getSwaggerDefinition())));
            } else {
                api.setSwaggerDefinition(apiConsumer.getOpenAPIDefinition(mcpServerId, organization));
            }
            if (StringUtils.isEmpty(environmentName)) {
                Map<String, Environment> existingEnvironments = APIUtil.getEnvironments(organization);

                for (String environmentNameOfApi : api.getEnvironments()) {
                    if (existingEnvironments.get(environmentNameOfApi) != null) {
                        environmentName = environmentNameOfApi;
                        break;
                    }
                }
                if (StringUtils.isEmpty(environmentName)) {

                    if (!existingEnvironments.keySet().isEmpty()) {
                        environmentName = existingEnvironments.keySet().iterator().next();
                    }
                }
            }

            String apiSwagger = null;
            if (StringUtils.isNotEmpty(environmentName)) {
                try {
                    if (StringUtils.isNotEmpty(query)) {
                        String kmId = APIMappingUtil.getKmIdValue(query);
                        if (StringUtils.isNotBlank(kmId)) {
                            apiSwagger = apiConsumer.getOpenAPIDefinitionForEnvironmentByKm(api, environmentName, kmId);
                        }
                    } else {
                        apiSwagger = apiConsumer.getOpenAPIDefinitionForEnvironment(api, environmentName);
                    }
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
                    "attachment; filename=\"" + "swagger.json" + "\"").build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId, e, log);
            } else if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId, e, log);
            } else {
                String errorMessage = "Error while retrieving swagger of API : " + mcpServerId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * Retrieves the rating information of a specific MCP server for the logged-in user.
     *
     * @param mcpServerId    the UUID of the MCP server whose rating is to be retrieved
     * @param xWSO2Tenant    the tenant header (not directly used here, may be validated upstream)
     * @param ifNoneMatch    optional ETag for conditional requests (not directly used here)
     * @param messageContext the message context containing request-related metadata
     * @return a {@link Response} with the {@link RatingDTO} containing user rating information
     * @throws APIManagementException if an error occurs while validating visibility or retrieving rating info
     */
    @Override
    public Response getMCPServerThumbnail(String mcpServerId, String xWSO2Tenant, String ifNoneMatch,
                                          MessageContext messageContext) throws APIManagementException {

        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();

            //this will fail if user does not have access to the API or the API does not exist
            apiConsumer.getLightweightAPIByUUID(mcpServerId, organization);
            ResourceFile thumbnailResource = apiConsumer.getIcon(mcpServerId, organization);

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
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId, e, log);
            } else {
                String errorMessage = "Error while retrieving thumbnail of MCP Server : " + mcpServerId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }

    /**
     * Retrieves the replies of a specific comment associated with an MCP server.
     * The replies are paginated based on the provided limit and offset.
     *
     * @param mcpServerId          the UUID of the MCP server containing the comment
     * @param commentId            the UUID of the comment whose replies are to be retrieved
     * @param xWSO2Tenant          the tenant header (not directly used here, may be validated upstream)
     * @param limit                the maximum number of replies to retrieve (default is 10)
     * @param offset               the offset for pagination of replies (default is 0)
     * @param ifNoneMatch          optional ETag for conditional requests (not directly used here)
     * @param includeCommenterInfo whether to include commenter information in the response
     * @param messageContext       the message context containing request-related metadata
     * @return a {@link Response} with a list of {@link CommentListDTO} containing reply details
     * @throws APIManagementException if an error occurs during reply retrieval or URI building
     */
    @Override
    public Response getRepliesOfCommentOfMCPServer(String mcpServerId, String commentId, String xWSO2Tenant,
                                                   Integer limit, Integer offset, String ifNoneMatch,
                                                   Boolean includeCommenterInfo, MessageContext messageContext)
            throws APIManagementException {

        String organization = RestApiUtil.getValidatedOrganization(messageContext);
        try {
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
            ApiTypeWrapper apiTypeWrapper = apiConsumer.getAPIorAPIProductByUUID(mcpServerId, organization);
            CommentList comments = apiConsumer.getComments(apiTypeWrapper, commentId, limit, offset);
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
     * Retrieves the ratings of a specific MCP server, including average rating and user-specific rating.
     * The ratings are paginated based on the provided limit and offset.
     *
     * @param mcpServerId    the UUID of the MCP server whose ratings are to be retrieved
     * @param limit          the maximum number of ratings to retrieve (default is 10)
     * @param offset         the offset for pagination of ratings (default is 0)
     * @param xWSO2Tenant    the tenant header (not directly used here, may be validated upstream)
     * @param messageContext the message context containing request-related metadata
     * @return a {@link Response} with a list of {@link RatingListDTO} containing rating details
     * @throws APIManagementException if an error occurs during rating retrieval or pagination
     */
    @Override
    public Response getMCPServerRatings(String mcpServerId, Integer limit, Integer offset,
                                                    String xWSO2Tenant, MessageContext messageContext) {

        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        try {
            String organization = RestApiUtil.getValidatedOrganization(messageContext);
            String username = RestApiCommonUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
            apiConsumer.checkAPIVisibility(mcpServerId, organization);
            float avgRating = apiConsumer.getAverageAPIRating(mcpServerId);
            int userRating = 0;
            if (!APIConstants.WSO2_ANONYMOUS_USER.equals(username)) {
                userRating = apiConsumer.getUserRating(mcpServerId, username);
            }
            List<RatingDTO> ratingDTOList = new ArrayList<>();
            JSONArray array = apiConsumer.getAPIRatings(mcpServerId);
            for (int i = 0; i < array.size(); i++) {
                JSONObject obj = (JSONObject) array.get(i);
                RatingDTO ratingDTO = APIMappingUtil.fromJsonToRatingDTO(obj);
                ratingDTO.setApiId(mcpServerId);
                ratingDTOList.add(ratingDTO);
            }
            RatingListDTO ratingListDTO = APIMappingUtil.fromRatingListToDTO(ratingDTOList, offset, limit);
            ratingListDTO.setUserRating(userRating);
            ratingListDTO.setAvgRating(String.valueOf(avgRating));
            APIMappingUtil.setRatingPaginationParams(ratingListDTO, mcpServerId, offset, limit, ratingDTOList.size());
            return Response.ok().entity(ratingListDTO).build();
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(
                        RestApiConstants.RESOURCE_RATING + " for " + RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId,
                        e, log);
            } else {
                RestApiUtil.handleInternalServerError("Error while retrieving ratings for MCP Server " + mcpServerId, e,
                        log);
            }
        }
        return null;
    }

    /**
     * Retrieves the API details by its ID, including status checks and user organization visibility.
     *
     * @param mcpServerId  the UUID of the MCP Server to be retrieved
     * @param organization the organization to which the API belongs
     * @param userOrgInfo  information about the user's organization
     * @return an {@link APIDTO} containing API details if found and accessible, otherwise throws an error
     */
    private APIDTO getMCPServerByMCPServerId(String mcpServerId, String organization, OrganizationInfo userOrgInfo) {

        try {
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
            ApiTypeWrapper api = apiConsumer.getAPIorAPIProductByUUID(mcpServerId, organization);
            String status = api.getStatus();
            String userOrg = userOrgInfo.getOrganizationId();

            String userName = RestApiCommonUtil.getLoggedInUsername();

            if (!api.isAPIProduct() && !RestApiUtil.isOrganizationVisibilityAllowed(userName,
                    api.getApi().getVisibleOrganizations(), userOrg)) {
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId, log);
            }

            if (!api.isAPIProduct() && !StringUtils.isEmpty(userOrgInfo.getOrganizationId())) {
                org.wso2.carbon.apimgt.rest.api.store.v1.utils.APIUtils.updateAvailableTiersByOrganization(
                        api.getApi(), userOrgInfo.getOrganizationId());
            }

            // Extracting clicked API name by the user, for the recommendation system
            apiConsumer.publishClickedAPI(api, userName, organization);

            if (APIConstants.PUBLISHED.equals(status) || APIConstants.PROTOTYPED.equals(status)
                    || APIConstants.DEPRECATED.equals(status)) {

                APIDTO apidto = APIMappingUtil.fromAPItoDTO(api, organization);
                long subscriptionCountOfAPI = apiConsumer.getSubscriptionCountOfAPI(mcpServerId, organization);
                apidto.setSubscriptions(subscriptionCountOfAPI);
                return apidto;
            } else {
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId, log);
            }
        } catch (APIManagementException e) {
            if (RestApiUtil.isDueToAuthorizationFailure(e)) {
                RestApiUtil.handleAuthorizationFailure(RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId, e, log);
            } else if (RestApiUtil.isDueToResourceNotFound(e)) {
                RestApiUtil.handleResourceNotFoundError(RestApiConstants.RESOURCE_MCP_SERVER, mcpServerId, e, log);
            } else {
                String errorMessage = "Error while retrieving MCP Server: " + mcpServerId;
                RestApiUtil.handleInternalServerError(errorMessage, e, log);
            }
        }
        return null;
    }
}
