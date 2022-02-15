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



public class OperationPolicySpecAttributeDTO   {
  
    private String name = null;
    private String displayName = null;
    private String description = null;
    private String validationRegex = null;
    private String type = null;
    private Boolean required = null;

  /**
   * Name of the attibute
   **/
  public OperationPolicySpecAttributeDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "headerName", value = "Name of the attibute")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Display name of the attibute
   **/
  public OperationPolicySpecAttributeDTO displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(example = "Header Name", value = "Display name of the attibute")
  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * Description of the attibute
   **/
  public OperationPolicySpecAttributeDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "Name of the header to be removed", value = "Description of the attibute")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * UI validation regex for the attibute
   **/
  public OperationPolicySpecAttributeDTO validationRegex(String validationRegex) {
    this.validationRegex = validationRegex;
    return this;
  }

  
  @ApiModelProperty(example = "/^[a-z\\s]{0,255}$/i", value = "UI validation regex for the attibute")
  @JsonProperty("validationRegex")
  public String getValidationRegex() {
    return validationRegex;
  }
  public void setValidationRegex(String validationRegex) {
    this.validationRegex = validationRegex;
  }

  /**
   * Type of the attibute
   **/
  public OperationPolicySpecAttributeDTO type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(example = "string", value = "Type of the attibute")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Is this attibute mandetory for the policy
   **/
  public OperationPolicySpecAttributeDTO required(Boolean required) {
    this.required = required;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "Is this attibute mandetory for the policy")
  @JsonProperty("required")
  public Boolean isRequired() {
    return required;
  }
  public void setRequired(Boolean required) {
    this.required = required;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OperationPolicySpecAttributeDTO operationPolicySpecAttribute = (OperationPolicySpecAttributeDTO) o;
    return Objects.equals(name, operationPolicySpecAttribute.name) &&
        Objects.equals(displayName, operationPolicySpecAttribute.displayName) &&
        Objects.equals(description, operationPolicySpecAttribute.description) &&
        Objects.equals(validationRegex, operationPolicySpecAttribute.validationRegex) &&
        Objects.equals(type, operationPolicySpecAttribute.type) &&
        Objects.equals(required, operationPolicySpecAttribute.required);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, displayName, description, validationRegex, type, required);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OperationPolicySpecAttributeDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    validationRegex: ").append(toIndentedString(validationRegex)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    required: ").append(toIndentedString(required)).append("\n");
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

