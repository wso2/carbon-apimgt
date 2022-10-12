package org.wso2.apk.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.*;

import java.util.Objects;

import javax.xml.bind.annotation.*;

import javax.validation.Valid;



public class ThrottlingPolicyDTO   {
  
    private String name = null;
    private String description = null;

    @XmlType(name="PolicyLevelEnum")
    @XmlEnum(String.class)
    public enum PolicyLevelEnum {
        APPLICATION("application"),
        SUBSCRIPTION("subscription");
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
    private Map<String, String> attributes = new HashMap<String, String>();
    private Long requestCount = null;
    private String dataUnit = null;
    private Long unitTime = null;
    private String timeUnit = null;
    private Integer rateLimitCount = 0;
    private String rateLimitTimeUnit = null;

    @XmlType(name="QuotaPolicyTypeEnum")
    @XmlEnum(String.class)
    public enum QuotaPolicyTypeEnum {
        REQUESTCOUNT("REQUESTCOUNT"),
        BANDWIDTHVOLUME("BANDWIDTHVOLUME");
        private String value;

        QuotaPolicyTypeEnum (String v) {
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
        public static QuotaPolicyTypeEnum fromValue(String v) {
            for (QuotaPolicyTypeEnum b : QuotaPolicyTypeEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private QuotaPolicyTypeEnum quotaPolicyType = null;

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
    private MonetizationInfoDTO monetizationAttributes = null;
    private ThrottlingPolicyPermissionInfoDTO throttlingPolicyPermissions = null;

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

  
  @ApiModelProperty(example = "subscription", value = "")
  @JsonProperty("policyLevel")
  public PolicyLevelEnum getPolicyLevel() {
    return policyLevel;
  }
  public void setPolicyLevel(PolicyLevelEnum policyLevel) {
    this.policyLevel = policyLevel;
  }

  /**
   * Custom attributes added to the throttling policy 
   **/
  public ThrottlingPolicyDTO attributes(Map<String, String> attributes) {
    this.attributes = attributes;
    return this;
  }

  
  @ApiModelProperty(example = "{}", value = "Custom attributes added to the throttling policy ")
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
   * Unit of data allowed to be transfered. Allowed values are \&quot;KB\&quot;, \&quot;MB\&quot; and \&quot;GB\&quot; 
   **/
  public ThrottlingPolicyDTO dataUnit(String dataUnit) {
    this.dataUnit = dataUnit;
    return this;
  }

  
  @ApiModelProperty(example = "KB", value = "Unit of data allowed to be transfered. Allowed values are \"KB\", \"MB\" and \"GB\" ")
  @JsonProperty("dataUnit")
  public String getDataUnit() {
    return dataUnit;
  }
  public void setDataUnit(String dataUnit) {
    this.dataUnit = dataUnit;
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
   * Burst control request count
   **/
  public ThrottlingPolicyDTO rateLimitCount(Integer rateLimitCount) {
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
  public ThrottlingPolicyDTO rateLimitTimeUnit(String rateLimitTimeUnit) {
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
   * Default quota limit type
   **/
  public ThrottlingPolicyDTO quotaPolicyType(QuotaPolicyTypeEnum quotaPolicyType) {
    this.quotaPolicyType = quotaPolicyType;
    return this;
  }

  
  @ApiModelProperty(example = "REQUESTCOUNT", value = "Default quota limit type")
  @JsonProperty("quotaPolicyType")
  public QuotaPolicyTypeEnum getQuotaPolicyType() {
    return quotaPolicyType;
  }
  public void setQuotaPolicyType(QuotaPolicyTypeEnum quotaPolicyType) {
    this.quotaPolicyType = quotaPolicyType;
  }

  /**
   * This attribute declares whether this tier is available under commercial or free 
   **/
  public ThrottlingPolicyDTO tierPlan(TierPlanEnum tierPlan) {
    this.tierPlan = tierPlan;
    return this;
  }

  
  @ApiModelProperty(example = "FREE", required = true, value = "This attribute declares whether this tier is available under commercial or free ")
  @JsonProperty("tierPlan")
  @NotNull
  public TierPlanEnum getTierPlan() {
    return tierPlan;
  }
  public void setTierPlan(TierPlanEnum tierPlan) {
    this.tierPlan = tierPlan;
  }

  /**
   * If this attribute is set to false, you are capabale of sending requests even if the request count exceeded within a unit time 
   **/
  public ThrottlingPolicyDTO stopOnQuotaReach(Boolean stopOnQuotaReach) {
    this.stopOnQuotaReach = stopOnQuotaReach;
    return this;
  }

  
  @ApiModelProperty(example = "true", required = true, value = "If this attribute is set to false, you are capabale of sending requests even if the request count exceeded within a unit time ")
  @JsonProperty("stopOnQuotaReach")
  @NotNull
  public Boolean isStopOnQuotaReach() {
    return stopOnQuotaReach;
  }
  public void setStopOnQuotaReach(Boolean stopOnQuotaReach) {
    this.stopOnQuotaReach = stopOnQuotaReach;
  }

  /**
   **/
  public ThrottlingPolicyDTO monetizationAttributes(MonetizationInfoDTO monetizationAttributes) {
    this.monetizationAttributes = monetizationAttributes;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("monetizationAttributes")
  public MonetizationInfoDTO getMonetizationAttributes() {
    return monetizationAttributes;
  }
  public void setMonetizationAttributes(MonetizationInfoDTO monetizationAttributes) {
    this.monetizationAttributes = monetizationAttributes;
  }

  /**
   **/
  public ThrottlingPolicyDTO throttlingPolicyPermissions(ThrottlingPolicyPermissionInfoDTO throttlingPolicyPermissions) {
    this.throttlingPolicyPermissions = throttlingPolicyPermissions;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("throttlingPolicyPermissions")
  public ThrottlingPolicyPermissionInfoDTO getThrottlingPolicyPermissions() {
    return throttlingPolicyPermissions;
  }
  public void setThrottlingPolicyPermissions(ThrottlingPolicyPermissionInfoDTO throttlingPolicyPermissions) {
    this.throttlingPolicyPermissions = throttlingPolicyPermissions;
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
        Objects.equals(attributes, throttlingPolicy.attributes) &&
        Objects.equals(requestCount, throttlingPolicy.requestCount) &&
        Objects.equals(dataUnit, throttlingPolicy.dataUnit) &&
        Objects.equals(unitTime, throttlingPolicy.unitTime) &&
        Objects.equals(timeUnit, throttlingPolicy.timeUnit) &&
        Objects.equals(rateLimitCount, throttlingPolicy.rateLimitCount) &&
        Objects.equals(rateLimitTimeUnit, throttlingPolicy.rateLimitTimeUnit) &&
        Objects.equals(quotaPolicyType, throttlingPolicy.quotaPolicyType) &&
        Objects.equals(tierPlan, throttlingPolicy.tierPlan) &&
        Objects.equals(stopOnQuotaReach, throttlingPolicy.stopOnQuotaReach) &&
        Objects.equals(monetizationAttributes, throttlingPolicy.monetizationAttributes) &&
        Objects.equals(throttlingPolicyPermissions, throttlingPolicy.throttlingPolicyPermissions);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, policyLevel, attributes, requestCount, dataUnit, unitTime, timeUnit, rateLimitCount, rateLimitTimeUnit, quotaPolicyType, tierPlan, stopOnQuotaReach, monetizationAttributes, throttlingPolicyPermissions);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ThrottlingPolicyDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    policyLevel: ").append(toIndentedString(policyLevel)).append("\n");
    sb.append("    attributes: ").append(toIndentedString(attributes)).append("\n");
    sb.append("    requestCount: ").append(toIndentedString(requestCount)).append("\n");
    sb.append("    dataUnit: ").append(toIndentedString(dataUnit)).append("\n");
    sb.append("    unitTime: ").append(toIndentedString(unitTime)).append("\n");
    sb.append("    timeUnit: ").append(toIndentedString(timeUnit)).append("\n");
    sb.append("    rateLimitCount: ").append(toIndentedString(rateLimitCount)).append("\n");
    sb.append("    rateLimitTimeUnit: ").append(toIndentedString(rateLimitTimeUnit)).append("\n");
    sb.append("    quotaPolicyType: ").append(toIndentedString(quotaPolicyType)).append("\n");
    sb.append("    tierPlan: ").append(toIndentedString(tierPlan)).append("\n");
    sb.append("    stopOnQuotaReach: ").append(toIndentedString(stopOnQuotaReach)).append("\n");
    sb.append("    monetizationAttributes: ").append(toIndentedString(monetizationAttributes)).append("\n");
    sb.append("    throttlingPolicyPermissions: ").append(toIndentedString(throttlingPolicyPermissions)).append("\n");
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

