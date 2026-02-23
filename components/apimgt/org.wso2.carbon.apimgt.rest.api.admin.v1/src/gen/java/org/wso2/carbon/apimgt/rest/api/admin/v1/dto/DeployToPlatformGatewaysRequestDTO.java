package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;

/**
 * Request body for deploying an API to platform gateways. Provide either platformGatewayIds (UUIDs from GET /gateways) or platformGatewayNames (e.g. prod-gateway-02), or both. At least one of the two arrays must be non-empty. 
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Request body for deploying an API to platform gateways. Provide either platformGatewayIds (UUIDs from GET /gateways) or platformGatewayNames (e.g. prod-gateway-02), or both. At least one of the two arrays must be non-empty. ")

public class DeployToPlatformGatewaysRequestDTO   {
  
    private List<String> platformGatewayIds = new ArrayList<String>();
    private List<String> platformGatewayNames = new ArrayList<String>();

  /**
   * Platform gateway UUIDs (from GET /gateways).
   **/
  public DeployToPlatformGatewaysRequestDTO platformGatewayIds(List<String> platformGatewayIds) {
    this.platformGatewayIds = platformGatewayIds;
    return this;
  }

  
  @ApiModelProperty(value = "Platform gateway UUIDs (from GET /gateways).")
  @JsonProperty("platformGatewayIds")
  public List<String> getPlatformGatewayIds() {
    return platformGatewayIds;
  }
  public void setPlatformGatewayIds(List<String> platformGatewayIds) {
    this.platformGatewayIds = platformGatewayIds;
  }

  /**
   * Platform gateway names (e.g. prod-gateway-02). Resolved to IDs for the organization.
   **/
  public DeployToPlatformGatewaysRequestDTO platformGatewayNames(List<String> platformGatewayNames) {
    this.platformGatewayNames = platformGatewayNames;
    return this;
  }

  
  @ApiModelProperty(value = "Platform gateway names (e.g. prod-gateway-02). Resolved to IDs for the organization.")
  @JsonProperty("platformGatewayNames")
  public List<String> getPlatformGatewayNames() {
    return platformGatewayNames;
  }
  public void setPlatformGatewayNames(List<String> platformGatewayNames) {
    this.platformGatewayNames = platformGatewayNames;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DeployToPlatformGatewaysRequestDTO deployToPlatformGatewaysRequest = (DeployToPlatformGatewaysRequestDTO) o;
    return Objects.equals(platformGatewayIds, deployToPlatformGatewaysRequest.platformGatewayIds) &&
        Objects.equals(platformGatewayNames, deployToPlatformGatewaysRequest.platformGatewayNames);
  }

  @Override
  public int hashCode() {
    return Objects.hash(platformGatewayIds, platformGatewayNames);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DeployToPlatformGatewaysRequestDTO {\n");
    
    sb.append("    platformGatewayIds: ").append(toIndentedString(platformGatewayIds)).append("\n");
    sb.append("    platformGatewayNames: ").append(toIndentedString(platformGatewayNames)).append("\n");
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

