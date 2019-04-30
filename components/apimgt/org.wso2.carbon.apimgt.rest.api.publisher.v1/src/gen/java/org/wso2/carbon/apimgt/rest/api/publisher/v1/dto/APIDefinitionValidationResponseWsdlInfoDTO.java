package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import io.swagger.annotations.ApiModel;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDefinitionValidationResponseWsdlInfoBindingInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDefinitionValidationResponseWsdlInfoEndpointsDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;



/**
 * Summary of the WSDL including the basic information
 **/


@ApiModel(description = "Summary of the WSDL including the basic information")
public class APIDefinitionValidationResponseWsdlInfoDTO  {
  
  
  
  private List<APIDefinitionValidationResponseWsdlInfoEndpointsDTO> endpoints = new ArrayList<APIDefinitionValidationResponseWsdlInfoEndpointsDTO>();
  
  
  private APIDefinitionValidationResponseWsdlInfoBindingInfoDTO bindingInfo = null;
  
  
  private String version = null;

  
  /**
   * A list of endpoints the service exposes\n
   **/
  @ApiModelProperty(value = "A list of endpoints the service exposes\n")
  @JsonProperty("endpoints")
  public List<APIDefinitionValidationResponseWsdlInfoEndpointsDTO> getEndpoints() {
    return endpoints;
  }
  public void setEndpoints(List<APIDefinitionValidationResponseWsdlInfoEndpointsDTO> endpoints) {
    this.endpoints = endpoints;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("bindingInfo")
  public APIDefinitionValidationResponseWsdlInfoBindingInfoDTO getBindingInfo() {
    return bindingInfo;
  }
  public void setBindingInfo(APIDefinitionValidationResponseWsdlInfoBindingInfoDTO bindingInfo) {
    this.bindingInfo = bindingInfo;
  }

  
  /**
   * WSDL version\n
   **/
  @ApiModelProperty(value = "WSDL version\n")
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIDefinitionValidationResponseWsdlInfoDTO {\n");
    
    sb.append("  endpoints: ").append(endpoints).append("\n");
    sb.append("  bindingInfo: ").append(bindingInfo).append("\n");
    sb.append("  version: ").append(version).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
