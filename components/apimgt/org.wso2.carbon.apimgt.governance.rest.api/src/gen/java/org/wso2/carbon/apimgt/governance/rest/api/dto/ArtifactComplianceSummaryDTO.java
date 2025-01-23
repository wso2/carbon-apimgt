package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
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
    private Integer notApplicableArtifacts = null;

  /**
   * Type of the artifact.
   **/
  public ArtifactComplianceSummaryDTO artifactType(ArtifactTypeEnum artifactType) {
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
   * Total number of artifacts.
   **/
  public ArtifactComplianceSummaryDTO totalArtifacts(Integer totalArtifacts) {
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
  public ArtifactComplianceSummaryDTO compliantArtifacts(Integer compliantArtifacts) {
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
  public ArtifactComplianceSummaryDTO nonCompliantArtifacts(Integer nonCompliantArtifacts) {
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

  /**
   * Number of artifacts not applicable for compliance yet.
   **/
  public ArtifactComplianceSummaryDTO notApplicableArtifacts(Integer notApplicableArtifacts) {
    this.notApplicableArtifacts = notApplicableArtifacts;
    return this;
  }

  
  @ApiModelProperty(example = "0", value = "Number of artifacts not applicable for compliance yet.")
  @JsonProperty("notApplicableArtifacts")
  public Integer getNotApplicableArtifacts() {
    return notApplicableArtifacts;
  }
  public void setNotApplicableArtifacts(Integer notApplicableArtifacts) {
    this.notApplicableArtifacts = notApplicableArtifacts;
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
    return Objects.equals(artifactType, artifactComplianceSummary.artifactType) &&
        Objects.equals(totalArtifacts, artifactComplianceSummary.totalArtifacts) &&
        Objects.equals(compliantArtifacts, artifactComplianceSummary.compliantArtifacts) &&
        Objects.equals(nonCompliantArtifacts, artifactComplianceSummary.nonCompliantArtifacts) &&
        Objects.equals(notApplicableArtifacts, artifactComplianceSummary.notApplicableArtifacts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(artifactType, totalArtifacts, compliantArtifacts, nonCompliantArtifacts, notApplicableArtifacts);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ArtifactComplianceSummaryDTO {\n");
    
    sb.append("    artifactType: ").append(toIndentedString(artifactType)).append("\n");
    sb.append("    totalArtifacts: ").append(toIndentedString(totalArtifacts)).append("\n");
    sb.append("    compliantArtifacts: ").append(toIndentedString(compliantArtifacts)).append("\n");
    sb.append("    nonCompliantArtifacts: ").append(toIndentedString(nonCompliantArtifacts)).append("\n");
    sb.append("    notApplicableArtifacts: ").append(toIndentedString(notApplicableArtifacts)).append("\n");
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

