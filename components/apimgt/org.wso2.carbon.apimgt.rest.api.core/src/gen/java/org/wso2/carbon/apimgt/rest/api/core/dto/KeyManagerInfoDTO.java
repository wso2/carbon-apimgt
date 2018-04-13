package org.wso2.carbon.apimgt.rest.api.core.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.core.dto.CredentialsDTO;
import java.util.Objects;

/**
 * KeyManagerInfoDTO
 */
public class KeyManagerInfoDTO   {
  @SerializedName("dcrEndpoint")
  private String dcrEndpoint = null;

  @SerializedName("tokenEndpoint")
  private String tokenEndpoint = null;

  @SerializedName("revokeEndpoint")
  private String revokeEndpoint = null;

  @SerializedName("introspectEndpoint")
  private String introspectEndpoint = null;

  @SerializedName("credentials")
  private CredentialsDTO credentials = null;

  public KeyManagerInfoDTO dcrEndpoint(String dcrEndpoint) {
    this.dcrEndpoint = dcrEndpoint;
    return this;
  }

   /**
   * Get dcrEndpoint
   * @return dcrEndpoint
  **/
  @ApiModelProperty(example = "http://localhost:9763/identity/connect/register", value = "")
  public String getDcrEndpoint() {
    return dcrEndpoint;
  }

  public void setDcrEndpoint(String dcrEndpoint) {
    this.dcrEndpoint = dcrEndpoint;
  }

  public KeyManagerInfoDTO tokenEndpoint(String tokenEndpoint) {
    this.tokenEndpoint = tokenEndpoint;
    return this;
  }

   /**
   * Get tokenEndpoint
   * @return tokenEndpoint
  **/
  @ApiModelProperty(example = "https://localhost:9443/oauth2/token", value = "")
  public String getTokenEndpoint() {
    return tokenEndpoint;
  }

  public void setTokenEndpoint(String tokenEndpoint) {
    this.tokenEndpoint = tokenEndpoint;
  }

  public KeyManagerInfoDTO revokeEndpoint(String revokeEndpoint) {
    this.revokeEndpoint = revokeEndpoint;
    return this;
  }

   /**
   * Get revokeEndpoint
   * @return revokeEndpoint
  **/
  @ApiModelProperty(example = "https://localhost:9443/oauth2/revoke", value = "")
  public String getRevokeEndpoint() {
    return revokeEndpoint;
  }

  public void setRevokeEndpoint(String revokeEndpoint) {
    this.revokeEndpoint = revokeEndpoint;
  }

  public KeyManagerInfoDTO introspectEndpoint(String introspectEndpoint) {
    this.introspectEndpoint = introspectEndpoint;
    return this;
  }

   /**
   * Get introspectEndpoint
   * @return introspectEndpoint
  **/
  @ApiModelProperty(example = "http://localhost:9763/oauth2/introspect", value = "")
  public String getIntrospectEndpoint() {
    return introspectEndpoint;
  }

  public void setIntrospectEndpoint(String introspectEndpoint) {
    this.introspectEndpoint = introspectEndpoint;
  }

  public KeyManagerInfoDTO credentials(CredentialsDTO credentials) {
    this.credentials = credentials;
    return this;
  }

   /**
   * Get credentials
   * @return credentials
  **/
  @ApiModelProperty(value = "")
  public CredentialsDTO getCredentials() {
    return credentials;
  }

  public void setCredentials(CredentialsDTO credentials) {
    this.credentials = credentials;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    KeyManagerInfoDTO keyManagerInfo = (KeyManagerInfoDTO) o;
    return Objects.equals(this.dcrEndpoint, keyManagerInfo.dcrEndpoint) &&
        Objects.equals(this.tokenEndpoint, keyManagerInfo.tokenEndpoint) &&
        Objects.equals(this.revokeEndpoint, keyManagerInfo.revokeEndpoint) &&
        Objects.equals(this.introspectEndpoint, keyManagerInfo.introspectEndpoint) &&
        Objects.equals(this.credentials, keyManagerInfo.credentials);
  }

  @Override
  public int hashCode() {
    return Objects.hash(dcrEndpoint, tokenEndpoint, revokeEndpoint, introspectEndpoint, credentials);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class KeyManagerInfoDTO {\n");
    
    sb.append("    dcrEndpoint: ").append(toIndentedString(dcrEndpoint)).append("\n");
    sb.append("    tokenEndpoint: ").append(toIndentedString(tokenEndpoint)).append("\n");
    sb.append("    revokeEndpoint: ").append(toIndentedString(revokeEndpoint)).append("\n");
    sb.append("    introspectEndpoint: ").append(toIndentedString(introspectEndpoint)).append("\n");
    sb.append("    credentials: ").append(toIndentedString(credentials)).append("\n");
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

