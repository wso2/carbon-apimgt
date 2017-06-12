package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDefinitionValidationResponse_wsdlInfo_bindingInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDefinitionValidationResponse_wsdlInfo_endpointsDTO;
import java.util.Objects;

/**
 * Summary of the WSDL including the basic information
 */
@ApiModel(description = "Summary of the WSDL including the basic information")
public class APIDefinitionValidationResponse_wsdlInfoDTO   {
  @JsonProperty("version")
  private String version = null;

  @JsonProperty("endpoints")
  private List<APIDefinitionValidationResponse_wsdlInfo_endpointsDTO> endpoints = new ArrayList<APIDefinitionValidationResponse_wsdlInfo_endpointsDTO>();

  @JsonProperty("bindingInfo")
  private APIDefinitionValidationResponse_wsdlInfo_bindingInfoDTO bindingInfo = null;

  public APIDefinitionValidationResponse_wsdlInfoDTO version(String version) {
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

  public APIDefinitionValidationResponse_wsdlInfoDTO endpoints(List<APIDefinitionValidationResponse_wsdlInfo_endpointsDTO> endpoints) {
    this.endpoints = endpoints;
    return this;
  }

  public APIDefinitionValidationResponse_wsdlInfoDTO addEndpointsItem(APIDefinitionValidationResponse_wsdlInfo_endpointsDTO endpointsItem) {
    this.endpoints.add(endpointsItem);
    return this;
  }

   /**
   * A list of endpoints the service exposes 
   * @return endpoints
  **/
  @ApiModelProperty(value = "A list of endpoints the service exposes ")
  public List<APIDefinitionValidationResponse_wsdlInfo_endpointsDTO> getEndpoints() {
    return endpoints;
  }

  public void setEndpoints(List<APIDefinitionValidationResponse_wsdlInfo_endpointsDTO> endpoints) {
    this.endpoints = endpoints;
  }

  public APIDefinitionValidationResponse_wsdlInfoDTO bindingInfo(APIDefinitionValidationResponse_wsdlInfo_bindingInfoDTO bindingInfo) {
    this.bindingInfo = bindingInfo;
    return this;
  }

   /**
   * Get bindingInfo
   * @return bindingInfo
  **/
  @ApiModelProperty(value = "")
  public APIDefinitionValidationResponse_wsdlInfo_bindingInfoDTO getBindingInfo() {
    return bindingInfo;
  }

  public void setBindingInfo(APIDefinitionValidationResponse_wsdlInfo_bindingInfoDTO bindingInfo) {
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
    APIDefinitionValidationResponse_wsdlInfoDTO apIDefinitionValidationResponseWsdlInfo = (APIDefinitionValidationResponse_wsdlInfoDTO) o;
    return Objects.equals(this.version, apIDefinitionValidationResponseWsdlInfo.version) &&
        Objects.equals(this.endpoints, apIDefinitionValidationResponseWsdlInfo.endpoints) &&
        Objects.equals(this.bindingInfo, apIDefinitionValidationResponseWsdlInfo.bindingInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(version, endpoints, bindingInfo);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIDefinitionValidationResponse_wsdlInfoDTO {\n");
    
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

