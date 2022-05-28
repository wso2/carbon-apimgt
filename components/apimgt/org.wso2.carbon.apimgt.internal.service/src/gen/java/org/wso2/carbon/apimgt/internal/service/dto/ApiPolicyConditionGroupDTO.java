package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.internal.service.dto.ConditionDTO;
import org.wso2.carbon.apimgt.internal.service.dto.ThrottleLimitDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class ApiPolicyConditionGroupDTO   {
  
    private Integer policyId = null;
    private String quotaType = null;
    private Integer conditionGroupId = null;
    private List<ConditionDTO> condition = new ArrayList<>();
    private ThrottleLimitDTO defaultLimit = null;

  /**
   **/
  public ApiPolicyConditionGroupDTO policyId(Integer policyId) {
    this.policyId = policyId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("policyId")
  public Integer getPolicyId() {
    return policyId;
  }
  public void setPolicyId(Integer policyId) {
    this.policyId = policyId;
  }

  /**
   **/
  public ApiPolicyConditionGroupDTO quotaType(String quotaType) {
    this.quotaType = quotaType;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("quotaType")
  public String getQuotaType() {
    return quotaType;
  }
  public void setQuotaType(String quotaType) {
    this.quotaType = quotaType;
  }

  /**
   **/
  public ApiPolicyConditionGroupDTO conditionGroupId(Integer conditionGroupId) {
    this.conditionGroupId = conditionGroupId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("conditionGroupId")
  public Integer getConditionGroupId() {
    return conditionGroupId;
  }
  public void setConditionGroupId(Integer conditionGroupId) {
    this.conditionGroupId = conditionGroupId;
  }

  /**
   **/
  public ApiPolicyConditionGroupDTO condition(List<ConditionDTO> condition) {
    this.condition = condition;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("condition")
  public List<ConditionDTO> getCondition() {
    return condition;
  }
  public void setCondition(List<ConditionDTO> condition) {
    this.condition = condition;
  }

  /**
   **/
  public ApiPolicyConditionGroupDTO defaultLimit(ThrottleLimitDTO defaultLimit) {
    this.defaultLimit = defaultLimit;
    return this;
  }

  
  @ApiModelProperty(value = "")
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
    ApiPolicyConditionGroupDTO apiPolicyConditionGroup = (ApiPolicyConditionGroupDTO) o;
    return Objects.equals(policyId, apiPolicyConditionGroup.policyId) &&
        Objects.equals(quotaType, apiPolicyConditionGroup.quotaType) &&
        Objects.equals(conditionGroupId, apiPolicyConditionGroup.conditionGroupId) &&
        Objects.equals(condition, apiPolicyConditionGroup.condition) &&
        Objects.equals(defaultLimit, apiPolicyConditionGroup.defaultLimit);
  }

  @Override
  public int hashCode() {
    return Objects.hash(policyId, quotaType, conditionGroupId, condition, defaultLimit);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiPolicyConditionGroupDTO {\n");
    
    sb.append("    policyId: ").append(toIndentedString(policyId)).append("\n");
    sb.append("    quotaType: ").append(toIndentedString(quotaType)).append("\n");
    sb.append("    conditionGroupId: ").append(toIndentedString(conditionGroupId)).append("\n");
    sb.append("    condition: ").append(toIndentedString(condition)).append("\n");
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

