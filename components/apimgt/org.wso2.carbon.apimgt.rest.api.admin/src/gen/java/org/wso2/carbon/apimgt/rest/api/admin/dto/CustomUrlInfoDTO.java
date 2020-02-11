package org.wso2.carbon.apimgt.rest.api.admin.dto;

import io.swagger.annotations.ApiModel;
import org.wso2.carbon.apimgt.rest.api.admin.dto.CustomUrlInfoDevPortalDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;



/**
 * The custom url information of the tenant domain
 **/


@ApiModel(description = "The custom url information of the tenant domain")
public class CustomUrlInfoDTO  {
  
  
  
  private String tenantDomain = null;
  
  
  private String tenantAdminUsername = null;
  
  
  private Boolean enabled = null;
  
  
  private CustomUrlInfoDevPortalDTO devPortal = null;

  
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
  @JsonProperty("enabled")
  public Boolean getEnabled() {
    return enabled;
  }
  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("devPortal")
  public CustomUrlInfoDevPortalDTO getDevPortal() {
    return devPortal;
  }
  public void setDevPortal(CustomUrlInfoDevPortalDTO devPortal) {
    this.devPortal = devPortal;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class CustomUrlInfoDTO {\n");
    
    sb.append("  tenantDomain: ").append(tenantDomain).append("\n");
    sb.append("  tenantAdminUsername: ").append(tenantAdminUsername).append("\n");
    sb.append("  enabled: ").append(enabled).append("\n");
    sb.append("  devPortal: ").append(devPortal).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
