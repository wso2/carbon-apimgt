package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class SubscriptionDTO   {
  
    private String subscriptionUUID = null;
    private Integer subscriptionId = null;
    private String policyId = null;
    private String apiUUID = null;
    private Integer apiId = null;
    private String applicationUUID = null;
    private Integer appId = null;
    private String subscriptionState = null;

  /**
   **/
  public SubscriptionDTO subscriptionUUID(String subscriptionUUID) {
    this.subscriptionUUID = subscriptionUUID;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("subscriptionUUID")
  public String getSubscriptionUUID() {
    return subscriptionUUID;
  }
  public void setSubscriptionUUID(String subscriptionUUID) {
    this.subscriptionUUID = subscriptionUUID;
  }

  /**
   **/
  public SubscriptionDTO subscriptionId(Integer subscriptionId) {
    this.subscriptionId = subscriptionId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("subscriptionId")
  public Integer getSubscriptionId() {
    return subscriptionId;
  }
  public void setSubscriptionId(Integer subscriptionId) {
    this.subscriptionId = subscriptionId;
  }

  /**
   **/
  public SubscriptionDTO policyId(String policyId) {
    this.policyId = policyId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("policyId")
  public String getPolicyId() {
    return policyId;
  }
  public void setPolicyId(String policyId) {
    this.policyId = policyId;
  }

  /**
   **/
  public SubscriptionDTO apiUUID(String apiUUID) {
    this.apiUUID = apiUUID;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("apiUUID")
  public String getApiUUID() {
    return apiUUID;
  }
  public void setApiUUID(String apiUUID) {
    this.apiUUID = apiUUID;
  }

  /**
   **/
  public SubscriptionDTO apiId(Integer apiId) {
    this.apiId = apiId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("apiId")
  public Integer getApiId() {
    return apiId;
  }
  public void setApiId(Integer apiId) {
    this.apiId = apiId;
  }

  /**
   **/
  public SubscriptionDTO applicationUUID(String applicationUUID) {
    this.applicationUUID = applicationUUID;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("applicationUUID")
  public String getApplicationUUID() {
    return applicationUUID;
  }
  public void setApplicationUUID(String applicationUUID) {
    this.applicationUUID = applicationUUID;
  }

  /**
   **/
  public SubscriptionDTO appId(Integer appId) {
    this.appId = appId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("appId")
  public Integer getAppId() {
    return appId;
  }
  public void setAppId(Integer appId) {
    this.appId = appId;
  }

  /**
   **/
  public SubscriptionDTO subscriptionState(String subscriptionState) {
    this.subscriptionState = subscriptionState;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("subscriptionState")
  public String getSubscriptionState() {
    return subscriptionState;
  }
  public void setSubscriptionState(String subscriptionState) {
    this.subscriptionState = subscriptionState;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SubscriptionDTO subscription = (SubscriptionDTO) o;
    return Objects.equals(subscriptionUUID, subscription.subscriptionUUID) &&
        Objects.equals(subscriptionId, subscription.subscriptionId) &&
        Objects.equals(policyId, subscription.policyId) &&
        Objects.equals(apiUUID, subscription.apiUUID) &&
        Objects.equals(apiId, subscription.apiId) &&
        Objects.equals(applicationUUID, subscription.applicationUUID) &&
        Objects.equals(appId, subscription.appId) &&
        Objects.equals(subscriptionState, subscription.subscriptionState);
  }

  @Override
  public int hashCode() {
    return Objects.hash(subscriptionUUID, subscriptionId, policyId, apiUUID, apiId, applicationUUID, appId, subscriptionState);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubscriptionDTO {\n");
    
    sb.append("    subscriptionUUID: ").append(toIndentedString(subscriptionUUID)).append("\n");
    sb.append("    subscriptionId: ").append(toIndentedString(subscriptionId)).append("\n");
    sb.append("    policyId: ").append(toIndentedString(policyId)).append("\n");
    sb.append("    apiUUID: ").append(toIndentedString(apiUUID)).append("\n");
    sb.append("    apiId: ").append(toIndentedString(apiId)).append("\n");
    sb.append("    applicationUUID: ").append(toIndentedString(applicationUUID)).append("\n");
    sb.append("    appId: ").append(toIndentedString(appId)).append("\n");
    sb.append("    subscriptionState: ").append(toIndentedString(subscriptionState)).append("\n");
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

