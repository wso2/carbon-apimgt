package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * APIDefinitionValidationResponse_wsdlInfo_endpointsDTO
 */
public class APIDefinitionValidationResponse_wsdlInfo_endpointsDTO   {
  @JsonProperty("name")
  private String name = null;

  @JsonProperty("location")
  private String location = null;

  public APIDefinitionValidationResponse_wsdlInfo_endpointsDTO name(String name) {
    this.name = name;
    return this;
  }

   /**
   * Name of the endpoint
   * @return name
  **/
  @ApiModelProperty(example = "StockQuoteSoap", value = "Name of the endpoint")
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public APIDefinitionValidationResponse_wsdlInfo_endpointsDTO location(String location) {
    this.location = location;
    return this;
  }

   /**
   * Endpoint URL
   * @return location
  **/
  @ApiModelProperty(example = "http://www.webservicex.net/stockquote.asmx", value = "Endpoint URL")
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
    APIDefinitionValidationResponse_wsdlInfo_endpointsDTO apIDefinitionValidationResponseWsdlInfoEndpoints = (APIDefinitionValidationResponse_wsdlInfo_endpointsDTO) o;
    return Objects.equals(this.name, apIDefinitionValidationResponseWsdlInfoEndpoints.name) &&
        Objects.equals(this.location, apIDefinitionValidationResponseWsdlInfoEndpoints.location);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, location);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIDefinitionValidationResponse_wsdlInfo_endpointsDTO {\n");
    
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

