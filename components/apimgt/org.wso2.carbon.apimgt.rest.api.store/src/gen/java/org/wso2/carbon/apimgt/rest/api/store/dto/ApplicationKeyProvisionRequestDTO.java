package org.wso2.carbon.apimgt.rest.api.store.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * ApplicationKeyProvisionRequestDTO
 */
public class ApplicationKeyProvisionRequestDTO   {
  @JsonProperty("consumerKey")
  private String consumerKey = null;

  @JsonProperty("consumerSecret")
  private String consumerSecret = null;

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

  public ApplicationKeyProvisionRequestDTO consumerKey(String consumerKey) {
    this.consumerKey = consumerKey;
    return this;
  }

   /**
   * Consumer key of the application
   * @return consumerKey
  **/
  @ApiModelProperty(required = true, value = "Consumer key of the application")
  public String getConsumerKey() {
    return consumerKey;
  }

  public void setConsumerKey(String consumerKey) {
    this.consumerKey = consumerKey;
  }

  public ApplicationKeyProvisionRequestDTO consumerSecret(String consumerSecret) {
    this.consumerSecret = consumerSecret;
    return this;
  }

   /**
   * Consumer secret of the application
   * @return consumerSecret
  **/
  @ApiModelProperty(required = true, value = "Consumer secret of the application")
  public String getConsumerSecret() {
    return consumerSecret;
  }

  public void setConsumerSecret(String consumerSecret) {
    this.consumerSecret = consumerSecret;
  }

  public ApplicationKeyProvisionRequestDTO keyType(KeyTypeEnum keyType) {
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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationKeyProvisionRequestDTO applicationKeyProvisionRequest = (ApplicationKeyProvisionRequestDTO) o;
    return Objects.equals(this.consumerKey, applicationKeyProvisionRequest.consumerKey) &&
        Objects.equals(this.consumerSecret, applicationKeyProvisionRequest.consumerSecret) &&
        Objects.equals(this.keyType, applicationKeyProvisionRequest.keyType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consumerKey, consumerSecret, keyType);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationKeyProvisionRequestDTO {\n");
    
    sb.append("    consumerKey: ").append(toIndentedString(consumerKey)).append("\n");
    sb.append("    consumerSecret: ").append(toIndentedString(consumerSecret)).append("\n");
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

