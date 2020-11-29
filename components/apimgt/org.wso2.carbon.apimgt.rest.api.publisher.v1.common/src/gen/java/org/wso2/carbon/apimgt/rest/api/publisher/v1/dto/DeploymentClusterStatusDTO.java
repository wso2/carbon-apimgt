package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.PodStatusDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class DeploymentClusterStatusDTO   {
  
    private String clusterName = null;
    private Integer podsRunning = null;
    private List<PodStatusDTO> healthStatus = new ArrayList<PodStatusDTO>();

  /**
   **/
  public DeploymentClusterStatusDTO clusterName(String clusterName) {
    this.clusterName = clusterName;
    return this;
  }

  
  @ApiModelProperty(example = "Minikube", required = true, value = "")
  @JsonProperty("clusterName")
  @NotNull
  public String getClusterName() {
    return clusterName;
  }
  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  /**
   **/
  public DeploymentClusterStatusDTO podsRunning(Integer podsRunning) {
    this.podsRunning = podsRunning;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("podsRunning")
  @NotNull
  public Integer getPodsRunning() {
    return podsRunning;
  }
  public void setPodsRunning(Integer podsRunning) {
    this.podsRunning = podsRunning;
  }

  /**
   **/
  public DeploymentClusterStatusDTO healthStatus(List<PodStatusDTO> healthStatus) {
    this.healthStatus = healthStatus;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
      @Valid
  @JsonProperty("healthStatus")
  @NotNull
  public List<PodStatusDTO> getHealthStatus() {
    return healthStatus;
  }
  public void setHealthStatus(List<PodStatusDTO> healthStatus) {
    this.healthStatus = healthStatus;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DeploymentClusterStatusDTO deploymentClusterStatus = (DeploymentClusterStatusDTO) o;
    return Objects.equals(clusterName, deploymentClusterStatus.clusterName) &&
        Objects.equals(podsRunning, deploymentClusterStatus.podsRunning) &&
        Objects.equals(healthStatus, deploymentClusterStatus.healthStatus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(clusterName, podsRunning, healthStatus);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DeploymentClusterStatusDTO {\n");
    
    sb.append("    clusterName: ").append(toIndentedString(clusterName)).append("\n");
    sb.append("    podsRunning: ").append(toIndentedString(podsRunning)).append("\n");
    sb.append("    healthStatus: ").append(toIndentedString(healthStatus)).append("\n");
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

