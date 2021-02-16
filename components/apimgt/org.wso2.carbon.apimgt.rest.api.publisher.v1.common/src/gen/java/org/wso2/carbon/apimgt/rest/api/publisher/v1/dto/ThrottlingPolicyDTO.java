package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class ThrottlingPolicyDTO   {
  
    private String name = null;
    private String description = null;

    @XmlType(name="PolicyLevelEnum")
    @XmlEnum(String.class)
    public enum PolicyLevelEnum {
        SUBSCRIPTION("subscription"),
        API("api");
        private String value;

        PolicyLevelEnum (String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static PolicyLevelEnum fromValue(String v) {
            for (PolicyLevelEnum b : PolicyLevelEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private PolicyLevelEnum policyLevel = null;
    private String displayName = null;
    private Map<String, String> attributes = new HashMap<String, String>();
    private Long requestCount = null;
    private Long unitTime = null;
    private String timeUnit = null;

    @XmlType(name="TierPlanEnum")
    @XmlEnum(String.class)
    public enum TierPlanEnum {
        FREE("FREE"),
        COMMERCIAL("COMMERCIAL");
        private String value;

        TierPlanEnum (String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static TierPlanEnum fromValue(String v) {
            for (TierPlanEnum b : TierPlanEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private TierPlanEnum tierPlan = null;
    private Boolean stopOnQuotaReach = null;
    private Map<String, String> monetizationProperties = new HashMap<String, String>();

  /**
   **/
  public ThrottlingPolicyDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "Platinum", required = true, value = "")
  @JsonProperty("name")
  @NotNull
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public ThrottlingPolicyDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "Allows 50 request(s) per minute.", value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   **/
  public ThrottlingPolicyDTO policyLevel(PolicyLevelEnum policyLevel) {
    this.policyLevel = policyLevel;
    return this;
  }

  
  @ApiModelProperty(example = "api", value = "")
  @JsonProperty("policyLevel")
  public PolicyLevelEnum getPolicyLevel() {
    return policyLevel;
  }
  public void setPolicyLevel(PolicyLevelEnum policyLevel) {
    this.policyLevel = policyLevel;
  }

  /**
   **/
  public ThrottlingPolicyDTO displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(example = "Platinum", value = "")
  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * Custom attributes added to the policy policy 
   **/
  public ThrottlingPolicyDTO attributes(Map<String, String> attributes) {
    this.attributes = attributes;
    return this;
  }

  
  @ApiModelProperty(example = "{}", value = "Custom attributes added to the policy policy ")
  @JsonProperty("attributes")
  public Map<String, String> getAttributes() {
    return attributes;
  }
  public void setAttributes(Map<String, String> attributes) {
    this.attributes = attributes;
  }

  /**
   * Maximum number of requests which can be sent within a provided unit time 
   **/
  public ThrottlingPolicyDTO requestCount(Long requestCount) {
    this.requestCount = requestCount;
    return this;
  }

  
  @ApiModelProperty(example = "50", required = true, value = "Maximum number of requests which can be sent within a provided unit time ")
  @JsonProperty("requestCount")
  @NotNull
  public Long getRequestCount() {
    return requestCount;
  }
  public void setRequestCount(Long requestCount) {
    this.requestCount = requestCount;
  }

  /**
   **/
  public ThrottlingPolicyDTO unitTime(Long unitTime) {
    this.unitTime = unitTime;
    return this;
  }

  
  @ApiModelProperty(example = "60000", required = true, value = "")
  @JsonProperty("unitTime")
  @NotNull
  public Long getUnitTime() {
    return unitTime;
  }
  public void setUnitTime(Long unitTime) {
    this.unitTime = unitTime;
  }

  /**
   **/
  public ThrottlingPolicyDTO timeUnit(String timeUnit) {
    this.timeUnit = timeUnit;
    return this;
  }

  
  @ApiModelProperty(example = "min", value = "")
  @JsonProperty("timeUnit")
  public String getTimeUnit() {
    return timeUnit;
  }
  public void setTimeUnit(String timeUnit) {
    this.timeUnit = timeUnit;
  }

  /**
   * This attribute declares whether this policy is available under commercial or free 
   **/
  public ThrottlingPolicyDTO tierPlan(TierPlanEnum tierPlan) {
    this.tierPlan = tierPlan;
    return this;
  }

  
  @ApiModelProperty(example = "FREE", required = true, value = "This attribute declares whether this policy is available under commercial or free ")
  @JsonProperty("tierPlan")
  @NotNull
  public TierPlanEnum getTierPlan() {
    return tierPlan;
  }
  public void setTierPlan(TierPlanEnum tierPlan) {
    this.tierPlan = tierPlan;
  }

  /**
   * By making this attribute to false, you are capabale of sending requests even if the request count exceeded within a unit time 
   **/
  public ThrottlingPolicyDTO stopOnQuotaReach(Boolean stopOnQuotaReach) {
    this.stopOnQuotaReach = stopOnQuotaReach;
    return this;
  }

  
  @ApiModelProperty(example = "true", required = true, value = "By making this attribute to false, you are capabale of sending requests even if the request count exceeded within a unit time ")
  @JsonProperty("stopOnQuotaReach")
  @NotNull
  public Boolean isStopOnQuotaReach() {
    return stopOnQuotaReach;
  }
  public void setStopOnQuotaReach(Boolean stopOnQuotaReach) {
    this.stopOnQuotaReach = stopOnQuotaReach;
  }

  /**
   * Properties of a tier plan which are related to monetization
   **/
  public ThrottlingPolicyDTO monetizationProperties(Map<String, String> monetizationProperties) {
    this.monetizationProperties = monetizationProperties;
    return this;
  }

  
  @ApiModelProperty(example = "{}", value = "Properties of a tier plan which are related to monetization")
  @JsonProperty("monetizationProperties")
  public Map<String, String> getMonetizationProperties() {
    return monetizationProperties;
  }
  public void setMonetizationProperties(Map<String, String> monetizationProperties) {
    this.monetizationProperties = monetizationProperties;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ThrottlingPolicyDTO throttlingPolicy = (ThrottlingPolicyDTO) o;
    return Objects.equals(name, throttlingPolicy.name) &&
        Objects.equals(description, throttlingPolicy.description) &&
        Objects.equals(policyLevel, throttlingPolicy.policyLevel) &&
        Objects.equals(displayName, throttlingPolicy.displayName) &&
        Objects.equals(attributes, throttlingPolicy.attributes) &&
        Objects.equals(requestCount, throttlingPolicy.requestCount) &&
        Objects.equals(unitTime, throttlingPolicy.unitTime) &&
        Objects.equals(timeUnit, throttlingPolicy.timeUnit) &&
        Objects.equals(tierPlan, throttlingPolicy.tierPlan) &&
        Objects.equals(stopOnQuotaReach, throttlingPolicy.stopOnQuotaReach) &&
        Objects.equals(monetizationProperties, throttlingPolicy.monetizationProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, policyLevel, displayName, attributes, requestCount, unitTime, timeUnit, tierPlan, stopOnQuotaReach, monetizationProperties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ThrottlingPolicyDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    policyLevel: ").append(toIndentedString(policyLevel)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    attributes: ").append(toIndentedString(attributes)).append("\n");
    sb.append("    requestCount: ").append(toIndentedString(requestCount)).append("\n");
    sb.append("    unitTime: ").append(toIndentedString(unitTime)).append("\n");
    sb.append("    timeUnit: ").append(toIndentedString(timeUnit)).append("\n");
    sb.append("    tierPlan: ").append(toIndentedString(tierPlan)).append("\n");
    sb.append("    stopOnQuotaReach: ").append(toIndentedString(stopOnQuotaReach)).append("\n");
    sb.append("    monetizationProperties: ").append(toIndentedString(monetizationProperties)).append("\n");
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

