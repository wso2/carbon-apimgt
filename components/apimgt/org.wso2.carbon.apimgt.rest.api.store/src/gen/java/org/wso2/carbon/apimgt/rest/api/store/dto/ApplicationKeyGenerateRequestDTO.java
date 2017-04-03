package org.wso2.carbon.apimgt.rest.api.store.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * ApplicationKeyGenerateRequestDTO
 */
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-03-28T22:39:18.827+05:30")
public class ApplicationKeyGenerateRequestDTO   {
  /**
   * Gets or Sets keyType
   */
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

  @JsonProperty("keyType")
  private KeyTypeEnum keyType = null;

  @JsonProperty("validityTime")
  private String validityTime = null;

  @JsonProperty("callbackUrl")
  private String callbackUrl = null;

  @JsonProperty("accessAllowDomains")
  private List<String> accessAllowDomains = new ArrayList<String>();

  @JsonProperty("scopes")
  private List<String> scopes = new ArrayList<String>();

  public ApplicationKeyGenerateRequestDTO keyType(KeyTypeEnum keyType) {
    this.keyType = keyType;
    return this;
  }

   /**
   * Get keyType
   * @return keyType
  **/
  @ApiModelProperty(required = true, value = "")
  public KeyTypeEnum getKeyType() {
    return keyType;
  }

  public void setKeyType(KeyTypeEnum keyType) {
    this.keyType = keyType;
  }

  public ApplicationKeyGenerateRequestDTO validityTime(String validityTime) {
    this.validityTime = validityTime;
    return this;
  }

   /**
   * Get validityTime
   * @return validityTime
  **/
  @ApiModelProperty(required = true, value = "")
  public String getValidityTime() {
    return validityTime;
  }

  public void setValidityTime(String validityTime) {
    this.validityTime = validityTime;
  }

  public ApplicationKeyGenerateRequestDTO callbackUrl(String callbackUrl) {
    this.callbackUrl = callbackUrl;
    return this;
  }

   /**
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

  public ApplicationKeyGenerateRequestDTO accessAllowDomains(List<String> accessAllowDomains) {
    this.accessAllowDomains = accessAllowDomains;
    return this;
  }

  public ApplicationKeyGenerateRequestDTO addAccessAllowDomainsItem(String accessAllowDomainsItem) {
    this.accessAllowDomains.add(accessAllowDomainsItem);
    return this;
  }

   /**
   * Allowed domains for the access token
   * @return accessAllowDomains
  **/
  @ApiModelProperty(required = true, value = "Allowed domains for the access token")
  public List<String> getAccessAllowDomains() {
    return accessAllowDomains;
  }

  public void setAccessAllowDomains(List<String> accessAllowDomains) {
    this.accessAllowDomains = accessAllowDomains;
  }

  public ApplicationKeyGenerateRequestDTO scopes(List<String> scopes) {
    this.scopes = scopes;
    return this;
  }

  public ApplicationKeyGenerateRequestDTO addScopesItem(String scopesItem) {
    this.scopes.add(scopesItem);
    return this;
  }

   /**
   * Allowed scopes for the access token
   * @return scopes
  **/
  @ApiModelProperty(value = "Allowed scopes for the access token")
  public List<String> getScopes() {
    return scopes;
  }

  public void setScopes(List<String> scopes) {
    this.scopes = scopes;
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
    return Objects.equals(this.keyType, applicationKeyGenerateRequest.keyType) &&
        Objects.equals(this.validityTime, applicationKeyGenerateRequest.validityTime) &&
        Objects.equals(this.callbackUrl, applicationKeyGenerateRequest.callbackUrl) &&
        Objects.equals(this.accessAllowDomains, applicationKeyGenerateRequest.accessAllowDomains) &&
        Objects.equals(this.scopes, applicationKeyGenerateRequest.scopes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(keyType, validityTime, callbackUrl, accessAllowDomains, scopes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationKeyGenerateRequestDTO {\n");
    
    sb.append("    keyType: ").append(toIndentedString(keyType)).append("\n");
    sb.append("    validityTime: ").append(toIndentedString(validityTime)).append("\n");
    sb.append("    callbackUrl: ").append(toIndentedString(callbackUrl)).append("\n");
    sb.append("    accessAllowDomains: ").append(toIndentedString(accessAllowDomains)).append("\n");
    sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
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

