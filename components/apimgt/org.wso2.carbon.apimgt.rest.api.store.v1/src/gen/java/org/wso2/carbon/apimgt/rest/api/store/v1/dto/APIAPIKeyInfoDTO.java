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



public class APIAPIKeyInfoDTO   {
  
    private String keyUUID = null;
    private String keyName = null;
    private String associatedApp = null;
    private String issuedOn = null;
    private Integer validityPeriod = null;
    private String lastUsed = null;

  /**
   * The UUID of the API key
   **/
  public APIAPIKeyInfoDTO keyUUID(String keyUUID) {
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
  public APIAPIKeyInfoDTO keyName(String keyName) {
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
   * Associated application
   **/
  public APIAPIKeyInfoDTO associatedApp(String associatedApp) {
    this.associatedApp = associatedApp;
    return this;
  }

  
  @ApiModelProperty(example = "DefaultApplication", value = "Associated application")
  @JsonProperty("associatedApp")
  public String getAssociatedApp() {
    return associatedApp;
  }
  public void setAssociatedApp(String associatedApp) {
    this.associatedApp = associatedApp;
  }

  /**
   * Created Time
   **/
  public APIAPIKeyInfoDTO issuedOn(String issuedOn) {
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
  public APIAPIKeyInfoDTO validityPeriod(Integer validityPeriod) {
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
  public APIAPIKeyInfoDTO lastUsed(String lastUsed) {
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
    APIAPIKeyInfoDTO apIAPIKeyInfo = (APIAPIKeyInfoDTO) o;
    return Objects.equals(keyUUID, apIAPIKeyInfo.keyUUID) &&
        Objects.equals(keyName, apIAPIKeyInfo.keyName) &&
        Objects.equals(associatedApp, apIAPIKeyInfo.associatedApp) &&
        Objects.equals(issuedOn, apIAPIKeyInfo.issuedOn) &&
        Objects.equals(validityPeriod, apIAPIKeyInfo.validityPeriod) &&
        Objects.equals(lastUsed, apIAPIKeyInfo.lastUsed);
  }

  @Override
  public int hashCode() {
    return Objects.hash(keyUUID, keyName, associatedApp, issuedOn, validityPeriod, lastUsed);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIAPIKeyInfoDTO {\n");
    
    sb.append("    keyUUID: ").append(toIndentedString(keyUUID)).append("\n");
    sb.append("    keyName: ").append(toIndentedString(keyName)).append("\n");
    sb.append("    associatedApp: ").append(toIndentedString(associatedApp)).append("\n");
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

