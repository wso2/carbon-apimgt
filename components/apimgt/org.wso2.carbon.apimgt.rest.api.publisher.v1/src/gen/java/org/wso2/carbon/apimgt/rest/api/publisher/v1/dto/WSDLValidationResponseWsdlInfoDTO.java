package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.WSDLValidationResponseWsdlInfoEndpointsDTO;
import javax.validation.constraints.*;

/**
 * Summary of the WSDL including the basic information
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Summary of the WSDL including the basic information")

public class WSDLValidationResponseWsdlInfoDTO   {
  
    private String version = null;
    private List<WSDLValidationResponseWsdlInfoEndpointsDTO> endpoints = new ArrayList<WSDLValidationResponseWsdlInfoEndpointsDTO>();

  /**
   * WSDL version 
   **/
  public WSDLValidationResponseWsdlInfoDTO version(String version) {
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
  public WSDLValidationResponseWsdlInfoDTO endpoints(List<WSDLValidationResponseWsdlInfoEndpointsDTO> endpoints) {
    this.endpoints = endpoints;
    return this;
  }

  
  @ApiModelProperty(value = "A list of endpoints the service exposes ")
      @Valid
  @JsonProperty("endpoints")
  public List<WSDLValidationResponseWsdlInfoEndpointsDTO> getEndpoints() {
    return endpoints;
  }
  public void setEndpoints(List<WSDLValidationResponseWsdlInfoEndpointsDTO> endpoints) {
    this.endpoints = endpoints;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WSDLValidationResponseWsdlInfoDTO wsDLValidationResponseWsdlInfo = (WSDLValidationResponseWsdlInfoDTO) o;
    return Objects.equals(version, wsDLValidationResponseWsdlInfo.version) &&
        Objects.equals(endpoints, wsDLValidationResponseWsdlInfo.endpoints);
  }

  @Override
  public int hashCode() {
    return Objects.hash(version, endpoints);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WSDLValidationResponseWsdlInfoDTO {\n");
    
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    endpoints: ").append(toIndentedString(endpoints)).append("\n");
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

