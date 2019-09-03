package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIProductURLsDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;



public class APIProductEndpointURLsDTO   {
  
    private String environmentName = null;
    private String environmentType = null;
    private APIProductURLsDTO urLs = null;

  /**
   **/
  public APIProductEndpointURLsDTO environmentName(String environmentName) {
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
  public APIProductEndpointURLsDTO environmentType(String environmentType) {
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
  public APIProductEndpointURLsDTO urLs(APIProductURLsDTO urLs) {
    this.urLs = urLs;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("URLs")
  public APIProductURLsDTO getUrLs() {
    return urLs;
  }
  public void setUrLs(APIProductURLsDTO urLs) {
    this.urLs = urLs;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIProductEndpointURLsDTO apIProductEndpointURLs = (APIProductEndpointURLsDTO) o;
    return Objects.equals(environmentName, apIProductEndpointURLs.environmentName) &&
        Objects.equals(environmentType, apIProductEndpointURLs.environmentType) &&
        Objects.equals(urLs, apIProductEndpointURLs.urLs);
  }

  @Override
  public int hashCode() {
    return Objects.hash(environmentName, environmentType, urLs);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIProductEndpointURLsDTO {\n");
    
    sb.append("    environmentName: ").append(toIndentedString(environmentName)).append("\n");
    sb.append("    environmentType: ").append(toIndentedString(environmentType)).append("\n");
    sb.append("    urLs: ").append(toIndentedString(urLs)).append("\n");
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

