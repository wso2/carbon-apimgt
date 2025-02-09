package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.File;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class LLMProviderRequestDTO   {
  
    private String name = null;
    private String apiVersion = null;
    private String description = null;
    private String configurations = null;
    private File apiDefinition = null;
    private String modelList = null;

  /**
   **/
  public LLMProviderRequestDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "OpenAI", value = "")
  @JsonProperty("name")
 @Size(min=1,max=255)  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public LLMProviderRequestDTO apiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
    return this;
  }

  
  @ApiModelProperty(example = "1.0.0", value = "")
  @JsonProperty("apiVersion")
 @Size(min=1,max=255)  public String getApiVersion() {
    return apiVersion;
  }
  public void setApiVersion(String apiVersion) {
    this.apiVersion = apiVersion;
  }

  /**
   **/
  public LLMProviderRequestDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "OpenAI LLM", value = "")
  @JsonProperty("description")
 @Size(max=1023)  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * LLM Provider configurations
   **/
  public LLMProviderRequestDTO configurations(String configurations) {
    this.configurations = configurations;
    return this;
  }

  
  @ApiModelProperty(value = "LLM Provider configurations")
  @JsonProperty("configurations")
  public String getConfigurations() {
    return configurations;
  }
  public void setConfigurations(String configurations) {
    this.configurations = configurations;
  }

  /**
   * OpenAPI specification
   **/
  public LLMProviderRequestDTO apiDefinition(File apiDefinition) {
    this.apiDefinition = apiDefinition;
    return this;
  }

  
  @ApiModelProperty(value = "OpenAPI specification")
  @JsonProperty("apiDefinition")
  public File getApiDefinition() {
    return apiDefinition;
  }
  public void setApiDefinition(File apiDefinition) {
    this.apiDefinition = apiDefinition;
  }

  /**
   * List of models supported by the LLM Provider as a stringified JSON array
   **/
  public LLMProviderRequestDTO modelList(String modelList) {
    this.modelList = modelList;
    return this;
  }

  
  @ApiModelProperty(value = "List of models supported by the LLM Provider as a stringified JSON array")
  @JsonProperty("modelList")
  public String getModelList() {
    return modelList;
  }
  public void setModelList(String modelList) {
    this.modelList = modelList;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LLMProviderRequestDTO llMProviderRequest = (LLMProviderRequestDTO) o;
    return Objects.equals(name, llMProviderRequest.name) &&
        Objects.equals(apiVersion, llMProviderRequest.apiVersion) &&
        Objects.equals(description, llMProviderRequest.description) &&
        Objects.equals(configurations, llMProviderRequest.configurations) &&
        Objects.equals(apiDefinition, llMProviderRequest.apiDefinition) &&
        Objects.equals(modelList, llMProviderRequest.modelList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, apiVersion, description, configurations, apiDefinition, modelList);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LLMProviderRequestDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    configurations: ").append(toIndentedString(configurations)).append("\n");
    sb.append("    apiDefinition: ").append(toIndentedString(apiDefinition)).append("\n");
    sb.append("    modelList: ").append(toIndentedString(modelList)).append("\n");
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

