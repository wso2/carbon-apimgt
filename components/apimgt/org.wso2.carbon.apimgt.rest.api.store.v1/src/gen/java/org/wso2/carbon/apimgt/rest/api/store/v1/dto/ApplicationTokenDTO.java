package org.wso2.carbon.apimgt.rest.api.store.v1.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ApplicationTokenDTO  {
  
  
  
  private String accessToken = null;
  
  
  private String tokenScopes = null;
  
  
  private Long validityTime = null;

  
  /**
   * Access token
   **/
  @ApiModelProperty(value = "Access token")
  @JsonProperty("accessToken")
  public String getAccessToken() {
    return accessToken;
  }
  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  
  /**
   * Valid scopes for the access token
   **/
  @ApiModelProperty(value = "Valid scopes for the access token")
  @JsonProperty("tokenScopes")
  public String getTokenScopes() {
    return tokenScopes;
  }
  public void setTokenScopes(String tokenScopes) {
    this.tokenScopes = tokenScopes;
  }

  
  /**
   * Maximum validity time for the access token
   **/
  @ApiModelProperty(value = "Maximum validity time for the access token")
  @JsonProperty("validityTime")
  public Long getValidityTime() {
    return validityTime;
  }
  public void setValidityTime(Long validityTime) {
    this.validityTime = validityTime;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationTokenDTO {\n");
    
    sb.append("  accessToken: ").append(accessToken).append("\n");
    sb.append("  tokenScopes: ").append(tokenScopes).append("\n");
    sb.append("  validityTime: ").append(validityTime).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
