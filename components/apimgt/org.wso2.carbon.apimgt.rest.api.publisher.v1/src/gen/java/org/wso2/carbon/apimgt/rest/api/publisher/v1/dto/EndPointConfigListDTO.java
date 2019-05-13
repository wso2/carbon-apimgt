package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class EndPointConfigListDTO  {
  
  
  
  private String url = null;
  
  
  private String timeout = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("url")
  public String getUrl() {
    return url;
  }
  public void setUrl(String url) {
    this.url = url;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("timeout")
  public String getTimeout() {
    return timeout;
  }
  public void setTimeout(String timeout) {
    this.timeout = timeout;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class EndPointConfigListDTO {\n");
    
    sb.append("  url: ").append(url).append("\n");
    sb.append("  timeout: ").append(timeout).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
