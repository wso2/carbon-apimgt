package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;



public class ApplicationKeyGenerateRequestDTO   {
  

@XmlType(name="KeyTypeEnum")
@XmlEnum(String.class)
public enum KeyTypeEnum {

    @XmlEnumValue("PRODUCTION") PRODUCTION(String.valueOf("PRODUCTION")), @XmlEnumValue("SANDBOX") SANDBOX(String.valueOf("SANDBOX"));


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
    private List<String> grantTypesToBeSupported = new ArrayList<>();
    private String callbackUrl = null;
    private List<String> scopes = new ArrayList<>();
    private String validityTime = null;
    private String clientId = null;
    private String clientSecret = null;

  /**
   **/
  public ApplicationKeyGenerateRequestDTO keyType(KeyTypeEnum keyType) {
    this.keyType = keyType;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("keyType")
  @NotNull
  public KeyTypeEnum getKeyType() {
    return keyType;
  }
  public void setKeyType(KeyTypeEnum keyType) {
    this.keyType = keyType;
  }

  /**
   * Grant types that should be supported by the application
   **/
  public ApplicationKeyGenerateRequestDTO grantTypesToBeSupported(List<String> grantTypesToBeSupported) {
    this.grantTypesToBeSupported = grantTypesToBeSupported;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Grant types that should be supported by the application")
  @JsonProperty("grantTypesToBeSupported")
  @NotNull
  public List<String> getGrantTypesToBeSupported() {
    return grantTypesToBeSupported;
  }
  public void setGrantTypesToBeSupported(List<String> grantTypesToBeSupported) {
    this.grantTypesToBeSupported = grantTypesToBeSupported;
  }

  /**
   * Callback URL
   **/
  public ApplicationKeyGenerateRequestDTO callbackUrl(String callbackUrl) {
    this.callbackUrl = callbackUrl;
    return this;
  }

  
  @ApiModelProperty(value = "Callback URL")
  @JsonProperty("callbackUrl")
  public String getCallbackUrl() {
    return callbackUrl;
  }
  public void setCallbackUrl(String callbackUrl) {
    this.callbackUrl = callbackUrl;
  }

  /**
   * Allowed scopes for the access token
   **/
  public ApplicationKeyGenerateRequestDTO scopes(List<String> scopes) {
    this.scopes = scopes;
    return this;
  }

  
  @ApiModelProperty(example = "[\"am_application_scope\",\"default\"]", value = "Allowed scopes for the access token")
  @JsonProperty("scopes")
  public List<String> getScopes() {
    return scopes;
  }
  public void setScopes(List<String> scopes) {
    this.scopes = scopes;
  }

  /**
   **/
  public ApplicationKeyGenerateRequestDTO validityTime(String validityTime) {
    this.validityTime = validityTime;
    return this;
  }

  
  @ApiModelProperty(example = "3600", value = "")
  @JsonProperty("validityTime")
  public String getValidityTime() {
    return validityTime;
  }
  public void setValidityTime(String validityTime) {
    this.validityTime = validityTime;
  }

  /**
   * Client ID for generating access token.
   **/
  public ApplicationKeyGenerateRequestDTO clientId(String clientId) {
    this.clientId = clientId;
    return this;
  }

  
  @ApiModelProperty(example = "", value = "Client ID for generating access token.")
  @JsonProperty("clientId")
  public String getClientId() {
    return clientId;
  }
  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  /**
   * Client secret for generating access token. This is given together with the client Id.
   **/
  public ApplicationKeyGenerateRequestDTO clientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
    return this;
  }

  
  @ApiModelProperty(example = "", value = "Client secret for generating access token. This is given together with the client Id.")
  @JsonProperty("clientSecret")
  public String getClientSecret() {
    return clientSecret;
  }
  public void setClientSecret(String clientSecret) {
    this.clientSecret = clientSecret;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationKeyGenerateRequestDTO applicationKeyGenerateRequest = (ApplicationKeyGenerateRequestDTO) o;
    return Objects.equals(keyType, applicationKeyGenerateRequest.keyType) &&
        Objects.equals(grantTypesToBeSupported, applicationKeyGenerateRequest.grantTypesToBeSupported) &&
        Objects.equals(callbackUrl, applicationKeyGenerateRequest.callbackUrl) &&
        Objects.equals(scopes, applicationKeyGenerateRequest.scopes) &&
        Objects.equals(validityTime, applicationKeyGenerateRequest.validityTime) &&
        Objects.equals(clientId, applicationKeyGenerateRequest.clientId) &&
        Objects.equals(clientSecret, applicationKeyGenerateRequest.clientSecret);
  }

  @Override
  public int hashCode() {
    return Objects.hash(keyType, grantTypesToBeSupported, callbackUrl, scopes, validityTime, clientId, clientSecret);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationKeyGenerateRequestDTO {\n");
    
    sb.append("    keyType: ").append(toIndentedString(keyType)).append("\n");
    sb.append("    grantTypesToBeSupported: ").append(toIndentedString(grantTypesToBeSupported)).append("\n");
    sb.append("    callbackUrl: ").append(toIndentedString(callbackUrl)).append("\n");
    sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
    sb.append("    validityTime: ").append(toIndentedString(validityTime)).append("\n");
    sb.append("    clientId: ").append(toIndentedString(clientId)).append("\n");
    sb.append("    clientSecret: ").append(toIndentedString(clientSecret)).append("\n");
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

