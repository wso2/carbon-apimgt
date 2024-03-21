package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.HttpToolDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class EnrichedAPISpecDTO   {
  
    private String serviceUrl = null;
    private List<HttpToolDTO> tools = new ArrayList<HttpToolDTO>();

  /**
   * Extracted service URL from the OpenAPI specification if there is any
   **/
  public EnrichedAPISpecDTO serviceUrl(String serviceUrl) {
    this.serviceUrl = serviceUrl;
    return this;
  }

  
  @ApiModelProperty(value = "Extracted service URL from the OpenAPI specification if there is any")
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
  public EnrichedAPISpecDTO tools(List<HttpToolDTO> tools) {
    this.tools = tools;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Extracted Http tools from the OpenAPI specification")
      @Valid
  @JsonProperty("tools")
  @NotNull
  public List<HttpToolDTO> getTools() {
    return tools;
  }
  public void setTools(List<HttpToolDTO> tools) {
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
    EnrichedAPISpecDTO enrichedAPISpec = (EnrichedAPISpecDTO) o;
    return Objects.equals(serviceUrl, enrichedAPISpec.serviceUrl) &&
        Objects.equals(tools, enrichedAPISpec.tools);
  }

  @Override
  public int hashCode() {
    return Objects.hash(serviceUrl, tools);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EnrichedAPISpecDTO {\n");
    
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

