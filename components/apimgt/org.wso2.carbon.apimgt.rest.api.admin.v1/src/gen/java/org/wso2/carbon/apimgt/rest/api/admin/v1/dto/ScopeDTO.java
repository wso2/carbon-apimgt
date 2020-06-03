package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class ScopeDTO   {
  
    private String scope = null;
    private String roles = null;

  /**
   * Scope name. 
   **/
  public ScopeDTO scope(String scope) {
    this.scope = scope;
    return this;
  }

  
  @ApiModelProperty(example = "apim:api_publish", value = "Scope name. ")
  @JsonProperty("scope")
  public String getScope() {
    return scope;
  }
  public void setScope(String scope) {
    this.scope = scope;
  }

  /**
   * Roles for the particular scope. 
   **/
  public ScopeDTO roles(String roles) {
    this.roles = roles;
    return this;
  }

  
  @ApiModelProperty(example = "admin,Internal/publisher", value = "Roles for the particular scope. ")
  @JsonProperty("roles")
  public String getRoles() {
    return roles;
  }
  public void setRoles(String roles) {
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
    ScopeDTO scope = (ScopeDTO) o;
    return Objects.equals(scope, scope.scope) &&
        Objects.equals(roles, scope.roles);
  }

  @Override
  public int hashCode() {
    return Objects.hash(scope, roles);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ScopeDTO {\n");
    
    sb.append("    scope: ").append(toIndentedString(scope)).append("\n");
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

