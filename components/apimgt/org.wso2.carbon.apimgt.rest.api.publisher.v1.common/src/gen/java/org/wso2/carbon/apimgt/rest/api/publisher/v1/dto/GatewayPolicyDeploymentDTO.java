package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

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



public class GatewayPolicyDeploymentDTO   {
  
    private String mappingUUID = null;
    private String gatewayLabel = null;
    private Boolean gatewayDeployment = null;

  /**
   **/
  public GatewayPolicyDeploymentDTO mappingUUID(String mappingUUID) {
    this.mappingUUID = mappingUUID;
    return this;
  }

  
  @ApiModelProperty(example = "01234567-0123-0123-0123-012345678901", value = "")
  @JsonProperty("mappingUUID")
  public String getMappingUUID() {
    return mappingUUID;
  }
  public void setMappingUUID(String mappingUUID) {
    this.mappingUUID = mappingUUID;
  }

  /**
   **/
  public GatewayPolicyDeploymentDTO gatewayLabel(String gatewayLabel) {
    this.gatewayLabel = gatewayLabel;
    return this;
  }

  
  @ApiModelProperty(example = "gatewayLabel_1", value = "")
  @JsonProperty("gatewayLabel")
  public String getGatewayLabel() {
    return gatewayLabel;
  }
  public void setGatewayLabel(String gatewayLabel) {
    this.gatewayLabel = gatewayLabel;
  }

  /**
   **/
  public GatewayPolicyDeploymentDTO gatewayDeployment(Boolean gatewayDeployment) {
    this.gatewayDeployment = gatewayDeployment;
    return this;
  }

  
  @ApiModelProperty(example = "true", value = "")
  @JsonProperty("gatewayDeployment")
  public Boolean isGatewayDeployment() {
    return gatewayDeployment;
  }
  public void setGatewayDeployment(Boolean gatewayDeployment) {
    this.gatewayDeployment = gatewayDeployment;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GatewayPolicyDeploymentDTO gatewayPolicyDeployment = (GatewayPolicyDeploymentDTO) o;
    return Objects.equals(mappingUUID, gatewayPolicyDeployment.mappingUUID) &&
        Objects.equals(gatewayLabel, gatewayPolicyDeployment.gatewayLabel) &&
        Objects.equals(gatewayDeployment, gatewayPolicyDeployment.gatewayDeployment);
  }

  @Override
  public int hashCode() {
    return Objects.hash(mappingUUID, gatewayLabel, gatewayDeployment);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GatewayPolicyDeploymentDTO {\n");
    
    sb.append("    mappingUUID: ").append(toIndentedString(mappingUUID)).append("\n");
    sb.append("    gatewayLabel: ").append(toIndentedString(gatewayLabel)).append("\n");
    sb.append("    gatewayDeployment: ").append(toIndentedString(gatewayDeployment)).append("\n");
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

