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



public class BackendDTO   {
  
    private String id = null;
    private String name = null;
    private Object endpointConfig = null;
    private String definition = null;

  /**
   * Backend API ID consisting of the UUID of the Endpoint
   **/
  public BackendDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "0c4439fd-9416-3c2e-be6e-1086e0b9aa93", value = "Backend API ID consisting of the UUID of the Endpoint")
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
  public BackendDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(value = "Backend name")
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
  public BackendDTO endpointConfig(Object endpointConfig) {
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
   * Definition of the backend
   **/
  public BackendDTO definition(String definition) {
    this.definition = definition;
    return this;
  }

  
  @ApiModelProperty(value = "Definition of the backend")
  @JsonProperty("definition")
  public String getDefinition() {
    return definition;
  }
  public void setDefinition(String definition) {
    this.definition = definition;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BackendDTO backend = (BackendDTO) o;
    return Objects.equals(id, backend.id) &&
        Objects.equals(name, backend.name) &&
        Objects.equals(endpointConfig, backend.endpointConfig) &&
        Objects.equals(definition, backend.definition);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, endpointConfig, definition);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BackendDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    endpointConfig: ").append(toIndentedString(endpointConfig)).append("\n");
    sb.append("    definition: ").append(toIndentedString(definition)).append("\n");
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

