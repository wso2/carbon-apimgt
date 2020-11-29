package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class WSDLValidationResponseWsdlInfoEndpointsDTO   {
  
    private String name = null;
    private String location = null;

  /**
   * Name of the endpoint
   **/
  public WSDLValidationResponseWsdlInfoEndpointsDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "StockQuoteSoap", value = "Name of the endpoint")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Endpoint URL
   **/
  public WSDLValidationResponseWsdlInfoEndpointsDTO location(String location) {
    this.location = location;
    return this;
  }

  
  @ApiModelProperty(example = "http://www.webservicex.net/stockquote.asmx", value = "Endpoint URL")
  @JsonProperty("location")
  public String getLocation() {
    return location;
  }
  public void setLocation(String location) {
    this.location = location;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WSDLValidationResponseWsdlInfoEndpointsDTO wsDLValidationResponseWsdlInfoEndpoints = (WSDLValidationResponseWsdlInfoEndpointsDTO) o;
    return Objects.equals(name, wsDLValidationResponseWsdlInfoEndpoints.name) &&
        Objects.equals(location, wsDLValidationResponseWsdlInfoEndpoints.location);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, location);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WSDLValidationResponseWsdlInfoEndpointsDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    location: ").append(toIndentedString(location)).append("\n");
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

