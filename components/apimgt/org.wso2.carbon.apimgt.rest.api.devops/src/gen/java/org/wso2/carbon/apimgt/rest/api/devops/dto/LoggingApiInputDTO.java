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



public class LoggingApiInputDTO   {
  
    private String logLevel = null;
    private String resourceMethod = null;
    private String resourcePath = null;

  /**
   **/
  public LoggingApiInputDTO logLevel(String logLevel) {
    this.logLevel = logLevel;
    return this;
  }

  
  @ApiModelProperty(example = "FULL", required = true, value = "")
  @JsonProperty("logLevel")
  @NotNull
  public String getLogLevel() {
    return logLevel;
  }
  public void setLogLevel(String logLevel) {
    this.logLevel = logLevel;
  }

  /**
   **/
  public LoggingApiInputDTO resourceMethod(String resourceMethod) {
    this.resourceMethod = resourceMethod;
    return this;
  }

  
  @ApiModelProperty(example = "GET", value = "")
  @JsonProperty("resourceMethod")
  public String getResourceMethod() {
    return resourceMethod;
  }
  public void setResourceMethod(String resourceMethod) {
    this.resourceMethod = resourceMethod;
  }

  /**
   **/
  public LoggingApiInputDTO resourcePath(String resourcePath) {
    this.resourcePath = resourcePath;
    return this;
  }

  
  @ApiModelProperty(example = "/v1.0.0/pizza", value = "")
  @JsonProperty("resourcePath")
  public String getResourcePath() {
    return resourcePath;
  }
  public void setResourcePath(String resourcePath) {
    this.resourcePath = resourcePath;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LoggingApiInputDTO loggingApiInput = (LoggingApiInputDTO) o;
    return Objects.equals(logLevel, loggingApiInput.logLevel) &&
        Objects.equals(resourceMethod, loggingApiInput.resourceMethod) &&
        Objects.equals(resourcePath, loggingApiInput.resourcePath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(logLevel, resourceMethod, resourcePath);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LoggingApiInputDTO {\n");
    
    sb.append("    logLevel: ").append(toIndentedString(logLevel)).append("\n");
    sb.append("    resourceMethod: ").append(toIndentedString(resourceMethod)).append("\n");
    sb.append("    resourcePath: ").append(toIndentedString(resourcePath)).append("\n");
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

