package org.wso2.apk.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;

import java.util.Objects;

import javax.xml.bind.annotation.*;

import javax.validation.Valid;



public class ApplicationKeyDTO   {
  
    private String keyMappingId = null;
    private String keyManager = null;
    private String consumerKey = null;
    private String consumerSecret = null;
    private List<String> supportedGrantTypes = new ArrayList<String>();
    private String callbackUrl = null;
    private String keyState = null;

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

    @XmlType(name="ModeEnum")
    @XmlEnum(String.class)
    public enum ModeEnum {
        MAPPED("MAPPED"),
        CREATED("CREATED");
        private String value;

        ModeEnum (String v) {
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
        public static ModeEnum fromValue(String v) {
            for (ModeEnum b : ModeEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private ModeEnum mode = null;
    private String groupId = null;
    private ApplicationTokenDTO token = null;
    private Object additionalProperties = null;

  /**
   * Key Manager Mapping UUID
   **/
  public ApplicationKeyDTO keyMappingId(String keyMappingId) {
    this.keyMappingId = keyMappingId;
    return this;
  }

  
  @ApiModelProperty(example = "92ab520c-8847-427a-a921-3ed19b15aad7", value = "Key Manager Mapping UUID")
  @JsonProperty("keyMappingId")
  public String getKeyMappingId() {
    return keyMappingId;
  }
  public void setKeyMappingId(String keyMappingId) {
    this.keyMappingId = keyMappingId;
  }

  /**
   * Key Manager Name
   **/
  public ApplicationKeyDTO keyManager(String keyManager) {
    this.keyManager = keyManager;
    return this;
  }

  
  @ApiModelProperty(example = "Resident Key Manager", value = "Key Manager Name")
  @JsonProperty("keyManager")
  public String getKeyManager() {
    return keyManager;
  }
  public void setKeyManager(String keyManager) {
    this.keyManager = keyManager;
  }

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
   * Describe the which mode Application Mapped.
   **/
  public ApplicationKeyDTO mode(ModeEnum mode) {
    this.mode = mode;
    return this;
  }

  
  @ApiModelProperty(example = "CREATED", value = "Describe the which mode Application Mapped.")
  @JsonProperty("mode")
  public ModeEnum getMode() {
    return mode;
  }
  public void setMode(ModeEnum mode) {
    this.mode = mode;
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
   **/
  public ApplicationKeyDTO token(ApplicationTokenDTO token) {
    this.token = token;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("token")
  public ApplicationTokenDTO getToken() {
    return token;
  }
  public void setToken(ApplicationTokenDTO token) {
    this.token = token;
  }

  /**
   * additionalProperties (if any).
   **/
  public ApplicationKeyDTO additionalProperties(Object additionalProperties) {
    this.additionalProperties = additionalProperties;
    return this;
  }

  
  @ApiModelProperty(value = "additionalProperties (if any).")
      @Valid
  @JsonProperty("additionalProperties")
  public Object getAdditionalProperties() {
    return additionalProperties;
  }
  public void setAdditionalProperties(Object additionalProperties) {
    this.additionalProperties = additionalProperties;
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
    return Objects.equals(keyMappingId, applicationKey.keyMappingId) &&
        Objects.equals(keyManager, applicationKey.keyManager) &&
        Objects.equals(consumerKey, applicationKey.consumerKey) &&
        Objects.equals(consumerSecret, applicationKey.consumerSecret) &&
        Objects.equals(supportedGrantTypes, applicationKey.supportedGrantTypes) &&
        Objects.equals(callbackUrl, applicationKey.callbackUrl) &&
        Objects.equals(keyState, applicationKey.keyState) &&
        Objects.equals(keyType, applicationKey.keyType) &&
        Objects.equals(mode, applicationKey.mode) &&
        Objects.equals(groupId, applicationKey.groupId) &&
        Objects.equals(token, applicationKey.token) &&
        Objects.equals(additionalProperties, applicationKey.additionalProperties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(keyMappingId, keyManager, consumerKey, consumerSecret, supportedGrantTypes, callbackUrl, keyState, keyType, mode, groupId, token, additionalProperties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationKeyDTO {\n");
    
    sb.append("    keyMappingId: ").append(toIndentedString(keyMappingId)).append("\n");
    sb.append("    keyManager: ").append(toIndentedString(keyManager)).append("\n");
    sb.append("    consumerKey: ").append(toIndentedString(consumerKey)).append("\n");
    sb.append("    consumerSecret: ").append(toIndentedString(consumerSecret)).append("\n");
    sb.append("    supportedGrantTypes: ").append(toIndentedString(supportedGrantTypes)).append("\n");
    sb.append("    callbackUrl: ").append(toIndentedString(callbackUrl)).append("\n");
    sb.append("    keyState: ").append(toIndentedString(keyState)).append("\n");
    sb.append("    keyType: ").append(toIndentedString(keyType)).append("\n");
    sb.append("    mode: ").append(toIndentedString(mode)).append("\n");
    sb.append("    groupId: ").append(toIndentedString(groupId)).append("\n");
    sb.append("    token: ").append(toIndentedString(token)).append("\n");
    sb.append("    additionalProperties: ").append(toIndentedString(additionalProperties)).append("\n");
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

