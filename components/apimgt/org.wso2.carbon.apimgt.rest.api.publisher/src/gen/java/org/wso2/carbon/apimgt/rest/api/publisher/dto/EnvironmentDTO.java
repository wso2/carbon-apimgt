package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.EnvironmentEndpointsDTO;

import io.swagger.annotations.*;
import javax.ws.rs.*;
import com.fasterxml.jackson.annotation.*;
import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class EnvironmentDTO  {
  
  
  @NotNull
  private String name = null;
  
  @NotNull
  private String type = null;
  
  @NotNull
  private String serverUrl = null;
  
  @NotNull
  private Boolean showInApiConsole = null;
  
  @NotNull
  private EnvironmentEndpointsDTO endpoints = null;

  
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
  @JsonProperty("serverUrl")
  public String getServerUrl() {
    return serverUrl;
  }
  public void setServerUrl(String serverUrl) {
    this.serverUrl = serverUrl;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("showInApiConsole")
  public Boolean getShowInApiConsole() {
    return showInApiConsole;
  }
  public void setShowInApiConsole(Boolean showInApiConsole) {
    this.showInApiConsole = showInApiConsole;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("endpoints")
  public EnvironmentEndpointsDTO getEndpoints() {
    return endpoints;
  }
  public void setEndpoints(EnvironmentEndpointsDTO endpoints) {
    this.endpoints = endpoints;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class EnvironmentDTO {\n");
    
    sb.append("  name: ").append(name).append("\n");
    sb.append("  type: ").append(type).append("\n");
    sb.append("  serverUrl: ").append(serverUrl).append("\n");
    sb.append("  showInApiConsole: ").append(showInApiConsole).append("\n");
    sb.append("  endpoints: ").append(endpoints).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
