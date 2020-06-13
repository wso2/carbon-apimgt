package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class OldAndNewPasswordsDTO   {
  
    private String oldPassword = null;
    private String newPassword = null;

  /**
   **/
  public OldAndNewPasswordsDTO oldPassword(String oldPassword) {
    this.oldPassword = oldPassword;
    return this;
  }

  
  @ApiModelProperty(example = "password123", value = "")
  @JsonProperty("oldPassword")
  public String getOldPassword() {
    return oldPassword;
  }
  public void setOldPassword(String oldPassword) {
    this.oldPassword = oldPassword;
  }

  /**
   **/
  public OldAndNewPasswordsDTO newPassword(String newPassword) {
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
    OldAndNewPasswordsDTO oldAndNewPasswords = (OldAndNewPasswordsDTO) o;
    return Objects.equals(oldPassword, oldAndNewPasswords.oldPassword) &&
        Objects.equals(newPassword, oldAndNewPasswords.newPassword);
  }

  @Override
  public int hashCode() {
    return Objects.hash(oldPassword, newPassword);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OldAndNewPasswordsDTO {\n");
    
    sb.append("    oldPassword: ").append(toIndentedString(oldPassword)).append("\n");
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

