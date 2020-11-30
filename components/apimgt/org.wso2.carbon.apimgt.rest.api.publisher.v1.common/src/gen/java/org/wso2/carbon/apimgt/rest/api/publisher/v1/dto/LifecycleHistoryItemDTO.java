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



public class LifecycleHistoryItemDTO   {
  
    private String previousState = null;
    private String postState = null;
    private String user = null;
    private String updatedTime = null;

  /**
   **/
  public LifecycleHistoryItemDTO previousState(String previousState) {
    this.previousState = previousState;
    return this;
  }

  
  @ApiModelProperty(example = "Created", value = "")
  @JsonProperty("previousState")
  public String getPreviousState() {
    return previousState;
  }
  public void setPreviousState(String previousState) {
    this.previousState = previousState;
  }

  /**
   **/
  public LifecycleHistoryItemDTO postState(String postState) {
    this.postState = postState;
    return this;
  }

  
  @ApiModelProperty(example = "Published", value = "")
  @JsonProperty("postState")
  public String getPostState() {
    return postState;
  }
  public void setPostState(String postState) {
    this.postState = postState;
  }

  /**
   **/
  public LifecycleHistoryItemDTO user(String user) {
    this.user = user;
    return this;
  }

  
  @ApiModelProperty(example = "admin", value = "")
  @JsonProperty("user")
  public String getUser() {
    return user;
  }
  public void setUser(String user) {
    this.user = user;
  }

  /**
   **/
  public LifecycleHistoryItemDTO updatedTime(String updatedTime) {
    this.updatedTime = updatedTime;
    return this;
  }

  
  @ApiModelProperty(example = "2019-02-31T23:59:60Z", value = "")
  @JsonProperty("updatedTime")
  public String getUpdatedTime() {
    return updatedTime;
  }
  public void setUpdatedTime(String updatedTime) {
    this.updatedTime = updatedTime;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LifecycleHistoryItemDTO lifecycleHistoryItem = (LifecycleHistoryItemDTO) o;
    return Objects.equals(previousState, lifecycleHistoryItem.previousState) &&
        Objects.equals(postState, lifecycleHistoryItem.postState) &&
        Objects.equals(user, lifecycleHistoryItem.user) &&
        Objects.equals(updatedTime, lifecycleHistoryItem.updatedTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(previousState, postState, user, updatedTime);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LifecycleHistoryItemDTO {\n");
    
    sb.append("    previousState: ").append(toIndentedString(previousState)).append("\n");
    sb.append("    postState: ").append(toIndentedString(postState)).append("\n");
    sb.append("    user: ").append(toIndentedString(user)).append("\n");
    sb.append("    updatedTime: ").append(toIndentedString(updatedTime)).append("\n");
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

