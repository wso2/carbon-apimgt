package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;

/**
 * Result of the rule validation.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Result of the rule validation.")

public class RuleValidationResultDTO   {
  
    private String id = null;
    private String name = null;

          @XmlType(name="StatusEnum")
    @XmlEnum(String.class)
    public enum StatusEnum {
        PASSED("PASSED"),
        FAILED("FAILED");
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

          @XmlType(name="SeverityEnum")
    @XmlEnum(String.class)
    public enum SeverityEnum {
        ERROR("ERROR"),
        WARNING("WARNING"),
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
    private List<String> violatedPaths = new ArrayList<String>();
    private String message = null;

  /**
   * UUID of the rule.
   **/
  public RuleValidationResultDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "123e4567-e89b-12d3-a456-426614174000", value = "UUID of the rule.")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Name of the rule.
   **/
  public RuleValidationResultDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "API Name Rule", value = "Name of the rule.")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Status of the rule validation.
   **/
  public RuleValidationResultDTO status(StatusEnum status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(example = "FAILED", value = "Status of the rule validation.")
  @JsonProperty("status")
  public StatusEnum getStatus() {
    return status;
  }
  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  /**
   * Severity level of the rule violation.
   **/
  public RuleValidationResultDTO severity(SeverityEnum severity) {
    this.severity = severity;
    return this;
  }

  
  @ApiModelProperty(example = "WARNING", value = "Severity level of the rule violation.")
  @JsonProperty("severity")
  public SeverityEnum getSeverity() {
    return severity;
  }
  public void setSeverity(SeverityEnum severity) {
    this.severity = severity;
  }

  /**
   * List of paths that violate the rule.
   **/
  public RuleValidationResultDTO violatedPaths(List<String> violatedPaths) {
    this.violatedPaths = violatedPaths;
    return this;
  }

  
  @ApiModelProperty(value = "List of paths that violate the rule.")
  @JsonProperty("violatedPaths")
  public List<String> getViolatedPaths() {
    return violatedPaths;
  }
  public void setViolatedPaths(List<String> violatedPaths) {
    this.violatedPaths = violatedPaths;
  }

  /**
   * Message to be displayed when the rule is violated.
   **/
  public RuleValidationResultDTO message(String message) {
    this.message = message;
    return this;
  }

  
  @ApiModelProperty(example = "API name can not be too long or short", value = "Message to be displayed when the rule is violated.")
  @JsonProperty("message")
  public String getMessage() {
    return message;
  }
  public void setMessage(String message) {
    this.message = message;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RuleValidationResultDTO ruleValidationResult = (RuleValidationResultDTO) o;
    return Objects.equals(id, ruleValidationResult.id) &&
        Objects.equals(name, ruleValidationResult.name) &&
        Objects.equals(status, ruleValidationResult.status) &&
        Objects.equals(severity, ruleValidationResult.severity) &&
        Objects.equals(violatedPaths, ruleValidationResult.violatedPaths) &&
        Objects.equals(message, ruleValidationResult.message);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, status, severity, violatedPaths, message);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RuleValidationResultDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    severity: ").append(toIndentedString(severity)).append("\n");
    sb.append("    violatedPaths: ").append(toIndentedString(violatedPaths)).append("\n");
    sb.append("    message: ").append(toIndentedString(message)).append("\n");
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

