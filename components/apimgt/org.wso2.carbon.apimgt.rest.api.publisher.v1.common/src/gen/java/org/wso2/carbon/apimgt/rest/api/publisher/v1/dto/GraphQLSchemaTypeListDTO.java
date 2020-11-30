package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GraphQLSchemaTypeDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class GraphQLSchemaTypeListDTO   {
  
    private List<GraphQLSchemaTypeDTO> typeList = new ArrayList<GraphQLSchemaTypeDTO>();

  /**
   **/
  public GraphQLSchemaTypeListDTO typeList(List<GraphQLSchemaTypeDTO> typeList) {
    this.typeList = typeList;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("typeList")
  public List<GraphQLSchemaTypeDTO> getTypeList() {
    return typeList;
  }
  public void setTypeList(List<GraphQLSchemaTypeDTO> typeList) {
    this.typeList = typeList;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GraphQLSchemaTypeListDTO graphQLSchemaTypeList = (GraphQLSchemaTypeListDTO) o;
    return Objects.equals(typeList, graphQLSchemaTypeList.typeList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(typeList);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GraphQLSchemaTypeListDTO {\n");
    
    sb.append("    typeList: ").append(toIndentedString(typeList)).append("\n");
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

