package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import io.swagger.annotations.*;
import javax.ws.rs.*;
import com.fasterxml.jackson.annotation.*;
import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ApplicationDTO  {
  
  
  
  private String applicationId = null;
  
  @NotNull
  private String name = null;
  
  
  private String subscriber = null;
  
  @NotNull
  private String throttlingTier = null;
  
  
  private String description = null;
  
  
  private String groupId = null;

  
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
  @ApiModelProperty(required = true, value = "")
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
  @ApiModelProperty(required = true, value = "")
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
  @JsonProperty("groupId")
  public String getGroupId() {
    return groupId;
  }
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationDTO {\n");
    
    sb.append("  applicationId: ").append(applicationId).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  subscriber: ").append(subscriber).append("\n");
    sb.append("  throttlingTier: ").append(throttlingTier).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("  groupId: ").append(groupId).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
