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



public class GraphQLSchemaDTO   {
  
    private String name = null;
    private String schemaDefinition = null;

  /**
   **/
  public GraphQLSchemaDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "admin--HackerNewsAPI.graphql", required = true, value = "")
  @JsonProperty("name")
  @NotNull
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public GraphQLSchemaDTO schemaDefinition(String schemaDefinition) {
    this.schemaDefinition = schemaDefinition;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("schemaDefinition")
  public String getSchemaDefinition() {
    return schemaDefinition;
  }
  public void setSchemaDefinition(String schemaDefinition) {
    this.schemaDefinition = schemaDefinition;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GraphQLSchemaDTO graphQLSchema = (GraphQLSchemaDTO) o;
    return Objects.equals(name, graphQLSchema.name) &&
        Objects.equals(schemaDefinition, graphQLSchema.schemaDefinition);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, schemaDefinition);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GraphQLSchemaDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    schemaDefinition: ").append(toIndentedString(schemaDefinition)).append("\n");
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

