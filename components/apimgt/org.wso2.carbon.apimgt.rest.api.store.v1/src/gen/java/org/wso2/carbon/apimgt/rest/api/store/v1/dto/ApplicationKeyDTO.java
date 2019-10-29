package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApplicationTokenDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class ApplicationKeyDTO   {
  
    private String consumerKey = null;
    private String consumerSecret = null;
    private List<String> supportedGrantTypes = new ArrayList<>();
    private String callbackUrl = null;
    private String keyState = null;

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
    private String groupId = null;
    private Long userAccessTokenDefaultValidityPeriod = null;
    private Long applicationAccessTokenExpiryTime = null;
    private Long refreshTokenExpiryTime = null;
    private Long idTokenExpiryTime = null;
    private String renewRefreshTokenEnabled = null;
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
    private Boolean byPassClientCredentials = null;
    private Boolean pkceMandatory = null;
    private Boolean pkceSupportPlain = null;
    private List<String> audiences = new ArrayList<>();
    private Boolean requestObjectSignatureValidationEnabled = null;

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
    private ApplicationTokenDTO token = null;

  /**
   * Consumer key of the application
   **/
  public ApplicationKeyDTO consumerKey(String consumerKey) {
    this.consumerKey = consumerKey;
    return this;
  }

  
  @ApiModelProperty(example = "vYDoc9s7IgAFdkSyNDaswBX7ejoa", value = "Consumer key of the application")
  @JsonProperty("consumerKey")
  public String getConsumerKey() {
    return consumerKey;
  }
  public void setConsumerKey(String consumerKey) {
    this.consumerKey = consumerKey;
  }

  /**
   * Consumer secret of the application
   **/
  public ApplicationKeyDTO consumerSecret(String consumerSecret) {
    this.consumerSecret = consumerSecret;
    return this;
  }

  
  @ApiModelProperty(example = "TIDlOFkpzB7WjufO3OJUhy1fsvAa", value = "Consumer secret of the application")
  @JsonProperty("consumerSecret")
  public String getConsumerSecret() {
    return consumerSecret;
  }
  public void setConsumerSecret(String consumerSecret) {
    this.consumerSecret = consumerSecret;
  }

  /**
   * The grant types that are supported by the application
   **/
  public ApplicationKeyDTO supportedGrantTypes(List<String> supportedGrantTypes) {
    this.supportedGrantTypes = supportedGrantTypes;
    return this;
  }

  
  @ApiModelProperty(example = "[\"client_credentials\",\"password\"]", value = "The grant types that are supported by the application")
  @JsonProperty("supportedGrantTypes")
  public List<String> getSupportedGrantTypes() {
    return supportedGrantTypes;
  }
  public void setSupportedGrantTypes(List<String> supportedGrantTypes) {
    this.supportedGrantTypes = supportedGrantTypes;
  }

  /**
   * Callback URL
   **/
  public ApplicationKeyDTO callbackUrl(String callbackUrl) {
    this.callbackUrl = callbackUrl;
    return this;
  }

  
  @ApiModelProperty(example = "http://sample.com/callback/url", value = "Callback URL")
  @JsonProperty("callbackUrl")
  public String getCallbackUrl() {
    return callbackUrl;
  }
  public void setCallbackUrl(String callbackUrl) {
    this.callbackUrl = callbackUrl;
  }

  /**
   * Describes the state of the key generation.
   **/
  public ApplicationKeyDTO keyState(String keyState) {
    this.keyState = keyState;
    return this;
  }

  
  @ApiModelProperty(example = "APPROVED", value = "Describes the state of the key generation.")
  @JsonProperty("keyState")
  public String getKeyState() {
    return keyState;
  }
  public void setKeyState(String keyState) {
    this.keyState = keyState;
  }

  /**
   * Describes to which endpoint the key belongs
   **/
  public ApplicationKeyDTO keyType(KeyTypeEnum keyType) {
    this.keyType = keyType;
    return this;
  }

  
  @ApiModelProperty(example = "PRODUCTION", value = "Describes to which endpoint the key belongs")
  @JsonProperty("keyType")
  public KeyTypeEnum getKeyType() {
    return keyType;
  }
  public void setKeyType(KeyTypeEnum keyType) {
    this.keyType = keyType;
  }

  /**
   * Application group id (if any).
   **/
  public ApplicationKeyDTO groupId(String groupId) {
    this.groupId = groupId;
    return this;
  }

  
  @ApiModelProperty(example = "2", value = "Application group id (if any).")
  @JsonProperty("groupId")
  public String getGroupId() {
    return groupId;
  }
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  /**
   * Maximum validity time for the access token
   **/
  public ApplicationKeyDTO userAccessTokenDefaultValidityPeriod(Long userAccessTokenDefaultValidityPeriod) {
    this.userAccessTokenDefaultValidityPeriod = userAccessTokenDefaultValidityPeriod;
    return this;
  }

  
  @ApiModelProperty(example = "3600", value = "Maximum validity time for the access token")
  @JsonProperty("userAccessTokenDefaultValidityPeriod")
  public Long getUserAccessTokenDefaultValidityPeriod() {
    return userAccessTokenDefaultValidityPeriod;
  }
  public void setUserAccessTokenDefaultValidityPeriod(Long userAccessTokenDefaultValidityPeriod) {
    this.userAccessTokenDefaultValidityPeriod = userAccessTokenDefaultValidityPeriod;
  }

  /**
   * Maximum validity time for the access token
   **/
  public ApplicationKeyDTO applicationAccessTokenExpiryTime(Long applicationAccessTokenExpiryTime) {
    this.applicationAccessTokenExpiryTime = applicationAccessTokenExpiryTime;
    return this;
  }

  
  @ApiModelProperty(example = "3600", value = "Maximum validity time for the access token")
  @JsonProperty("applicationAccessTokenExpiryTime")
  public Long getApplicationAccessTokenExpiryTime() {
    return applicationAccessTokenExpiryTime;
  }
  public void setApplicationAccessTokenExpiryTime(Long applicationAccessTokenExpiryTime) {
    this.applicationAccessTokenExpiryTime = applicationAccessTokenExpiryTime;
  }

  /**
   * Maximum validity time for the access token
   **/
  public ApplicationKeyDTO refreshTokenExpiryTime(Long refreshTokenExpiryTime) {
    this.refreshTokenExpiryTime = refreshTokenExpiryTime;
    return this;
  }

  
  @ApiModelProperty(example = "3600", value = "Maximum validity time for the access token")
  @JsonProperty("refreshTokenExpiryTime")
  public Long getRefreshTokenExpiryTime() {
    return refreshTokenExpiryTime;
  }
  public void setRefreshTokenExpiryTime(Long refreshTokenExpiryTime) {
    this.refreshTokenExpiryTime = refreshTokenExpiryTime;
  }

  /**
   * Maximum validity time for the access token
   **/
  public ApplicationKeyDTO idTokenExpiryTime(Long idTokenExpiryTime) {
    this.idTokenExpiryTime = idTokenExpiryTime;
    return this;
  }

  
  @ApiModelProperty(example = "3600", value = "Maximum validity time for the access token")
  @JsonProperty("idTokenExpiryTime")
  public Long getIdTokenExpiryTime() {
    return idTokenExpiryTime;
  }
  public void setIdTokenExpiryTime(Long idTokenExpiryTime) {
    this.idTokenExpiryTime = idTokenExpiryTime;
  }

  /**
   * nnnnn
   **/
  public ApplicationKeyDTO renewRefreshTokenEnabled(String renewRefreshTokenEnabled) {
    this.renewRefreshTokenEnabled = renewRefreshTokenEnabled;
    return this;
  }

  
  @ApiModelProperty(example = "false", value = "nnnnn")
  @JsonProperty("renewRefreshTokenEnabled")
  public String getRenewRefreshTokenEnabled() {
    return renewRefreshTokenEnabled;
  }
  public void setRenewRefreshTokenEnabled(String renewRefreshTokenEnabled) {
    this.renewRefreshTokenEnabled = renewRefreshTokenEnabled;
  }

  /**
   * whether id encryption is enable.
   **/
  public ApplicationKeyDTO idTokenEncryptionEnabled(Boolean idTokenEncryptionEnabled) {
    this.idTokenEncryptionEnabled = idTokenEncryptionEnabled;
    return this;
  }

  
  @ApiModelProperty(value = "whether id encryption is enable.")
  @JsonProperty("idTokenEncryptionEnabled")
  public Boolean isIdTokenEncryptionEnabled() {
    return idTokenEncryptionEnabled;
  }
  public void setIdTokenEncryptionEnabled(Boolean idTokenEncryptionEnabled) {
    this.idTokenEncryptionEnabled = idTokenEncryptionEnabled;
  }

  /**
   **/
  public ApplicationKeyDTO idTokenEncryptionAlgorithm(IdTokenEncryptionAlgorithmEnum idTokenEncryptionAlgorithm) {
    this.idTokenEncryptionAlgorithm = idTokenEncryptionAlgorithm;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("idTokenEncryptionAlgorithm")
  public IdTokenEncryptionAlgorithmEnum getIdTokenEncryptionAlgorithm() {
    return idTokenEncryptionAlgorithm;
  }
  public void setIdTokenEncryptionAlgorithm(IdTokenEncryptionAlgorithmEnum idTokenEncryptionAlgorithm) {
    this.idTokenEncryptionAlgorithm = idTokenEncryptionAlgorithm;
  }

  /**
   * This option will allow the client to authenticate without a client secret
   **/
  public ApplicationKeyDTO byPassClientCredentials(Boolean byPassClientCredentials) {
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
  public ApplicationKeyDTO pkceMandatory(Boolean pkceMandatory) {
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
  public ApplicationKeyDTO pkceSupportPlain(Boolean pkceSupportPlain) {
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
  public ApplicationKeyDTO audiences(List<String> audiences) {
    this.audiences = audiences;
    return this;
  }

  
  @ApiModelProperty(example = "\"http://org.wso2.apimgt/gateway\"", value = "valid array of audiences")
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
  public ApplicationKeyDTO requestObjectSignatureValidationEnabled(Boolean requestObjectSignatureValidationEnabled) {
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
   * configure identity providers for federated authentication.
   **/
  public ApplicationKeyDTO federatedIdentityProvider(FederatedIdentityProviderEnum federatedIdentityProvider) {
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

  /**
   **/
  public ApplicationKeyDTO token(ApplicationTokenDTO token) {
    this.token = token;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("token")
  public ApplicationTokenDTO getToken() {
    return token;
  }
  public void setToken(ApplicationTokenDTO token) {
    this.token = token;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationKeyDTO applicationKey = (ApplicationKeyDTO) o;
    return Objects.equals(consumerKey, applicationKey.consumerKey) &&
        Objects.equals(consumerSecret, applicationKey.consumerSecret) &&
        Objects.equals(supportedGrantTypes, applicationKey.supportedGrantTypes) &&
        Objects.equals(callbackUrl, applicationKey.callbackUrl) &&
        Objects.equals(keyState, applicationKey.keyState) &&
        Objects.equals(keyType, applicationKey.keyType) &&
        Objects.equals(groupId, applicationKey.groupId) &&
        Objects.equals(userAccessTokenDefaultValidityPeriod, applicationKey.userAccessTokenDefaultValidityPeriod) &&
        Objects.equals(applicationAccessTokenExpiryTime, applicationKey.applicationAccessTokenExpiryTime) &&
        Objects.equals(refreshTokenExpiryTime, applicationKey.refreshTokenExpiryTime) &&
        Objects.equals(idTokenExpiryTime, applicationKey.idTokenExpiryTime) &&
        Objects.equals(renewRefreshTokenEnabled, applicationKey.renewRefreshTokenEnabled) &&
        Objects.equals(idTokenEncryptionEnabled, applicationKey.idTokenEncryptionEnabled) &&
        Objects.equals(idTokenEncryptionAlgorithm, applicationKey.idTokenEncryptionAlgorithm) &&
        Objects.equals(byPassClientCredentials, applicationKey.byPassClientCredentials) &&
        Objects.equals(pkceMandatory, applicationKey.pkceMandatory) &&
        Objects.equals(pkceSupportPlain, applicationKey.pkceSupportPlain) &&
        Objects.equals(audiences, applicationKey.audiences) &&
        Objects.equals(requestObjectSignatureValidationEnabled, applicationKey.requestObjectSignatureValidationEnabled) &&
        Objects.equals(federatedIdentityProvider, applicationKey.federatedIdentityProvider) &&
        Objects.equals(token, applicationKey.token);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consumerKey, consumerSecret, supportedGrantTypes, callbackUrl, keyState, keyType, groupId, userAccessTokenDefaultValidityPeriod, applicationAccessTokenExpiryTime, refreshTokenExpiryTime, idTokenExpiryTime, renewRefreshTokenEnabled, idTokenEncryptionEnabled, idTokenEncryptionAlgorithm, byPassClientCredentials, pkceMandatory, pkceSupportPlain, audiences, requestObjectSignatureValidationEnabled, federatedIdentityProvider, token);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationKeyDTO {\n");
    
    sb.append("    consumerKey: ").append(toIndentedString(consumerKey)).append("\n");
    sb.append("    consumerSecret: ").append(toIndentedString(consumerSecret)).append("\n");
    sb.append("    supportedGrantTypes: ").append(toIndentedString(supportedGrantTypes)).append("\n");
    sb.append("    callbackUrl: ").append(toIndentedString(callbackUrl)).append("\n");
    sb.append("    keyState: ").append(toIndentedString(keyState)).append("\n");
    sb.append("    keyType: ").append(toIndentedString(keyType)).append("\n");
    sb.append("    groupId: ").append(toIndentedString(groupId)).append("\n");
    sb.append("    userAccessTokenDefaultValidityPeriod: ").append(toIndentedString(userAccessTokenDefaultValidityPeriod)).append("\n");
    sb.append("    applicationAccessTokenExpiryTime: ").append(toIndentedString(applicationAccessTokenExpiryTime)).append("\n");
    sb.append("    refreshTokenExpiryTime: ").append(toIndentedString(refreshTokenExpiryTime)).append("\n");
    sb.append("    idTokenExpiryTime: ").append(toIndentedString(idTokenExpiryTime)).append("\n");
    sb.append("    renewRefreshTokenEnabled: ").append(toIndentedString(renewRefreshTokenEnabled)).append("\n");
    sb.append("    idTokenEncryptionEnabled: ").append(toIndentedString(idTokenEncryptionEnabled)).append("\n");
    sb.append("    idTokenEncryptionAlgorithm: ").append(toIndentedString(idTokenEncryptionAlgorithm)).append("\n");
    sb.append("    byPassClientCredentials: ").append(toIndentedString(byPassClientCredentials)).append("\n");
    sb.append("    pkceMandatory: ").append(toIndentedString(pkceMandatory)).append("\n");
    sb.append("    pkceSupportPlain: ").append(toIndentedString(pkceSupportPlain)).append("\n");
    sb.append("    audiences: ").append(toIndentedString(audiences)).append("\n");
    sb.append("    requestObjectSignatureValidationEnabled: ").append(toIndentedString(requestObjectSignatureValidationEnabled)).append("\n");
    sb.append("    federatedIdentityProvider: ").append(toIndentedString(federatedIdentityProvider)).append("\n");
    sb.append("    token: ").append(toIndentedString(token)).append("\n");
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

