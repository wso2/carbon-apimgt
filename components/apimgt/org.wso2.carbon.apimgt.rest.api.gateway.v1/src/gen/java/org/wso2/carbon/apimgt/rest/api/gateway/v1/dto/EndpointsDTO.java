package org.wso2.carbon.apimgt.rest.api.gateway.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class EndpointsDTO   {
  
    private List<String> deployedEndpoints = new ArrayList<>();
    private List<String> notdeployedEndpoints = new ArrayList<>();

  /**
   * The end points which has been deployed in the gateway 
   **/
  public EndpointsDTO deployedEndpoints(List<String> deployedEndpoints) {
    this.deployedEndpoints = deployedEndpoints;
    return this;
  }

  
  @ApiModelProperty(value = "The end points which has been deployed in the gateway ")
  @JsonProperty("deployedEndpoints")
  public List<String> getDeployedEndpoints() {
    return deployedEndpoints;
  }
  public void setDeployedEndpoints(List<String> deployedEndpoints) {
    this.deployedEndpoints = deployedEndpoints;
  }

  /**
   * The end points which has not been deployed in the gateway 
   **/
  public EndpointsDTO notdeployedEndpoints(List<String> notdeployedEndpoints) {
    this.notdeployedEndpoints = notdeployedEndpoints;
    return this;
  }

  
  @ApiModelProperty(value = "The end points which has not been deployed in the gateway ")
  @JsonProperty("notdeployedEndpoints")
  public List<String> getNotdeployedEndpoints() {
    return notdeployedEndpoints;
  }
  public void setNotdeployedEndpoints(List<String> notdeployedEndpoints) {
    this.notdeployedEndpoints = notdeployedEndpoints;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EndpointsDTO endpoints = (EndpointsDTO) o;
    return Objects.equals(deployedEndpoints, endpoints.deployedEndpoints) &&
        Objects.equals(notdeployedEndpoints, endpoints.notdeployedEndpoints);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deployedEndpoints, notdeployedEndpoints);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EndpointsDTO {\n");
    
    sb.append("    deployedEndpoints: ").append(toIndentedString(deployedEndpoints)).append("\n");
    sb.append("    notdeployedEndpoints: ").append(toIndentedString(notdeployedEndpoints)).append("\n");
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

