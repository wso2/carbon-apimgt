package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

/**
 * Summary of governance policy attachment adherence in the organization.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "Summary of governance policy attachment adherence in the organization.")

public class PolicyAttachmentAdherenceSummaryDTO   {
  
    private Integer total = null;
    private Integer followed = null;
    private Integer violated = null;
    private Integer unApplied = null;

  /**
   * Total number of policy attachments.
   **/
  public PolicyAttachmentAdherenceSummaryDTO total(Integer total) {
    this.total = total;
    return this;
  }

  
  @ApiModelProperty(example = "10", value = "Total number of policy attachments.")
  @JsonProperty("total")
  public Integer getTotal() {
    return total;
  }
  public void setTotal(Integer total) {
    this.total = total;
  }

  /**
   * Number of policy attachments followed to.
   **/
  public PolicyAttachmentAdherenceSummaryDTO followed(Integer followed) {
    this.followed = followed;
    return this;
  }

  
  @ApiModelProperty(example = "6", value = "Number of policy attachments followed to.")
  @JsonProperty("followed")
  public Integer getFollowed() {
    return followed;
  }
  public void setFollowed(Integer followed) {
    this.followed = followed;
  }

  /**
   * Number of policy attachments violated.
   **/
  public PolicyAttachmentAdherenceSummaryDTO violated(Integer violated) {
    this.violated = violated;
    return this;
  }

  
  @ApiModelProperty(example = "4", value = "Number of policy attachments violated.")
  @JsonProperty("violated")
  public Integer getViolated() {
    return violated;
  }
  public void setViolated(Integer violated) {
    this.violated = violated;
  }

  /**
   * Number of policy attachments unApplied.
   **/
  public PolicyAttachmentAdherenceSummaryDTO unApplied(Integer unApplied) {
    this.unApplied = unApplied;
    return this;
  }

  
  @ApiModelProperty(example = "0", value = "Number of policy attachments unApplied.")
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
    PolicyAttachmentAdherenceSummaryDTO policyAttachmentAdherenceSummary = (PolicyAttachmentAdherenceSummaryDTO) o;
    return Objects.equals(total, policyAttachmentAdherenceSummary.total) &&
        Objects.equals(followed, policyAttachmentAdherenceSummary.followed) &&
        Objects.equals(violated, policyAttachmentAdherenceSummary.violated) &&
        Objects.equals(unApplied, policyAttachmentAdherenceSummary.unApplied);
  }

  @Override
  public int hashCode() {
    return Objects.hash(total, followed, violated, unApplied);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class PolicyAttachmentAdherenceSummaryDTO {\n");
    
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

