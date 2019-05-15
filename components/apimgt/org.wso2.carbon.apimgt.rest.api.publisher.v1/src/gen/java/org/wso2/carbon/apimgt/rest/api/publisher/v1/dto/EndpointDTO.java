package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.EndpointEndpointConfigDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.EndpointEndpointSecurityDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class EndpointDTO  {
  
  
  
  private String id = null;
  
  
  private String name = null;
  
  
  private EndpointEndpointConfigDTO endpointConfig = null;
  
  
  private EndpointEndpointSecurityDTO endpointSecurity = null;
  
  
  private Long maxTps = null;
  
  
  private String type = null;

  
  /**
   * UUID of the Endpoint entry\n
   **/
  @ApiModelProperty(value = "UUID of the Endpoint entry\n")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  
  /**
   * name of the Endpoint entry\n
   **/
  @ApiModelProperty(value = "name of the Endpoint entry\n")
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
  @JsonProperty("endpointConfig")
  public EndpointEndpointConfigDTO getEndpointConfig() {
    return endpointConfig;
  }
  public void setEndpointConfig(EndpointEndpointConfigDTO endpointConfig) {
    this.endpointConfig = endpointConfig;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("endpointSecurity")
  public EndpointEndpointSecurityDTO getEndpointSecurity() {
    return endpointSecurity;
  }
  public void setEndpointSecurity(EndpointEndpointSecurityDTO endpointSecurity) {
    this.endpointSecurity = endpointSecurity;
  }

  
  /**
   * Endpoint max tps
   **/
  @ApiModelProperty(value = "Endpoint max tps")
  @JsonProperty("maxTps")
  public Long getMaxTps() {
    return maxTps;
  }
  public void setMaxTps(Long maxTps) {
    this.maxTps = maxTps;
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
    sb.append("class EndpointDTO {\n");
    
    sb.append("  id: ").append(id).append("\n");
    sb.append("  name: ").append(name).append("\n");
    sb.append("  endpointConfig: ").append(endpointConfig).append("\n");
    sb.append("  endpointSecurity: ").append(endpointSecurity).append("\n");
    sb.append("  maxTps: ").append(maxTps).append("\n");
    sb.append("  type: ").append(type).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
