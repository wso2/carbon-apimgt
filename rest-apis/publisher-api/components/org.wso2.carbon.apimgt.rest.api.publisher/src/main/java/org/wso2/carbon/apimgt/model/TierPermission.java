package org.wso2.carbon.apimgt.model;

import java.util.*;



@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-10-14T14:19:24.818+05:30")
public class TierPermission  {
  
  public enum PermissionTypeEnum {
     allow,  deny, 
  };
  private PermissionTypeEnum permissionType = null;
  private List<String> roles = new ArrayList<String>();

  /**
   **/
  public PermissionTypeEnum getPermissionType() {
    return permissionType;
  }
  public void setPermissionType(PermissionTypeEnum permissionType) {
    this.permissionType = permissionType;
  }

  /**
   **/
  public List<String> getRoles() {
    return roles;
  }
  public void setRoles(List<String> roles) {
    this.roles = roles;
  }


  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class TierPermission {\n");
    
    sb.append("  permissionType: ").append(permissionType).append("\n");
    sb.append("  roles: ").append(roles).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
