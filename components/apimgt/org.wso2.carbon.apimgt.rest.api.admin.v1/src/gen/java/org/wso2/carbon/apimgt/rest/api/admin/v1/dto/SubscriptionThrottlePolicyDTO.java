package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.CustomAttributeDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.MonetizationInfoDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ThrottleLimitDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ThrottlePolicyDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class SubscriptionThrottlePolicyDTO   {
  
    private String policyId = null;
    private String policyName = null;
    private String displayName = null;
    private String description = null;
    private Boolean isDeployed = false;
    private ThrottleLimitDTO defaultLimit = null;
    private MonetizationInfoDTO monetization = null;
    private Integer rateLimitCount = null;
    private String rateLimitTimeUnit = null;
    private List<CustomAttributeDTO> customAttributes = new ArrayList<>();
    private Boolean stopOnQuotaReach = false;
    private String billingPlan = null;

  /**
   * Id of policy
   **/
  public SubscriptionThrottlePolicyDTO policyId(String policyId) {
    this.policyId = policyId;
    return this;
  }

  
  @ApiModelProperty(example = "0c6439fd-9b16-3c2e-be6e-1086e0b9aa93", value = "Id of policy")
  @JsonProperty("policyId")
  public String getPolicyId() {
    return policyId;
  }
  public void setPolicyId(String policyId) {
    this.policyId = policyId;
  }

  /**
   * Name of policy
   **/
  public SubscriptionThrottlePolicyDTO policyName(String policyName) {
    this.policyName = policyName;
    return this;
  }

  
  @ApiModelProperty(example = "Policy1", required = true, value = "Name of policy")
  @JsonProperty("policyName")
  @NotNull
  public String getPolicyName() {
    return policyName;
  }
  public void setPolicyName(String policyName) {
    this.policyName = policyName;
  }

  /**
   * Display name of the policy
   **/
  public SubscriptionThrottlePolicyDTO displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(value = "Display name of the policy")
  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * Description of the policy
   **/
  public SubscriptionThrottlePolicyDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(value = "Description of the policy")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Indicates whether the policy is deployed successfully or not.
   **/
  public SubscriptionThrottlePolicyDTO isDeployed(Boolean isDeployed) {
    this.isDeployed = isDeployed;
    return this;
  }

  
  @ApiModelProperty(value = "Indicates whether the policy is deployed successfully or not.")
  @JsonProperty("isDeployed")
  public Boolean isIsDeployed() {
    return isDeployed;
  }
  public void setIsDeployed(Boolean isDeployed) {
    this.isDeployed = isDeployed;
  }

  /**
   **/
  public SubscriptionThrottlePolicyDTO defaultLimit(ThrottleLimitDTO defaultLimit) {
    this.defaultLimit = defaultLimit;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("defaultLimit")
  public ThrottleLimitDTO getDefaultLimit() {
    return defaultLimit;
  }
  public void setDefaultLimit(ThrottleLimitDTO defaultLimit) {
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
    return Objects.equals(policyId, subscriptionThrottlePolicy.policyId) &&
        Objects.equals(policyName, subscriptionThrottlePolicy.policyName) &&
        Objects.equals(displayName, subscriptionThrottlePolicy.displayName) &&
        Objects.equals(description, subscriptionThrottlePolicy.description) &&
        Objects.equals(isDeployed, subscriptionThrottlePolicy.isDeployed) &&
        Objects.equals(defaultLimit, subscriptionThrottlePolicy.defaultLimit) &&
        Objects.equals(monetization, subscriptionThrottlePolicy.monetization) &&
        Objects.equals(rateLimitCount, subscriptionThrottlePolicy.rateLimitCount) &&
        Objects.equals(rateLimitTimeUnit, subscriptionThrottlePolicy.rateLimitTimeUnit) &&
        Objects.equals(customAttributes, subscriptionThrottlePolicy.customAttributes) &&
        Objects.equals(stopOnQuotaReach, subscriptionThrottlePolicy.stopOnQuotaReach) &&
        Objects.equals(billingPlan, subscriptionThrottlePolicy.billingPlan);
  }

  @Override
  public int hashCode() {
    return Objects.hash(policyId, policyName, displayName, description, isDeployed, defaultLimit, monetization, rateLimitCount, rateLimitTimeUnit, customAttributes, stopOnQuotaReach, billingPlan);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubscriptionThrottlePolicyDTO {\n");
    
    sb.append("    policyId: ").append(toIndentedString(policyId)).append("\n");
    sb.append("    policyName: ").append(toIndentedString(policyName)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    isDeployed: ").append(toIndentedString(isDeployed)).append("\n");
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

