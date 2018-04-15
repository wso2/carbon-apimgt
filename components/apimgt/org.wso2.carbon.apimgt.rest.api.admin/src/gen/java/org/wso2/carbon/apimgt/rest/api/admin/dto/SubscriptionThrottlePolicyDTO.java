package org.wso2.carbon.apimgt.rest.api.admin.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.dto.CustomAttributeDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottleLimitDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottlePolicyDTO;
import java.util.Objects;

/**
 * SubscriptionThrottlePolicyDTO
 */
public class SubscriptionThrottlePolicyDTO extends ThrottlePolicyDTO  {
  @SerializedName("defaultLimit")
  private ThrottleLimitDTO defaultLimit = null;

  @SerializedName("rateLimitCount")
  private Integer rateLimitCount = null;

  @SerializedName("rateLimitTimeUnit")
  private String rateLimitTimeUnit = null;

  @SerializedName("customAttributes")
  private List<CustomAttributeDTO> customAttributes = new ArrayList<CustomAttributeDTO>();

  @SerializedName("stopOnQuotaReach")
  private Boolean stopOnQuotaReach = false;

  @SerializedName("billingPlan")
  private String billingPlan = null;

  public SubscriptionThrottlePolicyDTO defaultLimit(ThrottleLimitDTO defaultLimit) {
    this.defaultLimit = defaultLimit;
    return this;
  }

   /**
   * Get defaultLimit
   * @return defaultLimit
  **/
  @ApiModelProperty(value = "")
  public ThrottleLimitDTO getDefaultLimit() {
    return defaultLimit;
  }

  public void setDefaultLimit(ThrottleLimitDTO defaultLimit) {
    this.defaultLimit = defaultLimit;
  }

  public SubscriptionThrottlePolicyDTO rateLimitCount(Integer rateLimitCount) {
    this.rateLimitCount = rateLimitCount;
    return this;
  }

   /**
   * Get rateLimitCount
   * @return rateLimitCount
  **/
  @ApiModelProperty(value = "")
  public Integer getRateLimitCount() {
    return rateLimitCount;
  }

  public void setRateLimitCount(Integer rateLimitCount) {
    this.rateLimitCount = rateLimitCount;
  }

  public SubscriptionThrottlePolicyDTO rateLimitTimeUnit(String rateLimitTimeUnit) {
    this.rateLimitTimeUnit = rateLimitTimeUnit;
    return this;
  }

   /**
   * Get rateLimitTimeUnit
   * @return rateLimitTimeUnit
  **/
  @ApiModelProperty(value = "")
  public String getRateLimitTimeUnit() {
    return rateLimitTimeUnit;
  }

  public void setRateLimitTimeUnit(String rateLimitTimeUnit) {
    this.rateLimitTimeUnit = rateLimitTimeUnit;
  }

  public SubscriptionThrottlePolicyDTO customAttributes(List<CustomAttributeDTO> customAttributes) {
    this.customAttributes = customAttributes;
    return this;
  }

  public SubscriptionThrottlePolicyDTO addCustomAttributesItem(CustomAttributeDTO customAttributesItem) {
    this.customAttributes.add(customAttributesItem);
    return this;
  }

   /**
   * Custom attributes added to the Subscription Throttle policy 
   * @return customAttributes
  **/
  @ApiModelProperty(example = "{}", value = "Custom attributes added to the Subscription Throttle policy ")
  public List<CustomAttributeDTO> getCustomAttributes() {
    return customAttributes;
  }

  public void setCustomAttributes(List<CustomAttributeDTO> customAttributes) {
    this.customAttributes = customAttributes;
  }

  public SubscriptionThrottlePolicyDTO stopOnQuotaReach(Boolean stopOnQuotaReach) {
    this.stopOnQuotaReach = stopOnQuotaReach;
    return this;
  }

   /**
   * Get stopOnQuotaReach
   * @return stopOnQuotaReach
  **/
  @ApiModelProperty(value = "")
  public Boolean getStopOnQuotaReach() {
    return stopOnQuotaReach;
  }

  public void setStopOnQuotaReach(Boolean stopOnQuotaReach) {
    this.stopOnQuotaReach = stopOnQuotaReach;
  }

  public SubscriptionThrottlePolicyDTO billingPlan(String billingPlan) {
    this.billingPlan = billingPlan;
    return this;
  }

   /**
   * Get billingPlan
   * @return billingPlan
  **/
  @ApiModelProperty(value = "")
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
    return Objects.equals(this.defaultLimit, subscriptionThrottlePolicy.defaultLimit) &&
        Objects.equals(this.rateLimitCount, subscriptionThrottlePolicy.rateLimitCount) &&
        Objects.equals(this.rateLimitTimeUnit, subscriptionThrottlePolicy.rateLimitTimeUnit) &&
        Objects.equals(this.customAttributes, subscriptionThrottlePolicy.customAttributes) &&
        Objects.equals(this.stopOnQuotaReach, subscriptionThrottlePolicy.stopOnQuotaReach) &&
        Objects.equals(this.billingPlan, subscriptionThrottlePolicy.billingPlan) &&
        super.equals(o);
  }

  @Override
  public int hashCode() {
    return Objects.hash(defaultLimit, rateLimitCount, rateLimitTimeUnit, customAttributes, stopOnQuotaReach, billingPlan, super.hashCode());
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubscriptionThrottlePolicyDTO {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    defaultLimit: ").append(toIndentedString(defaultLimit)).append("\n");
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

