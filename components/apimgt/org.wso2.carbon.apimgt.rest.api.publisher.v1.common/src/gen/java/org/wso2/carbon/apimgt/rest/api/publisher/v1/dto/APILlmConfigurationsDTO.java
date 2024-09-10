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



public class APILlmConfigurationsDTO   {
  
    private Boolean enabled = false;
    private String llmProviderName = "OpenAI";
    private String llmProviderApiVersion = "1.0.0";
    private String additionalHeaders = null;
    private String additionalQueryParameters = null;

  /**
   **/
  public APILlmConfigurationsDTO enabled(Boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("enabled")
  public Boolean isEnabled() {
    return enabled;
  }
  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  /**
   **/
  public APILlmConfigurationsDTO llmProviderName(String llmProviderName) {
    this.llmProviderName = llmProviderName;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("llmProviderName")
  public String getLlmProviderName() {
    return llmProviderName;
  }
  public void setLlmProviderName(String llmProviderName) {
    this.llmProviderName = llmProviderName;
  }

  /**
   **/
  public APILlmConfigurationsDTO llmProviderApiVersion(String llmProviderApiVersion) {
    this.llmProviderApiVersion = llmProviderApiVersion;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("llmProviderApiVersion")
  public String getLlmProviderApiVersion() {
    return llmProviderApiVersion;
  }
  public void setLlmProviderApiVersion(String llmProviderApiVersion) {
    this.llmProviderApiVersion = llmProviderApiVersion;
  }

  /**
   * A JSON string representing key-value pairs for headers
   **/
  public APILlmConfigurationsDTO additionalHeaders(String additionalHeaders) {
    this.additionalHeaders = additionalHeaders;
    return this;
  }

  
  @ApiModelProperty(value = "A JSON string representing key-value pairs for headers")
  @JsonProperty("additionalHeaders")
  public String getAdditionalHeaders() {
    return additionalHeaders;
  }
  public void setAdditionalHeaders(String additionalHeaders) {
    this.additionalHeaders = additionalHeaders;
  }

  /**
   * A JSON string representing key-value pairs for query parameters
   **/
  public APILlmConfigurationsDTO additionalQueryParameters(String additionalQueryParameters) {
    this.additionalQueryParameters = additionalQueryParameters;
    return this;
  }

  
  @ApiModelProperty(value = "A JSON string representing key-value pairs for query parameters")
  @JsonProperty("additionalQueryParameters")
  public String getAdditionalQueryParameters() {
    return additionalQueryParameters;
  }
  public void setAdditionalQueryParameters(String additionalQueryParameters) {
    this.additionalQueryParameters = additionalQueryParameters;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APILlmConfigurationsDTO apILlmConfigurations = (APILlmConfigurationsDTO) o;
    return Objects.equals(enabled, apILlmConfigurations.enabled) &&
        Objects.equals(llmProviderName, apILlmConfigurations.llmProviderName) &&
        Objects.equals(llmProviderApiVersion, apILlmConfigurations.llmProviderApiVersion) &&
        Objects.equals(additionalHeaders, apILlmConfigurations.additionalHeaders) &&
        Objects.equals(additionalQueryParameters, apILlmConfigurations.additionalQueryParameters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(enabled, llmProviderName, llmProviderApiVersion, additionalHeaders, additionalQueryParameters);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APILlmConfigurationsDTO {\n");
    
    sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
    sb.append("    llmProviderName: ").append(toIndentedString(llmProviderName)).append("\n");
    sb.append("    llmProviderApiVersion: ").append(toIndentedString(llmProviderApiVersion)).append("\n");
    sb.append("    additionalHeaders: ").append(toIndentedString(additionalHeaders)).append("\n");
    sb.append("    additionalQueryParameters: ").append(toIndentedString(additionalQueryParameters)).append("\n");
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

