package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

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



public class AIAPIQuotaLimitAllOfDTO   {
  
    private Long requestCount = null;
    private Long totalTokenCount = null;
    private Long promptTokenCount = null;
    private Long completionTokenCount = null;

  /**
   * Maximum number of requests allowed
   **/
  public AIAPIQuotaLimitAllOfDTO requestCount(Long requestCount) {
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
  public AIAPIQuotaLimitAllOfDTO totalTokenCount(Long totalTokenCount) {
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
  public AIAPIQuotaLimitAllOfDTO promptTokenCount(Long promptTokenCount) {
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
  public AIAPIQuotaLimitAllOfDTO completionTokenCount(Long completionTokenCount) {
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
    AIAPIQuotaLimitAllOfDTO aiAPIQuotaLimitAllOf = (AIAPIQuotaLimitAllOfDTO) o;
    return Objects.equals(requestCount, aiAPIQuotaLimitAllOf.requestCount) &&
        Objects.equals(totalTokenCount, aiAPIQuotaLimitAllOf.totalTokenCount) &&
        Objects.equals(promptTokenCount, aiAPIQuotaLimitAllOf.promptTokenCount) &&
        Objects.equals(completionTokenCount, aiAPIQuotaLimitAllOf.completionTokenCount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requestCount, totalTokenCount, promptTokenCount, completionTokenCount);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AIAPIQuotaLimitAllOfDTO {\n");
    
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

