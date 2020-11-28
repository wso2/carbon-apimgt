package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DeploymentClusterStatusDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class DeploymentStatusDTO   {
  
    private String type = null;
    private List<DeploymentClusterStatusDTO> clusters = new ArrayList<DeploymentClusterStatusDTO>();

  /**
   **/
  public DeploymentStatusDTO type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(example = "Kubernetes", required = true, value = "")
  @JsonProperty("type")
  @NotNull
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   **/
  public DeploymentStatusDTO clusters(List<DeploymentClusterStatusDTO> clusters) {
    this.clusters = clusters;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
      @Valid
  @JsonProperty("clusters")
  @NotNull
  public List<DeploymentClusterStatusDTO> getClusters() {
    return clusters;
  }
  public void setClusters(List<DeploymentClusterStatusDTO> clusters) {
    this.clusters = clusters;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DeploymentStatusDTO deploymentStatus = (DeploymentStatusDTO) o;
    return Objects.equals(type, deploymentStatus.type) &&
        Objects.equals(clusters, deploymentStatus.clusters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, clusters);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DeploymentStatusDTO {\n");
    
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    clusters: ").append(toIndentedString(clusters)).append("\n");
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

