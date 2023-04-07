package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.GlobalPolicyDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class GlobalPoliciesDTO   {
  
    private String gatewayLabel = null;
    private List<GlobalPolicyDTO> request = new ArrayList<GlobalPolicyDTO>();
    private List<GlobalPolicyDTO> response = new ArrayList<GlobalPolicyDTO>();
    private List<GlobalPolicyDTO> fault = new ArrayList<GlobalPolicyDTO>();

  /**
   **/
  public GlobalPoliciesDTO gatewayLabel(String gatewayLabel) {
    this.gatewayLabel = gatewayLabel;
    return this;
  }

  
  @ApiModelProperty(example = "gateway1", value = "")
  @JsonProperty("gatewayLabel")
  public String getGatewayLabel() {
    return gatewayLabel;
  }
  public void setGatewayLabel(String gatewayLabel) {
    this.gatewayLabel = gatewayLabel;
  }

  /**
   **/
  public GlobalPoliciesDTO request(List<GlobalPolicyDTO> request) {
    this.request = request;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("request")
  public List<GlobalPolicyDTO> getRequest() {
    return request;
  }
  public void setRequest(List<GlobalPolicyDTO> request) {
    this.request = request;
  }

  /**
   **/
  public GlobalPoliciesDTO response(List<GlobalPolicyDTO> response) {
    this.response = response;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("response")
  public List<GlobalPolicyDTO> getResponse() {
    return response;
  }
  public void setResponse(List<GlobalPolicyDTO> response) {
    this.response = response;
  }

  /**
   **/
  public GlobalPoliciesDTO fault(List<GlobalPolicyDTO> fault) {
    this.fault = fault;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("fault")
  public List<GlobalPolicyDTO> getFault() {
    return fault;
  }
  public void setFault(List<GlobalPolicyDTO> fault) {
    this.fault = fault;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GlobalPoliciesDTO globalPolicies = (GlobalPoliciesDTO) o;
    return Objects.equals(gatewayLabel, globalPolicies.gatewayLabel) &&
        Objects.equals(request, globalPolicies.request) &&
        Objects.equals(response, globalPolicies.response) &&
        Objects.equals(fault, globalPolicies.fault);
  }

  @Override
  public int hashCode() {
    return Objects.hash(gatewayLabel, request, response, fault);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GlobalPoliciesDTO {\n");
    
    sb.append("    gatewayLabel: ").append(toIndentedString(gatewayLabel)).append("\n");
    sb.append("    request: ").append(toIndentedString(request)).append("\n");
    sb.append("    response: ").append(toIndentedString(response)).append("\n");
    sb.append("    fault: ").append(toIndentedString(fault)).append("\n");
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

