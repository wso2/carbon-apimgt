package org.wso2.carbon.apimgt.rest.api.admin.dto;

import io.swagger.annotations.ApiModel;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;



/**
 * The tenant information of the user
 **/


@ApiModel(description = "The tenant information of the user")
public class TenantInfoDTO  {
  
  
  
  private String username = null;
  
  
  private String tenantDomain = null;
  
  
  private Integer tenantId = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("username")
  public String getUsername() {
    return username;
  }
  public void setUsername(String username) {
    this.username = username;
  }

  
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
  @JsonProperty("tenantId")
  public Integer getTenantId() {
    return tenantId;
  }
  public void setTenantId(Integer tenantId) {
    this.tenantId = tenantId;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class TenantInfoDTO {\n");
    
    sb.append("  username: ").append(username).append("\n");
    sb.append("  tenantDomain: ").append(tenantDomain).append("\n");
    sb.append("  tenantId: ").append(tenantId).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
