package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class APIMaxTpsDTO  {
  
  
  
  private Long sandbox = null;
  
  
  private Long production = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("sandbox")
  public Long getSandbox() {
    return sandbox;
  }
  public void setSandbox(Long sandbox) {
    this.sandbox = sandbox;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("production")
  public Long getProduction() {
    return production;
  }
  public void setProduction(Long production) {
    this.production = production;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIMaxTpsDTO {\n");
    
    sb.append("  sandbox: ").append(sandbox).append("\n");
    sb.append("  production: ").append(production).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
