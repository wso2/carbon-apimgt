package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.BackendOperationDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class BackendEndpointDataDTO   {
  
    private String id = null;
    private String name = null;
    private String endpoint = null;
    private List<BackendOperationDTO> list = new ArrayList<BackendOperationDTO>();

  /**
   * Backend ID consisting of the UUID of the Endpoint
   **/
  public BackendEndpointDataDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "0c4439fd-9416-3c2e-be6e-1086e0b9aa93", value = "Backend ID consisting of the UUID of the Endpoint")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Backend name
   **/
  public BackendEndpointDataDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "backend1", value = "Backend name")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Backend endpoint
   **/
  public BackendEndpointDataDTO endpoint(String endpoint) {
    this.endpoint = endpoint;
    return this;
  }

  
  @ApiModelProperty(value = "Backend endpoint")
  @JsonProperty("endpoint")
  public String getEndpoint() {
    return endpoint;
  }
  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  /**
   **/
  public BackendEndpointDataDTO list(List<BackendOperationDTO> list) {
    this.list = list;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("list")
  public List<BackendOperationDTO> getList() {
    return list;
  }
  public void setList(List<BackendOperationDTO> list) {
    this.list = list;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BackendEndpointDataDTO backendEndpointData = (BackendEndpointDataDTO) o;
    return Objects.equals(id, backendEndpointData.id) &&
        Objects.equals(name, backendEndpointData.name) &&
        Objects.equals(endpoint, backendEndpointData.endpoint) &&
        Objects.equals(list, backendEndpointData.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, endpoint, list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BackendEndpointDataDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    endpoint: ").append(toIndentedString(endpoint)).append("\n");
    sb.append("    list: ").append(toIndentedString(list)).append("\n");
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

