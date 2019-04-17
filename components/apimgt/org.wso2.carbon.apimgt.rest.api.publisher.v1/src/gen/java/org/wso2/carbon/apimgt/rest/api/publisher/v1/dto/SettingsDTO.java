package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class SettingsDTO  {
  
  
  
  private String apiPublisherUrl = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("apiPublisherUrl")
  public String getApiPublisherUrl() {
    return apiPublisherUrl;
  }
  public void setApiPublisherUrl(String apiPublisherUrl) {
    this.apiPublisherUrl = apiPublisherUrl;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class SettingsDTO {\n");
    
    sb.append("  apiPublisherUrl: ").append(apiPublisherUrl).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
