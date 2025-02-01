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
  
    private Integer total = null;
    private Integer followed = null;
    private Integer violated = null;
    private Integer unApplied = null;

  /**
   * Total number of policies.
   **/
  public PolicyAdherenceSummaryDTO total(Integer total) {
    this.total = total;
    return this;
  }

  
  @ApiModelProperty(example = "10", value = "Total number of policies.")
  @JsonProperty("total")
  public Integer getTotal() {
    return total;
  }
  public void setTotal(Integer total) {
    this.total = total;
  }

  /**
   * Number of policies followed to.
   **/
  public PolicyAdherenceSummaryDTO followed(Integer followed) {
    this.followed = followed;
    return this;
  }

  
  @ApiModelProperty(example = "6", value = "Number of policies followed to.")
  @JsonProperty("followed")
  public Integer getFollowed() {
    return followed;
  }
  public void setFollowed(Integer followed) {
    this.followed = followed;
  }

  /**
   * Number of policies violated.
   **/
  public PolicyAdherenceSummaryDTO violated(Integer violated) {
    this.violated = violated;
    return this;
  }

  
  @ApiModelProperty(example = "4", value = "Number of policies violated.")
  @JsonProperty("violated")
  public Integer getViolated() {
    return violated;
  }
  public void setViolated(Integer violated) {
    this.violated = violated;
  }

  /**
   * Number of policies unApplied.
   **/
  public PolicyAdherenceSummaryDTO unApplied(Integer unApplied) {
    this.unApplied = unApplied;
    return this;
  }

  
  @ApiModelProperty(example = "0", value = "Number of policies unApplied.")
  @JsonProperty("unApplied")
  public Integer getUnApplied() {
    return unApplied;
  }
  public void setUnApplied(Integer unApplied) {
    this.unApplied = unApplied;
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
    return Objects.equals(total, policyAdherenceSummary.total) &&
        Objects.equals(followed, policyAdherenceSummary.followed) &&
        Objects.equals(violated, policyAdherenceSummary.violated) &&
        Objects.equals(unApplied, policyAdherenceSummary.unApplied);
  }

  @Override
  public int hashCode() {
    return Objects.hash(total, followed, violated, unApplied);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PolicyAdherenceSummaryDTO {\n");
    
    sb.append("    total: ").append(toIndentedString(total)).append("\n");
    sb.append("    followed: ").append(toIndentedString(followed)).append("\n");
    sb.append("    violated: ").append(toIndentedString(violated)).append("\n");
    sb.append("    unApplied: ").append(toIndentedString(unApplied)).append("\n");
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

