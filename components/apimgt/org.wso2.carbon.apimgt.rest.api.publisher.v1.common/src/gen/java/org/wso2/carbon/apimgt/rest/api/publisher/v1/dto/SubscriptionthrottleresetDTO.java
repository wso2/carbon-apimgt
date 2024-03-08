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



public class SubscriptionthrottleresetDTO   {
  
    private String policylevel = null;
    private String subscriptiontier = null;
    private String apiContext = null;
    private String apiVersion = null;
    private String apiTenant = null;
    private String appId = null;
    private String apiName = null;

  /**
   * the policy Level which the counters should be reset
   **/
  public SubscriptionthrottleresetDTO policylevel(String policylevel) {
    this.policylevel = policylevel;
    return this;
  }

  
  @ApiModelProperty(example = "sub", value = "the policy Level which the counters should be reset")
  @JsonProperty("policylevel")
  public String getPolicylevel() {
    return policylevel;
  }
  public void setPolicylevel(String policylevel) {
    this.policylevel = policylevel;
  }

  /**
   **/
  public SubscriptionthrottleresetDTO subscriptiontier(String subscriptiontier) {
    this.subscriptiontier = subscriptiontier;
    return this;
  }

  
  @ApiModelProperty(example = "Unlimited", value = "")
  @JsonProperty("subscriptiontier")
  public String getSubscriptiontier() {
    return subscriptiontier;
  }
  public void setSubscriptiontier(String subscriptiontier) {
    this.subscriptiontier = subscriptiontier;
  }

  /**
   **/
  public SubscriptionthrottleresetDTO apiContext(String apiContext) {
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
  public SubscriptionthrottleresetDTO apiVersion(String apiVersion) {
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
  public SubscriptionthrottleresetDTO apiTenant(String apiTenant) {
    this.apiTenant = apiTenant;
    return this;
  }

  
  @ApiModelProperty(example = "carbon.super", value = "")
  @JsonProperty("apiTenant")
  public String getApiTenant() {
    return apiTenant;
  }
  public void setApiTenant(String apiTenant) {
    this.apiTenant = apiTenant;
  }

  /**
   **/
  public SubscriptionthrottleresetDTO appId(String appId) {
    this.appId = appId;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "")
  @JsonProperty("appId")
  public String getAppId() {
    return appId;
  }
  public void setAppId(String appId) {
    this.appId = appId;
  }

  /**
   **/
  public SubscriptionthrottleresetDTO apiName(String apiName) {
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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SubscriptionthrottleresetDTO subscriptionthrottlereset = (SubscriptionthrottleresetDTO) o;
    return Objects.equals(policylevel, subscriptionthrottlereset.policylevel) &&
        Objects.equals(subscriptiontier, subscriptionthrottlereset.subscriptiontier) &&
        Objects.equals(apiContext, subscriptionthrottlereset.apiContext) &&
        Objects.equals(apiVersion, subscriptionthrottlereset.apiVersion) &&
        Objects.equals(apiTenant, subscriptionthrottlereset.apiTenant) &&
        Objects.equals(appId, subscriptionthrottlereset.appId) &&
        Objects.equals(apiName, subscriptionthrottlereset.apiName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(policylevel, subscriptiontier, apiContext, apiVersion, apiTenant, appId, apiName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubscriptionthrottleresetDTO {\n");
    
    sb.append("    policylevel: ").append(toIndentedString(policylevel)).append("\n");
    sb.append("    subscriptiontier: ").append(toIndentedString(subscriptiontier)).append("\n");
    sb.append("    apiContext: ").append(toIndentedString(apiContext)).append("\n");
    sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
    sb.append("    apiTenant: ").append(toIndentedString(apiTenant)).append("\n");
    sb.append("    appId: ").append(toIndentedString(appId)).append("\n");
    sb.append("    apiName: ").append(toIndentedString(apiName)).append("\n");
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

