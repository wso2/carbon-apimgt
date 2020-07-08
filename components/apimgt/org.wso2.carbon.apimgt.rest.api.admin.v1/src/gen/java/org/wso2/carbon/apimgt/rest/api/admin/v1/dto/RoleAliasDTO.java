package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class RoleAliasDTO   {
  
    private String role = null;
    private Object aliases = null;

  /**
   * The original role
   **/
  public RoleAliasDTO role(String role) {
    this.role = role;
    return this;
  }

  
  @ApiModelProperty(example = "Internal/subscriber", value = "The original role")
  @JsonProperty("role")
  public String getRole() {
    return role;
  }
  public void setRole(String role) {
    this.role = role;
  }

  /**
   * The role mapping for role alias
   **/
  public RoleAliasDTO aliases(Object aliases) {
    this.aliases = aliases;
    return this;
  }

  
  @ApiModelProperty(example = "", value = "The role mapping for role alias")
  @JsonProperty("aliases")
  public Object getAliases() {
    return aliases;
  }
  public void setAliases(Object aliases) {
    this.aliases = aliases;
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
    return Objects.equals(role, roleAlias.role) &&
        Objects.equals(aliases, roleAlias.aliases);
  }

  @Override
  public int hashCode() {
    return Objects.hash(role, aliases);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RoleAliasDTO {\n");
    
    sb.append("    role: ").append(toIndentedString(role)).append("\n");
    sb.append("    aliases: ").append(toIndentedString(aliases)).append("\n");
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

