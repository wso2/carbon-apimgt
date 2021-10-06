package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;

/**
 * API definition information
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "API definition information")

public class AsyncAPISpecificationValidationResponseInfoDTO   {
  
    private String name = null;
    private String version = null;
    private String context = null;
    private String description = null;
    private String asyncAPIVersion = null;
    private String protocol = null;
    private List<String> endpoints = new ArrayList<String>();
    private String gatewayVendor = null;
    private List<String> asyncTransportProtocols = new ArrayList<String>();

  /**
   **/
  public AsyncAPISpecificationValidationResponseInfoDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "Streetlights", value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public AsyncAPISpecificationValidationResponseInfoDTO version(String version) {
    this.version = version;
    return this;
  }

  
  @ApiModelProperty(example = "1.0.0", value = "")
  @JsonProperty("version")
  public String getVersion() {
    return version;
  }
  public void setVersion(String version) {
    this.version = version;
  }

  /**
   **/
  public AsyncAPISpecificationValidationResponseInfoDTO context(String context) {
    this.context = context;
    return this;
  }

  
  @ApiModelProperty(example = "/streetlights", value = "")
  @JsonProperty("context")
  public String getContext() {
    return context;
  }
  public void setContext(String context) {
    this.context = context;
  }

  /**
   **/
  public AsyncAPISpecificationValidationResponseInfoDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "A sample API that uses a streetlights as an example to demonstrate AsyncAPI specifications", value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   **/
  public AsyncAPISpecificationValidationResponseInfoDTO asyncAPIVersion(String asyncAPIVersion) {
    this.asyncAPIVersion = asyncAPIVersion;
    return this;
  }

  
  @ApiModelProperty(example = "2.0", value = "")
  @JsonProperty("asyncAPIVersion")
  public String getAsyncAPIVersion() {
    return asyncAPIVersion;
  }
  public void setAsyncAPIVersion(String asyncAPIVersion) {
    this.asyncAPIVersion = asyncAPIVersion;
  }

  /**
   **/
  public AsyncAPISpecificationValidationResponseInfoDTO protocol(String protocol) {
    this.protocol = protocol;
    return this;
  }

  
  @ApiModelProperty(example = "WEBSUB", value = "")
  @JsonProperty("protocol")
  public String getProtocol() {
    return protocol;
  }
  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  /**
   * contains host/servers specified in the AsyncAPI file/URL
   **/
  public AsyncAPISpecificationValidationResponseInfoDTO endpoints(List<String> endpoints) {
    this.endpoints = endpoints;
    return this;
  }

  
  @ApiModelProperty(value = "contains host/servers specified in the AsyncAPI file/URL")
  @JsonProperty("endpoints")
  public List<String> getEndpoints() {
    return endpoints;
  }
  public void setEndpoints(List<String> endpoints) {
    this.endpoints = endpoints;
  }

  /**
   **/
  public AsyncAPISpecificationValidationResponseInfoDTO gatewayVendor(String gatewayVendor) {
    this.gatewayVendor = gatewayVendor;
    return this;
  }

  
  @ApiModelProperty(example = "wso2", value = "")
  @JsonProperty("gatewayVendor")
  public String getGatewayVendor() {
    return gatewayVendor;
  }
  public void setGatewayVendor(String gatewayVendor) {
    this.gatewayVendor = gatewayVendor;
  }

  /**
   * contains available transports for an async API
   **/
  public AsyncAPISpecificationValidationResponseInfoDTO asyncTransportProtocols(List<String> asyncTransportProtocols) {
    this.asyncTransportProtocols = asyncTransportProtocols;
    return this;
  }

  
  @ApiModelProperty(value = "contains available transports for an async API")
  @JsonProperty("asyncTransportProtocols")
  public List<String> getAsyncTransportProtocols() {
    return asyncTransportProtocols;
  }
  public void setAsyncTransportProtocols(List<String> asyncTransportProtocols) {
    this.asyncTransportProtocols = asyncTransportProtocols;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AsyncAPISpecificationValidationResponseInfoDTO asyncAPISpecificationValidationResponseInfo = (AsyncAPISpecificationValidationResponseInfoDTO) o;
    return Objects.equals(name, asyncAPISpecificationValidationResponseInfo.name) &&
        Objects.equals(version, asyncAPISpecificationValidationResponseInfo.version) &&
        Objects.equals(context, asyncAPISpecificationValidationResponseInfo.context) &&
        Objects.equals(description, asyncAPISpecificationValidationResponseInfo.description) &&
        Objects.equals(asyncAPIVersion, asyncAPISpecificationValidationResponseInfo.asyncAPIVersion) &&
        Objects.equals(protocol, asyncAPISpecificationValidationResponseInfo.protocol) &&
        Objects.equals(endpoints, asyncAPISpecificationValidationResponseInfo.endpoints) &&
        Objects.equals(gatewayVendor, asyncAPISpecificationValidationResponseInfo.gatewayVendor) &&
        Objects.equals(asyncTransportProtocols, asyncAPISpecificationValidationResponseInfo.asyncTransportProtocols);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, version, context, description, asyncAPIVersion, protocol, endpoints, gatewayVendor, asyncTransportProtocols);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AsyncAPISpecificationValidationResponseInfoDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    version: ").append(toIndentedString(version)).append("\n");
    sb.append("    context: ").append(toIndentedString(context)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    asyncAPIVersion: ").append(toIndentedString(asyncAPIVersion)).append("\n");
    sb.append("    protocol: ").append(toIndentedString(protocol)).append("\n");
    sb.append("    endpoints: ").append(toIndentedString(endpoints)).append("\n");
    sb.append("    gatewayVendor: ").append(toIndentedString(gatewayVendor)).append("\n");
    sb.append("    asyncTransportProtocols: ").append(toIndentedString(asyncTransportProtocols)).append("\n");
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

