package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.annotations.ApiModel;
import javax.validation.constraints.*;

/**
 * Throttling Conditions
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;

@ApiModel(description = "Throttling Conditions")

public class ThrottleConditionDTO   {
  

@XmlType(name="TypeEnum")
@XmlEnum(String.class)
public enum TypeEnum {

    @XmlEnumValue("HeaderCondition") HEADERCONDITION(String.valueOf("HeaderCondition")), @XmlEnumValue("IPCondition") IPCONDITION(String.valueOf("IPCondition")), @XmlEnumValue("JWTClaimsCondition") JWTCLAIMSCONDITION(String.valueOf("JWTClaimsCondition")), @XmlEnumValue("QueryParameterCondition") QUERYPARAMETERCONDITION(String.valueOf("QueryParameterCondition"));


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
    private Boolean invertCondition = false;

  /**
   * Type of the thottling condition. Allowed values are HeaderCondition, IPCondition, JWTClaimsCondition, QueryParameterCondition 
   **/
  public ThrottleConditionDTO type(TypeEnum type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Type of the thottling condition. Allowed values are HeaderCondition, IPCondition, JWTClaimsCondition, QueryParameterCondition ")
  @JsonProperty("type")
  @NotNull
  public TypeEnum getType() {
    return type;
  }
  public void setType(TypeEnum type) {
    this.type = type;
  }

  /**
   * Specifies whether inversion of the condition to be matched against the request.  **Note:** When you add conditional groups for advanced throttling policies, this paramater should have the same value (&#x60;true&#x60; or &#x60;false&#x60;) for the same type of conditional group. 
   **/
  public ThrottleConditionDTO invertCondition(Boolean invertCondition) {
    this.invertCondition = invertCondition;
    return this;
  }

  
  @ApiModelProperty(value = "Specifies whether inversion of the condition to be matched against the request.  **Note:** When you add conditional groups for advanced throttling policies, this paramater should have the same value (`true` or `false`) for the same type of conditional group. ")
  @JsonProperty("invertCondition")
  public Boolean isInvertCondition() {
    return invertCondition;
  }
  public void setInvertCondition(Boolean invertCondition) {
    this.invertCondition = invertCondition;
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
        Objects.equals(invertCondition, throttleCondition.invertCondition);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, invertCondition);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ThrottleConditionDTO {\n");
    
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    invertCondition: ").append(toIndentedString(invertCondition)).append("\n");
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

