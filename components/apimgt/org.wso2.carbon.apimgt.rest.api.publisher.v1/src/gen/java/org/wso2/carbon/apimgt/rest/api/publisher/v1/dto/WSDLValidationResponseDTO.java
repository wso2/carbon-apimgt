package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorListItemDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.WSDLValidationResponseWsdlInfoDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class WSDLValidationResponseDTO   {
  
    private Boolean isValid = null;
    private List<ErrorListItemDTO> errors = new ArrayList<ErrorListItemDTO>();
    private WSDLValidationResponseWsdlInfoDTO wsdlInfo = null;

  /**
   * This attribute declares whether this definition is valid or not. 
   **/
  public WSDLValidationResponseDTO isValid(Boolean isValid) {
    this.isValid = isValid;
    return this;
  }

  
  @ApiModelProperty(example = "true", required = true, value = "This attribute declares whether this definition is valid or not. ")
  @JsonProperty("isValid")
  @NotNull
  public Boolean isIsValid() {
    return isValid;
  }
  public void setIsValid(Boolean isValid) {
    this.isValid = isValid;
  }

  /**
   * If there are more than one error list them out. For example, list out validation errors by each field. 
   **/
  public WSDLValidationResponseDTO errors(List<ErrorListItemDTO> errors) {
    this.errors = errors;
    return this;
  }

  
  @ApiModelProperty(value = "If there are more than one error list them out. For example, list out validation errors by each field. ")
      @Valid
  @JsonProperty("errors")
  public List<ErrorListItemDTO> getErrors() {
    return errors;
  }
  public void setErrors(List<ErrorListItemDTO> errors) {
    this.errors = errors;
  }

  /**
   **/
  public WSDLValidationResponseDTO wsdlInfo(WSDLValidationResponseWsdlInfoDTO wsdlInfo) {
    this.wsdlInfo = wsdlInfo;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("wsdlInfo")
  public WSDLValidationResponseWsdlInfoDTO getWsdlInfo() {
    return wsdlInfo;
  }
  public void setWsdlInfo(WSDLValidationResponseWsdlInfoDTO wsdlInfo) {
    this.wsdlInfo = wsdlInfo;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WSDLValidationResponseDTO wsDLValidationResponse = (WSDLValidationResponseDTO) o;
    return Objects.equals(isValid, wsDLValidationResponse.isValid) &&
        Objects.equals(errors, wsDLValidationResponse.errors) &&
        Objects.equals(wsdlInfo, wsDLValidationResponse.wsdlInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isValid, errors, wsdlInfo);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WSDLValidationResponseDTO {\n");
    
    sb.append("    isValid: ").append(toIndentedString(isValid)).append("\n");
    sb.append("    errors: ").append(toIndentedString(errors)).append("\n");
    sb.append("    wsdlInfo: ").append(toIndentedString(wsdlInfo)).append("\n");
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

