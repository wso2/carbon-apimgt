package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * CORS configuration for the API 
 */
@ApiModel(description = "CORS configuration for the API ")
@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-10-26T15:09:45.077+05:30")
public class APICorsConfiguration   {
  private Boolean corsConfigurationEnabled = false;

  private List<String> accessControlAllowOrigins = new ArrayList<String>();

  private Boolean accessControlAllowCredentials = false;

  private List<String> accessControlAllowHeaders = new ArrayList<String>();

  private List<String> accessControlAllowMethods = new ArrayList<String>();

  public APICorsConfiguration corsConfigurationEnabled(Boolean corsConfigurationEnabled) {
    this.corsConfigurationEnabled = corsConfigurationEnabled;
    return this;
  }

   /**
   * Get corsConfigurationEnabled
   * @return corsConfigurationEnabled
  **/
  @ApiModelProperty(value = "")
  public Boolean getCorsConfigurationEnabled() {
    return corsConfigurationEnabled;
  }

  public void setCorsConfigurationEnabled(Boolean corsConfigurationEnabled) {
    this.corsConfigurationEnabled = corsConfigurationEnabled;
  }

  public APICorsConfiguration accessControlAllowOrigins(List<String> accessControlAllowOrigins) {
    this.accessControlAllowOrigins = accessControlAllowOrigins;
    return this;
  }

  public APICorsConfiguration addAccessControlAllowOriginsItem(String accessControlAllowOriginsItem) {
    this.accessControlAllowOrigins.add(accessControlAllowOriginsItem);
    return this;
  }

   /**
   * Get accessControlAllowOrigins
   * @return accessControlAllowOrigins
  **/
  @ApiModelProperty(value = "")
  public List<String> getAccessControlAllowOrigins() {
    return accessControlAllowOrigins;
  }

  public void setAccessControlAllowOrigins(List<String> accessControlAllowOrigins) {
    this.accessControlAllowOrigins = accessControlAllowOrigins;
  }

  public APICorsConfiguration accessControlAllowCredentials(Boolean accessControlAllowCredentials) {
    this.accessControlAllowCredentials = accessControlAllowCredentials;
    return this;
  }

   /**
   * Get accessControlAllowCredentials
   * @return accessControlAllowCredentials
  **/
  @ApiModelProperty(value = "")
  public Boolean getAccessControlAllowCredentials() {
    return accessControlAllowCredentials;
  }

  public void setAccessControlAllowCredentials(Boolean accessControlAllowCredentials) {
    this.accessControlAllowCredentials = accessControlAllowCredentials;
  }

  public APICorsConfiguration accessControlAllowHeaders(List<String> accessControlAllowHeaders) {
    this.accessControlAllowHeaders = accessControlAllowHeaders;
    return this;
  }

  public APICorsConfiguration addAccessControlAllowHeadersItem(String accessControlAllowHeadersItem) {
    this.accessControlAllowHeaders.add(accessControlAllowHeadersItem);
    return this;
  }

   /**
   * Get accessControlAllowHeaders
   * @return accessControlAllowHeaders
  **/
  @ApiModelProperty(value = "")
  public List<String> getAccessControlAllowHeaders() {
    return accessControlAllowHeaders;
  }

  public void setAccessControlAllowHeaders(List<String> accessControlAllowHeaders) {
    this.accessControlAllowHeaders = accessControlAllowHeaders;
  }

  public APICorsConfiguration accessControlAllowMethods(List<String> accessControlAllowMethods) {
    this.accessControlAllowMethods = accessControlAllowMethods;
    return this;
  }

  public APICorsConfiguration addAccessControlAllowMethodsItem(String accessControlAllowMethodsItem) {
    this.accessControlAllowMethods.add(accessControlAllowMethodsItem);
    return this;
  }

   /**
   * Get accessControlAllowMethods
   * @return accessControlAllowMethods
  **/
  @ApiModelProperty(value = "")
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
    APICorsConfiguration aPICorsConfiguration = (APICorsConfiguration) o;
    return Objects.equals(this.corsConfigurationEnabled, aPICorsConfiguration.corsConfigurationEnabled) &&
        Objects.equals(this.accessControlAllowOrigins, aPICorsConfiguration.accessControlAllowOrigins) &&
        Objects.equals(this.accessControlAllowCredentials, aPICorsConfiguration.accessControlAllowCredentials) &&
        Objects.equals(this.accessControlAllowHeaders, aPICorsConfiguration.accessControlAllowHeaders) &&
        Objects.equals(this.accessControlAllowMethods, aPICorsConfiguration.accessControlAllowMethods);
  }

  @Override
  public int hashCode() {
    return Objects.hash(corsConfigurationEnabled, accessControlAllowOrigins, accessControlAllowCredentials, accessControlAllowHeaders, accessControlAllowMethods);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APICorsConfiguration {\n");
    
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

