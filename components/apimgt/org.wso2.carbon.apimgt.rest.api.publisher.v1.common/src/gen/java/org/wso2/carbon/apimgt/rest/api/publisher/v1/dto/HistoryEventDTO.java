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
    private String operation = null;
    private String description = null;
    private String user = null;
    private String timestamp = null;

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
  public HistoryEventDTO operation(String operation) {
    this.operation = operation;
    return this;
  }

  
  @ApiModelProperty(example = "PUT /apis/2a478f6e-dbd6-4036-9da8-33cf714886e5", value = "")
  @JsonProperty("operation")
  public String getOperation() {
    return operation;
  }
  public void setOperation(String operation) {
    this.operation = operation;
  }

  /**
   **/
  public HistoryEventDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "Update API", value = "")
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
  public HistoryEventDTO timestamp(String timestamp) {
    this.timestamp = timestamp;
    return this;
  }

  
  @ApiModelProperty(example = "2017-02-20T13:57:16.229", value = "")
  @JsonProperty("timestamp")
  public String getTimestamp() {
    return timestamp;
  }
  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
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
        Objects.equals(operation, historyEvent.operation) &&
        Objects.equals(description, historyEvent.description) &&
        Objects.equals(user, historyEvent.user) &&
        Objects.equals(timestamp, historyEvent.timestamp);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, operation, description, user, timestamp);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class HistoryEventDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    operation: ").append(toIndentedString(operation)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    user: ").append(toIndentedString(user)).append("\n");
    sb.append("    timestamp: ").append(toIndentedString(timestamp)).append("\n");
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

