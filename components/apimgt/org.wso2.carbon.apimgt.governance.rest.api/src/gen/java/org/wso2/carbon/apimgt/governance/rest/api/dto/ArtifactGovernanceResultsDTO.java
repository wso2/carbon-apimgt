package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactGovernanceResultListDTO;
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
  
    private GovernanceSummaryForArtifactsDTO summary = null;
    private ArtifactGovernanceResultListDTO results = null;

  /**
   **/
  public ArtifactGovernanceResultsDTO summary(GovernanceSummaryForArtifactsDTO summary) {
    this.summary = summary;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("summary")
  public GovernanceSummaryForArtifactsDTO getSummary() {
    return summary;
  }
  public void setSummary(GovernanceSummaryForArtifactsDTO summary) {
    this.summary = summary;
  }

  /**
   **/
  public ArtifactGovernanceResultsDTO results(ArtifactGovernanceResultListDTO results) {
    this.results = results;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("results")
  public ArtifactGovernanceResultListDTO getResults() {
    return results;
  }
  public void setResults(ArtifactGovernanceResultListDTO results) {
    this.results = results;
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
    return Objects.equals(summary, artifactGovernanceResults.summary) &&
        Objects.equals(results, artifactGovernanceResults.results);
  }

  @Override
  public int hashCode() {
    return Objects.hash(summary, results);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ArtifactGovernanceResultsDTO {\n");
    
    sb.append("    summary: ").append(toIndentedString(summary)).append("\n");
    sb.append("    results: ").append(toIndentedString(results)).append("\n");
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

