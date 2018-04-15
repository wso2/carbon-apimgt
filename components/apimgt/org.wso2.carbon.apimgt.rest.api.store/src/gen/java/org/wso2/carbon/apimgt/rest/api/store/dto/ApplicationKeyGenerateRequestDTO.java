package org.wso2.carbon.apimgt.rest.api.store.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * ApplicationKeyGenerateRequestDTO
 */
public class ApplicationKeyGenerateRequestDTO   {
  /**
   * Gets or Sets keyType
   */
  public enum KeyTypeEnum {
    @SerializedName("PRODUCTION")
    PRODUCTION("PRODUCTION"),
    
    @SerializedName("SANDBOX")
    SANDBOX("SANDBOX");

    private String value;

    KeyTypeEnum(String value) {
      this.value = value;
    }

    @Override
    public String toString() {
      return String.valueOf(value);
    }
    public static KeyTypeEnum fromValue(String text) {
      for (KeyTypeEnum b : KeyTypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @SerializedName("keyType")
  private KeyTypeEnum keyType = null;

  @SerializedName("grantTypesToBeSupported")
  private List<String> grantTypesToBeSupported = new ArrayList<String>();

  @SerializedName("callbackUrl")
  private String callbackUrl = null;

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

  public ApplicationKeyGenerateRequestDTO grantTypesToBeSupported(List<String> grantTypesToBeSupported) {
    this.grantTypesToBeSupported = grantTypesToBeSupported;
    return this;
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

  public void setGrantTypesToBeSupported(List<String> grantTypesToBeSupported) {
    this.grantTypesToBeSupported = grantTypesToBeSupported;
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

