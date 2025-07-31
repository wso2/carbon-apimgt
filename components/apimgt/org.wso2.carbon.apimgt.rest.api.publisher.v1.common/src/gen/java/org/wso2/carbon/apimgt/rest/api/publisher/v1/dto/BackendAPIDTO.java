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



public class BackendAPIDTO   {
  
    private String backendApiId = null;
    private String backendApiName = null;
    private Object endpointConfig = null;
    private String apiDefinition = null;

  /**
   * Backend API ID consisting of the UUID of the Endpoint
   **/
  public BackendAPIDTO backendApiId(String backendApiId) {
    this.backendApiId = backendApiId;
    return this;
  }

  
  @ApiModelProperty(example = "0c4439fd-9416-3c2e-be6e-1086e0b9aa93", value = "Backend API ID consisting of the UUID of the Endpoint")
  @JsonProperty("backendApiId")
  public String getBackendApiId() {
    return backendApiId;
  }
  public void setBackendApiId(String backendApiId) {
    this.backendApiId = backendApiId;
  }

  /**
   * Backend name
   **/
  public BackendAPIDTO backendApiName(String backendApiName) {
    this.backendApiName = backendApiName;
    return this;
  }

  
  @ApiModelProperty(value = "Backend name")
  @JsonProperty("backendApiName")
  public String getBackendApiName() {
    return backendApiName;
  }
  public void setBackendApiName(String backendApiName) {
    this.backendApiName = backendApiName;
  }

  /**
   * Endpoint configuration of the backend.
   **/
  public BackendAPIDTO endpointConfig(Object endpointConfig) {
    this.endpointConfig = endpointConfig;
    return this;
  }

  
  @ApiModelProperty(value = "Endpoint configuration of the backend.")
      @Valid
  @JsonProperty("endpointConfig")
  public Object getEndpointConfig() {
    return endpointConfig;
  }
  public void setEndpointConfig(Object endpointConfig) {
    this.endpointConfig = endpointConfig;
  }

  /**
   * OpenAPI specification of the backend API
   **/
  public BackendAPIDTO apiDefinition(String apiDefinition) {
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
    BackendAPIDTO backendAPI = (BackendAPIDTO) o;
    return Objects.equals(backendApiId, backendAPI.backendApiId) &&
        Objects.equals(backendApiName, backendAPI.backendApiName) &&
        Objects.equals(endpointConfig, backendAPI.endpointConfig) &&
        Objects.equals(apiDefinition, backendAPI.apiDefinition);
  }

  @Override
  public int hashCode() {
    return Objects.hash(backendApiId, backendApiName, endpointConfig, apiDefinition);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BackendAPIDTO {\n");
    
    sb.append("    backendApiId: ").append(toIndentedString(backendApiId)).append("\n");
    sb.append("    backendApiName: ").append(toIndentedString(backendApiName)).append("\n");
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

