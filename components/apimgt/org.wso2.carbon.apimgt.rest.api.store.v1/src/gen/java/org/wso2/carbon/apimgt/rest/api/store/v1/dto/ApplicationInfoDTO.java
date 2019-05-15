package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ApplicationInfoDTO  {
  
  
  
  private String applicationId = null;
  
  
  private String name = null;
  
  
  private String subscriber = null;
  
  
  private String throttlingTier = null;
  
  
  private String description = null;
  
  
  private String status = "";
  
  
  private List<String> groups = new ArrayList<String>();
  
  
  private Integer subscriptionCount = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("applicationId")
  public String getApplicationId() {
    return applicationId;
  }
  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("subscriber")
  public String getSubscriber() {
    return subscriber;
  }
  public void setSubscriber(String subscriber) {
    this.subscriber = subscriber;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("throttlingTier")
  public String getThrottlingTier() {
    return throttlingTier;
  }
  public void setThrottlingTier(String throttlingTier) {
    this.throttlingTier = throttlingTier;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("groups")
  public List<String> getGroups() {
    return groups;
  }
  public void setGroups(List<String> groups) {
    this.groups = groups;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("subscriptionCount")
  public Integer getSubscriptionCount() {
    return subscriptionCount;
  }
  public void setSubscriptionCount(Integer subscriptionCount) {
    this.subscriptionCount = subscriptionCount;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationInfoDTO {\n");
    
    sb.append("  applicationId: ").append(applicationId).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  subscriber: ").append(subscriber).append("\n");
    sb.append("  throttlingTier: ").append(throttlingTier).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("  status: ").append(status).append("\n");
    sb.append("  groups: ").append(groups).append("\n");
    sb.append("  subscriptionCount: ").append(subscriptionCount).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
