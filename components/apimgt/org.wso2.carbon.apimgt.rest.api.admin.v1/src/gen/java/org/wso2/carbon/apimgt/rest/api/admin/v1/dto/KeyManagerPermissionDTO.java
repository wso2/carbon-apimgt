package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class KeyManagerPermissionDTO   {
  
    private Integer keyManagerPermissionID = null;
    private String keyManagerUUID = null;
    private String permissionType = null;
    private String role = null;

  /**
   **/
  public KeyManagerPermissionDTO keyManagerPermissionID(Integer keyManagerPermissionID) {
    this.keyManagerPermissionID = keyManagerPermissionID;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("keyManagerPermissionID")
  public Integer getKeyManagerPermissionID() {
    return keyManagerPermissionID;
  }
  public void setKeyManagerPermissionID(Integer keyManagerPermissionID) {
    this.keyManagerPermissionID = keyManagerPermissionID;
  }

  /**
   **/
  public KeyManagerPermissionDTO keyManagerUUID(String keyManagerUUID) {
    this.keyManagerUUID = keyManagerUUID;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("keyManagerUUID")
  public String getKeyManagerUUID() {
    return keyManagerUUID;
  }
  public void setKeyManagerUUID(String keyManagerUUID) {
    this.keyManagerUUID = keyManagerUUID;
  }

  /**
   **/
  public KeyManagerPermissionDTO permissionType(String permissionType) {
    this.permissionType = permissionType;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("permissionType")
  public String getPermissionType() {
    return permissionType;
  }
  public void setPermissionType(String permissionType) {
    this.permissionType = permissionType;
  }

  /**
   **/
  public KeyManagerPermissionDTO role(String role) {
    this.role = role;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("role")
  public String getRole() {
    return role;
  }
  public void setRole(String role) {
    this.role = role;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KeyManagerPermissionDTO keyManagerPermission = (KeyManagerPermissionDTO) o;
    return Objects.equals(keyManagerPermissionID, keyManagerPermission.keyManagerPermissionID) &&
        Objects.equals(keyManagerUUID, keyManagerPermission.keyManagerUUID) &&
        Objects.equals(permissionType, keyManagerPermission.permissionType) &&
        Objects.equals(role, keyManagerPermission.role);
  }

  @Override
  public int hashCode() {
    return Objects.hash(keyManagerPermissionID, keyManagerUUID, permissionType, role);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class KeyManagerPermissionDTO {\n");
    
    sb.append("    keyManagerPermissionID: ").append(toIndentedString(keyManagerPermissionID)).append("\n");
    sb.append("    keyManagerUUID: ").append(toIndentedString(keyManagerUUID)).append("\n");
    sb.append("    permissionType: ").append(toIndentedString(permissionType)).append("\n");
    sb.append("    role: ").append(toIndentedString(role)).append("\n");
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

