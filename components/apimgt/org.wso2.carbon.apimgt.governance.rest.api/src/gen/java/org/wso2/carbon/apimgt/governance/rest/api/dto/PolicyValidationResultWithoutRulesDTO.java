package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

/**
 * Result of the policy validation.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Result of the policy validation.")

public class PolicyValidationResultWithoutRulesDTO   {
  
    private String id = null;
    private String name = null;

          @XmlType(name="PolicyTypeEnum")
    @XmlEnum(String.class)
    public enum PolicyTypeEnum {
        API_METADATA("API_METADATA"),
        API_DEFINITION("API_DEFINITION"),
        API_DOCUMENTATION("API_DOCUMENTATION");
        private String value;

        PolicyTypeEnum (String v) {
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
        public static PolicyTypeEnum fromValue(String v) {
            for (PolicyTypeEnum b : PolicyTypeEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    } 
    private PolicyTypeEnum policyType = null;

          @XmlType(name="StatusEnum")
    @XmlEnum(String.class)
    public enum StatusEnum {
        PASSED("PASSED"),
        FAILED("FAILED"),
        UNAPPLIED("UNAPPLIED");
        private String value;

        StatusEnum (String v) {
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
        public static StatusEnum fromValue(String v) {
            for (StatusEnum b : StatusEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    } 
    private StatusEnum status = null;

  /**
   * UUID of the policy.
   **/
  public PolicyValidationResultWithoutRulesDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "123e4567-e89b-12d3-a456-426614174000", value = "UUID of the policy.")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Name of the policy.
   **/
  public PolicyValidationResultWithoutRulesDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "API Security Policy", value = "Name of the policy.")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Context or area to which the policy applies.
   **/
  public PolicyValidationResultWithoutRulesDTO policyType(PolicyTypeEnum policyType) {
    this.policyType = policyType;
    return this;
  }

  
  @ApiModelProperty(example = "API_DEFINITION", value = "Context or area to which the policy applies.")
  @JsonProperty("policyType")
  public PolicyTypeEnum getPolicyType() {
    return policyType;
  }
  public void setPolicyType(PolicyTypeEnum policyType) {
    this.policyType = policyType;
  }

  /**
   * Status of the policy validation.
   **/
  public PolicyValidationResultWithoutRulesDTO status(StatusEnum status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(example = "PASSED", value = "Status of the policy validation.")
  @JsonProperty("status")
  public StatusEnum getStatus() {
    return status;
  }
  public void setStatus(StatusEnum status) {
    this.status = status;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PolicyValidationResultWithoutRulesDTO policyValidationResultWithoutRules = (PolicyValidationResultWithoutRulesDTO) o;
    return Objects.equals(id, policyValidationResultWithoutRules.id) &&
        Objects.equals(name, policyValidationResultWithoutRules.name) &&
        Objects.equals(policyType, policyValidationResultWithoutRules.policyType) &&
        Objects.equals(status, policyValidationResultWithoutRules.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, policyType, status);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PolicyValidationResultWithoutRulesDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    policyType: ").append(toIndentedString(policyType)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
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

