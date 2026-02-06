package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class AppConfigConstraintsDTO   {
  
    private String name = null;
    private String label = null;
    private String type = null;
    private Boolean multiple = null;
    private String tooltip = null;
    private String constraintType = null;
    private List<Object> values = new ArrayList<Object>();
    private Object _default = null;

  /**
   **/
  public AppConfigConstraintsDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "application_access_token_expiry_time", value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public AppConfigConstraintsDTO label(String label) {
    this.label = label;
    return this;
  }

  
  @ApiModelProperty(example = "Access Token Expiry", value = "")
  @JsonProperty("label")
  public String getLabel() {
    return label;
  }
  public void setLabel(String label) {
    this.label = label;
  }

  /**
   **/
  public AppConfigConstraintsDTO type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(example = "input", value = "")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   **/
  public AppConfigConstraintsDTO multiple(Boolean multiple) {
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
  public AppConfigConstraintsDTO tooltip(String tooltip) {
    this.tooltip = tooltip;
    return this;
  }

  
  @ApiModelProperty(example = "Define the valid range for token expiration in seconds.", value = "")
  @JsonProperty("tooltip")
  public String getTooltip() {
    return tooltip;
  }
  public void setTooltip(String tooltip) {
    this.tooltip = tooltip;
  }

  /**
   **/
  public AppConfigConstraintsDTO constraintType(String constraintType) {
    this.constraintType = constraintType;
    return this;
  }

  
  @ApiModelProperty(example = "RANGE", value = "")
  @JsonProperty("constraintType")
  public String getConstraintType() {
    return constraintType;
  }
  public void setConstraintType(String constraintType) {
    this.constraintType = constraintType;
  }

  /**
   **/
  public AppConfigConstraintsDTO values(List<Object> values) {
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

  /**
   **/
  public AppConfigConstraintsDTO _default(Object _default) {
    this._default = _default;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("default")
  public Object getDefault() {
    return _default;
  }
  public void setDefault(Object _default) {
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
    AppConfigConstraintsDTO appConfigConstraints = (AppConfigConstraintsDTO) o;
    return Objects.equals(name, appConfigConstraints.name) &&
        Objects.equals(label, appConfigConstraints.label) &&
        Objects.equals(type, appConfigConstraints.type) &&
        Objects.equals(multiple, appConfigConstraints.multiple) &&
        Objects.equals(tooltip, appConfigConstraints.tooltip) &&
        Objects.equals(constraintType, appConfigConstraints.constraintType) &&
        Objects.equals(values, appConfigConstraints.values) &&
        Objects.equals(_default, appConfigConstraints._default);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, label, type, multiple, tooltip, constraintType, values, _default);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AppConfigConstraintsDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    label: ").append(toIndentedString(label)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    multiple: ").append(toIndentedString(multiple)).append("\n");
    sb.append("    tooltip: ").append(toIndentedString(tooltip)).append("\n");
    sb.append("    constraintType: ").append(toIndentedString(constraintType)).append("\n");
    sb.append("    values: ").append(toIndentedString(values)).append("\n");
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

