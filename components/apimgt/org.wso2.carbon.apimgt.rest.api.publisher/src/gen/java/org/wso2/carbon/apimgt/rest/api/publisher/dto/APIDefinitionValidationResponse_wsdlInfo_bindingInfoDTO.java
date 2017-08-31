package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * WSDL binding related information 
 */
@ApiModel(description = "WSDL binding related information ")
public class APIDefinitionValidationResponse_wsdlInfo_bindingInfoDTO   {
  @JsonProperty("hasHttpBinding")
  private Boolean hasHttpBinding = null;

  @JsonProperty("hasSoapBinding")
  private Boolean hasSoapBinding = null;

  public APIDefinitionValidationResponse_wsdlInfo_bindingInfoDTO hasHttpBinding(Boolean hasHttpBinding) {
    this.hasHttpBinding = hasHttpBinding;
    return this;
  }

   /**
   * Indicates whether the WSDL contains HTTP Bindings
   * @return hasHttpBinding
  **/
  @ApiModelProperty(value = "Indicates whether the WSDL contains HTTP Bindings")
  public Boolean getHasHttpBinding() {
    return hasHttpBinding;
  }

  public void setHasHttpBinding(Boolean hasHttpBinding) {
    this.hasHttpBinding = hasHttpBinding;
  }

  public APIDefinitionValidationResponse_wsdlInfo_bindingInfoDTO hasSoapBinding(Boolean hasSoapBinding) {
    this.hasSoapBinding = hasSoapBinding;
    return this;
  }

   /**
   * Indicates whether the WSDL contains SOAP Bindings
   * @return hasSoapBinding
  **/
  @ApiModelProperty(value = "Indicates whether the WSDL contains SOAP Bindings")
  public Boolean getHasSoapBinding() {
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
    APIDefinitionValidationResponse_wsdlInfo_bindingInfoDTO apIDefinitionValidationResponseWsdlInfoBindingInfo = (APIDefinitionValidationResponse_wsdlInfo_bindingInfoDTO) o;
    return Objects.equals(this.hasHttpBinding, apIDefinitionValidationResponseWsdlInfoBindingInfo.hasHttpBinding) &&
        Objects.equals(this.hasSoapBinding, apIDefinitionValidationResponseWsdlInfoBindingInfo.hasSoapBinding);
  }

  @Override
  public int hashCode() {
    return Objects.hash(hasHttpBinding, hasSoapBinding);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIDefinitionValidationResponse_wsdlInfo_bindingInfoDTO {\n");
    
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

