package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * LifecycleState_permissionBeansDTO
 */
public class LifecycleState_permissionBeansDTO   {
  @SerializedName("roles")
  private List<String> roles = new ArrayList<String>();

  @SerializedName("forTarget")
  private String forTarget = null;

  public LifecycleState_permissionBeansDTO roles(List<String> roles) {
    this.roles = roles;
    return this;
  }

  public LifecycleState_permissionBeansDTO addRolesItem(String rolesItem) {
    this.roles.add(rolesItem);
    return this;
  }

   /**
   * Get roles
   * @return roles
  **/
  @ApiModelProperty(value = "")
  public List<String> getRoles() {
    return roles;
  }

  public void setRoles(List<String> roles) {
    this.roles = roles;
  }

  public LifecycleState_permissionBeansDTO forTarget(String forTarget) {
    this.forTarget = forTarget;
    return this;
  }

   /**
   * Get forTarget
   * @return forTarget
  **/
  @ApiModelProperty(value = "")
  public String getForTarget() {
    return forTarget;
  }

  public void setForTarget(String forTarget) {
    this.forTarget = forTarget;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LifecycleState_permissionBeansDTO lifecycleStatePermissionBeans = (LifecycleState_permissionBeansDTO) o;
    return Objects.equals(this.roles, lifecycleStatePermissionBeans.roles) &&
        Objects.equals(this.forTarget, lifecycleStatePermissionBeans.forTarget);
  }

  @Override
  public int hashCode() {
    return Objects.hash(roles, forTarget);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LifecycleState_permissionBeansDTO {\n");
    
    sb.append("    roles: ").append(toIndentedString(roles)).append("\n");
    sb.append("    forTarget: ").append(toIndentedString(forTarget)).append("\n");
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

