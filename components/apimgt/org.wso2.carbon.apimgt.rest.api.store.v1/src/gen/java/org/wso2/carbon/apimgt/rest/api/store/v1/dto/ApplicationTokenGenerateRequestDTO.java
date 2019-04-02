package org.wso2.carbon.apimgt.rest.api.store.v1.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ApplicationTokenGenerateRequestDTO  {
  
  
  @NotNull
  private String consumerKey = null;
  
  @NotNull
  private String consumerSecret = null;
  
  
  private Integer validityPeriod = null;
  
  
  private String scopes = null;
  
  
  private String revokeToken = null;

  
  /**
   * Consumer key of the application
   **/
  @ApiModelProperty(required = true, value = "Consumer key of the application")
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
  @ApiModelProperty(required = true, value = "Consumer secret of the application")
  @JsonProperty("consumerSecret")
  public String getConsumerSecret() {
    return consumerSecret;
  }
  public void setConsumerSecret(String consumerSecret) {
    this.consumerSecret = consumerSecret;
  }

  
  /**
   * Token validity period
   **/
  @ApiModelProperty(value = "Token validity period")
  @JsonProperty("validityPeriod")
  public Integer getValidityPeriod() {
    return validityPeriod;
  }
  public void setValidityPeriod(Integer validityPeriod) {
    this.validityPeriod = validityPeriod;
  }

  
  /**
   * Allowed scopes (space seperated) for the access token
   **/
  @ApiModelProperty(value = "Allowed scopes (space seperated) for the access token")
  @JsonProperty("scopes")
  public String getScopes() {
    return scopes;
  }
  public void setScopes(String scopes) {
    this.scopes = scopes;
  }

  
  /**
   * Token to be revoked, if any.
   **/
  @ApiModelProperty(value = "Token to be revoked, if any.")
  @JsonProperty("revokeToken")
  public String getRevokeToken() {
    return revokeToken;
  }
  public void setRevokeToken(String revokeToken) {
    this.revokeToken = revokeToken;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationTokenGenerateRequestDTO {\n");
    
    sb.append("  consumerKey: ").append(consumerKey).append("\n");
    sb.append("  consumerSecret: ").append(consumerSecret).append("\n");
    sb.append("  validityPeriod: ").append(validityPeriod).append("\n");
    sb.append("  scopes: ").append(scopes).append("\n");
    sb.append("  revokeToken: ").append(revokeToken).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
