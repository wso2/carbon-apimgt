package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MCPServerValidationResponseToolInfoDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class MCPServerValidationResponseDTO   {
  
    private Boolean isValid = null;
    private String errorMessage = null;
    private String content = null;
    private MCPServerValidationResponseToolInfoDTO toolInfo = null;

  /**
   * This attribute declares whether this definition is valid or not. 
   **/
  public MCPServerValidationResponseDTO isValid(Boolean isValid) {
    this.isValid = isValid;
    return this;
  }

  
  @ApiModelProperty(example = "true", required = true, value = "This attribute declares whether this definition is valid or not. ")
  @JsonProperty("isValid")
  @NotNull
  public Boolean isIsValid() {
    return isValid;
  }
  public void setIsValid(Boolean isValid) {
    this.isValid = isValid;
  }

  /**
   * This attribute declares the validation error message 
   **/
  public MCPServerValidationResponseDTO errorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "This attribute declares the validation error message ")
  @JsonProperty("errorMessage")
  @NotNull
  public String getErrorMessage() {
    return errorMessage;
  }
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  /**
   * MCP Server schema definition content. 
   **/
  public MCPServerValidationResponseDTO content(String content) {
    this.content = content;
    return this;
  }

  
  @ApiModelProperty(value = "MCP Server schema definition content. ")
  @JsonProperty("content")
  public String getContent() {
    return content;
  }
  public void setContent(String content) {
    this.content = content;
  }

  /**
   **/
  public MCPServerValidationResponseDTO toolInfo(MCPServerValidationResponseToolInfoDTO toolInfo) {
    this.toolInfo = toolInfo;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("toolInfo")
  public MCPServerValidationResponseToolInfoDTO getToolInfo() {
    return toolInfo;
  }
  public void setToolInfo(MCPServerValidationResponseToolInfoDTO toolInfo) {
    this.toolInfo = toolInfo;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MCPServerValidationResponseDTO mcPServerValidationResponse = (MCPServerValidationResponseDTO) o;
    return Objects.equals(isValid, mcPServerValidationResponse.isValid) &&
        Objects.equals(errorMessage, mcPServerValidationResponse.errorMessage) &&
        Objects.equals(content, mcPServerValidationResponse.content) &&
        Objects.equals(toolInfo, mcPServerValidationResponse.toolInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isValid, errorMessage, content, toolInfo);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MCPServerValidationResponseDTO {\n");
    
    sb.append("    isValid: ").append(toIndentedString(isValid)).append("\n");
    sb.append("    errorMessage: ").append(toIndentedString(errorMessage)).append("\n");
    sb.append("    content: ").append(toIndentedString(content)).append("\n");
    sb.append("    toolInfo: ").append(toIndentedString(toolInfo)).append("\n");
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

