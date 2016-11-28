package org.wso2.carbon.apimgt.rest.api.store.dto;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;
import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class TokenDTO  {
  
  
  
  private String accessToken = null;
  
  
  private List<String> tokenScopes = new ArrayList<String>();
  
  
  private Long validityTime = null;


  private String lastUpdatedTime = null;

  private String createdTime = null;

  /**
  * gets and sets the lastUpdatedTime for TokenDTO
  **/
  @JsonIgnore
  public String getLastUpdatedTime(){
    return lastUpdatedTime;
  }
  public void setLastUpdatedTime(String lastUpdatedTime){
    this.lastUpdatedTime=lastUpdatedTime;
  }

  /**
  * gets and sets the createdTime for a TokenDTO
  **/

  @JsonIgnore
  public String getCreatedTime(){
    return createdTime;
  }
  public void setCreatedTime(String createdTime){
    this.createdTime=createdTime;
  }

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
  public List<String> getTokenScopes() {
    return tokenScopes;
  }
  public void setTokenScopes(List<String> tokenScopes) {
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
    sb.append("class TokenDTO {\n");
    
    sb.append("  accessToken: ").append(accessToken).append("\n");
    sb.append("  tokenScopes: ").append(tokenScopes).append("\n");
    sb.append("  validityTime: ").append(validityTime).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
