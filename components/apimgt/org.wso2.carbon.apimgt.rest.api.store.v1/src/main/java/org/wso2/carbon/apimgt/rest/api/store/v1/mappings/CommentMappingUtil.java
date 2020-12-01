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

package org.wso2.carbon.apimgt.rest.api.store.v1.mappings;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.Comment;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.CommentDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.CommentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.FullNameDTO;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CommentMappingUtil {

    private static final Logger log = LoggerFactory.getLogger(CommentMappingUtil.class);

    /**
     * Converts a Comment object into corresponding REST API CommentDTO object
     *
     * @param comment comment object
     * @return CommentDTO
     */
    public static CommentDTO fromCommentToDTO(Comment comment) throws APIManagementException {

        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setId(comment.getId());
        commentDTO.setContent(comment.getText());
        commentDTO.setCreatedBy(comment.getUser());
        commentDTO.setCreatedTime(comment.getCreatedTime());
        return commentDTO;
    }

    /**
     * Converts a Comment object into corresponding REST API CommentDTO object with User Info
     *
     * @param comment comment object
     * @return CommentDTO
     */
    public static CommentDTO fromCommentToDTOWithUserInfo(Comment comment) throws APIManagementException {

        CommentDTO commentDTO = fromCommentToDTO(comment);
        String username = RestApiUtil.getLoggedInUsername();
        APIProvider apiProvider = RestApiUtil.getProvider(username);
        Map userClaims = apiProvider.getLoggedInUserClaims(comment.getUser());
        FullNameDTO fullNameDTO = new FullNameDTO();
        fullNameDTO.setFullName((String) userClaims.get("http://wso2.org/claims/userprincipal"));
        commentDTO.setCommenterInformation(fullNameDTO);
        return  commentDTO;
    }

    /**
     * Converts a CommentDTO to a Comment object
     *
     * @param body     commentDTO body
     * @param username username of the consumer
     * @param apiId    API ID
     * @return Comment object
     */
    public static Comment fromDTOToComment(CommentDTO body, String username, String apiId) {
        Comment comment = new Comment();
        comment.setText(body.getContent());
        comment.setUser(username);
        comment.setApiId(apiId);
        return comment;
    }

    /**
     * Wraps a List of Comments to a CommentListDTO
     *
     * @param commentList list of comments
     * @param limit       maximum comments to return
     * @param offset      starting position of the pagination
     * @return CommentListDTO
     */
    public static CommentListDTO fromCommentListToDTO(Comment[] commentList, int limit, int offset, boolean commenterInfo) {
        CommentListDTO commentListDTO = new CommentListDTO();
        List<CommentDTO> listOfCommentDTOs = new ArrayList<>();
        commentListDTO.setCount(commentList.length);

        int start = offset < commentList.length && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = offset + limit - 1 <= commentList.length - 1 ? offset + limit - 1 : commentList.length - 1;

        for (int i = start; i <= end; i++) {
            try {
                if (commenterInfo) {
                    listOfCommentDTOs.add(fromCommentToDTOWithUserInfo(commentList[i]));
                } else {
                    listOfCommentDTOs.add(fromCommentToDTO(commentList[i]));
                }
            } catch (APIManagementException e) {
                log.error("Error while creating comments list", e);
            }
        }
        commentListDTO.setList(listOfCommentDTOs);
        return commentListDTO;
    }

}
