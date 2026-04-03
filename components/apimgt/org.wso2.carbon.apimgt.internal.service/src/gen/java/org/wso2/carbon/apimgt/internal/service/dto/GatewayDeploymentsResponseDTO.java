package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.internal.service.dto.GatewayDeploymentInfoDTO;
import javax.validation.constraints.*;

/**
 * Response for GET /deployments (platform contract).
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
@ApiModel(description = "Response for GET /deployments (platform contract).")

@JsonIgnoreProperties(ignoreUnknown = true)

public class GatewayDeploymentsResponseDTO   {
  
    private List<GatewayDeploymentInfoDTO> deployments = new ArrayList<>();

  /**
   **/
  public GatewayDeploymentsResponseDTO deployments(List<GatewayDeploymentInfoDTO> deployments) {
    this.deployments = deployments;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("deployments")
  public List<GatewayDeploymentInfoDTO> getDeployments() {
    return deployments;
  }
  public void setDeployments(List<GatewayDeploymentInfoDTO> deployments) {
    this.deployments = deployments;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GatewayDeploymentsResponseDTO gatewayDeploymentsResponse = (GatewayDeploymentsResponseDTO) o;
    return Objects.equals(deployments, gatewayDeploymentsResponse.deployments);
  }

  @Override
  public int hashCode() {
    return Objects.hash(deployments);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GatewayDeploymentsResponseDTO {\n");
    
    sb.append("    deployments: ").append(toIndentedString(deployments)).append("\n");
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

