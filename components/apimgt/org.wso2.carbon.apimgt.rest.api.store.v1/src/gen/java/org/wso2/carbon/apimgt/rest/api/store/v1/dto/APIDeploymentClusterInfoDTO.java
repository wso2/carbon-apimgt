package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class APIDeploymentClusterInfoDTO   {
  
    private String clusterName = null;
    private String clusterDisplayName = null;
    private String ingressURL = null;

  /**
   * Name of the cluster 
   **/
  public APIDeploymentClusterInfoDTO clusterName(String clusterName) {
    this.clusterName = clusterName;
    return this;
  }

  
  @ApiModelProperty(example = "minikube", required = true, value = "Name of the cluster ")
  @JsonProperty("clusterName")
  @NotNull
  public String getClusterName() {
    return clusterName;
  }
  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  /**
   * Display name of the cluster 
   **/
  public APIDeploymentClusterInfoDTO clusterDisplayName(String clusterDisplayName) {
    this.clusterDisplayName = clusterDisplayName;
    return this;
  }

  
  @ApiModelProperty(example = "Minikube Cluster", value = "Display name of the cluster ")
  @JsonProperty("clusterDisplayName")
  public String getClusterDisplayName() {
    return clusterDisplayName;
  }
  public void setClusterDisplayName(String clusterDisplayName) {
    this.clusterDisplayName = clusterDisplayName;
  }

  /**
   * ingress URL
   **/
  public APIDeploymentClusterInfoDTO ingressURL(String ingressURL) {
    this.ingressURL = ingressURL;
    return this;
  }

  
  @ApiModelProperty(example = "http://wso2.com:9443", required = true, value = "ingress URL")
  @JsonProperty("ingressURL")
  @NotNull
  public String getIngressURL() {
    return ingressURL;
  }
  public void setIngressURL(String ingressURL) {
    this.ingressURL = ingressURL;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIDeploymentClusterInfoDTO apIDeploymentClusterInfo = (APIDeploymentClusterInfoDTO) o;
    return Objects.equals(clusterName, apIDeploymentClusterInfo.clusterName) &&
        Objects.equals(clusterDisplayName, apIDeploymentClusterInfo.clusterDisplayName) &&
        Objects.equals(ingressURL, apIDeploymentClusterInfo.ingressURL);
  }

  @Override
  public int hashCode() {
    return Objects.hash(clusterName, clusterDisplayName, ingressURL);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIDeploymentClusterInfoDTO {\n");
    
    sb.append("    clusterName: ").append(toIndentedString(clusterName)).append("\n");
    sb.append("    clusterDisplayName: ").append(toIndentedString(clusterDisplayName)).append("\n");
    sb.append("    ingressURL: ").append(toIndentedString(ingressURL)).append("\n");
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

