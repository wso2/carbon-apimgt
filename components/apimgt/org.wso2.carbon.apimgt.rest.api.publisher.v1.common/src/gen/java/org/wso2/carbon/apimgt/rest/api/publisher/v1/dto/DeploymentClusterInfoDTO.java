package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class DeploymentClusterInfoDTO   {
  
    private String clusterName = null;
    private String accessURL = null;
    private String displayName = null;
    private Map<String, String> properties = new HashMap<String, String>();

  /**
   **/
  public DeploymentClusterInfoDTO clusterName(String clusterName) {
    this.clusterName = clusterName;
    return this;
  }

  
  @ApiModelProperty(example = "minikube", required = true, value = "")
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
  public DeploymentClusterInfoDTO accessURL(String accessURL) {
    this.accessURL = accessURL;
    return this;
  }

  
  @ApiModelProperty(example = "https://api.com", required = true, value = "")
  @JsonProperty("accessURL")
  @NotNull
  public String getAccessURL() {
    return accessURL;
  }
  public void setAccessURL(String accessURL) {
    this.accessURL = accessURL;
  }

  /**
   **/
  public DeploymentClusterInfoDTO displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(example = "kubernetes-minikube", required = true, value = "")
  @JsonProperty("displayName")
  @NotNull
  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   **/
  public DeploymentClusterInfoDTO properties(Map<String, String> properties) {
    this.properties = properties;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("properties")
  @NotNull
  public Map<String, String> getProperties() {
    return properties;
  }
  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
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
        Objects.equals(accessURL, deploymentClusterInfo.accessURL) &&
        Objects.equals(displayName, deploymentClusterInfo.displayName) &&
        Objects.equals(properties, deploymentClusterInfo.properties);
  }

  @Override
  public int hashCode() {
    return Objects.hash(clusterName, accessURL, displayName, properties);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DeploymentClusterInfoDTO {\n");
    
    sb.append("    clusterName: ").append(toIndentedString(clusterName)).append("\n");
    sb.append("    accessURL: ").append(toIndentedString(accessURL)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    properties: ").append(toIndentedString(properties)).append("\n");
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

