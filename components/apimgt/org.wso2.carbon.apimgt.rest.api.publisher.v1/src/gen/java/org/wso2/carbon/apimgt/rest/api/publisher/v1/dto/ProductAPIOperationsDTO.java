package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ProductAPIOperationsDTO  {
  
  
  
  private String uritemplate = null;
  
  
  private String httpVerb = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("uritemplate")
  public String getUritemplate() {
    return uritemplate;
  }
  public void setUritemplate(String uritemplate) {
    this.uritemplate = uritemplate;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("httpVerb")
  public String getHttpVerb() {
    return httpVerb;
  }
  public void setHttpVerb(String httpVerb) {
    this.httpVerb = httpVerb;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ProductAPIOperationsDTO {\n");
    
    sb.append("  uritemplate: ").append(uritemplate).append("\n");
    sb.append("  httpVerb: ").append(httpVerb).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
