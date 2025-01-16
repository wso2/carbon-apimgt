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

public class GovernanceSummaryForPoliciesDTO   {
  
    private Integer total = null;
    private Integer followed = null;
    private Integer violated = null;
    private Integer unapplied = null;

  /**
   * Total number of policies.
   **/
  public GovernanceSummaryForPoliciesDTO total(Integer total) {
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
  public GovernanceSummaryForPoliciesDTO followed(Integer followed) {
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
  public GovernanceSummaryForPoliciesDTO violated(Integer violated) {
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
   * Number of policies unapplied.
   **/
  public GovernanceSummaryForPoliciesDTO unapplied(Integer unapplied) {
    this.unapplied = unapplied;
    return this;
  }

  
  @ApiModelProperty(example = "0", value = "Number of policies unapplied.")
  @JsonProperty("unapplied")
  public Integer getUnapplied() {
    return unapplied;
  }
  public void setUnapplied(Integer unapplied) {
    this.unapplied = unapplied;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    GovernanceSummaryForPoliciesDTO governanceSummaryForPolicies = (GovernanceSummaryForPoliciesDTO) o;
    return Objects.equals(total, governanceSummaryForPolicies.total) &&
        Objects.equals(followed, governanceSummaryForPolicies.followed) &&
        Objects.equals(violated, governanceSummaryForPolicies.violated) &&
        Objects.equals(unapplied, governanceSummaryForPolicies.unapplied);
  }

  @Override
  public int hashCode() {
    return Objects.hash(total, followed, violated, unapplied);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GovernanceSummaryForPoliciesDTO {\n");
    
    sb.append("    total: ").append(toIndentedString(total)).append("\n");
    sb.append("    followed: ").append(toIndentedString(followed)).append("\n");
    sb.append("    violated: ").append(toIndentedString(violated)).append("\n");
    sb.append("    unapplied: ").append(toIndentedString(unapplied)).append("\n");
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

