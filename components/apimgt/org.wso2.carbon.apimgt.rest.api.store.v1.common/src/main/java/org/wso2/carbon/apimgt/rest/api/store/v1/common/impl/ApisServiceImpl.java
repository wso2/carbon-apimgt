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

package org.wso2.carbon.apimgt.rest.api.store.v1.common.impl;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.ExceptionCodes;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIRating;
import org.wso2.carbon.apimgt.api.model.ApiTypeWrapper;
import org.wso2.carbon.apimgt.api.model.Comment;
import org.wso2.carbon.apimgt.api.model.CommentList;
import org.wso2.carbon.apimgt.api.model.Documentation;
import org.wso2.carbon.apimgt.api.model.DocumentationContent;
import org.wso2.carbon.apimgt.api.model.Environment;
import org.wso2.carbon.apimgt.api.model.ResourceFile;
import org.wso2.carbon.apimgt.api.model.Tier;
import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.GraphqlComplexityInfo;
import org.wso2.carbon.apimgt.api.model.graphql.queryanalysis.GraphqlSchemaType;
import org.wso2.carbon.apimgt.api.model.webhooks.Topic;
import org.wso2.carbon.apimgt.impl.APIClientGenerationException;
import org.wso2.carbon.apimgt.impl.APIClientGenerationManager;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.definitions.GraphQLSchemaDefinition;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.store.v1.common.mappings.APIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.common.mappings.AsyncAPIMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.common.mappings.CommentMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.common.mappings.DocumentationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.common.mappings.GraphqlQueryAnalysisMappingUtil;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APITiersDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.CommentDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.CommentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.GraphQLQueryComplexityInfoDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.GraphQLSchemaTypeListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PaginationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PostRequestBodyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.RatingDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.RatingListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ThrottlingPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.TopicListDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestAPIStoreUtils;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This class has Api service related Implementation
 */
public class ApisServiceImpl {

    private static final Log log = LogFactory.getLog(ApisServiceImpl.class);

    private ApisServiceImpl() {
    }

    /**
     * Get API List
     *
     * @param limit        limit
     * @param offset       offset
     * @param query        query
     * @param organization organization
     * @return
     */
    public static APIListDTO getAPIList(Integer limit, Integer offset, String query, String organization)
            throws APIManagementException {
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        query = query == null ? "" : query;
        APIListDTO apiListDTO = new APIListDTO();

        try {
            String username = RestApiCommonUtil.getLoggedInUsername();
            APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);

            //revert content search back to normal search by name to avoid doc result complexity and to comply
            // with REST api practices
            if (query.startsWith(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + ":")) {
                query = query.replace(APIConstants.CONTENT_SEARCH_TYPE_PREFIX + ":",
                        APIConstants.NAME_TYPE_PREFIX + ":");
            }

            Map<String, Object> allMatchedApisMap = apiConsumer.searchPaginatedAPIs(query, organization, offset, limit,
                    null, null);

            Set<Object> sortedSet = (Set<Object>) allMatchedApisMap.get("apis"); // This is a SortedSet
            ArrayList<Object> allMatchedApis = new ArrayList<>(sortedSet);

            apiListDTO = APIMappingUtil.fromAPIListToDTO(allMatchedApis, organization);
            //Add pagination section in the response
            Object totalLength = allMatchedApisMap.get("length");
            Integer totalAvailableAPis = 0;
            if (totalLength != null) {
                totalAvailableAPis = (Integer) totalLength;
            }

            APIMappingUtil.setPaginationParams(apiListDTO, query, offset, limit, totalAvailableAPis);

            return apiListDTO;
        } catch (APIManagementException e) {
            if (RestApiUtil.rootCauseMessageMatches(e, "start index seems to be greater than the limit count")) {
                //this is not an error of the user as he does not know the total number of apis available. Thus sends
                //  an empty response
                apiListDTO.setCount(0);
                apiListDTO.setPagination(new PaginationDTO());
                return apiListDTO;
            } else {
                String errorMessage = "Error while retrieving APIs";
                throw new APIManagementException(errorMessage, e.getErrorHandler());
            }
        }
    }

    /**
     * @param apiId apiId
     * @param organization organization
     * @return
     */
    public static GraphQLQueryComplexityInfoDTO getGraphqlPoliciesComplexity(String apiId, String organization)
            throws APIManagementException {
        try {
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
            API api = apiConsumer.getLightweightAPIByUUID(apiId, organization);
            if (APIConstants.GRAPHQL_API.equals(api.getType())) {
                GraphqlComplexityInfo graphqlComplexityInfo = apiConsumer.getComplexityDetails(apiId);
                return GraphqlQueryAnalysisMappingUtil.fromGraphqlComplexityInfotoDTO(graphqlComplexityInfo);
            } else {
                throw new APIManagementException(ExceptionCodes.API_NOT_GRAPHQL);
            }
        } catch (APIManagementException e) {
            String msg = "Error while retrieving complexity details of API " + apiId;
            throw new APIManagementException(msg, e.getErrorHandler());
        }
    }

    /**
     * @param apiId
     * @param organization
     * @return
     * @throws APIManagementException
     */
    public static GraphQLSchemaTypeListDTO getGraphqlPoliciesComplexityTypes(String apiId, String organization)
            throws APIManagementException {
        GraphQLSchemaDefinition graphql = new GraphQLSchemaDefinition();
        try {
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
            API api = apiConsumer.getLightweightAPIByUUID(apiId, organization);
            if (APIConstants.GRAPHQL_API.equals(api.getType())) {
                String schemaContent = apiConsumer.getGraphqlSchemaDefinition(apiId, organization);
                List<GraphqlSchemaType> typeList = graphql.extractGraphQLTypeList(schemaContent);
                return GraphqlQueryAnalysisMappingUtil.fromGraphqlSchemaTypeListtoDTO(typeList);
            } else {
                throw new APIManagementException(ExceptionCodes.API_NOT_GRAPHQL);
            }
        } catch (APIManagementException e) {
            String msg = "Error while retrieving types and fields of the schema of API " + apiId;
            throw new APIManagementException(msg, e.getErrorHandler());
        }
    }

    /**
     * @param apiId
     * @param organization
     * @return
     */
    public static String getGraphqlSchemaDefinition(String apiId, String organization) throws APIManagementException {
        try {
            APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
            // keep this line to check the existence of the api
            apiConsumer.getLightweightAPIByUUID(apiId, organization);
            return apiConsumer.getGraphqlSchemaDefinition(apiId, organization);
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving API : " + apiId;
            throw new APIManagementException(errorMessage, e.getErrorHandler());
        }
    }

    /**
     * @param apiId
     * @param postRequestBodyDTO
     * @param replyTo
     * @param organization
     * @return
     * @throws APIManagementException
     */
    public static CommentDTO addCommentToAPI(String apiId, PostRequestBodyDTO postRequestBodyDTO, String replyTo,
            String organization) throws APIManagementException {

        String username = RestApiCommonUtil.getLoggedInUsername();
        APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
        ApiTypeWrapper apiTypeWrapper = apiConsumer.getAPIorAPIProductByUUID(apiId, organization);
        Comment comment = createComment(postRequestBodyDTO.getContent(), postRequestBodyDTO.getCategory(), replyTo,
                username, apiId);
        String createdCommentId = apiConsumer.addComment(apiId, comment, username);
        Comment createdComment = apiConsumer.getComment(apiTypeWrapper, createdCommentId, 0, 0);
        return CommentMappingUtil.fromCommentToDTO(createdComment);
    }

    /**
     * @param apiId
     * @param organization
     * @param limit
     * @param offset
     * @param includeCommenterInfo
     * @return
     */
    public static CommentListDTO getAllCommentsOfAPI(String apiId, String organization, Integer limit, Integer offset,
            Boolean includeCommenterInfo) throws APIManagementException {

        APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
        ApiTypeWrapper apiTypeWrapper = apiConsumer.getAPIorAPIProductByUUID(apiId, organization);
        String parentCommentID = null;
        CommentList comments = apiConsumer.getComments(apiTypeWrapper, parentCommentID, limit, offset);
        return CommentMappingUtil.fromCommentListToDTO(comments, includeCommenterInfo);
    }

    /**
     * @param commentId
     * @param organization
     * @param apiId
     * @param includeCommenterInfo
     * @param replyLimit
     * @param replyOffset
     * @return
     */
    public static CommentDTO getCommentOfAPI(String commentId, String organization, String apiId,
            Boolean includeCommenterInfo, Integer replyLimit, Integer replyOffset) throws APIManagementException {

        APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
        ApiTypeWrapper apiTypeWrapper = apiConsumer.getAPIorAPIProductByUUID(apiId, organization);
        Comment comment = apiConsumer.getComment(apiTypeWrapper, commentId, replyLimit, replyOffset);

        if (comment != null) {
            CommentDTO commentDTO;
            if (Boolean.TRUE.equals(includeCommenterInfo)) {
                Map<String, Map<String, String>> userClaimsMap = CommentMappingUtil.retrieveUserClaims(
                        comment.getUser(), new HashMap<>());
                commentDTO = CommentMappingUtil.fromCommentToDTOWithUserInfo(comment, userClaimsMap);
            } else {
                commentDTO = CommentMappingUtil.fromCommentToDTO(comment);
            }
            return commentDTO;
        } else {
            String errorMessage = "Failed to retrieve comments for API " +  apiId;
            throw new APIManagementException(errorMessage, ExceptionCodes.COMMENT_NOT_FOUND);
        }
    }

    /**
     * @param commentId
     * @param apiId
     * @param limit
     * @param organization
     * @param offset
     * @param includeCommenterInfo
     * @return
     */
    public static CommentListDTO getRepliesOfComment(String commentId, String apiId, Integer limit, String organization,
            Integer offset, Boolean includeCommenterInfo) throws APIManagementException {

        APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
        ApiTypeWrapper apiTypeWrapper = apiConsumer.getAPIorAPIProductByUUID(apiId, organization);
        CommentList comments = apiConsumer.getComments(apiTypeWrapper, commentId, limit, offset);
        return CommentMappingUtil.fromCommentListToDTO(comments, includeCommenterInfo);

    }

    /**
     * @param commentId
     * @param apiId
     * @param organization
     * @param category
     * @param content
     * @return
     * @throws APIManagementException
     */
    public static CommentDTO editCommentOfAPI(String commentId, String apiId, String organization, String category,
            String content) throws APIManagementException {

        String username = RestApiCommonUtil.getLoggedInUsername();
        APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
        ApiTypeWrapper apiTypeWrapper = apiConsumer.getAPIorAPIProductByUUID(apiId, organization);
        Comment comment = apiConsumer.getComment(apiTypeWrapper, commentId, 0, 0);
        if (comment != null) {
            if (comment.getUser().equals(username)) {
                boolean isEditedComment = isEditedComment(comment, category, content);

                if (isEditedComment && apiConsumer.editComment(apiTypeWrapper, commentId, comment)) {
                    Comment editedComment = apiConsumer.getComment(apiTypeWrapper, commentId, 0, 0);
                    return CommentMappingUtil.fromCommentToDTO(editedComment);
                }
            } else {
                throw new APIManagementException(ExceptionCodes
                        .from(ExceptionCodes.COMMENT_NO_PERMISSION, username, comment.getId()));
            }
        } else {
            String errorMessage = "Failed to retrieve comment" + commentId + " of API " +  apiId;
            throw new APIManagementException(errorMessage, ExceptionCodes.COMMENT_NOT_FOUND);
        }
        return null;
    }

    /**
     * @param commentId
     * @param apiId
     * @param organization
     * @param tokenScopes
     * @return
     */
    public static JSONObject deleteComment(String commentId, String apiId, String organization, String[] tokenScopes)
            throws APIManagementException {

        String username = RestApiCommonUtil.getLoggedInUsername();
        APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
        ApiTypeWrapper apiTypeWrapper = apiConsumer.getAPIorAPIProductByUUID(apiId, organization);
        Comment comment = apiConsumer.getComment(apiTypeWrapper, commentId, 0, 0);
        if (comment != null) {
            if (Arrays.asList(tokenScopes).contains("apim:admin") || comment.getUser().equals(username)) {
                if (apiConsumer.deleteComment(apiTypeWrapper, commentId)) {
                    return generateMessageForDeletedComment(commentId);
                }
            } else {
                throw new APIManagementException(ExceptionCodes
                        .from(ExceptionCodes.COMMENT_NO_PERMISSION, username, comment.getId()));
            }
        } else {
            String errorMessage = "Failed to retrieve comment" + commentId + " of API " + apiId;
            throw new APIManagementException(errorMessage, ExceptionCodes.COMMENT_NOT_FOUND);
        }
        return null;
    }

    /**
     * @param apiId
     * @param documentId
     * @param organization
     * @return
     * @throws APIManagementException
     */
    public static DocumentationContent getDocumentContent(String apiId, String documentId, String organization)
            throws APIManagementException {

        APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
        DocumentationContent docContent = apiConsumer.getDocumentationContent(apiId, documentId, organization);
        if (docContent == null) {
            String msg = "Failed to get the document. Artifact corresponding to document id " + documentId
                    + " does not exist";
            throw new APIManagementException(msg, ExceptionCodes.DOCUMENT_NOT_FOUND);
        }
        return docContent;
    }

    /**
     * @param apiId
     * @param documentId
     * @param organization
     * @return
     */
    public static DocumentDTO getDocumentation(String apiId, String documentId, String organization)
            throws APIManagementException {
        Documentation documentation;
        String username = RestApiCommonUtil.getLoggedInUsername();
        APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);

        if (!RestAPIStoreUtils.isUserAccessAllowedForAPIByUUID(apiId, organization)) {
            throw new APIManagementException(
                    "User " + username + " does not have permission to access API with Id : " + apiId,
                    ExceptionCodes.NO_READ_PERMISSIONS);
        }

        documentation = apiConsumer.getDocumentation(apiId, documentId, organization);
        if (null != documentation) {
            return DocumentationMappingUtil.fromDocumentationToDTO(documentation);
        } else {
            String msg = "Failed to get the document. Artifact corresponding to document id " + documentId
                    + " does not exist";
            throw new APIManagementException(msg, ExceptionCodes.DOCUMENT_NOT_FOUND);
        }
    }


    /**
     * @param apiId
     * @param limit
     * @param offset
     * @param organization
     * @return
     */
    public static DocumentListDTO getDocumentationList(String apiId, Integer limit, Integer offset,
            String organization) throws APIManagementException {
        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

        String username = RestApiCommonUtil.getLoggedInUsername();
        APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
        List<Documentation> documentationList = apiConsumer.getAllDocumentation(apiId, organization);
        DocumentListDTO documentListDTO = DocumentationMappingUtil.fromDocumentationListToDTO(documentationList, offset,
                limit);
        DocumentationMappingUtil.setPaginationParams(documentListDTO, apiId, offset, limit, documentationList.size());
        return documentListDTO;

    }

    /**
     * @param id
     * @param limit
     * @param offset
     * @param organization
     * @return
     * @throws APIManagementException
     */
    public static RatingListDTO getAPIRating(String id, Integer limit, Integer offset, String organization)
            throws APIManagementException {
        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;

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
        return ratingListDTO;
    }

    /**
     * Rest api implementation to downloading the client sdk for given api in given sdk language.
     *
     * @param apiId    : The id of the api.
     * @param language : Preferred sdk language.
     * @return : The sdk as a zip archive.
     */
    public static Map<String, String> getSdkArtifacts(String apiId, String language, String organization)
            throws APIManagementException {

        APIDTO api = getAPIByAPIId(apiId, organization);
        APIClientGenerationManager apiClientGenerationManager = new APIClientGenerationManager();
        Map<String, String> sdkArtifacts;
        if (api != null) {
            try {
                sdkArtifacts = apiClientGenerationManager.generateSDK(language, api.getName(), api.getVersion(),
                        api.getApiDefinition());
                //Create the sdk response.
                return sdkArtifacts;
            } catch (APIClientGenerationException e) {
                String message = "Error generating client sdk for api: " + api.getName() + " for language: " + language;
                throw new APIManagementException(message, ExceptionCodes.SDK_NOT_GENERATED);
            }
        }
        String message = "Could not find an API for ID " + apiId;
        throw new APIManagementException(message, ExceptionCodes.from(ExceptionCodes.API_NOT_FOUND, apiId));
    }

    /**
     * Retrieves the swagger document of an API
     *
     * @param apiId           API identifier
     * @param environmentName name of the gateway environment
     * @return Swagger document of the API for the given cluster or gateway environment
     */

    public static String getOpenAPIDefinitionForEnvironment(String apiId, String environmentName, String organization)
            throws APIManagementException {
        String apiSwagger;
        APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
        API api = apiConsumer.getLightweightAPIByUUID(apiId, organization);
        removeInterceptorsFromSwagger(apiConsumer, apiId, organization, api);

        if (StringUtils.isEmpty(environmentName)) {
            environmentName = retrieveAvailableEnvironment(organization, api.getEnvironmentList());
        }

        if (StringUtils.isNotEmpty(environmentName)) {
            apiSwagger = getSwaggerForEnvironment(apiConsumer, api, organization);
        } else {
            apiSwagger = api.getSwaggerDefinition();
        }

        return apiSwagger;
    }

    private static String getSwaggerForEnvironment(APIConsumer apiConsumer, API api, String environmentName)
            throws APIManagementException {
        String apiSwagger;
        apiSwagger = apiConsumer.getOpenAPIDefinitionForEnvironment(api, environmentName);
        return apiSwagger;
    }

    /**
     * Get Thumbnail for apiId
     *
     * @param apiId        apiId
     * @param organization organization
     * @return ResourceFile
     */
    public static ResourceFile getThumbnail(String apiId, String organization) throws APIManagementException {
        APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
        apiConsumer.getLightweightAPIByUUID(apiId, organization);
        return apiConsumer.getIcon(apiId, organization);
    }

    /**
     * get APITopicList
     *
     * @param apiId        apiId
     * @param organization organization
     * @return TopicListDTO
     * @throws APIManagementException
     */
    public static TopicListDTO getAPITopicList(String apiId, String organization) throws APIManagementException {
        String username = RestApiCommonUtil.getLoggedInUsername();
        APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
        TopicListDTO topicListDTO;
        topicListDTO = AsyncAPIMappingUtil.fromTopicListToDTO(getTopics(apiConsumer, apiId, organization));
        return topicListDTO;
    }

    /**
     * update UserRating
     *
     * @param id           id
     * @param body         body
     * @param organization organization
     * @return
     */
    public static RatingDTO updateUserRating(String id, RatingDTO body, String organization)
            throws APIManagementException {
        int rating = 0;
        String username = RestApiCommonUtil.getLoggedInUsername();
        APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
        //this will fail if user doesn't have access to the API or the API does not exist
        apiConsumer.checkAPIVisibility(id, organization);
        if (body != null) {
            rating = body.getRating();
        }

        JSONObject obj = getRating(apiConsumer, rating, id, username);
        if (obj == null) {
            String message = "Provided API Rating is not in the range from 1 to 5";
            throw new APIManagementException(message, ExceptionCodes.RATING_VALUE_INVALID);
        }

        RatingDTO ratingDTO = new RatingDTO();
        if (obj != null && !obj.isEmpty()) {
            ratingDTO = APIMappingUtil.fromJsonToRatingDTO(obj);
            ratingDTO.setApiId(id);
        }
        return ratingDTO;
    }

    /**
     * get UserRating
     *
     * @param id           id
     * @param organization organization
     * @return
     */
    public static RatingDTO getUserRating(String id, String organization) throws APIManagementException {
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
        return ratingDTO;
    }

    /**
     * delete APIUserRating
     *
     * @param apiId        apiId
     * @param organization organization
     */
    public static void deleteAPIUserRating(String apiId, String organization) throws APIManagementException {
        String username = RestApiCommonUtil.getLoggedInUsername();
        APIConsumer apiConsumer = RestApiCommonUtil.getConsumer(username);
        //this will fail if user doesn't have access to the API or the API does not exist
        apiConsumer.checkAPIVisibility(apiId, organization);
        apiConsumer.removeAPIRating(apiId, username);
    }

    /**
     * get API
     *
     * @param apiId        apiId
     * @param organization organization
     * @return API
     * @throws APIManagementException
     */
    public static API getAPI(String apiId, String organization) throws APIManagementException {
        APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
        return apiConsumer.getLightweightAPIByUUID(apiId, organization);
    }

    public static ResourceFile getWSDLOfAPI(API api, String environmentName, String organization)
            throws APIManagementException {

        APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
        List<Environment> environments = APIUtil.getEnvironmentsOfAPI(api);
        if (!environments.isEmpty()) {
            return getWSDLFromSelectedEnvironment(apiConsumer, organization, api, environmentName, environments);
        } else {
            throw new APIManagementException(
                    ExceptionCodes.from(ExceptionCodes.NO_GATEWAY_ENVIRONMENTS_ADDED, api.getId().toString()));
        }
    }

    /**
     * get Subscription Policies
     *
     * @param apiId        apiId
     * @param organization organization
     * @return Tier List
     * @throws APIManagementException APIManagementException
     */
    public static List<Tier> getSubscriptionPolicies(String apiId, String organization) throws APIManagementException {
        APIDTO apiInfo = getAPIByAPIId(apiId, organization);
        List<Tier> availableThrottlingPolicyList = ThrottlingPoliciesServiceImpl.getThrottlingPolicyList(
                ThrottlingPolicyDTO.PolicyLevelEnum.SUBSCRIPTION.toString(), organization);
        List<Tier> tierList = null;

        if (apiInfo != null && apiInfo.getTiers() != null) {
            List<String> apiTiersNameList = apiInfo.getTiers().stream().map(APITiersDTO::getTierName)
                    .collect(Collectors.toList());
            tierList = getAPIThrottlePolicies(apiTiersNameList, availableThrottlingPolicyList);
        }
        return tierList;
    }

    /**
     * get API By Id
     *
     * @param apiId        apiId
     * @param organization organization
     * @return
     */
    public static APIDTO getAPIByAPIId(String apiId, String organization) throws APIManagementException {
        APIConsumer apiConsumer = RestApiCommonUtil.getLoggedInUserConsumer();
        ApiTypeWrapper api = apiConsumer.getAPIorAPIProductByUUID(apiId, organization);
        String status = api.getStatus();

        // Extracting clicked API name by the user, for the recommendation system
        String userName = RestApiCommonUtil.getLoggedInUsername();
        apiConsumer.publishClickedAPI(api, userName, organization);

        if (APIConstants.PUBLISHED.equals(status) || APIConstants.PROTOTYPED.equals(
                status) || APIConstants.DEPRECATED.equals(status)) {

            return APIMappingUtil.fromAPItoDTO(api, organization);
        } else {
            throw new APIManagementException(
                    "User " + userName + " does not have permission to access API with Id : " + apiId,
                    ExceptionCodes.NO_READ_PERMISSIONS);
        }
    }

    private static Comment createComment(String content, String category, String replyTo, String username,
            String apiId) {
        Comment comment = new Comment();
        comment.setText(content);
        comment.setCategory(category);
        comment.setParentCommentID(replyTo);
        comment.setEntryPoint("DEVPORTAL");
        comment.setUser(username);
        comment.setApiId(apiId);

        return comment;
    }

    private static boolean isEditedComment(Comment comment, String category, String content) {

        boolean commentEdited = false;
        if (category != null && !(category.equals(comment.getCategory()))) {
            comment.setCategory(category);
            commentEdited = true;
        }
        if (content != null && !(content.equals(comment.getText()))) {
            comment.setText(content);
            commentEdited = true;
        }
        return commentEdited;
    }

    private static JSONObject generateMessageForDeletedComment(String commentId) {
        JSONObject obj = new JSONObject();
        obj.put("id", commentId);
        obj.put("message", "The comment has been deleted");
        return obj;
    }

    private static void removeInterceptorsFromSwagger(APIConsumer apiConsumer, String apiId, String organization,
            API api) throws APIManagementException {

        if (api.getUuid() == null) {
            api.setUuid(apiId);
        }

        if (api.getSwaggerDefinition() != null) {
            api.setSwaggerDefinition(APIUtil.removeInterceptorsFromSwagger(
                    APIUtil.removeXMediationScriptsFromSwagger(api.getSwaggerDefinition())));
        } else {
            api.setSwaggerDefinition(apiConsumer.getOpenAPIDefinition(apiId, organization));
        }
    }

    private static String retrieveAvailableEnvironment(String organization, Set<String> environmentList)
            throws APIManagementException {

        String environmentName = "";
        Map<String, Environment> existingEnvironments = new HashMap<>();

        existingEnvironments = APIUtil.getEnvironments(organization);


        // find a valid environment name from API
        // gateway environment may be invalid due to inconsistent state of the API
        // example: publish an API and later rename gateway environment from configurations
        //          then the old gateway environment name becomes invalid
        for (String environmentNameOfApi : environmentList) {
            if (existingEnvironments.get(environmentNameOfApi) != null) {
                environmentName = environmentNameOfApi;
                break;
            }
        }

        // if all environment of API are invalid or there are no environments (i.e. empty)
        if (StringUtils.isEmpty(environmentName) && !existingEnvironments.keySet().isEmpty()) {
            // if there are no environments in the API, take a random environment from the existing ones.
            // This is to make sure the swagger doesn't have invalid endpoints
            environmentName = existingEnvironments.keySet().iterator().next();
        }

        return environmentName;
    }

    private static Set<Topic> getTopics(APIConsumer apiConsumer, String apiId, String organization)
            throws APIManagementException {
        Set<Topic> topics;
        ApiTypeWrapper apiTypeWrapper = apiConsumer.getAPIorAPIProductByUUID(apiId, organization);
        if (apiTypeWrapper.isAPIProduct()) {
            topics = apiConsumer.getTopics(apiTypeWrapper.getApiProduct().getUuid());
        } else {
            topics = apiConsumer.getTopics(apiTypeWrapper.getApi().getUuid());
        }
        return topics;
    }

    /**
     * get Rating
     *
     * @param apiConsumer apiConsumer
     * @param rating      rating
     * @param id          id
     * @param username    username
     * @return
     * @throws APIManagementException
     */
    public static JSONObject  getRating(APIConsumer apiConsumer, int rating, String id, String username)
            throws APIManagementException {
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
            return null;

        }
        }
        return apiConsumer.getUserRatingInfo(id, username);
    }

    /**
     * @param apiConsumer     APIConsumer
     * @param organization    Organization
     * @param api             API
     * @param environmentName Requested environmentName
     * @param environments    environment list
     * @return ResourceFile ResourceFile
     * @throws APIManagementException APIManagementException
     */
    private static ResourceFile getWSDLFromSelectedEnvironment(APIConsumer apiConsumer, String organization, API api,
            String environmentName, List<Environment> environments) throws APIManagementException {
        ResourceFile wsdl = null;
        if (StringUtils.isEmpty(environmentName)) {
            environmentName = api.getEnvironments().iterator().next();
        }

        Environment selectedEnvironment = null;
        for (Environment environment : environments) {
            if (environment.getName().equals(environmentName)) {
                selectedEnvironment = environment;
                break;
            }
        }

        if (selectedEnvironment != null) {
            wsdl = apiConsumer.getWSDL(api, selectedEnvironment.getName(), selectedEnvironment.getType(), organization);

        }
        return wsdl;
    }

    /**
     * @param apiTiersNameList              ApiTiersNameList
     * @param availableThrottlingPolicyList AvailableThrottlingPolicyList
     * @return TierList
     */
    public static List<Tier> getAPIThrottlePolicies(List<String> apiTiersNameList,
            List<Tier> availableThrottlingPolicyList) {

        if (apiTiersNameList != null && !apiTiersNameList.isEmpty()) {
            List<Tier> apiThrottlingPolicies = new ArrayList<>();
            for (Tier policy : availableThrottlingPolicyList) {
                for (String apiTier : apiTiersNameList) {
                    if (apiTier.equalsIgnoreCase(policy.getName())) {
                        apiThrottlingPolicies.add(policy);
                    }
                }
            }
            return apiThrottlingPolicies;
        }
        return null;
    }

}
