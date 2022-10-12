package org.wso2.apk.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.*;

import java.util.Objects;

import javax.xml.bind.annotation.*;

import javax.validation.Valid;



public class CommentDTO   {
  
    private String id = null;
    private String content = null;
    private String createdTime = null;
    private String createdBy = null;
    private String updatedTime = null;
    private String category = "general";
    private String parentCommentId = null;

    @XmlType(name="EntryPointEnum")
    @XmlEnum(String.class)
    public enum EntryPointEnum {
        DEVPORTAL("devPortal"),
        PUBLISHER("publisher");
        private String value;

        EntryPointEnum (String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static EntryPointEnum fromValue(String v) {
            for (EntryPointEnum b : EntryPointEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private EntryPointEnum entryPoint = null;
    private CommenterInfoDTO commenterInfo = null;
    private CommentListDTO replies = null;

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

  
  @ApiModelProperty(example = "2021-02-11-09:57:25", value = "")
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
  public CommentDTO updatedTime(String updatedTime) {
    this.updatedTime = updatedTime;
    return this;
  }

  
  @ApiModelProperty(example = "2021-02-12-19:57:25", value = "")
  @JsonProperty("updatedTime")
  public String getUpdatedTime() {
    return updatedTime;
  }
  public void setUpdatedTime(String updatedTime) {
    this.updatedTime = updatedTime;
  }

  /**
   **/
  public CommentDTO category(String category) {
    this.category = category;
    return this;
  }

  
  @ApiModelProperty(example = "general", value = "")
  @JsonProperty("category")
  public String getCategory() {
    return category;
  }
  public void setCategory(String category) {
    this.category = category;
  }

  /**
   **/
  public CommentDTO parentCommentId(String parentCommentId) {
    this.parentCommentId = parentCommentId;
    return this;
  }

  
  @ApiModelProperty(example = "6f38aea2-f41e-4ac9-b3f2-a9493d00ba97", value = "")
  @JsonProperty("parentCommentId")
  public String getParentCommentId() {
    return parentCommentId;
  }
  public void setParentCommentId(String parentCommentId) {
    this.parentCommentId = parentCommentId;
  }

  /**
   **/
  public CommentDTO entryPoint(EntryPointEnum entryPoint) {
    this.entryPoint = entryPoint;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("entryPoint")
  public EntryPointEnum getEntryPoint() {
    return entryPoint;
  }
  public void setEntryPoint(EntryPointEnum entryPoint) {
    this.entryPoint = entryPoint;
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

  /**
   **/
  public CommentDTO replies(CommentListDTO replies) {
    this.replies = replies;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("replies")
  public CommentListDTO getReplies() {
    return replies;
  }
  public void setReplies(CommentListDTO replies) {
    this.replies = replies;
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
        Objects.equals(updatedTime, comment.updatedTime) &&
        Objects.equals(category, comment.category) &&
        Objects.equals(parentCommentId, comment.parentCommentId) &&
        Objects.equals(entryPoint, comment.entryPoint) &&
        Objects.equals(commenterInfo, comment.commenterInfo) &&
        Objects.equals(replies, comment.replies);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, content, createdTime, createdBy, updatedTime, category, parentCommentId, entryPoint, commenterInfo, replies);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CommentDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    content: ").append(toIndentedString(content)).append("\n");
    sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
    sb.append("    createdBy: ").append(toIndentedString(createdBy)).append("\n");
    sb.append("    updatedTime: ").append(toIndentedString(updatedTime)).append("\n");
    sb.append("    category: ").append(toIndentedString(category)).append("\n");
    sb.append("    parentCommentId: ").append(toIndentedString(parentCommentId)).append("\n");
    sb.append("    entryPoint: ").append(toIndentedString(entryPoint)).append("\n");
    sb.append("    commenterInfo: ").append(toIndentedString(commenterInfo)).append("\n");
    sb.append("    replies: ").append(toIndentedString(replies)).append("\n");
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

