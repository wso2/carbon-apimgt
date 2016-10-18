package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import io.swagger.annotations.*;
import javax.ws.rs.*;
import com.fasterxml.jackson.annotation.*;
import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class SequenceDTO  {
  
  
  @NotNull
  private String name = null;
  
  
  private String config = null;
  
  
  private String type = null;

  
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
  @ApiModelProperty(value = "")
  @JsonProperty("config")
  public String getConfig() {
    return config;
  }
  public void setConfig(String config) {
    this.config = config;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class SequenceDTO {\n");
    
    sb.append("  name: ").append(name).append("\n");
    sb.append("  config: ").append(config).append("\n");
    sb.append("  type: ").append(type).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
