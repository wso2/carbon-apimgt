package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class DeploymentEnvironmentsDTO   {
  
    private String type = null;
    private List<String> clusterName = new ArrayList<String>();

  /**
   **/
  public DeploymentEnvironmentsDTO type(String type) {
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
  public DeploymentEnvironmentsDTO clusterName(List<String> clusterName) {
    this.clusterName = clusterName;
    return this;
  }

  
  @ApiModelProperty(example = "[\"minikube\"]", required = true, value = "")
  @JsonProperty("clusterName")
  @NotNull
  public List<String> getClusterName() {
    return clusterName;
  }
  public void setClusterName(List<String> clusterName) {
    this.clusterName = clusterName;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DeploymentEnvironmentsDTO deploymentEnvironments = (DeploymentEnvironmentsDTO) o;
    return Objects.equals(type, deploymentEnvironments.type) &&
        Objects.equals(clusterName, deploymentEnvironments.clusterName);
  }

  @Override
  public int hashCode() {
    return Objects.hash(type, clusterName);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DeploymentEnvironmentsDTO {\n");
    
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    clusterName: ").append(toIndentedString(clusterName)).append("\n");
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

