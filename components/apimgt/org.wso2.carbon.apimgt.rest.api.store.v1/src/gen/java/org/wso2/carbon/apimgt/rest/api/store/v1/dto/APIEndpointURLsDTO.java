package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIDefaultVersionURLsDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIURLsDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class APIEndpointURLsDTO   {
  
    private String environmentName = null;
    private String environmentType = null;
    private APIURLsDTO urLs = null;
    private APIDefaultVersionURLsDTO defaultVersionURLs = null;

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
  public APIEndpointURLsDTO urLs(APIURLsDTO urLs) {
    this.urLs = urLs;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("URLs")
  public APIURLsDTO getUrLs() {
    return urLs;
  }
  public void setUrLs(APIURLsDTO urLs) {
    this.urLs = urLs;
  }

  /**
   **/
  public APIEndpointURLsDTO defaultVersionURLs(APIDefaultVersionURLsDTO defaultVersionURLs) {
    this.defaultVersionURLs = defaultVersionURLs;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("defaultVersionURLs")
  public APIDefaultVersionURLsDTO getDefaultVersionURLs() {
    return defaultVersionURLs;
  }
  public void setDefaultVersionURLs(APIDefaultVersionURLsDTO defaultVersionURLs) {
    this.defaultVersionURLs = defaultVersionURLs;
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
        Objects.equals(urLs, apIEndpointURLs.urLs) &&
        Objects.equals(defaultVersionURLs, apIEndpointURLs.defaultVersionURLs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(environmentName, environmentType, urLs, defaultVersionURLs);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIEndpointURLsDTO {\n");
    
    sb.append("    environmentName: ").append(toIndentedString(environmentName)).append("\n");
    sb.append("    environmentType: ").append(toIndentedString(environmentType)).append("\n");
    sb.append("    urLs: ").append(toIndentedString(urLs)).append("\n");
    sb.append("    defaultVersionURLs: ").append(toIndentedString(defaultVersionURLs)).append("\n");
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

