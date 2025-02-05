package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactInfoDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyAdherenceSummaryDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.SeverityBasedRuleViolationCountDTO;
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
  
    private String id = null;

          @XmlType(name="StatusEnum")
    @XmlEnum(String.class)
    public enum StatusEnum {
        COMPLIANT("COMPLIANT"),
        NON_COMPLIANT("NON-COMPLIANT"),
        NOT_APPLICABLE("NOT-APPLICABLE"),
        PENDING("PENDING");
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
    private ArtifactInfoDTO info = null;
    private PolicyAdherenceSummaryDTO policyAdherenceSummary = null;
    private List<SeverityBasedRuleViolationCountDTO> severityBasedRuleViolationSummary = new ArrayList<SeverityBasedRuleViolationCountDTO>();

  /**
   * UUID of the API.
   **/
  public ArtifactComplianceStatusDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "123e4567-e89b-12d3-a456-426614174000", value = "UUID of the API.")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Status of the API&#39;s governance compliance.
   **/
  public ArtifactComplianceStatusDTO status(StatusEnum status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(example = "COMPLIANT", value = "Status of the API's governance compliance.")
  @JsonProperty("status")
  public StatusEnum getStatus() {
    return status;
  }
  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  /**
   **/
  public ArtifactComplianceStatusDTO info(ArtifactInfoDTO info) {
    this.info = info;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("info")
  public ArtifactInfoDTO getInfo() {
    return info;
  }
  public void setInfo(ArtifactInfoDTO info) {
    this.info = info;
  }

  /**
   **/
  public ArtifactComplianceStatusDTO policyAdherenceSummary(PolicyAdherenceSummaryDTO policyAdherenceSummary) {
    this.policyAdherenceSummary = policyAdherenceSummary;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("policyAdherenceSummary")
  public PolicyAdherenceSummaryDTO getPolicyAdherenceSummary() {
    return policyAdherenceSummary;
  }
  public void setPolicyAdherenceSummary(PolicyAdherenceSummaryDTO policyAdherenceSummary) {
    this.policyAdherenceSummary = policyAdherenceSummary;
  }

  /**
   * Summary of severity based rule violations.
   **/
  public ArtifactComplianceStatusDTO severityBasedRuleViolationSummary(List<SeverityBasedRuleViolationCountDTO> severityBasedRuleViolationSummary) {
    this.severityBasedRuleViolationSummary = severityBasedRuleViolationSummary;
    return this;
  }

  
  @ApiModelProperty(value = "Summary of severity based rule violations.")
      @Valid
  @JsonProperty("severityBasedRuleViolationSummary")
  public List<SeverityBasedRuleViolationCountDTO> getSeverityBasedRuleViolationSummary() {
    return severityBasedRuleViolationSummary;
  }
  public void setSeverityBasedRuleViolationSummary(List<SeverityBasedRuleViolationCountDTO> severityBasedRuleViolationSummary) {
    this.severityBasedRuleViolationSummary = severityBasedRuleViolationSummary;
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
    return Objects.equals(id, artifactComplianceStatus.id) &&
        Objects.equals(status, artifactComplianceStatus.status) &&
        Objects.equals(info, artifactComplianceStatus.info) &&
        Objects.equals(policyAdherenceSummary, artifactComplianceStatus.policyAdherenceSummary) &&
        Objects.equals(severityBasedRuleViolationSummary, artifactComplianceStatus.severityBasedRuleViolationSummary);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, status, info, policyAdherenceSummary, severityBasedRuleViolationSummary);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ArtifactComplianceStatusDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    info: ").append(toIndentedString(info)).append("\n");
    sb.append("    policyAdherenceSummary: ").append(toIndentedString(policyAdherenceSummary)).append("\n");
    sb.append("    severityBasedRuleViolationSummary: ").append(toIndentedString(severityBasedRuleViolationSummary)).append("\n");
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

