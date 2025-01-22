package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.File;
import javax.validation.constraints.*;

/**
 * Request object for governance compliance validation.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Request object for governance compliance validation.")

public class ComplianceEvaluationRequestDTO   {
  
    private String artifactId = null;

          @XmlType(name="ArtifactTypeEnum")
    @XmlEnum(String.class)
    public enum ArtifactTypeEnum {
        REST_API("REST_API"),
        ASYNC_API("ASYNC_API");
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
    private File targetFile = null;

          @XmlType(name="GovernableStateEnum")
    @XmlEnum(String.class)
    public enum GovernableStateEnum {
        API_CREATE("API_CREATE"),
        API_UPDATE("API_UPDATE"),
        API_DEPLOY("API_DEPLOY"),
        API_PUBLISH("API_PUBLISH");
        private String value;

        GovernableStateEnum (String v) {
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
        public static GovernableStateEnum fromValue(String v) {
            for (GovernableStateEnum b : GovernableStateEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    } 
    private GovernableStateEnum governableState = null;

  /**
   * UUID of the artifact.
   **/
  public ComplianceEvaluationRequestDTO artifactId(String artifactId) {
    this.artifactId = artifactId;
    return this;
  }

  
  @ApiModelProperty(example = "123e4567-e89b-12d3-a456-426614174000", required = true, value = "UUID of the artifact.")
  @JsonProperty("artifactId")
  @NotNull
  public String getArtifactId() {
    return artifactId;
  }
  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }

  /**
   * Type of the artifact.
   **/
  public ComplianceEvaluationRequestDTO artifactType(ArtifactTypeEnum artifactType) {
    this.artifactType = artifactType;
    return this;
  }

  
  @ApiModelProperty(example = "REST_API", required = true, value = "Type of the artifact.")
  @JsonProperty("artifactType")
  @NotNull
  public ArtifactTypeEnum getArtifactType() {
    return artifactType;
  }
  public void setArtifactType(ArtifactTypeEnum artifactType) {
    this.artifactType = artifactType;
  }

  /**
   * The file to be evaluated. Required only in blocking scenario.
   **/
  public ComplianceEvaluationRequestDTO targetFile(File targetFile) {
    this.targetFile = targetFile;
    return this;
  }

  
  @ApiModelProperty(value = "The file to be evaluated. Required only in blocking scenario.")
  @JsonProperty("targetFile")
  public File getTargetFile() {
    return targetFile;
  }
  public void setTargetFile(File targetFile) {
    this.targetFile = targetFile;
  }

  /**
   * The state of the artifact at which the evaluation should run
   **/
  public ComplianceEvaluationRequestDTO governableState(GovernableStateEnum governableState) {
    this.governableState = governableState;
    return this;
  }

  
  @ApiModelProperty(example = "API_DEPLOY", required = true, value = "The state of the artifact at which the evaluation should run")
  @JsonProperty("governableState")
  @NotNull
  public GovernableStateEnum getGovernableState() {
    return governableState;
  }
  public void setGovernableState(GovernableStateEnum governableState) {
    this.governableState = governableState;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ComplianceEvaluationRequestDTO complianceEvaluationRequest = (ComplianceEvaluationRequestDTO) o;
    return Objects.equals(artifactId, complianceEvaluationRequest.artifactId) &&
        Objects.equals(artifactType, complianceEvaluationRequest.artifactType) &&
        Objects.equals(targetFile, complianceEvaluationRequest.targetFile) &&
        Objects.equals(governableState, complianceEvaluationRequest.governableState);
  }

  @Override
  public int hashCode() {
    return Objects.hash(artifactId, artifactType, targetFile, governableState);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ComplianceEvaluationRequestDTO {\n");
    
    sb.append("    artifactId: ").append(toIndentedString(artifactId)).append("\n");
    sb.append("    artifactType: ").append(toIndentedString(artifactType)).append("\n");
    sb.append("    targetFile: ").append(toIndentedString(targetFile)).append("\n");
    sb.append("    governableState: ").append(toIndentedString(governableState)).append("\n");
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

