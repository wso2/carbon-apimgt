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



public class AIQuotaLimitAllOfDTO   {
  
    private Long requestCount = null;
    private Long totalTokenCount = null;
    private Long requestTokenCount = null;
    private Long responseTokenCount = null;

  /**
   * Maximum number of requests allowed
   **/
  public AIQuotaLimitAllOfDTO requestCount(Long requestCount) {
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
  public AIQuotaLimitAllOfDTO totalTokenCount(Long totalTokenCount) {
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
  public AIQuotaLimitAllOfDTO requestTokenCount(Long requestTokenCount) {
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
  public AIQuotaLimitAllOfDTO responseTokenCount(Long responseTokenCount) {
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
    AIQuotaLimitAllOfDTO aiQuotaLimitAllOf = (AIQuotaLimitAllOfDTO) o;
    return Objects.equals(requestCount, aiQuotaLimitAllOf.requestCount) &&
        Objects.equals(totalTokenCount, aiQuotaLimitAllOf.totalTokenCount) &&
        Objects.equals(requestTokenCount, aiQuotaLimitAllOf.requestTokenCount) &&
        Objects.equals(responseTokenCount, aiQuotaLimitAllOf.responseTokenCount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requestCount, totalTokenCount, requestTokenCount, responseTokenCount);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AIQuotaLimitAllOfDTO {\n");
    
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

