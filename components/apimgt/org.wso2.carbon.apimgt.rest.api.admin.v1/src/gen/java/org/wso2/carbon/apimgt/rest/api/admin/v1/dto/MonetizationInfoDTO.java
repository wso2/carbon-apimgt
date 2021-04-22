package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class MonetizationInfoDTO   {
  

    @XmlType(name="MonetizationPlanEnum")
    @XmlEnum(String.class)
    public enum MonetizationPlanEnum {
        FIXEDRATE("FIXEDRATE"),
        DYNAMICRATE("DYNAMICRATE");
        private String value;

        MonetizationPlanEnum (String v) {
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
        public static MonetizationPlanEnum fromValue(String v) {
            for (MonetizationPlanEnum b : MonetizationPlanEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private MonetizationPlanEnum monetizationPlan = null;
    private Map<String, String> properties = new HashMap<String, String>();

  /**
   * Flag to indicate the monetization plan
   **/
  public MonetizationInfoDTO monetizationPlan(MonetizationPlanEnum monetizationPlan) {
    this.monetizationPlan = monetizationPlan;
    return this;
  }

  
  @ApiModelProperty(example = "FixedRate", value = "Flag to indicate the monetization plan")
  @JsonProperty("monetizationPlan")
  public MonetizationPlanEnum getMonetizationPlan() {
    return monetizationPlan;
  }
  public void setMonetizationPlan(MonetizationPlanEnum monetizationPlan) {
    this.monetizationPlan = monetizationPlan;
  }

  /**
   * Map of custom properties related to each monetization plan
   **/
  public MonetizationInfoDTO properties(Map<String, String> properties) {
    this.properties = properties;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Map of custom properties related to each monetization plan")
  @JsonProperty("properties")
  @NotNull
  public Map<String, String> getProperties() {
    return properties;
  }
  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    MonetizationInfoDTO monetizationInfo = (MonetizationInfoDTO) o;
    return Objects.equals(monetizationPlan, monetizationInfo.monetizationPlan) &&
        Objects.equals(properties, monetizationInfo.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(monetizationPlan, properties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class MonetizationInfoDTO {\n");
    
    sb.append("    monetizationPlan: ").append(toIndentedString(monetizationPlan)).append("\n");
    sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
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

