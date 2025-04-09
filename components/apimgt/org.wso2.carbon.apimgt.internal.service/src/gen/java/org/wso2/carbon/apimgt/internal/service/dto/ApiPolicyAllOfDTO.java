package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.internal.service.dto.ApiPolicyConditionGroupDTO;
import org.wso2.carbon.apimgt.internal.service.dto.ThrottleLimitDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class ApiPolicyAllOfDTO   {
  
    private List<ApiPolicyConditionGroupDTO> conditionGroups = new ArrayList<>();
    private String applicableLevel = null;
    private ThrottleLimitDTO defaultLimit = null;

  /**
   **/
  public ApiPolicyAllOfDTO conditionGroups(List<ApiPolicyConditionGroupDTO> conditionGroups) {
    this.conditionGroups = conditionGroups;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("conditionGroups")
  public List<ApiPolicyConditionGroupDTO> getConditionGroups() {
    return conditionGroups;
  }
  public void setConditionGroups(List<ApiPolicyConditionGroupDTO> conditionGroups) {
    this.conditionGroups = conditionGroups;
  }

  /**
   **/
  public ApiPolicyAllOfDTO applicableLevel(String applicableLevel) {
    this.applicableLevel = applicableLevel;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("applicableLevel")
  public String getApplicableLevel() {
    return applicableLevel;
  }
  public void setApplicableLevel(String applicableLevel) {
    this.applicableLevel = applicableLevel;
  }

  /**
   **/
  public ApiPolicyAllOfDTO defaultLimit(ThrottleLimitDTO defaultLimit) {
    this.defaultLimit = defaultLimit;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("defaultLimit")
  public ThrottleLimitDTO getDefaultLimit() {
    return defaultLimit;
  }
  public void setDefaultLimit(ThrottleLimitDTO defaultLimit) {
    this.defaultLimit = defaultLimit;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiPolicyAllOfDTO apiPolicyAllOf = (ApiPolicyAllOfDTO) o;
    return Objects.equals(conditionGroups, apiPolicyAllOf.conditionGroups) &&
        Objects.equals(applicableLevel, apiPolicyAllOf.applicableLevel) &&
        Objects.equals(defaultLimit, apiPolicyAllOf.defaultLimit);
  }

  @Override
  public int hashCode() {
    return Objects.hash(conditionGroups, applicableLevel, defaultLimit);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiPolicyAllOfDTO {\n");
    
    sb.append("    conditionGroups: ").append(toIndentedString(conditionGroups)).append("\n");
    sb.append("    applicableLevel: ").append(toIndentedString(applicableLevel)).append("\n");
    sb.append("    defaultLimit: ").append(toIndentedString(defaultLimit)).append("\n");
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

