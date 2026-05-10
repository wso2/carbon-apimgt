package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class TemplateDefaultViolationDTO   {
  
    private String ruleName = null;
    private String rulesetId = null;
    private String rulesetName = null;
    private String violatedPath = null;
    private String message = null;

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

  /**
   * Name of the rule that was violated.
   **/
  public TemplateDefaultViolationDTO ruleName(String ruleName) {
    this.ruleName = ruleName;
    return this;
  }

  
  @ApiModelProperty(example = "description-min-length", required = true, value = "Name of the rule that was violated.")
  @JsonProperty("ruleName")
  @NotNull
  public String getRuleName() {
    return ruleName;
  }
  public void setRuleName(String ruleName) {
    this.ruleName = ruleName;
  }

  /**
   * ID of the ruleset containing the violated rule.
   **/
  public TemplateDefaultViolationDTO rulesetId(String rulesetId) {
    this.rulesetId = rulesetId;
    return this;
  }

  
  @ApiModelProperty(value = "ID of the ruleset containing the violated rule.")
  @JsonProperty("rulesetId")
  public String getRulesetId() {
    return rulesetId;
  }
  public void setRulesetId(String rulesetId) {
    this.rulesetId = rulesetId;
  }

  /**
   * Name of the ruleset containing the violated rule.
   **/
  public TemplateDefaultViolationDTO rulesetName(String rulesetName) {
    this.rulesetName = rulesetName;
    return this;
  }

  
  @ApiModelProperty(value = "Name of the ruleset containing the violated rule.")
  @JsonProperty("rulesetName")
  public String getRulesetName() {
    return rulesetName;
  }
  public void setRulesetName(String rulesetName) {
    this.rulesetName = rulesetName;
  }

  /**
   * JSON path of the field whose default value caused the violation.
   **/
  public TemplateDefaultViolationDTO violatedPath(String violatedPath) {
    this.violatedPath = violatedPath;
    return this;
  }

  
  @ApiModelProperty(example = "application.description", value = "JSON path of the field whose default value caused the violation.")
  @JsonProperty("violatedPath")
  public String getViolatedPath() {
    return violatedPath;
  }
  public void setViolatedPath(String violatedPath) {
    this.violatedPath = violatedPath;
  }

  /**
   * Human-readable description of what the rule requires.
   **/
  public TemplateDefaultViolationDTO message(String message) {
    this.message = message;
    return this;
  }

  
  @ApiModelProperty(example = "description must be at least 50 characters", required = true, value = "Human-readable description of what the rule requires.")
  @JsonProperty("message")
  @NotNull
  public String getMessage() {
    return message;
  }
  public void setMessage(String message) {
    this.message = message;
  }

  /**
   * Severity of the violation.
   **/
  public TemplateDefaultViolationDTO severity(SeverityEnum severity) {
    this.severity = severity;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "Severity of the violation.")
  @JsonProperty("severity")
  @NotNull
  public SeverityEnum getSeverity() {
    return severity;
  }
  public void setSeverity(SeverityEnum severity) {
    this.severity = severity;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    TemplateDefaultViolationDTO templateDefaultViolation = (TemplateDefaultViolationDTO) o;
    return Objects.equals(ruleName, templateDefaultViolation.ruleName) &&
        Objects.equals(rulesetId, templateDefaultViolation.rulesetId) &&
        Objects.equals(rulesetName, templateDefaultViolation.rulesetName) &&
        Objects.equals(violatedPath, templateDefaultViolation.violatedPath) &&
        Objects.equals(message, templateDefaultViolation.message) &&
        Objects.equals(severity, templateDefaultViolation.severity);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ruleName, rulesetId, rulesetName, violatedPath, message, severity);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TemplateDefaultViolationDTO {\n");
    
    sb.append("    ruleName: ").append(toIndentedString(ruleName)).append("\n");
    sb.append("    rulesetId: ").append(toIndentedString(rulesetId)).append("\n");
    sb.append("    rulesetName: ").append(toIndentedString(rulesetName)).append("\n");
    sb.append("    violatedPath: ").append(toIndentedString(violatedPath)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
    sb.append("    severity: ").append(toIndentedString(severity)).append("\n");
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

