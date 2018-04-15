package org.wso2.carbon.apimgt.rest.api.store.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * ApplicationTokenDTO
 */
public class ApplicationTokenDTO   {
  @SerializedName("accessToken")
  private String accessToken = null;

  @SerializedName("tokenScopes")
  private String tokenScopes = null;

  @SerializedName("validityTime")
  private Long validityTime = null;

  public ApplicationTokenDTO accessToken(String accessToken) {
    this.accessToken = accessToken;
    return this;
  }

   /**
   * Access token
   * @return accessToken
  **/
  @ApiModelProperty(value = "Access token")
  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public ApplicationTokenDTO tokenScopes(String tokenScopes) {
    this.tokenScopes = tokenScopes;
    return this;
  }

   /**
   * Valid scopes for the access token
   * @return tokenScopes
  **/
  @ApiModelProperty(value = "Valid scopes for the access token")
  public String getTokenScopes() {
    return tokenScopes;
  }

  public void setTokenScopes(String tokenScopes) {
    this.tokenScopes = tokenScopes;
  }

  public ApplicationTokenDTO validityTime(Long validityTime) {
    this.validityTime = validityTime;
    return this;
  }

   /**
   * Maximum validity time for the access token
   * @return validityTime
  **/
  @ApiModelProperty(value = "Maximum validity time for the access token")
  public Long getValidityTime() {
    return validityTime;
  }

  public void setValidityTime(Long validityTime) {
    this.validityTime = validityTime;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApplicationTokenDTO applicationToken = (ApplicationTokenDTO) o;
    return Objects.equals(this.accessToken, applicationToken.accessToken) &&
        Objects.equals(this.tokenScopes, applicationToken.tokenScopes) &&
        Objects.equals(this.validityTime, applicationToken.validityTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accessToken, tokenScopes, validityTime);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationTokenDTO {\n");
    
    sb.append("    accessToken: ").append(toIndentedString(accessToken)).append("\n");
    sb.append("    tokenScopes: ").append(toIndentedString(tokenScopes)).append("\n");
    sb.append("    validityTime: ").append(toIndentedString(validityTime)).append("\n");
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

