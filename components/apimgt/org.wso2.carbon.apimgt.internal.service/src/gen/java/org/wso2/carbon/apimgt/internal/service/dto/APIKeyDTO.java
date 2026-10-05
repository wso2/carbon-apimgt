package org.wso2.carbon.apimgt.internal.service.dto;

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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)

public class APIKeyDTO   {
  
    private String apiKeyHash = null;
    private String keyName = null;
    private String keyType = null;
    private String status = null;
    private Long expiresAt = null;
    private String authUser = null;
    private Integer appId = null;
    private Integer apiId = null;
    private String applicationUUID = null;
    private String apiUUID = null;
    private Long validityPeriod = null;
    private Long createdTime = null;
    private Object additionalProperties = null;

    @XmlType(name="KeyBoundaryEnum")
    @XmlEnum(String.class)
    public enum KeyBoundaryEnum {
        API("API"),
        APPLICATION("APPLICATION");
        private String value;

        KeyBoundaryEnum (String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static KeyBoundaryEnum fromValue(String v) {
            for (KeyBoundaryEnum b : KeyBoundaryEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private KeyBoundaryEnum keyBoundary = null;

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
  public APIKeyDTO expiresAt(Long expiresAt) {
    this.expiresAt = expiresAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("expiresAt")
  public Long getExpiresAt() {
    return expiresAt;
  }
  public void setExpiresAt(Long expiresAt) {
    this.expiresAt = expiresAt;
  }

  /**
   **/
  public APIKeyDTO authUser(String authUser) {
    this.authUser = authUser;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("authUser")
  public String getAuthUser() {
    return authUser;
  }
  public void setAuthUser(String authUser) {
    this.authUser = authUser;
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
  public APIKeyDTO apiId(Integer apiId) {
    this.apiId = apiId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("apiId")
  public Integer getApiId() {
    return apiId;
  }
  public void setApiId(Integer apiId) {
    this.apiId = apiId;
  }

  /**
   **/
  public APIKeyDTO applicationUUID(String applicationUUID) {
    this.applicationUUID = applicationUUID;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("applicationUUID")
  public String getApplicationUUID() {
    return applicationUUID;
  }
  public void setApplicationUUID(String applicationUUID) {
    this.applicationUUID = applicationUUID;
  }

  /**
   **/
  public APIKeyDTO apiUUID(String apiUUID) {
    this.apiUUID = apiUUID;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("apiUUID")
  public String getApiUUID() {
    return apiUUID;
  }
  public void setApiUUID(String apiUUID) {
    this.apiUUID = apiUUID;
  }

  /**
   **/
  public APIKeyDTO validityPeriod(Long validityPeriod) {
    this.validityPeriod = validityPeriod;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("validityPeriod")
  public Long getValidityPeriod() {
    return validityPeriod;
  }
  public void setValidityPeriod(Long validityPeriod) {
    this.validityPeriod = validityPeriod;
  }

  /**
   **/
  public APIKeyDTO createdTime(Long createdTime) {
    this.createdTime = createdTime;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("createdTime")
  public Long getCreatedTime() {
    return createdTime;
  }
  public void setCreatedTime(Long createdTime) {
    this.createdTime = createdTime;
  }

  /**
   **/
  public APIKeyDTO additionalProperties(Object additionalProperties) {
    this.additionalProperties = additionalProperties;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("additionalProperties")
  public Object getAdditionalProperties() {
    return additionalProperties;
  }
  public void setAdditionalProperties(Object additionalProperties) {
    this.additionalProperties = additionalProperties;
  }

  /**
   **/
  public APIKeyDTO keyBoundary(KeyBoundaryEnum keyBoundary) {
    this.keyBoundary = keyBoundary;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("keyBoundary")
  public KeyBoundaryEnum getKeyBoundary() {
    return keyBoundary;
  }
  public void setKeyBoundary(KeyBoundaryEnum keyBoundary) {
    this.keyBoundary = keyBoundary;
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
        Objects.equals(authUser, apIKey.authUser) &&
        Objects.equals(appId, apIKey.appId) &&
        Objects.equals(apiId, apIKey.apiId) &&
        Objects.equals(applicationUUID, apIKey.applicationUUID) &&
        Objects.equals(apiUUID, apIKey.apiUUID) &&
        Objects.equals(validityPeriod, apIKey.validityPeriod) &&
        Objects.equals(createdTime, apIKey.createdTime) &&
        Objects.equals(additionalProperties, apIKey.additionalProperties) &&
        Objects.equals(keyBoundary, apIKey.keyBoundary);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiKeyHash, keyName, keyType, status, expiresAt, authUser, appId, apiId, applicationUUID, apiUUID, validityPeriod, createdTime, additionalProperties, keyBoundary);
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
    sb.append("    authUser: ").append(toIndentedString(authUser)).append("\n");
    sb.append("    appId: ").append(toIndentedString(appId)).append("\n");
    sb.append("    apiId: ").append(toIndentedString(apiId)).append("\n");
    sb.append("    applicationUUID: ").append(toIndentedString(applicationUUID)).append("\n");
    sb.append("    apiUUID: ").append(toIndentedString(apiUUID)).append("\n");
    sb.append("    validityPeriod: ").append(toIndentedString(validityPeriod)).append("\n");
    sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
    sb.append("    additionalProperties: ").append(toIndentedString(additionalProperties)).append("\n");
    sb.append("    keyBoundary: ").append(toIndentedString(keyBoundary)).append("\n");
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

