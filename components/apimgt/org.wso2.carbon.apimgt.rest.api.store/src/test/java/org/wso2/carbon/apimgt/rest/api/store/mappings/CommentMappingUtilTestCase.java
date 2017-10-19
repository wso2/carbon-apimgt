/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import org.junit.Assert;
import org.junit.Test;
import org.wso2.carbon.apimgt.core.models.Comment;
import org.wso2.carbon.apimgt.rest.api.store.dto.CommentDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.CommentListDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommentMappingUtilTestCase {

    CommentMappingUtil commentMappingUtil = new CommentMappingUtil();

    @Test
    public void testFromCommentToDTO() {

        String commentUUID = UUID.randomUUID().toString();

        Comment comment = new Comment();
        comment.setUuid(commentUUID);
        comment.setCommentedUser("commentedUser");
        comment.setCommentText("this is a comment");
        comment.setCreatedUser("createdUser");
        comment.setUpdatedUser("updatedUser");
        comment.setCreatedTime(LocalDateTime.now().minusHours(1));
        comment.setUpdatedTime(LocalDateTime.now());

        CommentDTO commentDTO = commentMappingUtil.fromCommentToDTO(comment);

        Assert.assertEquals(commentDTO.getCommentId().toString(), commentUUID);
    }

    @Test
    public void testFromDTOToComment() {

        String apiID = UUID.randomUUID().toString();
        String username = "user";

        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setApiId(apiID);
        commentDTO.setCommentText("comment text");
        commentDTO.setCreatedBy("creater");
        commentDTO.setLastUpdatedBy("updater");

        Comment comment = commentMappingUtil.fromDTOToComment(commentDTO, username);

        Assert.assertEquals(comment.getApiId(), apiID);
        Assert.assertEquals(comment.getCommentText(), "comment text");
    }

    @Test
    public void testFromCommentListToDTO() {

        Comment comment1 = new Comment();
        comment1.setUuid(UUID.randomUUID().toString());
        comment1.setCommentedUser("commentedUser1");
        comment1.setCommentText("this is a comment 1");
        comment1.setCreatedUser("createdUser1");
        comment1.setUpdatedUser("updatedUser1");
        comment1.setCreatedTime(LocalDateTime.now().minusHours(1));
        comment1.setUpdatedTime(LocalDateTime.now());

        Comment comment2 = new Comment();
        comment2.setUuid(UUID.randomUUID().toString());
        comment2.setCommentedUser("commentedUser2");
        comment2.setCommentText("this is a comment 2");
        comment2.setCreatedUser("createdUser2");
        comment2.setUpdatedUser("updatedUser2");
        comment2.setCreatedTime(LocalDateTime.now().minusHours(1));
        comment2.setUpdatedTime(LocalDateTime.now());

        List<Comment> commentList = new ArrayList<>();
        commentList.add(comment1);
        commentList.add(comment2);

        CommentListDTO commentListDTO =
                commentMappingUtil.fromCommentListToDTO(commentList, 10, 0);

        Assert.assertEquals(commentListDTO.getList().get(0).getUsername().toString(), "commentedUser1");

    }
}
