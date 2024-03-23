package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class ApiChatRequestApiSpecDTO   {
  
    private String serviceUrl = null;
    private List<Object> tools = new ArrayList<Object>();

  /**
   * Service URL of API if any
   **/
  public ApiChatRequestApiSpecDTO serviceUrl(String serviceUrl) {
    this.serviceUrl = serviceUrl;
    return this;
  }

  
  @ApiModelProperty(example = "https://localhost:8243/pizzashack/1.0.0", value = "Service URL of API if any")
  @JsonProperty("serviceUrl")
  public String getServiceUrl() {
    return serviceUrl;
  }
  public void setServiceUrl(String serviceUrl) {
    this.serviceUrl = serviceUrl;
  }

  /**
   * Extracted Http tools from the OpenAPI specification
   **/
  public ApiChatRequestApiSpecDTO tools(List<Object> tools) {
    this.tools = tools;
    return this;
  }

  
  @ApiModelProperty(value = "Extracted Http tools from the OpenAPI specification")
  @JsonProperty("tools")
  public List<Object> getTools() {
    return tools;
  }
  public void setTools(List<Object> tools) {
    this.tools = tools;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiChatRequestApiSpecDTO apiChatRequestApiSpec = (ApiChatRequestApiSpecDTO) o;
    return Objects.equals(serviceUrl, apiChatRequestApiSpec.serviceUrl) &&
        Objects.equals(tools, apiChatRequestApiSpec.tools);
  }

  @Override
  public int hashCode() {
    return Objects.hash(serviceUrl, tools);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiChatRequestApiSpecDTO {\n");
    
    sb.append("    serviceUrl: ").append(toIndentedString(serviceUrl)).append("\n");
    sb.append("    tools: ").append(toIndentedString(tools)).append("\n");
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

