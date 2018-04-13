package org.wso2.carbon.apimgt.rest.api.core.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.core.dto.CredentialsDTO;
import java.util.Objects;

/**
 * AnalyticsInfoDTO
 */
public class AnalyticsInfoDTO   {
  @SerializedName("enabled")
  private Boolean enabled = null;

  @SerializedName("serverURL")
  private String serverURL = null;

  @SerializedName("credentials")
  private CredentialsDTO credentials = null;

  public AnalyticsInfoDTO enabled(Boolean enabled) {
    this.enabled = enabled;
    return this;
  }

   /**
   * Get enabled
   * @return enabled
  **/
  @ApiModelProperty(example = "true", value = "")
  public Boolean getEnabled() {
    return enabled;
  }

  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  public AnalyticsInfoDTO serverURL(String serverURL) {
    this.serverURL = serverURL;
    return this;
  }

   /**
   * Get serverURL
   * @return serverURL
  **/
  @ApiModelProperty(example = "tcp://localhost:7612", value = "")
  public String getServerURL() {
    return serverURL;
  }

  public void setServerURL(String serverURL) {
    this.serverURL = serverURL;
  }

  public AnalyticsInfoDTO credentials(CredentialsDTO credentials) {
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
    AnalyticsInfoDTO analyticsInfo = (AnalyticsInfoDTO) o;
    return Objects.equals(this.enabled, analyticsInfo.enabled) &&
        Objects.equals(this.serverURL, analyticsInfo.serverURL) &&
        Objects.equals(this.credentials, analyticsInfo.credentials);
  }

  @Override
  public int hashCode() {
    return Objects.hash(enabled, serverURL, credentials);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AnalyticsInfoDTO {\n");
    
    sb.append("    enabled: ").append(toIndentedString(enabled)).append("\n");
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

