package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class OperationPolicyDTO   {
  
    private String policyName = null;
    private String policyVersion = null;
    private String direction = null;
    private String policyId = null;
    private Integer order = null;
    private Map<String, Object> parameters = new HashMap<>();

  /**
   **/
  public OperationPolicyDTO policyName(String policyName) {
    this.policyName = policyName;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("policyName")
  public String getPolicyName() {
    return policyName;
  }
  public void setPolicyName(String policyName) {
    this.policyName = policyName;
  }

  /**
   **/
  public OperationPolicyDTO policyVersion(String policyVersion) {
    this.policyVersion = policyVersion;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("policyVersion")
  public String getPolicyVersion() {
    return policyVersion;
  }
  public void setPolicyVersion(String policyVersion) {
    this.policyVersion = policyVersion;
  }

  /**
   **/
  public OperationPolicyDTO direction(String direction) {
    this.direction = direction;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("direction")
  public String getDirection() {
    return direction;
  }
  public void setDirection(String direction) {
    this.direction = direction;
  }

  /**
   **/
  public OperationPolicyDTO policyId(String policyId) {
    this.policyId = policyId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("policyId")
  public String getPolicyId() {
    return policyId;
  }
  public void setPolicyId(String policyId) {
    this.policyId = policyId;
  }

  /**
   **/
  public OperationPolicyDTO order(Integer order) {
    this.order = order;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("order")
  public Integer getOrder() {
    return order;
  }
  public void setOrder(Integer order) {
    this.order = order;
  }

  /**
   **/
  public OperationPolicyDTO parameters(Map<String, Object> parameters) {
    this.parameters = parameters;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("parameters")
  public Map<String, Object> getParameters() {
    return parameters;
  }
  public void setParameters(Map<String, Object> parameters) {
    this.parameters = parameters;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OperationPolicyDTO operationPolicy = (OperationPolicyDTO) o;
    return Objects.equals(policyName, operationPolicy.policyName) &&
        Objects.equals(policyVersion, operationPolicy.policyVersion) &&
        Objects.equals(direction, operationPolicy.direction) &&
        Objects.equals(policyId, operationPolicy.policyId) &&
        Objects.equals(order, operationPolicy.order) &&
        Objects.equals(parameters, operationPolicy.parameters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(policyName, policyVersion, direction, policyId, order, parameters);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OperationPolicyDTO {\n");
    
    sb.append("    policyName: ").append(toIndentedString(policyName)).append("\n");
    sb.append("    policyVersion: ").append(toIndentedString(policyVersion)).append("\n");
    sb.append("    direction: ").append(toIndentedString(direction)).append("\n");
    sb.append("    policyId: ").append(toIndentedString(policyId)).append("\n");
    sb.append("    order: ").append(toIndentedString(order)).append("\n");
    sb.append("    parameters: ").append(toIndentedString(parameters)).append("\n");
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

