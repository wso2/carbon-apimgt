package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactGovernanceResultInfoDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.GovernanceSummaryForArtifactsDTO;
import javax.validation.constraints.*;

/**
 * Governance results of all artifacts of an organization.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Governance results of all artifacts of an organization.")

public class ArtifactGovernanceResultsDTO   {
  
    private GovernanceSummaryForArtifactsDTO artifactGovernanceSummary = null;
    private List<ArtifactGovernanceResultInfoDTO> artifactGovernanceResults = new ArrayList<ArtifactGovernanceResultInfoDTO>();

  /**
   **/
  public ArtifactGovernanceResultsDTO artifactGovernanceSummary(GovernanceSummaryForArtifactsDTO artifactGovernanceSummary) {
    this.artifactGovernanceSummary = artifactGovernanceSummary;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("artifactGovernanceSummary")
  public GovernanceSummaryForArtifactsDTO getArtifactGovernanceSummary() {
    return artifactGovernanceSummary;
  }
  public void setArtifactGovernanceSummary(GovernanceSummaryForArtifactsDTO artifactGovernanceSummary) {
    this.artifactGovernanceSummary = artifactGovernanceSummary;
  }

  /**
   * Governance results for a list of artifacts.
   **/
  public ArtifactGovernanceResultsDTO artifactGovernanceResults(List<ArtifactGovernanceResultInfoDTO> artifactGovernanceResults) {
    this.artifactGovernanceResults = artifactGovernanceResults;
    return this;
  }

  
  @ApiModelProperty(value = "Governance results for a list of artifacts.")
      @Valid
  @JsonProperty("artifactGovernanceResults")
  public List<ArtifactGovernanceResultInfoDTO> getArtifactGovernanceResults() {
    return artifactGovernanceResults;
  }
  public void setArtifactGovernanceResults(List<ArtifactGovernanceResultInfoDTO> artifactGovernanceResults) {
    this.artifactGovernanceResults = artifactGovernanceResults;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ArtifactGovernanceResultsDTO artifactGovernanceResults = (ArtifactGovernanceResultsDTO) o;
    return Objects.equals(artifactGovernanceSummary, artifactGovernanceResults.artifactGovernanceSummary) &&
        Objects.equals(artifactGovernanceResults, artifactGovernanceResults.artifactGovernanceResults);
  }

  @Override
  public int hashCode() {
    return Objects.hash(artifactGovernanceSummary, artifactGovernanceResults);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ArtifactGovernanceResultsDTO {\n");
    
    sb.append("    artifactGovernanceSummary: ").append(toIndentedString(artifactGovernanceSummary)).append("\n");
    sb.append("    artifactGovernanceResults: ").append(toIndentedString(artifactGovernanceResults)).append("\n");
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

