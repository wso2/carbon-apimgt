package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ApplicationDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class SubscriptionDTO  {
  
  
  @NotNull
  private String subscriptionId = null;
  
  @NotNull
  private ApplicationDTO applicationInfo = null;
  
  @NotNull
  private String policy = null;
  
  public enum SubscriptionStatusEnum {
     BLOCKED,  PROD_ONLY_BLOCKED,  UNBLOCKED,  ON_HOLD,  REJECTED, 
  };
  @NotNull
  private SubscriptionStatusEnum subscriptionStatus = null;

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
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
  @JsonProperty("applicationInfo")
  public ApplicationDTO getApplicationInfo() {
    return applicationInfo;
  }
  public void setApplicationInfo(ApplicationDTO applicationInfo) {
    this.applicationInfo = applicationInfo;
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
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("subscriptionStatus")
  public SubscriptionStatusEnum getSubscriptionStatus() {
    return subscriptionStatus;
  }
  public void setSubscriptionStatus(SubscriptionStatusEnum subscriptionStatus) {
    this.subscriptionStatus = subscriptionStatus;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubscriptionDTO {\n");
    
    sb.append("  subscriptionId: ").append(subscriptionId).append("\n");
    sb.append("  applicationInfo: ").append(applicationInfo).append("\n");
    sb.append("  policy: ").append(policy).append("\n");
    sb.append("  subscriptionStatus: ").append(subscriptionStatus).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
