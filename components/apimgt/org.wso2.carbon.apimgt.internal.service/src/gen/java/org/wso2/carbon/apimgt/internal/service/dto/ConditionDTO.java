package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class ConditionDTO   {
  
    private String conditionType = null;
    private String name = null;
    private String value = null;
    private Boolean isInverted = null;

  /**
   **/
  public ConditionDTO conditionType(String conditionType) {
    this.conditionType = conditionType;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("conditionType")
  public String getConditionType() {
    return conditionType;
  }
  public void setConditionType(String conditionType) {
    this.conditionType = conditionType;
  }

  /**
   **/
  public ConditionDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public ConditionDTO value(String value) {
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
  public ConditionDTO isInverted(Boolean isInverted) {
    this.isInverted = isInverted;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("isInverted")
  public Boolean isIsInverted() {
    return isInverted;
  }
  public void setIsInverted(Boolean isInverted) {
    this.isInverted = isInverted;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ConditionDTO condition = (ConditionDTO) o;
    return Objects.equals(conditionType, condition.conditionType) &&
        Objects.equals(name, condition.name) &&
        Objects.equals(value, condition.value) &&
        Objects.equals(isInverted, condition.isInverted);
  }

  @Override
  public int hashCode() {
    return Objects.hash(conditionType, name, value, isInverted);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConditionDTO {\n");
    
    sb.append("    conditionType: ").append(toIndentedString(conditionType)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
    sb.append("    isInverted: ").append(toIndentedString(isInverted)).append("\n");
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

