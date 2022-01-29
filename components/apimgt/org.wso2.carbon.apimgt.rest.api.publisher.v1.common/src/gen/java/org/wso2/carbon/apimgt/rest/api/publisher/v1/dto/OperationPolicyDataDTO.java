package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicySpecAttributeDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class OperationPolicyDataDTO   {
  
    private String policyId = null;
    private String name = null;
    private String displayName = null;
    private String policyDescription = null;
    private List<String> flows = new ArrayList<String>();
    private List<String> gatewayTypes = new ArrayList<String>();
    private List<String> apiTypes = new ArrayList<String>();
    private List<OperationPolicySpecAttributeDTO> polictAttributes = new ArrayList<OperationPolicySpecAttributeDTO>();

  /**
   **/
  public OperationPolicyDataDTO policyId(String policyId) {
    this.policyId = policyId;
    return this;
  }

  
  @ApiModelProperty(example = "121223q41-24141-124124124-12414", value = "")
  @JsonProperty("policyId")
  public String getPolicyId() {
    return policyId;
  }
  public void setPolicyId(String policyId) {
    this.policyId = policyId;
  }

  /**
   **/
  public OperationPolicyDataDTO name(String name) {
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
  public OperationPolicyDataDTO displayName(String displayName) {
    this.displayName = displayName;
    return this;
  }

  
  @ApiModelProperty(example = "Remove Header Policy", value = "")
  @JsonProperty("displayName")
  public String getDisplayName() {
    return displayName;
  }
  public void setDisplayName(String displayName) {
    this.displayName = displayName;
  }

  /**
   **/
  public OperationPolicyDataDTO policyDescription(String policyDescription) {
    this.policyDescription = policyDescription;
    return this;
  }

  
  @ApiModelProperty(example = "With this policy, user can add a new header to the request", value = "")
  @JsonProperty("policyDescription")
  public String getPolicyDescription() {
    return policyDescription;
  }
  public void setPolicyDescription(String policyDescription) {
    this.policyDescription = policyDescription;
  }

  /**
   **/
  public OperationPolicyDataDTO flows(List<String> flows) {
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
  public OperationPolicyDataDTO gatewayTypes(List<String> gatewayTypes) {
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
  public OperationPolicyDataDTO apiTypes(List<String> apiTypes) {
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

  /**
   **/
  public OperationPolicyDataDTO polictAttributes(List<OperationPolicySpecAttributeDTO> polictAttributes) {
    this.polictAttributes = polictAttributes;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("polictAttributes")
  public List<OperationPolicySpecAttributeDTO> getPolictAttributes() {
    return polictAttributes;
  }
  public void setPolictAttributes(List<OperationPolicySpecAttributeDTO> polictAttributes) {
    this.polictAttributes = polictAttributes;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OperationPolicyDataDTO operationPolicyData = (OperationPolicyDataDTO) o;
    return Objects.equals(policyId, operationPolicyData.policyId) &&
        Objects.equals(name, operationPolicyData.name) &&
        Objects.equals(displayName, operationPolicyData.displayName) &&
        Objects.equals(policyDescription, operationPolicyData.policyDescription) &&
        Objects.equals(flows, operationPolicyData.flows) &&
        Objects.equals(gatewayTypes, operationPolicyData.gatewayTypes) &&
        Objects.equals(apiTypes, operationPolicyData.apiTypes) &&
        Objects.equals(polictAttributes, operationPolicyData.polictAttributes);
  }

  @Override
  public int hashCode() {
    return Objects.hash(policyId, name, displayName, policyDescription, flows, gatewayTypes, apiTypes, polictAttributes);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OperationPolicyDataDTO {\n");
    
    sb.append("    policyId: ").append(toIndentedString(policyId)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    displayName: ").append(toIndentedString(displayName)).append("\n");
    sb.append("    policyDescription: ").append(toIndentedString(policyDescription)).append("\n");
    sb.append("    flows: ").append(toIndentedString(flows)).append("\n");
    sb.append("    gatewayTypes: ").append(toIndentedString(gatewayTypes)).append("\n");
    sb.append("    apiTypes: ").append(toIndentedString(apiTypes)).append("\n");
    sb.append("    polictAttributes: ").append(toIndentedString(polictAttributes)).append("\n");
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

