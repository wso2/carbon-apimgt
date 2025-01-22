package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.governance.rest.api.dto.RuleValidationResultSummaryDTO;
import javax.validation.constraints.*;

/**
 * Provides compliance status of an artifact.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Provides compliance status of an artifact.")

public class ArtifactComplianceStatusDTO   {
  
    private String artifactId = null;

          @XmlType(name="ArtifactTypeEnum")
    @XmlEnum(String.class)
    public enum ArtifactTypeEnum {
        API("API");
        private String value;

        ArtifactTypeEnum (String v) {
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
        public static ArtifactTypeEnum fromValue(String v) {
            for (ArtifactTypeEnum b : ArtifactTypeEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    } 
    private ArtifactTypeEnum artifactType = null;
    private String artifactName = null;

          @XmlType(name="StatusEnum")
    @XmlEnum(String.class)
    public enum StatusEnum {
        COMPLAINT("COMPLAINT"),
        NON_COMPLAINT("NON-COMPLAINT");
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
    private Integer applicablePolicyCount = null;
    private Integer violatedPolicyCount = null;
    private List<RuleValidationResultSummaryDTO> ruleValidationResultSummary = new ArrayList<RuleValidationResultSummaryDTO>();

  /**
   * UUID of the artifact.
   **/
  public ArtifactComplianceStatusDTO artifactId(String artifactId) {
    this.artifactId = artifactId;
    return this;
  }

  
  @ApiModelProperty(example = "123e4567-e89b-12d3-a456-426614174000", value = "UUID of the artifact.")
  @JsonProperty("artifactId")
  public String getArtifactId() {
    return artifactId;
  }
  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }

  /**
   * Type of the artifact.
   **/
  public ArtifactComplianceStatusDTO artifactType(ArtifactTypeEnum artifactType) {
    this.artifactType = artifactType;
    return this;
  }

  
  @ApiModelProperty(example = "API", value = "Type of the artifact.")
  @JsonProperty("artifactType")
  public ArtifactTypeEnum getArtifactType() {
    return artifactType;
  }
  public void setArtifactType(ArtifactTypeEnum artifactType) {
    this.artifactType = artifactType;
  }

  /**
   * Display name of the artifact.
   **/
  public ArtifactComplianceStatusDTO artifactName(String artifactName) {
    this.artifactName = artifactName;
    return this;
  }

  
  @ApiModelProperty(example = "Test API v1", value = "Display name of the artifact.")
  @JsonProperty("artifactName")
  public String getArtifactName() {
    return artifactName;
  }
  public void setArtifactName(String artifactName) {
    this.artifactName = artifactName;
  }

  /**
   * Status of the artifact&#39;s governance compliance.
   **/
  public ArtifactComplianceStatusDTO status(StatusEnum status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(example = "COMPLAINT", value = "Status of the artifact's governance compliance.")
  @JsonProperty("status")
  public StatusEnum getStatus() {
    return status;
  }
  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  /**
   * Number of policies applicable to the artifact.
   **/
  public ArtifactComplianceStatusDTO applicablePolicyCount(Integer applicablePolicyCount) {
    this.applicablePolicyCount = applicablePolicyCount;
    return this;
  }

  
  @ApiModelProperty(example = "2", value = "Number of policies applicable to the artifact.")
  @JsonProperty("applicablePolicyCount")
  public Integer getApplicablePolicyCount() {
    return applicablePolicyCount;
  }
  public void setApplicablePolicyCount(Integer applicablePolicyCount) {
    this.applicablePolicyCount = applicablePolicyCount;
  }

  /**
   * Number of policies violated by the artifact.
   **/
  public ArtifactComplianceStatusDTO violatedPolicyCount(Integer violatedPolicyCount) {
    this.violatedPolicyCount = violatedPolicyCount;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "Number of policies violated by the artifact.")
  @JsonProperty("violatedPolicyCount")
  public Integer getViolatedPolicyCount() {
    return violatedPolicyCount;
  }
  public void setViolatedPolicyCount(Integer violatedPolicyCount) {
    this.violatedPolicyCount = violatedPolicyCount;
  }

  /**
   * Summary of rule validation results.
   **/
  public ArtifactComplianceStatusDTO ruleValidationResultSummary(List<RuleValidationResultSummaryDTO> ruleValidationResultSummary) {
    this.ruleValidationResultSummary = ruleValidationResultSummary;
    return this;
  }

  
  @ApiModelProperty(value = "Summary of rule validation results.")
      @Valid
  @JsonProperty("ruleValidationResultSummary")
  public List<RuleValidationResultSummaryDTO> getRuleValidationResultSummary() {
    return ruleValidationResultSummary;
  }
  public void setRuleValidationResultSummary(List<RuleValidationResultSummaryDTO> ruleValidationResultSummary) {
    this.ruleValidationResultSummary = ruleValidationResultSummary;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ArtifactComplianceStatusDTO artifactComplianceStatus = (ArtifactComplianceStatusDTO) o;
    return Objects.equals(artifactId, artifactComplianceStatus.artifactId) &&
        Objects.equals(artifactType, artifactComplianceStatus.artifactType) &&
        Objects.equals(artifactName, artifactComplianceStatus.artifactName) &&
        Objects.equals(status, artifactComplianceStatus.status) &&
        Objects.equals(applicablePolicyCount, artifactComplianceStatus.applicablePolicyCount) &&
        Objects.equals(violatedPolicyCount, artifactComplianceStatus.violatedPolicyCount) &&
        Objects.equals(ruleValidationResultSummary, artifactComplianceStatus.ruleValidationResultSummary);
  }

  @Override
  public int hashCode() {
    return Objects.hash(artifactId, artifactType, artifactName, status, applicablePolicyCount, violatedPolicyCount, ruleValidationResultSummary);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ArtifactComplianceStatusDTO {\n");
    
    sb.append("    artifactId: ").append(toIndentedString(artifactId)).append("\n");
    sb.append("    artifactType: ").append(toIndentedString(artifactType)).append("\n");
    sb.append("    artifactName: ").append(toIndentedString(artifactName)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    applicablePolicyCount: ").append(toIndentedString(applicablePolicyCount)).append("\n");
    sb.append("    violatedPolicyCount: ").append(toIndentedString(violatedPolicyCount)).append("\n");
    sb.append("    ruleValidationResultSummary: ").append(toIndentedString(ruleValidationResultSummary)).append("\n");
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

