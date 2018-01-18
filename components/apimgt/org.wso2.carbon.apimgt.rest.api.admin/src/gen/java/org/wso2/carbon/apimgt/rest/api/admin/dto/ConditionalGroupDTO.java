package org.wso2.carbon.apimgt.rest.api.admin.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottleConditionDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.ThrottleLimitDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ConditionalGroupDTO  {
  
  
  
  private String description = null;
  
  @NotNull
  private List<ThrottleConditionDTO> conditions = new ArrayList<ThrottleConditionDTO>();
  
  @NotNull
  private ThrottleLimitDTO limit = null;

  
  /**
   * Description of the Conditional Group
   **/
  @ApiModelProperty(value = "Description of the Conditional Group")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  
  /**
   * Individual throttling conditions. They can be defined as either HeaderCondition, IPCondition, JWTClaimsCondition, QueryParameterCondition\nPlease see schemas of each of those throttling condition in Definitions section.\n
   **/
  @ApiModelProperty(required = true, value = "Individual throttling conditions. They can be defined as either HeaderCondition, IPCondition, JWTClaimsCondition, QueryParameterCondition\nPlease see schemas of each of those throttling condition in Definitions section.\n")
  @JsonProperty("conditions")
  public List<ThrottleConditionDTO> getConditions() {
    return conditions;
  }
  public void setConditions(List<ThrottleConditionDTO> conditions) {
    this.conditions = conditions;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("limit")
  public ThrottleLimitDTO getLimit() {
    return limit;
  }
  public void setLimit(ThrottleLimitDTO limit) {
    this.limit = limit;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ConditionalGroupDTO {\n");
    
    sb.append("  description: ").append(description).append("\n");
    sb.append("  conditions: ").append(conditions).append("\n");
    sb.append("  limit: ").append(limit).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
