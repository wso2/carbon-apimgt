package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.AIAPIQuotaLimitAllOfDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ThrottleLimitBaseDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class AIAPIQuotaLimitDTO   {
  
    private String timeUnit = null;
    private Integer unitTime = null;
    private Long requestCount = null;
    private Long totalTokenCount = null;
    private Long promptTokenCount = null;
    private Long completionTokenCount = null;

  /**
   * Unit of the time. Allowed values are \&quot;sec\&quot;, \&quot;min\&quot;, \&quot;hour\&quot;, \&quot;day\&quot;
   **/
  public AIAPIQuotaLimitDTO timeUnit(String timeUnit) {
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
  public AIAPIQuotaLimitDTO unitTime(Integer unitTime) {
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
  public AIAPIQuotaLimitDTO requestCount(Long requestCount) {
    this.requestCount = requestCount;
    return this;
  }

  
  @ApiModelProperty(example = "300", required = true, value = "Maximum number of requests allowed")
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
  public AIAPIQuotaLimitDTO totalTokenCount(Long totalTokenCount) {
    this.totalTokenCount = totalTokenCount;
    return this;
  }

  
  @ApiModelProperty(example = "800", value = "Maximum number of total tokens allowed")
  @JsonProperty("totalTokenCount")
  public Long getTotalTokenCount() {
    return totalTokenCount;
  }
  public void setTotalTokenCount(Long totalTokenCount) {
    this.totalTokenCount = totalTokenCount;
  }

  /**
   * Maximum number of prompt tokens allowed
   **/
  public AIAPIQuotaLimitDTO promptTokenCount(Long promptTokenCount) {
    this.promptTokenCount = promptTokenCount;
    return this;
  }

  
  @ApiModelProperty(example = "400", value = "Maximum number of prompt tokens allowed")
  @JsonProperty("promptTokenCount")
  public Long getPromptTokenCount() {
    return promptTokenCount;
  }
  public void setPromptTokenCount(Long promptTokenCount) {
    this.promptTokenCount = promptTokenCount;
  }

  /**
   * Maximum number of completion tokens allowed
   **/
  public AIAPIQuotaLimitDTO completionTokenCount(Long completionTokenCount) {
    this.completionTokenCount = completionTokenCount;
    return this;
  }

  
  @ApiModelProperty(example = "500", value = "Maximum number of completion tokens allowed")
  @JsonProperty("completionTokenCount")
  public Long getCompletionTokenCount() {
    return completionTokenCount;
  }
  public void setCompletionTokenCount(Long completionTokenCount) {
    this.completionTokenCount = completionTokenCount;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AIAPIQuotaLimitDTO aiAPIQuotaLimit = (AIAPIQuotaLimitDTO) o;
    return Objects.equals(timeUnit, aiAPIQuotaLimit.timeUnit) &&
        Objects.equals(unitTime, aiAPIQuotaLimit.unitTime) &&
        Objects.equals(requestCount, aiAPIQuotaLimit.requestCount) &&
        Objects.equals(totalTokenCount, aiAPIQuotaLimit.totalTokenCount) &&
        Objects.equals(promptTokenCount, aiAPIQuotaLimit.promptTokenCount) &&
        Objects.equals(completionTokenCount, aiAPIQuotaLimit.completionTokenCount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(timeUnit, unitTime, requestCount, totalTokenCount, promptTokenCount, completionTokenCount);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AIAPIQuotaLimitDTO {\n");
    
    sb.append("    timeUnit: ").append(toIndentedString(timeUnit)).append("\n");
    sb.append("    unitTime: ").append(toIndentedString(unitTime)).append("\n");
    sb.append("    requestCount: ").append(toIndentedString(requestCount)).append("\n");
    sb.append("    totalTokenCount: ").append(toIndentedString(totalTokenCount)).append("\n");
    sb.append("    promptTokenCount: ").append(toIndentedString(promptTokenCount)).append("\n");
    sb.append("    completionTokenCount: ").append(toIndentedString(completionTokenCount)).append("\n");
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

