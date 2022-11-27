package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ConditionalGroupDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ThrottleLimitDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class AdvancedThrottlePolicyAllOfDTO   {
  
    private ThrottleLimitDTO defaultLimit = null;
    private List<ConditionalGroupDTO> conditionalGroups = new ArrayList<ConditionalGroupDTO>();

  /**
   **/
  public AdvancedThrottlePolicyAllOfDTO defaultLimit(ThrottleLimitDTO defaultLimit) {
    this.defaultLimit = defaultLimit;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
      @Valid
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
  public AdvancedThrottlePolicyAllOfDTO conditionalGroups(List<ConditionalGroupDTO> conditionalGroups) {
    this.conditionalGroups = conditionalGroups;
    return this;
  }

  
  @ApiModelProperty(value = "Group of conditions which allow adding different parameter conditions to the throttling limit. ")
      @Valid
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
    AdvancedThrottlePolicyAllOfDTO advancedThrottlePolicyAllOf = (AdvancedThrottlePolicyAllOfDTO) o;
    return Objects.equals(defaultLimit, advancedThrottlePolicyAllOf.defaultLimit) &&
        Objects.equals(conditionalGroups, advancedThrottlePolicyAllOf.conditionalGroups);
  }

  @Override
  public int hashCode() {
    return Objects.hash(defaultLimit, conditionalGroups);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AdvancedThrottlePolicyAllOfDTO {\n");
    
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

