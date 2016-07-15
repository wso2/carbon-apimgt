package org.wso2.carbon.apimgt.rest.api.admin.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.dto.CustomAttributeDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottleLimitDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottlePolicyDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class SubscriptionThrottlePolicyDTO extends ThrottlePolicyDTO {
  
  
  
  private ThrottleLimitDTO defaultLimit = null;
  
  
  private Integer rateLimitCount = null;
  
  
  private String rateLimitTimeUnit = null;
  
  
  private List<CustomAttributeDTO> customAttributes = new ArrayList<CustomAttributeDTO>();
  
  
  private Boolean stopOnQuotaReach = false;
  
  
  private String billingPlan = null;

  
  /**
   **/
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
  @ApiModelProperty(value = "")
  @JsonProperty("rateLimitCount")
  public Integer getRateLimitCount() {
    return rateLimitCount;
  }
  public void setRateLimitCount(Integer rateLimitCount) {
    this.rateLimitCount = rateLimitCount;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("rateLimitTimeUnit")
  public String getRateLimitTimeUnit() {
    return rateLimitTimeUnit;
  }
  public void setRateLimitTimeUnit(String rateLimitTimeUnit) {
    this.rateLimitTimeUnit = rateLimitTimeUnit;
  }

  
  /**
   * Custom attributes added to the Subscription Throttle policy\n
   **/
  @ApiModelProperty(value = "Custom attributes added to the Subscription Throttle policy\n")
  @JsonProperty("customAttributes")
  public List<CustomAttributeDTO> getCustomAttributes() {
    return customAttributes;
  }
  public void setCustomAttributes(List<CustomAttributeDTO> customAttributes) {
    this.customAttributes = customAttributes;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("stopOnQuotaReach")
  public Boolean getStopOnQuotaReach() {
    return stopOnQuotaReach;
  }
  public void setStopOnQuotaReach(Boolean stopOnQuotaReach) {
    this.stopOnQuotaReach = stopOnQuotaReach;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("billingPlan")
  public String getBillingPlan() {
    return billingPlan;
  }
  public void setBillingPlan(String billingPlan) {
    this.billingPlan = billingPlan;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubscriptionThrottlePolicyDTO {\n");
    sb.append("  " + super.toString()).append("\n");
    sb.append("  defaultLimit: ").append(defaultLimit).append("\n");
    sb.append("  rateLimitCount: ").append(rateLimitCount).append("\n");
    sb.append("  rateLimitTimeUnit: ").append(rateLimitTimeUnit).append("\n");
    sb.append("  customAttributes: ").append(customAttributes).append("\n");
    sb.append("  stopOnQuotaReach: ").append(stopOnQuotaReach).append("\n");
    sb.append("  billingPlan: ").append(billingPlan).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
