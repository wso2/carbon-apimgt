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
  
    private List<OperationPolicyDTO> in = new ArrayList<OperationPolicyDTO>();
    private List<OperationPolicyDTO> out = new ArrayList<OperationPolicyDTO>();

  /**
   **/
  public APIOperationPoliciesDTO in(List<OperationPolicyDTO> in) {
    this.in = in;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("in")
  public List<OperationPolicyDTO> getIn() {
    return in;
  }
  public void setIn(List<OperationPolicyDTO> in) {
    this.in = in;
  }

  /**
   **/
  public APIOperationPoliciesDTO out(List<OperationPolicyDTO> out) {
    this.out = out;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("out")
  public List<OperationPolicyDTO> getOut() {
    return out;
  }
  public void setOut(List<OperationPolicyDTO> out) {
    this.out = out;
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
    return Objects.equals(in, apIOperationPolicies.in) &&
        Objects.equals(out, apIOperationPolicies.out);
  }

  @Override
  public int hashCode() {
    return Objects.hash(in, out);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIOperationPoliciesDTO {\n");
    
    sb.append("    in: ").append(toIndentedString(in)).append("\n");
    sb.append("    out: ").append(toIndentedString(out)).append("\n");
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

