package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

/**
 * Single deployment info for gateway sync (platform contract).
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Single deployment info for gateway sync (platform contract).")

public class GatewayDeploymentInfoDTO   {
  
    private String deploymentId = null;
    private String artifactId = null;

    @XmlType(name="KindEnum")
    @XmlEnum(String.class)
    public enum KindEnum {
        RESTAPI("RestAPI");
        private String value;

        KindEnum (String v) {
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
        public static KindEnum fromValue(String v) {
            for (KindEnum b : KindEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private KindEnum kind = null;
    private String updatedAt = null;

  /**
   * Deployment/revision identifier (revision UUID on-prem).
   **/
  public GatewayDeploymentInfoDTO deploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
    return this;
  }

  
  @ApiModelProperty(value = "Deployment/revision identifier (revision UUID on-prem).")
  @JsonProperty("deploymentId")
  public String getDeploymentId() {
    return deploymentId;
  }
  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }

  /**
   * Artifact identifier (API UUID on-prem).
   **/
  public GatewayDeploymentInfoDTO artifactId(String artifactId) {
    this.artifactId = artifactId;
    return this;
  }

  
  @ApiModelProperty(value = "Artifact identifier (API UUID on-prem).")
  @JsonProperty("artifactId")
  public String getArtifactId() {
    return artifactId;
  }
  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }

  /**
   * Artifact type (e.g. RestAPI).
   **/
  public GatewayDeploymentInfoDTO kind(KindEnum kind) {
    this.kind = kind;
    return this;
  }

  
  @ApiModelProperty(value = "Artifact type (e.g. RestAPI).")
  @JsonProperty("kind")
  public KindEnum getKind() {
    return kind;
  }
  public void setKind(KindEnum kind) {
    this.kind = kind;
  }

  /**
   * When the deployment was last updated (ISO-8601 date-time string).
   **/
  public GatewayDeploymentInfoDTO updatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  
  @ApiModelProperty(value = "When the deployment was last updated (ISO-8601 date-time string).")
  @JsonProperty("updatedAt")
  public String getUpdatedAt() {
    return updatedAt;
  }
  public void setUpdatedAt(String updatedAt) {
    this.updatedAt = updatedAt;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GatewayDeploymentInfoDTO gatewayDeploymentInfo = (GatewayDeploymentInfoDTO) o;
    return Objects.equals(deploymentId, gatewayDeploymentInfo.deploymentId) &&
        Objects.equals(artifactId, gatewayDeploymentInfo.artifactId) &&
        Objects.equals(kind, gatewayDeploymentInfo.kind) &&
        Objects.equals(updatedAt, gatewayDeploymentInfo.updatedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deploymentId, artifactId, kind, updatedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GatewayDeploymentInfoDTO {\n");
    
    sb.append("    deploymentId: ").append(toIndentedString(deploymentId)).append("\n");
    sb.append("    artifactId: ").append(toIndentedString(artifactId)).append("\n");
    sb.append("    kind: ").append(toIndentedString(kind)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
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

