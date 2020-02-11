package org.wso2.carbon.apimgt.rest.api.admin.dto;

import io.swagger.annotations.ApiModel;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

/**
 * The custom url information of the tenant domain
 **/


@ApiModel(description = "The custom url information of the tenant domain")
public class CustomUrlInfoDTO  {
  
  
  
  private String tenantDomain = null;

  private String tenantAdminUsername = null;
  
  private Boolean isCustomUrlEnabled = null;
  
  
  private String customUrl = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("tenantDomain")
  public String getTenantDomain() {
    return tenantDomain;
  }
  public void setTenantDomain(String tenantDomain) {
    this.tenantDomain = tenantDomain;
  }

  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("tenantAdminUsername")
  public String getTenantAdminUsername() {
    return tenantAdminUsername;
  }
  public void setTenantAdminUsername(String tenantAdminUsername) {
    this.tenantAdminUsername = tenantAdminUsername;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("isCustomUrlEnabled")
  public Boolean getIsCustomUrlEnabled() {
    return isCustomUrlEnabled;
  }
  public void setIsCustomUrlEnabled(Boolean isCustomUrlEnabled) {
    this.isCustomUrlEnabled = isCustomUrlEnabled;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("customUrl")
  public String getCustomUrl() {
    return customUrl;
  }
  public void setCustomUrl(String customUrl) {
    this.customUrl = customUrl;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class CustomUrlInfoDTO {\n");
    
    sb.append("  tenantDomain: ").append(tenantDomain).append("\n");
    sb.append("  tenantAdminUsername: ").append(tenantAdminUsername).append("\n");
    sb.append("  isCustomUrlEnabled: ").append(isCustomUrlEnabled).append("\n");
    sb.append("  customUrl: ").append(customUrl).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
