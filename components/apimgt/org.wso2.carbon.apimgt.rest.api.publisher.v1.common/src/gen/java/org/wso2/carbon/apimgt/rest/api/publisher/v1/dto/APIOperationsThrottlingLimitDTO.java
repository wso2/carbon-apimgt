package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class APIOperationsThrottlingLimitDTO   {
  
    private Integer requestCount = null;
    private String unit = null;

  /**
   **/
  public APIOperationsThrottlingLimitDTO requestCount(Integer requestCount) {
    this.requestCount = requestCount;
    return this;
  }

  
  @ApiModelProperty(example = "10000", value = "")
  @JsonProperty("requestCount")
  public Integer getRequestCount() {
    return requestCount;
  }
  public void setRequestCount(Integer requestCount) {
    this.requestCount = requestCount;
  }

  /**
   **/
  public APIOperationsThrottlingLimitDTO unit(String unit) {
    this.unit = unit;
    return this;
  }

  
  @ApiModelProperty(example = "min", value = "")
  @JsonProperty("unit")
  public String getUnit() {
    return unit;
  }
  public void setUnit(String unit) {
    this.unit = unit;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIOperationsThrottlingLimitDTO apIOperationsThrottlingLimit = (APIOperationsThrottlingLimitDTO) o;
    return Objects.equals(requestCount, apIOperationsThrottlingLimit.requestCount) &&
        Objects.equals(unit, apIOperationsThrottlingLimit.unit);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requestCount, unit);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIOperationsThrottlingLimitDTO {\n");
    
    sb.append("    requestCount: ").append(toIndentedString(requestCount)).append("\n");
    sb.append("    unit: ").append(toIndentedString(unit)).append("\n");
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

