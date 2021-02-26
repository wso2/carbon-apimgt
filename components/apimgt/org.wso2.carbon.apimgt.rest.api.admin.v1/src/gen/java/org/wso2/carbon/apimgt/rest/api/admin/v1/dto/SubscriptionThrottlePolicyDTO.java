package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.CustomAttributeDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.GraphQLQueryDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.MonetizationInfoDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.SubscriptionThrottlePolicyAllOfDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.SubscriptionThrottlePolicyPermissionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ThrottleLimitDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ThrottlePolicyDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class SubscriptionThrottlePolicyDTO extends ThrottlePolicyDTO  {
  
    private Integer graphQLMaxComplexity = null;
    private Integer graphQLMaxDepth = null;
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
   * Maximum Complexity of the GraphQL query
   **/
  public SubscriptionThrottlePolicyDTO graphQLMaxComplexity(Integer graphQLMaxComplexity) {
    this.graphQLMaxComplexity = graphQLMaxComplexity;
    return this;
  }

  
  @ApiModelProperty(example = "400", value = "Maximum Complexity of the GraphQL query")
  @JsonProperty("graphQLMaxComplexity")
  public Integer getGraphQLMaxComplexity() {
    return graphQLMaxComplexity;
  }
  public void setGraphQLMaxComplexity(Integer graphQLMaxComplexity) {
    this.graphQLMaxComplexity = graphQLMaxComplexity;
  }

  /**
   * Maximum Depth of the GraphQL query
   **/
  public SubscriptionThrottlePolicyDTO graphQLMaxDepth(Integer graphQLMaxDepth) {
    this.graphQLMaxDepth = graphQLMaxDepth;
    return this;
  }

  
  @ApiModelProperty(example = "10", value = "Maximum Depth of the GraphQL query")
  @JsonProperty("graphQLMaxDepth")
  public Integer getGraphQLMaxDepth() {
    return graphQLMaxDepth;
  }
  public void setGraphQLMaxDepth(Integer graphQLMaxDepth) {
    this.graphQLMaxDepth = graphQLMaxDepth;
  }

  /**
   **/
  public SubscriptionThrottlePolicyDTO defaultLimit(ThrottleLimitDTO defaultLimit) {
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
  public SubscriptionThrottlePolicyDTO monetization(MonetizationInfoDTO monetization) {
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
  public SubscriptionThrottlePolicyDTO subscriberCount(Integer subscriberCount) {
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
  public SubscriptionThrottlePolicyDTO customAttributes(List<CustomAttributeDTO> customAttributes) {
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
  public SubscriptionThrottlePolicyDTO permissions(SubscriptionThrottlePolicyPermissionDTO permissions) {
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
    SubscriptionThrottlePolicyDTO subscriptionThrottlePolicy = (SubscriptionThrottlePolicyDTO) o;
    return Objects.equals(graphQLMaxComplexity, subscriptionThrottlePolicy.graphQLMaxComplexity) &&
        Objects.equals(graphQLMaxDepth, subscriptionThrottlePolicy.graphQLMaxDepth) &&
        Objects.equals(defaultLimit, subscriptionThrottlePolicy.defaultLimit) &&
        Objects.equals(monetization, subscriptionThrottlePolicy.monetization) &&
        Objects.equals(rateLimitCount, subscriptionThrottlePolicy.rateLimitCount) &&
        Objects.equals(rateLimitTimeUnit, subscriptionThrottlePolicy.rateLimitTimeUnit) &&
        Objects.equals(subscriberCount, subscriptionThrottlePolicy.subscriberCount) &&
        Objects.equals(customAttributes, subscriptionThrottlePolicy.customAttributes) &&
        Objects.equals(stopOnQuotaReach, subscriptionThrottlePolicy.stopOnQuotaReach) &&
        Objects.equals(billingPlan, subscriptionThrottlePolicy.billingPlan) &&
        Objects.equals(permissions, subscriptionThrottlePolicy.permissions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(graphQLMaxComplexity, graphQLMaxDepth, defaultLimit, monetization, rateLimitCount, rateLimitTimeUnit, subscriberCount, customAttributes, stopOnQuotaReach, billingPlan, permissions);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubscriptionThrottlePolicyDTO {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    graphQLMaxComplexity: ").append(toIndentedString(graphQLMaxComplexity)).append("\n");
    sb.append("    graphQLMaxDepth: ").append(toIndentedString(graphQLMaxDepth)).append("\n");
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

