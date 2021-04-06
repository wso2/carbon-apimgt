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
import org.wso2.carbon.apimgt.api.model.Comment;
import org.wso2.carbon.apimgt.api.model.CommentList;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIRealmUtils;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.CommentDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.CommentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.CommenterInfoDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PaginationDTO;

import java.util.ArrayList;
import java.util.HashMap;
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
        commentDTO.setCreatedTime(comment.getCreatedTime().toString());
        if (comment.getUpdatedTime() != null) {
            commentDTO.setUpdatedTime(comment.getUpdatedTime().toString());
        }
        commentDTO.setCategory(comment.getCategory());
        commentDTO.setParentCommentId(comment.getParentCommentID());
        if (APIConstants.CommentEntryPoint.DEVPORTAL.toString().equals(comment.getEntryPoint())) {
            commentDTO.setEntryPoint(CommentDTO.EntryPointEnum.DEVPORTAL);
        } else if (APIConstants.CommentEntryPoint.PUBLISHER.toString().equals(comment.getEntryPoint())) {
            commentDTO.setEntryPoint(CommentDTO.EntryPointEnum.PUBLISHER);
        }
        commentDTO.setReplies(fromCommentListToDTO(comment.getReplies(), false));
        return commentDTO;
    }

    /**
     * Converts a Comment object into corresponding REST API CommentDTO object with User Info
     *
     * @param comment comment object
     * @return CommentDTO
     */
    public static CommentDTO fromCommentToDTOWithUserInfo(Comment comment, Map<String,
            Map<String, String>> userClaimsMap) throws APIManagementException {
        CommentDTO commentDTO = fromCommentToDTO(comment);
        if (userClaimsMap.get(comment.getUser()) != null) {
            Map userClaims = userClaimsMap.get(comment.getUser());
            CommenterInfoDTO commenterInfoDTO = new CommenterInfoDTO();
            commenterInfoDTO.setFullName((String) userClaims.get(APIConstants.FULL_NAME));
            commenterInfoDTO.setFirstName((String) userClaims.get(APIConstants.FIRST_NAME));
            commenterInfoDTO.setLastName((String) userClaims.get(APIConstants.LAST_NAME));
            commentDTO.setCommenterInfo(commenterInfoDTO);
        }
        return commentDTO;
    }

    /**
     * Retrieve userClaims from UserStore and save it in a cache map.
     *
     * @param username      commenter username
     * @param userClaimsMap cache map with user deatils
     * @return Map<String, Map < String, String>>
     */
    public static Map<String, Map<String, String>> retrieveUserClaims(String username, Map<String,
            Map<String, String>> userClaimsMap) throws APIManagementException {
        Map userClaims;
        if (userClaimsMap.get(username) == null) {
            userClaims = APIRealmUtils.getUserClaims(username);
            userClaimsMap.put(username, userClaims);
        }
        return userClaimsMap;
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
     * @return CommentListDTO
     */
    public static CommentListDTO fromCommentListToDTO(CommentList commentList, boolean includeCommenterInfo) {
        CommentListDTO commentListDTO = new CommentListDTO();
        List<CommentDTO> listOfCommentDTOs = new ArrayList<>();
        commentListDTO.setCount(commentList.getCount());

        PaginationDTO paginationDTO = new PaginationDTO();
        paginationDTO.setLimit(commentList.getPagination().getLimit());
        paginationDTO.setOffset(commentList.getPagination().getOffset());
        paginationDTO.setTotal(commentList.getPagination().getTotal());
        paginationDTO.setNext(commentList.getPagination().getNext());
        paginationDTO.setPrevious(commentList.getPagination().getPrevious());
        commentListDTO.setPagination(paginationDTO);

        Map<String, Map<String, String>> userClaimsMap = new HashMap<>();
        for (Comment comment : commentList.getList()) {
            try {
                if (includeCommenterInfo) {
                    userClaimsMap = retrieveUserClaims(comment.getUser(), userClaimsMap);
                    listOfCommentDTOs.add(fromCommentToDTOWithUserInfo(comment, userClaimsMap));
                } else {
                    listOfCommentDTOs.add(fromCommentToDTO(comment));
                }
            } catch (APIManagementException e) {
                log.error("Error while creating comments list", e);
            }
        }
        commentListDTO.setList(listOfCommentDTOs);
        return commentListDTO;
    }
}
