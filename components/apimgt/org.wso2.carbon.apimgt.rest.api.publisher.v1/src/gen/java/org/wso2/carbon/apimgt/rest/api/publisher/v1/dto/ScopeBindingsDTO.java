package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class ScopeBindingsDTO   {
  
    private String type = null;
    private List<String> values = new ArrayList<>();

  /**
   * Type of binding role / permission 
   **/
  public ScopeBindingsDTO type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(value = "Type of binding role / permission ")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   **/
  public ScopeBindingsDTO values(List<String> values) {
    this.values = values;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("values")
  public List<String> getValues() {
    return values;
  }
  public void setValues(List<String> values) {
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
    ScopeBindingsDTO scopeBindings = (ScopeBindingsDTO) o;
    return Objects.equals(type, scopeBindings.type) &&
        Objects.equals(values, scopeBindings.values);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, values);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ScopeBindingsDTO {\n");
    
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
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

