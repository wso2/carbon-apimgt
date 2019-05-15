package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.EndpointDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class APIEndpointDTO  {
  
  
  
  private EndpointDTO inline = null;
  
  
  private String type = null;
  
  
  private String key = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("inline")
  public EndpointDTO getInline() {
    return inline;
  }
  public void setInline(EndpointDTO inline) {
    this.inline = inline;
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

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("key")
  public String getKey() {
    return key;
  }
  public void setKey(String key) {
    this.key = key;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIEndpointDTO {\n");
    
    sb.append("  inline: ").append(inline).append("\n");
    sb.append("  type: ").append(type).append("\n");
    sb.append("  key: ").append(key).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
