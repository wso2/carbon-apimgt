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
    private String version = null;
    private List<String> headers = new ArrayList<String>();
    private List<String> queryParams = new ArrayList<String>();
    private String description = null;
    private String apiDefinition = null;
    private String modelPath = null;
    private String promptTokensPath = null;
    private String completionTokensPath = null;
    private String totalTokensPath = null;
    private String hasMetadataInPayload = null;
    private String payloadHandler = null;

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
  public LLMProviderResponseDTO version(String version) {
    this.version = version;
    return this;
  }

  
  @ApiModelProperty(example = "1.0.0", value = "")
  @JsonProperty("version")
 @Size(min=1,max=255)  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   **/
  public LLMProviderResponseDTO headers(List<String> headers) {
    this.headers = headers;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("headers")
  public List<String> getHeaders() {
    return headers;
  }
  public void setHeaders(List<String> headers) {
    this.headers = headers;
  }

  /**
   **/
  public LLMProviderResponseDTO queryParams(List<String> queryParams) {
    this.queryParams = queryParams;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("queryParams")
  public List<String> getQueryParams() {
    return queryParams;
  }
  public void setQueryParams(List<String> queryParams) {
    this.queryParams = queryParams;
  }

  /**
   **/
  public LLMProviderResponseDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "OpenAI LLM Provider", value = "")
  @JsonProperty("description")
 @Size(max=1023)  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
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
   **/
  public LLMProviderResponseDTO modelPath(String modelPath) {
    this.modelPath = modelPath;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("modelPath")
 @Size(min=1,max=255)  public String getModelPath() {
    return modelPath;
  }
  public void setModelPath(String modelPath) {
    this.modelPath = modelPath;
  }

  /**
   **/
  public LLMProviderResponseDTO promptTokensPath(String promptTokensPath) {
    this.promptTokensPath = promptTokensPath;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("promptTokensPath")
 @Size(min=1,max=255)  public String getPromptTokensPath() {
    return promptTokensPath;
  }
  public void setPromptTokensPath(String promptTokensPath) {
    this.promptTokensPath = promptTokensPath;
  }

  /**
   **/
  public LLMProviderResponseDTO completionTokensPath(String completionTokensPath) {
    this.completionTokensPath = completionTokensPath;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("completionTokensPath")
 @Size(min=1,max=255)  public String getCompletionTokensPath() {
    return completionTokensPath;
  }
  public void setCompletionTokensPath(String completionTokensPath) {
    this.completionTokensPath = completionTokensPath;
  }

  /**
   **/
  public LLMProviderResponseDTO totalTokensPath(String totalTokensPath) {
    this.totalTokensPath = totalTokensPath;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("totalTokensPath")
 @Size(min=1,max=255)  public String getTotalTokensPath() {
    return totalTokensPath;
  }
  public void setTotalTokensPath(String totalTokensPath) {
    this.totalTokensPath = totalTokensPath;
  }

  /**
   **/
  public LLMProviderResponseDTO hasMetadataInPayload(String hasMetadataInPayload) {
    this.hasMetadataInPayload = hasMetadataInPayload;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("hasMetadataInPayload")
  public String getHasMetadataInPayload() {
    return hasMetadataInPayload;
  }
  public void setHasMetadataInPayload(String hasMetadataInPayload) {
    this.hasMetadataInPayload = hasMetadataInPayload;
  }

  /**
   **/
  public LLMProviderResponseDTO payloadHandler(String payloadHandler) {
    this.payloadHandler = payloadHandler;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("payloadHandler")
 @Size(min=1,max=255)  public String getPayloadHandler() {
    return payloadHandler;
  }
  public void setPayloadHandler(String payloadHandler) {
    this.payloadHandler = payloadHandler;
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
        Objects.equals(version, llMProviderResponse.version) &&
        Objects.equals(headers, llMProviderResponse.headers) &&
        Objects.equals(queryParams, llMProviderResponse.queryParams) &&
        Objects.equals(description, llMProviderResponse.description) &&
        Objects.equals(apiDefinition, llMProviderResponse.apiDefinition) &&
        Objects.equals(modelPath, llMProviderResponse.modelPath) &&
        Objects.equals(promptTokensPath, llMProviderResponse.promptTokensPath) &&
        Objects.equals(completionTokensPath, llMProviderResponse.completionTokensPath) &&
        Objects.equals(totalTokensPath, llMProviderResponse.totalTokensPath) &&
        Objects.equals(hasMetadataInPayload, llMProviderResponse.hasMetadataInPayload) &&
        Objects.equals(payloadHandler, llMProviderResponse.payloadHandler);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, version, headers, queryParams, description, apiDefinition, modelPath, promptTokensPath, completionTokensPath, totalTokensPath, hasMetadataInPayload, payloadHandler);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LLMProviderResponseDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    headers: ").append(toIndentedString(headers)).append("\n");
    sb.append("    queryParams: ").append(toIndentedString(queryParams)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    apiDefinition: ").append(toIndentedString(apiDefinition)).append("\n");
    sb.append("    modelPath: ").append(toIndentedString(modelPath)).append("\n");
    sb.append("    promptTokensPath: ").append(toIndentedString(promptTokensPath)).append("\n");
    sb.append("    completionTokensPath: ").append(toIndentedString(completionTokensPath)).append("\n");
    sb.append("    totalTokensPath: ").append(toIndentedString(totalTokensPath)).append("\n");
    sb.append("    hasMetadataInPayload: ").append(toIndentedString(hasMetadataInPayload)).append("\n");
    sb.append("    payloadHandler: ").append(toIndentedString(payloadHandler)).append("\n");
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

