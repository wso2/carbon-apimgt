package org.wso2.carbon.apimgt.rest.api.store.v1.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class CommentDTO  {
  
  
  @NotNull
  private String commentId = null;
  
  @NotNull
  private String apiId = null;
  
  @NotNull
  private String username = null;
  
  @NotNull
  private String commentText = null;
  
  
  private String createdTime = null;
  
  
  private String createdBy = null;
  
  
  private String lastUpdatedTime = null;
  
  
  private String lastUpdatedBy = null;

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("commentId")
  public String getCommentId() {
    return commentId;
  }
  public void setCommentId(String commentId) {
    this.commentId = commentId;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("apiId")
  public String getApiId() {
    return apiId;
  }
  public void setApiId(String apiId) {
    this.apiId = apiId;
  }

  
  /**
   * If username is not given user invoking the API will be taken as the username.\n
   **/
  @ApiModelProperty(required = true, value = "If username is not given user invoking the API will be taken as the username.\n")
  @JsonProperty("username")
  public String getUsername() {
    return username;
  }
  public void setUsername(String username) {
    this.username = username;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("commentText")
  public String getCommentText() {
    return commentText;
  }
  public void setCommentText(String commentText) {
    this.commentText = commentText;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("createdTime")
  public String getCreatedTime() {
    return createdTime;
  }
  public void setCreatedTime(String createdTime) {
    this.createdTime = createdTime;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("createdBy")
  public String getCreatedBy() {
    return createdBy;
  }
  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("lastUpdatedTime")
  public String getLastUpdatedTime() {
    return lastUpdatedTime;
  }
  public void setLastUpdatedTime(String lastUpdatedTime) {
    this.lastUpdatedTime = lastUpdatedTime;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("lastUpdatedBy")
  public String getLastUpdatedBy() {
    return lastUpdatedBy;
  }
  public void setLastUpdatedBy(String lastUpdatedBy) {
    this.lastUpdatedBy = lastUpdatedBy;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class CommentDTO {\n");
    
    sb.append("  commentId: ").append(commentId).append("\n");
    sb.append("  apiId: ").append(apiId).append("\n");
    sb.append("  username: ").append(username).append("\n");
    sb.append("  commentText: ").append(commentText).append("\n");
    sb.append("  createdTime: ").append(createdTime).append("\n");
    sb.append("  createdBy: ").append(createdBy).append("\n");
    sb.append("  lastUpdatedTime: ").append(lastUpdatedTime).append("\n");
    sb.append("  lastUpdatedBy: ").append(lastUpdatedBy).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
