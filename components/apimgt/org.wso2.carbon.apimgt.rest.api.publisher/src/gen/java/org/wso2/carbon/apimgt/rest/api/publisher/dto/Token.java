package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;

/**
 * Token
 */
@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-10-24T13:00:17.095+05:30")
public class Token   {
  private String accessToken = null;

  private List<String> tokenScopes = new ArrayList<String>();

  private Long validityTime = null;

  public Token accessToken(String accessToken) {
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

  public Token tokenScopes(List<String> tokenScopes) {
    this.tokenScopes = tokenScopes;
    return this;
  }

  public Token addTokenScopesItem(String tokenScopesItem) {
    this.tokenScopes.add(tokenScopesItem);
    return this;
  }

   /**
   * Valid scopes for the access token
   * @return tokenScopes
  **/
  @ApiModelProperty(value = "Valid scopes for the access token")
  public List<String> getTokenScopes() {
    return tokenScopes;
  }

  public void setTokenScopes(List<String> tokenScopes) {
    this.tokenScopes = tokenScopes;
  }

  public Token validityTime(Long validityTime) {
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
    Token token = (Token) o;
    return Objects.equals(this.accessToken, token.accessToken) &&
        Objects.equals(this.tokenScopes, token.tokenScopes) &&
        Objects.equals(this.validityTime, token.validityTime);
  }

  @Override
  public int hashCode() {
    return Objects.hash(accessToken, tokenScopes, validityTime);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class Token {\n");
    
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

