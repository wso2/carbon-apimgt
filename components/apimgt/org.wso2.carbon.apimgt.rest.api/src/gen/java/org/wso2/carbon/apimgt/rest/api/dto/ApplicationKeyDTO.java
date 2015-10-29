package org.wso2.carbon.apimgt.rest.api.dto;

import org.wso2.carbon.apimgt.rest.api.dto.TokenDTO;
import java.util.*;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class ApplicationKeyDTO  {
  
  
  
  private String consumerKey = null;
  
  
  private String consumerSecret = null;
  
  
  private List<String> supportedGrantTypes = new ArrayList<String>() ;
  
  
  private String keyState = null;
  
  
  private String clientName = null;
  
  
  private TokenDTO token = null;

  
  /**
   * Consumer key of the application
   **/
  @ApiModelProperty(value = "Consumer key of the application")
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
  @ApiModelProperty(value = "Consumer secret of the application")
  @JsonProperty("consumerSecret")
  public String getConsumerSecret() {
    return consumerSecret;
  }
  public void setConsumerSecret(String consumerSecret) {
    this.consumerSecret = consumerSecret;
  }

  
  /**
   * Supported grant types for the application
   **/
  @ApiModelProperty(value = "Supported grant types for the application")
  @JsonProperty("supportedGrantTypes")
  public List<String> getSupportedGrantTypes() {
    return supportedGrantTypes;
  }
  public void setSupportedGrantTypes(List<String> supportedGrantTypes) {
    this.supportedGrantTypes = supportedGrantTypes;
  }

  
  /**
   * State of the key generation of the application
   **/
  @ApiModelProperty(value = "State of the key generation of the application")
  @JsonProperty("keyState")
  public String getKeyState() {
    return keyState;
  }
  public void setKeyState(String keyState) {
    this.keyState = keyState;
  }

  
  /**
   * Application client representation
   **/
  @ApiModelProperty(value = "Application client representation")
  @JsonProperty("clientName")
  public String getClientName() {
    return clientName;
  }
  public void setClientName(String clientName) {
    this.clientName = clientName;
  }

  
  /**
   * Token details object
   **/
  @ApiModelProperty(value = "Token details object")
  @JsonProperty("token")
  public TokenDTO getToken() {
    return token;
  }
  public void setToken(TokenDTO token) {
    this.token = token;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationKeyDTO {\n");
    
    sb.append("  consumerKey: ").append(consumerKey).append("\n");
    sb.append("  consumerSecret: ").append(consumerSecret).append("\n");
    sb.append("  supportedGrantTypes: ").append(supportedGrantTypes).append("\n");
    sb.append("  keyState: ").append(keyState).append("\n");
    sb.append("  clientName: ").append(clientName).append("\n");
    sb.append("  token: ").append(token).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
