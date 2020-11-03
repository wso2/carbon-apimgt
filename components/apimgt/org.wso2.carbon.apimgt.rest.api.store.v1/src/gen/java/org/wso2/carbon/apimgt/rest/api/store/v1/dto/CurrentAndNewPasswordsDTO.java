package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class CurrentAndNewPasswordsDTO   {
  
    private String currentPassword = null;
    private String newPassword = null;

  /**
   **/
  public CurrentAndNewPasswordsDTO currentPassword(String currentPassword) {
    this.currentPassword = currentPassword;
    return this;
  }

  
  @ApiModelProperty(example = "password123", value = "")
  @JsonProperty("currentPassword")
  public String getCurrentPassword() {
    return currentPassword;
  }
  public void setCurrentPassword(String currentPassword) {
    this.currentPassword = currentPassword;
  }

  /**
   **/
  public CurrentAndNewPasswordsDTO newPassword(String newPassword) {
    this.newPassword = newPassword;
    return this;
  }

  
  @ApiModelProperty(example = "newpassword1234", value = "")
  @JsonProperty("newPassword")
  public String getNewPassword() {
    return newPassword;
  }
  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    CurrentAndNewPasswordsDTO currentAndNewPasswords = (CurrentAndNewPasswordsDTO) o;
    return Objects.equals(currentPassword, currentAndNewPasswords.currentPassword) &&
        Objects.equals(newPassword, currentAndNewPasswords.newPassword);
  }

  @Override
  public int hashCode() {
    return Objects.hash(currentPassword, newPassword);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class CurrentAndNewPasswordsDTO {\n");
    
    sb.append("    currentPassword: ").append(toIndentedString(currentPassword)).append("\n");
    sb.append("    newPassword: ").append(toIndentedString(newPassword)).append("\n");
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

