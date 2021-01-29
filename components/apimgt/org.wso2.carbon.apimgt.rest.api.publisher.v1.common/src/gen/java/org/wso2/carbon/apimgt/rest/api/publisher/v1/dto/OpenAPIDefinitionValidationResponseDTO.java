package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorListItemDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OpenAPIDefinitionValidationResponseInfoDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class OpenAPIDefinitionValidationResponseDTO   {
  
    private Boolean isValid = null;
    private String content = null;
    private OpenAPIDefinitionValidationResponseInfoDTO info = null;
    private List<ErrorListItemDTO> errors = new ArrayList<ErrorListItemDTO>();

  /**
   * This attribute declares whether this definition is valid or not. 
   **/
  public OpenAPIDefinitionValidationResponseDTO isValid(Boolean isValid) {
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
   * OpenAPI definition content. 
   **/
  public OpenAPIDefinitionValidationResponseDTO content(String content) {
    this.content = content;
    return this;
  }

  
  @ApiModelProperty(value = "OpenAPI definition content. ")
  @JsonProperty("content")
  public String getContent() {
    return content;
  }
  public void setContent(String content) {
    this.content = content;
  }

  /**
   **/
  public OpenAPIDefinitionValidationResponseDTO info(OpenAPIDefinitionValidationResponseInfoDTO info) {
    this.info = info;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("info")
  public OpenAPIDefinitionValidationResponseInfoDTO getInfo() {
    return info;
  }
  public void setInfo(OpenAPIDefinitionValidationResponseInfoDTO info) {
    this.info = info;
  }

  /**
   * If there are more than one error list them out. For example, list out validation errors by each field. 
   **/
  public OpenAPIDefinitionValidationResponseDTO errors(List<ErrorListItemDTO> errors) {
    this.errors = errors;
    return this;
  }

  
  @ApiModelProperty(value = "If there are more than one error list them out. For example, list out validation errors by each field. ")
      @Valid
  @JsonProperty("errors")
  public List<ErrorListItemDTO> getErrors() {
    return errors;
  }
  public void setErrors(List<ErrorListItemDTO> errors) {
    this.errors = errors;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OpenAPIDefinitionValidationResponseDTO openAPIDefinitionValidationResponse = (OpenAPIDefinitionValidationResponseDTO) o;
    return Objects.equals(isValid, openAPIDefinitionValidationResponse.isValid) &&
        Objects.equals(content, openAPIDefinitionValidationResponse.content) &&
        Objects.equals(info, openAPIDefinitionValidationResponse.info) &&
        Objects.equals(errors, openAPIDefinitionValidationResponse.errors);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isValid, content, info, errors);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OpenAPIDefinitionValidationResponseDTO {\n");
    
    sb.append("    isValid: ").append(toIndentedString(isValid)).append("\n");
    sb.append("    content: ").append(toIndentedString(content)).append("\n");
    sb.append("    info: ").append(toIndentedString(info)).append("\n");
    sb.append("    errors: ").append(toIndentedString(errors)).append("\n");
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

