package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactCompForPolicyAttachmentDTO;
import javax.validation.constraints.*;

/**
 * Provides adherence details of a policy attachment.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Provides adherence details of a policy attachment.")

public class PolicyAttachmentAdherenceDetailsDTO   {
  
    private String id = null;
    private String name = null;

          @XmlType(name="StatusEnum")
    @XmlEnum(String.class)
    public enum StatusEnum {
        FOLLOWED("FOLLOWED"),
        VIOLATED("VIOLATED"),
        UNAPPLIED("UNAPPLIED");
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
    private List<ArtifactCompForPolicyAttachmentDTO> evaluatedArtifacts = new ArrayList<ArtifactCompForPolicyAttachmentDTO>();

  /**
   * UUID of the policy attachment.
   **/
  public PolicyAttachmentAdherenceDetailsDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "123e4567-e89b-12d3-a456-426614174000", value = "UUID of the policy attachment.")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Name of the policy attachment.
   **/
  public PolicyAttachmentAdherenceDetailsDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "PolicyAttachment1", value = "Name of the policy attachment.")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Status of the policy attachment&#39;s governance compliance.
   **/
  public PolicyAttachmentAdherenceDetailsDTO status(StatusEnum status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(example = "FOLLOWED", value = "Status of the policy attachment's governance compliance.")
  @JsonProperty("status")
  public StatusEnum getStatus() {
    return status;
  }
  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  /**
   * Compliance status of the artifacts evaluated against the policy attachment.
   **/
  public PolicyAttachmentAdherenceDetailsDTO evaluatedArtifacts(List<ArtifactCompForPolicyAttachmentDTO> evaluatedArtifacts) {
    this.evaluatedArtifacts = evaluatedArtifacts;
    return this;
  }

  
  @ApiModelProperty(value = "Compliance status of the artifacts evaluated against the policy attachment.")
      @Valid
  @JsonProperty("evaluatedArtifacts")
  public List<ArtifactCompForPolicyAttachmentDTO> getEvaluatedArtifacts() {
    return evaluatedArtifacts;
  }
  public void setEvaluatedArtifacts(List<ArtifactCompForPolicyAttachmentDTO> evaluatedArtifacts) {
    this.evaluatedArtifacts = evaluatedArtifacts;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PolicyAttachmentAdherenceDetailsDTO policyAttachmentAdherenceDetails = (PolicyAttachmentAdherenceDetailsDTO) o;
    return Objects.equals(id, policyAttachmentAdherenceDetails.id) &&
        Objects.equals(name, policyAttachmentAdherenceDetails.name) &&
        Objects.equals(status, policyAttachmentAdherenceDetails.status) &&
        Objects.equals(evaluatedArtifacts, policyAttachmentAdherenceDetails.evaluatedArtifacts);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, name, status, evaluatedArtifacts);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PolicyAttachmentAdherenceDetailsDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    evaluatedArtifacts: ").append(toIndentedString(evaluatedArtifacts)).append("\n");
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

