package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class HistoryEventDTO   {
  
    private String id = null;
    private String operationId = null;
    private String description = null;
    private String user = null;
    private java.util.Date createdTime = null;
    private String revisionKey = null;

  /**
   **/
  public HistoryEventDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "3333ce7e1-8233-46a5-9295-525dca347f33", value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public HistoryEventDTO operationId(String operationId) {
    this.operationId = operationId;
    return this;
  }

  
  @ApiModelProperty(example = "updateAPI", value = "")
  @JsonProperty("operationId")
  public String getOperationId() {
    return operationId;
  }
  public void setOperationId(String operationId) {
    this.operationId = operationId;
  }

  /**
   **/
  public HistoryEventDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "PUT /apis/2a478f6e-dbd6-4036-9da8-33cf714886e5", value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   **/
  public HistoryEventDTO user(String user) {
    this.user = user;
    return this;
  }

  
  @ApiModelProperty(example = "user1", value = "")
  @JsonProperty("user")
  public String getUser() {
    return user;
  }
  public void setUser(String user) {
    this.user = user;
  }

  /**
   **/
  public HistoryEventDTO createdTime(java.util.Date createdTime) {
    this.createdTime = createdTime;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("createdTime")
  public java.util.Date getCreatedTime() {
    return createdTime;
  }
  public void setCreatedTime(java.util.Date createdTime) {
    this.createdTime = createdTime;
  }

  /**
   **/
  public HistoryEventDTO revisionKey(String revisionKey) {
    this.revisionKey = revisionKey;
    return this;
  }

  
  @ApiModelProperty(example = "REVISION 1", value = "")
  @JsonProperty("revisionKey")
  public String getRevisionKey() {
    return revisionKey;
  }
  public void setRevisionKey(String revisionKey) {
    this.revisionKey = revisionKey;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    HistoryEventDTO historyEvent = (HistoryEventDTO) o;
    return Objects.equals(id, historyEvent.id) &&
        Objects.equals(operationId, historyEvent.operationId) &&
        Objects.equals(description, historyEvent.description) &&
        Objects.equals(user, historyEvent.user) &&
        Objects.equals(createdTime, historyEvent.createdTime) &&
        Objects.equals(revisionKey, historyEvent.revisionKey);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, operationId, description, user, createdTime, revisionKey);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class HistoryEventDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    operationId: ").append(toIndentedString(operationId)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    user: ").append(toIndentedString(user)).append("\n");
    sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
    sb.append("    revisionKey: ").append(toIndentedString(revisionKey)).append("\n");
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

