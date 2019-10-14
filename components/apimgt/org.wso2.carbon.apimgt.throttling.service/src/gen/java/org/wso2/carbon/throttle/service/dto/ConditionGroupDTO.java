package org.wso2.carbon.throttle.service.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.throttle.service.dto.ConditionDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ConditionGroupDTO  {
  
  
  
  private Integer conditionGroupId = null;
  
  
  private String policyName = null;
  
  
  private Integer tenantId = null;
  
  
  private List<ConditionDTO> conditions = new ArrayList<ConditionDTO>();

  
  /**
   **/
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
  @ApiModelProperty(value = "")
  @JsonProperty("policyName")
  public String getPolicyName() {
    return policyName;
  }
  public void setPolicyName(String policyName) {
    this.policyName = policyName;
  }

  
  /**
   **/
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
  @ApiModelProperty(value = "")
  @JsonProperty("conditions")
  public List<ConditionDTO> getConditions() {
    return conditions;
  }
  public void setConditions(List<ConditionDTO> conditions) {
    this.conditions = conditions;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConditionGroupDTO {\n");
    
    sb.append("  conditionGroupId: ").append(conditionGroupId).append("\n");
    sb.append("  policyName: ").append(policyName).append("\n");
    sb.append("  tenantId: ").append(tenantId).append("\n");
    sb.append("  conditions: ").append(conditions).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
