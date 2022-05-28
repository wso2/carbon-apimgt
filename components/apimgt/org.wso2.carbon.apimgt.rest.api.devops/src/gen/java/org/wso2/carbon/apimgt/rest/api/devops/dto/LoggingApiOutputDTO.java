package org.wso2.carbon.apimgt.rest.api.devops.dto;

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



public class LoggingApiOutputDTO   {
  
    private String context = null;
    private String logLevel = null;
    private String apiId = null;

  /**
   **/
  public LoggingApiOutputDTO context(String context) {
    this.context = context;
    return this;
  }

  
  @ApiModelProperty(example = "pizashack/v1.0.0", value = "")
  @JsonProperty("context")
  public String getContext() {
    return context;
  }
  public void setContext(String context) {
    this.context = context;
  }

  /**
   **/
  public LoggingApiOutputDTO logLevel(String logLevel) {
    this.logLevel = logLevel;
    return this;
  }

  
  @ApiModelProperty(example = "FULL", value = "")
  @JsonProperty("logLevel")
  public String getLogLevel() {
    return logLevel;
  }
  public void setLogLevel(String logLevel) {
    this.logLevel = logLevel;
  }

  /**
   **/
  public LoggingApiOutputDTO apiId(String apiId) {
    this.apiId = apiId;
    return this;
  }

  
  @ApiModelProperty(example = "12d6e73c-778d-45ac-b57d-117c6c5092a4", value = "")
  @JsonProperty("apiId")
  public String getApiId() {
    return apiId;
  }
  public void setApiId(String apiId) {
    this.apiId = apiId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LoggingApiOutputDTO loggingApiOutput = (LoggingApiOutputDTO) o;
    return Objects.equals(context, loggingApiOutput.context) &&
        Objects.equals(logLevel, loggingApiOutput.logLevel) &&
        Objects.equals(apiId, loggingApiOutput.apiId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(context, logLevel, apiId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LoggingApiOutputDTO {\n");
    
    sb.append("    context: ").append(toIndentedString(context)).append("\n");
    sb.append("    logLevel: ").append(toIndentedString(logLevel)).append("\n");
    sb.append("    apiId: ").append(toIndentedString(apiId)).append("\n");
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

