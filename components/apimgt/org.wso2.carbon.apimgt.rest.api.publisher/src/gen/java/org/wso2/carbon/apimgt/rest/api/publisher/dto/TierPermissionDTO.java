package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.*;
import javax.ws.rs.*;
import com.fasterxml.jackson.annotation.*;
import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class TierPermissionDTO  {
  
  
  public enum PermissionTypeEnum {
     allow,  deny, 
  };
  @NotNull
  private PermissionTypeEnum permissionType = null;
  
  @NotNull
  private List<String> roles = new ArrayList<String>();

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("permissionType")
  public PermissionTypeEnum getPermissionType() {
    return permissionType;
  }
  public void setPermissionType(PermissionTypeEnum permissionType) {
    this.permissionType = permissionType;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
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
    
    sb.append("  permissionType: ").append(permissionType).append("\n");
    sb.append("  roles: ").append(roles).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
