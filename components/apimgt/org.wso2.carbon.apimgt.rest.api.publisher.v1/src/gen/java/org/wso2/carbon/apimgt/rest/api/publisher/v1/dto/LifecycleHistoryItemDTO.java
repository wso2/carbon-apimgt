package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class LifecycleHistoryItemDTO  {
  
  
  
  private String previousState = null;
  
  
  private String postState = null;
  
  
  private String user = null;
  
  
  private String updatedTime = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("previousState")
  public String getPreviousState() {
    return previousState;
  }
  public void setPreviousState(String previousState) {
    this.previousState = previousState;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("postState")
  public String getPostState() {
    return postState;
  }
  public void setPostState(String postState) {
    this.postState = postState;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("user")
  public String getUser() {
    return user;
  }
  public void setUser(String user) {
    this.user = user;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("updatedTime")
  public String getUpdatedTime() {
    return updatedTime;
  }
  public void setUpdatedTime(String updatedTime) {
    this.updatedTime = updatedTime;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class LifecycleHistoryItemDTO {\n");
    
    sb.append("  previousState: ").append(previousState).append("\n");
    sb.append("  postState: ").append(postState).append("\n");
    sb.append("  user: ").append(user).append("\n");
    sb.append("  updatedTime: ").append(updatedTime).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
