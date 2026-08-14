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
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@ApiModel(description = "Single deployment info for gateway sync (platform contract).")

@JsonIgnoreProperties(ignoreUnknown = true)

public class GatewayDeploymentInfoDTO   {
  
    private String deploymentId = null;
    private String artifactId = null;
    private String kind = null;
    private String state = null;
    private String deployedAt = null;

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
   * Artifact type (e.g. RestApi).
   **/
  public GatewayDeploymentInfoDTO kind(String kind) {
    this.kind = kind;
    return this;
  }

  
  @ApiModelProperty(value = "Artifact type (e.g. RestApi).")
  @JsonProperty("kind")
  public String getKind() {
    return kind;
  }
  public void setKind(String kind) {
    this.kind = kind;
  }

  /**
   * Desired deployment state of the artifact on the gateway. Every entry returned by this endpoint is currently deployed; absence from the list means undeployed/orphaned.
   **/
  public GatewayDeploymentInfoDTO state(String state) {
    this.state = state;
    return this;
  }

  
  @ApiModelProperty(value = "Desired deployment state of the artifact on the gateway. Every entry returned by this endpoint is currently deployed; absence from the list means undeployed/orphaned.")
  @JsonProperty("state")
  public String getState() {
    return state;
  }
  public void setState(String state) {
    this.state = state;
  }

  /**
   * When the deployment was last (un)deployed (ISO-8601 date-time string).
   **/
  public GatewayDeploymentInfoDTO deployedAt(String deployedAt) {
    this.deployedAt = deployedAt;
    return this;
  }

  
  @ApiModelProperty(value = "When the deployment was last (un)deployed (ISO-8601 date-time string).")
  @JsonProperty("deployedAt")
  public String getDeployedAt() {
    return deployedAt;
  }
  public void setDeployedAt(String deployedAt) {
    this.deployedAt = deployedAt;
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
        Objects.equals(state, gatewayDeploymentInfo.state) &&
        Objects.equals(deployedAt, gatewayDeploymentInfo.deployedAt);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deploymentId, artifactId, kind, state, deployedAt);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GatewayDeploymentInfoDTO {\n");
    
    sb.append("    deploymentId: ").append(toIndentedString(deploymentId)).append("\n");
    sb.append("    artifactId: ").append(toIndentedString(artifactId)).append("\n");
    sb.append("    kind: ").append(toIndentedString(kind)).append("\n");
    sb.append("    state: ").append(toIndentedString(state)).append("\n");
    sb.append("    deployedAt: ").append(toIndentedString(deployedAt)).append("\n");
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

