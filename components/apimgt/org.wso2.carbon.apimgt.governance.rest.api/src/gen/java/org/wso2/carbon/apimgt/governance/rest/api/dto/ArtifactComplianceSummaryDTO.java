package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

/**
 * Summary of compliance of certain artifact in the organization.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Summary of compliance of certain artifact in the organization.")

public class ArtifactComplianceSummaryDTO   {
  
    private Integer total = null;
    private Integer compliant = null;
    private Integer nonCompliant = null;
    private Integer pending = null;
    private Integer notApplicable = null;

  /**
   * Total number of artifacts.
   **/
  public ArtifactComplianceSummaryDTO total(Integer total) {
    this.total = total;
    return this;
  }

  
  @ApiModelProperty(example = "10", value = "Total number of artifacts.")
  @JsonProperty("total")
  public Integer getTotal() {
    return total;
  }
  public void setTotal(Integer total) {
    this.total = total;
  }

  /**
   * Number of compliant artifacts.
   **/
  public ArtifactComplianceSummaryDTO compliant(Integer compliant) {
    this.compliant = compliant;
    return this;
  }

  
  @ApiModelProperty(example = "6", value = "Number of compliant artifacts.")
  @JsonProperty("compliant")
  public Integer getCompliant() {
    return compliant;
  }
  public void setCompliant(Integer compliant) {
    this.compliant = compliant;
  }

  /**
   * Number of non-compliant artifacts.
   **/
  public ArtifactComplianceSummaryDTO nonCompliant(Integer nonCompliant) {
    this.nonCompliant = nonCompliant;
    return this;
  }

  
  @ApiModelProperty(example = "4", value = "Number of non-compliant artifacts.")
  @JsonProperty("nonCompliant")
  public Integer getNonCompliant() {
    return nonCompliant;
  }
  public void setNonCompliant(Integer nonCompliant) {
    this.nonCompliant = nonCompliant;
  }

  /**
   * Number of artifacts pending for compliance.
   **/
  public ArtifactComplianceSummaryDTO pending(Integer pending) {
    this.pending = pending;
    return this;
  }

  
  @ApiModelProperty(example = "0", value = "Number of artifacts pending for compliance.")
  @JsonProperty("pending")
  public Integer getPending() {
    return pending;
  }
  public void setPending(Integer pending) {
    this.pending = pending;
  }

  /**
   * Number of artifacts not applicable for compliance yet.
   **/
  public ArtifactComplianceSummaryDTO notApplicable(Integer notApplicable) {
    this.notApplicable = notApplicable;
    return this;
  }

  
  @ApiModelProperty(example = "0", value = "Number of artifacts not applicable for compliance yet.")
  @JsonProperty("notApplicable")
  public Integer getNotApplicable() {
    return notApplicable;
  }
  public void setNotApplicable(Integer notApplicable) {
    this.notApplicable = notApplicable;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ArtifactComplianceSummaryDTO artifactComplianceSummary = (ArtifactComplianceSummaryDTO) o;
    return Objects.equals(total, artifactComplianceSummary.total) &&
        Objects.equals(compliant, artifactComplianceSummary.compliant) &&
        Objects.equals(nonCompliant, artifactComplianceSummary.nonCompliant) &&
        Objects.equals(pending, artifactComplianceSummary.pending) &&
        Objects.equals(notApplicable, artifactComplianceSummary.notApplicable);
  }

  @Override
  public int hashCode() {
    return Objects.hash(total, compliant, nonCompliant, pending, notApplicable);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ArtifactComplianceSummaryDTO {\n");
    
    sb.append("    total: ").append(toIndentedString(total)).append("\n");
    sb.append("    compliant: ").append(toIndentedString(compliant)).append("\n");
    sb.append("    nonCompliant: ").append(toIndentedString(nonCompliant)).append("\n");
    sb.append("    pending: ").append(toIndentedString(pending)).append("\n");
    sb.append("    notApplicable: ").append(toIndentedString(notApplicable)).append("\n");
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

