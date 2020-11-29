package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

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



public class LifecycleStateCheckItemsDTO   {
  
    private String name = null;
    private Boolean value = null;
    private List<String> requiredStates = new ArrayList<String>();

  /**
   **/
  public LifecycleStateCheckItemsDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "Deprecate old versions after publishing the API", value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public LifecycleStateCheckItemsDTO value(Boolean value) {
    this.value = value;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "")
  @JsonProperty("value")
  public Boolean isValue() {
    return value;
  }
  public void setValue(Boolean value) {
    this.value = value;
  }

  /**
   **/
  public LifecycleStateCheckItemsDTO requiredStates(List<String> requiredStates) {
    this.requiredStates = requiredStates;
    return this;
  }

  
  @ApiModelProperty(example = "[]", value = "")
  @JsonProperty("requiredStates")
  public List<String> getRequiredStates() {
    return requiredStates;
  }
  public void setRequiredStates(List<String> requiredStates) {
    this.requiredStates = requiredStates;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LifecycleStateCheckItemsDTO lifecycleStateCheckItems = (LifecycleStateCheckItemsDTO) o;
    return Objects.equals(name, lifecycleStateCheckItems.name) &&
        Objects.equals(value, lifecycleStateCheckItems.value) &&
        Objects.equals(requiredStates, lifecycleStateCheckItems.requiredStates);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, value, requiredStates);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LifecycleStateCheckItemsDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    value: ").append(toIndentedString(value)).append("\n");
    sb.append("    requiredStates: ").append(toIndentedString(requiredStates)).append("\n");
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

