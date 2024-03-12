package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

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



public class ApiChatExecuteResponseResourceDTO   {
  
    private String method = null;
    private String path = null;
    private Object inputs = null;

  /**
   * HTTP method of the resource
   **/
  public ApiChatExecuteResponseResourceDTO method(String method) {
    this.method = method;
    return this;
  }

  
  @ApiModelProperty(value = "HTTP method of the resource")
  @JsonProperty("method")
  public String getMethod() {
    return method;
  }
  public void setMethod(String method) {
    this.method = method;
  }

  /**
   * Path of the resource
   **/
  public ApiChatExecuteResponseResourceDTO path(String path) {
    this.path = path;
    return this;
  }

  
  @ApiModelProperty(value = "Path of the resource")
  @JsonProperty("path")
  public String getPath() {
    return path;
  }
  public void setPath(String path) {
    this.path = path;
  }

  /**
   * Input parameters for the resource
   **/
  public ApiChatExecuteResponseResourceDTO inputs(Object inputs) {
    this.inputs = inputs;
    return this;
  }

  
  @ApiModelProperty(value = "Input parameters for the resource")
      @Valid
  @JsonProperty("inputs")
  public Object getInputs() {
    return inputs;
  }
  public void setInputs(Object inputs) {
    this.inputs = inputs;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiChatExecuteResponseResourceDTO apiChatExecuteResponseResource = (ApiChatExecuteResponseResourceDTO) o;
    return Objects.equals(method, apiChatExecuteResponseResource.method) &&
        Objects.equals(path, apiChatExecuteResponseResource.path) &&
        Objects.equals(inputs, apiChatExecuteResponseResource.inputs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(method, path, inputs);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiChatExecuteResponseResourceDTO {\n");
    
    sb.append("    method: ").append(toIndentedString(method)).append("\n");
    sb.append("    path: ").append(toIndentedString(path)).append("\n");
    sb.append("    inputs: ").append(toIndentedString(inputs)).append("\n");
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

