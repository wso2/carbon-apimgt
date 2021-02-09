package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;

/**
 * CORS configuration for the API 
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "CORS configuration for the API ")

public class APICorsConfigurationDTO   {
  
    private Boolean corsConfigurationEnabled = false;
    private List<String> accessControlAllowOrigins = new ArrayList<String>();
    private Boolean accessControlAllowCredentials = false;
    private List<String> accessControlAllowHeaders = new ArrayList<String>();
    private List<String> accessControlAllowMethods = new ArrayList<String>();

  /**
   **/
  public APICorsConfigurationDTO corsConfigurationEnabled(Boolean corsConfigurationEnabled) {
    this.corsConfigurationEnabled = corsConfigurationEnabled;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("corsConfigurationEnabled")
  public Boolean isCorsConfigurationEnabled() {
    return corsConfigurationEnabled;
  }
  public void setCorsConfigurationEnabled(Boolean corsConfigurationEnabled) {
    this.corsConfigurationEnabled = corsConfigurationEnabled;
  }

  /**
   **/
  public APICorsConfigurationDTO accessControlAllowOrigins(List<String> accessControlAllowOrigins) {
    this.accessControlAllowOrigins = accessControlAllowOrigins;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("accessControlAllowOrigins")
  public List<String> getAccessControlAllowOrigins() {
    return accessControlAllowOrigins;
  }
  public void setAccessControlAllowOrigins(List<String> accessControlAllowOrigins) {
    this.accessControlAllowOrigins = accessControlAllowOrigins;
  }

  /**
   **/
  public APICorsConfigurationDTO accessControlAllowCredentials(Boolean accessControlAllowCredentials) {
    this.accessControlAllowCredentials = accessControlAllowCredentials;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("accessControlAllowCredentials")
  public Boolean isAccessControlAllowCredentials() {
    return accessControlAllowCredentials;
  }
  public void setAccessControlAllowCredentials(Boolean accessControlAllowCredentials) {
    this.accessControlAllowCredentials = accessControlAllowCredentials;
  }

  /**
   **/
  public APICorsConfigurationDTO accessControlAllowHeaders(List<String> accessControlAllowHeaders) {
    this.accessControlAllowHeaders = accessControlAllowHeaders;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("accessControlAllowHeaders")
  public List<String> getAccessControlAllowHeaders() {
    return accessControlAllowHeaders;
  }
  public void setAccessControlAllowHeaders(List<String> accessControlAllowHeaders) {
    this.accessControlAllowHeaders = accessControlAllowHeaders;
  }

  /**
   **/
  public APICorsConfigurationDTO accessControlAllowMethods(List<String> accessControlAllowMethods) {
    this.accessControlAllowMethods = accessControlAllowMethods;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("accessControlAllowMethods")
  public List<String> getAccessControlAllowMethods() {
    return accessControlAllowMethods;
  }
  public void setAccessControlAllowMethods(List<String> accessControlAllowMethods) {
    this.accessControlAllowMethods = accessControlAllowMethods;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APICorsConfigurationDTO apICorsConfiguration = (APICorsConfigurationDTO) o;
    return Objects.equals(corsConfigurationEnabled, apICorsConfiguration.corsConfigurationEnabled) &&
        Objects.equals(accessControlAllowOrigins, apICorsConfiguration.accessControlAllowOrigins) &&
        Objects.equals(accessControlAllowCredentials, apICorsConfiguration.accessControlAllowCredentials) &&
        Objects.equals(accessControlAllowHeaders, apICorsConfiguration.accessControlAllowHeaders) &&
        Objects.equals(accessControlAllowMethods, apICorsConfiguration.accessControlAllowMethods);
  }

  @Override
  public int hashCode() {
    return Objects.hash(corsConfigurationEnabled, accessControlAllowOrigins, accessControlAllowCredentials, accessControlAllowHeaders, accessControlAllowMethods);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APICorsConfigurationDTO {\n");
    
    sb.append("    corsConfigurationEnabled: ").append(toIndentedString(corsConfigurationEnabled)).append("\n");
    sb.append("    accessControlAllowOrigins: ").append(toIndentedString(accessControlAllowOrigins)).append("\n");
    sb.append("    accessControlAllowCredentials: ").append(toIndentedString(accessControlAllowCredentials)).append("\n");
    sb.append("    accessControlAllowHeaders: ").append(toIndentedString(accessControlAllowHeaders)).append("\n");
    sb.append("    accessControlAllowMethods: ").append(toIndentedString(accessControlAllowMethods)).append("\n");
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

