/*
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.wso2.carbon.apimgt.rest.api.store.mappings;

import org.wso2.carbon.apimgt.core.models.Comment;
import org.wso2.carbon.apimgt.rest.api.store.dto.CommentDTO;

/**
 * Mapping class for Comment Object and Comment List object to DTO and vise-versa
 *
 */
public class CommentMappingUtil {

    /** Converts an ArtifactResource object into corresponding REST API Comment DTO object
     *
     * @param comment Comment object
     * @return a new Comment object corresponding to given ArtifactResource object
     */
    public static CommentDTO fromCommentToDTO(Comment comment) {

        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setCommentId(comment.getUuid());
        commentDTO.setApiId(comment.getApiId());
        commentDTO.setSubscriberName(comment.getCommentedUser());
        commentDTO.setCommentText(comment.getCommentText());
        commentDTO.setCreatedTime(comment.getCreatedTime().toString());
        commentDTO.setCreatedBy(comment.getCreatedUser());
        commentDTO.setLastUpdatedTime(comment.getUpdatedTime().toString());
        commentDTO.setLastUpdatedBy(comment.getUpdatedUser());
        
        return commentDTO;
    }



}
