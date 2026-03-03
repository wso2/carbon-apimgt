/*
 * Copyright (c) 2026, WSO2 LLC. (https://www.wso2.com/).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.gateway.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.Valid;
import java.util.Arrays;
import java.util.Objects;

public class APIKeyDTO {

  private String apiKeyHash = null;
  private String keyName = null;
  private String keyType = null;
  private String status = null;
  private Integer expiresAt = null;
  private Integer appId = null;
  private byte[] additionalProperties = null;

  /**
   **/
  public APIKeyDTO apiKeyHash(String apiKeyHash) {
    this.apiKeyHash = apiKeyHash;
    return this;
  }


  @ApiModelProperty(value = "")
  @JsonProperty("apiKeyHash")
  public String getApiKeyHash() {
    return apiKeyHash;
  }
  public void setApiKeyHash(String apiKeyHash) {
    this.apiKeyHash = apiKeyHash;
  }

  /**
   **/
  public APIKeyDTO keyName(String keyName) {
    this.keyName = keyName;
    return this;
  }


  @ApiModelProperty(value = "")
  @JsonProperty("keyName")
  public String getKeyName() {
    return keyName;
  }
  public void setKeyName(String keyName) {
    this.keyName = keyName;
  }

  /**
   **/
  public APIKeyDTO keyType(String keyType) {
    this.keyType = keyType;
    return this;
  }


  @ApiModelProperty(value = "")
  @JsonProperty("keyType")
  public String getKeyType() {
    return keyType;
  }
  public void setKeyType(String keyType) {
    this.keyType = keyType;
  }

  /**
   **/
  public APIKeyDTO status(String status) {
    this.status = status;
    return this;
  }


  @ApiModelProperty(value = "")
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   **/
  public APIKeyDTO expiresAt(Integer expiresAt) {
    this.expiresAt = expiresAt;
    return this;
  }


  @ApiModelProperty(value = "")
  @JsonProperty("expiresAt")
  public Integer getExpiresAt() {
    return expiresAt;
  }
  public void setExpiresAt(Integer expiresAt) {
    this.expiresAt = expiresAt;
  }

  /**
   **/
  public APIKeyDTO appId(Integer appId) {
    this.appId = appId;
    return this;
  }


  @ApiModelProperty(value = "")
  @JsonProperty("appId")
  public Integer getAppId() {
    return appId;
  }
  public void setAppId(Integer appId) {
    this.appId = appId;
  }

  /**
   **/
  public APIKeyDTO additionalProperties(byte[] additionalProperties) {
    this.additionalProperties = additionalProperties;
    return this;
  }


  @ApiModelProperty(value = "")
  @Valid
  @JsonProperty("additionalProperties")
  public byte[] getAdditionalProperties() {
    return additionalProperties;
  }
  public void setAdditionalProperties(byte[] additionalProperties) {
    this.additionalProperties = additionalProperties;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIKeyDTO apIKey = (APIKeyDTO) o;
    return Objects.equals(apiKeyHash, apIKey.apiKeyHash) &&
            Objects.equals(keyName, apIKey.keyName) &&
            Objects.equals(keyType, apIKey.keyType) &&
            Objects.equals(status, apIKey.status) &&
            Objects.equals(expiresAt, apIKey.expiresAt) &&
            Objects.equals(appId, apIKey.appId) &&
            Arrays.equals(additionalProperties, apIKey.additionalProperties);
  }

  @Override
  public int hashCode() {
    int result = Objects.hash(apiKeyHash, keyName, keyType, status, expiresAt, appId);
    result = 31 * result + Arrays.hashCode(additionalProperties);
    return result;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIKeyDTO {\n");

    sb.append("    apiKeyHash: ").append(toIndentedString(apiKeyHash)).append("\n");
    sb.append("    keyName: ").append(toIndentedString(keyName)).append("\n");
    sb.append("    keyType: ").append(toIndentedString(keyType)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    expiresAt: ").append(toIndentedString(expiresAt)).append("\n");
    sb.append("    appId: ").append(toIndentedString(appId)).append("\n");
    sb.append("    additionalProperties: ").append(toIndentedString(additionalProperties)).append("\n");
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
