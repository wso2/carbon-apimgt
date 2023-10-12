package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class RevokedJWTConsumerKeyDTO   {
  
    private String consumerKey = null;
    private Boolean isRevokeAppOnly = null;
    private Long revocationTime = null;
    private String type = null;
    private String tenantId = null;

  /**
   * consumer key of the JWT.
   **/
  public RevokedJWTConsumerKeyDTO consumerKey(String consumerKey) {
    this.consumerKey = consumerKey;
    return this;
  }

  
  @ApiModelProperty(value = "consumer key of the JWT.")
  @JsonProperty("consumer_key")
  public String getConsumerKey() {
    return consumerKey;
  }
  public void setConsumerKey(String consumerKey) {
    this.consumerKey = consumerKey;
  }

  /**
   * whether the consumer key is revoked only for the application type.
   **/
  public RevokedJWTConsumerKeyDTO isRevokeAppOnly(Boolean isRevokeAppOnly) {
    this.isRevokeAppOnly = isRevokeAppOnly;
    return this;
  }

  
  @ApiModelProperty(value = "whether the consumer key is revoked only for the application type.")
  @JsonProperty("is_revoke_app_only")
  public Boolean isIsRevokeAppOnly() {
    return isRevokeAppOnly;
  }
  public void setIsRevokeAppOnly(Boolean isRevokeAppOnly) {
    this.isRevokeAppOnly = isRevokeAppOnly;
  }

  /**
   * revocation timestamp.
   **/
  public RevokedJWTConsumerKeyDTO revocationTime(Long revocationTime) {
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

  /**
   * revoked consumer key type.
   **/
  public RevokedJWTConsumerKeyDTO type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(value = "revoked consumer key type.")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   * revoked consumer key&#39;s respective tenant Id.
   **/
  public RevokedJWTConsumerKeyDTO tenantId(String tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  
  @ApiModelProperty(value = "revoked consumer key's respective tenant Id.")
  @JsonProperty("tenant_id")
  public String getTenantId() {
    return tenantId;
  }
  public void setTenantId(String tenantId) {
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
    RevokedJWTConsumerKeyDTO revokedJWTConsumerKey = (RevokedJWTConsumerKeyDTO) o;
    return Objects.equals(consumerKey, revokedJWTConsumerKey.consumerKey) &&
        Objects.equals(isRevokeAppOnly, revokedJWTConsumerKey.isRevokeAppOnly) &&
        Objects.equals(revocationTime, revokedJWTConsumerKey.revocationTime) &&
        Objects.equals(type, revokedJWTConsumerKey.type) &&
        Objects.equals(tenantId, revokedJWTConsumerKey.tenantId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consumerKey, isRevokeAppOnly, revocationTime, type, tenantId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RevokedJWTConsumerKeyDTO {\n");
    
    sb.append("    consumerKey: ").append(toIndentedString(consumerKey)).append("\n");
    sb.append("    isRevokeAppOnly: ").append(toIndentedString(isRevokeAppOnly)).append("\n");
    sb.append("    revocationTime: ").append(toIndentedString(revocationTime)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
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

