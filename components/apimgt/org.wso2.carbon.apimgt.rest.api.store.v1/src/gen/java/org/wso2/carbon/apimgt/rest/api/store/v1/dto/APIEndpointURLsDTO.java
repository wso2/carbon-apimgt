package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIEnvironmentURLsDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;



public class APIEndpointURLsDTO   {
  
    private String environmentName = null;
    private String environmentType = null;
    private APIEnvironmentURLsDTO environmentURLs = null;

  /**
   **/
  public APIEndpointURLsDTO environmentName(String environmentName) {
    this.environmentName = environmentName;
    return this;
  }

  
  @ApiModelProperty(example = "Production and Sandbox", value = "")
  @JsonProperty("environmentName")
  public String getEnvironmentName() {
    return environmentName;
  }
  public void setEnvironmentName(String environmentName) {
    this.environmentName = environmentName;
  }

  /**
   **/
  public APIEndpointURLsDTO environmentType(String environmentType) {
    this.environmentType = environmentType;
    return this;
  }

  
  @ApiModelProperty(example = "hybrid", value = "")
  @JsonProperty("environmentType")
  public String getEnvironmentType() {
    return environmentType;
  }
  public void setEnvironmentType(String environmentType) {
    this.environmentType = environmentType;
  }

  /**
   **/
  public APIEndpointURLsDTO environmentURLs(APIEnvironmentURLsDTO environmentURLs) {
    this.environmentURLs = environmentURLs;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("environmentURLs")
  public APIEnvironmentURLsDTO getEnvironmentURLs() {
    return environmentURLs;
  }
  public void setEnvironmentURLs(APIEnvironmentURLsDTO environmentURLs) {
    this.environmentURLs = environmentURLs;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIEndpointURLsDTO apIEndpointURLs = (APIEndpointURLsDTO) o;
    return Objects.equals(environmentName, apIEndpointURLs.environmentName) &&
        Objects.equals(environmentType, apIEndpointURLs.environmentType) &&
        Objects.equals(environmentURLs, apIEndpointURLs.environmentURLs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(environmentName, environmentType, environmentURLs);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIEndpointURLsDTO {\n");
    
    sb.append("    environmentName: ").append(toIndentedString(environmentName)).append("\n");
    sb.append("    environmentType: ").append(toIndentedString(environmentType)).append("\n");
    sb.append("    environmentURLs: ").append(toIndentedString(environmentURLs)).append("\n");
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

