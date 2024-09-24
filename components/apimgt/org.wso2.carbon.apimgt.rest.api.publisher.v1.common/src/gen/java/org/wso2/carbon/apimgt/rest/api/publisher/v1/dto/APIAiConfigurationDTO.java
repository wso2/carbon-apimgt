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



public class APIAiConfigurationDTO   {
  
    private String llmProviderName = null;
    private String llmProviderApiVersion = null;

  /**
   **/
  public APIAiConfigurationDTO llmProviderName(String llmProviderName) {
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
  public APIAiConfigurationDTO llmProviderApiVersion(String llmProviderApiVersion) {
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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIAiConfigurationDTO apIAiConfiguration = (APIAiConfigurationDTO) o;
    return Objects.equals(llmProviderName, apIAiConfiguration.llmProviderName) &&
        Objects.equals(llmProviderApiVersion, apIAiConfiguration.llmProviderApiVersion);
  }

  @Override
  public int hashCode() {
    return Objects.hash(llmProviderName, llmProviderApiVersion);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIAiConfigurationDTO {\n");
    
    sb.append("    llmProviderName: ").append(toIndentedString(llmProviderName)).append("\n");
    sb.append("    llmProviderApiVersion: ").append(toIndentedString(llmProviderApiVersion)).append("\n");
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

