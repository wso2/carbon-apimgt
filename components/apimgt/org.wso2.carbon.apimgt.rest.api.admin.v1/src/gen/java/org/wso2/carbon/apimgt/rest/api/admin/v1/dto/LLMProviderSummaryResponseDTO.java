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



public class LLMProviderSummaryResponseDTO   {
  
    private String id = null;
    private String name = null;
    private String version = null;
    private String description = null;

  /**
   **/
  public LLMProviderSummaryResponseDTO id(String id) {
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
  public LLMProviderSummaryResponseDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "open-ai", value = "")
  @JsonProperty("name")
 @Size(min=1,max=255)  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public LLMProviderSummaryResponseDTO version(String version) {
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
  public LLMProviderSummaryResponseDTO description(String description) {
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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LLMProviderSummaryResponseDTO llMProviderSummaryResponse = (LLMProviderSummaryResponseDTO) o;
    return Objects.equals(id, llMProviderSummaryResponse.id) &&
        Objects.equals(name, llMProviderSummaryResponse.name) &&
        Objects.equals(version, llMProviderSummaryResponse.version) &&
        Objects.equals(description, llMProviderSummaryResponse.description);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, version, description);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LLMProviderSummaryResponseDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
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

