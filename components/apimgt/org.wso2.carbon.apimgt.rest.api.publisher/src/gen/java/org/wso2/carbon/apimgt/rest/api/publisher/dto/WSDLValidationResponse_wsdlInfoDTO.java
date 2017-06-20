package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.WSDLValidationResponse_wsdlInfo_endpointsDTO;
import java.util.Objects;

/**
 * Summary of the WSDL including the basic information
 */
@ApiModel(description = "Summary of the WSDL including the basic information")
public class WSDLValidationResponse_wsdlInfoDTO   {
  @JsonProperty("version")
  private String version = null;

  @JsonProperty("endpoints")
  private List<WSDLValidationResponse_wsdlInfo_endpointsDTO> endpoints = new ArrayList<WSDLValidationResponse_wsdlInfo_endpointsDTO>();

  public WSDLValidationResponse_wsdlInfoDTO version(String version) {
    this.version = version;
    return this;
  }

   /**
   * WSDL version 
   * @return version
  **/
  @ApiModelProperty(example = "1.1", value = "WSDL version ")
  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public WSDLValidationResponse_wsdlInfoDTO endpoints(List<WSDLValidationResponse_wsdlInfo_endpointsDTO> endpoints) {
    this.endpoints = endpoints;
    return this;
  }

  public WSDLValidationResponse_wsdlInfoDTO addEndpointsItem(WSDLValidationResponse_wsdlInfo_endpointsDTO endpointsItem) {
    this.endpoints.add(endpointsItem);
    return this;
  }

   /**
   * A list of endpoints the service exposes 
   * @return endpoints
  **/
  @ApiModelProperty(value = "A list of endpoints the service exposes ")
  public List<WSDLValidationResponse_wsdlInfo_endpointsDTO> getEndpoints() {
    return endpoints;
  }

  public void setEndpoints(List<WSDLValidationResponse_wsdlInfo_endpointsDTO> endpoints) {
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
    WSDLValidationResponse_wsdlInfoDTO wsDLValidationResponseWsdlInfo = (WSDLValidationResponse_wsdlInfoDTO) o;
    return Objects.equals(this.version, wsDLValidationResponseWsdlInfo.version) &&
        Objects.equals(this.endpoints, wsDLValidationResponseWsdlInfo.endpoints);
  }

  @Override
  public int hashCode() {
    return Objects.hash(version, endpoints);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WSDLValidationResponse_wsdlInfoDTO {\n");
    
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

