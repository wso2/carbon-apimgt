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



public class ArtifactGovernanceResultsSummaryResultsDTO   {
  

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
    private Integer totalArtifacts = null;
    private Integer compliantArtifacts = null;
    private Integer nonCompliantArtifacts = null;

  /**
   * Type of the artifact.
   **/
  public ArtifactGovernanceResultsSummaryResultsDTO artifactType(ArtifactTypeEnum artifactType) {
    this.artifactType = artifactType;
    return this;
  }

  
  @ApiModelProperty(value = "Type of the artifact.")
  @JsonProperty("artifactType")
  public ArtifactTypeEnum getArtifactType() {
    return artifactType;
  }
  public void setArtifactType(ArtifactTypeEnum artifactType) {
    this.artifactType = artifactType;
  }

  /**
   * Total number of artifacts.
   **/
  public ArtifactGovernanceResultsSummaryResultsDTO totalArtifacts(Integer totalArtifacts) {
    this.totalArtifacts = totalArtifacts;
    return this;
  }

  
  @ApiModelProperty(example = "10", value = "Total number of artifacts.")
  @JsonProperty("totalArtifacts")
  public Integer getTotalArtifacts() {
    return totalArtifacts;
  }
  public void setTotalArtifacts(Integer totalArtifacts) {
    this.totalArtifacts = totalArtifacts;
  }

  /**
   * Number of compliant artifacts.
   **/
  public ArtifactGovernanceResultsSummaryResultsDTO compliantArtifacts(Integer compliantArtifacts) {
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
  public ArtifactGovernanceResultsSummaryResultsDTO nonCompliantArtifacts(Integer nonCompliantArtifacts) {
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
    ArtifactGovernanceResultsSummaryResultsDTO artifactGovernanceResultsSummaryResults = (ArtifactGovernanceResultsSummaryResultsDTO) o;
    return Objects.equals(artifactType, artifactGovernanceResultsSummaryResults.artifactType) &&
        Objects.equals(totalArtifacts, artifactGovernanceResultsSummaryResults.totalArtifacts) &&
        Objects.equals(compliantArtifacts, artifactGovernanceResultsSummaryResults.compliantArtifacts) &&
        Objects.equals(nonCompliantArtifacts, artifactGovernanceResultsSummaryResults.nonCompliantArtifacts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(artifactType, totalArtifacts, compliantArtifacts, nonCompliantArtifacts);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ArtifactGovernanceResultsSummaryResultsDTO {\n");
    
    sb.append("    artifactType: ").append(toIndentedString(artifactType)).append("\n");
    sb.append("    totalArtifacts: ").append(toIndentedString(totalArtifacts)).append("\n");
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

