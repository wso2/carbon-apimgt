package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class RevokeAPIKeyDTO   {
  
    private String apiKey = null;
    private Long expiryTime = null;
    private Long tenantId = null;

  /**
   * API Key token.
   **/
  public RevokeAPIKeyDTO apiKey(String apiKey) {
    this.apiKey = apiKey;
    return this;
  }

  
  @ApiModelProperty(value = "API Key token.")
  @JsonProperty("apiKey")
  public String getApiKey() {
    return apiKey;
  }
  public void setApiKey(String apiKey) {
    this.apiKey = apiKey;
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
    return Objects.equals(apiKey, revokeAPIKey.apiKey) &&
        Objects.equals(expiryTime, revokeAPIKey.expiryTime) &&
        Objects.equals(tenantId, revokeAPIKey.tenantId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiKey, expiryTime, tenantId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RevokeAPIKeyDTO {\n");
    
    sb.append("    apiKey: ").append(toIndentedString(apiKey)).append("\n");
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

