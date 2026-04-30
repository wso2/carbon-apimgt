package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;

/**
 * Request body for POST /deployments/batch.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@ApiModel(description = "Request body for POST /deployments/batch.")

@JsonIgnoreProperties(ignoreUnknown = true)

public class BatchDeploymentsRequestDTO   {
  
    private List<String> deploymentIds = new ArrayList<>();

  /**
   * List of deployment IDs (revision UUIDs) to fetch.
   **/
  public BatchDeploymentsRequestDTO deploymentIds(List<String> deploymentIds) {
    this.deploymentIds = deploymentIds;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "List of deployment IDs (revision UUIDs) to fetch.")
  @JsonProperty("deploymentIds")
  @NotNull
  public List<String> getDeploymentIds() {
    return deploymentIds;
  }
  public void setDeploymentIds(List<String> deploymentIds) {
    this.deploymentIds = deploymentIds;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BatchDeploymentsRequestDTO batchDeploymentsRequest = (BatchDeploymentsRequestDTO) o;
    return Objects.equals(deploymentIds, batchDeploymentsRequest.deploymentIds);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deploymentIds);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class BatchDeploymentsRequestDTO {\n");
    
    sb.append("    deploymentIds: ").append(toIndentedString(deploymentIds)).append("\n");
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

