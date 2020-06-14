package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.internal.service.dto.ApiPolicyConditionGroupDTO;
import org.wso2.carbon.apimgt.internal.service.dto.PolicyDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class ApiPolicyDTO   {
  
    private Integer id = null;
    private Integer tenantId = null;
    private String name = null;
    private String quotaType = null;
    private List<ApiPolicyConditionGroupDTO> conditionGroups = new ArrayList<>();
    private String applicableLevel = null;

  /**
   **/
  public ApiPolicyDTO id(Integer id) {
    this.id = id;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("id")
  public Integer getId() {
    return id;
  }
  public void setId(Integer id) {
    this.id = id;
  }

  /**
   **/
  public ApiPolicyDTO tenantId(Integer tenantId) {
    this.tenantId = tenantId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("tenantId")
  public Integer getTenantId() {
    return tenantId;
  }
  public void setTenantId(Integer tenantId) {
    this.tenantId = tenantId;
  }

  /**
   **/
  public ApiPolicyDTO name(String name) {
    this.name = name;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("name")
  public String getName() {
    return name;
  }
  public void setName(String name) {
    this.name = name;
  }

  /**
   **/
  public ApiPolicyDTO quotaType(String quotaType) {
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
  public ApiPolicyDTO conditionGroups(List<ApiPolicyConditionGroupDTO> conditionGroups) {
    this.conditionGroups = conditionGroups;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("conditionGroups")
  public List<ApiPolicyConditionGroupDTO> getConditionGroups() {
    return conditionGroups;
  }
  public void setConditionGroups(List<ApiPolicyConditionGroupDTO> conditionGroups) {
    this.conditionGroups = conditionGroups;
  }

  /**
   **/
  public ApiPolicyDTO applicableLevel(String applicableLevel) {
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


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiPolicyDTO apiPolicy = (ApiPolicyDTO) o;
    return Objects.equals(id, apiPolicy.id) &&
        Objects.equals(tenantId, apiPolicy.tenantId) &&
        Objects.equals(name, apiPolicy.name) &&
        Objects.equals(quotaType, apiPolicy.quotaType) &&
        Objects.equals(conditionGroups, apiPolicy.conditionGroups) &&
        Objects.equals(applicableLevel, apiPolicy.applicableLevel);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id, tenantId, name, quotaType, conditionGroups, applicableLevel);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiPolicyDTO {\n");
    
    sb.append("    id: ").append(toIndentedString(id)).append("\n");
    sb.append("    tenantId: ").append(toIndentedString(tenantId)).append("\n");
    sb.append("    name: ").append(toIndentedString(name)).append("\n");
    sb.append("    quotaType: ").append(toIndentedString(quotaType)).append("\n");
    sb.append("    conditionGroups: ").append(toIndentedString(conditionGroups)).append("\n");
    sb.append("    applicableLevel: ").append(toIndentedString(applicableLevel)).append("\n");
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

