package org.wso2.carbon.apimgt.rest.api.admin.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class APIInfoDTO  {
  
  
  
  private String name = null;
  
  
  private String version = null;
  
  
  private String provider = null;

  
  /**
   * The name of the API.
   **/
  @ApiModelProperty(value = "The name of the API.")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  
  /**
   * The version of the API
   **/
  @ApiModelProperty(value = "The version of the API")
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  
  /**
   * The provider of the API
   **/
  @ApiModelProperty(value = "The provider of the API")
  @JsonProperty("provider")
  public String getProvider() {
    return provider;
  }
  public void setProvider(String provider) {
    this.provider = provider;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIInfoDTO {\n");
    
    sb.append("  name: ").append(name).append("\n");
    sb.append("  version: ").append(version).append("\n");
    sb.append("  provider: ").append(provider).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
