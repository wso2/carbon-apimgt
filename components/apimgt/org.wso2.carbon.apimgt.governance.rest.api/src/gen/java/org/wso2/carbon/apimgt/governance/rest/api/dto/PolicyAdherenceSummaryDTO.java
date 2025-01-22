package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

/**
 * Summary of governance policy adherence in the organization.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Summary of governance policy adherence in the organization.")

public class PolicyAdherenceSummaryDTO   {
  
    private Integer totalPolicies = null;
    private Integer followedPolicies = null;
    private Integer violatedPolicies = null;
    private Integer unAppliedPolicies = null;

  /**
   * Total number of policies.
   **/
  public PolicyAdherenceSummaryDTO totalPolicies(Integer totalPolicies) {
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
  public PolicyAdherenceSummaryDTO followedPolicies(Integer followedPolicies) {
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
  public PolicyAdherenceSummaryDTO violatedPolicies(Integer violatedPolicies) {
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
   * Number of policies unApplied.
   **/
  public PolicyAdherenceSummaryDTO unAppliedPolicies(Integer unAppliedPolicies) {
    this.unAppliedPolicies = unAppliedPolicies;
    return this;
  }

  
  @ApiModelProperty(example = "0", value = "Number of policies unApplied.")
  @JsonProperty("unAppliedPolicies")
  public Integer getUnAppliedPolicies() {
    return unAppliedPolicies;
  }
  public void setUnAppliedPolicies(Integer unAppliedPolicies) {
    this.unAppliedPolicies = unAppliedPolicies;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    PolicyAdherenceSummaryDTO policyAdherenceSummary = (PolicyAdherenceSummaryDTO) o;
    return Objects.equals(totalPolicies, policyAdherenceSummary.totalPolicies) &&
        Objects.equals(followedPolicies, policyAdherenceSummary.followedPolicies) &&
        Objects.equals(violatedPolicies, policyAdherenceSummary.violatedPolicies) &&
        Objects.equals(unAppliedPolicies, policyAdherenceSummary.unAppliedPolicies);
  }

  @Override
  public int hashCode() {
    return Objects.hash(totalPolicies, followedPolicies, violatedPolicies, unAppliedPolicies);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PolicyAdherenceSummaryDTO {\n");
    
    sb.append("    totalPolicies: ").append(toIndentedString(totalPolicies)).append("\n");
    sb.append("    followedPolicies: ").append(toIndentedString(followedPolicies)).append("\n");
    sb.append("    violatedPolicies: ").append(toIndentedString(violatedPolicies)).append("\n");
    sb.append("    unAppliedPolicies: ").append(toIndentedString(unAppliedPolicies)).append("\n");
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

