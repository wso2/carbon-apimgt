package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ConditionalGroupDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.QuotaPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ThrottlePolicyDTO;

import io.swagger.annotations.*;
import org.codehaus.jackson.annotate.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class AdvancedThrottlePolicyDTO extends ThrottlePolicyDTO {
  
  
  public enum UserLevelEnum {
     apiLevel,  userLevel, 
  };
  
  private UserLevelEnum userLevel = null;
  
  
  private List<ConditionalGroupDTO> conditionalGroups = new ArrayList<ConditionalGroupDTO>();

  
  /**
   * Applicable throttling level
   **/
  @ApiModelProperty(value = "Applicable throttling level")
  @JsonProperty("userLevel")
  public UserLevelEnum getUserLevel() {
    return userLevel;
  }
  public void setUserLevel(UserLevelEnum userLevel) {
    this.userLevel = userLevel;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
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
    sb.append("  userLevel: ").append(userLevel).append("\n");
    sb.append("  conditionalGroups: ").append(conditionalGroups).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
