package org.wso2.carbon.apimgt.rest.api.model;

import java.math.BigDecimal;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;


@ApiModel(description = "")
public class ExternalStore  {
  
  private BigDecimal name = null;
  private String endpoint = null;

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("name")
  public BigDecimal getName() {
    return name;
  }
  public void setName(BigDecimal name) {
    this.name = name;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("endpoint")
  public String getEndpoint() {
    return endpoint;
  }
  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExternalStore {\n");
    
    sb.append("  name: ").append(name).append("\n");
    sb.append("  endpoint: ").append(endpoint).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
