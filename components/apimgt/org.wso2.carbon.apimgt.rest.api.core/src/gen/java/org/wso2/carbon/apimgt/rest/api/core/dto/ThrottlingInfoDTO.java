package org.wso2.carbon.apimgt.rest.api.core.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.core.dto.CredentialsDTO;
import java.util.Objects;

/**
 * ThrottlingInfoDTO
 */
public class ThrottlingInfoDTO   {
  @SerializedName("serverURL")
  private String serverURL = null;

  @SerializedName("credentials")
  private CredentialsDTO credentials = null;

  public ThrottlingInfoDTO serverURL(String serverURL) {
    this.serverURL = serverURL;
    return this;
  }

   /**
   * Get serverURL
   * @return serverURL
  **/
  @ApiModelProperty(value = "")
  public String getServerURL() {
    return serverURL;
  }

  public void setServerURL(String serverURL) {
    this.serverURL = serverURL;
  }

  public ThrottlingInfoDTO credentials(CredentialsDTO credentials) {
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
    ThrottlingInfoDTO throttlingInfo = (ThrottlingInfoDTO) o;
    return Objects.equals(this.serverURL, throttlingInfo.serverURL) &&
        Objects.equals(this.credentials, throttlingInfo.credentials);
  }

  @Override
  public int hashCode() {
    return Objects.hash(serverURL, credentials);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ThrottlingInfoDTO {\n");
    
    sb.append("    serverURL: ").append(toIndentedString(serverURL)).append("\n");
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

