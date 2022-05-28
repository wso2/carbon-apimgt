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



public class GraphQLCustomComplexityInfoDTO   {
  
    private String type = null;
    private String field = null;
    private Integer complexityValue = null;

  /**
   * The type found within the schema of the API 
   **/
  public GraphQLCustomComplexityInfoDTO type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(example = "Country", required = true, value = "The type found within the schema of the API ")
  @JsonProperty("type")
  @NotNull
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   * The field which is found under the type within the schema of the API 
   **/
  public GraphQLCustomComplexityInfoDTO field(String field) {
    this.field = field;
    return this;
  }

  
  @ApiModelProperty(example = "name", required = true, value = "The field which is found under the type within the schema of the API ")
  @JsonProperty("field")
  @NotNull
  public String getField() {
    return field;
  }
  public void setField(String field) {
    this.field = field;
  }

  /**
   * The complexity value allocated for the associated field under the specified type 
   **/
  public GraphQLCustomComplexityInfoDTO complexityValue(Integer complexityValue) {
    this.complexityValue = complexityValue;
    return this;
  }

  
  @ApiModelProperty(example = "1", required = true, value = "The complexity value allocated for the associated field under the specified type ")
  @JsonProperty("complexityValue")
  @NotNull
  public Integer getComplexityValue() {
    return complexityValue;
  }
  public void setComplexityValue(Integer complexityValue) {
    this.complexityValue = complexityValue;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GraphQLCustomComplexityInfoDTO graphQLCustomComplexityInfo = (GraphQLCustomComplexityInfoDTO) o;
    return Objects.equals(type, graphQLCustomComplexityInfo.type) &&
        Objects.equals(field, graphQLCustomComplexityInfo.field) &&
        Objects.equals(complexityValue, graphQLCustomComplexityInfo.complexityValue);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, field, complexityValue);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GraphQLCustomComplexityInfoDTO {\n");
    
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    field: ").append(toIndentedString(field)).append("\n");
    sb.append("    complexityValue: ").append(toIndentedString(complexityValue)).append("\n");
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

