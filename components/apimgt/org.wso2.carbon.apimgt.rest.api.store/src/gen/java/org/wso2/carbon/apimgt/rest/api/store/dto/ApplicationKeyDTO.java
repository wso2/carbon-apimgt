package org.wso2.carbon.apimgt.rest.api.store.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.dto.TokenDTO;

/**
 * ApplicationKeyDTO
 */
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-02-09T12:36:56.084+05:30")
public class ApplicationKeyDTO   {
  @JsonProperty("consumerKey")
  private String consumerKey = null;

  @JsonProperty("consumerSecret")
  private String consumerSecret = null;

  @JsonProperty("supportedGrantTypes")
  private List<String> supportedGrantTypes = new ArrayList<String>();

  @JsonProperty("keyState")
  private String keyState = null;

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

  @JsonProperty("token")
  private TokenDTO token = null;

  public ApplicationKeyDTO consumerKey(String consumerKey) {
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

  public ApplicationKeyDTO consumerSecret(String consumerSecret) {
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

  public ApplicationKeyDTO supportedGrantTypes(List<String> supportedGrantTypes) {
    this.supportedGrantTypes = supportedGrantTypes;
    return this;
  }

  public ApplicationKeyDTO addSupportedGrantTypesItem(String supportedGrantTypesItem) {
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

  public ApplicationKeyDTO keyState(String keyState) {
    this.keyState = keyState;
    return this;
  }

   /**
   * State of the key generation of the application
   * @return keyState
  **/
  @ApiModelProperty(value = "State of the key generation of the application")
  public String getKeyState() {
    return keyState;
  }

  public void setKeyState(String keyState) {
    this.keyState = keyState;
  }

  public ApplicationKeyDTO keyType(KeyTypeEnum keyType) {
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

  public ApplicationKeyDTO token(TokenDTO token) {
    this.token = token;
    return this;
  }

   /**
   * Get token
   * @return token
  **/
  @ApiModelProperty(value = "")
  public TokenDTO getToken() {
    return token;
  }

  public void setToken(TokenDTO token) {
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
    return Objects.equals(this.consumerKey, applicationKey.consumerKey) &&
        Objects.equals(this.consumerSecret, applicationKey.consumerSecret) &&
        Objects.equals(this.supportedGrantTypes, applicationKey.supportedGrantTypes) &&
        Objects.equals(this.keyState, applicationKey.keyState) &&
        Objects.equals(this.keyType, applicationKey.keyType) &&
        Objects.equals(this.token, applicationKey.token);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consumerKey, consumerSecret, supportedGrantTypes, keyState, keyType, token);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationKeyDTO {\n");
    
    sb.append("    consumerKey: ").append(toIndentedString(consumerKey)).append("\n");
    sb.append("    consumerSecret: ").append(toIndentedString(consumerSecret)).append("\n");
    sb.append("    supportedGrantTypes: ").append(toIndentedString(supportedGrantTypes)).append("\n");
    sb.append("    keyState: ").append(toIndentedString(keyState)).append("\n");
    sb.append("    keyType: ").append(toIndentedString(keyType)).append("\n");
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

