package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import io.swagger.annotations.ApiModel;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;



/**
 * WSDL binding related information\n
 **/


@ApiModel(description = "WSDL binding related information\n")
public class APIDefinitionValidationResponseWsdlInfoBindingInfoDTO  {
  
  
  
  private Boolean hasSoapBinding = null;
  
  
  private Boolean hasHttpBinding = null;

  
  /**
   * Indicates whether the WSDL contains SOAP Bindings
   **/
  @ApiModelProperty(value = "Indicates whether the WSDL contains SOAP Bindings")
  @JsonProperty("hasSoapBinding")
  public Boolean getHasSoapBinding() {
    return hasSoapBinding;
  }
  public void setHasSoapBinding(Boolean hasSoapBinding) {
    this.hasSoapBinding = hasSoapBinding;
  }

  
  /**
   * Indicates whether the WSDL contains HTTP Bindings
   **/
  @ApiModelProperty(value = "Indicates whether the WSDL contains HTTP Bindings")
  @JsonProperty("hasHttpBinding")
  public Boolean getHasHttpBinding() {
    return hasHttpBinding;
  }
  public void setHasHttpBinding(Boolean hasHttpBinding) {
    this.hasHttpBinding = hasHttpBinding;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIDefinitionValidationResponseWsdlInfoBindingInfoDTO {\n");
    
    sb.append("  hasSoapBinding: ").append(hasSoapBinding).append("\n");
    sb.append("  hasHttpBinding: ").append(hasHttpBinding).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
