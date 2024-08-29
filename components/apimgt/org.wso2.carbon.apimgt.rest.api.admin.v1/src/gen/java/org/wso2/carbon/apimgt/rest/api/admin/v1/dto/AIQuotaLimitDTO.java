package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.AIQuotaLimitAllOfDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ThrottleLimitBaseDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class AIQuotaLimitDTO   {
  
    private String timeUnit = null;
    private Integer unitTime = null;
    private Long requestCount = null;
    private Long totalTokenCount = null;
    private Long requestTokenCount = null;
    private Long responseTokenCount = null;

  /**
   * Unit of the time. Allowed values are \&quot;sec\&quot;, \&quot;min\&quot;, \&quot;hour\&quot;, \&quot;day\&quot;
   **/
  public AIQuotaLimitDTO timeUnit(String timeUnit) {
    this.timeUnit = timeUnit;
    return this;
  }

  
  @ApiModelProperty(example = "min", required = true, value = "Unit of the time. Allowed values are \"sec\", \"min\", \"hour\", \"day\"")
  @JsonProperty("timeUnit")
  @NotNull
  public String getTimeUnit() {
    return timeUnit;
  }
  public void setTimeUnit(String timeUnit) {
    this.timeUnit = timeUnit;
  }

  /**
   * Time limit that the throttling limit applies.
   **/
  public AIQuotaLimitDTO unitTime(Integer unitTime) {
    this.unitTime = unitTime;
    return this;
  }

  
  @ApiModelProperty(example = "10", required = true, value = "Time limit that the throttling limit applies.")
  @JsonProperty("unitTime")
  @NotNull
  public Integer getUnitTime() {
    return unitTime;
  }
  public void setUnitTime(Integer unitTime) {
    this.unitTime = unitTime;
  }

  /**
   * Maximum number of requests allowed
   **/
  public AIQuotaLimitDTO requestCount(Long requestCount) {
    this.requestCount = requestCount;
    return this;
  }

  
  @ApiModelProperty(example = "30", required = true, value = "Maximum number of requests allowed")
  @JsonProperty("requestCount")
  @NotNull
  public Long getRequestCount() {
    return requestCount;
  }
  public void setRequestCount(Long requestCount) {
    this.requestCount = requestCount;
  }

  /**
   * Maximum number of total tokens allowed
   **/
  public AIQuotaLimitDTO totalTokenCount(Long totalTokenCount) {
    this.totalTokenCount = totalTokenCount;
    return this;
  }

  
  @ApiModelProperty(example = "1000", value = "Maximum number of total tokens allowed")
  @JsonProperty("totalTokenCount")
  public Long getTotalTokenCount() {
    return totalTokenCount;
  }
  public void setTotalTokenCount(Long totalTokenCount) {
    this.totalTokenCount = totalTokenCount;
  }

  /**
   * Maximum number of request tokens allowed
   **/
  public AIQuotaLimitDTO requestTokenCount(Long requestTokenCount) {
    this.requestTokenCount = requestTokenCount;
    return this;
  }

  
  @ApiModelProperty(example = "300", value = "Maximum number of request tokens allowed")
  @JsonProperty("requestTokenCount")
  public Long getRequestTokenCount() {
    return requestTokenCount;
  }
  public void setRequestTokenCount(Long requestTokenCount) {
    this.requestTokenCount = requestTokenCount;
  }

  /**
   * Maximum number of response tokens allowed
   **/
  public AIQuotaLimitDTO responseTokenCount(Long responseTokenCount) {
    this.responseTokenCount = responseTokenCount;
    return this;
  }

  
  @ApiModelProperty(example = "300", value = "Maximum number of response tokens allowed")
  @JsonProperty("responseTokenCount")
  public Long getResponseTokenCount() {
    return responseTokenCount;
  }
  public void setResponseTokenCount(Long responseTokenCount) {
    this.responseTokenCount = responseTokenCount;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AIQuotaLimitDTO aiQuotaLimit = (AIQuotaLimitDTO) o;
    return Objects.equals(timeUnit, aiQuotaLimit.timeUnit) &&
        Objects.equals(unitTime, aiQuotaLimit.unitTime) &&
        Objects.equals(requestCount, aiQuotaLimit.requestCount) &&
        Objects.equals(totalTokenCount, aiQuotaLimit.totalTokenCount) &&
        Objects.equals(requestTokenCount, aiQuotaLimit.requestTokenCount) &&
        Objects.equals(responseTokenCount, aiQuotaLimit.responseTokenCount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(timeUnit, unitTime, requestCount, totalTokenCount, requestTokenCount, responseTokenCount);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AIQuotaLimitDTO {\n");
    
    sb.append("    timeUnit: ").append(toIndentedString(timeUnit)).append("\n");
    sb.append("    unitTime: ").append(toIndentedString(unitTime)).append("\n");
    sb.append("    requestCount: ").append(toIndentedString(requestCount)).append("\n");
    sb.append("    totalTokenCount: ").append(toIndentedString(totalTokenCount)).append("\n");
    sb.append("    requestTokenCount: ").append(toIndentedString(requestTokenCount)).append("\n");
    sb.append("    responseTokenCount: ").append(toIndentedString(responseTokenCount)).append("\n");
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

