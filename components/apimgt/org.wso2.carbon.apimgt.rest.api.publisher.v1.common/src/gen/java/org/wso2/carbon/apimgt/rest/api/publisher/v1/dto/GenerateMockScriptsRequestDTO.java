package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class GenerateMockScriptsRequestDTO   {
  
    private Boolean generateWithAI = false;
    private Map<String, Object> config = new HashMap<String, Object>();

  /**
   * Generate mock payload with AI 
   **/
  public GenerateMockScriptsRequestDTO generateWithAI(Boolean generateWithAI) {
    this.generateWithAI = generateWithAI;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "Generate mock payload with AI ")
  @JsonProperty("generateWithAI")
  public Boolean isGenerateWithAI() {
    return generateWithAI;
  }
  public void setGenerateWithAI(Boolean generateWithAI) {
    this.generateWithAI = generateWithAI;
  }

  /**
   * Config to generate mock scripts and db properties. Can contain any key-value pairs. 
   **/
  public GenerateMockScriptsRequestDTO config(Map<String, Object> config) {
    this.config = config;
    return this;
  }

  
  @ApiModelProperty(example = "{}", value = "Config to generate mock scripts and db properties. Can contain any key-value pairs. ")
  @JsonProperty("config")
  public Map<String, Object> getConfig() {
    return config;
  }
  public void setConfig(Map<String, Object> config) {
    this.config = config;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GenerateMockScriptsRequestDTO generateMockScriptsRequest = (GenerateMockScriptsRequestDTO) o;
    return Objects.equals(generateWithAI, generateMockScriptsRequest.generateWithAI) &&
        Objects.equals(config, generateMockScriptsRequest.config);
  }

  @Override
  public int hashCode() {
    return Objects.hash(generateWithAI, config);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GenerateMockScriptsRequestDTO {\n");
    
    sb.append("    generateWithAI: ").append(toIndentedString(generateWithAI)).append("\n");
    sb.append("    config: ").append(toIndentedString(config)).append("\n");
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

