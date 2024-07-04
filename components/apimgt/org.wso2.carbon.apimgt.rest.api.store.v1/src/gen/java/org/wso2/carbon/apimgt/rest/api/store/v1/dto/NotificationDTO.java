package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

/**
 * Object with basic notification details.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Object with basic notification details.")

public class NotificationDTO   {
  
    private String id = null;
    private String type = null;
    private String content = null;
    private String createdTime = null;
    private Boolean isRead = null;

  /**
   **/
  public NotificationDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "863aef13-dab4-48b4-bf58-7363cd29601a", value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public NotificationDTO type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(example = "APPLICATION_CREATION", value = "")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   **/
  public NotificationDTO content(String content) {
    this.content = content;
    return this;
  }

  
  @ApiModelProperty(example = "Application creation is rejected due to some reason.", value = "")
  @JsonProperty("content")
 @Size(max=512)  public String getContent() {
    return content;
  }
  public void setContent(String content) {
    this.content = content;
  }

  /**
   **/
  public NotificationDTO createdTime(String createdTime) {
    this.createdTime = createdTime;
    return this;
  }

  
  @ApiModelProperty(example = "2021-02-11 09:57:25", value = "")
  @JsonProperty("createdTime")
  public String getCreatedTime() {
    return createdTime;
  }
  public void setCreatedTime(String createdTime) {
    this.createdTime = createdTime;
  }

  /**
   **/
  public NotificationDTO isRead(Boolean isRead) {
    this.isRead = isRead;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "")
  @JsonProperty("isRead")
  public Boolean isIsRead() {
    return isRead;
  }
  public void setIsRead(Boolean isRead) {
    this.isRead = isRead;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NotificationDTO notification = (NotificationDTO) o;
    return Objects.equals(id, notification.id) &&
        Objects.equals(type, notification.type) &&
        Objects.equals(content, notification.content) &&
        Objects.equals(createdTime, notification.createdTime) &&
        Objects.equals(isRead, notification.isRead);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, type, content, createdTime, isRead);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class NotificationDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    content: ").append(toIndentedString(content)).append("\n");
    sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
    sb.append("    isRead: ").append(toIndentedString(isRead)).append("\n");
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

