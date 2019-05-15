package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ThrottlingPolicyDTO  {
  
  
  @NotNull
  private String name = null;
  
  
  private String description = null;
  
  public enum PolicyLevelEnum {
     subscription,  api, 
  };
  
  private PolicyLevelEnum policyLevel = null;
  
  
  private String displayName = null;
  
  
  private Map<String, String> attributes = new HashMap<String, String>();
  
  @NotNull
  private Long requestCount = null;
  
  @NotNull
  private Long unitTime = null;
  
  
  private String timeUnit = null;
  
  public enum TierPlanEnum {
     FREE,  COMMERCIAL, 
  };
  @NotNull
  private TierPlanEnum tierPlan = null;
  
  @NotNull
  private Boolean stopOnQuotaReach = null;

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("policyLevel")
  public PolicyLevelEnum getPolicyLevel() {
    return policyLevel;
  }
  public void setPolicyLevel(PolicyLevelEnum policyLevel) {
    this.policyLevel = policyLevel;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  
  /**
   * Custom attributes added to the policy policy\n
   **/
  @ApiModelProperty(value = "Custom attributes added to the policy policy\n")
  @JsonProperty("attributes")
  public Map<String, String> getAttributes() {
    return attributes;
  }
  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
  }

  
  /**
   * Maximum number of requests which can be sent within a provided unit time\n
   **/
  @ApiModelProperty(required = true, value = "Maximum number of requests which can be sent within a provided unit time\n")
  @JsonProperty("requestCount")
  public Long getRequestCount() {
    return requestCount;
  }
  public void setRequestCount(Long requestCount) {
    this.requestCount = requestCount;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("unitTime")
  public Long getUnitTime() {
    return unitTime;
  }
  public void setUnitTime(Long unitTime) {
    this.unitTime = unitTime;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("timeUnit")
  public String getTimeUnit() {
    return timeUnit;
  }
  public void setTimeUnit(String timeUnit) {
    this.timeUnit = timeUnit;
  }

  
  /**
   * This attribute declares whether this policy is available under commercial or free\n
   **/
  @ApiModelProperty(required = true, value = "This attribute declares whether this policy is available under commercial or free\n")
  @JsonProperty("tierPlan")
  public TierPlanEnum getTierPlan() {
    return tierPlan;
  }
  public void setTierPlan(TierPlanEnum tierPlan) {
    this.tierPlan = tierPlan;
  }

  
  /**
   * By making this attribute to false, you are capabale of sending requests\neven if the request count exceeded within a unit time\n
   **/
  @ApiModelProperty(required = true, value = "By making this attribute to false, you are capabale of sending requests\neven if the request count exceeded within a unit time\n")
  @JsonProperty("stopOnQuotaReach")
  public Boolean getStopOnQuotaReach() {
    return stopOnQuotaReach;
  }
  public void setStopOnQuotaReach(Boolean stopOnQuotaReach) {
    this.stopOnQuotaReach = stopOnQuotaReach;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ThrottlingPolicyDTO {\n");
    
    sb.append("  name: ").append(name).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("  policyLevel: ").append(policyLevel).append("\n");
    sb.append("  displayName: ").append(displayName).append("\n");
    sb.append("  attributes: ").append(attributes).append("\n");
    sb.append("  requestCount: ").append(requestCount).append("\n");
    sb.append("  unitTime: ").append(unitTime).append("\n");
    sb.append("  timeUnit: ").append(timeUnit).append("\n");
    sb.append("  tierPlan: ").append(tierPlan).append("\n");
    sb.append("  stopOnQuotaReach: ").append(stopOnQuotaReach).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
