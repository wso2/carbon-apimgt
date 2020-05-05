package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class RevokedJWTDTO   {
  
    private String jwtSignature = null;
    private Long expiryTime = null;

  /**
   * signature of the JWT token.
   **/
  public RevokedJWTDTO jwtSignature(String jwtSignature) {
    this.jwtSignature = jwtSignature;
    return this;
  }

  
  @ApiModelProperty(value = "signature of the JWT token.")
  @JsonProperty("jwt_signature")
  public String getJwtSignature() {
    return jwtSignature;
  }
  public void setJwtSignature(String jwtSignature) {
    this.jwtSignature = jwtSignature;
  }

  /**
   * expiry timestamp.
   **/
  public RevokedJWTDTO expiryTime(Long expiryTime) {
    this.expiryTime = expiryTime;
    return this;
  }

  
  @ApiModelProperty(value = "expiry timestamp.")
  @JsonProperty("expiry_time")
  public Long getExpiryTime() {
    return expiryTime;
  }
  public void setExpiryTime(Long expiryTime) {
    this.expiryTime = expiryTime;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RevokedJWTDTO revokedJWT = (RevokedJWTDTO) o;
    return Objects.equals(jwtSignature, revokedJWT.jwtSignature) &&
        Objects.equals(expiryTime, revokedJWT.expiryTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(jwtSignature, expiryTime);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RevokedJWTDTO {\n");
    
    sb.append("    jwtSignature: ").append(toIndentedString(jwtSignature)).append("\n");
    sb.append("    expiryTime: ").append(toIndentedString(expiryTime)).append("\n");
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

