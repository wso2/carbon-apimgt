package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.governance.rest.api.dto.ArtifactInfoDTO;
import javax.validation.constraints.*;

/**
 * Compliance status of an artifact for a specific policy attachment.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Compliance status of an artifact for a specific policy attachment.")

public class ArtifactComplianceForPolicyDTO   {
  
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

  /**
   * UUID of the artifact.
   **/
  public ArtifactComplianceForPolicyDTO id(String id) {
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
   * Status of the artifact&#39;s compliance to the policy attachment.
   **/
  public ArtifactComplianceForPolicyDTO status(StatusEnum status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(example = "COMPLIANT", value = "Status of the artifact's compliance to the policy attachment.")
  @JsonProperty("status")
  public StatusEnum getStatus() {
    return status;
  }
  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  /**
   **/
  public ArtifactComplianceForPolicyDTO info(ArtifactInfoDTO info) {
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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ArtifactComplianceForPolicyDTO artifactComplianceForPolicy = (ArtifactComplianceForPolicyDTO) o;
    return Objects.equals(id, artifactComplianceForPolicy.id) &&
        Objects.equals(status, artifactComplianceForPolicy.status) &&
        Objects.equals(info, artifactComplianceForPolicy.info);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, status, info);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ArtifactComplianceForPolicyDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    info: ").append(toIndentedString(info)).append("\n");
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

