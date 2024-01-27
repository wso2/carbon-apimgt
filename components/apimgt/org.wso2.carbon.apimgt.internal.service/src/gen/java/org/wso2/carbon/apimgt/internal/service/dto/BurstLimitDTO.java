package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class BurstLimitDTO   {
  
    private Integer rateLimitCount = null;
    private String rateLimitTimeUnit = null;

  /**
   * Burst control request count
   **/
  public BurstLimitDTO rateLimitCount(Integer rateLimitCount) {
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
  public BurstLimitDTO rateLimitTimeUnit(String rateLimitTimeUnit) {
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
    BurstLimitDTO burstLimit = (BurstLimitDTO) o;
    return Objects.equals(rateLimitCount, burstLimit.rateLimitCount) &&
        Objects.equals(rateLimitTimeUnit, burstLimit.rateLimitTimeUnit);
  }

  @Override
  public int hashCode() {
    return Objects.hash(rateLimitCount, rateLimitTimeUnit);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BurstLimitDTO {\n");
    
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

