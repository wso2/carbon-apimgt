package org.wso2.carbon.apimgt.rest.api.store.v1.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ApplicationKeyReGenerateResponseDTO  {
  
  
  
  private String consumerKey = null;
  
  
  private String consumerSecret = null;

  
  /**
   * The consumer key associated with the application, used to indetify the client
   **/
  @ApiModelProperty(value = "The consumer key associated with the application, used to indetify the client")
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

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationKeyReGenerateResponseDTO {\n");
    
    sb.append("  consumerKey: ").append(consumerKey).append("\n");
    sb.append("  consumerSecret: ").append(consumerSecret).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
