package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDefinitionValidationResponseWsdlInfoBindingInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDefinitionValidationResponseWsdlInfoEndpointsDTO;
import javax.validation.constraints.*;

/**
 * Summary of the WSDL including the basic information
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;

@ApiModel(description = "Summary of the WSDL including the basic information")

public class APIDefinitionValidationResponseWsdlInfoDTO   {
  
    private String version = null;
    private List<APIDefinitionValidationResponseWsdlInfoEndpointsDTO> endpoints = new ArrayList<>();
    private APIDefinitionValidationResponseWsdlInfoBindingInfoDTO bindingInfo = null;

  /**
   * WSDL version 
   **/
  public APIDefinitionValidationResponseWsdlInfoDTO version(String version) {
    this.version = version;
    return this;
  }

  
  @ApiModelProperty(example = "1.1", value = "WSDL version ")
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   * A list of endpoints the service exposes 
   **/
  public APIDefinitionValidationResponseWsdlInfoDTO endpoints(List<APIDefinitionValidationResponseWsdlInfoEndpointsDTO> endpoints) {
    this.endpoints = endpoints;
    return this;
  }

  
  @ApiModelProperty(value = "A list of endpoints the service exposes ")
  @JsonProperty("endpoints")
  public List<APIDefinitionValidationResponseWsdlInfoEndpointsDTO> getEndpoints() {
    return endpoints;
  }
  public void setEndpoints(List<APIDefinitionValidationResponseWsdlInfoEndpointsDTO> endpoints) {
    this.endpoints = endpoints;
  }

  /**
   **/
  public APIDefinitionValidationResponseWsdlInfoDTO bindingInfo(APIDefinitionValidationResponseWsdlInfoBindingInfoDTO bindingInfo) {
    this.bindingInfo = bindingInfo;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("bindingInfo")
  public APIDefinitionValidationResponseWsdlInfoBindingInfoDTO getBindingInfo() {
    return bindingInfo;
  }
  public void setBindingInfo(APIDefinitionValidationResponseWsdlInfoBindingInfoDTO bindingInfo) {
    this.bindingInfo = bindingInfo;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIDefinitionValidationResponseWsdlInfoDTO apIDefinitionValidationResponseWsdlInfo = (APIDefinitionValidationResponseWsdlInfoDTO) o;
    return Objects.equals(version, apIDefinitionValidationResponseWsdlInfo.version) &&
        Objects.equals(endpoints, apIDefinitionValidationResponseWsdlInfo.endpoints) &&
        Objects.equals(bindingInfo, apIDefinitionValidationResponseWsdlInfo.bindingInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(version, endpoints, bindingInfo);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIDefinitionValidationResponseWsdlInfoDTO {\n");
    
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    endpoints: ").append(toIndentedString(endpoints)).append("\n");
    sb.append("    bindingInfo: ").append(toIndentedString(bindingInfo)).append("\n");
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

