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



public class AIServiceProviderRequestDTO   {
  
    private String name = null;
    private String apiVersion = null;
    private String description = null;
    private String multitpleModelProviderSupport = "false";
    private String configurations = null;
    private File apiDefinition = null;
    private String modelProviders = null;

  /**
   **/
  public AIServiceProviderRequestDTO name(String name) {
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
  public AIServiceProviderRequestDTO apiVersion(String apiVersion) {
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
  public AIServiceProviderRequestDTO description(String description) {
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
   * Indicates whether the AI Service Provider supports multiple model providers. If true, the AI Service Provider can support multiple model providers. If false, the AI Service Provider supports only one model provider. 
   **/
  public AIServiceProviderRequestDTO multitpleModelProviderSupport(String multitpleModelProviderSupport) {
    this.multitpleModelProviderSupport = multitpleModelProviderSupport;
    return this;
  }

  
  @ApiModelProperty(value = "Indicates whether the AI Service Provider supports multiple model providers. If true, the AI Service Provider can support multiple model providers. If false, the AI Service Provider supports only one model provider. ")
  @JsonProperty("multitpleModelProviderSupport")
  public String getMultitpleModelProviderSupport() {
    return multitpleModelProviderSupport;
  }
  public void setMultitpleModelProviderSupport(String multitpleModelProviderSupport) {
    this.multitpleModelProviderSupport = multitpleModelProviderSupport;
  }

  /**
   * LLM Provider configurations
   **/
  public AIServiceProviderRequestDTO configurations(String configurations) {
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
  public AIServiceProviderRequestDTO apiDefinition(File apiDefinition) {
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
   **/
  public AIServiceProviderRequestDTO modelProviders(String modelProviders) {
    this.modelProviders = modelProviders;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("modelProviders")
  public String getModelProviders() {
    return modelProviders;
  }
  public void setModelProviders(String modelProviders) {
    this.modelProviders = modelProviders;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AIServiceProviderRequestDTO aiServiceProviderRequest = (AIServiceProviderRequestDTO) o;
    return Objects.equals(name, aiServiceProviderRequest.name) &&
        Objects.equals(apiVersion, aiServiceProviderRequest.apiVersion) &&
        Objects.equals(description, aiServiceProviderRequest.description) &&
        Objects.equals(multitpleModelProviderSupport, aiServiceProviderRequest.multitpleModelProviderSupport) &&
        Objects.equals(configurations, aiServiceProviderRequest.configurations) &&
        Objects.equals(apiDefinition, aiServiceProviderRequest.apiDefinition) &&
        Objects.equals(modelProviders, aiServiceProviderRequest.modelProviders);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, apiVersion, description, multitpleModelProviderSupport, configurations, apiDefinition, modelProviders);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AIServiceProviderRequestDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    multitpleModelProviderSupport: ").append(toIndentedString(multitpleModelProviderSupport)).append("\n");
    sb.append("    configurations: ").append(toIndentedString(configurations)).append("\n");
    sb.append("    apiDefinition: ").append(toIndentedString(apiDefinition)).append("\n");
    sb.append("    modelProviders: ").append(toIndentedString(modelProviders)).append("\n");
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

