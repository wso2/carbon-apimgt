package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.OperationPolicyDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class APIOperationPoliciesDTO   {
  
    private List<OperationPolicyDTO> request = new ArrayList<OperationPolicyDTO>();
    private List<OperationPolicyDTO> response = new ArrayList<OperationPolicyDTO>();
    private List<OperationPolicyDTO> fault = new ArrayList<OperationPolicyDTO>();

  /**
   **/
  public APIOperationPoliciesDTO request(List<OperationPolicyDTO> request) {
    this.request = request;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("request")
  public List<OperationPolicyDTO> getRequest() {
    return request;
  }
  public void setRequest(List<OperationPolicyDTO> request) {
    this.request = request;
  }

  /**
   **/
  public APIOperationPoliciesDTO response(List<OperationPolicyDTO> response) {
    this.response = response;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("response")
  public List<OperationPolicyDTO> getResponse() {
    return response;
  }
  public void setResponse(List<OperationPolicyDTO> response) {
    this.response = response;
  }

  /**
   **/
  public APIOperationPoliciesDTO fault(List<OperationPolicyDTO> fault) {
    this.fault = fault;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("fault")
  public List<OperationPolicyDTO> getFault() {
    return fault;
  }
  public void setFault(List<OperationPolicyDTO> fault) {
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
    APIOperationPoliciesDTO apIOperationPolicies = (APIOperationPoliciesDTO) o;
    return Objects.equals(request, apIOperationPolicies.request) &&
        Objects.equals(response, apIOperationPolicies.response) &&
        Objects.equals(fault, apIOperationPolicies.fault);
  }

  @Override
  public int hashCode() {
    return Objects.hash(request, response, fault);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIOperationPoliciesDTO {\n");
    
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

