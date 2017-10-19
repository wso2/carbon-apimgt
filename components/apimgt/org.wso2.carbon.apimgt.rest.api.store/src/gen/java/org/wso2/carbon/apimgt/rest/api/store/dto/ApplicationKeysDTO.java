package org.wso2.carbon.apimgt.rest.api.store.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ApplicationKeysDTO
 */
public class ApplicationKeysDTO   {
  @JsonProperty("consumerKey")
  private String consumerKey = null;

  @JsonProperty("consumerSecret")
  private String consumerSecret = null;

  @JsonProperty("supportedGrantTypes")
  private List<String> supportedGrantTypes = new ArrayList<String>();

  @JsonProperty("callbackUrl")
  private String callbackUrl = null;

  /**
   * Key type
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

  public ApplicationKeysDTO consumerKey(String consumerKey) {
    this.consumerKey = consumerKey;
    return this;
  }

   /**
   * Consumer key of the application
   * @return consumerKey
  **/
  @ApiModelProperty(value = "Consumer key of the application")
  public String getConsumerKey() {
    return consumerKey;
  }

  public void setConsumerKey(String consumerKey) {
    this.consumerKey = consumerKey;
  }

  public ApplicationKeysDTO consumerSecret(String consumerSecret) {
    this.consumerSecret = consumerSecret;
    return this;
  }

   /**
   * Consumer secret of the application
   * @return consumerSecret
  **/
  @ApiModelProperty(value = "Consumer secret of the application")
  public String getConsumerSecret() {
    return consumerSecret;
  }

  public void setConsumerSecret(String consumerSecret) {
    this.consumerSecret = consumerSecret;
  }

  public ApplicationKeysDTO supportedGrantTypes(List<String> supportedGrantTypes) {
    this.supportedGrantTypes = supportedGrantTypes;
    return this;
  }

  public ApplicationKeysDTO addSupportedGrantTypesItem(String supportedGrantTypesItem) {
    this.supportedGrantTypes.add(supportedGrantTypesItem);
    return this;
  }

   /**
   * Supported grant types for the application
   * @return supportedGrantTypes
  **/
  @ApiModelProperty(value = "Supported grant types for the application")
  public List<String> getSupportedGrantTypes() {
    return supportedGrantTypes;
  }

  public void setSupportedGrantTypes(List<String> supportedGrantTypes) {
    this.supportedGrantTypes = supportedGrantTypes;
  }

  public ApplicationKeysDTO callbackUrl(String callbackUrl) {
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

  public ApplicationKeysDTO keyType(KeyTypeEnum keyType) {
    this.keyType = keyType;
    return this;
  }

   /**
   * Key type
   * @return keyType
  **/
  @ApiModelProperty(value = "Key type")
  public KeyTypeEnum getKeyType() {
    return keyType;
  }

  public void setKeyType(KeyTypeEnum keyType) {
    this.keyType = keyType;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationKeysDTO applicationKeys = (ApplicationKeysDTO) o;
    return Objects.equals(this.consumerKey, applicationKeys.consumerKey) &&
        Objects.equals(this.consumerSecret, applicationKeys.consumerSecret) &&
        Objects.equals(this.supportedGrantTypes, applicationKeys.supportedGrantTypes) &&
        Objects.equals(this.callbackUrl, applicationKeys.callbackUrl) &&
        Objects.equals(this.keyType, applicationKeys.keyType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consumerKey, consumerSecret, supportedGrantTypes, callbackUrl, keyType);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationKeysDTO {\n");
    
    sb.append("    consumerKey: ").append(toIndentedString(consumerKey)).append("\n");
    sb.append("    consumerSecret: ").append(toIndentedString(consumerSecret)).append("\n");
    sb.append("    supportedGrantTypes: ").append(toIndentedString(supportedGrantTypes)).append("\n");
    sb.append("    callbackUrl: ").append(toIndentedString(callbackUrl)).append("\n");
    sb.append("    keyType: ").append(toIndentedString(keyType)).append("\n");
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

