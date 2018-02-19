package org.wso2.carbon.apimgt.micro.gateway.api.synchronizer.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


@ApiModel(description = "")
public class EnvironmentEndpointsDTO  {
  
  
  
  private String http = null;
  
  
  private String https = null;

  
  /**
   * HTTP environment URL
   **/
  @ApiModelProperty(value = "HTTP environment URL")
  @JsonProperty("http")
  public String getHttp() {
    return http;
  }
  public void setHttp(String http) {
    this.http = http;
  }

  
  /**
   * HTTPS environment URL
   **/
  @ApiModelProperty(value = "HTTPS environment URL")
  @JsonProperty("https")
  public String getHttps() {
    return https;
  }
  public void setHttps(String https) {
    this.https = https;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class EnvironmentEndpointsDTO {\n");
    
    sb.append("  http: ").append(http).append("\n");
    sb.append("  https: ").append(https).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
