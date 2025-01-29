package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

/**
 * Result of the ruleset validation.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Result of the ruleset validation.")

public class RulesetValidationResultWithoutRulesDTO   {
  
    private String id = null;
    private String name = null;

          @XmlType(name="RuleTypeEnum")
    @XmlEnum(String.class)
    public enum RuleTypeEnum {
        API_METADATA("API_METADATA"),
        API_DEFINITION("API_DEFINITION"),
        API_DOCUMENTATION("API_DOCUMENTATION");
        private String value;

        RuleTypeEnum (String v) {
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
        public static RuleTypeEnum fromValue(String v) {
            for (RuleTypeEnum b : RuleTypeEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    } 
    private RuleTypeEnum ruleType = null;

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
   * UUID of the ruleset.
   **/
  public RulesetValidationResultWithoutRulesDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "123e4567-e89b-12d3-a456-426614174000", value = "UUID of the ruleset.")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Name of the ruleset.
   **/
  public RulesetValidationResultWithoutRulesDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "API Security Ruleset", value = "Name of the ruleset.")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Context or area to which the ruleset applies.
   **/
  public RulesetValidationResultWithoutRulesDTO ruleType(RuleTypeEnum ruleType) {
    this.ruleType = ruleType;
    return this;
  }

  
  @ApiModelProperty(example = "API_DEFINITION", value = "Context or area to which the ruleset applies.")
  @JsonProperty("ruleType")
  public RuleTypeEnum getRuleType() {
    return ruleType;
  }
  public void setRuleType(RuleTypeEnum ruleType) {
    this.ruleType = ruleType;
  }

  /**
   * Status of the ruleset validation.
   **/
  public RulesetValidationResultWithoutRulesDTO status(StatusEnum status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(example = "PASSED", value = "Status of the ruleset validation.")
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
    RulesetValidationResultWithoutRulesDTO rulesetValidationResultWithoutRules = (RulesetValidationResultWithoutRulesDTO) o;
    return Objects.equals(id, rulesetValidationResultWithoutRules.id) &&
        Objects.equals(name, rulesetValidationResultWithoutRules.name) &&
        Objects.equals(ruleType, rulesetValidationResultWithoutRules.ruleType) &&
        Objects.equals(status, rulesetValidationResultWithoutRules.status);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, ruleType, status);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RulesetValidationResultWithoutRulesDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    ruleType: ").append(toIndentedString(ruleType)).append("\n");
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

