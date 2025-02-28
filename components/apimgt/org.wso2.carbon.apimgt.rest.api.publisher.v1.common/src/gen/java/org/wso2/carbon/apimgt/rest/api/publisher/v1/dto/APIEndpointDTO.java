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



public class APIEndpointDTO   {
  
    private String id = null;
    private String name = null;
    private String deploymentStage = null;
    private Object endpointConfig = null;

  /**
   **/
  public APIEndpointDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "57a380b7-d852-4f56-bb23-db172722e9d4", value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public APIEndpointDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "Endpoint1", required = true, value = "")
  @JsonProperty("name")
  @NotNull
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public APIEndpointDTO deploymentStage(String deploymentStage) {
    this.deploymentStage = deploymentStage;
    return this;
  }

  
  @ApiModelProperty(example = "PRODUCTION or SANDBOX", value = "")
  @JsonProperty("deploymentStage")
  public String getDeploymentStage() {
    return deploymentStage;
  }
  public void setDeploymentStage(String deploymentStage) {
    this.deploymentStage = deploymentStage;
  }

  /**
   * Endpoint configuration of the API. This can be used to provide different types of endpoints including Simple REST Endpoints, Loadbalanced and Failover. 
   **/
  public APIEndpointDTO endpointConfig(Object endpointConfig) {
    this.endpointConfig = endpointConfig;
    return this;
  }

  
  @ApiModelProperty(value = "Endpoint configuration of the API. This can be used to provide different types of endpoints including Simple REST Endpoints, Loadbalanced and Failover. ")
      @Valid
  @JsonProperty("endpointConfig")
  public Object getEndpointConfig() {
    return endpointConfig;
  }
  public void setEndpointConfig(Object endpointConfig) {
    this.endpointConfig = endpointConfig;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIEndpointDTO apIEndpoint = (APIEndpointDTO) o;
    return Objects.equals(id, apIEndpoint.id) &&
        Objects.equals(name, apIEndpoint.name) &&
        Objects.equals(deploymentStage, apIEndpoint.deploymentStage) &&
        Objects.equals(endpointConfig, apIEndpoint.endpointConfig);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, deploymentStage, endpointConfig);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIEndpointDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    deploymentStage: ").append(toIndentedString(deploymentStage)).append("\n");
    sb.append("    endpointConfig: ").append(toIndentedString(endpointConfig)).append("\n");
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

