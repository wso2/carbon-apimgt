package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

/**
 * Summary of compliance of artifacts evaluated against a specific policy.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Summary of compliance of artifacts evaluated against a specific policy.")

public class ArtifactComplianceSummaryForPolicyDTO   {
  
    private Integer compliantArtifacts = null;
    private Integer nonCompliantArtifacts = null;

  /**
   * Number of compliant artifacts.
   **/
  public ArtifactComplianceSummaryForPolicyDTO compliantArtifacts(Integer compliantArtifacts) {
    this.compliantArtifacts = compliantArtifacts;
    return this;
  }

  
  @ApiModelProperty(example = "6", value = "Number of compliant artifacts.")
  @JsonProperty("compliantArtifacts")
  public Integer getCompliantArtifacts() {
    return compliantArtifacts;
  }
  public void setCompliantArtifacts(Integer compliantArtifacts) {
    this.compliantArtifacts = compliantArtifacts;
  }

  /**
   * Number of non-compliant artifacts.
   **/
  public ArtifactComplianceSummaryForPolicyDTO nonCompliantArtifacts(Integer nonCompliantArtifacts) {
    this.nonCompliantArtifacts = nonCompliantArtifacts;
    return this;
  }

  
  @ApiModelProperty(example = "4", value = "Number of non-compliant artifacts.")
  @JsonProperty("nonCompliantArtifacts")
  public Integer getNonCompliantArtifacts() {
    return nonCompliantArtifacts;
  }
  public void setNonCompliantArtifacts(Integer nonCompliantArtifacts) {
    this.nonCompliantArtifacts = nonCompliantArtifacts;
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
    return Objects.equals(compliantArtifacts, artifactComplianceSummaryForPolicy.compliantArtifacts) &&
        Objects.equals(nonCompliantArtifacts, artifactComplianceSummaryForPolicy.nonCompliantArtifacts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(compliantArtifacts, nonCompliantArtifacts);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ArtifactComplianceSummaryForPolicyDTO {\n");
    
    sb.append("    compliantArtifacts: ").append(toIndentedString(compliantArtifacts)).append("\n");
    sb.append("    nonCompliantArtifacts: ").append(toIndentedString(nonCompliantArtifacts)).append("\n");
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

