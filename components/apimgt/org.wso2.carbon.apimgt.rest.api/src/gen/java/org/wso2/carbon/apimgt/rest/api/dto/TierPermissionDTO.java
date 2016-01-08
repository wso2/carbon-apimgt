package org.wso2.carbon.apimgt.rest.api.dto;

import java.util.*;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class TierPermissionDTO  {
  
  
  
  private String enableAccess = null;
  
  
  private List<String> roles = new ArrayList<String>() ;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("enableAccess")
  public String getEnableAccess() {
    return enableAccess;
  }
  public void setEnableAccess(String enableAccess) {
    this.enableAccess = enableAccess;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("roles")
  public List<String> getRoles() {
    return roles;
  }
  public void setRoles(List<String> roles) {
    this.roles = roles;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class TierPermissionDTO {\n");
    
    sb.append("  enableAccess: ").append(enableAccess).append("\n");
    sb.append("  roles: ").append(roles).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
