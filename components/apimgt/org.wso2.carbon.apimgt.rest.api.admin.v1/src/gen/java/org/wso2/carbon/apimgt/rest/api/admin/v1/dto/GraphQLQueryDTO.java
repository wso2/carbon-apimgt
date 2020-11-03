package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class GraphQLQueryDTO   {
  
    private Integer graphQLMaxComplexity = null;
    private Integer graphQLMaxDepth = null;

  /**
   * Maximum Complexity of the GraphQL query
   **/
  public GraphQLQueryDTO graphQLMaxComplexity(Integer graphQLMaxComplexity) {
    this.graphQLMaxComplexity = graphQLMaxComplexity;
    return this;
  }

  
  @ApiModelProperty(example = "400", value = "Maximum Complexity of the GraphQL query")
  @JsonProperty("graphQLMaxComplexity")
  public Integer getGraphQLMaxComplexity() {
    return graphQLMaxComplexity;
  }
  public void setGraphQLMaxComplexity(Integer graphQLMaxComplexity) {
    this.graphQLMaxComplexity = graphQLMaxComplexity;
  }

  /**
   * Maximum Depth of the GraphQL query
   **/
  public GraphQLQueryDTO graphQLMaxDepth(Integer graphQLMaxDepth) {
    this.graphQLMaxDepth = graphQLMaxDepth;
    return this;
  }

  
  @ApiModelProperty(example = "10", value = "Maximum Depth of the GraphQL query")
  @JsonProperty("graphQLMaxDepth")
  public Integer getGraphQLMaxDepth() {
    return graphQLMaxDepth;
  }
  public void setGraphQLMaxDepth(Integer graphQLMaxDepth) {
    this.graphQLMaxDepth = graphQLMaxDepth;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GraphQLQueryDTO graphQLQuery = (GraphQLQueryDTO) o;
    return Objects.equals(graphQLMaxComplexity, graphQLQuery.graphQLMaxComplexity) &&
        Objects.equals(graphQLMaxDepth, graphQLQuery.graphQLMaxDepth);
  }

  @Override
  public int hashCode() {
    return Objects.hash(graphQLMaxComplexity, graphQLMaxDepth);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GraphQLQueryDTO {\n");
    
    sb.append("    graphQLMaxComplexity: ").append(toIndentedString(graphQLMaxComplexity)).append("\n");
    sb.append("    graphQLMaxDepth: ").append(toIndentedString(graphQLMaxDepth)).append("\n");
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

