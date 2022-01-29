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
  
    private String attributeName = null;
    private String attributeDisplayName = null;
    private String attributeDescription = null;
    private String attributeValidationRegex = null;
    private String attributeType = null;
    private Boolean required = null;

  /**
   * Name of the attibute
   **/
  public OperationPolicySpecAttributeDTO attributeName(String attributeName) {
    this.attributeName = attributeName;
    return this;
  }

  
  @ApiModelProperty(example = "headerName", value = "Name of the attibute")
  @JsonProperty("attributeName")
  public String getAttributeName() {
    return attributeName;
  }
  public void setAttributeName(String attributeName) {
    this.attributeName = attributeName;
  }

  /**
   * Display name of the attibute
   **/
  public OperationPolicySpecAttributeDTO attributeDisplayName(String attributeDisplayName) {
    this.attributeDisplayName = attributeDisplayName;
    return this;
  }

  
  @ApiModelProperty(example = "Header Name", value = "Display name of the attibute")
  @JsonProperty("attributeDisplayName")
  public String getAttributeDisplayName() {
    return attributeDisplayName;
  }
  public void setAttributeDisplayName(String attributeDisplayName) {
    this.attributeDisplayName = attributeDisplayName;
  }

  /**
   * Description of the attibute
   **/
  public OperationPolicySpecAttributeDTO attributeDescription(String attributeDescription) {
    this.attributeDescription = attributeDescription;
    return this;
  }

  
  @ApiModelProperty(example = "Name of the header to be removed", value = "Description of the attibute")
  @JsonProperty("attributeDescription")
  public String getAttributeDescription() {
    return attributeDescription;
  }
  public void setAttributeDescription(String attributeDescription) {
    this.attributeDescription = attributeDescription;
  }

  /**
   * UI validation regex for the attibute
   **/
  public OperationPolicySpecAttributeDTO attributeValidationRegex(String attributeValidationRegex) {
    this.attributeValidationRegex = attributeValidationRegex;
    return this;
  }

  
  @ApiModelProperty(example = "/^[a-z\\s]{0,255}$/i", value = "UI validation regex for the attibute")
  @JsonProperty("attributeValidationRegex")
  public String getAttributeValidationRegex() {
    return attributeValidationRegex;
  }
  public void setAttributeValidationRegex(String attributeValidationRegex) {
    this.attributeValidationRegex = attributeValidationRegex;
  }

  /**
   * Type of the attibute
   **/
  public OperationPolicySpecAttributeDTO attributeType(String attributeType) {
    this.attributeType = attributeType;
    return this;
  }

  
  @ApiModelProperty(example = "string", value = "Type of the attibute")
  @JsonProperty("attributeType")
  public String getAttributeType() {
    return attributeType;
  }
  public void setAttributeType(String attributeType) {
    this.attributeType = attributeType;
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
    return Objects.equals(attributeName, operationPolicySpecAttribute.attributeName) &&
        Objects.equals(attributeDisplayName, operationPolicySpecAttribute.attributeDisplayName) &&
        Objects.equals(attributeDescription, operationPolicySpecAttribute.attributeDescription) &&
        Objects.equals(attributeValidationRegex, operationPolicySpecAttribute.attributeValidationRegex) &&
        Objects.equals(attributeType, operationPolicySpecAttribute.attributeType) &&
        Objects.equals(required, operationPolicySpecAttribute.required);
  }

  @Override
  public int hashCode() {
    return Objects.hash(attributeName, attributeDisplayName, attributeDescription, attributeValidationRegex, attributeType, required);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OperationPolicySpecAttributeDTO {\n");
    
    sb.append("    attributeName: ").append(toIndentedString(attributeName)).append("\n");
    sb.append("    attributeDisplayName: ").append(toIndentedString(attributeDisplayName)).append("\n");
    sb.append("    attributeDescription: ").append(toIndentedString(attributeDescription)).append("\n");
    sb.append("    attributeValidationRegex: ").append(toIndentedString(attributeValidationRegex)).append("\n");
    sb.append("    attributeType: ").append(toIndentedString(attributeType)).append("\n");
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

