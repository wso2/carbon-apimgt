package org.wso2.carbon.apimgt.rest.api.store.dto;

import java.util.Map;
import java.math.BigDecimal;
import java.util.*;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class TierDTO  {
  
  
  @NotNull
  private String name = null;
  
  
  private String description = null;
  
  
  private Map<String, String> attributes = new HashMap<String, String>();
  
  
  private BigDecimal requestCount = null;
  
  
  private BigDecimal unitTime = null;
  
  
  private String billingPlan = null;
  
  
  private Boolean continueOnQuotaReach = null;

  
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
   * Custom attributes added to the tier policy
   **/
  @ApiModelProperty(value = "Custom attributes added to the tier policy")
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
  @ApiModelProperty(value = "Maximum number of requests which can be sent within a provided unit time")
  @JsonProperty("requestCount")
  public BigDecimal getRequestCount() {
    return requestCount;
  }
  public void setRequestCount(BigDecimal requestCount) {
    this.requestCount = requestCount;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("unitTime")
  public BigDecimal getUnitTime() {
    return unitTime;
  }
  public void setUnitTime(BigDecimal unitTime) {
    this.unitTime = unitTime;
  }

  
  /**
   * This attribute declares whether this tier is available under commercial or free
   **/
  @ApiModelProperty(value = "This attribute declares whether this tier is available under commercial or free")
  @JsonProperty("billingPlan")
  public String getBillingPlan() {
    return billingPlan;
  }
  public void setBillingPlan(String billingPlan) {
    this.billingPlan = billingPlan;
  }

  
  /**
   * By making this attribute to true, you are capabale of sending requests \neven if the request count exceeded within a unit time
   **/
  @ApiModelProperty(value = "By making this attribute to true, you are capabale of sending requests \neven if the request count exceeded within a unit time")
  @JsonProperty("continueOnQuotaReach")
  public Boolean getContinueOnQuotaReach() {
    return continueOnQuotaReach;
  }
  public void setContinueOnQuotaReach(Boolean continueOnQuotaReach) {
    this.continueOnQuotaReach = continueOnQuotaReach;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class TierDTO {\n");
    
    sb.append("  name: ").append(name).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("  attributes: ").append(attributes).append("\n");
    sb.append("  requestCount: ").append(requestCount).append("\n");
    sb.append("  unitTime: ").append(unitTime).append("\n");
    sb.append("  billingPlan: ").append(billingPlan).append("\n");
    sb.append("  continueOnQuotaReach: ").append(continueOnQuotaReach).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
