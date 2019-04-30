package org.wso2.carbon.apimgt.rest.api.store.v1.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ApplicationKeyMappingRequestDTO  {
  
  
  @NotNull
  private String consumerKey = null;
  
  @NotNull
  private String consumerSecret = null;
  
  public enum KeyTypeEnum {
     PRODUCTION,  SANDBOX, 
  };
  
  private KeyTypeEnum keyType = null;

  
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
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("keyType")
  public KeyTypeEnum getKeyType() {
    return keyType;
  }
  public void setKeyType(KeyTypeEnum keyType) {
    this.keyType = keyType;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationKeyMappingRequestDTO {\n");
    
    sb.append("  consumerKey: ").append(consumerKey).append("\n");
    sb.append("  consumerSecret: ").append(consumerSecret).append("\n");
    sb.append("  keyType: ").append(keyType).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
