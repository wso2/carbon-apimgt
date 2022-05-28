package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class OperationPolicyDTO   {
  
    private String policyName = null;
    private String policyVersion = "v1";
    private String policyId = null;
    private Map<String, Object> parameters = new HashMap<String, Object>();

  /**
   **/
  public OperationPolicyDTO policyName(String policyName) {
    this.policyName = policyName;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("policyName")
  @NotNull
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
        Objects.equals(policyId, operationPolicy.policyId) &&
        Objects.equals(parameters, operationPolicy.parameters);
  }

  @Override
  public int hashCode() {
    return Objects.hash(policyName, policyVersion, policyId, parameters);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OperationPolicyDTO {\n");
    
    sb.append("    policyName: ").append(toIndentedString(policyName)).append("\n");
    sb.append("    policyVersion: ").append(toIndentedString(policyVersion)).append("\n");
    sb.append("    policyId: ").append(toIndentedString(policyId)).append("\n");
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

