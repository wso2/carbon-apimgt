package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDefinitionValidationResponse_wsdlInfoDTO;
import java.util.Objects;

/**
 * APIDefinitionValidationResponseDTO
 */
public class APIDefinitionValidationResponseDTO   {
  @JsonProperty("isValid")
  private Boolean isValid = null;

  /**
   * This attribute declares whether this definition is a swagger or WSDL 
   */
  public enum DefinitionTypeEnum {
    SWAGGER("SWAGGER"),
    
    WSDL("WSDL");

    private String value;

    DefinitionTypeEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static DefinitionTypeEnum fromValue(String text) {
      for (DefinitionTypeEnum b : DefinitionTypeEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("definitionType")
  private DefinitionTypeEnum definitionType = null;

  @JsonProperty("wsdlInfo")
  private APIDefinitionValidationResponse_wsdlInfoDTO wsdlInfo = null;

  public APIDefinitionValidationResponseDTO isValid(Boolean isValid) {
    this.isValid = isValid;
    return this;
  }

   /**
   * This attribute declares whether this definition is valid or not. 
   * @return isValid
  **/
  @ApiModelProperty(example = "true", required = true, value = "This attribute declares whether this definition is valid or not. ")
  public Boolean getIsValid() {
    return isValid;
  }

  public void setIsValid(Boolean isValid) {
    this.isValid = isValid;
  }

  public APIDefinitionValidationResponseDTO definitionType(DefinitionTypeEnum definitionType) {
    this.definitionType = definitionType;
    return this;
  }

   /**
   * This attribute declares whether this definition is a swagger or WSDL 
   * @return definitionType
  **/
  @ApiModelProperty(example = "WSDL", value = "This attribute declares whether this definition is a swagger or WSDL ")
  public DefinitionTypeEnum getDefinitionType() {
    return definitionType;
  }

  public void setDefinitionType(DefinitionTypeEnum definitionType) {
    this.definitionType = definitionType;
  }

  public APIDefinitionValidationResponseDTO wsdlInfo(APIDefinitionValidationResponse_wsdlInfoDTO wsdlInfo) {
    this.wsdlInfo = wsdlInfo;
    return this;
  }

   /**
   * Get wsdlInfo
   * @return wsdlInfo
  **/
  @ApiModelProperty(value = "")
  public APIDefinitionValidationResponse_wsdlInfoDTO getWsdlInfo() {
    return wsdlInfo;
  }

  public void setWsdlInfo(APIDefinitionValidationResponse_wsdlInfoDTO wsdlInfo) {
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
    return Objects.equals(this.isValid, apIDefinitionValidationResponse.isValid) &&
        Objects.equals(this.definitionType, apIDefinitionValidationResponse.definitionType) &&
        Objects.equals(this.wsdlInfo, apIDefinitionValidationResponse.wsdlInfo);
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

