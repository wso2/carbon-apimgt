package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.internal.service.dto.ThrottleLimitDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class SubscriptionPolicyAllOfDTO   {
  
    private Integer rateLimitCount = null;
    private String rateLimitTimeUnit = null;
    private Boolean stopOnQuotaReach = null;
    private ThrottleLimitDTO defaultLimit = null;

  /**
   **/
  public SubscriptionPolicyAllOfDTO rateLimitCount(Integer rateLimitCount) {
    this.rateLimitCount = rateLimitCount;
    return this;
  }

  
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
  public SubscriptionPolicyAllOfDTO rateLimitTimeUnit(String rateLimitTimeUnit) {
    this.rateLimitTimeUnit = rateLimitTimeUnit;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("rateLimitTimeUnit")
  public String getRateLimitTimeUnit() {
    return rateLimitTimeUnit;
  }
  public void setRateLimitTimeUnit(String rateLimitTimeUnit) {
    this.rateLimitTimeUnit = rateLimitTimeUnit;
  }

  /**
   **/
  public SubscriptionPolicyAllOfDTO stopOnQuotaReach(Boolean stopOnQuotaReach) {
    this.stopOnQuotaReach = stopOnQuotaReach;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("stopOnQuotaReach")
  public Boolean isStopOnQuotaReach() {
    return stopOnQuotaReach;
  }
  public void setStopOnQuotaReach(Boolean stopOnQuotaReach) {
    this.stopOnQuotaReach = stopOnQuotaReach;
  }

  /**
   **/
  public SubscriptionPolicyAllOfDTO defaultLimit(ThrottleLimitDTO defaultLimit) {
    this.defaultLimit = defaultLimit;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("defaultLimit")
  public ThrottleLimitDTO getDefaultLimit() {
    return defaultLimit;
  }
  public void setDefaultLimit(ThrottleLimitDTO defaultLimit) {
    this.defaultLimit = defaultLimit;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SubscriptionPolicyAllOfDTO subscriptionPolicyAllOf = (SubscriptionPolicyAllOfDTO) o;
    return Objects.equals(rateLimitCount, subscriptionPolicyAllOf.rateLimitCount) &&
        Objects.equals(rateLimitTimeUnit, subscriptionPolicyAllOf.rateLimitTimeUnit) &&
        Objects.equals(stopOnQuotaReach, subscriptionPolicyAllOf.stopOnQuotaReach) &&
        Objects.equals(defaultLimit, subscriptionPolicyAllOf.defaultLimit);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rateLimitCount, rateLimitTimeUnit, stopOnQuotaReach, defaultLimit);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubscriptionPolicyAllOfDTO {\n");
    
    sb.append("    rateLimitCount: ").append(toIndentedString(rateLimitCount)).append("\n");
    sb.append("    rateLimitTimeUnit: ").append(toIndentedString(rateLimitTimeUnit)).append("\n");
    sb.append("    stopOnQuotaReach: ").append(toIndentedString(stopOnQuotaReach)).append("\n");
    sb.append("    defaultLimit: ").append(toIndentedString(defaultLimit)).append("\n");
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

