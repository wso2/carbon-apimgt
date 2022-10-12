package org.wso2.apk.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.validation.Valid;

@ApiModel(description = "Summary of the GraphQL including the basic information")

public class GraphQLValidationResponseGraphQLInfoDTO   {
  
    private List<APIOperationsDTO> operations = new ArrayList<APIOperationsDTO>();
    private GraphQLSchemaDTO graphQLSchema = null;

  /**
   **/
  public GraphQLValidationResponseGraphQLInfoDTO operations(List<APIOperationsDTO> operations) {
    this.operations = operations;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("operations")
  public List<APIOperationsDTO> getOperations() {
    return operations;
  }
  public void setOperations(List<APIOperationsDTO> operations) {
    this.operations = operations;
  }

  /**
   **/
  public GraphQLValidationResponseGraphQLInfoDTO graphQLSchema(GraphQLSchemaDTO graphQLSchema) {
    this.graphQLSchema = graphQLSchema;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("graphQLSchema")
  public GraphQLSchemaDTO getGraphQLSchema() {
    return graphQLSchema;
  }
  public void setGraphQLSchema(GraphQLSchemaDTO graphQLSchema) {
    this.graphQLSchema = graphQLSchema;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GraphQLValidationResponseGraphQLInfoDTO graphQLValidationResponseGraphQLInfo = (GraphQLValidationResponseGraphQLInfoDTO) o;
    return Objects.equals(operations, graphQLValidationResponseGraphQLInfo.operations) &&
        Objects.equals(graphQLSchema, graphQLValidationResponseGraphQLInfo.graphQLSchema);
  }

  @Override
  public int hashCode() {
    return Objects.hash(operations, graphQLSchema);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GraphQLValidationResponseGraphQLInfoDTO {\n");
    
    sb.append("    operations: ").append(toIndentedString(operations)).append("\n");
    sb.append("    graphQLSchema: ").append(toIndentedString(graphQLSchema)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

