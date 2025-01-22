package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactComplianceSummaryResultsDTO;
import javax.validation.constraints.*;

/**
 * Summary of compliance of artifacts in the organization.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Summary of compliance of artifacts in the organization.")

public class ArtifactComplianceSummaryDTO   {
  
    private List<ArtifactComplianceSummaryResultsDTO> results = new ArrayList<ArtifactComplianceSummaryResultsDTO>();

  /**
   * List of summaries for each artifact type.
   **/
  public ArtifactComplianceSummaryDTO results(List<ArtifactComplianceSummaryResultsDTO> results) {
    this.results = results;
    return this;
  }

  
  @ApiModelProperty(value = "List of summaries for each artifact type.")
      @Valid
  @JsonProperty("results")
  public List<ArtifactComplianceSummaryResultsDTO> getResults() {
    return results;
  }
  public void setResults(List<ArtifactComplianceSummaryResultsDTO> results) {
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
    ArtifactComplianceSummaryDTO artifactComplianceSummary = (ArtifactComplianceSummaryDTO) o;
    return Objects.equals(results, artifactComplianceSummary.results);
  }

  @Override
  public int hashCode() {
    return Objects.hash(results);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ArtifactComplianceSummaryDTO {\n");
    
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

