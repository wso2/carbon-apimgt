package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

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



public class AIServiceProviderResponseDTO   {
  
    private String id = null;
    private String name = null;
    private String apiVersion = null;
    private Boolean builtInSupport = null;
    private Boolean multipleModelProviderSupport = null;
    private String description = null;
    private String configurations = null;
    private String apiDefinition = null;
    private String modelProviders = null;

  /**
   **/
  public AIServiceProviderResponseDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "ece92bdc-e1e6-325c-b6f4-656208a041e9", value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public AIServiceProviderResponseDTO name(String name) {
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
  public AIServiceProviderResponseDTO apiVersion(String apiVersion) {
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
   * Is built-in support
   **/
  public AIServiceProviderResponseDTO builtInSupport(Boolean builtInSupport) {
    this.builtInSupport = builtInSupport;
    return this;
  }

  
  @ApiModelProperty(value = "Is built-in support")
  @JsonProperty("builtInSupport")
  public Boolean isBuiltInSupport() {
    return builtInSupport;
  }
  public void setBuiltInSupport(Boolean builtInSupport) {
    this.builtInSupport = builtInSupport;
  }

  /**
   * Indicates whether the AI Service Provider supports multiple model providers. If true, the AI Service Provider can support multiple model providers. If false, the AI Service Provider supports only one model provider. 
   **/
  public AIServiceProviderResponseDTO multipleModelProviderSupport(Boolean multipleModelProviderSupport) {
    this.multipleModelProviderSupport = multipleModelProviderSupport;
    return this;
  }

  
  @ApiModelProperty(value = "Indicates whether the AI Service Provider supports multiple model providers. If true, the AI Service Provider can support multiple model providers. If false, the AI Service Provider supports only one model provider. ")
  @JsonProperty("multipleModelProviderSupport")
  public Boolean isMultipleModelProviderSupport() {
    return multipleModelProviderSupport;
  }
  public void setMultipleModelProviderSupport(Boolean multipleModelProviderSupport) {
    this.multipleModelProviderSupport = multipleModelProviderSupport;
  }

  /**
   **/
  public AIServiceProviderResponseDTO description(String description) {
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
  public AIServiceProviderResponseDTO configurations(String configurations) {
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
  public AIServiceProviderResponseDTO apiDefinition(String apiDefinition) {
    this.apiDefinition = apiDefinition;
    return this;
  }

  
  @ApiModelProperty(value = "OpenAPI specification")
  @JsonProperty("apiDefinition")
  public String getApiDefinition() {
    return apiDefinition;
  }
  public void setApiDefinition(String apiDefinition) {
    this.apiDefinition = apiDefinition;
  }

  /**
   * List of models supported by the LLM Provider
   **/
  public AIServiceProviderResponseDTO modelProviders(String modelProviders) {
    this.modelProviders = modelProviders;
    return this;
  }

  
  @ApiModelProperty(value = "List of models supported by the LLM Provider")
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
    AIServiceProviderResponseDTO aiServiceProviderResponse = (AIServiceProviderResponseDTO) o;
    return Objects.equals(id, aiServiceProviderResponse.id) &&
        Objects.equals(name, aiServiceProviderResponse.name) &&
        Objects.equals(apiVersion, aiServiceProviderResponse.apiVersion) &&
        Objects.equals(builtInSupport, aiServiceProviderResponse.builtInSupport) &&
        Objects.equals(multipleModelProviderSupport, aiServiceProviderResponse.multipleModelProviderSupport) &&
        Objects.equals(description, aiServiceProviderResponse.description) &&
        Objects.equals(configurations, aiServiceProviderResponse.configurations) &&
        Objects.equals(apiDefinition, aiServiceProviderResponse.apiDefinition) &&
        Objects.equals(modelProviders, aiServiceProviderResponse.modelProviders);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, apiVersion, builtInSupport, multipleModelProviderSupport, description, configurations, apiDefinition, modelProviders);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AIServiceProviderResponseDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
    sb.append("    builtInSupport: ").append(toIndentedString(builtInSupport)).append("\n");
    sb.append("    multipleModelProviderSupport: ").append(toIndentedString(multipleModelProviderSupport)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
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

