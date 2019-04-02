package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDefinitionValidationResponseWsdlInfoDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class APIDefinitionValidationResponseDTO  {
  
  
  @NotNull
  private Boolean isValid = null;
  
  public enum DefinitionTypeEnum {
     SWAGGER,  WSDL, 
  };
  
  private DefinitionTypeEnum definitionType = null;
  
  
  private APIDefinitionValidationResponseWsdlInfoDTO wsdlInfo = null;

  
  /**
   * This attribute declares whether this definition is valid or not.\n
   **/
  @ApiModelProperty(required = true, value = "This attribute declares whether this definition is valid or not.\n")
  @JsonProperty("isValid")
  public Boolean getIsValid() {
    return isValid;
  }
  public void setIsValid(Boolean isValid) {
    this.isValid = isValid;
  }

  
  /**
   * This attribute declares whether this definition is a swagger or WSDL\n
   **/
  @ApiModelProperty(value = "This attribute declares whether this definition is a swagger or WSDL\n")
  @JsonProperty("definitionType")
  public DefinitionTypeEnum getDefinitionType() {
    return definitionType;
  }
  public void setDefinitionType(DefinitionTypeEnum definitionType) {
    this.definitionType = definitionType;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("wsdlInfo")
  public APIDefinitionValidationResponseWsdlInfoDTO getWsdlInfo() {
    return wsdlInfo;
  }
  public void setWsdlInfo(APIDefinitionValidationResponseWsdlInfoDTO wsdlInfo) {
    this.wsdlInfo = wsdlInfo;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIDefinitionValidationResponseDTO {\n");
    
    sb.append("  isValid: ").append(isValid).append("\n");
    sb.append("  definitionType: ").append(definitionType).append("\n");
    sb.append("  wsdlInfo: ").append(wsdlInfo).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
