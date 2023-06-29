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



public class SettingsCustomPropertiesDTO   {
  
    private String name = null;
    private String description = null;
    private Boolean required = null;

  /**
   * Custom property name 
   **/
  public SettingsCustomPropertiesDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "Department", value = "Custom property name ")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Description of custom property
   **/
  public SettingsCustomPropertiesDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "Relevant Department", value = "Description of custom property")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   **/
  public SettingsCustomPropertiesDTO required(Boolean required) {
    this.required = required;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "")
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
    SettingsCustomPropertiesDTO settingsCustomProperties = (SettingsCustomPropertiesDTO) o;
    return Objects.equals(name, settingsCustomProperties.name) &&
        Objects.equals(description, settingsCustomProperties.description) &&
        Objects.equals(required, settingsCustomProperties.required);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, description, required);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SettingsCustomPropertiesDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
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

