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
  
    private String keyDisplayName = null;
    private String apiName = null;
    private String associatedOn = null;
    private Integer validityPeriod = null;
    private String lastUsed = null;

  /**
   * API Key name
   **/
  public APIKeyAssociationInfoDTO keyDisplayName(String keyDisplayName) {
    this.keyDisplayName = keyDisplayName;
    return this;
  }

  
  @ApiModelProperty(example = "Test_Key", value = "API Key name")
  @JsonProperty("keyDisplayName")
  public String getKeyDisplayName() {
    return keyDisplayName;
  }
  public void setKeyDisplayName(String keyDisplayName) {
    this.keyDisplayName = keyDisplayName;
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
   * Created Time
   **/
  public APIKeyAssociationInfoDTO associatedOn(String associatedOn) {
    this.associatedOn = associatedOn;
    return this;
  }

  
  @ApiModelProperty(example = "2026-02-06 23:45:07", value = "Created Time")
  @JsonProperty("associatedOn")
  public String getAssociatedOn() {
    return associatedOn;
  }
  public void setAssociatedOn(String associatedOn) {
    this.associatedOn = associatedOn;
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
   * Last used time as epoch milliseconds, or NOT_USED if never used.
   **/
  public APIKeyAssociationInfoDTO lastUsed(String lastUsed) {
    this.lastUsed = lastUsed;
    return this;
  }

  
  @ApiModelProperty(example = "NOT_USED", value = "Last used time as epoch milliseconds, or NOT_USED if never used.")
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
    return Objects.equals(keyDisplayName, apIKeyAssociationInfo.keyDisplayName) &&
        Objects.equals(apiName, apIKeyAssociationInfo.apiName) &&
        Objects.equals(associatedOn, apIKeyAssociationInfo.associatedOn) &&
        Objects.equals(validityPeriod, apIKeyAssociationInfo.validityPeriod) &&
        Objects.equals(lastUsed, apIKeyAssociationInfo.lastUsed);
  }

  @Override
  public int hashCode() {
    return Objects.hash(keyDisplayName, apiName, associatedOn, validityPeriod, lastUsed);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIKeyAssociationInfoDTO {\n");
    
    sb.append("    keyDisplayName: ").append(toIndentedString(keyDisplayName)).append("\n");
    sb.append("    apiName: ").append(toIndentedString(apiName)).append("\n");
    sb.append("    associatedOn: ").append(toIndentedString(associatedOn)).append("\n");
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

