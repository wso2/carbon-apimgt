package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.WSDLValidationResponse_wsdlInfoDTO;
import java.util.Objects;

/**
 * WSDLValidationResponseDTO
 */
public class WSDLValidationResponseDTO   {
  @JsonProperty("isValid")
  private Boolean isValid = null;

  @JsonProperty("wsdlInfo")
  private WSDLValidationResponse_wsdlInfoDTO wsdlInfo = null;

  public WSDLValidationResponseDTO isValid(Boolean isValid) {
    this.isValid = isValid;
    return this;
  }

   /**
   * This attribute declares whether this WSDL is valid or not. 
   * @return isValid
  **/
  @ApiModelProperty(example = "true", required = true, value = "This attribute declares whether this WSDL is valid or not. ")
  public Boolean getIsValid() {
    return isValid;
  }

  public void setIsValid(Boolean isValid) {
    this.isValid = isValid;
  }

  public WSDLValidationResponseDTO wsdlInfo(WSDLValidationResponse_wsdlInfoDTO wsdlInfo) {
    this.wsdlInfo = wsdlInfo;
    return this;
  }

   /**
   * Get wsdlInfo
   * @return wsdlInfo
  **/
  @ApiModelProperty(value = "")
  public WSDLValidationResponse_wsdlInfoDTO getWsdlInfo() {
    return wsdlInfo;
  }

  public void setWsdlInfo(WSDLValidationResponse_wsdlInfoDTO wsdlInfo) {
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
    return Objects.equals(this.isValid, wsDLValidationResponse.isValid) &&
        Objects.equals(this.wsdlInfo, wsDLValidationResponse.wsdlInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isValid, wsdlInfo);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WSDLValidationResponseDTO {\n");
    
    sb.append("    isValid: ").append(toIndentedString(isValid)).append("\n");
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

