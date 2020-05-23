package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.CustomAttributeDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.MonetizationInfoDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ThrottleLimitTypeDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ThrottlePolicyDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class SubscriptionThrottlePolicyDTO extends ThrottlePolicyDTO  {
  
    private ThrottleLimitTypeDTO defaultLimit = null;
    private MonetizationInfoDTO monetization = null;
    private Integer rateLimitCount = null;
    private String rateLimitTimeUnit = null;
    private List<CustomAttributeDTO> customAttributes = new ArrayList<>();
    private Boolean stopOnQuotaReach = false;
    private String billingPlan = null;

  /**
   **/
  public SubscriptionThrottlePolicyDTO defaultLimit(ThrottleLimitTypeDTO defaultLimit) {
    this.defaultLimit = defaultLimit;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("defaultLimit")
  public ThrottleLimitTypeDTO getDefaultLimit() {
    return defaultLimit;
  }
  public void setDefaultLimit(ThrottleLimitTypeDTO defaultLimit) {
    this.defaultLimit = defaultLimit;
  }

  /**
   **/
  public SubscriptionThrottlePolicyDTO monetization(MonetizationInfoDTO monetization) {
    this.monetization = monetization;
    return this;
  }

  
  @ApiModelProperty(value = "")
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
  public SubscriptionThrottlePolicyDTO rateLimitCount(Integer rateLimitCount) {
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
  public SubscriptionThrottlePolicyDTO rateLimitTimeUnit(String rateLimitTimeUnit) {
    this.rateLimitTimeUnit = rateLimitTimeUnit;
    return this;
  }

  
  @ApiModelProperty(value = "Burst control time unit")
  @JsonProperty("rateLimitTimeUnit")
  public String getRateLimitTimeUnit() {
    return rateLimitTimeUnit;
  }
  public void setRateLimitTimeUnit(String rateLimitTimeUnit) {
    this.rateLimitTimeUnit = rateLimitTimeUnit;
  }

  /**
   * Custom attributes added to the Subscription Throttling Policy 
   **/
  public SubscriptionThrottlePolicyDTO customAttributes(List<CustomAttributeDTO> customAttributes) {
    this.customAttributes = customAttributes;
    return this;
  }

  
  @ApiModelProperty(example = "{}", value = "Custom attributes added to the Subscription Throttling Policy ")
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
  public SubscriptionThrottlePolicyDTO stopOnQuotaReach(Boolean stopOnQuotaReach) {
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
  public SubscriptionThrottlePolicyDTO billingPlan(String billingPlan) {
    this.billingPlan = billingPlan;
    return this;
  }

  
  @ApiModelProperty(value = "define whether this is Paid or a Free plan. Allowed values are FREE or COMMERCIAL. ")
  @JsonProperty("billingPlan")
  public String getBillingPlan() {
    return billingPlan;
  }
  public void setBillingPlan(String billingPlan) {
    this.billingPlan = billingPlan;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SubscriptionThrottlePolicyDTO subscriptionThrottlePolicy = (SubscriptionThrottlePolicyDTO) o;
    return Objects.equals(defaultLimit, subscriptionThrottlePolicy.defaultLimit) &&
        Objects.equals(monetization, subscriptionThrottlePolicy.monetization) &&
        Objects.equals(rateLimitCount, subscriptionThrottlePolicy.rateLimitCount) &&
        Objects.equals(rateLimitTimeUnit, subscriptionThrottlePolicy.rateLimitTimeUnit) &&
        Objects.equals(customAttributes, subscriptionThrottlePolicy.customAttributes) &&
        Objects.equals(stopOnQuotaReach, subscriptionThrottlePolicy.stopOnQuotaReach) &&
        Objects.equals(billingPlan, subscriptionThrottlePolicy.billingPlan);
  }

  @Override
  public int hashCode() {
    return Objects.hash(defaultLimit, monetization, rateLimitCount, rateLimitTimeUnit, customAttributes, stopOnQuotaReach, billingPlan);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubscriptionThrottlePolicyDTO {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    defaultLimit: ").append(toIndentedString(defaultLimit)).append("\n");
    sb.append("    monetization: ").append(toIndentedString(monetization)).append("\n");
    sb.append("    rateLimitCount: ").append(toIndentedString(rateLimitCount)).append("\n");
    sb.append("    rateLimitTimeUnit: ").append(toIndentedString(rateLimitTimeUnit)).append("\n");
    sb.append("    customAttributes: ").append(toIndentedString(customAttributes)).append("\n");
    sb.append("    stopOnQuotaReach: ").append(toIndentedString(stopOnQuotaReach)).append("\n");
    sb.append("    billingPlan: ").append(toIndentedString(billingPlan)).append("\n");
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

