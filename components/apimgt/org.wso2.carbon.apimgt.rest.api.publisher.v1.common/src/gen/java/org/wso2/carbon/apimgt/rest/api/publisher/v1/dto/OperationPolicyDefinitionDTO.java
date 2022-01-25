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



public class OperationPolicyDefinitionDTO   {
  
    private String name = null;
    private List<String> flows = new ArrayList<String>();
    private List<String> gatewayTypes = new ArrayList<String>();
    private List<String> apiTypes = new ArrayList<String>();

  /**
   **/
  public OperationPolicyDefinitionDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(example = "removeHeaderPolicy", value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public OperationPolicyDefinitionDTO flows(List<String> flows) {
    this.flows = flows;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("flows")
  public List<String> getFlows() {
    return flows;
  }
  public void setFlows(List<String> flows) {
    this.flows = flows;
  }

  /**
   **/
  public OperationPolicyDefinitionDTO gatewayTypes(List<String> gatewayTypes) {
    this.gatewayTypes = gatewayTypes;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("gatewayTypes")
  public List<String> getGatewayTypes() {
    return gatewayTypes;
  }
  public void setGatewayTypes(List<String> gatewayTypes) {
    this.gatewayTypes = gatewayTypes;
  }

  /**
   **/
  public OperationPolicyDefinitionDTO apiTypes(List<String> apiTypes) {
    this.apiTypes = apiTypes;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("apiTypes")
  public List<String> getApiTypes() {
    return apiTypes;
  }
  public void setApiTypes(List<String> apiTypes) {
    this.apiTypes = apiTypes;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OperationPolicyDefinitionDTO operationPolicyDefinition = (OperationPolicyDefinitionDTO) o;
    return Objects.equals(name, operationPolicyDefinition.name) &&
        Objects.equals(flows, operationPolicyDefinition.flows) &&
        Objects.equals(gatewayTypes, operationPolicyDefinition.gatewayTypes) &&
        Objects.equals(apiTypes, operationPolicyDefinition.apiTypes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(name, flows, gatewayTypes, apiTypes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OperationPolicyDefinitionDTO {\n");
    
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    flows: ").append(toIndentedString(flows)).append("\n");
    sb.append("    gatewayTypes: ").append(toIndentedString(gatewayTypes)).append("\n");
    sb.append("    apiTypes: ").append(toIndentedString(apiTypes)).append("\n");
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

