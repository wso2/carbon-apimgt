package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class APIThreatProtectionPoliciesListDTO  {
  
  
  
  private String policyId = null;
  
  
  private Integer priority = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("policyId")
  public String getPolicyId() {
    return policyId;
  }
  public void setPolicyId(String policyId) {
    this.policyId = policyId;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("priority")
  public Integer getPriority() {
    return priority;
  }
  public void setPriority(Integer priority) {
    this.priority = priority;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIThreatProtectionPoliciesListDTO {\n");
    
    sb.append("  policyId: ").append(policyId).append("\n");
    sb.append("  priority: ").append(priority).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
