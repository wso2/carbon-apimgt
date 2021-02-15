package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.HeaderConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.IPConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.JWTClaimsConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.QueryParameterConditionDTO;
import javax.validation.constraints.*;

/**
 * Conditions used for Throttling
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Conditions used for Throttling")

public class ThrottleConditionDTO   {
  

    @XmlType(name="TypeEnum")
    @XmlEnum(String.class)
    public enum TypeEnum {
        HEADERCONDITION("HEADERCONDITION"),
        IPCONDITION("IPCONDITION"),
        JWTCLAIMSCONDITION("JWTCLAIMSCONDITION"),
        QUERYPARAMETERCONDITION("QUERYPARAMETERCONDITION");
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

        @JsonCreator
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
    private Boolean invertCondition = false;
    private HeaderConditionDTO headerCondition = null;
    private IPConditionDTO ipCondition = null;
    private JWTClaimsConditionDTO jwtClaimsCondition = null;
    private QueryParameterConditionDTO queryParameterCondition = null;

  /**
   * Type of the throttling condition. Allowed values are \&quot;HEADERCONDITION\&quot;, \&quot;IPCONDITION\&quot;, \&quot;JWTCLAIMSCONDITION\&quot; and \&quot;QUERYPARAMETERCONDITION\&quot;. 
   **/
  public ThrottleConditionDTO type(TypeEnum type) {
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
   * Specifies whether inversion of the condition to be matched against the request.  **Note:** When you add conditional groups for advanced throttling policies, this paramater should have the same value (&#39;true&#39; or &#39;false&#39;) for the same type of conditional group. 
   **/
  public ThrottleConditionDTO invertCondition(Boolean invertCondition) {
    this.invertCondition = invertCondition;
    return this;
  }

  
  @ApiModelProperty(value = "Specifies whether inversion of the condition to be matched against the request.  **Note:** When you add conditional groups for advanced throttling policies, this paramater should have the same value ('true' or 'false') for the same type of conditional group. ")
  @JsonProperty("invertCondition")
  public Boolean isInvertCondition() {
    return invertCondition;
  }
  public void setInvertCondition(Boolean invertCondition) {
    this.invertCondition = invertCondition;
  }

  /**
   **/
  public ThrottleConditionDTO headerCondition(HeaderConditionDTO headerCondition) {
    this.headerCondition = headerCondition;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("headerCondition")
  public HeaderConditionDTO getHeaderCondition() {
    return headerCondition;
  }
  public void setHeaderCondition(HeaderConditionDTO headerCondition) {
    this.headerCondition = headerCondition;
  }

  /**
   **/
  public ThrottleConditionDTO ipCondition(IPConditionDTO ipCondition) {
    this.ipCondition = ipCondition;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("ipCondition")
  public IPConditionDTO getIpCondition() {
    return ipCondition;
  }
  public void setIpCondition(IPConditionDTO ipCondition) {
    this.ipCondition = ipCondition;
  }

  /**
   **/
  public ThrottleConditionDTO jwtClaimsCondition(JWTClaimsConditionDTO jwtClaimsCondition) {
    this.jwtClaimsCondition = jwtClaimsCondition;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("jwtClaimsCondition")
  public JWTClaimsConditionDTO getJwtClaimsCondition() {
    return jwtClaimsCondition;
  }
  public void setJwtClaimsCondition(JWTClaimsConditionDTO jwtClaimsCondition) {
    this.jwtClaimsCondition = jwtClaimsCondition;
  }

  /**
   **/
  public ThrottleConditionDTO queryParameterCondition(QueryParameterConditionDTO queryParameterCondition) {
    this.queryParameterCondition = queryParameterCondition;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
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
    ThrottleConditionDTO throttleCondition = (ThrottleConditionDTO) o;
    return Objects.equals(type, throttleCondition.type) &&
        Objects.equals(invertCondition, throttleCondition.invertCondition) &&
        Objects.equals(headerCondition, throttleCondition.headerCondition) &&
        Objects.equals(ipCondition, throttleCondition.ipCondition) &&
        Objects.equals(jwtClaimsCondition, throttleCondition.jwtClaimsCondition) &&
        Objects.equals(queryParameterCondition, throttleCondition.queryParameterCondition);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, invertCondition, headerCondition, ipCondition, jwtClaimsCondition, queryParameterCondition);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ThrottleConditionDTO {\n");
    
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    invertCondition: ").append(toIndentedString(invertCondition)).append("\n");
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

