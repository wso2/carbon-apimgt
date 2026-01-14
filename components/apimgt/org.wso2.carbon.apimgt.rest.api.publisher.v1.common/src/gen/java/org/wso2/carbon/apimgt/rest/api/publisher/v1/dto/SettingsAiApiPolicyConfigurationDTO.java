package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

/**
 * AI provider configuration status
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "AI provider configuration status")

public class SettingsAiApiPolicyConfigurationDTO   {
  
    private Boolean llmProviderConfigured = false;
    private Boolean embeddingProviderConfigured = false;

  /**
   * Checks if LLM provider is configured in deployment.toml
   **/
  public SettingsAiApiPolicyConfigurationDTO llmProviderConfigured(Boolean llmProviderConfigured) {
    this.llmProviderConfigured = llmProviderConfigured;
    return this;
  }

  
  @ApiModelProperty(value = "Checks if LLM provider is configured in deployment.toml")
  @JsonProperty("llmProviderConfigured")
  public Boolean isLlmProviderConfigured() {
    return llmProviderConfigured;
  }
  public void setLlmProviderConfigured(Boolean llmProviderConfigured) {
    this.llmProviderConfigured = llmProviderConfigured;
  }

  /**
   * Checks if Embedding provider is configured in deployment.toml
   **/
  public SettingsAiApiPolicyConfigurationDTO embeddingProviderConfigured(Boolean embeddingProviderConfigured) {
    this.embeddingProviderConfigured = embeddingProviderConfigured;
    return this;
  }

  
  @ApiModelProperty(value = "Checks if Embedding provider is configured in deployment.toml")
  @JsonProperty("embeddingProviderConfigured")
  public Boolean isEmbeddingProviderConfigured() {
    return embeddingProviderConfigured;
  }
  public void setEmbeddingProviderConfigured(Boolean embeddingProviderConfigured) {
    this.embeddingProviderConfigured = embeddingProviderConfigured;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SettingsAiApiPolicyConfigurationDTO settingsAiApiPolicyConfiguration = (SettingsAiApiPolicyConfigurationDTO) o;
    return Objects.equals(llmProviderConfigured, settingsAiApiPolicyConfiguration.llmProviderConfigured) &&
        Objects.equals(embeddingProviderConfigured, settingsAiApiPolicyConfiguration.embeddingProviderConfigured);
  }

  @Override
  public int hashCode() {
    return Objects.hash(llmProviderConfigured, embeddingProviderConfigured);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SettingsAiApiPolicyConfigurationDTO {\n");
    
    sb.append("    llmProviderConfigured: ").append(toIndentedString(llmProviderConfigured)).append("\n");
    sb.append("    embeddingProviderConfigured: ").append(toIndentedString(embeddingProviderConfigured)).append("\n");
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

