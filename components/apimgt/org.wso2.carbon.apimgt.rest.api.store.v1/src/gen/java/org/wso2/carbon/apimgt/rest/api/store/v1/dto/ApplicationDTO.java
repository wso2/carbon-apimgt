package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationKeysDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationTokenDTO;

import io.swagger.annotations.*;
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
  
  
  private String permission = null;
  
  
  private String description = null;
  
  
  private String lifeCycleStatus = null;
  
  
  private ApplicationTokenDTO token = null;
  
  
  private List<ApplicationKeysDTO> keys = new ArrayList<ApplicationKeysDTO>();

  
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
   * If subscriber is not given user invoking the API will be taken as the subscriber.\n
   **/
  @ApiModelProperty(value = "If subscriber is not given user invoking the API will be taken as the subscriber.\n")
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
  @JsonProperty("permission")
  public String getPermission() {
    return permission;
  }
  public void setPermission(String permission) {
    this.permission = permission;
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
  @JsonProperty("lifeCycleStatus")
  public String getLifeCycleStatus() {
    return lifeCycleStatus;
  }
  public void setLifeCycleStatus(String lifeCycleStatus) {
    this.lifeCycleStatus = lifeCycleStatus;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("token")
  public ApplicationTokenDTO getToken() {
    return token;
  }
  public void setToken(ApplicationTokenDTO token) {
    this.token = token;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("keys")
  public List<ApplicationKeysDTO> getKeys() {
    return keys;
  }
  public void setKeys(List<ApplicationKeysDTO> keys) {
    this.keys = keys;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationDTO {\n");
    
    sb.append("  applicationId: ").append(applicationId).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  subscriber: ").append(subscriber).append("\n");
    sb.append("  throttlingTier: ").append(throttlingTier).append("\n");
    sb.append("  permission: ").append(permission).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("  lifeCycleStatus: ").append(lifeCycleStatus).append("\n");
    sb.append("  token: ").append(token).append("\n");
    sb.append("  keys: ").append(keys).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
