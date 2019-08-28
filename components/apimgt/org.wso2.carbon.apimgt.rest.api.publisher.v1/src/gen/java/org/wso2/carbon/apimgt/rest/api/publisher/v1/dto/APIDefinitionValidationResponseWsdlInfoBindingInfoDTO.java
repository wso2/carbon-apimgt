package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import javax.validation.constraints.*;

/**
 * WSDL binding related information 
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;

@ApiModel(description = "WSDL binding related information ")

public class APIDefinitionValidationResponseWsdlInfoBindingInfoDTO   {
  
    private Boolean hasHttpBinding = null;
    private Boolean hasSoapBinding = null;

  /**
   * Indicates whether the WSDL contains HTTP Bindings
   **/
  public APIDefinitionValidationResponseWsdlInfoBindingInfoDTO hasHttpBinding(Boolean hasHttpBinding) {
    this.hasHttpBinding = hasHttpBinding;
    return this;
  }

  
  @ApiModelProperty(value = "Indicates whether the WSDL contains HTTP Bindings")
  @JsonProperty("hasHttpBinding")
  public Boolean isHasHttpBinding() {
    return hasHttpBinding;
  }
  public void setHasHttpBinding(Boolean hasHttpBinding) {
    this.hasHttpBinding = hasHttpBinding;
  }

  /**
   * Indicates whether the WSDL contains SOAP Bindings
   **/
  public APIDefinitionValidationResponseWsdlInfoBindingInfoDTO hasSoapBinding(Boolean hasSoapBinding) {
    this.hasSoapBinding = hasSoapBinding;
    return this;
  }

  
  @ApiModelProperty(value = "Indicates whether the WSDL contains SOAP Bindings")
  @JsonProperty("hasSoapBinding")
  public Boolean isHasSoapBinding() {
    return hasSoapBinding;
  }
  public void setHasSoapBinding(Boolean hasSoapBinding) {
    this.hasSoapBinding = hasSoapBinding;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIDefinitionValidationResponseWsdlInfoBindingInfoDTO apIDefinitionValidationResponseWsdlInfoBindingInfo = (APIDefinitionValidationResponseWsdlInfoBindingInfoDTO) o;
    return Objects.equals(hasHttpBinding, apIDefinitionValidationResponseWsdlInfoBindingInfo.hasHttpBinding) &&
        Objects.equals(hasSoapBinding, apIDefinitionValidationResponseWsdlInfoBindingInfo.hasSoapBinding);
  }

  @Override
  public int hashCode() {
    return Objects.hash(hasHttpBinding, hasSoapBinding);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIDefinitionValidationResponseWsdlInfoBindingInfoDTO {\n");
    
    sb.append("    hasHttpBinding: ").append(toIndentedString(hasHttpBinding)).append("\n");
    sb.append("    hasSoapBinding: ").append(toIndentedString(hasSoapBinding)).append("\n");
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

