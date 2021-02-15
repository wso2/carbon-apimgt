package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.CommenterInfoDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class CommentDTO   {
  
    private String id = null;
    private String content = null;
    private String createdTime = null;
    private String createdBy = null;
    private CommenterInfoDTO commenterInfo = null;

  /**
   **/
  public CommentDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "943d3002-000c-42d3-a1b9-d6559f8a4d49", value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public CommentDTO content(String content) {
    this.content = content;
    return this;
  }

  
  @ApiModelProperty(example = "This is a comment", required = true, value = "")
  @JsonProperty("content")
  @NotNull
 @Size(max=512)  public String getContent() {
    return content;
  }
  public void setContent(String content) {
    this.content = content;
  }

  /**
   **/
  public CommentDTO createdTime(String createdTime) {
    this.createdTime = createdTime;
    return this;
  }

  
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
  public CommentDTO createdBy(String createdBy) {
    this.createdBy = createdBy;
    return this;
  }

  
  @ApiModelProperty(example = "admin", value = "")
  @JsonProperty("createdBy")
  public String getCreatedBy() {
    return createdBy;
  }
  public void setCreatedBy(String createdBy) {
    this.createdBy = createdBy;
  }

  /**
   **/
  public CommentDTO commenterInfo(CommenterInfoDTO commenterInfo) {
    this.commenterInfo = commenterInfo;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("commenterInfo")
  public CommenterInfoDTO getCommenterInfo() {
    return commenterInfo;
  }
  public void setCommenterInfo(CommenterInfoDTO commenterInfo) {
    this.commenterInfo = commenterInfo;
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
    return Objects.equals(id, comment.id) &&
        Objects.equals(content, comment.content) &&
        Objects.equals(createdTime, comment.createdTime) &&
        Objects.equals(createdBy, comment.createdBy) &&
        Objects.equals(commenterInfo, comment.commenterInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, content, createdTime, createdBy, commenterInfo);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CommentDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    content: ").append(toIndentedString(content)).append("\n");
    sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
    sb.append("    createdBy: ").append(toIndentedString(createdBy)).append("\n");
    sb.append("    commenterInfo: ").append(toIndentedString(commenterInfo)).append("\n");
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

