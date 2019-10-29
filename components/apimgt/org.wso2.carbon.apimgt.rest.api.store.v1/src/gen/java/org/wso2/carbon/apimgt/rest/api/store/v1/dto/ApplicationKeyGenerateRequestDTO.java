package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



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
    private String additionalProperties = null;
    private String userAccessTokenDefaultValidityPeriod = null;
    private String applicationAccessTokenExpiryTime = null;
    private String refreshTokenExpiryTime = null;
    private String idTokenExpiryTime = null;
    private String renewRefreshTokenEnabled = null;
    private Boolean byPassClientCredentials = null;
    private Boolean pkceMandatory = null;
    private Boolean pkceSupportPlain = null;
    private List<String> audiences = new ArrayList<>();
    private Boolean requestObjectSignatureValidationEnabled = null;
    private Boolean idTokenEncryptionEnabled = null;

@XmlType(name="IdTokenEncryptionAlgorithmEnum")
@XmlEnum(String.class)
public enum IdTokenEncryptionAlgorithmEnum {

    @XmlEnumValue("RSA-OAEP") RSA_OAEP(String.valueOf("RSA-OAEP")), @XmlEnumValue("RSA1_5") RSA1_5(String.valueOf("RSA1_5"));


    private String value;

    IdTokenEncryptionAlgorithmEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static IdTokenEncryptionAlgorithmEnum fromValue(String v) {
        for (IdTokenEncryptionAlgorithmEnum b : IdTokenEncryptionAlgorithmEnum.values()) {
            if (String.valueOf(b.value).equals(v)) {
                return b;
            }
        }
        return null;
    }
}

    private IdTokenEncryptionAlgorithmEnum idTokenEncryptionAlgorithm = null;

@XmlType(name="FederatedIdentityProviderEnum")
@XmlEnum(String.class)
public enum FederatedIdentityProviderEnum {

    @XmlEnumValue("Google-OpenID-Connect") GOOGLE_OPENID_CONNECT(String.valueOf("Google-OpenID-Connect")), @XmlEnumValue("shibboleth-IDP") SHIBBOLETH_IDP(String.valueOf("shibboleth-IDP")), @XmlEnumValue("facebook") FACEBOOK(String.valueOf("facebook"));


    private String value;

    FederatedIdentityProviderEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static FederatedIdentityProviderEnum fromValue(String v) {
        for (FederatedIdentityProviderEnum b : FederatedIdentityProviderEnum.values()) {
            if (String.valueOf(b.value).equals(v)) {
                return b;
            }
        }
        return null;
    }
}

    private FederatedIdentityProviderEnum federatedIdentityProvider = null;

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

  /**
   * Additional properties needed.
   **/
  public ApplicationKeyGenerateRequestDTO additionalProperties(String additionalProperties) {
    this.additionalProperties = additionalProperties;
    return this;
  }

  
  @ApiModelProperty(example = "", value = "Additional properties needed.")
  @JsonProperty("additionalProperties")
  public String getAdditionalProperties() {
    return additionalProperties;
  }
  public void setAdditionalProperties(String additionalProperties) {
    this.additionalProperties = additionalProperties;
  }

  /**
   * work
   **/
  public ApplicationKeyGenerateRequestDTO userAccessTokenDefaultValidityPeriod(String userAccessTokenDefaultValidityPeriod) {
    this.userAccessTokenDefaultValidityPeriod = userAccessTokenDefaultValidityPeriod;
    return this;
  }

  
  @ApiModelProperty(example = "", value = "work")
  @JsonProperty("userAccessTokenDefaultValidityPeriod")
  public String getUserAccessTokenDefaultValidityPeriod() {
    return userAccessTokenDefaultValidityPeriod;
  }
  public void setUserAccessTokenDefaultValidityPeriod(String userAccessTokenDefaultValidityPeriod) {
    this.userAccessTokenDefaultValidityPeriod = userAccessTokenDefaultValidityPeriod;
  }

  /**
   * Maximum validity time for the access token
   **/
  public ApplicationKeyGenerateRequestDTO applicationAccessTokenExpiryTime(String applicationAccessTokenExpiryTime) {
    this.applicationAccessTokenExpiryTime = applicationAccessTokenExpiryTime;
    return this;
  }

  
  @ApiModelProperty(example = "3600", value = "Maximum validity time for the access token")
  @JsonProperty("applicationAccessTokenExpiryTime")
  public String getApplicationAccessTokenExpiryTime() {
    return applicationAccessTokenExpiryTime;
  }
  public void setApplicationAccessTokenExpiryTime(String applicationAccessTokenExpiryTime) {
    this.applicationAccessTokenExpiryTime = applicationAccessTokenExpiryTime;
  }

  /**
   * Maximum validity time for the access token
   **/
  public ApplicationKeyGenerateRequestDTO refreshTokenExpiryTime(String refreshTokenExpiryTime) {
    this.refreshTokenExpiryTime = refreshTokenExpiryTime;
    return this;
  }

  
  @ApiModelProperty(example = "3600", value = "Maximum validity time for the access token")
  @JsonProperty("refreshTokenExpiryTime")
  public String getRefreshTokenExpiryTime() {
    return refreshTokenExpiryTime;
  }
  public void setRefreshTokenExpiryTime(String refreshTokenExpiryTime) {
    this.refreshTokenExpiryTime = refreshTokenExpiryTime;
  }

  /**
   * Maximum validity time for the access token
   **/
  public ApplicationKeyGenerateRequestDTO idTokenExpiryTime(String idTokenExpiryTime) {
    this.idTokenExpiryTime = idTokenExpiryTime;
    return this;
  }

  
  @ApiModelProperty(example = "3600", value = "Maximum validity time for the access token")
  @JsonProperty("idTokenExpiryTime")
  public String getIdTokenExpiryTime() {
    return idTokenExpiryTime;
  }
  public void setIdTokenExpiryTime(String idTokenExpiryTime) {
    this.idTokenExpiryTime = idTokenExpiryTime;
  }

  /**
   * nnnnn
   **/
  public ApplicationKeyGenerateRequestDTO renewRefreshTokenEnabled(String renewRefreshTokenEnabled) {
    this.renewRefreshTokenEnabled = renewRefreshTokenEnabled;
    return this;
  }

  
  @ApiModelProperty(example = "", value = "nnnnn")
  @JsonProperty("renewRefreshTokenEnabled")
  public String getRenewRefreshTokenEnabled() {
    return renewRefreshTokenEnabled;
  }
  public void setRenewRefreshTokenEnabled(String renewRefreshTokenEnabled) {
    this.renewRefreshTokenEnabled = renewRefreshTokenEnabled;
  }

  /**
   * This option will allow the client to authenticate without a client secret
   **/
  public ApplicationKeyGenerateRequestDTO byPassClientCredentials(Boolean byPassClientCredentials) {
    this.byPassClientCredentials = byPassClientCredentials;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "This option will allow the client to authenticate without a client secret")
  @JsonProperty("byPassClientCredentials")
  public Boolean isByPassClientCredentials() {
    return byPassClientCredentials;
  }
  public void setByPassClientCredentials(Boolean byPassClientCredentials) {
    this.byPassClientCredentials = byPassClientCredentials;
  }

  /**
   * Only allow applications that bear PKCE Code Challenge with them.
   **/
  public ApplicationKeyGenerateRequestDTO pkceMandatory(Boolean pkceMandatory) {
    this.pkceMandatory = pkceMandatory;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "Only allow applications that bear PKCE Code Challenge with them.")
  @JsonProperty("pkceMandatory")
  public Boolean isPkceMandatory() {
    return pkceMandatory;
  }
  public void setPkceMandatory(Boolean pkceMandatory) {
    this.pkceMandatory = pkceMandatory;
  }

  /**
   * Server supports &#39;S256&#39; PKCE tranformation algorithm by default.
   **/
  public ApplicationKeyGenerateRequestDTO pkceSupportPlain(Boolean pkceSupportPlain) {
    this.pkceSupportPlain = pkceSupportPlain;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "Server supports 'S256' PKCE tranformation algorithm by default.")
  @JsonProperty("pkceSupportPlain")
  public Boolean isPkceSupportPlain() {
    return pkceSupportPlain;
  }
  public void setPkceSupportPlain(Boolean pkceSupportPlain) {
    this.pkceSupportPlain = pkceSupportPlain;
  }

  /**
   * valid array of audiences
   **/
  public ApplicationKeyGenerateRequestDTO audiences(List<String> audiences) {
    this.audiences = audiences;
    return this;
  }

  
  @ApiModelProperty(example = "[\"http://org.wso2.apimgt/gateway\"]", value = "valid array of audiences")
  @JsonProperty("audiences")
  public List<String> getAudiences() {
    return audiences;
  }
  public void setAudiences(List<String> audiences) {
    this.audiences = audiences;
  }

  /**
   * enabling object signature validation.
   **/
  public ApplicationKeyGenerateRequestDTO requestObjectSignatureValidationEnabled(Boolean requestObjectSignatureValidationEnabled) {
    this.requestObjectSignatureValidationEnabled = requestObjectSignatureValidationEnabled;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "enabling object signature validation.")
  @JsonProperty("requestObjectSignatureValidationEnabled")
  public Boolean isRequestObjectSignatureValidationEnabled() {
    return requestObjectSignatureValidationEnabled;
  }
  public void setRequestObjectSignatureValidationEnabled(Boolean requestObjectSignatureValidationEnabled) {
    this.requestObjectSignatureValidationEnabled = requestObjectSignatureValidationEnabled;
  }

  /**
   * whether id encryption is enable.
   **/
  public ApplicationKeyGenerateRequestDTO idTokenEncryptionEnabled(Boolean idTokenEncryptionEnabled) {
    this.idTokenEncryptionEnabled = idTokenEncryptionEnabled;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "whether id encryption is enable.")
  @JsonProperty("idTokenEncryptionEnabled")
  public Boolean isIdTokenEncryptionEnabled() {
    return idTokenEncryptionEnabled;
  }
  public void setIdTokenEncryptionEnabled(Boolean idTokenEncryptionEnabled) {
    this.idTokenEncryptionEnabled = idTokenEncryptionEnabled;
  }

  /**
   * id token encrryption
   **/
  public ApplicationKeyGenerateRequestDTO idTokenEncryptionAlgorithm(IdTokenEncryptionAlgorithmEnum idTokenEncryptionAlgorithm) {
    this.idTokenEncryptionAlgorithm = idTokenEncryptionAlgorithm;
    return this;
  }

  
  @ApiModelProperty(value = "id token encrryption")
  @JsonProperty("idTokenEncryptionAlgorithm")
  public IdTokenEncryptionAlgorithmEnum getIdTokenEncryptionAlgorithm() {
    return idTokenEncryptionAlgorithm;
  }
  public void setIdTokenEncryptionAlgorithm(IdTokenEncryptionAlgorithmEnum idTokenEncryptionAlgorithm) {
    this.idTokenEncryptionAlgorithm = idTokenEncryptionAlgorithm;
  }

  /**
   * configure identity providers for federated authentication.
   **/
  public ApplicationKeyGenerateRequestDTO federatedIdentityProvider(FederatedIdentityProviderEnum federatedIdentityProvider) {
    this.federatedIdentityProvider = federatedIdentityProvider;
    return this;
  }

  
  @ApiModelProperty(value = "configure identity providers for federated authentication.")
  @JsonProperty("federatedIdentityProvider")
  public FederatedIdentityProviderEnum getFederatedIdentityProvider() {
    return federatedIdentityProvider;
  }
  public void setFederatedIdentityProvider(FederatedIdentityProviderEnum federatedIdentityProvider) {
    this.federatedIdentityProvider = federatedIdentityProvider;
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
        Objects.equals(clientSecret, applicationKeyGenerateRequest.clientSecret) &&
        Objects.equals(additionalProperties, applicationKeyGenerateRequest.additionalProperties) &&
        Objects.equals(userAccessTokenDefaultValidityPeriod, applicationKeyGenerateRequest.userAccessTokenDefaultValidityPeriod) &&
        Objects.equals(applicationAccessTokenExpiryTime, applicationKeyGenerateRequest.applicationAccessTokenExpiryTime) &&
        Objects.equals(refreshTokenExpiryTime, applicationKeyGenerateRequest.refreshTokenExpiryTime) &&
        Objects.equals(idTokenExpiryTime, applicationKeyGenerateRequest.idTokenExpiryTime) &&
        Objects.equals(renewRefreshTokenEnabled, applicationKeyGenerateRequest.renewRefreshTokenEnabled) &&
        Objects.equals(byPassClientCredentials, applicationKeyGenerateRequest.byPassClientCredentials) &&
        Objects.equals(pkceMandatory, applicationKeyGenerateRequest.pkceMandatory) &&
        Objects.equals(pkceSupportPlain, applicationKeyGenerateRequest.pkceSupportPlain) &&
        Objects.equals(audiences, applicationKeyGenerateRequest.audiences) &&
        Objects.equals(requestObjectSignatureValidationEnabled, applicationKeyGenerateRequest.requestObjectSignatureValidationEnabled) &&
        Objects.equals(idTokenEncryptionEnabled, applicationKeyGenerateRequest.idTokenEncryptionEnabled) &&
        Objects.equals(idTokenEncryptionAlgorithm, applicationKeyGenerateRequest.idTokenEncryptionAlgorithm) &&
        Objects.equals(federatedIdentityProvider, applicationKeyGenerateRequest.federatedIdentityProvider);
  }

  @Override
  public int hashCode() {
    return Objects.hash(keyType, grantTypesToBeSupported, callbackUrl, scopes, validityTime, clientId, clientSecret, additionalProperties, userAccessTokenDefaultValidityPeriod, applicationAccessTokenExpiryTime, refreshTokenExpiryTime, idTokenExpiryTime, renewRefreshTokenEnabled, byPassClientCredentials, pkceMandatory, pkceSupportPlain, audiences, requestObjectSignatureValidationEnabled, idTokenEncryptionEnabled, idTokenEncryptionAlgorithm, federatedIdentityProvider);
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
    sb.append("    additionalProperties: ").append(toIndentedString(additionalProperties)).append("\n");
    sb.append("    userAccessTokenDefaultValidityPeriod: ").append(toIndentedString(userAccessTokenDefaultValidityPeriod)).append("\n");
    sb.append("    applicationAccessTokenExpiryTime: ").append(toIndentedString(applicationAccessTokenExpiryTime)).append("\n");
    sb.append("    refreshTokenExpiryTime: ").append(toIndentedString(refreshTokenExpiryTime)).append("\n");
    sb.append("    idTokenExpiryTime: ").append(toIndentedString(idTokenExpiryTime)).append("\n");
    sb.append("    renewRefreshTokenEnabled: ").append(toIndentedString(renewRefreshTokenEnabled)).append("\n");
    sb.append("    byPassClientCredentials: ").append(toIndentedString(byPassClientCredentials)).append("\n");
    sb.append("    pkceMandatory: ").append(toIndentedString(pkceMandatory)).append("\n");
    sb.append("    pkceSupportPlain: ").append(toIndentedString(pkceSupportPlain)).append("\n");
    sb.append("    audiences: ").append(toIndentedString(audiences)).append("\n");
    sb.append("    requestObjectSignatureValidationEnabled: ").append(toIndentedString(requestObjectSignatureValidationEnabled)).append("\n");
    sb.append("    idTokenEncryptionEnabled: ").append(toIndentedString(idTokenEncryptionEnabled)).append("\n");
    sb.append("    idTokenEncryptionAlgorithm: ").append(toIndentedString(idTokenEncryptionAlgorithm)).append("\n");
    sb.append("    federatedIdentityProvider: ").append(toIndentedString(federatedIdentityProvider)).append("\n");
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

