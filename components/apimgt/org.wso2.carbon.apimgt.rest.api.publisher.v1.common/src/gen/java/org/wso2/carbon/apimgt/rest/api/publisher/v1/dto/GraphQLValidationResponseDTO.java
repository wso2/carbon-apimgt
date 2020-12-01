package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GraphQLValidationResponseGraphQLInfoDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class GraphQLValidationResponseDTO   {
  
    private Boolean isValid = null;
    private String errorMessage = null;
    private GraphQLValidationResponseGraphQLInfoDTO graphQLInfo = null;

  /**
   * This attribute declares whether this definition is valid or not. 
   **/
  public GraphQLValidationResponseDTO isValid(Boolean isValid) {
    this.isValid = isValid;
    return this;
  }

  
  @ApiModelProperty(example = "true", required = true, value = "This attribute declares whether this definition is valid or not. ")
  @JsonProperty("isValid")
  @NotNull
  public Boolean isIsValid() {
    return isValid;
  }
  public void setIsValid(Boolean isValid) {
    this.isValid = isValid;
  }

  /**
   * This attribute declares the validation error message 
   **/
  public GraphQLValidationResponseDTO errorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "This attribute declares the validation error message ")
  @JsonProperty("errorMessage")
  @NotNull
  public String getErrorMessage() {
    return errorMessage;
  }
  public void setErrorMessage(String errorMessage) {
    this.errorMessage = errorMessage;
  }

  /**
   **/
  public GraphQLValidationResponseDTO graphQLInfo(GraphQLValidationResponseGraphQLInfoDTO graphQLInfo) {
    this.graphQLInfo = graphQLInfo;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("graphQLInfo")
  public GraphQLValidationResponseGraphQLInfoDTO getGraphQLInfo() {
    return graphQLInfo;
  }
  public void setGraphQLInfo(GraphQLValidationResponseGraphQLInfoDTO graphQLInfo) {
    this.graphQLInfo = graphQLInfo;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GraphQLValidationResponseDTO graphQLValidationResponse = (GraphQLValidationResponseDTO) o;
    return Objects.equals(isValid, graphQLValidationResponse.isValid) &&
        Objects.equals(errorMessage, graphQLValidationResponse.errorMessage) &&
        Objects.equals(graphQLInfo, graphQLValidationResponse.graphQLInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isValid, errorMessage, graphQLInfo);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GraphQLValidationResponseDTO {\n");
    
    sb.append("    isValid: ").append(toIndentedString(isValid)).append("\n");
    sb.append("    errorMessage: ").append(toIndentedString(errorMessage)).append("\n");
    sb.append("    graphQLInfo: ").append(toIndentedString(graphQLInfo)).append("\n");
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

