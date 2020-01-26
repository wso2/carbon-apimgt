package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class GraphQLLimitationStatusDTO   {
  
    private String limitationType = null;
    private Boolean enabled = null;

  /**
   * Type of the limitation 
   **/
  public GraphQLLimitationStatusDTO limitationType(String limitationType) {
    this.limitationType = limitationType;
    return this;
  }

  
  @ApiModelProperty(example = "depth", value = "Type of the limitation ")
  @JsonProperty("limitationType")
  public String getLimitationType() {
    return limitationType;
  }
  public void setLimitationType(String limitationType) {
    this.limitationType = limitationType;
  }

  /**
   * Indicates if limitation type is enabled 
   **/
  public GraphQLLimitationStatusDTO enabled(Boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "Indicates if limitation type is enabled ")
  @JsonProperty("enabled")
  public Boolean isEnabled() {
    return enabled;
  }
  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GraphQLLimitationStatusDTO graphQLLimitationStatus = (GraphQLLimitationStatusDTO) o;
    return Objects.equals(limitationType, graphQLLimitationStatus.limitationType) &&
        Objects.equals(enabled, graphQLLimitationStatus.enabled);
  }

  @Override
  public int hashCode() {
    return Objects.hash(limitationType, enabled);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GraphQLLimitationStatusDTO {\n");
    
    sb.append("    limitationType: ").append(toIndentedString(limitationType)).append("\n");
    sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
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

