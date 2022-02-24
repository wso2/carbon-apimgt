package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.EndpointConfigDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.EndpointSecurityDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class OperationEndpointDTO   {
  
    private String id = null;
    private String name = null;
    private EndpointSecurityDTO securityConfig = null;
    private EndpointConfigDTO endpointConfig = null;

  /**
   **/
  public OperationEndpointDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   **/
  public OperationEndpointDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("name")
  @NotNull
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public OperationEndpointDTO securityConfig(EndpointSecurityDTO securityConfig) {
    this.securityConfig = securityConfig;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("securityConfig")
  public EndpointSecurityDTO getSecurityConfig() {
    return securityConfig;
  }
  public void setSecurityConfig(EndpointSecurityDTO securityConfig) {
    this.securityConfig = securityConfig;
  }

  /**
   **/
  public OperationEndpointDTO endpointConfig(EndpointConfigDTO endpointConfig) {
    this.endpointConfig = endpointConfig;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("endpointConfig")
  public EndpointConfigDTO getEndpointConfig() {
    return endpointConfig;
  }
  public void setEndpointConfig(EndpointConfigDTO endpointConfig) {
    this.endpointConfig = endpointConfig;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OperationEndpointDTO operationEndpoint = (OperationEndpointDTO) o;
    return Objects.equals(id, operationEndpoint.id) &&
        Objects.equals(name, operationEndpoint.name) &&
        Objects.equals(securityConfig, operationEndpoint.securityConfig) &&
        Objects.equals(endpointConfig, operationEndpoint.endpointConfig);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, securityConfig, endpointConfig);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OperationEndpointDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    securityConfig: ").append(toIndentedString(securityConfig)).append("\n");
    sb.append("    endpointConfig: ").append(toIndentedString(endpointConfig)).append("\n");
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

