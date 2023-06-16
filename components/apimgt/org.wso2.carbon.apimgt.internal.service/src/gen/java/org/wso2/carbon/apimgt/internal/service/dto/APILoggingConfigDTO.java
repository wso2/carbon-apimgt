package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class APILoggingConfigDTO   {
  
    private String context = null;
    private String logLevel = null;
    private String resourceMethod = null;
    private String resourcePath = null;

  /**
   **/
  public APILoggingConfigDTO context(String context) {
    this.context = context;
    return this;
  }

  
  @ApiModelProperty(example = "pizashack/v1.0.0", required = true, value = "")
  @JsonProperty("context")
  @NotNull
  public String getContext() {
    return context;
  }
  public void setContext(String context) {
    this.context = context;
  }

  /**
   **/
  public APILoggingConfigDTO logLevel(String logLevel) {
    this.logLevel = logLevel;
    return this;
  }

  
  @ApiModelProperty(example = "all", required = true, value = "")
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
  public APILoggingConfigDTO resourceMethod(String resourceMethod) {
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
  public APILoggingConfigDTO resourcePath(String resourcePath) {
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
    APILoggingConfigDTO apILoggingConfig = (APILoggingConfigDTO) o;
    return Objects.equals(context, apILoggingConfig.context) &&
        Objects.equals(logLevel, apILoggingConfig.logLevel) &&
        Objects.equals(resourceMethod, apILoggingConfig.resourceMethod) &&
        Objects.equals(resourcePath, apILoggingConfig.resourcePath);
  }

  @Override
  public int hashCode() {
    return Objects.hash(context, logLevel, resourceMethod, resourcePath);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APILoggingConfigDTO {\n");
    
    sb.append("    context: ").append(toIndentedString(context)).append("\n");
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

