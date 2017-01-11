package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class WsdlDTO  {
  
  
  
  private String id = null;
  
  @NotNull
  private String name = null;
  
  @NotNull
  private String config = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("config")
  public String getConfig() {
    return config;
  }
  public void setConfig(String config) {
    this.config = config;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class WsdlDTO {\n");
    
    sb.append("  id: ").append(id).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  config: ").append(config).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
