package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.EndpointConfigDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;



public class EndpointEndpointConfigDTO   {
  

@XmlType(name="EndpointTypeEnum")
@XmlEnum(String.class)
public enum EndpointTypeEnum {

    @XmlEnumValue("SINGLE") SINGLE(String.valueOf("SINGLE")), @XmlEnumValue("LOAD_BALANCED") LOAD_BALANCED(String.valueOf("LOAD_BALANCED")), @XmlEnumValue("FAIL_OVER") FAIL_OVER(String.valueOf("FAIL_OVER"));


    private String value;

    EndpointTypeEnum (String v) {
        value = v;
    }

    public String value() {
        return value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    public static EndpointTypeEnum fromValue(String v) {
        for (EndpointTypeEnum b : EndpointTypeEnum.values()) {
            if (String.valueOf(b.value).equals(v)) {
                return b;
            }
        }
        return null;
    }
}

    private EndpointTypeEnum endpointType = null;
    private List<EndpointConfigDTO> list = new ArrayList<>();

  /**
   **/
  public EndpointEndpointConfigDTO endpointType(EndpointTypeEnum endpointType) {
    this.endpointType = endpointType;
    return this;
  }

  
  @ApiModelProperty(example = "FAIL_OVER", value = "")
  @JsonProperty("endpointType")
  public EndpointTypeEnum getEndpointType() {
    return endpointType;
  }
  public void setEndpointType(EndpointTypeEnum endpointType) {
    this.endpointType = endpointType;
  }

  /**
   **/
  public EndpointEndpointConfigDTO list(List<EndpointConfigDTO> list) {
    this.list = list;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("list")
  public List<EndpointConfigDTO> getList() {
    return list;
  }
  public void setList(List<EndpointConfigDTO> list) {
    this.list = list;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EndpointEndpointConfigDTO endpointEndpointConfig = (EndpointEndpointConfigDTO) o;
    return Objects.equals(endpointType, endpointEndpointConfig.endpointType) &&
        Objects.equals(list, endpointEndpointConfig.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(endpointType, list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EndpointEndpointConfigDTO {\n");
    
    sb.append("    endpointType: ").append(toIndentedString(endpointType)).append("\n");
    sb.append("    list: ").append(toIndentedString(list)).append("\n");
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

