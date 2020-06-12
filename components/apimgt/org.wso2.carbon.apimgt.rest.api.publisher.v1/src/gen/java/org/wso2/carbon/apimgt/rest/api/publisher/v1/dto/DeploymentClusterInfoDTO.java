package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class DeploymentClusterInfoDTO   {
  
    private String clusterName = null;
    private String masterURL = null;
    private String namespace = null;
    private String ingressURL = null;
    private String clusterId = null;

  /**
   **/
  public DeploymentClusterInfoDTO clusterName(String clusterName) {
    this.clusterName = clusterName;
    return this;
  }

  
  @ApiModelProperty(example = "Kubernetes", required = true, value = "")
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
  public DeploymentClusterInfoDTO masterURL(String masterURL) {
    this.masterURL = masterURL;
    return this;
  }

  
  @ApiModelProperty(example = "https://localhost:9095", required = true, value = "")
  @JsonProperty("masterURL")
  @NotNull
  public String getMasterURL() {
    return masterURL;
  }
  public void setMasterURL(String masterURL) {
    this.masterURL = masterURL;
  }

  /**
   **/
  public DeploymentClusterInfoDTO namespace(String namespace) {
    this.namespace = namespace;
    return this;
  }

  
  @ApiModelProperty(example = "def", required = true, value = "")
  @JsonProperty("namespace")
  @NotNull
  public String getNamespace() {
    return namespace;
  }
  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  /**
   **/
  public DeploymentClusterInfoDTO ingressURL(String ingressURL) {
    this.ingressURL = ingressURL;
    return this;
  }

  
  @ApiModelProperty(example = "https://api.com", required = true, value = "")
  @JsonProperty("ingressURL")
  @NotNull
  public String getIngressURL() {
    return ingressURL;
  }
  public void setIngressURL(String ingressURL) {
    this.ingressURL = ingressURL;
  }

  /**
   **/
  public DeploymentClusterInfoDTO clusterId(String clusterId) {
    this.clusterId = clusterId;
    return this;
  }

  
  @ApiModelProperty(example = "kubernetes-minikube", required = true, value = "")
  @JsonProperty("clusterId")
  @NotNull
  public String getClusterId() {
    return clusterId;
  }
  public void setClusterId(String clusterId) {
    this.clusterId = clusterId;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DeploymentClusterInfoDTO deploymentClusterInfo = (DeploymentClusterInfoDTO) o;
    return Objects.equals(clusterName, deploymentClusterInfo.clusterName) &&
        Objects.equals(masterURL, deploymentClusterInfo.masterURL) &&
        Objects.equals(namespace, deploymentClusterInfo.namespace) &&
        Objects.equals(ingressURL, deploymentClusterInfo.ingressURL) &&
        Objects.equals(clusterId, deploymentClusterInfo.clusterId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(clusterName, masterURL, namespace, ingressURL, clusterId);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DeploymentClusterInfoDTO {\n");
    
    sb.append("    clusterName: ").append(toIndentedString(clusterName)).append("\n");
    sb.append("    masterURL: ").append(toIndentedString(masterURL)).append("\n");
    sb.append("    namespace: ").append(toIndentedString(namespace)).append("\n");
    sb.append("    ingressURL: ").append(toIndentedString(ingressURL)).append("\n");
    sb.append("    clusterId: ").append(toIndentedString(clusterId)).append("\n");
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

