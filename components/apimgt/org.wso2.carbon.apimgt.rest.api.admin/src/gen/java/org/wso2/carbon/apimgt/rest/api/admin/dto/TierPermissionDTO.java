package org.wso2.carbon.apimgt.rest.api.admin.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * TierPermissionDTO
 */
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-03-22T12:24:12.664+05:30")
public class TierPermissionDTO   {
  /**
   * Gets or Sets permissionType
   */
  public enum PermissionTypeEnum {
    ALLOW("allow"),
    
    DENY("deny");

    private String value;

    PermissionTypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static PermissionTypeEnum fromValue(String text) {
      for (PermissionTypeEnum b : PermissionTypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("permissionType")
  private PermissionTypeEnum permissionType = null;

  @JsonProperty("roles")
  private List<String> roles = new ArrayList<String>();

  public TierPermissionDTO permissionType(PermissionTypeEnum permissionType) {
    this.permissionType = permissionType;
    return this;
  }

   /**
   * Get permissionType
   * @return permissionType
  **/
  @ApiModelProperty(example = "deny", required = true, value = "")
  public PermissionTypeEnum getPermissionType() {
    return permissionType;
  }

  public void setPermissionType(PermissionTypeEnum permissionType) {
    this.permissionType = permissionType;
  }

  public TierPermissionDTO roles(List<String> roles) {
    this.roles = roles;
    return this;
  }

  public TierPermissionDTO addRolesItem(String rolesItem) {
    this.roles.add(rolesItem);
    return this;
  }

   /**
   * Get roles
   * @return roles
  **/
  @ApiModelProperty(example = "[&quot;Internal/everyone&quot;]", required = true, value = "")
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
    TierPermissionDTO tierPermission = (TierPermissionDTO) o;
    return Objects.equals(this.permissionType, tierPermission.permissionType) &&
        Objects.equals(this.roles, tierPermission.roles);
  }

  @Override
  public int hashCode() {
    return Objects.hash(permissionType, roles);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TierPermissionDTO {\n");
    
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

