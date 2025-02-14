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



public class OrganizationPoliciesDTO   {
  
    private String organizationID = null;
    private List<String> policies = new ArrayList<String>();

  /**
   **/
  public OrganizationPoliciesDTO organizationID(String organizationID) {
    this.organizationID = organizationID;
    return this;
  }

  
  @ApiModelProperty(example = "36c87e00-57f0-4000-9f97-b379acc4e577", required = true, value = "")
  @JsonProperty("organizationID")
  @NotNull
  public String getOrganizationID() {
    return organizationID;
  }
  public void setOrganizationID(String organizationID) {
    this.organizationID = organizationID;
  }

  /**
   **/
  public OrganizationPoliciesDTO policies(List<String> policies) {
    this.policies = policies;
    return this;
  }

  
  @ApiModelProperty(example = "[\"Silver\",\"Gold\",\"Unlimited\"]", value = "")
  @JsonProperty("policies")
  public List<String> getPolicies() {
    return policies;
  }
  public void setPolicies(List<String> policies) {
    this.policies = policies;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    OrganizationPoliciesDTO organizationPolicies = (OrganizationPoliciesDTO) o;
    return Objects.equals(organizationID, organizationPolicies.organizationID) &&
        Objects.equals(policies, organizationPolicies.policies);
  }

  @Override
  public int hashCode() {
    return Objects.hash(organizationID, policies);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class OrganizationPoliciesDTO {\n");
    
    sb.append("    organizationID: ").append(toIndentedString(organizationID)).append("\n");
    sb.append("    policies: ").append(toIndentedString(policies)).append("\n");
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

