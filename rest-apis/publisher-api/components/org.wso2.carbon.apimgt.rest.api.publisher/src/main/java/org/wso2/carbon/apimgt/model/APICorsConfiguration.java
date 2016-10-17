package org.wso2.carbon.apimgt.model;

import java.util.*;



/**
 * CORS configuration for the API

 **/
@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-10-14T14:19:24.818+05:30")
public class APICorsConfiguration  {
  
  private List<String> accessControlAllowOrigins = new ArrayList<String>();
  private Boolean accessControlAllowCredentials = null;
  private Boolean corsConfigurationEnabled = null;
  private List<String> accessControlAllowHeaders = new ArrayList<String>();
  private List<String> accessControlAllowMethods = new ArrayList<String>();

  /**
   **/
  public List<String> getAccessControlAllowOrigins() {
    return accessControlAllowOrigins;
  }
  public void setAccessControlAllowOrigins(List<String> accessControlAllowOrigins) {
    this.accessControlAllowOrigins = accessControlAllowOrigins;
  }

  /**
   **/
  public Boolean getAccessControlAllowCredentials() {
    return accessControlAllowCredentials;
  }
  public void setAccessControlAllowCredentials(Boolean accessControlAllowCredentials) {
    this.accessControlAllowCredentials = accessControlAllowCredentials;
  }

  /**
   **/
  public Boolean getCorsConfigurationEnabled() {
    return corsConfigurationEnabled;
  }
  public void setCorsConfigurationEnabled(Boolean corsConfigurationEnabled) {
    this.corsConfigurationEnabled = corsConfigurationEnabled;
  }

  /**
   **/
  public List<String> getAccessControlAllowHeaders() {
    return accessControlAllowHeaders;
  }
  public void setAccessControlAllowHeaders(List<String> accessControlAllowHeaders) {
    this.accessControlAllowHeaders = accessControlAllowHeaders;
  }

  /**
   **/
  public List<String> getAccessControlAllowMethods() {
    return accessControlAllowMethods;
  }
  public void setAccessControlAllowMethods(List<String> accessControlAllowMethods) {
    this.accessControlAllowMethods = accessControlAllowMethods;
  }


  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class APICorsConfiguration {\n");
    
    sb.append("  accessControlAllowOrigins: ").append(accessControlAllowOrigins).append("\n");
    sb.append("  accessControlAllowCredentials: ").append(accessControlAllowCredentials).append("\n");
    sb.append("  corsConfigurationEnabled: ").append(corsConfigurationEnabled).append("\n");
    sb.append("  accessControlAllowHeaders: ").append(accessControlAllowHeaders).append("\n");
    sb.append("  accessControlAllowMethods: ").append(accessControlAllowMethods).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
