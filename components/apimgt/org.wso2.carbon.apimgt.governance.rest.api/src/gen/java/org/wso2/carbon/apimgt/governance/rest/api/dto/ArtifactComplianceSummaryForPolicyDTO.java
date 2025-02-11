package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

/**
 * Summary of compliance of artifacts evaluated against a specific policy attachment.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Summary of compliance of artifacts evaluated against a specific policy attachment.")

public class ArtifactComplianceSummaryForPolicyDTO   {
  
    private Integer compliant = null;
    private Integer nonCompliant = null;

  /**
   * Number of compliant artifacts.
   **/
  public ArtifactComplianceSummaryForPolicyDTO compliant(Integer compliant) {
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
  public ArtifactComplianceSummaryForPolicyDTO nonCompliant(Integer nonCompliant) {
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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ArtifactComplianceSummaryForPolicyDTO artifactComplianceSummaryForPolicy = (ArtifactComplianceSummaryForPolicyDTO) o;
    return Objects.equals(compliant, artifactComplianceSummaryForPolicy.compliant) &&
        Objects.equals(nonCompliant, artifactComplianceSummaryForPolicy.nonCompliant);
  }

  @Override
  public int hashCode() {
    return Objects.hash(compliant, nonCompliant);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ArtifactComplianceSummaryForPolicyDTO {\n");
    
    sb.append("    compliant: ").append(toIndentedString(compliant)).append("\n");
    sb.append("    nonCompliant: ").append(toIndentedString(nonCompliant)).append("\n");
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

