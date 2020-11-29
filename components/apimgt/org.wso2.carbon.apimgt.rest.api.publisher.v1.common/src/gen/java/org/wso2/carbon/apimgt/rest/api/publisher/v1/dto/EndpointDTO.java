package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.EndpointEndpointConfigDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.EndpointEndpointSecurityDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class EndpointDTO   {
  
    private String id = null;
    private String name = null;
    private EndpointEndpointConfigDTO endpointConfig = null;
    private EndpointEndpointSecurityDTO endpointSecurity = null;
    private Long maxTps = null;
    private String type = null;

  /**
   * UUID of the Endpoint entry 
   **/
  public EndpointDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "UUID of the Endpoint entry ")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   * name of the Endpoint entry 
   **/
  public EndpointDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "Endpoint 1", value = "name of the Endpoint entry ")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public EndpointDTO endpointConfig(EndpointEndpointConfigDTO endpointConfig) {
    this.endpointConfig = endpointConfig;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("endpointConfig")
  public EndpointEndpointConfigDTO getEndpointConfig() {
    return endpointConfig;
  }
  public void setEndpointConfig(EndpointEndpointConfigDTO endpointConfig) {
    this.endpointConfig = endpointConfig;
  }

  /**
   **/
  public EndpointDTO endpointSecurity(EndpointEndpointSecurityDTO endpointSecurity) {
    this.endpointSecurity = endpointSecurity;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
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
  public EndpointDTO maxTps(Long maxTps) {
    this.maxTps = maxTps;
    return this;
  }

  
  @ApiModelProperty(example = "1000", value = "Endpoint max tps")
  @JsonProperty("maxTps")
  public Long getMaxTps() {
    return maxTps;
  }
  public void setMaxTps(Long maxTps) {
    this.maxTps = maxTps;
  }

  /**
   **/
  public EndpointDTO type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(example = "http", value = "")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EndpointDTO endpoint = (EndpointDTO) o;
    return Objects.equals(id, endpoint.id) &&
        Objects.equals(name, endpoint.name) &&
        Objects.equals(endpointConfig, endpoint.endpointConfig) &&
        Objects.equals(endpointSecurity, endpoint.endpointSecurity) &&
        Objects.equals(maxTps, endpoint.maxTps) &&
        Objects.equals(type, endpoint.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, endpointConfig, endpointSecurity, maxTps, type);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EndpointDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    endpointConfig: ").append(toIndentedString(endpointConfig)).append("\n");
    sb.append("    endpointSecurity: ").append(toIndentedString(endpointSecurity)).append("\n");
    sb.append("    maxTps: ").append(toIndentedString(maxTps)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

