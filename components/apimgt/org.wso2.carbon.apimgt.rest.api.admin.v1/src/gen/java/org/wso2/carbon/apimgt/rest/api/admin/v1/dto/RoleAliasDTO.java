package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class RoleAliasDTO   {
  
    private String name = null;
    private String roleList = null;

  /**
   * The role alias name
   **/
  public RoleAliasDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "Internal-subscriber", value = "The role alias name")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * The role mapping for role alias
   **/
  public RoleAliasDTO roleList(String roleList) {
    this.roleList = roleList;
    return this;
  }

  
  @ApiModelProperty(value = "The role mapping for role alias")
  @JsonProperty("roleList")
  public String getRoleList() {
    return roleList;
  }
  public void setRoleList(String roleList) {
    this.roleList = roleList;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RoleAliasDTO roleAlias = (RoleAliasDTO) o;
    return Objects.equals(name, roleAlias.name) &&
        Objects.equals(roleList, roleAlias.roleList);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, roleList);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RoleAliasDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    roleList: ").append(toIndentedString(roleList)).append("\n");
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

