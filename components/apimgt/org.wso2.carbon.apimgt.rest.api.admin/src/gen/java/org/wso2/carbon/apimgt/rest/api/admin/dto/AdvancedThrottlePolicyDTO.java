package org.wso2.carbon.apimgt.rest.api.admin.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ConditionalGroupDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottleLimitDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottlePolicyDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class AdvancedThrottlePolicyDTO extends ThrottlePolicyDTO {
  
  
  
  private ThrottleLimitDTO defaultLimit = null;
  
  
  private List<ConditionalGroupDTO> conditionalGroups = new ArrayList<ConditionalGroupDTO>();

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("defaultLimit")
  public ThrottleLimitDTO getDefaultLimit() {
    return defaultLimit;
  }
  public void setDefaultLimit(ThrottleLimitDTO defaultLimit) {
    this.defaultLimit = defaultLimit;
  }

  
  /**
   * Group of conditions which allow adding different parameter conditions to the throttling limit.\n
   **/
  @ApiModelProperty(value = "Group of conditions which allow adding different parameter conditions to the throttling limit.\n")
  @JsonProperty("conditionalGroups")
  public List<ConditionalGroupDTO> getConditionalGroups() {
    return conditionalGroups;
  }
  public void setConditionalGroups(List<ConditionalGroupDTO> conditionalGroups) {
    this.conditionalGroups = conditionalGroups;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class AdvancedThrottlePolicyDTO {\n");
    sb.append("  " + super.toString()).append("\n");
    sb.append("  defaultLimit: ").append(defaultLimit).append("\n");
    sb.append("  conditionalGroups: ").append(conditionalGroups).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
