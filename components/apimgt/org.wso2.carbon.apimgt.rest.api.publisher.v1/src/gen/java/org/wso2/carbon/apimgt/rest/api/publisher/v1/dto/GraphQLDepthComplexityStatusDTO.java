package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class GraphQLDepthComplexityStatusDTO   {
  
    private Boolean depthEnabled = null;
    private Boolean complexityEnabled = null;

  /**
   * Indicates if depth check is enabled 
   **/
  public GraphQLDepthComplexityStatusDTO depthEnabled(Boolean depthEnabled) {
    this.depthEnabled = depthEnabled;
    return this;
  }

  
  @ApiModelProperty(value = "Indicates if depth check is enabled ")
  @JsonProperty("depthEnabled")
  public Boolean isDepthEnabled() {
    return depthEnabled;
  }
  public void setDepthEnabled(Boolean depthEnabled) {
    this.depthEnabled = depthEnabled;
  }

  /**
   * Indicates if complexity check is enabled 
   **/
  public GraphQLDepthComplexityStatusDTO complexityEnabled(Boolean complexityEnabled) {
    this.complexityEnabled = complexityEnabled;
    return this;
  }

  
  @ApiModelProperty(value = "Indicates if complexity check is enabled ")
  @JsonProperty("complexityEnabled")
  public Boolean isComplexityEnabled() {
    return complexityEnabled;
  }
  public void setComplexityEnabled(Boolean complexityEnabled) {
    this.complexityEnabled = complexityEnabled;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GraphQLDepthComplexityStatusDTO graphQLDepthComplexityStatus = (GraphQLDepthComplexityStatusDTO) o;
    return Objects.equals(depthEnabled, graphQLDepthComplexityStatus.depthEnabled) &&
        Objects.equals(complexityEnabled, graphQLDepthComplexityStatus.complexityEnabled);
  }

  @Override
  public int hashCode() {
    return Objects.hash(depthEnabled, complexityEnabled);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GraphQLDepthComplexityStatusDTO {\n");
    
    sb.append("    depthEnabled: ").append(toIndentedString(depthEnabled)).append("\n");
    sb.append("    complexityEnabled: ").append(toIndentedString(complexityEnabled)).append("\n");
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

