package org.wso2.carbon.throttle.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;



public class RevokeAPIKeyDTO   {
  
  private String signature = null;
  private Long expiryTime = null;
  private Long tenantId = null;

  /**
   * Signature of the API Key token.
   **/
  public RevokeAPIKeyDTO signature(String signature) {
    this.signature = signature;
    return this;
  }

  
  @ApiModelProperty(value = "Signature of the API Key token.")
  @JsonProperty("signature")
  public String getSignature() {
    return signature;
  }
  public void setSignature(String signature) {
    this.signature = signature;
  }

  /**
   * expiry timestamp.
   **/
  public RevokeAPIKeyDTO expiryTime(Long expiryTime) {
    this.expiryTime = expiryTime;
    return this;
  }

  
  @ApiModelProperty(value = "expiry timestamp.")
  @JsonProperty("expiryTime")
  public Long getExpiryTime() {
    return expiryTime;
  }
  public void setExpiryTime(Long expiryTime) {
    this.expiryTime = expiryTime;
  }

  /**
   * expiry timestamp.
   **/
  public RevokeAPIKeyDTO tenantId(Long tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  
  @ApiModelProperty(value = "expiry timestamp.")
  @JsonProperty("tenantId")
  public Long getTenantId() {
    return tenantId;
  }
  public void setTenantId(Long tenantId) {
    this.tenantId = tenantId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RevokeAPIKeyDTO revokeAPIKey = (RevokeAPIKeyDTO) o;
    return Objects.equals(signature, revokeAPIKey.signature) &&
        Objects.equals(expiryTime, revokeAPIKey.expiryTime) &&
        Objects.equals(tenantId, revokeAPIKey.tenantId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(signature, expiryTime, tenantId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RevokeAPIKeyDTO {\n");
    
    sb.append("    signature: ").append(toIndentedString(signature)).append("\n");
    sb.append("    expiryTime: ").append(toIndentedString(expiryTime)).append("\n");
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
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

