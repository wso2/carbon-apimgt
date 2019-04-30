package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class APIDefinitionValidationResponseWsdlInfoEndpointsDTO  {
  
  
  
  private String name = null;
  
  
  private String location = null;

  
  /**
   * Name of the endpoint
   **/
  @ApiModelProperty(value = "Name of the endpoint")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  
  /**
   * Endpoint URL
   **/
  @ApiModelProperty(value = "Endpoint URL")
  @JsonProperty("location")
  public String getLocation() {
    return location;
  }
  public void setLocation(String location) {
    this.location = location;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIDefinitionValidationResponseWsdlInfoEndpointsDTO {\n");
    
    sb.append("  name: ").append(name).append("\n");
    sb.append("  location: ").append(location).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
