package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.APIDeploymentClusterInfoDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class APIIngressURLsDTO   {
  
    private String deploymentEnvironmentName = null;
    private List<APIDeploymentClusterInfoDTO> clusterDetails = new ArrayList<APIDeploymentClusterInfoDTO>();

  /**
   **/
  public APIIngressURLsDTO deploymentEnvironmentName(String deploymentEnvironmentName) {
    this.deploymentEnvironmentName = deploymentEnvironmentName;
    return this;
  }

  
  @ApiModelProperty(example = "kubernetes", value = "")
  @JsonProperty("deploymentEnvironmentName")
  public String getDeploymentEnvironmentName() {
    return deploymentEnvironmentName;
  }
  public void setDeploymentEnvironmentName(String deploymentEnvironmentName) {
    this.deploymentEnvironmentName = deploymentEnvironmentName;
  }

  /**
   **/
  public APIIngressURLsDTO clusterDetails(List<APIDeploymentClusterInfoDTO> clusterDetails) {
    this.clusterDetails = clusterDetails;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("clusterDetails")
  public List<APIDeploymentClusterInfoDTO> getClusterDetails() {
    return clusterDetails;
  }
  public void setClusterDetails(List<APIDeploymentClusterInfoDTO> clusterDetails) {
    this.clusterDetails = clusterDetails;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APIIngressURLsDTO apIIngressURLs = (APIIngressURLsDTO) o;
    return Objects.equals(deploymentEnvironmentName, apIIngressURLs.deploymentEnvironmentName) &&
        Objects.equals(clusterDetails, apIIngressURLs.clusterDetails);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deploymentEnvironmentName, clusterDetails);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIIngressURLsDTO {\n");
    
    sb.append("    deploymentEnvironmentName: ").append(toIndentedString(deploymentEnvironmentName)).append("\n");
    sb.append("    clusterDetails: ").append(toIndentedString(clusterDetails)).append("\n");
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

