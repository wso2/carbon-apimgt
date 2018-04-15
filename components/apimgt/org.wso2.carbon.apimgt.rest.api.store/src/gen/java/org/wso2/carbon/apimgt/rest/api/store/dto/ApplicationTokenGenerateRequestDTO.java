package org.wso2.carbon.apimgt.rest.api.store.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * ApplicationTokenGenerateRequestDTO
 */
public class ApplicationTokenGenerateRequestDTO   {
  @SerializedName("consumerKey")
  private String consumerKey = null;

  @SerializedName("consumerSecret")
  private String consumerSecret = null;

  @SerializedName("validityPeriod")
  private Integer validityPeriod = null;

  @SerializedName("scopes")
  private String scopes = null;

  @SerializedName("revokeToken")
  private String revokeToken = null;

  public ApplicationTokenGenerateRequestDTO consumerKey(String consumerKey) {
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

  public ApplicationTokenGenerateRequestDTO consumerSecret(String consumerSecret) {
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

  public ApplicationTokenGenerateRequestDTO validityPeriod(Integer validityPeriod) {
    this.validityPeriod = validityPeriod;
    return this;
  }

   /**
   * Token validity period
   * @return validityPeriod
  **/
  @ApiModelProperty(value = "Token validity period")
  public Integer getValidityPeriod() {
    return validityPeriod;
  }

  public void setValidityPeriod(Integer validityPeriod) {
    this.validityPeriod = validityPeriod;
  }

  public ApplicationTokenGenerateRequestDTO scopes(String scopes) {
    this.scopes = scopes;
    return this;
  }

   /**
   * Allowed scopes (space seperated) for the access token
   * @return scopes
  **/
  @ApiModelProperty(value = "Allowed scopes (space seperated) for the access token")
  public String getScopes() {
    return scopes;
  }

  public void setScopes(String scopes) {
    this.scopes = scopes;
  }

  public ApplicationTokenGenerateRequestDTO revokeToken(String revokeToken) {
    this.revokeToken = revokeToken;
    return this;
  }

   /**
   * Token to be revoked, if any.
   * @return revokeToken
  **/
  @ApiModelProperty(value = "Token to be revoked, if any.")
  public String getRevokeToken() {
    return revokeToken;
  }

  public void setRevokeToken(String revokeToken) {
    this.revokeToken = revokeToken;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationTokenGenerateRequestDTO applicationTokenGenerateRequest = (ApplicationTokenGenerateRequestDTO) o;
    return Objects.equals(this.consumerKey, applicationTokenGenerateRequest.consumerKey) &&
        Objects.equals(this.consumerSecret, applicationTokenGenerateRequest.consumerSecret) &&
        Objects.equals(this.validityPeriod, applicationTokenGenerateRequest.validityPeriod) &&
        Objects.equals(this.scopes, applicationTokenGenerateRequest.scopes) &&
        Objects.equals(this.revokeToken, applicationTokenGenerateRequest.revokeToken);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consumerKey, consumerSecret, validityPeriod, scopes, revokeToken);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationTokenGenerateRequestDTO {\n");
    
    sb.append("    consumerKey: ").append(toIndentedString(consumerKey)).append("\n");
    sb.append("    consumerSecret: ").append(toIndentedString(consumerSecret)).append("\n");
    sb.append("    validityPeriod: ").append(toIndentedString(validityPeriod)).append("\n");
    sb.append("    scopes: ").append(toIndentedString(scopes)).append("\n");
    sb.append("    revokeToken: ").append(toIndentedString(revokeToken)).append("\n");
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

