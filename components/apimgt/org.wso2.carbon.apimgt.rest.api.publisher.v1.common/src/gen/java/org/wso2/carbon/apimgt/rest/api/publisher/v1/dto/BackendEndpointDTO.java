package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

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



public class BackendEndpointDTO   {
  
    private String id = null;
    private String name = null;
    private String endpointConfig = null;
    private String apiDefinition = null;

  /**
   * Backend ID consisting of the UUID of the Endpoint
   **/
  public BackendEndpointDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "0c4439fd-9416-3c2e-be6e-1086e0b9aa93", value = "Backend ID consisting of the UUID of the Endpoint")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Backend name
   **/
  public BackendEndpointDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "backend1", value = "Backend name")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Endpoint configuration of the backend.
   **/
  public BackendEndpointDTO endpointConfig(String endpointConfig) {
    this.endpointConfig = endpointConfig;
    return this;
  }

  
  @ApiModelProperty(value = "Endpoint configuration of the backend.")
  @JsonProperty("endpointConfig")
  public String getEndpointConfig() {
    return endpointConfig;
  }
  public void setEndpointConfig(String endpointConfig) {
    this.endpointConfig = endpointConfig;
  }

  /**
   * OpenAPI specification of the backend API
   **/
  public BackendEndpointDTO apiDefinition(String apiDefinition) {
    this.apiDefinition = apiDefinition;
    return this;
  }

  
  @ApiModelProperty(value = "OpenAPI specification of the backend API")
  @JsonProperty("apiDefinition")
  public String getApiDefinition() {
    return apiDefinition;
  }
  public void setApiDefinition(String apiDefinition) {
    this.apiDefinition = apiDefinition;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BackendEndpointDTO backendEndpoint = (BackendEndpointDTO) o;
    return Objects.equals(id, backendEndpoint.id) &&
        Objects.equals(name, backendEndpoint.name) &&
        Objects.equals(endpointConfig, backendEndpoint.endpointConfig) &&
        Objects.equals(apiDefinition, backendEndpoint.apiDefinition);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, endpointConfig, apiDefinition);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BackendEndpointDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    endpointConfig: ").append(toIndentedString(endpointConfig)).append("\n");
    sb.append("    apiDefinition: ").append(toIndentedString(apiDefinition)).append("\n");
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

