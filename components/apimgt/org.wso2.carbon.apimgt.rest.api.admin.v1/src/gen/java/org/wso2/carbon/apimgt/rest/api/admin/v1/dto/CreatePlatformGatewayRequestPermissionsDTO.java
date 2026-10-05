package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;

/**
 * Gateway visibility permissions configuration
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;
@ApiModel(description = "Gateway visibility permissions configuration")


public class CreatePlatformGatewayRequestPermissionsDTO   {
  

    @XmlType(name="PermissionTypeEnum")
    @XmlEnum(String.class)
    public enum PermissionTypeEnum {
        PUBLIC("PUBLIC"),
        ALLOW("ALLOW"),
        DENY("DENY");
        private String value;

        PermissionTypeEnum (String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static PermissionTypeEnum fromValue(String v) {
            for (PermissionTypeEnum b : PermissionTypeEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private PermissionTypeEnum permissionType = PermissionTypeEnum.PUBLIC;
    private List<String> roles = new ArrayList<String>();

  /**
   * Permission type for gateway visibility: - PUBLIC: Gateway is visible to all users - ALLOW: Gateway is visible only to specified roles - DENY: Gateway is hidden from specified roles 
   **/
  public CreatePlatformGatewayRequestPermissionsDTO permissionType(PermissionTypeEnum permissionType) {
    this.permissionType = permissionType;
    return this;
  }

  
  @ApiModelProperty(value = "Permission type for gateway visibility: - PUBLIC: Gateway is visible to all users - ALLOW: Gateway is visible only to specified roles - DENY: Gateway is hidden from specified roles ")
  @JsonProperty("permissionType")
  public PermissionTypeEnum getPermissionType() {
    return permissionType;
  }
  public void setPermissionType(PermissionTypeEnum permissionType) {
    this.permissionType = permissionType;
  }

  /**
   * List of roles for ALLOW/DENY permission types
   **/
  public CreatePlatformGatewayRequestPermissionsDTO roles(List<String> roles) {
    this.roles = roles;
    return this;
  }

  
  @ApiModelProperty(example = "[\"admin\",\"publisher\"]", value = "List of roles for ALLOW/DENY permission types")
  @JsonProperty("roles")
  public List<String> getRoles() {
    return roles;
  }
  public void setRoles(List<String> roles) {
    this.roles = roles;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CreatePlatformGatewayRequestPermissionsDTO createPlatformGatewayRequestPermissions = (CreatePlatformGatewayRequestPermissionsDTO) o;
    return Objects.equals(permissionType, createPlatformGatewayRequestPermissions.permissionType) &&
        Objects.equals(roles, createPlatformGatewayRequestPermissions.roles);
  }

  @Override
  public int hashCode() {
    return Objects.hash(permissionType, roles);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CreatePlatformGatewayRequestPermissionsDTO {\n");
    
    sb.append("    permissionType: ").append(toIndentedString(permissionType)).append("\n");
    sb.append("    roles: ").append(toIndentedString(roles)).append("\n");
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

