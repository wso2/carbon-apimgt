package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class WsdlDTO  {
  
  
  @NotNull
  private String name = null;
  
  
  private String wsdlDefinition = null;

  
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
  @JsonProperty("wsdlDefinition")
  public String getWsdlDefinition() {
    return wsdlDefinition;
  }
  public void setWsdlDefinition(String wsdlDefinition) {
    this.wsdlDefinition = wsdlDefinition;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class WsdlDTO {\n");
    
    sb.append("  name: ").append(name).append("\n");
    sb.append("  wsdlDefinition: ").append(wsdlDefinition).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
