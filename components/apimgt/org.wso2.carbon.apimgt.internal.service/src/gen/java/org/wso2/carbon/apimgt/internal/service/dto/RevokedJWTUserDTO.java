package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class RevokedJWTUserDTO   {
  
    private String userUuid = null;
    private Long revocationTime = null;

  /**
   * User UUID of revoked JWT(s).
   **/
  public RevokedJWTUserDTO userUuid(String userUuid) {
    this.userUuid = userUuid;
    return this;
  }

  
  @ApiModelProperty(value = "User UUID of revoked JWT(s).")
  @JsonProperty("user_uuid")
  public String getUserUuid() {
    return userUuid;
  }
  public void setUserUuid(String userUuid) {
    this.userUuid = userUuid;
  }

  /**
   * revocation timestamp.
   **/
  public RevokedJWTUserDTO revocationTime(Long revocationTime) {
    this.revocationTime = revocationTime;
    return this;
  }

  
  @ApiModelProperty(value = "revocation timestamp.")
  @JsonProperty("revocation_time")
  public Long getRevocationTime() {
    return revocationTime;
  }
  public void setRevocationTime(Long revocationTime) {
    this.revocationTime = revocationTime;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RevokedJWTUserDTO revokedJWTUser = (RevokedJWTUserDTO) o;
    return Objects.equals(userUuid, revokedJWTUser.userUuid) &&
        Objects.equals(revocationTime, revokedJWTUser.revocationTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(userUuid, revocationTime);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RevokedJWTUserDTO {\n");
    
    sb.append("    userUuid: ").append(toIndentedString(userUuid)).append("\n");
    sb.append("    revocationTime: ").append(toIndentedString(revocationTime)).append("\n");
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

