package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * CORS configuration for the API 
 */
@ApiModel(description = "CORS configuration for the API ")
public class API_corsConfigurationDTO   {
  @SerializedName("corsConfigurationEnabled")
  private Boolean corsConfigurationEnabled = false;

  @SerializedName("accessControlAllowOrigins")
  private List<String> accessControlAllowOrigins = new ArrayList<String>();

  @SerializedName("accessControlAllowCredentials")
  private Boolean accessControlAllowCredentials = false;

  @SerializedName("accessControlAllowHeaders")
  private List<String> accessControlAllowHeaders = new ArrayList<String>();

  @SerializedName("accessControlAllowMethods")
  private List<String> accessControlAllowMethods = new ArrayList<String>();

  public API_corsConfigurationDTO corsConfigurationEnabled(Boolean corsConfigurationEnabled) {
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

  public API_corsConfigurationDTO accessControlAllowOrigins(List<String> accessControlAllowOrigins) {
    this.accessControlAllowOrigins = accessControlAllowOrigins;
    return this;
  }

  public API_corsConfigurationDTO addAccessControlAllowOriginsItem(String accessControlAllowOriginsItem) {
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

  public API_corsConfigurationDTO accessControlAllowCredentials(Boolean accessControlAllowCredentials) {
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

  public API_corsConfigurationDTO accessControlAllowHeaders(List<String> accessControlAllowHeaders) {
    this.accessControlAllowHeaders = accessControlAllowHeaders;
    return this;
  }

  public API_corsConfigurationDTO addAccessControlAllowHeadersItem(String accessControlAllowHeadersItem) {
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

  public API_corsConfigurationDTO accessControlAllowMethods(List<String> accessControlAllowMethods) {
    this.accessControlAllowMethods = accessControlAllowMethods;
    return this;
  }

  public API_corsConfigurationDTO addAccessControlAllowMethodsItem(String accessControlAllowMethodsItem) {
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
    API_corsConfigurationDTO apICorsConfiguration = (API_corsConfigurationDTO) o;
    return Objects.equals(this.corsConfigurationEnabled, apICorsConfiguration.corsConfigurationEnabled) &&
        Objects.equals(this.accessControlAllowOrigins, apICorsConfiguration.accessControlAllowOrigins) &&
        Objects.equals(this.accessControlAllowCredentials, apICorsConfiguration.accessControlAllowCredentials) &&
        Objects.equals(this.accessControlAllowHeaders, apICorsConfiguration.accessControlAllowHeaders) &&
        Objects.equals(this.accessControlAllowMethods, apICorsConfiguration.accessControlAllowMethods);
  }

  @Override
  public int hashCode() {
    return Objects.hash(corsConfigurationEnabled, accessControlAllowOrigins, accessControlAllowCredentials, accessControlAllowHeaders, accessControlAllowMethods);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class API_corsConfigurationDTO {\n");
    
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

