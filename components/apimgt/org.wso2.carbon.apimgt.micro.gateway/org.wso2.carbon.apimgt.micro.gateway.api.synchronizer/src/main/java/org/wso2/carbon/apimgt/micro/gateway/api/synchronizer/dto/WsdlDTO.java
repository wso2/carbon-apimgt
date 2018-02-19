package org.wso2.carbon.apimgt.micro.gateway.api.synchronizer.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

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
