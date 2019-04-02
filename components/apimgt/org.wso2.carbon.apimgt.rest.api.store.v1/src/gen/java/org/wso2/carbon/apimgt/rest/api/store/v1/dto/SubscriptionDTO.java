package org.wso2.carbon.apimgt.rest.api.store.v1.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class SubscriptionDTO  {
  
  
  
  private String subscriptionId = null;
  
  @NotNull
  private String applicationId = null;
  
  @NotNull
  private String apiIdentifier = null;
  
  @NotNull
  private String apiName = null;
  
  @NotNull
  private String apiVersion = null;
  
  @NotNull
  private String policy = null;
  
  public enum LifeCycleStatusEnum {
     BLOCKED,  PROD_ONLY_BLOCKED,  ACTIVE,  ON_HOLD,  REJECTED, 
  };
  
  private LifeCycleStatusEnum lifeCycleStatus = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("subscriptionId")
  public String getSubscriptionId() {
    return subscriptionId;
  }
  public void setSubscriptionId(String subscriptionId) {
    this.subscriptionId = subscriptionId;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
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
  @JsonProperty("apiIdentifier")
  public String getApiIdentifier() {
    return apiIdentifier;
  }
  public void setApiIdentifier(String apiIdentifier) {
    this.apiIdentifier = apiIdentifier;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("apiName")
  public String getApiName() {
    return apiName;
  }
  public void setApiName(String apiName) {
    this.apiName = apiName;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("apiVersion")
  public String getApiVersion() {
    return apiVersion;
  }
  public void setApiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("policy")
  public String getPolicy() {
    return policy;
  }
  public void setPolicy(String policy) {
    this.policy = policy;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("lifeCycleStatus")
  public LifeCycleStatusEnum getLifeCycleStatus() {
    return lifeCycleStatus;
  }
  public void setLifeCycleStatus(LifeCycleStatusEnum lifeCycleStatus) {
    this.lifeCycleStatus = lifeCycleStatus;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubscriptionDTO {\n");
    
    sb.append("  subscriptionId: ").append(subscriptionId).append("\n");
    sb.append("  applicationId: ").append(applicationId).append("\n");
    sb.append("  apiIdentifier: ").append(apiIdentifier).append("\n");
    sb.append("  apiName: ").append(apiName).append("\n");
    sb.append("  apiVersion: ").append(apiVersion).append("\n");
    sb.append("  policy: ").append(policy).append("\n");
    sb.append("  lifeCycleStatus: ").append(lifeCycleStatus).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
