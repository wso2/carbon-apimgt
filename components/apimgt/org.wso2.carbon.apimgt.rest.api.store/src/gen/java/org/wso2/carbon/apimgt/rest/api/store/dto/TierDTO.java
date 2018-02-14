package org.wso2.carbon.apimgt.rest.api.store.dto;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.wso2.carbon.apimgt.rest.api.store.dto.TierPermissionInfoDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class TierDTO  {
  
  
  @NotNull
  private String name = null;
  
  
  private String description = null;
  
  public enum TierLevelEnum {
     api,  application, 
  };
  
  private TierLevelEnum tierLevel = null;
  
  
  private Map<String, String> attributes = new HashMap<String, String>();
  
  @NotNull
  private Long requestCount = null;
  
  @NotNull
  private Long unitTime = null;
  
  public enum TierPlanEnum {
     FREE,  COMMERCIAL, 
  };
  @NotNull
  private TierPlanEnum tierPlan = null;
  
  @NotNull
  private Boolean stopOnQuotaReach = null;
  
  
  private TierPermissionInfoDTO tierPermissions = null;

  private String lastUpdatedTime = null;

  private String createdTime = null;

  /**
  * gets and sets the lastUpdatedTime for TierDTO
  **/
  @JsonIgnore
  public String getLastUpdatedTime(){
    return lastUpdatedTime;
  }
  public void setLastUpdatedTime(String lastUpdatedTime){
    this.lastUpdatedTime=lastUpdatedTime;
  }

  /**
  * gets and sets the createdTime for a TierDTO
  **/

  @JsonIgnore
  public String getCreatedTime(){
    return createdTime;
  }
  public void setCreatedTime(String createdTime){
    this.createdTime=createdTime;
  }

  
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
   * If this attribute is set to false, you are capabale of sending requests\neven if the request count exceeded within a unit time\n
   **/
  @ApiModelProperty(required = true, value = "If this attribute is set to false, you are capabale of sending requests\neven if the request count exceeded within a unit time\n")
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
  @JsonProperty("tierPermissions")
  public TierPermissionInfoDTO getTierPermissions() {
    return tierPermissions;
  }
  public void setTierPermissions(TierPermissionInfoDTO tierPermissions) {
    this.tierPermissions = tierPermissions;
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
    sb.append("  tierPlan: ").append(tierPlan).append("\n");
    sb.append("  stopOnQuotaReach: ").append(stopOnQuotaReach).append("\n");
    sb.append("  tierPermissions: ").append(tierPermissions).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
