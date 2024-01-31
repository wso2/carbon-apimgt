package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class GatewayPolicyMappingDeploymentInfoDTO   {
  
    private String id = null;
    private String description = null;
    private String displayName = null;
    private List<String> appliedGatewayLabels = new ArrayList<String>();

  /**
   **/
  public GatewayPolicyMappingDeploymentInfoDTO id(String id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(example = "121223q41-24141-124124124-12414", value = "")
  @JsonProperty("id")
  public String getId() {
    return id;
  }
  public void setId(String id) {
    this.id = id;
  }

  /**
   * A brief description about the policy mapping
   **/
  public GatewayPolicyMappingDeploymentInfoDTO description(String description) {
    this.description = description;
    return this;
  }

  
  @ApiModelProperty(example = "Set header value to the request with item type and response header set with served server name", value = "A brief description about the policy mapping")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  /**
   * Meaningful name to identify the policy mapping
   **/
  public GatewayPolicyMappingDeploymentInfoDTO displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(example = "item_type_setter", value = "Meaningful name to identify the policy mapping")
  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   **/
  public GatewayPolicyMappingDeploymentInfoDTO appliedGatewayLabels(List<String> appliedGatewayLabels) {
    this.appliedGatewayLabels = appliedGatewayLabels;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("appliedGatewayLabels")
  public List<String> getAppliedGatewayLabels() {
    return appliedGatewayLabels;
  }
  public void setAppliedGatewayLabels(List<String> appliedGatewayLabels) {
    this.appliedGatewayLabels = appliedGatewayLabels;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GatewayPolicyMappingDeploymentInfoDTO gatewayPolicyMappingDeploymentInfo = (GatewayPolicyMappingDeploymentInfoDTO) o;
    return Objects.equals(id, gatewayPolicyMappingDeploymentInfo.id) &&
        Objects.equals(description, gatewayPolicyMappingDeploymentInfo.description) &&
        Objects.equals(displayName, gatewayPolicyMappingDeploymentInfo.displayName) &&
        Objects.equals(appliedGatewayLabels, gatewayPolicyMappingDeploymentInfo.appliedGatewayLabels);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, description, displayName, appliedGatewayLabels);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GatewayPolicyMappingDeploymentInfoDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    appliedGatewayLabels: ").append(toIndentedString(appliedGatewayLabels)).append("\n");
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

