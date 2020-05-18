package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;

/**
 * API definition information 
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;

@ApiModel(description = "API definition information ")

public class OpenAPIDefinitionValidationResponseInfoDTO   {
  
    private String name = null;
    private String version = null;
    private String context = null;
    private String description = null;
    private String openAPIVersion = null;
    private List<String> endpoints = new ArrayList<>();

  /**
   **/
  public OpenAPIDefinitionValidationResponseInfoDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "PetStore", value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public OpenAPIDefinitionValidationResponseInfoDTO version(String version) {
    this.version = version;
    return this;
  }

  
  @ApiModelProperty(example = "1.0.0", value = "")
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   **/
  public OpenAPIDefinitionValidationResponseInfoDTO context(String context) {
    this.context = context;
    return this;
  }

  
  @ApiModelProperty(example = "/petstore", value = "")
  @JsonProperty("context")
  public String getContext() {
    return context;
  }
  public void setContext(String context) {
    this.context = context;
  }

  /**
   **/
  public OpenAPIDefinitionValidationResponseInfoDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "A sample API that uses a petstore as an example to demonstrate swagger-2.0 specification", value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   **/
  public OpenAPIDefinitionValidationResponseInfoDTO openAPIVersion(String openAPIVersion) {
    this.openAPIVersion = openAPIVersion;
    return this;
  }

  
  @ApiModelProperty(example = "3.0.0", value = "")
  @JsonProperty("openAPIVersion")
  public String getOpenAPIVersion() {
    return openAPIVersion;
  }
  public void setOpenAPIVersion(String openAPIVersion) {
    this.openAPIVersion = openAPIVersion;
  }

  /**
   * contains host/servers specified in the OpenAPI file/URL 
   **/
  public OpenAPIDefinitionValidationResponseInfoDTO endpoints(List<String> endpoints) {
    this.endpoints = endpoints;
    return this;
  }

  
  @ApiModelProperty(value = "contains host/servers specified in the OpenAPI file/URL ")
  @JsonProperty("endpoints")
  public List<String> getEndpoints() {
    return endpoints;
  }
  public void setEndpoints(List<String> endpoints) {
    this.endpoints = endpoints;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OpenAPIDefinitionValidationResponseInfoDTO openAPIDefinitionValidationResponseInfo = (OpenAPIDefinitionValidationResponseInfoDTO) o;
    return Objects.equals(name, openAPIDefinitionValidationResponseInfo.name) &&
        Objects.equals(version, openAPIDefinitionValidationResponseInfo.version) &&
        Objects.equals(context, openAPIDefinitionValidationResponseInfo.context) &&
        Objects.equals(description, openAPIDefinitionValidationResponseInfo.description) &&
        Objects.equals(openAPIVersion, openAPIDefinitionValidationResponseInfo.openAPIVersion) &&
        Objects.equals(endpoints, openAPIDefinitionValidationResponseInfo.endpoints);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, version, context, description, openAPIVersion, endpoints);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OpenAPIDefinitionValidationResponseInfoDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    context: ").append(toIndentedString(context)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    openAPIVersion: ").append(toIndentedString(openAPIVersion)).append("\n");
    sb.append("    endpoints: ").append(toIndentedString(endpoints)).append("\n");
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

