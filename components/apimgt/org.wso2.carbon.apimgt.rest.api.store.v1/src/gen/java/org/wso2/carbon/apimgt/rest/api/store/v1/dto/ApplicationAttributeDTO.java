package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class ApplicationAttributeDTO   {
  
    private String description = null;
    private String type = null;
    private String tooltip = null;
    private String required = null;
    private String attribute = null;
    private String hidden = null;

  /**
   * description of the application attribute
   **/
  public ApplicationAttributeDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "Sample description of the attribute", value = "description of the application attribute")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * type of the input element to display
   **/
  public ApplicationAttributeDTO type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(example = "text", value = "type of the input element to display")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   * tooltop to display for the input element
   **/
  public ApplicationAttributeDTO tooltip(String tooltip) {
    this.tooltip = tooltip;
    return this;
  }

  
  @ApiModelProperty(example = "Sample tooltip", value = "tooltop to display for the input element")
  @JsonProperty("tooltip")
  public String getTooltip() {
    return tooltip;
  }
  public void setTooltip(String tooltip) {
    this.tooltip = tooltip;
  }

  /**
   * whether this is a required attribute
   **/
  public ApplicationAttributeDTO required(String required) {
    this.required = required;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "whether this is a required attribute")
  @JsonProperty("required")
  public String getRequired() {
    return required;
  }
  public void setRequired(String required) {
    this.required = required;
  }

  /**
   * the name of the attribute
   **/
  public ApplicationAttributeDTO attribute(String attribute) {
    this.attribute = attribute;
    return this;
  }

  
  @ApiModelProperty(example = "External Reference Id", value = "the name of the attribute")
  @JsonProperty("attribute")
  public String getAttribute() {
    return attribute;
  }
  public void setAttribute(String attribute) {
    this.attribute = attribute;
  }

  /**
   * whether this is a hidden attribute
   **/
  public ApplicationAttributeDTO hidden(String hidden) {
    this.hidden = hidden;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "whether this is a hidden attribute")
  @JsonProperty("hidden")
  public String getHidden() {
    return hidden;
  }
  public void setHidden(String hidden) {
    this.hidden = hidden;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationAttributeDTO applicationAttribute = (ApplicationAttributeDTO) o;
    return Objects.equals(description, applicationAttribute.description) &&
        Objects.equals(type, applicationAttribute.type) &&
        Objects.equals(tooltip, applicationAttribute.tooltip) &&
        Objects.equals(required, applicationAttribute.required) &&
        Objects.equals(attribute, applicationAttribute.attribute) &&
        Objects.equals(hidden, applicationAttribute.hidden);
  }

  @Override
  public int hashCode() {
    return Objects.hash(description, type, tooltip, required, attribute, hidden);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationAttributeDTO {\n");
    
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    tooltip: ").append(toIndentedString(tooltip)).append("\n");
    sb.append("    required: ").append(toIndentedString(required)).append("\n");
    sb.append("    attribute: ").append(toIndentedString(attribute)).append("\n");
    sb.append("    hidden: ").append(toIndentedString(hidden)).append("\n");
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

