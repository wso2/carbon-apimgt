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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONObject;
import org.wso2.apk.apimgt.api.APIManagementException;
import org.wso2.apk.apimgt.api.APIProvider;
import org.wso2.apk.apimgt.api.ExceptionCodes;
import org.wso2.apk.apimgt.api.model.ApiTypeWrapper;
import org.wso2.apk.apimgt.api.model.Comment;
import org.wso2.apk.apimgt.api.model.CommentList;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.common.utils.BackofficeAPIUtils;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.common.utils.mappings.CommentMappingUtil;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.CommentDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.PatchRequestBodyDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.PostRequestBodyDTO;
import org.wso2.apk.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.apk.apimgt.rest.api.common.RestApiConstants;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ApiCommentsAPICommonImpl {

    public static final String MESSAGE = "message";

    private ApiCommentsAPICommonImpl() {

    }

    private static final Log log = LogFactory.getLog(ApiCommentsAPICommonImpl.class);

    public static String addCommentToAPI(String apiId, String postRequestBodyJson, String replyTo,
                                             String organization) throws APIManagementException {

        String username = RestApiCommonUtil.getLoggedInUsername();
        PostRequestBodyDTO postRequestBodyDTO = BackofficeAPIUtils.getDTOFromJson(postRequestBodyJson,
                PostRequestBodyDTO.class);
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, organization);
        Comment comment = createComment(postRequestBodyDTO.getContent(), postRequestBodyDTO.getCategory(),
                replyTo, username, apiId);
        String createdCommentId = apiProvider.addComment(apiId, comment, username);
        Comment createdComment = apiProvider.getComment(apiTypeWrapper, createdCommentId, 0, 0);
        return BackofficeAPIUtils.getJsonFromDTO(CommentMappingUtil.fromCommentToDTO(createdComment));
    }

    public static String getAllCommentsOfAPI(String apiId, Integer limit, Integer offset,
                                                     Boolean includeCommenterInfo, String requestedTenantDomain)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, requestedTenantDomain);
        String parentCommentID = null;
        CommentList comments = apiProvider.getComments(apiTypeWrapper, parentCommentID, limit, offset);
        return BackofficeAPIUtils.getJsonFromDTO(CommentMappingUtil.
                fromCommentListToDTO(comments, includeCommenterInfo));
    }

    public static String getCommentOfAPI(String commentId, String apiId, Boolean includeCommenterInfo,
                                             Integer replyLimit, Integer replyOffset, String requestedTenantDomain)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, requestedTenantDomain);
        Comment comment = apiProvider.getComment(apiTypeWrapper, commentId, replyLimit, replyOffset);

        CommentDTO commentDTO;
        if (Boolean.TRUE.equals(includeCommenterInfo)) {
            Map<String, Map<String, String>> userClaimsMap = CommentMappingUtil
                    .retrieveUserClaims(comment.getUser(), new HashMap<>());
            commentDTO = CommentMappingUtil.fromCommentToDTOWithUserInfo(comment, userClaimsMap);
        } else {
            commentDTO = CommentMappingUtil.fromCommentToDTO(comment);
        }
        return BackofficeAPIUtils.getJsonFromDTO(commentDTO);
    }

    public static String editCommentOfAPI(String commentId, String apiId, String patchRequestBodyJson)
            throws APIManagementException {

        CommentDTO commentDTO = null;
        String username = RestApiCommonUtil.getLoggedInUsername();
        PatchRequestBodyDTO patchRequestBodyDTO = BackofficeAPIUtils.getDTOFromJson(patchRequestBodyJson,
                PatchRequestBodyDTO.class);
        String requestedTenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, requestedTenantDomain);
        Comment comment = apiProvider.getComment(apiTypeWrapper, commentId, 0, 0);
        checkCommentOwner(comment, username);

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
        if (commentEdited && apiProvider.editComment(apiTypeWrapper, commentId, comment)) {
            Comment editedComment = apiProvider.getComment(apiTypeWrapper, commentId, 0, 0);
            commentDTO = CommentMappingUtil.fromCommentToDTO(editedComment);
        }
        return BackofficeAPIUtils.getJsonFromDTO(commentDTO);
    }

    public static String getRepliesOfComment(String commentId, String apiId, Integer limit, Integer offset,
                                                     Boolean includeCommenterInfo, String requestedTenantDomain)
            throws APIManagementException {

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, requestedTenantDomain);
        CommentList comments = apiProvider.getComments(apiTypeWrapper, commentId, limit, offset);
        return BackofficeAPIUtils.getJsonFromDTO(CommentMappingUtil.
                fromCommentListToDTO(comments, includeCommenterInfo));
    }

    /**
     * @param content  Comment content
     * @param category Category
     * @param replyTo  Parent comment ID
     * @param username User commenting
     * @param apiId    API UUID
     * @return Comment
     */
    public static Comment createComment(String content, String category, String replyTo, String username,
                                        String apiId) {

        Comment comment = new Comment();
        comment.setText(content);
        comment.setCategory(category);
        comment.setParentCommentID(replyTo);
        comment.setEntryPoint("PUBLISHER");
        comment.setUser(username);
        comment.setApiId(apiId);
        return comment;
    }

    public static void checkCommentOwner(Comment comment, String username) throws APIManagementException {

        if (!comment.getUser().equals(username)) {
            throw new APIManagementException(ExceptionCodes
                    .from(ExceptionCodes.COMMENT_NO_PERMISSION, username, comment.getId()));
        }
    }

    public static JSONObject deleteComment(String commentId, String apiId, String[] tokenScopes)
            throws APIManagementException {

        String requestedTenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        String username = RestApiCommonUtil.getLoggedInUsername();

        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        ApiTypeWrapper apiTypeWrapper = apiProvider.getAPIorAPIProductByUUID(apiId, requestedTenantDomain);
        Comment comment = apiProvider.getComment(apiTypeWrapper, commentId, 0, 0);

        if (Arrays.asList(tokenScopes).contains(RestApiConstants.ADMIN_SCOPE) || comment.getUser().equals(username)) {
            if (apiProvider.deleteComment(apiTypeWrapper, commentId)) {
                JSONObject obj = new JSONObject();
                obj.put("id", commentId);
                obj.put(MESSAGE, "The comment has been deleted");
                return obj;
            } else {
                throw new APIManagementException(ExceptionCodes.METHOD_NOT_ALLOWED);
            }
        } else {
            throw new APIManagementException(ExceptionCodes.AUTH_GENERAL_ERROR);
        }
    }
}
