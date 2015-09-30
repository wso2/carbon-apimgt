package org.wso2.carbon.apimgt.rest.api.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class EnvironmentDTO  {
  
  
  @NotNull
  private String name = null;
  
  @NotNull
  private String type = null;
  
  @NotNull
  private Boolean apiConsole = null;
  
  @NotNull
  private String serverUrl = null;

  
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
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("apiConsole")
  public Boolean getApiConsole() {
    return apiConsole;
  }
  public void setApiConsole(Boolean apiConsole) {
    this.apiConsole = apiConsole;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("serverUrl")
  public String getServerUrl() {
    return serverUrl;
  }
  public void setServerUrl(String serverUrl) {
    this.serverUrl = serverUrl;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class EnvironmentDTO {\n");
    
    sb.append("  name: ").append(name).append("\n");
    sb.append("  type: ").append(type).append("\n");
    sb.append("  apiConsole: ").append(apiConsole).append("\n");
    sb.append("  serverUrl: ").append(serverUrl).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
