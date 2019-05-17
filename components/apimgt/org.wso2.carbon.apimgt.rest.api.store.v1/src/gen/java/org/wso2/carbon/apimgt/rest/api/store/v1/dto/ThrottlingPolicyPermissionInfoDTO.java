package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ThrottlingPolicyPermissionInfoDTO  {
  
  
  public enum TypeEnum {
     allow,  deny, 
  };
  
  private TypeEnum type = null;
  
  
  private List<String> roles = new ArrayList<String>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("type")
  public TypeEnum getType() {
    return type;
  }
  public void setType(TypeEnum type) {
    this.type = type;
  }

  
  /**
   * roles for this permission
   **/
  @ApiModelProperty(value = "roles for this permission")
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
    sb.append("class ThrottlingPolicyPermissionInfoDTO {\n");
    
    sb.append("  type: ").append(type).append("\n");
    sb.append("  roles: ").append(roles).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
