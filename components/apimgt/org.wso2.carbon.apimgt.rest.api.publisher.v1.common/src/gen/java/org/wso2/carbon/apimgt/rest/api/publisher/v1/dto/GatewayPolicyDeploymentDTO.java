/*
 * Copyright (c) 2023, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * Data Transfer Object for Gateway Policy Deployment
 */
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

