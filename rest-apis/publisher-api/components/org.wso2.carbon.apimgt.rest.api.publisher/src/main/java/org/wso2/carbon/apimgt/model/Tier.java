package org.wso2.carbon.apimgt.model;

import java.util.*;
import java.util.Map;



@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-10-14T14:19:24.818+05:30")
public class Tier  {
  
  private String name = null;
  private String description = null;
  public enum TierLevelEnum {
     api,  application,  resource, 
  };
  private TierLevelEnum tierLevel = null;
  private Map<String, String> attributes = new HashMap<String, String>();
  private Long requestCount = null;
  private Long unitTime = null;
  private String timeUnit = null;
  public enum TierPlanEnum {
     FREE,  COMMERCIAL, 
  };
  private TierPlanEnum tierPlan = null;
  private Boolean stopOnQuotaReach = null;

  /**
   **/
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   **/
  public TierLevelEnum getTierLevel() {
    return tierLevel;
  }
  public void setTierLevel(TierLevelEnum tierLevel) {
    this.tierLevel = tierLevel;
  }

  /**
   * Custom attributes added to the tier policy

   **/
  public Map<String, String> getAttributes() {
    return attributes;
  }
  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
  }

  /**
   * Maximum number of requests which can be sent within a provided unit time

   **/
  public Long getRequestCount() {
    return requestCount;
  }
  public void setRequestCount(Long requestCount) {
    this.requestCount = requestCount;
  }

  /**
   **/
  public Long getUnitTime() {
    return unitTime;
  }
  public void setUnitTime(Long unitTime) {
    this.unitTime = unitTime;
  }

  /**
   **/
  public String getTimeUnit() {
    return timeUnit;
  }
  public void setTimeUnit(String timeUnit) {
    this.timeUnit = timeUnit;
  }

  /**
   * This attribute declares whether this tier is available under commercial or free

   **/
  public TierPlanEnum getTierPlan() {
    return tierPlan;
  }
  public void setTierPlan(TierPlanEnum tierPlan) {
    this.tierPlan = tierPlan;
  }

  /**
   * By making this attribute to false, you are capabale of sending requests
even if the request count exceeded within a unit time

   **/
  public Boolean getStopOnQuotaReach() {
    return stopOnQuotaReach;
  }
  public void setStopOnQuotaReach(Boolean stopOnQuotaReach) {
    this.stopOnQuotaReach = stopOnQuotaReach;
  }


  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class Tier {\n");
    
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
