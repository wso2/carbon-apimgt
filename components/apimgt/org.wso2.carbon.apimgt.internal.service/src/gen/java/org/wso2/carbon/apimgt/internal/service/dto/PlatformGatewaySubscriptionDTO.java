package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
import javax.validation.constraints.*;

/**
 * Subscription in the format expected by the API Platform gateway.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Subscription in the format expected by the API Platform gateway.")

public class PlatformGatewaySubscriptionDTO   {
  
    private String id = null;
    private String apiId = null;
    private String applicationId = null;
    private String subscriptionToken = null;
    private String subscriptionPlanId = null;
    private String gatewayId = null;
    private String status = null;
    private OffsetDateTime createdAt = null;
    private OffsetDateTime updatedAt = null;

  /**
   * Subscription UUID.
   **/
  public PlatformGatewaySubscriptionDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(value = "Subscription UUID.")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   * API UUID.
   **/
  public PlatformGatewaySubscriptionDTO apiId(String apiId) {
    this.apiId = apiId;
    return this;
  }

  
  @ApiModelProperty(value = "API UUID.")
  @JsonProperty("apiId")
  public String getApiId() {
    return apiId;
  }
  public void setApiId(String apiId) {
    this.apiId = apiId;
  }

  /**
   * Application UUID (optional).
   **/
  public PlatformGatewaySubscriptionDTO applicationId(String applicationId) {
    this.applicationId = applicationId;
    return this;
  }

  
  @ApiModelProperty(value = "Application UUID (optional).")
  @JsonProperty("applicationId")
  public String getApplicationId() {
    return applicationId;
  }
  public void setApplicationId(String applicationId) {
    this.applicationId = applicationId;
  }

  /**
   * Opaque token for subscription validation (on-prem may be empty).
   **/
  public PlatformGatewaySubscriptionDTO subscriptionToken(String subscriptionToken) {
    this.subscriptionToken = subscriptionToken;
    return this;
  }

  
  @ApiModelProperty(value = "Opaque token for subscription validation (on-prem may be empty).")
  @JsonProperty("subscriptionToken")
  public String getSubscriptionToken() {
    return subscriptionToken;
  }
  public void setSubscriptionToken(String subscriptionToken) {
    this.subscriptionToken = subscriptionToken;
  }

  /**
   * Subscription plan UUID (optional).
   **/
  public PlatformGatewaySubscriptionDTO subscriptionPlanId(String subscriptionPlanId) {
    this.subscriptionPlanId = subscriptionPlanId;
    return this;
  }

  
  @ApiModelProperty(value = "Subscription plan UUID (optional).")
  @JsonProperty("subscriptionPlanId")
  public String getSubscriptionPlanId() {
    return subscriptionPlanId;
  }
  public void setSubscriptionPlanId(String subscriptionPlanId) {
    this.subscriptionPlanId = subscriptionPlanId;
  }

  /**
   * Gateway ID (empty for on-prem).
   **/
  public PlatformGatewaySubscriptionDTO gatewayId(String gatewayId) {
    this.gatewayId = gatewayId;
    return this;
  }

  
  @ApiModelProperty(value = "Gateway ID (empty for on-prem).")
  @JsonProperty("gatewayId")
  public String getGatewayId() {
    return gatewayId;
  }
  public void setGatewayId(String gatewayId) {
    this.gatewayId = gatewayId;
  }

  /**
   * Subscription status (e.g. ACTIVE).
   **/
  public PlatformGatewaySubscriptionDTO status(String status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(value = "Subscription status (e.g. ACTIVE).")
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   **/
  public PlatformGatewaySubscriptionDTO createdAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("createdAt")
  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  /**
   **/
  public PlatformGatewaySubscriptionDTO updatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("updatedAt")
  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }
  public void setUpdatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PlatformGatewaySubscriptionDTO platformGatewaySubscription = (PlatformGatewaySubscriptionDTO) o;
    return Objects.equals(id, platformGatewaySubscription.id) &&
        Objects.equals(apiId, platformGatewaySubscription.apiId) &&
        Objects.equals(applicationId, platformGatewaySubscription.applicationId) &&
        Objects.equals(subscriptionToken, platformGatewaySubscription.subscriptionToken) &&
        Objects.equals(subscriptionPlanId, platformGatewaySubscription.subscriptionPlanId) &&
        Objects.equals(gatewayId, platformGatewaySubscription.gatewayId) &&
        Objects.equals(status, platformGatewaySubscription.status) &&
        Objects.equals(createdAt, platformGatewaySubscription.createdAt) &&
        Objects.equals(updatedAt, platformGatewaySubscription.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, apiId, applicationId, subscriptionToken, subscriptionPlanId, gatewayId, status, createdAt, updatedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PlatformGatewaySubscriptionDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    apiId: ").append(toIndentedString(apiId)).append("\n");
    sb.append("    applicationId: ").append(toIndentedString(applicationId)).append("\n");
    sb.append("    subscriptionToken: ").append(toIndentedString(subscriptionToken)).append("\n");
    sb.append("    subscriptionPlanId: ").append(toIndentedString(subscriptionPlanId)).append("\n");
    sb.append("    gatewayId: ").append(toIndentedString(gatewayId)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
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

