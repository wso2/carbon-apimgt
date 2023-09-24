package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ApplicationThrottlePolicyAllOfDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ThrottleLimitDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ThrottlePolicyDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class ApplicationThrottlePolicyDTO extends ThrottlePolicyDTO  {
  
    private ThrottleLimitDTO defaultLimit = null;
    private Integer rateLimitCount = null;
    private String rateLimitTimeUnit = null;

  /**
   **/
  public ApplicationThrottlePolicyDTO defaultLimit(ThrottleLimitDTO defaultLimit) {
    this.defaultLimit = defaultLimit;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
      @Valid
  @JsonProperty("defaultLimit")
  @NotNull
  public ThrottleLimitDTO getDefaultLimit() {
    return defaultLimit;
  }
  public void setDefaultLimit(ThrottleLimitDTO defaultLimit) {
    this.defaultLimit = defaultLimit;
  }

  /**
   * Burst control request count
   **/
  public ApplicationThrottlePolicyDTO rateLimitCount(Integer rateLimitCount) {
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
  public ApplicationThrottlePolicyDTO rateLimitTimeUnit(String rateLimitTimeUnit) {
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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationThrottlePolicyDTO applicationThrottlePolicy = (ApplicationThrottlePolicyDTO) o;
    return Objects.equals(defaultLimit, applicationThrottlePolicy.defaultLimit) &&
        Objects.equals(rateLimitCount, applicationThrottlePolicy.rateLimitCount) &&
        Objects.equals(rateLimitTimeUnit, applicationThrottlePolicy.rateLimitTimeUnit);
  }

  @Override
  public int hashCode() {
    return Objects.hash(defaultLimit, rateLimitCount, rateLimitTimeUnit);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationThrottlePolicyDTO {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    defaultLimit: ").append(toIndentedString(defaultLimit)).append("\n");
    sb.append("    rateLimitCount: ").append(toIndentedString(rateLimitCount)).append("\n");
    sb.append("    rateLimitTimeUnit: ").append(toIndentedString(rateLimitTimeUnit)).append("\n");
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

