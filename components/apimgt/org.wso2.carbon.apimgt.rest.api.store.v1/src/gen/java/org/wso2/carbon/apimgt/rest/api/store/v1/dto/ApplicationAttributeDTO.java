package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class ApplicationAttributeDTO   {
  
    private String description = null;
    private String required = null;
    private String attribute = null;
    private String hidden = null;
    private String _default = null;

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

  /**
   * default attribute value
   **/
  public ApplicationAttributeDTO _default(String _default) {
    this._default = _default;
    return this;
  }

  
  @ApiModelProperty(example = "Default Value", value = "default attribute value")
  @JsonProperty("default")
  public String getDefault() {
    return _default;
  }
  public void setDefault(String _default) {
    this._default = _default;
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
        Objects.equals(required, applicationAttribute.required) &&
        Objects.equals(attribute, applicationAttribute.attribute) &&
        Objects.equals(hidden, applicationAttribute.hidden) &&
        Objects.equals(_default, applicationAttribute._default);
  }

  @Override
  public int hashCode() {
    return Objects.hash(description, required, attribute, hidden, _default);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationAttributeDTO {\n");
    
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    required: ").append(toIndentedString(required)).append("\n");
    sb.append("    attribute: ").append(toIndentedString(attribute)).append("\n");
    sb.append("    hidden: ").append(toIndentedString(hidden)).append("\n");
    sb.append("    _default: ").append(toIndentedString(_default)).append("\n");
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

