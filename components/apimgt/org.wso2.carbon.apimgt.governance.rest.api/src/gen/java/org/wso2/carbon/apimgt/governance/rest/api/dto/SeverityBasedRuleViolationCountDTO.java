package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

/**
 * List of rules violated by the artifact under each severity.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "List of rules violated by the artifact under each severity.")

public class SeverityBasedRuleViolationCountDTO   {
  

          @XmlType(name="SeverityEnum")
    @XmlEnum(String.class)
    public enum SeverityEnum {
        ERROR("ERROR"),
        WARN("WARN"),
        INFO("INFO");
        private String value;

        SeverityEnum (String v) {
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
        public static SeverityEnum fromValue(String v) {
            for (SeverityEnum b : SeverityEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    } 
    private SeverityEnum severity = null;
    private Integer violatedRulesCount = null;

  /**
   * Severity level of the rule violation.
   **/
  public SeverityBasedRuleViolationCountDTO severity(SeverityEnum severity) {
    this.severity = severity;
    return this;
  }

  
  @ApiModelProperty(example = "WARN", value = "Severity level of the rule violation.")
  @JsonProperty("severity")
  public SeverityEnum getSeverity() {
    return severity;
  }
  public void setSeverity(SeverityEnum severity) {
    this.severity = severity;
  }

  /**
   * Number of rules violated by the artifact under each severity.
   **/
  public SeverityBasedRuleViolationCountDTO violatedRulesCount(Integer violatedRulesCount) {
    this.violatedRulesCount = violatedRulesCount;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "Number of rules violated by the artifact under each severity.")
  @JsonProperty("violatedRulesCount")
  public Integer getViolatedRulesCount() {
    return violatedRulesCount;
  }
  public void setViolatedRulesCount(Integer violatedRulesCount) {
    this.violatedRulesCount = violatedRulesCount;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SeverityBasedRuleViolationCountDTO severityBasedRuleViolationCount = (SeverityBasedRuleViolationCountDTO) o;
    return Objects.equals(severity, severityBasedRuleViolationCount.severity) &&
        Objects.equals(violatedRulesCount, severityBasedRuleViolationCount.violatedRulesCount);
  }

  @Override
  public int hashCode() {
    return Objects.hash(severity, violatedRulesCount);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SeverityBasedRuleViolationCountDTO {\n");
    
    sb.append("    severity: ").append(toIndentedString(severity)).append("\n");
    sb.append("    violatedRulesCount: ").append(toIndentedString(violatedRulesCount)).append("\n");
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

