package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

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



public class APIKeyAssociationInfoDTO   {
  
    private String keyUUID = null;
    private String keyName = null;
    private String apiName = null;
    private String apiUUID = null;
    private String issuedOn = null;
    private Integer validityPeriod = null;
    private String lastUsed = null;

  /**
   * The UUID of the API key
   **/
  public APIKeyAssociationInfoDTO keyUUID(String keyUUID) {
    this.keyUUID = keyUUID;
    return this;
  }

  
  @ApiModelProperty(value = "The UUID of the API key")
  @JsonProperty("keyUUID")
  public String getKeyUUID() {
    return keyUUID;
  }
  public void setKeyUUID(String keyUUID) {
    this.keyUUID = keyUUID;
  }

  /**
   * API Key name
   **/
  public APIKeyAssociationInfoDTO keyName(String keyName) {
    this.keyName = keyName;
    return this;
  }

  
  @ApiModelProperty(example = "Test_Key", value = "API Key name")
  @JsonProperty("keyName")
  public String getKeyName() {
    return keyName;
  }
  public void setKeyName(String keyName) {
    this.keyName = keyName;
  }

  /**
   * API name
   **/
  public APIKeyAssociationInfoDTO apiName(String apiName) {
    this.apiName = apiName;
    return this;
  }

  
  @ApiModelProperty(example = "NotificationAPI", value = "API name")
  @JsonProperty("apiName")
  public String getApiName() {
    return apiName;
  }
  public void setApiName(String apiName) {
    this.apiName = apiName;
  }

  /**
   * The UUID of the API
   **/
  public APIKeyAssociationInfoDTO apiUUID(String apiUUID) {
    this.apiUUID = apiUUID;
    return this;
  }

  
  @ApiModelProperty(value = "The UUID of the API")
  @JsonProperty("apiUUID")
  public String getApiUUID() {
    return apiUUID;
  }
  public void setApiUUID(String apiUUID) {
    this.apiUUID = apiUUID;
  }

  /**
   * Created Time
   **/
  public APIKeyAssociationInfoDTO issuedOn(String issuedOn) {
    this.issuedOn = issuedOn;
    return this;
  }

  
  @ApiModelProperty(example = "2026-02-06 23:45:07", value = "Created Time")
  @JsonProperty("issuedOn")
  public String getIssuedOn() {
    return issuedOn;
  }
  public void setIssuedOn(String issuedOn) {
    this.issuedOn = issuedOn;
  }

  /**
   **/
  public APIKeyAssociationInfoDTO validityPeriod(Integer validityPeriod) {
    this.validityPeriod = validityPeriod;
    return this;
  }

  
  @ApiModelProperty(example = "3600", value = "")
  @JsonProperty("validityPeriod")
  public Integer getValidityPeriod() {
    return validityPeriod;
  }
  public void setValidityPeriod(Integer validityPeriod) {
    this.validityPeriod = validityPeriod;
  }

  /**
   * Last used time as epoch milliseconds (string), or &#x60;NOT_USED&#x60; if never used.
   **/
  public APIKeyAssociationInfoDTO lastUsed(String lastUsed) {
    this.lastUsed = lastUsed;
    return this;
  }

  
  @ApiModelProperty(example = "2026-02-06 23:45:07", value = "Last used time as epoch milliseconds (string), or `NOT_USED` if never used.")
  @JsonProperty("lastUsed")
  public String getLastUsed() {
    return lastUsed;
  }
  public void setLastUsed(String lastUsed) {
    this.lastUsed = lastUsed;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIKeyAssociationInfoDTO apIKeyAssociationInfo = (APIKeyAssociationInfoDTO) o;
    return Objects.equals(keyUUID, apIKeyAssociationInfo.keyUUID) &&
        Objects.equals(keyName, apIKeyAssociationInfo.keyName) &&
        Objects.equals(apiName, apIKeyAssociationInfo.apiName) &&
        Objects.equals(apiUUID, apIKeyAssociationInfo.apiUUID) &&
        Objects.equals(issuedOn, apIKeyAssociationInfo.issuedOn) &&
        Objects.equals(validityPeriod, apIKeyAssociationInfo.validityPeriod) &&
        Objects.equals(lastUsed, apIKeyAssociationInfo.lastUsed);
  }

  @Override
  public int hashCode() {
    return Objects.hash(keyUUID, keyName, apiName, apiUUID, issuedOn, validityPeriod, lastUsed);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIKeyAssociationInfoDTO {\n");
    
    sb.append("    keyUUID: ").append(toIndentedString(keyUUID)).append("\n");
    sb.append("    keyName: ").append(toIndentedString(keyName)).append("\n");
    sb.append("    apiName: ").append(toIndentedString(apiName)).append("\n");
    sb.append("    apiUUID: ").append(toIndentedString(apiUUID)).append("\n");
    sb.append("    issuedOn: ").append(toIndentedString(issuedOn)).append("\n");
    sb.append("    validityPeriod: ").append(toIndentedString(validityPeriod)).append("\n");
    sb.append("    lastUsed: ").append(toIndentedString(lastUsed)).append("\n");
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

