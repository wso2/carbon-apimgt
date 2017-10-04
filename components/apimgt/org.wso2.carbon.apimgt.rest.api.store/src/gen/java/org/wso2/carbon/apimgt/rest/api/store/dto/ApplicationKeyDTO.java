package org.wso2.carbon.apimgt.rest.api.store.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.dto.TokenDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ApplicationKeyDTO  {
  
  
  
  private String consumerKey = null;
  
  
  private String consumerSecret = null;
  
  
  private List<String> supportedGrantTypes = new ArrayList<String>();
  
  
  private String callbackUrl = null;
  
  
  private String keyState = null;
  
  public enum KeyTypeEnum {
     PRODUCTION,  SANDBOX, 
  };
  
  private KeyTypeEnum keyType = null;
  
  
  private String groupId = null;
  
  
  private TokenDTO token = null;

  private String lastUpdatedTime = null;

  private String createdTime = null;

  /**
  * gets and sets the lastUpdatedTime for ApplicationKeyDTO
  **/
  @JsonIgnore
  public String getLastUpdatedTime(){
    return lastUpdatedTime;
  }
  public void setLastUpdatedTime(String lastUpdatedTime){
    this.lastUpdatedTime=lastUpdatedTime;
  }

  /**
  * gets and sets the createdTime for a ApplicationKeyDTO
  **/

  @JsonIgnore
  public String getCreatedTime(){
    return createdTime;
  }
  public void setCreatedTime(String createdTime){
    this.createdTime=createdTime;
  }

  
  /**
   * The consumer key associated with the application and identifying the client
   **/
  @ApiModelProperty(value = "The consumer key associated with the application and identifying the client")
  @JsonProperty("consumerKey")
  public String getConsumerKey() {
    return consumerKey;
  }
  public void setConsumerKey(String consumerKey) {
    this.consumerKey = consumerKey;
  }

  
  /**
   * The client secret that is used to authenticate the client with the authentication server
   **/
  @ApiModelProperty(value = "The client secret that is used to authenticate the client with the authentication server")
  @JsonProperty("consumerSecret")
  public String getConsumerSecret() {
    return consumerSecret;
  }
  public void setConsumerSecret(String consumerSecret) {
    this.consumerSecret = consumerSecret;
  }

  
  /**
   * The grant types that are supported by the application
   **/
  @ApiModelProperty(value = "The grant types that are supported by the application")
  @JsonProperty("supportedGrantTypes")
  public List<String> getSupportedGrantTypes() {
    return supportedGrantTypes;
  }
  public void setSupportedGrantTypes(List<String> supportedGrantTypes) {
    this.supportedGrantTypes = supportedGrantTypes;
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
   * Describes the state of the key generation.
   **/
  @ApiModelProperty(value = "Describes the state of the key generation.")
  @JsonProperty("keyState")
  public String getKeyState() {
    return keyState;
  }
  public void setKeyState(String keyState) {
    this.keyState = keyState;
  }

  
  /**
   * Describes to which endpoint the key belongs
   **/
  @ApiModelProperty(value = "Describes to which endpoint the key belongs")
  @JsonProperty("keyType")
  public KeyTypeEnum getKeyType() {
    return keyType;
  }
  public void setKeyType(KeyTypeEnum keyType) {
    this.keyType = keyType;
  }

  
  /**
   * Application group id (if any).
   **/
  @ApiModelProperty(value = "Application group id (if any).")
  @JsonProperty("groupId")
  public String getGroupId() {
    return groupId;
  }
  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
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
    sb.append("  callbackUrl: ").append(callbackUrl).append("\n");
    sb.append("  keyState: ").append(keyState).append("\n");
    sb.append("  keyType: ").append(keyType).append("\n");
    sb.append("  groupId: ").append(groupId).append("\n");
    sb.append("  token: ").append(token).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
