package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.swagger.annotations.*;
import javax.ws.rs.*;
import com.fasterxml.jackson.annotation.*;
import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class TierDTO  {
  
  
  @NotNull
  private String name = null;
  
  
  private String description = null;
  
  public enum TierLevelEnum {
     api,  application,  resource, 
  };
  
  private TierLevelEnum tierLevel = null;
  
  
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
  @JsonProperty("tierLevel")
  public TierLevelEnum getTierLevel() {
    return tierLevel;
  }
  public void setTierLevel(TierLevelEnum tierLevel) {
    this.tierLevel = tierLevel;
  }

  
  /**
   * Custom attributes added to the tier policy\n
   **/
  @ApiModelProperty(value = "Custom attributes added to the tier policy\n")
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
   * This attribute declares whether this tier is available under commercial or free\n
   **/
  @ApiModelProperty(required = true, value = "This attribute declares whether this tier is available under commercial or free\n")
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
    sb.append("class TierDTO {\n");
    
    sb.append("  name: ").append(name).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("  tierLevel: ").append(tierLevel).append("\n");
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
