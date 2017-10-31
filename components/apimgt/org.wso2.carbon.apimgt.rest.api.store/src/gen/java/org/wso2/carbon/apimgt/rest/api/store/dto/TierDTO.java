package org.wso2.carbon.apimgt.rest.api.store.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

<<<<<<< HEAD
import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;
=======
/**
 * TierDTO
 */
public class TierDTO   {
  @JsonProperty("name")
  private String name = null;
>>>>>>> upstream/master

  @JsonProperty("description")
  private String description = null;

  /**
   * Gets or Sets tierLevel
   */
  public enum TierLevelEnum {
    API("api"),
    
    APPLICATION("application"),
    
    SUBSCRIPTION("subscription");

    private String value;

    TierLevelEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static TierLevelEnum fromValue(String text) {
      for (TierLevelEnum b : TierLevelEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("tierLevel")
  private TierLevelEnum tierLevel = null;

  @JsonProperty("attributes")
  private Map<String, String> attributes = new HashMap<String, String>();

  @JsonProperty("requestCount")
  private Long requestCount = null;

  @JsonProperty("unitTime")
  private Long unitTime = null;

  /**
   * This attribute declares whether this policy is available under commercial or free 
   */
  public enum TierPlanEnum {
    FREE("FREE"),
    
    COMMERCIAL("COMMERCIAL");

<<<<<<< HEAD
  private String lastUpdatedTime = null;
=======
    private String value;

    TierPlanEnum(String value) {
      this.value = value;
    }
>>>>>>> upstream/master

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static TierPlanEnum fromValue(String text) {
      for (TierPlanEnum b : TierPlanEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("tierPlan")
  private TierPlanEnum tierPlan = null;

  @JsonProperty("stopOnQuotaReach")
  private Boolean stopOnQuotaReach = null;

  public TierDTO name(String name) {
    this.name = name;
    return this;
  }

<<<<<<< HEAD
  
  /**
   **/
=======
   /**
   * Get name
   * @return name
  **/
>>>>>>> upstream/master
  @ApiModelProperty(required = true, value = "")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

<<<<<<< HEAD
  
  /**
   **/
=======
  public TierDTO description(String description) {
    this.description = description;
    return this;
  }

   /**
   * Get description
   * @return description
  **/
>>>>>>> upstream/master
  @ApiModelProperty(value = "")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

<<<<<<< HEAD
  
  /**
   **/
=======
  public TierDTO tierLevel(TierLevelEnum tierLevel) {
    this.tierLevel = tierLevel;
    return this;
  }

   /**
   * Get tierLevel
   * @return tierLevel
  **/
>>>>>>> upstream/master
  @ApiModelProperty(value = "")
  public TierLevelEnum getTierLevel() {
    return tierLevel;
  }

  public void setTierLevel(TierLevelEnum tierLevel) {
    this.tierLevel = tierLevel;
  }

<<<<<<< HEAD
  
  /**
   * Custom attributes added to the tier policy\n
   **/
  @ApiModelProperty(value = "Custom attributes added to the tier policy\n")
  @JsonProperty("attributes")
=======
  public TierDTO attributes(Map<String, String> attributes) {
    this.attributes = attributes;
    return this;
  }

  public TierDTO putAttributesItem(String key, String attributesItem) {
    this.attributes.put(key, attributesItem);
    return this;
  }

   /**
   * Custom attributes added to the policy policy 
   * @return attributes
  **/
  @ApiModelProperty(value = "Custom attributes added to the policy policy ")
>>>>>>> upstream/master
  public Map<String, String> getAttributes() {
    return attributes;
  }

  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
  }

<<<<<<< HEAD
  
  /**
   * Maximum number of requests which can be sent within a provided unit time\n
   **/
  @ApiModelProperty(required = true, value = "Maximum number of requests which can be sent within a provided unit time\n")
  @JsonProperty("requestCount")
=======
  public TierDTO requestCount(Long requestCount) {
    this.requestCount = requestCount;
    return this;
  }

   /**
   * Maximum number of requests which can be sent within a provided unit time 
   * @return requestCount
  **/
  @ApiModelProperty(required = true, value = "Maximum number of requests which can be sent within a provided unit time ")
>>>>>>> upstream/master
  public Long getRequestCount() {
    return requestCount;
  }

  public void setRequestCount(Long requestCount) {
    this.requestCount = requestCount;
  }

<<<<<<< HEAD
  
  /**
   **/
=======
  public TierDTO unitTime(Long unitTime) {
    this.unitTime = unitTime;
    return this;
  }

   /**
   * Get unitTime
   * @return unitTime
  **/
>>>>>>> upstream/master
  @ApiModelProperty(required = true, value = "")
  public Long getUnitTime() {
    return unitTime;
  }

  public void setUnitTime(Long unitTime) {
    this.unitTime = unitTime;
  }

<<<<<<< HEAD
  
  /**
   * This attribute declares whether this tier is available under commercial or free\n
   **/
  @ApiModelProperty(required = true, value = "This attribute declares whether this tier is available under commercial or free\n")
  @JsonProperty("tierPlan")
=======
  public TierDTO tierPlan(TierPlanEnum tierPlan) {
    this.tierPlan = tierPlan;
    return this;
  }

   /**
   * This attribute declares whether this policy is available under commercial or free 
   * @return tierPlan
  **/
  @ApiModelProperty(required = true, value = "This attribute declares whether this policy is available under commercial or free ")
>>>>>>> upstream/master
  public TierPlanEnum getTierPlan() {
    return tierPlan;
  }

  public void setTierPlan(TierPlanEnum tierPlan) {
    this.tierPlan = tierPlan;
  }

<<<<<<< HEAD
  
  /**
   * If this attribute is set to false, you are capabale of sending requests\neven if the request count exceeded within a unit time\n
   **/
  @ApiModelProperty(required = true, value = "If this attribute is set to false, you are capabale of sending requests\neven if the request count exceeded within a unit time\n")
  @JsonProperty("stopOnQuotaReach")
=======
  public TierDTO stopOnQuotaReach(Boolean stopOnQuotaReach) {
    this.stopOnQuotaReach = stopOnQuotaReach;
    return this;
  }

   /**
   * If this attribute is set to false, you are capabale of sending requests even if the request count exceeded within a unit time 
   * @return stopOnQuotaReach
  **/
  @ApiModelProperty(required = true, value = "If this attribute is set to false, you are capabale of sending requests even if the request count exceeded within a unit time ")
>>>>>>> upstream/master
  public Boolean getStopOnQuotaReach() {
    return stopOnQuotaReach;
  }

  public void setStopOnQuotaReach(Boolean stopOnQuotaReach) {
    this.stopOnQuotaReach = stopOnQuotaReach;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TierDTO tier = (TierDTO) o;
    return Objects.equals(this.name, tier.name) &&
        Objects.equals(this.description, tier.description) &&
        Objects.equals(this.tierLevel, tier.tierLevel) &&
        Objects.equals(this.attributes, tier.attributes) &&
        Objects.equals(this.requestCount, tier.requestCount) &&
        Objects.equals(this.unitTime, tier.unitTime) &&
        Objects.equals(this.tierPlan, tier.tierPlan) &&
        Objects.equals(this.stopOnQuotaReach, tier.stopOnQuotaReach);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, tierLevel, attributes, requestCount, unitTime, tierPlan, stopOnQuotaReach);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TierDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    tierLevel: ").append(toIndentedString(tierLevel)).append("\n");
    sb.append("    attributes: ").append(toIndentedString(attributes)).append("\n");
    sb.append("    requestCount: ").append(toIndentedString(requestCount)).append("\n");
    sb.append("    unitTime: ").append(toIndentedString(unitTime)).append("\n");
    sb.append("    tierPlan: ").append(toIndentedString(tierPlan)).append("\n");
    sb.append("    stopOnQuotaReach: ").append(toIndentedString(stopOnQuotaReach)).append("\n");
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

