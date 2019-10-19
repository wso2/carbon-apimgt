package org.wso2.carbon.throttle.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;



public class RevokeAPIKeyDTO   {
  
  private String apiKey = null;
  private Long expiryTime = null;
  private Integer tenantId = null;

  /**
   * Signature of the API Key token.
   **/
  public RevokeAPIKeyDTO apiKey(String apiKey) {
    this.apiKey = apiKey;
    return this;
  }

  
  @ApiModelProperty(value = "Signature of the API Key token.")
  @JsonProperty("signature")
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
  public RevokeAPIKeyDTO tenantId(Integer tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  
  @ApiModelProperty(value = "expiry timestamp.")
  @JsonProperty("tenantId")
  public Integer getTenantId() {
    return tenantId;
  }
  public void setTenantId(Integer tenantId) {
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
    
    sb.append("    signature: ").append(toIndentedString(apiKey)).append("\n");
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

