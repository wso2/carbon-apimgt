package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.APIDefinitionValidationResponseWsdlInfoDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;



public class APIDefinitionValidationResponseDTO   {
  
    private Boolean isValid = null;

@XmlType(name="DefinitionTypeEnum")
@XmlEnum(String.class)
public enum DefinitionTypeEnum {

    @XmlEnumValue("SWAGGER") SWAGGER(String.valueOf("SWAGGER")), @XmlEnumValue("WSDL") WSDL(String.valueOf("WSDL"));


    private String value;

    DefinitionTypeEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static DefinitionTypeEnum fromValue(String v) {
        for (DefinitionTypeEnum b : DefinitionTypeEnum.values()) {
            if (String.valueOf(b.value).equals(v)) {
                return b;
            }
        }
        return null;
    }
}

    private DefinitionTypeEnum definitionType = null;
    private APIDefinitionValidationResponseWsdlInfoDTO wsdlInfo = null;

  /**
   * This attribute declares whether this definition is valid or not. 
   **/
  public APIDefinitionValidationResponseDTO isValid(Boolean isValid) {
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
   * This attribute declares whether this definition is a swagger or WSDL 
   **/
  public APIDefinitionValidationResponseDTO definitionType(DefinitionTypeEnum definitionType) {
    this.definitionType = definitionType;
    return this;
  }

  
  @ApiModelProperty(example = "WSDL", value = "This attribute declares whether this definition is a swagger or WSDL ")
  @JsonProperty("definitionType")
  public DefinitionTypeEnum getDefinitionType() {
    return definitionType;
  }
  public void setDefinitionType(DefinitionTypeEnum definitionType) {
    this.definitionType = definitionType;
  }

  /**
   **/
  public APIDefinitionValidationResponseDTO wsdlInfo(APIDefinitionValidationResponseWsdlInfoDTO wsdlInfo) {
    this.wsdlInfo = wsdlInfo;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("wsdlInfo")
  public APIDefinitionValidationResponseWsdlInfoDTO getWsdlInfo() {
    return wsdlInfo;
  }
  public void setWsdlInfo(APIDefinitionValidationResponseWsdlInfoDTO wsdlInfo) {
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
    APIDefinitionValidationResponseDTO apIDefinitionValidationResponse = (APIDefinitionValidationResponseDTO) o;
    return Objects.equals(isValid, apIDefinitionValidationResponse.isValid) &&
        Objects.equals(definitionType, apIDefinitionValidationResponse.definitionType) &&
        Objects.equals(wsdlInfo, apIDefinitionValidationResponse.wsdlInfo);
  }

  @Override
  public int hashCode() {
    return Objects.hash(isValid, definitionType, wsdlInfo);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIDefinitionValidationResponseDTO {\n");
    
    sb.append("    isValid: ").append(toIndentedString(isValid)).append("\n");
    sb.append("    definitionType: ").append(toIndentedString(definitionType)).append("\n");
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

