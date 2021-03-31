package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.CustomAttributeDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SubscriptionThrottlePolicyPermissionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ThrottleLimitDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class SubscriptionPolicyAllOfDTO   {
  
    private Integer policyId = null;
    private String uuid = null;
    private String policyName = null;
    private String displayName = null;
    private String description = null;
    private Boolean isDeployed = false;
    private Integer tenantId = null;
    private String tenantDomain = null;
    private ThrottleLimitDTO defaultLimit = null;
    private Integer rateLimitCount = null;
    private String rateLimitTimeUnit = null;
    private Integer subscriberCount = null;
    private List<CustomAttributeDTO> customAttributes = new ArrayList<CustomAttributeDTO>();
    private Boolean stopOnQuotaReach = false;
    private String billingPlan = null;
    private SubscriptionThrottlePolicyPermissionDTO permissions = null;

  /**
   * Id of policy
   **/
  public SubscriptionPolicyAllOfDTO policyId(Integer policyId) {
    this.policyId = policyId;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "Id of policy")
  @JsonProperty("policyId")
  public Integer getPolicyId() {
    return policyId;
  }
  public void setPolicyId(Integer policyId) {
    this.policyId = policyId;
  }

  /**
   * policy uuid
   **/
  public SubscriptionPolicyAllOfDTO uuid(String uuid) {
    this.uuid = uuid;
    return this;
  }

  
  @ApiModelProperty(example = "0c6439fd-9b16-3c2e-be6e-1086e0b9aa93", value = "policy uuid")
  @JsonProperty("uuid")
  public String getUuid() {
    return uuid;
  }
  public void setUuid(String uuid) {
    this.uuid = uuid;
  }

  /**
   * Name of policy
   **/
  public SubscriptionPolicyAllOfDTO policyName(String policyName) {
    this.policyName = policyName;
    return this;
  }

  
  @ApiModelProperty(example = "30PerMin", value = "Name of policy")
  @JsonProperty("policyName")
 @Size(min=1,max=60)  public String getPolicyName() {
    return policyName;
  }
  public void setPolicyName(String policyName) {
    this.policyName = policyName;
  }

  /**
   * Display name of the policy
   **/
  public SubscriptionPolicyAllOfDTO displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(example = "30PerMin", value = "Display name of the policy")
  @JsonProperty("displayName")
 @Size(max=512)  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * Description of the policy
   **/
  public SubscriptionPolicyAllOfDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "Allows 30 request per minute", value = "Description of the policy")
  @JsonProperty("description")
 @Size(max=1024)  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Indicates whether the policy is deployed successfully or not.
   **/
  public SubscriptionPolicyAllOfDTO isDeployed(Boolean isDeployed) {
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
   * Throttling policy tenant domain id
   **/
  public SubscriptionPolicyAllOfDTO tenantId(Integer tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  
  @ApiModelProperty(example = "-1234", value = "Throttling policy tenant domain id")
  @JsonProperty("tenantId")
  public Integer getTenantId() {
    return tenantId;
  }
  public void setTenantId(Integer tenantId) {
    this.tenantId = tenantId;
  }

  /**
   * Throttling policy tenant domain
   **/
  public SubscriptionPolicyAllOfDTO tenantDomain(String tenantDomain) {
    this.tenantDomain = tenantDomain;
    return this;
  }

  
  @ApiModelProperty(example = "carbon.super", value = "Throttling policy tenant domain")
  @JsonProperty("tenantDomain")
  public String getTenantDomain() {
    return tenantDomain;
  }
  public void setTenantDomain(String tenantDomain) {
    this.tenantDomain = tenantDomain;
  }

  /**
   **/
  public SubscriptionPolicyAllOfDTO defaultLimit(ThrottleLimitDTO defaultLimit) {
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
   * Burst control request count
   **/
  public SubscriptionPolicyAllOfDTO rateLimitCount(Integer rateLimitCount) {
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
  public SubscriptionPolicyAllOfDTO rateLimitTimeUnit(String rateLimitTimeUnit) {
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
  public SubscriptionPolicyAllOfDTO subscriberCount(Integer subscriberCount) {
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
  public SubscriptionPolicyAllOfDTO customAttributes(List<CustomAttributeDTO> customAttributes) {
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
  public SubscriptionPolicyAllOfDTO stopOnQuotaReach(Boolean stopOnQuotaReach) {
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
  public SubscriptionPolicyAllOfDTO billingPlan(String billingPlan) {
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
  public SubscriptionPolicyAllOfDTO permissions(SubscriptionThrottlePolicyPermissionDTO permissions) {
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
    SubscriptionPolicyAllOfDTO subscriptionPolicyAllOf = (SubscriptionPolicyAllOfDTO) o;
    return Objects.equals(policyId, subscriptionPolicyAllOf.policyId) &&
        Objects.equals(uuid, subscriptionPolicyAllOf.uuid) &&
        Objects.equals(policyName, subscriptionPolicyAllOf.policyName) &&
        Objects.equals(displayName, subscriptionPolicyAllOf.displayName) &&
        Objects.equals(description, subscriptionPolicyAllOf.description) &&
        Objects.equals(isDeployed, subscriptionPolicyAllOf.isDeployed) &&
        Objects.equals(tenantId, subscriptionPolicyAllOf.tenantId) &&
        Objects.equals(tenantDomain, subscriptionPolicyAllOf.tenantDomain) &&
        Objects.equals(defaultLimit, subscriptionPolicyAllOf.defaultLimit) &&
        Objects.equals(rateLimitCount, subscriptionPolicyAllOf.rateLimitCount) &&
        Objects.equals(rateLimitTimeUnit, subscriptionPolicyAllOf.rateLimitTimeUnit) &&
        Objects.equals(subscriberCount, subscriptionPolicyAllOf.subscriberCount) &&
        Objects.equals(customAttributes, subscriptionPolicyAllOf.customAttributes) &&
        Objects.equals(stopOnQuotaReach, subscriptionPolicyAllOf.stopOnQuotaReach) &&
        Objects.equals(billingPlan, subscriptionPolicyAllOf.billingPlan) &&
        Objects.equals(permissions, subscriptionPolicyAllOf.permissions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(policyId, uuid, policyName, displayName, description, isDeployed, tenantId, tenantDomain, defaultLimit, rateLimitCount, rateLimitTimeUnit, subscriberCount, customAttributes, stopOnQuotaReach, billingPlan, permissions);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubscriptionPolicyAllOfDTO {\n");
    
    sb.append("    policyId: ").append(toIndentedString(policyId)).append("\n");
    sb.append("    uuid: ").append(toIndentedString(uuid)).append("\n");
    sb.append("    policyName: ").append(toIndentedString(policyName)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    isDeployed: ").append(toIndentedString(isDeployed)).append("\n");
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    tenantDomain: ").append(toIndentedString(tenantDomain)).append("\n");
    sb.append("    defaultLimit: ").append(toIndentedString(defaultLimit)).append("\n");
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

