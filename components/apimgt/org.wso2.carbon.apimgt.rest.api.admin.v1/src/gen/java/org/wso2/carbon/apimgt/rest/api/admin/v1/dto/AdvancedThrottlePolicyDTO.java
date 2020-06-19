package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ConditionalGroupDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ThrottleLimitDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ThrottlePolicyDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class AdvancedThrottlePolicyDTO extends ThrottlePolicyDTO  {
  
    private ThrottleLimitDTO defaultLimit = null;
    private List<ConditionalGroupDTO> conditionalGroups = new ArrayList<>();

  /**
   **/
  public AdvancedThrottlePolicyDTO defaultLimit(ThrottleLimitDTO defaultLimit) {
    this.defaultLimit = defaultLimit;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("defaultLimit")
  @NotNull
  public ThrottleLimitDTO getDefaultLimit() {
    return defaultLimit;
  }
  public void setDefaultLimit(ThrottleLimitDTO defaultLimit) {
    this.defaultLimit = defaultLimit;
  }

  /**
   * Group of conditions which allow adding different parameter conditions to the throttling limit. 
   **/
  public AdvancedThrottlePolicyDTO conditionalGroups(List<ConditionalGroupDTO> conditionalGroups) {
    this.conditionalGroups = conditionalGroups;
    return this;
  }

  
  @ApiModelProperty(value = "Group of conditions which allow adding different parameter conditions to the throttling limit. ")
  @JsonProperty("conditionalGroups")
  public List<ConditionalGroupDTO> getConditionalGroups() {
    return conditionalGroups;
  }
  public void setConditionalGroups(List<ConditionalGroupDTO> conditionalGroups) {
    this.conditionalGroups = conditionalGroups;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AdvancedThrottlePolicyDTO advancedThrottlePolicy = (AdvancedThrottlePolicyDTO) o;
    return Objects.equals(defaultLimit, advancedThrottlePolicy.defaultLimit) &&
        Objects.equals(conditionalGroups, advancedThrottlePolicy.conditionalGroups);
  }

  @Override
  public int hashCode() {
    return Objects.hash(defaultLimit, conditionalGroups);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AdvancedThrottlePolicyDTO {\n");
    sb.append("    ").append(toIndentedString(super.toString())).append("\n");
    sb.append("    defaultLimit: ").append(toIndentedString(defaultLimit)).append("\n");
    sb.append("    conditionalGroups: ").append(toIndentedString(conditionalGroups)).append("\n");
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

