package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.governance.rest.api.dto.GovernanceSummaryForPoliciesDTO;
import org.wso2.carbon.apimgt.governance.rest.api.dto.PolicyGovernanceResultInfoDTO;
import javax.validation.constraints.*;

/**
 * Governance results of all policies of an organization.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Governance results of all policies of an organization.")

public class PolicyGovernanceResultsDTO   {
  
    private GovernanceSummaryForPoliciesDTO policyGovernanceSummary = null;
    private List<PolicyGovernanceResultInfoDTO> policyGovernanceResults = new ArrayList<PolicyGovernanceResultInfoDTO>();

  /**
   **/
  public PolicyGovernanceResultsDTO policyGovernanceSummary(GovernanceSummaryForPoliciesDTO policyGovernanceSummary) {
    this.policyGovernanceSummary = policyGovernanceSummary;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("policyGovernanceSummary")
  public GovernanceSummaryForPoliciesDTO getPolicyGovernanceSummary() {
    return policyGovernanceSummary;
  }
  public void setPolicyGovernanceSummary(GovernanceSummaryForPoliciesDTO policyGovernanceSummary) {
    this.policyGovernanceSummary = policyGovernanceSummary;
  }

  /**
   * Governance results for a list of policies.
   **/
  public PolicyGovernanceResultsDTO policyGovernanceResults(List<PolicyGovernanceResultInfoDTO> policyGovernanceResults) {
    this.policyGovernanceResults = policyGovernanceResults;
    return this;
  }

  
  @ApiModelProperty(value = "Governance results for a list of policies.")
      @Valid
  @JsonProperty("policyGovernanceResults")
  public List<PolicyGovernanceResultInfoDTO> getPolicyGovernanceResults() {
    return policyGovernanceResults;
  }
  public void setPolicyGovernanceResults(List<PolicyGovernanceResultInfoDTO> policyGovernanceResults) {
    this.policyGovernanceResults = policyGovernanceResults;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PolicyGovernanceResultsDTO policyGovernanceResults = (PolicyGovernanceResultsDTO) o;
    return Objects.equals(policyGovernanceSummary, policyGovernanceResults.policyGovernanceSummary) &&
        Objects.equals(policyGovernanceResults, policyGovernanceResults.policyGovernanceResults);
  }

  @Override
  public int hashCode() {
    return Objects.hash(policyGovernanceSummary, policyGovernanceResults);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PolicyGovernanceResultsDTO {\n");
    
    sb.append("    policyGovernanceSummary: ").append(toIndentedString(policyGovernanceSummary)).append("\n");
    sb.append("    policyGovernanceResults: ").append(toIndentedString(policyGovernanceResults)).append("\n");
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

