package org.wso2.carbon.apimgt.rest.api.store.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ApplicationKeyReGenerateRequestDTO  {
  
  
  @NotNull
  private String consumerKey = null;

  private String lastUpdatedTime = null;

  private String createdTime = null;

  /**
  * gets and sets the lastUpdatedTime for ApplicationKeyReGenerateRequestDTO
  **/
  @JsonIgnore
  public String getLastUpdatedTime(){
    return lastUpdatedTime;
  }
  public void setLastUpdatedTime(String lastUpdatedTime){
    this.lastUpdatedTime=lastUpdatedTime;
  }

  /**
  * gets and sets the createdTime for a ApplicationKeyReGenerateRequestDTO
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
  @ApiModelProperty(required = true, value = "The consumer key associated with the application and identifying the client")
  @JsonProperty("consumerKey")
  public String getConsumerKey() {
    return consumerKey;
  }
  public void setConsumerKey(String consumerKey) {
    this.consumerKey = consumerKey;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationKeyReGenerateRequestDTO {\n");
    
    sb.append("  consumerKey: ").append(consumerKey).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
