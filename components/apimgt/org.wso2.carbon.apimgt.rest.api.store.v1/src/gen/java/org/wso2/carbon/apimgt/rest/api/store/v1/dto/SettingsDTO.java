package org.wso2.carbon.apimgt.rest.api.store.v1.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class SettingsDTO  {
  
  
  
  private String serverUrl = null;
  
  
  private String apiStoreUrl = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("serverUrl")
  public String getServerUrl() {
    return serverUrl;
  }
  public void setServerUrl(String serverUrl) {
    this.serverUrl = serverUrl;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("apiStoreUrl")
  public String getApiStoreUrl() {
    return apiStoreUrl;
  }
  public void setApiStoreUrl(String apiStoreUrl) {
    this.apiStoreUrl = apiStoreUrl;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class SettingsDTO {\n");
    
    sb.append("  serverUrl: ").append(serverUrl).append("\n");
    sb.append("  apiStoreUrl: ").append(apiStoreUrl).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
