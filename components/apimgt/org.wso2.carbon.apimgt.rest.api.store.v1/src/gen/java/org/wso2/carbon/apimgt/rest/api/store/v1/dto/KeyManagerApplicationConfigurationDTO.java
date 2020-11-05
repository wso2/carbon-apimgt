package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class KeyManagerApplicationConfigurationDTO   {
  
    private String name = null;
    private String label = null;
    private String type = null;
    private Boolean required = null;
    private Boolean mask = null;
    private Boolean multiple = null;
    private String tooltip = null;
    private Object _default = null;
    private List<Object> values = new ArrayList<Object>();

  /**
   **/
  public KeyManagerApplicationConfigurationDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "consumer_key", value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public KeyManagerApplicationConfigurationDTO label(String label) {
    this.label = label;
    return this;
  }

  
  @ApiModelProperty(example = "Consumer Key", value = "")
  @JsonProperty("label")
  public String getLabel() {
    return label;
  }
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   **/
  public KeyManagerApplicationConfigurationDTO type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(example = "select", value = "")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   **/
  public KeyManagerApplicationConfigurationDTO required(Boolean required) {
    this.required = required;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "")
  @JsonProperty("required")
  public Boolean isRequired() {
    return required;
  }
  public void setRequired(Boolean required) {
    this.required = required;
  }

  /**
   **/
  public KeyManagerApplicationConfigurationDTO mask(Boolean mask) {
    this.mask = mask;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "")
  @JsonProperty("mask")
  public Boolean isMask() {
    return mask;
  }
  public void setMask(Boolean mask) {
    this.mask = mask;
  }

  /**
   **/
  public KeyManagerApplicationConfigurationDTO multiple(Boolean multiple) {
    this.multiple = multiple;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "")
  @JsonProperty("multiple")
  public Boolean isMultiple() {
    return multiple;
  }
  public void setMultiple(Boolean multiple) {
    this.multiple = multiple;
  }

  /**
   **/
  public KeyManagerApplicationConfigurationDTO tooltip(String tooltip) {
    this.tooltip = tooltip;
    return this;
  }

  
  @ApiModelProperty(example = "Enter username to connect to key manager", value = "")
  @JsonProperty("tooltip")
  public String getTooltip() {
    return tooltip;
  }
  public void setTooltip(String tooltip) {
    this.tooltip = tooltip;
  }

  /**
   **/
  public KeyManagerApplicationConfigurationDTO _default(Object _default) {
    this._default = _default;
    return this;
  }

  
  @ApiModelProperty(example = "\"admin\"", value = "")
  @JsonProperty("default")
  public Object getDefault() {
    return _default;
  }
  public void setDefault(Object _default) {
    this._default = _default;
  }

  /**
   **/
  public KeyManagerApplicationConfigurationDTO values(List<Object> values) {
    this.values = values;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("values")
  public List<Object> getValues() {
    return values;
  }
  public void setValues(List<Object> values) {
    this.values = values;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KeyManagerApplicationConfigurationDTO keyManagerApplicationConfiguration = (KeyManagerApplicationConfigurationDTO) o;
    return Objects.equals(name, keyManagerApplicationConfiguration.name) &&
        Objects.equals(label, keyManagerApplicationConfiguration.label) &&
        Objects.equals(type, keyManagerApplicationConfiguration.type) &&
        Objects.equals(required, keyManagerApplicationConfiguration.required) &&
        Objects.equals(mask, keyManagerApplicationConfiguration.mask) &&
        Objects.equals(multiple, keyManagerApplicationConfiguration.multiple) &&
        Objects.equals(tooltip, keyManagerApplicationConfiguration.tooltip) &&
        Objects.equals(_default, keyManagerApplicationConfiguration._default) &&
        Objects.equals(values, keyManagerApplicationConfiguration.values);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, label, type, required, mask, multiple, tooltip, _default, values);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class KeyManagerApplicationConfigurationDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    label: ").append(toIndentedString(label)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    required: ").append(toIndentedString(required)).append("\n");
    sb.append("    mask: ").append(toIndentedString(mask)).append("\n");
    sb.append("    multiple: ").append(toIndentedString(multiple)).append("\n");
    sb.append("    tooltip: ").append(toIndentedString(tooltip)).append("\n");
    sb.append("    _default: ").append(toIndentedString(_default)).append("\n");
    sb.append("    values: ").append(toIndentedString(values)).append("\n");
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

