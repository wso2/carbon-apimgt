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



public class GraphQLSchemaTypeDTO   {
  
    private String type = null;
    private List<String> fieldList = new ArrayList<String>();

  /**
   * Type found within the GraphQL Schema 
   **/
  public GraphQLSchemaTypeDTO type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(example = "Country", value = "Type found within the GraphQL Schema ")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   * Array of fields under current type 
   **/
  public GraphQLSchemaTypeDTO fieldList(List<String> fieldList) {
    this.fieldList = fieldList;
    return this;
  }

  
  @ApiModelProperty(example = "[\"code\",\"name\"]", value = "Array of fields under current type ")
  @JsonProperty("fieldList")
  public List<String> getFieldList() {
    return fieldList;
  }
  public void setFieldList(List<String> fieldList) {
    this.fieldList = fieldList;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GraphQLSchemaTypeDTO graphQLSchemaType = (GraphQLSchemaTypeDTO) o;
    return Objects.equals(type, graphQLSchemaType.type) &&
        Objects.equals(fieldList, graphQLSchemaType.fieldList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, fieldList);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GraphQLSchemaTypeDTO {\n");
    
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    fieldList: ").append(toIndentedString(fieldList)).append("\n");
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

