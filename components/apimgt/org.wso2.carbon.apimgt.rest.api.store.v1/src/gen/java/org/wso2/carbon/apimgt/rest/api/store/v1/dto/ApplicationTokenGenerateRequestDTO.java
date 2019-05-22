package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;



public class ApplicationTokenGenerateRequestDTO   {
  
    private String consumerSecret = null;
    private Integer validityPeriod = null;
    private String scopes = null;
    private String revokeToken = null;

  /**
   * Consumer secret of the application
   **/
  public ApplicationTokenGenerateRequestDTO consumerSecret(String consumerSecret) {
    this.consumerSecret = consumerSecret;
    return this;
  }

  
  @ApiModelProperty(value = "Consumer secret of the application")
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
  public ApplicationTokenGenerateRequestDTO validityPeriod(Integer validityPeriod) {
    this.validityPeriod = validityPeriod;
    return this;
  }

  
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
  public ApplicationTokenGenerateRequestDTO scopes(String scopes) {
    this.scopes = scopes;
    return this;
  }

  
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
  public ApplicationTokenGenerateRequestDTO revokeToken(String revokeToken) {
    this.revokeToken = revokeToken;
    return this;
  }

  
  @ApiModelProperty(value = "Token to be revoked, if any.")
  @JsonProperty("revokeToken")
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
    return Objects.equals(consumerSecret, applicationTokenGenerateRequest.consumerSecret) &&
        Objects.equals(validityPeriod, applicationTokenGenerateRequest.validityPeriod) &&
        Objects.equals(scopes, applicationTokenGenerateRequest.scopes) &&
        Objects.equals(revokeToken, applicationTokenGenerateRequest.revokeToken);
  }

  @Override
  public int hashCode() {
    return Objects.hash(consumerSecret, validityPeriod, scopes, revokeToken);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationTokenGenerateRequestDTO {\n");
    
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

