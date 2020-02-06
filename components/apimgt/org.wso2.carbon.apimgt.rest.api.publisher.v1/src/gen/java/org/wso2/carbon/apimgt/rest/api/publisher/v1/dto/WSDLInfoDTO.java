package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class WSDLInfoDTO   {
  

@XmlType(name="TypeEnum")
@XmlEnum(String.class)
public enum TypeEnum {

    @XmlEnumValue("WSDL") WSDL(String.valueOf("WSDL")), @XmlEnumValue("ZIP") ZIP(String.valueOf("ZIP"));


    private String value;

    TypeEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static TypeEnum fromValue(String v) {
        for (TypeEnum b : TypeEnum.values()) {
            if (String.valueOf(b.value).equals(v)) {
                return b;
            }
        }
        return null;
    }
}

    private TypeEnum type = null;

  /**
   * Indicates whether the WSDL is a single WSDL or an archive in ZIP format
   **/
  public WSDLInfoDTO type(TypeEnum type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(value = "Indicates whether the WSDL is a single WSDL or an archive in ZIP format")
  @JsonProperty("type")
  public TypeEnum getType() {
    return type;
  }
  public void setType(TypeEnum type) {
    this.type = type;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WSDLInfoDTO wsDLInfo = (WSDLInfoDTO) o;
    return Objects.equals(type, wsDLInfo.type);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WSDLInfoDTO {\n");
    
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
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

