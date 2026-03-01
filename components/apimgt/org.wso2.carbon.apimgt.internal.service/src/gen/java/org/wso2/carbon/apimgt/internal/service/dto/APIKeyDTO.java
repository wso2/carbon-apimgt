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



public class APIKeyDTO   {
  
    private String apiKeyHash = null;
    private String keyName = null;
    private String keyType = null;
    private String status = null;
    private Integer validityPeriod = null;
    private Integer appId = null;
    private Object additionalProperties = null;

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
  public APIKeyDTO validityPeriod(Integer validityPeriod) {
    this.validityPeriod = validityPeriod;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("validityPeriod")
  public Integer getValidityPeriod() {
    return validityPeriod;
  }
  public void setValidityPeriod(Integer validityPeriod) {
    this.validityPeriod = validityPeriod;
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
        Objects.equals(validityPeriod, apIKey.validityPeriod) &&
        Objects.equals(appId, apIKey.appId) &&
        Objects.equals(additionalProperties, apIKey.additionalProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiKeyHash, keyName, keyType, status, validityPeriod, appId, additionalProperties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIKeyDTO {\n");
    
    sb.append("    apiKeyHash: ").append(toIndentedString(apiKeyHash)).append("\n");
    sb.append("    keyName: ").append(toIndentedString(keyName)).append("\n");
    sb.append("    keyType: ").append(toIndentedString(keyType)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    validityPeriod: ").append(toIndentedString(validityPeriod)).append("\n");
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

