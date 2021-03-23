package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.CustomAttributeDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.MonetizationInfoDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.SubscriptionThrottlePolicyPermissionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ThrottleLimitDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class SubscriptionThrottlePolicyAllOfDTO   {
  
    private ThrottleLimitDTO defaultLimit = null;
    private MonetizationInfoDTO monetization = null;
    private Integer rateLimitCount = null;
    private String rateLimitTimeUnit = null;
    private Integer subscriberCount = null;
    private List<CustomAttributeDTO> customAttributes = new ArrayList<CustomAttributeDTO>();
    private Boolean stopOnQuotaReach = false;
    private String billingPlan = null;
    private SubscriptionThrottlePolicyPermissionDTO permissions = null;

  /**
   **/
  public SubscriptionThrottlePolicyAllOfDTO defaultLimit(ThrottleLimitDTO defaultLimit) {
    this.defaultLimit = defaultLimit;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
      @Valid
  @JsonProperty("defaultLimit")
  @NotNull
  public ThrottleLimitDTO getDefaultLimit() {
    return defaultLimit;
  }
  public void setDefaultLimit(ThrottleLimitDTO defaultLimit) {
    this.defaultLimit = defaultLimit;
  }

  /**
   **/
  public SubscriptionThrottlePolicyAllOfDTO monetization(MonetizationInfoDTO monetization) {
    this.monetization = monetization;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("monetization")
  public MonetizationInfoDTO getMonetization() {
    return monetization;
  }
  public void setMonetization(MonetizationInfoDTO monetization) {
    this.monetization = monetization;
  }

  /**
   * Burst control request count
   **/
  public SubscriptionThrottlePolicyAllOfDTO rateLimitCount(Integer rateLimitCount) {
    this.rateLimitCount = rateLimitCount;
    return this;
  }

  
  @ApiModelProperty(example = "10", value = "Burst control request count")
  @JsonProperty("rateLimitCount")
  public Integer getRateLimitCount() {
    return rateLimitCount;
  }
  public void setRateLimitCount(Integer rateLimitCount) {
    this.rateLimitCount = rateLimitCount;
  }

  /**
   * Burst control time unit
   **/
  public SubscriptionThrottlePolicyAllOfDTO rateLimitTimeUnit(String rateLimitTimeUnit) {
    this.rateLimitTimeUnit = rateLimitTimeUnit;
    return this;
  }

  
  @ApiModelProperty(example = "min", value = "Burst control time unit")
  @JsonProperty("rateLimitTimeUnit")
  public String getRateLimitTimeUnit() {
    return rateLimitTimeUnit;
  }
  public void setRateLimitTimeUnit(String rateLimitTimeUnit) {
    this.rateLimitTimeUnit = rateLimitTimeUnit;
  }

  /**
   * Number of subscriptions allowed
   **/
  public SubscriptionThrottlePolicyAllOfDTO subscriberCount(Integer subscriberCount) {
    this.subscriberCount = subscriberCount;
    return this;
  }

  
  @ApiModelProperty(example = "10", value = "Number of subscriptions allowed")
  @JsonProperty("subscriberCount")
  public Integer getSubscriberCount() {
    return subscriberCount;
  }
  public void setSubscriberCount(Integer subscriberCount) {
    this.subscriberCount = subscriberCount;
  }

  /**
   * Custom attributes added to the Subscription Throttling Policy 
   **/
  public SubscriptionThrottlePolicyAllOfDTO customAttributes(List<CustomAttributeDTO> customAttributes) {
    this.customAttributes = customAttributes;
    return this;
  }

  
  @ApiModelProperty(example = "[]", value = "Custom attributes added to the Subscription Throttling Policy ")
      @Valid
  @JsonProperty("customAttributes")
  public List<CustomAttributeDTO> getCustomAttributes() {
    return customAttributes;
  }
  public void setCustomAttributes(List<CustomAttributeDTO> customAttributes) {
    this.customAttributes = customAttributes;
  }

  /**
   * This indicates the action to be taken when a user goes beyond the allocated quota. If checked, the user&#39;s requests will be dropped. If unchecked, the requests will be allowed to pass through. 
   **/
  public SubscriptionThrottlePolicyAllOfDTO stopOnQuotaReach(Boolean stopOnQuotaReach) {
    this.stopOnQuotaReach = stopOnQuotaReach;
    return this;
  }

  
  @ApiModelProperty(value = "This indicates the action to be taken when a user goes beyond the allocated quota. If checked, the user's requests will be dropped. If unchecked, the requests will be allowed to pass through. ")
  @JsonProperty("stopOnQuotaReach")
  public Boolean isStopOnQuotaReach() {
    return stopOnQuotaReach;
  }
  public void setStopOnQuotaReach(Boolean stopOnQuotaReach) {
    this.stopOnQuotaReach = stopOnQuotaReach;
  }

  /**
   * define whether this is Paid or a Free plan. Allowed values are FREE or COMMERCIAL. 
   **/
  public SubscriptionThrottlePolicyAllOfDTO billingPlan(String billingPlan) {
    this.billingPlan = billingPlan;
    return this;
  }

  
  @ApiModelProperty(example = "FREE", value = "define whether this is Paid or a Free plan. Allowed values are FREE or COMMERCIAL. ")
  @JsonProperty("billingPlan")
  public String getBillingPlan() {
    return billingPlan;
  }
  public void setBillingPlan(String billingPlan) {
    this.billingPlan = billingPlan;
  }

  /**
   **/
  public SubscriptionThrottlePolicyAllOfDTO permissions(SubscriptionThrottlePolicyPermissionDTO permissions) {
    this.permissions = permissions;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("permissions")
  public SubscriptionThrottlePolicyPermissionDTO getPermissions() {
    return permissions;
  }
  public void setPermissions(SubscriptionThrottlePolicyPermissionDTO permissions) {
    this.permissions = permissions;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SubscriptionThrottlePolicyAllOfDTO subscriptionThrottlePolicyAllOf = (SubscriptionThrottlePolicyAllOfDTO) o;
    return Objects.equals(defaultLimit, subscriptionThrottlePolicyAllOf.defaultLimit) &&
        Objects.equals(monetization, subscriptionThrottlePolicyAllOf.monetization) &&
        Objects.equals(rateLimitCount, subscriptionThrottlePolicyAllOf.rateLimitCount) &&
        Objects.equals(rateLimitTimeUnit, subscriptionThrottlePolicyAllOf.rateLimitTimeUnit) &&
        Objects.equals(subscriberCount, subscriptionThrottlePolicyAllOf.subscriberCount) &&
        Objects.equals(customAttributes, subscriptionThrottlePolicyAllOf.customAttributes) &&
        Objects.equals(stopOnQuotaReach, subscriptionThrottlePolicyAllOf.stopOnQuotaReach) &&
        Objects.equals(billingPlan, subscriptionThrottlePolicyAllOf.billingPlan) &&
        Objects.equals(permissions, subscriptionThrottlePolicyAllOf.permissions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(defaultLimit, monetization, rateLimitCount, rateLimitTimeUnit, subscriberCount, customAttributes, stopOnQuotaReach, billingPlan, permissions);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubscriptionThrottlePolicyAllOfDTO {\n");
    
    sb.append("    defaultLimit: ").append(toIndentedString(defaultLimit)).append("\n");
    sb.append("    monetization: ").append(toIndentedString(monetization)).append("\n");
    sb.append("    rateLimitCount: ").append(toIndentedString(rateLimitCount)).append("\n");
    sb.append("    rateLimitTimeUnit: ").append(toIndentedString(rateLimitTimeUnit)).append("\n");
    sb.append("    subscriberCount: ").append(toIndentedString(subscriberCount)).append("\n");
    sb.append("    customAttributes: ").append(toIndentedString(customAttributes)).append("\n");
    sb.append("    stopOnQuotaReach: ").append(toIndentedString(stopOnQuotaReach)).append("\n");
    sb.append("    billingPlan: ").append(toIndentedString(billingPlan)).append("\n");
    sb.append("    permissions: ").append(toIndentedString(permissions)).append("\n");
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

