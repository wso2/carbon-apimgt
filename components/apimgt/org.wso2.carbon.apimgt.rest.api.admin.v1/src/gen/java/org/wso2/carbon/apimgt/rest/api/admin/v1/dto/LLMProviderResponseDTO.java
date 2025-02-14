package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

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



public class LLMProviderResponseDTO   {
  
    private String id = null;
    private String name = null;
    private String apiVersion = null;
    private Boolean builtInSupport = null;
    private String description = null;
    private String configurations = null;
    private String apiDefinition = null;
    private List<String> modelList = new ArrayList<String>();

  /**
   **/
  public LLMProviderResponseDTO id(String id) {
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
  public LLMProviderResponseDTO name(String name) {
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
  public LLMProviderResponseDTO apiVersion(String apiVersion) {
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
  public LLMProviderResponseDTO builtInSupport(Boolean builtInSupport) {
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
   **/
  public LLMProviderResponseDTO description(String description) {
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
  public LLMProviderResponseDTO configurations(String configurations) {
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
  public LLMProviderResponseDTO apiDefinition(String apiDefinition) {
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
  public LLMProviderResponseDTO modelList(List<String> modelList) {
    this.modelList = modelList;
    return this;
  }

  
  @ApiModelProperty(value = "List of models supported by the LLM Provider")
  @JsonProperty("modelList")
  public List<String> getModelList() {
    return modelList;
  }
  public void setModelList(List<String> modelList) {
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
    LLMProviderResponseDTO llMProviderResponse = (LLMProviderResponseDTO) o;
    return Objects.equals(id, llMProviderResponse.id) &&
        Objects.equals(name, llMProviderResponse.name) &&
        Objects.equals(apiVersion, llMProviderResponse.apiVersion) &&
        Objects.equals(builtInSupport, llMProviderResponse.builtInSupport) &&
        Objects.equals(description, llMProviderResponse.description) &&
        Objects.equals(configurations, llMProviderResponse.configurations) &&
        Objects.equals(apiDefinition, llMProviderResponse.apiDefinition) &&
        Objects.equals(modelList, llMProviderResponse.modelList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, apiVersion, builtInSupport, description, configurations, apiDefinition, modelList);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LLMProviderResponseDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    apiVersion: ").append(toIndentedString(apiVersion)).append("\n");
    sb.append("    builtInSupport: ").append(toIndentedString(builtInSupport)).append("\n");
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

