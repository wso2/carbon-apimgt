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

import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.Comment;
import org.wso2.carbon.apimgt.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.CommentDTO;

public class CommentMappingUtil {

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
        return commentDTO;

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


}
