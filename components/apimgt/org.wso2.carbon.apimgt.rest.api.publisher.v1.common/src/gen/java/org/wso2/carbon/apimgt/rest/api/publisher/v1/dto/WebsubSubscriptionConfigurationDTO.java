package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class WebsubSubscriptionConfigurationDTO   {
  
    private Boolean enable = false;
    private String secret = null;
    private String signingAlgorithm = null;
    private String signatureHeader = null;

  /**
   * Toggle enable WebSub subscription configuration
   **/
  public WebsubSubscriptionConfigurationDTO enable(Boolean enable) {
    this.enable = enable;
    return this;
  }

  
  @ApiModelProperty(value = "Toggle enable WebSub subscription configuration")
  @JsonProperty("enable")
  public Boolean isEnable() {
    return enable;
  }
  public void setEnable(Boolean enable) {
    this.enable = enable;
  }

  /**
   * Secret key to be used for subscription
   **/
  public WebsubSubscriptionConfigurationDTO secret(String secret) {
    this.secret = secret;
    return this;
  }

  
  @ApiModelProperty(value = "Secret key to be used for subscription")
  @JsonProperty("secret")
  public String getSecret() {
    return secret;
  }
  public void setSecret(String secret) {
    this.secret = secret;
  }

  /**
   * The algorithm used for signing
   **/
  public WebsubSubscriptionConfigurationDTO signingAlgorithm(String signingAlgorithm) {
    this.signingAlgorithm = signingAlgorithm;
    return this;
  }

  
  @ApiModelProperty(value = "The algorithm used for signing")
  @JsonProperty("signingAlgorithm")
  public String getSigningAlgorithm() {
    return signingAlgorithm;
  }
  public void setSigningAlgorithm(String signingAlgorithm) {
    this.signingAlgorithm = signingAlgorithm;
  }

  /**
   * The header uses to send the signature
   **/
  public WebsubSubscriptionConfigurationDTO signatureHeader(String signatureHeader) {
    this.signatureHeader = signatureHeader;
    return this;
  }

  
  @ApiModelProperty(value = "The header uses to send the signature")
  @JsonProperty("signatureHeader")
  public String getSignatureHeader() {
    return signatureHeader;
  }
  public void setSignatureHeader(String signatureHeader) {
    this.signatureHeader = signatureHeader;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WebsubSubscriptionConfigurationDTO websubSubscriptionConfiguration = (WebsubSubscriptionConfigurationDTO) o;
    return Objects.equals(enable, websubSubscriptionConfiguration.enable) &&
        Objects.equals(secret, websubSubscriptionConfiguration.secret) &&
        Objects.equals(signingAlgorithm, websubSubscriptionConfiguration.signingAlgorithm) &&
        Objects.equals(signatureHeader, websubSubscriptionConfiguration.signatureHeader);
  }

  @Override
  public int hashCode() {
    return Objects.hash(enable, secret, signingAlgorithm, signatureHeader);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WebsubSubscriptionConfigurationDTO {\n");
    
    sb.append("    enable: ").append(toIndentedString(enable)).append("\n");
    sb.append("    secret: ").append(toIndentedString(secret)).append("\n");
    sb.append("    signingAlgorithm: ").append(toIndentedString(signingAlgorithm)).append("\n");
    sb.append("    signatureHeader: ").append(toIndentedString(signatureHeader)).append("\n");
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

