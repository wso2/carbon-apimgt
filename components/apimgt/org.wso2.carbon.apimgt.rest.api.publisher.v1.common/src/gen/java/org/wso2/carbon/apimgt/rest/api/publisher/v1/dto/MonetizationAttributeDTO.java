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



public class MonetizationAttributeDTO   {
  
    private Boolean required = null;
    private String name = null;
    private String displayName = null;
    private String description = null;
    private Boolean hidden = null;
    private String _default = null;

  /**
   * Is attribute required 
   **/
  public MonetizationAttributeDTO required(Boolean required) {
    this.required = required;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "Is attribute required ")
  @JsonProperty("required")
  public Boolean isRequired() {
    return required;
  }
  public void setRequired(Boolean required) {
    this.required = required;
  }

  /**
   * Name of the attribute 
   **/
  public MonetizationAttributeDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(value = "Name of the attribute ")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Display name of the attribute 
   **/
  public MonetizationAttributeDTO displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(value = "Display name of the attribute ")
  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   * Description of the attribute 
   **/
  public MonetizationAttributeDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(value = "Description of the attribute ")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Is attribute hidden 
   **/
  public MonetizationAttributeDTO hidden(Boolean hidden) {
    this.hidden = hidden;
    return this;
  }

  
  @ApiModelProperty(value = "Is attribute hidden ")
  @JsonProperty("hidden")
  public Boolean isHidden() {
    return hidden;
  }
  public void setHidden(Boolean hidden) {
    this.hidden = hidden;
  }

  /**
   * Default value of the attribute 
   **/
  public MonetizationAttributeDTO _default(String _default) {
    this._default = _default;
    return this;
  }

  
  @ApiModelProperty(value = "Default value of the attribute ")
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
    MonetizationAttributeDTO monetizationAttribute = (MonetizationAttributeDTO) o;
    return Objects.equals(required, monetizationAttribute.required) &&
        Objects.equals(name, monetizationAttribute.name) &&
        Objects.equals(displayName, monetizationAttribute.displayName) &&
        Objects.equals(description, monetizationAttribute.description) &&
        Objects.equals(hidden, monetizationAttribute.hidden) &&
        Objects.equals(_default, monetizationAttribute._default);
  }

  @Override
  public int hashCode() {
    return Objects.hash(required, name, displayName, description, hidden, _default);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MonetizationAttributeDTO {\n");
    
    sb.append("    required: ").append(toIndentedString(required)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
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

