package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

/**
 * Summary of governance policy compliance.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Summary of governance policy compliance.")

public class PolicyGovernanceResultsSummaryDTO   {
  
    private Integer totalPolicies = null;
    private Integer followedPolicies = null;
    private Integer violatedPolicies = null;
    private Integer unappliedPolicies = null;

  /**
   * Total number of policies.
   **/
  public PolicyGovernanceResultsSummaryDTO totalPolicies(Integer totalPolicies) {
    this.totalPolicies = totalPolicies;
    return this;
  }

  
  @ApiModelProperty(example = "10", value = "Total number of policies.")
  @JsonProperty("totalPolicies")
  public Integer getTotalPolicies() {
    return totalPolicies;
  }
  public void setTotalPolicies(Integer totalPolicies) {
    this.totalPolicies = totalPolicies;
  }

  /**
   * Number of policies followed to.
   **/
  public PolicyGovernanceResultsSummaryDTO followedPolicies(Integer followedPolicies) {
    this.followedPolicies = followedPolicies;
    return this;
  }

  
  @ApiModelProperty(example = "6", value = "Number of policies followed to.")
  @JsonProperty("followedPolicies")
  public Integer getFollowedPolicies() {
    return followedPolicies;
  }
  public void setFollowedPolicies(Integer followedPolicies) {
    this.followedPolicies = followedPolicies;
  }

  /**
   * Number of policies violated.
   **/
  public PolicyGovernanceResultsSummaryDTO violatedPolicies(Integer violatedPolicies) {
    this.violatedPolicies = violatedPolicies;
    return this;
  }

  
  @ApiModelProperty(example = "4", value = "Number of policies violated.")
  @JsonProperty("violatedPolicies")
  public Integer getViolatedPolicies() {
    return violatedPolicies;
  }
  public void setViolatedPolicies(Integer violatedPolicies) {
    this.violatedPolicies = violatedPolicies;
  }

  /**
   * Number of policies unapplied.
   **/
  public PolicyGovernanceResultsSummaryDTO unappliedPolicies(Integer unappliedPolicies) {
    this.unappliedPolicies = unappliedPolicies;
    return this;
  }

  
  @ApiModelProperty(example = "0", value = "Number of policies unapplied.")
  @JsonProperty("unappliedPolicies")
  public Integer getUnappliedPolicies() {
    return unappliedPolicies;
  }
  public void setUnappliedPolicies(Integer unappliedPolicies) {
    this.unappliedPolicies = unappliedPolicies;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PolicyGovernanceResultsSummaryDTO policyGovernanceResultsSummary = (PolicyGovernanceResultsSummaryDTO) o;
    return Objects.equals(totalPolicies, policyGovernanceResultsSummary.totalPolicies) &&
        Objects.equals(followedPolicies, policyGovernanceResultsSummary.followedPolicies) &&
        Objects.equals(violatedPolicies, policyGovernanceResultsSummary.violatedPolicies) &&
        Objects.equals(unappliedPolicies, policyGovernanceResultsSummary.unappliedPolicies);
  }

  @Override
  public int hashCode() {
    return Objects.hash(totalPolicies, followedPolicies, violatedPolicies, unappliedPolicies);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PolicyGovernanceResultsSummaryDTO {\n");
    
    sb.append("    totalPolicies: ").append(toIndentedString(totalPolicies)).append("\n");
    sb.append("    followedPolicies: ").append(toIndentedString(followedPolicies)).append("\n");
    sb.append("    violatedPolicies: ").append(toIndentedString(violatedPolicies)).append("\n");
    sb.append("    unappliedPolicies: ").append(toIndentedString(unappliedPolicies)).append("\n");
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

