package org.wso2.carbon.apimgt.rest.api.store.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

<<<<<<< HEAD
import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ApplicationKeyGenerateRequestDTO  {
  
  
=======
/**
 * ApplicationKeyGenerateRequestDTO
 */
public class ApplicationKeyGenerateRequestDTO   {
  /**
   * Gets or Sets keyType
   */
>>>>>>> upstream/master
  public enum KeyTypeEnum {
    PRODUCTION("PRODUCTION"),
    
    SANDBOX("SANDBOX");

    private String value;

    KeyTypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static KeyTypeEnum fromValue(String text) {
      for (KeyTypeEnum b : KeyTypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

<<<<<<< HEAD
  private String lastUpdatedTime = null;
=======
  @JsonProperty("keyType")
  private KeyTypeEnum keyType = null;

  @JsonProperty("grantTypesToBeSupported")
  private List<String> grantTypesToBeSupported = new ArrayList<String>();
>>>>>>> upstream/master

  @JsonProperty("callbackUrl")
  private String callbackUrl = null;

  public ApplicationKeyGenerateRequestDTO keyType(KeyTypeEnum keyType) {
    this.keyType = keyType;
    return this;
  }

   /**
   * Get keyType
   * @return keyType
  **/
<<<<<<< HEAD

  @JsonIgnore
  public String getCreatedTime(){
    return createdTime;
  }
  public void setCreatedTime(String createdTime){
    this.createdTime=createdTime;
  }

  
  /**
   **/
=======
>>>>>>> upstream/master
  @ApiModelProperty(required = true, value = "")
  public KeyTypeEnum getKeyType() {
    return keyType;
  }

  public void setKeyType(KeyTypeEnum keyType) {
    this.keyType = keyType;
  }

<<<<<<< HEAD
  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("validityTime")
  public String getValidityTime() {
    return validityTime;
=======
  public ApplicationKeyGenerateRequestDTO grantTypesToBeSupported(List<String> grantTypesToBeSupported) {
    this.grantTypesToBeSupported = grantTypesToBeSupported;
    return this;
>>>>>>> upstream/master
  }

  public ApplicationKeyGenerateRequestDTO addGrantTypesToBeSupportedItem(String grantTypesToBeSupportedItem) {
    this.grantTypesToBeSupported.add(grantTypesToBeSupportedItem);
    return this;
  }

   /**
   * Grant types that should be supported by the application
   * @return grantTypesToBeSupported
  **/
  @ApiModelProperty(required = true, value = "Grant types that should be supported by the application")
  public List<String> getGrantTypesToBeSupported() {
    return grantTypesToBeSupported;
  }

<<<<<<< HEAD
  
  /**
=======
  public void setGrantTypesToBeSupported(List<String> grantTypesToBeSupported) {
    this.grantTypesToBeSupported = grantTypesToBeSupported;
  }

  public ApplicationKeyGenerateRequestDTO callbackUrl(String callbackUrl) {
    this.callbackUrl = callbackUrl;
    return this;
  }

   /**
>>>>>>> upstream/master
   * Callback URL
   * @return callbackUrl
  **/
  @ApiModelProperty(value = "Callback URL")
  public String getCallbackUrl() {
    return callbackUrl;
  }

  public void setCallbackUrl(String callbackUrl) {
    this.callbackUrl = callbackUrl;
  }

<<<<<<< HEAD
  
  /**
   * Allowed domains for the access token
   **/
  @ApiModelProperty(required = true, value = "Allowed domains for the access token")
  @JsonProperty("accessAllowDomains")
  public List<String> getAccessAllowDomains() {
    return accessAllowDomains;
  }
  public void setAccessAllowDomains(List<String> accessAllowDomains) {
    this.accessAllowDomains = accessAllowDomains;
  }

  
  /**
   * Allowed scopes for the access token
   **/
  @ApiModelProperty(value = "Allowed scopes for the access token")
  @JsonProperty("scopes")
  public List<String> getScopes() {
    return scopes;
  }
  public void setScopes(List<String> scopes) {
    this.scopes = scopes;
=======

  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationKeyGenerateRequestDTO applicationKeyGenerateRequest = (ApplicationKeyGenerateRequestDTO) o;
    return Objects.equals(this.keyType, applicationKeyGenerateRequest.keyType) &&
        Objects.equals(this.grantTypesToBeSupported, applicationKeyGenerateRequest.grantTypesToBeSupported) &&
        Objects.equals(this.callbackUrl, applicationKeyGenerateRequest.callbackUrl);
>>>>>>> upstream/master
  }

  @Override
  public int hashCode() {
    return Objects.hash(keyType, grantTypesToBeSupported, callbackUrl);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationKeyGenerateRequestDTO {\n");
    
    sb.append("    keyType: ").append(toIndentedString(keyType)).append("\n");
    sb.append("    grantTypesToBeSupported: ").append(toIndentedString(grantTypesToBeSupported)).append("\n");
    sb.append("    callbackUrl: ").append(toIndentedString(callbackUrl)).append("\n");
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

