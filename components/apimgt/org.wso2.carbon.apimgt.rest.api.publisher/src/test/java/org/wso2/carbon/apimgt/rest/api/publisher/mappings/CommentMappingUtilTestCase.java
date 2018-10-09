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
package org.wso2.carbon.apimgt.rest.api.publisher.mappings;


import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.testng.Assert;
import org.wso2.carbon.apimgt.core.api.UserNameMapper;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.models.Comment;
import org.wso2.carbon.apimgt.core.util.APIUtils;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.CommentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.CommentListDTO;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class CommentMappingUtilTestCase {
    UserNameMapper userNameMapper;

    @Before
    public void setup() throws NoSuchFieldException, IllegalAccessException {
        APIManagerFactory apiManagerFactory = APIManagerFactory.getInstance();
        userNameMapper = Mockito.mock(UserNameMapper.class);
        Field field = APIManagerFactory.class.getDeclaredField("userNameMapper");
        field.setAccessible(true);
        field.set(apiManagerFactory, userNameMapper);
    }

    @Test
    public void testFromCommentToDTO() throws APIManagementException {

        String commentUUID = UUID.randomUUID().toString();
        Instant time = APIUtils.getCurrentUTCTime();

        Comment comment = new Comment();
        comment.setUuid(commentUUID);
        comment.setCommentedUser("commentedUser");
        comment.setCommentText("this is a comment");
        comment.setCategory("testingCategory");
        comment.setParentCommentId("");
        comment.setEntryPoint("APIPublisher");
        comment.setCreatedUser("createdUser");
        comment.setUpdatedUser("updatedUser");
        comment.setCreatedTime(time);
        comment.setUpdatedTime(time);

        CommentDTO commentDTO = CommentMappingUtil.fromCommentToDTO(comment);

        Assert.assertEquals(commentDTO.getCommentId().toString(), commentUUID);
    }

    @Test
    public void testFromDTOToComment() {

        String apiID = UUID.randomUUID().toString();
        String username = "user";

        CommentDTO commentDTO = new CommentDTO();
        commentDTO.setApiId(apiID);
        commentDTO.setCommentText("comment text");
        commentDTO.setCategory("testingCategory");
        commentDTO.setParentCommentId("");
        commentDTO.setEntryPoint("APIPublisher");
        commentDTO.setCreatedBy("creater");
        commentDTO.setLastUpdatedBy("updater");

        Comment comment = CommentMappingUtil.fromDTOToComment(commentDTO, username);

        Assert.assertEquals(comment.getApiId(), apiID);
        Assert.assertEquals(comment.getCommentText(), "comment text");
    }

    @Test
    public void testFromCommentListToDTO() throws APIManagementException {
        Mockito.when(userNameMapper.getLoggedInUserIDFromPseudoName(Mockito.anyString())).thenReturn(Mockito.anyString());
        Instant time = APIUtils.getCurrentUTCTime();
        Comment comment1 = new Comment();
        comment1.setUuid(UUID.randomUUID().toString());
        comment1.setCommentedUser("commentedUser1");
        comment1.setCommentText("this is a comment 1");
        comment1.setCategory("testingCategory1");
        comment1.setParentCommentId("");
        comment1.setEntryPoint("APIPublisher");
        comment1.setCreatedUser("createdUser1");
        comment1.setUpdatedUser("updatedUser1");
        comment1.setCreatedTime(time);
        comment1.setUpdatedTime(time);

        time = APIUtils.getCurrentUTCTime();
        Comment comment2 = new Comment();
        comment2.setUuid(UUID.randomUUID().toString());
        comment2.setCommentedUser("commentedUser2");
        comment2.setCommentText("this is a comment 2");
        comment2.setCategory("testingCategory1");
        comment2.setParentCommentId("");
        comment2.setEntryPoint("APIPublisher");
        comment2.setCreatedUser("createdUser2");
        comment2.setUpdatedUser("updatedUser2");
        comment2.setCreatedTime(time);
        comment2.setUpdatedTime(time);

        List<Comment> commentList = new ArrayList<>();
        commentList.add(comment1);
        commentList.add(comment2);

        CommentListDTO commentListDTO =
                CommentMappingUtil.fromCommentListToDTO(commentList, 10, 0);

        Assert.assertNotEquals(commentListDTO.getList().get(0).getUsername().toString(), "commentedUser1");

    }
}
