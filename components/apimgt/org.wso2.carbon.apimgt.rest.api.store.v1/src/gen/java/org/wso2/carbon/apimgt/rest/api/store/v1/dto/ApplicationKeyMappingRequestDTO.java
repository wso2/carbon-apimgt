package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class ApplicationKeyMappingRequestDTO   {
  
    private String consumerKey = null;
    private String consumerSecret = null;
    private String keyManager = null;

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

  /**
   * Consumer key of the application
   **/
  public ApplicationKeyMappingRequestDTO consumerKey(String consumerKey) {
    this.consumerKey = consumerKey;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Consumer key of the application")
  @JsonProperty("consumerKey")
  @NotNull
  public String getConsumerKey() {
    return consumerKey;
  }
  public void setConsumerKey(String consumerKey) {
    this.consumerKey = consumerKey;
  }

  /**
   * Consumer secret of the application
   **/
  public ApplicationKeyMappingRequestDTO consumerSecret(String consumerSecret) {
    this.consumerSecret = consumerSecret;
    return this;
  }

  
  @ApiModelProperty(value = "Consumer secret of the application")
  @JsonProperty("consumerSecret")
  public String getConsumerSecret() {
    return consumerSecret;
  }
  public void setConsumerSecret(String consumerSecret) {
    this.consumerSecret = consumerSecret;
  }

  /**
   * Key Manager Name
   **/
  public ApplicationKeyMappingRequestDTO keyManager(String keyManager) {
    this.keyManager = keyManager;
    return this;
  }

  
  @ApiModelProperty(value = "Key Manager Name")
  @JsonProperty("keyManager")
  public String getKeyManager() {
    return keyManager;
  }
  public void setKeyManager(String keyManager) {
    this.keyManager = keyManager;
  }

  /**
   **/
  public ApplicationKeyMappingRequestDTO keyType(KeyTypeEnum keyType) {
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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationKeyMappingRequestDTO applicationKeyMappingRequest = (ApplicationKeyMappingRequestDTO) o;
    return Objects.equals(consumerKey, applicationKeyMappingRequest.consumerKey) &&
        Objects.equals(consumerSecret, applicationKeyMappingRequest.consumerSecret) &&
        Objects.equals(keyManager, applicationKeyMappingRequest.keyManager) &&
        Objects.equals(keyType, applicationKeyMappingRequest.keyType);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consumerKey, consumerSecret, keyManager, keyType);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationKeyMappingRequestDTO {\n");
    
    sb.append("    consumerKey: ").append(toIndentedString(consumerKey)).append("\n");
    sb.append("    consumerSecret: ").append(toIndentedString(consumerSecret)).append("\n");
    sb.append("    keyManager: ").append(toIndentedString(keyManager)).append("\n");
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

