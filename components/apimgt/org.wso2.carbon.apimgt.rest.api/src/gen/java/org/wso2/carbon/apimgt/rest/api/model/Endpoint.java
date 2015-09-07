package org.wso2.carbon.apimgt.rest.api.model;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;


@ApiModel(description = "")
public class Endpoint  {
  
  public enum TypeEnum {
     Production,  Sandbox,  ProductionFailOver,  SandboxFailOver, 
  };
  private TypeEnum type = null;
  private String url = null;

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("type")
  public TypeEnum getType() {
    return type;
  }
  public void setType(TypeEnum type) {
    this.type = type;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("url")
  public String getUrl() {
    return url;
  }
  public void setUrl(String url) {
    this.url = url;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class Endpoint {\n");
    
    sb.append("  type: ").append(type).append("\n");
    sb.append("  url: ").append(url).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
