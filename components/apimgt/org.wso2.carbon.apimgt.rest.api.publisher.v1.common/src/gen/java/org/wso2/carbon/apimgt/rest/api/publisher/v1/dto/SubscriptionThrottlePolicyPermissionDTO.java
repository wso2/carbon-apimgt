package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class SubscriptionThrottlePolicyPermissionDTO   {
  

    @XmlType(name="PermissionTypeEnum")
    @XmlEnum(String.class)
    public enum PermissionTypeEnum {
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
    private PermissionTypeEnum permissionType = null;
    private List<String> roles = new ArrayList<String>();

  /**
   **/
  public SubscriptionThrottlePolicyPermissionDTO permissionType(PermissionTypeEnum permissionType) {
    this.permissionType = permissionType;
    return this;
  }

  
  @ApiModelProperty(example = "deny", required = true, value = "")
  @JsonProperty("permissionType")
  @NotNull
  public PermissionTypeEnum getPermissionType() {
    return permissionType;
  }
  public void setPermissionType(PermissionTypeEnum permissionType) {
    this.permissionType = permissionType;
  }

  /**
   **/
  public SubscriptionThrottlePolicyPermissionDTO roles(List<String> roles) {
    this.roles = roles;
    return this;
  }

  
  @ApiModelProperty(example = "[\"Internal/everyone\"]", required = true, value = "")
  @JsonProperty("roles")
  @NotNull
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
    SubscriptionThrottlePolicyPermissionDTO subscriptionThrottlePolicyPermission = (SubscriptionThrottlePolicyPermissionDTO) o;
    return Objects.equals(permissionType, subscriptionThrottlePolicyPermission.permissionType) &&
        Objects.equals(roles, subscriptionThrottlePolicyPermission.roles);
  }

  @Override
  public int hashCode() {
    return Objects.hash(permissionType, roles);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubscriptionThrottlePolicyPermissionDTO {\n");
    
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

