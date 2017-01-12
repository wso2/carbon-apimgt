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
  
  
  private String keyState = null;
  
  public enum KeyTypeEnum {
     PRODUCTION,  SANDBOX, 
  };
  
  private KeyTypeEnum keyType = null;
  
  
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
   * Key type
   **/
  @ApiModelProperty(value = "Key type")
  @JsonProperty("keyType")
  public KeyTypeEnum getKeyType() {
    return keyType;
  }
  public void setKeyType(KeyTypeEnum keyType) {
    this.keyType = keyType;
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
    sb.append("  keyState: ").append(keyState).append("\n");
    sb.append("  keyType: ").append(keyType).append("\n");
    sb.append("  token: ").append(token).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
