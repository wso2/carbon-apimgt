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



public class SubscriptionThrottleResetDTO   {
  
    private String policyLevel = null;
    private String apiName = null;
    private String apiContext = null;
    private String apiVersion = null;
    private String applicationId = null;
    private String subscriptionTier = null;

  /**
   * the policy Level which the counters should be reset
   **/
  public SubscriptionThrottleResetDTO policyLevel(String policyLevel) {
    this.policyLevel = policyLevel;
    return this;
  }

  
  @ApiModelProperty(example = "sub", value = "the policy Level which the counters should be reset")
  @JsonProperty("policyLevel")
  public String getPolicyLevel() {
    return policyLevel;
  }
  public void setPolicyLevel(String policyLevel) {
    this.policyLevel = policyLevel;
  }

  /**
   **/
  public SubscriptionThrottleResetDTO apiName(String apiName) {
    this.apiName = apiName;
    return this;
  }

  
  @ApiModelProperty(example = "pizzashack", value = "")
  @JsonProperty("apiName")
  public String getApiName() {
    return apiName;
  }
  public void setApiName(String apiName) {
    this.apiName = apiName;
  }

  /**
   **/
  public SubscriptionThrottleResetDTO apiContext(String apiContext) {
    this.apiContext = apiContext;
    return this;
  }

  
  @ApiModelProperty(example = "/pizzashack/1.0.0", value = "")
  @JsonProperty("apiContext")
  public String getApiContext() {
    return apiContext;
  }
  public void setApiContext(String apiContext) {
    this.apiContext = apiContext;
  }

  /**
   **/
  public SubscriptionThrottleResetDTO apiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
    return this;
  }

  
  @ApiModelProperty(example = "1.0.0", value = "")
  @JsonProperty("apiVersion")
  public String getApiVersion() {
    return apiVersion;
  }
  public void setApiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
  }

  /**
   **/
  public SubscriptionThrottleResetDTO applicationId(String applicationId) {
    this.applicationId = applicationId;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "")
  @JsonProperty("applicationId")
  public String getApplicationId() {
    return applicationId;
  }
  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  /**
   **/
  public SubscriptionThrottleResetDTO subscriptionTier(String subscriptionTier) {
    this.subscriptionTier = subscriptionTier;
    return this;
  }

  
  @ApiModelProperty(example = "Unlimited", value = "")
  @JsonProperty("subscriptionTier")
  public String getSubscriptionTier() {
    return subscriptionTier;
  }
  public void setSubscriptionTier(String subscriptionTier) {
    this.subscriptionTier = subscriptionTier;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SubscriptionThrottleResetDTO subscriptionThrottleReset = (SubscriptionThrottleResetDTO) o;
    return Objects.equals(policyLevel, subscriptionThrottleReset.policyLevel) &&
        Objects.equals(apiName, subscriptionThrottleReset.apiName) &&
        Objects.equals(apiContext, subscriptionThrottleReset.apiContext) &&
        Objects.equals(apiVersion, subscriptionThrottleReset.apiVersion) &&
        Objects.equals(applicationId, subscriptionThrottleReset.applicationId) &&
        Objects.equals(subscriptionTier, subscriptionThrottleReset.subscriptionTier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(policyLevel, apiName, apiContext, apiVersion, applicationId, subscriptionTier);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubscriptionThrottleResetDTO {\n");
    
    sb.append("    policyLevel: ").append(toIndentedString(policyLevel)).append("\n");
    sb.append("    apiName: ").append(toIndentedString(apiName)).append("\n");
    sb.append("    apiContext: ").append(toIndentedString(apiContext)).append("\n");
    sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
    sb.append("    applicationId: ").append(toIndentedString(applicationId)).append("\n");
    sb.append("    subscriptionTier: ").append(toIndentedString(subscriptionTier)).append("\n");
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

