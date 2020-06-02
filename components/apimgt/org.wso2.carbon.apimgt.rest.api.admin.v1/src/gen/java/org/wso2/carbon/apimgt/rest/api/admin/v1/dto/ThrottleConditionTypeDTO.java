package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.HeaderConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.IPConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.JWTClaimsConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.QueryParameterConditionDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class ThrottleConditionTypeDTO   {
  

@XmlType(name="TypeEnum")
@XmlEnum(String.class)
public enum TypeEnum {

    @XmlEnumValue("HEADERCONDITION") HEADERCONDITION(String.valueOf("HEADERCONDITION")), @XmlEnumValue("IPCONDITION") IPCONDITION(String.valueOf("IPCONDITION")), @XmlEnumValue("JWTCLAIMSCONDITION") JWTCLAIMSCONDITION(String.valueOf("JWTCLAIMSCONDITION")), @XmlEnumValue("QUERYPARAMETERCONDITION") QUERYPARAMETERCONDITION(String.valueOf("QUERYPARAMETERCONDITION"));


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
    private HeaderConditionDTO headerCondition = null;
    private IPConditionDTO ipCondition = null;
    private JWTClaimsConditionDTO jwtClaimsCondition = null;
    private QueryParameterConditionDTO queryParameterCondition = null;

  /**
   * Type of the throttling condition. Allowed values are \&quot;HEADERCONDITION\&quot;, \&quot;IPCONDITION\&quot;, \&quot;JWTCLAIMSCONDITION\&quot; and \&quot;QUERYPARAMETERCONDITION\&quot;. 
   **/
  public ThrottleConditionTypeDTO type(TypeEnum type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Type of the throttling condition. Allowed values are \"HEADERCONDITION\", \"IPCONDITION\", \"JWTCLAIMSCONDITION\" and \"QUERYPARAMETERCONDITION\". ")
  @JsonProperty("type")
  @NotNull
  public TypeEnum getType() {
    return type;
  }
  public void setType(TypeEnum type) {
    this.type = type;
  }

  /**
   **/
  public ThrottleConditionTypeDTO headerCondition(HeaderConditionDTO headerCondition) {
    this.headerCondition = headerCondition;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("headerCondition")
  public HeaderConditionDTO getHeaderCondition() {
    return headerCondition;
  }
  public void setHeaderCondition(HeaderConditionDTO headerCondition) {
    this.headerCondition = headerCondition;
  }

  /**
   **/
  public ThrottleConditionTypeDTO ipCondition(IPConditionDTO ipCondition) {
    this.ipCondition = ipCondition;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("ipCondition")
  public IPConditionDTO getIpCondition() {
    return ipCondition;
  }
  public void setIpCondition(IPConditionDTO ipCondition) {
    this.ipCondition = ipCondition;
  }

  /**
   **/
  public ThrottleConditionTypeDTO jwtClaimsCondition(JWTClaimsConditionDTO jwtClaimsCondition) {
    this.jwtClaimsCondition = jwtClaimsCondition;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("jwtClaimsCondition")
  public JWTClaimsConditionDTO getJwtClaimsCondition() {
    return jwtClaimsCondition;
  }
  public void setJwtClaimsCondition(JWTClaimsConditionDTO jwtClaimsCondition) {
    this.jwtClaimsCondition = jwtClaimsCondition;
  }

  /**
   **/
  public ThrottleConditionTypeDTO queryParameterCondition(QueryParameterConditionDTO queryParameterCondition) {
    this.queryParameterCondition = queryParameterCondition;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("queryParameterCondition")
  public QueryParameterConditionDTO getQueryParameterCondition() {
    return queryParameterCondition;
  }
  public void setQueryParameterCondition(QueryParameterConditionDTO queryParameterCondition) {
    this.queryParameterCondition = queryParameterCondition;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ThrottleConditionTypeDTO throttleConditionType = (ThrottleConditionTypeDTO) o;
    return Objects.equals(type, throttleConditionType.type) &&
        Objects.equals(headerCondition, throttleConditionType.headerCondition) &&
        Objects.equals(ipCondition, throttleConditionType.ipCondition) &&
        Objects.equals(jwtClaimsCondition, throttleConditionType.jwtClaimsCondition) &&
        Objects.equals(queryParameterCondition, throttleConditionType.queryParameterCondition);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, headerCondition, ipCondition, jwtClaimsCondition, queryParameterCondition);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ThrottleConditionTypeDTO {\n");
    
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    headerCondition: ").append(toIndentedString(headerCondition)).append("\n");
    sb.append("    ipCondition: ").append(toIndentedString(ipCondition)).append("\n");
    sb.append("    jwtClaimsCondition: ").append(toIndentedString(jwtClaimsCondition)).append("\n");
    sb.append("    queryParameterCondition: ").append(toIndentedString(queryParameterCondition)).append("\n");
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

