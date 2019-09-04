package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class CommentDTO   {
  
    private String commentId = null;
    private String apiId = null;
    private String username = null;
    private String commentText = null;
    private String createdTime = null;
    private String createdBy = null;
    private String lastUpdatedTime = null;
    private String lastUpdatedBy = null;

  /**
   **/
  public CommentDTO commentId(String commentId) {
    this.commentId = commentId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("commentId")
  @NotNull
  public String getCommentId() {
    return commentId;
  }
  public void setCommentId(String commentId) {
    this.commentId = commentId;
  }

  /**
   **/
  public CommentDTO apiId(String apiId) {
    this.apiId = apiId;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("apiId")
  @NotNull
  public String getApiId() {
    return apiId;
  }
  public void setApiId(String apiId) {
    this.apiId = apiId;
  }

  /**
   * If username is not given user invoking the API will be taken as the username. 
   **/
  public CommentDTO username(String username) {
    this.username = username;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "If username is not given user invoking the API will be taken as the username. ")
  @JsonProperty("username")
  @NotNull
  public String getUsername() {
    return username;
  }
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   **/
  public CommentDTO commentText(String commentText) {
    this.commentText = commentText;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("commentText")
  @NotNull
  public String getCommentText() {
    return commentText;
  }
  public void setCommentText(String commentText) {
    this.commentText = commentText;
  }

  /**
   **/
  public CommentDTO createdTime(String createdTime) {
    this.createdTime = createdTime;
    return this;
  }

  
  @ApiModelProperty(example = "2017-02-20T13:57:16.229+0000", value = "")
  @JsonProperty("createdTime")
  public String getCreatedTime() {
    return createdTime;
  }
  public void setCreatedTime(String createdTime) {
    this.createdTime = createdTime;
  }

  /**
   **/
  public CommentDTO createdBy(String createdBy) {
    this.createdBy = createdBy;
    return this;
  }

  
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
  public CommentDTO lastUpdatedTime(String lastUpdatedTime) {
    this.lastUpdatedTime = lastUpdatedTime;
    return this;
  }

  
  @ApiModelProperty(example = "2017-02-20T13:57:16.229+0000", value = "")
  @JsonProperty("lastUpdatedTime")
  public String getLastUpdatedTime() {
    return lastUpdatedTime;
  }
  public void setLastUpdatedTime(String lastUpdatedTime) {
    this.lastUpdatedTime = lastUpdatedTime;
  }

  /**
   **/
  public CommentDTO lastUpdatedBy(String lastUpdatedBy) {
    this.lastUpdatedBy = lastUpdatedBy;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("lastUpdatedBy")
  public String getLastUpdatedBy() {
    return lastUpdatedBy;
  }
  public void setLastUpdatedBy(String lastUpdatedBy) {
    this.lastUpdatedBy = lastUpdatedBy;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CommentDTO comment = (CommentDTO) o;
    return Objects.equals(commentId, comment.commentId) &&
        Objects.equals(apiId, comment.apiId) &&
        Objects.equals(username, comment.username) &&
        Objects.equals(commentText, comment.commentText) &&
        Objects.equals(createdTime, comment.createdTime) &&
        Objects.equals(createdBy, comment.createdBy) &&
        Objects.equals(lastUpdatedTime, comment.lastUpdatedTime) &&
        Objects.equals(lastUpdatedBy, comment.lastUpdatedBy);
  }

  @Override
  public int hashCode() {
    return Objects.hash(commentId, apiId, username, commentText, createdTime, createdBy, lastUpdatedTime, lastUpdatedBy);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CommentDTO {\n");
    
    sb.append("    commentId: ").append(toIndentedString(commentId)).append("\n");
    sb.append("    apiId: ").append(toIndentedString(apiId)).append("\n");
    sb.append("    username: ").append(toIndentedString(username)).append("\n");
    sb.append("    commentText: ").append(toIndentedString(commentText)).append("\n");
    sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
    sb.append("    createdBy: ").append(toIndentedString(createdBy)).append("\n");
    sb.append("    lastUpdatedTime: ").append(toIndentedString(lastUpdatedTime)).append("\n");
    sb.append("    lastUpdatedBy: ").append(toIndentedString(lastUpdatedBy)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

