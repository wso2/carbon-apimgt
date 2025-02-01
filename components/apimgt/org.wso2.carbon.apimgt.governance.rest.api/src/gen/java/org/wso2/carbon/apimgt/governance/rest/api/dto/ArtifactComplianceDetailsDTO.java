package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactInfoDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyAdherenceWithRulesetsDTO;
import javax.validation.constraints.*;

/**
 * Provides compliance details of an artifact.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Provides compliance details of an artifact.")

public class ArtifactComplianceDetailsDTO   {
  
    private String id = null;

          @XmlType(name="StatusEnum")
    @XmlEnum(String.class)
    public enum StatusEnum {
        COMPLIANT("COMPLIANT"),
        NON_COMPLIANT("NON-COMPLIANT"),
        NOT_APPLICABLE("NOT-APPLICABLE");
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
    private List<PolicyAdherenceWithRulesetsDTO> governedPolicies = new ArrayList<PolicyAdherenceWithRulesetsDTO>();

  /**
   * UUID of the artifact.
   **/
  public ArtifactComplianceDetailsDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "123e4567-e89b-12d3-a456-426614174000", value = "UUID of the artifact.")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Status of the artifact&#39;s governance compliance.
   **/
  public ArtifactComplianceDetailsDTO status(StatusEnum status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(example = "COMPLIANT", value = "Status of the artifact's governance compliance.")
  @JsonProperty("status")
  public StatusEnum getStatus() {
    return status;
  }
  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  /**
   **/
  public ArtifactComplianceDetailsDTO info(ArtifactInfoDTO info) {
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
   * List of policies under which the artifact was governed.
   **/
  public ArtifactComplianceDetailsDTO governedPolicies(List<PolicyAdherenceWithRulesetsDTO> governedPolicies) {
    this.governedPolicies = governedPolicies;
    return this;
  }

  
  @ApiModelProperty(value = "List of policies under which the artifact was governed.")
      @Valid
  @JsonProperty("governedPolicies")
  public List<PolicyAdherenceWithRulesetsDTO> getGovernedPolicies() {
    return governedPolicies;
  }
  public void setGovernedPolicies(List<PolicyAdherenceWithRulesetsDTO> governedPolicies) {
    this.governedPolicies = governedPolicies;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ArtifactComplianceDetailsDTO artifactComplianceDetails = (ArtifactComplianceDetailsDTO) o;
    return Objects.equals(id, artifactComplianceDetails.id) &&
        Objects.equals(status, artifactComplianceDetails.status) &&
        Objects.equals(info, artifactComplianceDetails.info) &&
        Objects.equals(governedPolicies, artifactComplianceDetails.governedPolicies);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, status, info, governedPolicies);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ArtifactComplianceDetailsDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    info: ").append(toIndentedString(info)).append("\n");
    sb.append("    governedPolicies: ").append(toIndentedString(governedPolicies)).append("\n");
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

