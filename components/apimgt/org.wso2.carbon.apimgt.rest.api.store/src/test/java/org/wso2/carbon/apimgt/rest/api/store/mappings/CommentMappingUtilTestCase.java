package org.wso2.carbon.apimgt.rest.api.store.mappings;

import org.testng.Assert;
import org.testng.annotations.Test;
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
