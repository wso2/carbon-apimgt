package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

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
  
    private String keyDisplayName = null;
    private String apiName = null;
    private String applicationName = null;

    @XmlType(name="KeyTypeEnum")
    @XmlEnum(String.class)
    public enum KeyTypeEnum {
        PRODUCTION("PRODUCTION"),
        SANDBOX("SANDBOX");
        private String value;

        KeyTypeEnum (String v) {
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
        public static KeyTypeEnum fromValue(String v) {
            for (KeyTypeEnum b : KeyTypeEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private KeyTypeEnum keyType = null;
    private String user = null;
    private String issuedOn = null;
    private Integer validityPeriod = null;
    private String lastUsed = null;

  /**
   * API Key name
   **/
  public APIKeyDTO keyDisplayName(String keyDisplayName) {
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
   * API Name
   **/
  public APIKeyDTO apiName(String apiName) {
    this.apiName = apiName;
    return this;
  }

  
  @ApiModelProperty(example = "NotificationsAPI", value = "API Name")
  @JsonProperty("apiName")
  public String getApiName() {
    return apiName;
  }
  public void setApiName(String apiName) {
    this.apiName = apiName;
  }

  /**
   * Application Name
   **/
  public APIKeyDTO applicationName(String applicationName) {
    this.applicationName = applicationName;
    return this;
  }

  
  @ApiModelProperty(example = "DefaultApplication", value = "Application Name")
  @JsonProperty("applicationName")
  public String getApplicationName() {
    return applicationName;
  }
  public void setApplicationName(String applicationName) {
    this.applicationName = applicationName;
  }

  /**
   * Application Key Type
   **/
  public APIKeyDTO keyType(KeyTypeEnum keyType) {
    this.keyType = keyType;
    return this;
  }

  
  @ApiModelProperty(example = "PRODUCTION", value = "Application Key Type")
  @JsonProperty("keyType")
  public KeyTypeEnum getKeyType() {
    return keyType;
  }
  public void setKeyType(KeyTypeEnum keyType) {
    this.keyType = keyType;
  }

  /**
   * Owner of the Application
   **/
  public APIKeyDTO user(String user) {
    this.user = user;
    return this;
  }

  
  @ApiModelProperty(example = "Bob", value = "Owner of the Application")
  @JsonProperty("user")
  public String getUser() {
    return user;
  }
  public void setUser(String user) {
    this.user = user;
  }

  /**
   * Created Time
   **/
  public APIKeyDTO issuedOn(String issuedOn) {
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
  public APIKeyDTO validityPeriod(Integer validityPeriod) {
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
   * Last Used Time
   **/
  public APIKeyDTO lastUsed(String lastUsed) {
    this.lastUsed = lastUsed;
    return this;
  }

  
  @ApiModelProperty(example = "NOT_USED", value = "Last Used Time")
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
    APIKeyDTO apIKey = (APIKeyDTO) o;
    return Objects.equals(keyDisplayName, apIKey.keyDisplayName) &&
        Objects.equals(apiName, apIKey.apiName) &&
        Objects.equals(applicationName, apIKey.applicationName) &&
        Objects.equals(keyType, apIKey.keyType) &&
        Objects.equals(user, apIKey.user) &&
        Objects.equals(issuedOn, apIKey.issuedOn) &&
        Objects.equals(validityPeriod, apIKey.validityPeriod) &&
        Objects.equals(lastUsed, apIKey.lastUsed);
  }

  @Override
  public int hashCode() {
    return Objects.hash(keyDisplayName, apiName, applicationName, keyType, user, issuedOn, validityPeriod, lastUsed);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIKeyDTO {\n");
    
    sb.append("    keyDisplayName: ").append(toIndentedString(keyDisplayName)).append("\n");
    sb.append("    apiName: ").append(toIndentedString(apiName)).append("\n");
    sb.append("    applicationName: ").append(toIndentedString(applicationName)).append("\n");
    sb.append("    keyType: ").append(toIndentedString(keyType)).append("\n");
    sb.append("    user: ").append(toIndentedString(user)).append("\n");
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

