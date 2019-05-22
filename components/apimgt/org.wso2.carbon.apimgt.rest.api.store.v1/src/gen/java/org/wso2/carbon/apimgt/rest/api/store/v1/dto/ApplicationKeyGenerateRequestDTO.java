package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ApplicationKeyGenerateRequestDTO  {
  
  
  public enum KeyTypeEnum {
     PRODUCTION,  SANDBOX, 
  };
  @NotNull
  private KeyTypeEnum keyType = null;
  
  @NotNull
  private List<String> grantTypesToBeSupported = new ArrayList<String>();
  
  
  private String callbackUrl = null;
  
  
  private List<String> scopes = new ArrayList<String>();
  
  
  private String validityTime = null;

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("keyType")
  public KeyTypeEnum getKeyType() {
    return keyType;
  }
  public void setKeyType(KeyTypeEnum keyType) {
    this.keyType = keyType;
  }

  
  /**
   * Grant types that should be supported by the application
   **/
  @ApiModelProperty(required = true, value = "Grant types that should be supported by the application")
  @JsonProperty("grantTypesToBeSupported")
  public List<String> getGrantTypesToBeSupported() {
    return grantTypesToBeSupported;
  }
  public void setGrantTypesToBeSupported(List<String> grantTypesToBeSupported) {
    this.grantTypesToBeSupported = grantTypesToBeSupported;
  }

  
  /**
   * Callback URL
   **/
  @ApiModelProperty(value = "Callback URL")
  @JsonProperty("callbackUrl")
  public String getCallbackUrl() {
    return callbackUrl;
  }
  public void setCallbackUrl(String callbackUrl) {
    this.callbackUrl = callbackUrl;
  }

  
  /**
   * Allowed scopes for the access token
   **/
  @ApiModelProperty(value = "Allowed scopes for the access token")
  @JsonProperty("scopes")
  public List<String> getScopes() {
    return scopes;
  }
  public void setScopes(List<String> scopes) {
    this.scopes = scopes;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("validityTime")
  public String getValidityTime() {
    return validityTime;
  }
  public void setValidityTime(String validityTime) {
    this.validityTime = validityTime;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationKeyGenerateRequestDTO {\n");
    
    sb.append("  keyType: ").append(keyType).append("\n");
    sb.append("  grantTypesToBeSupported: ").append(grantTypesToBeSupported).append("\n");
    sb.append("  callbackUrl: ").append(callbackUrl).append("\n");
    sb.append("  scopes: ").append(scopes).append("\n");
    sb.append("  validityTime: ").append(validityTime).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
