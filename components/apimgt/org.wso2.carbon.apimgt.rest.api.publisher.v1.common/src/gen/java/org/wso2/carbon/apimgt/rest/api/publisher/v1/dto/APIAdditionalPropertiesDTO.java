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



public class APIAdditionalPropertiesDTO   {
  
    private String value = null;
    private Boolean display = false;

  /**
   **/
  public APIAdditionalPropertiesDTO value(String value) {
    this.value = value;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("value")
  public String getValue() {
    return value;
  }
  public void setValue(String value) {
    this.value = value;
  }

  /**
   **/
  public APIAdditionalPropertiesDTO display(Boolean display) {
    this.display = display;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("display")
  public Boolean isDisplay() {
    return display;
  }
  public void setDisplay(Boolean display) {
    this.display = display;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIAdditionalPropertiesDTO apIAdditionalProperties = (APIAdditionalPropertiesDTO) o;
    return Objects.equals(value, apIAdditionalProperties.value) &&
        Objects.equals(display, apIAdditionalProperties.display);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, display);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIAdditionalPropertiesDTO {\n");
    
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
    sb.append("    display: ").append(toIndentedString(display)).append("\n");
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

