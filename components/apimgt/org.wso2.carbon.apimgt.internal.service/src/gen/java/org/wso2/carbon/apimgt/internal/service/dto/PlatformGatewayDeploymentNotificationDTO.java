package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import javax.validation.constraints.*;

/**
 * API Platform gateway deployment notification (sent by gateway after deploy).
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "API Platform gateway deployment notification (sent by gateway after deploy).")

public class PlatformGatewayDeploymentNotificationDTO extends HashMap<String, Object>  {
  
    private String id = null;
    private String status = null;
    private Object _configuration = null;
    private OffsetDateTime createdAt = null;
    private OffsetDateTime updatedAt = null;
    private OffsetDateTime deployedAt = null;
    private Long deployedVersion = null;
    private String projectIdentifier = null;

  /**
   * API ID (deployed API).
   **/
  public PlatformGatewayDeploymentNotificationDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(value = "API ID (deployed API).")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   * Deployment status (e.g. DEPLOYED).
   **/
  public PlatformGatewayDeploymentNotificationDTO status(String status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(value = "Deployment status (e.g. DEPLOYED).")
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }

  /**
   * API configuration snapshot.
   **/
  public PlatformGatewayDeploymentNotificationDTO _configuration(Object _configuration) {
    this._configuration = _configuration;
    return this;
  }

  
  @ApiModelProperty(value = "API configuration snapshot.")
      @Valid
  @JsonProperty("configuration")
  public Object getConfiguration() {
    return _configuration;
  }
  public void setConfiguration(Object _configuration) {
    this._configuration = _configuration;
  }

  /**
   **/
  public PlatformGatewayDeploymentNotificationDTO createdAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("createdAt")
  public OffsetDateTime getCreatedAt() {
    return createdAt;
  }
  public void setCreatedAt(OffsetDateTime createdAt) {
    this.createdAt = createdAt;
  }

  /**
   **/
  public PlatformGatewayDeploymentNotificationDTO updatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("updatedAt")
  public OffsetDateTime getUpdatedAt() {
    return updatedAt;
  }
  public void setUpdatedAt(OffsetDateTime updatedAt) {
    this.updatedAt = updatedAt;
  }

  /**
   **/
  public PlatformGatewayDeploymentNotificationDTO deployedAt(OffsetDateTime deployedAt) {
    this.deployedAt = deployedAt;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("deployedAt")
  public OffsetDateTime getDeployedAt() {
    return deployedAt;
  }
  public void setDeployedAt(OffsetDateTime deployedAt) {
    this.deployedAt = deployedAt;
  }

  /**
   **/
  public PlatformGatewayDeploymentNotificationDTO deployedVersion(Long deployedVersion) {
    this.deployedVersion = deployedVersion;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("deployedVersion")
  public Long getDeployedVersion() {
    return deployedVersion;
  }
  public void setDeployedVersion(Long deployedVersion) {
    this.deployedVersion = deployedVersion;
  }

  /**
   **/
  public PlatformGatewayDeploymentNotificationDTO projectIdentifier(String projectIdentifier) {
    this.projectIdentifier = projectIdentifier;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("projectIdentifier")
  public String getProjectIdentifier() {
    return projectIdentifier;
  }
  public void setProjectIdentifier(String projectIdentifier) {
    this.projectIdentifier = projectIdentifier;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PlatformGatewayDeploymentNotificationDTO platformGatewayDeploymentNotification = (PlatformGatewayDeploymentNotificationDTO) o;
    return Objects.equals(id, platformGatewayDeploymentNotification.id) &&
        Objects.equals(status, platformGatewayDeploymentNotification.status) &&
        Objects.equals(_configuration, platformGatewayDeploymentNotification._configuration) &&
        Objects.equals(createdAt, platformGatewayDeploymentNotification.createdAt) &&
        Objects.equals(updatedAt, platformGatewayDeploymentNotification.updatedAt) &&
        Objects.equals(deployedAt, platformGatewayDeploymentNotification.deployedAt) &&
        Objects.equals(deployedVersion, platformGatewayDeploymentNotification.deployedVersion) &&
        Objects.equals(projectIdentifier, platformGatewayDeploymentNotification.projectIdentifier);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, status, _configuration, createdAt, updatedAt, deployedAt, deployedVersion, projectIdentifier);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PlatformGatewayDeploymentNotificationDTO {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    _configuration: ").append(toIndentedString(_configuration)).append("\n");
    sb.append("    createdAt: ").append(toIndentedString(createdAt)).append("\n");
    sb.append("    updatedAt: ").append(toIndentedString(updatedAt)).append("\n");
    sb.append("    deployedAt: ").append(toIndentedString(deployedAt)).append("\n");
    sb.append("    deployedVersion: ").append(toIndentedString(deployedVersion)).append("\n");
    sb.append("    projectIdentifier: ").append(toIndentedString(projectIdentifier)).append("\n");
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

