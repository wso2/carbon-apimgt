package org.wso2.carbon.apimgt.rest.api.publisher.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ThrottleConditionDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ThrottleLimitDTO;

import io.swagger.annotations.*;
import org.codehaus.jackson.annotate.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ConditionalGroupDTO  {
  
  
  
  private Integer id = null;
  
  
  private Boolean enabled = null;
  
  
  private String description = null;
  
  
  private List<ThrottleConditionDTO> conditions = new ArrayList<ThrottleConditionDTO>();
  
  
  private ThrottleLimitDTO limit = null;

  
  /**
   **/
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
  @ApiModelProperty(value = "")
  @JsonProperty("enabled")
  public Boolean getEnabled() {
    return enabled;
  }
  public void setEnabled(Boolean enabled) {
    this.enabled = enabled;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("conditions")
  public List<ThrottleConditionDTO> getConditions() {
    return conditions;
  }
  public void setConditions(List<ThrottleConditionDTO> conditions) {
    this.conditions = conditions;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
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
    
    sb.append("  id: ").append(id).append("\n");
    sb.append("  enabled: ").append(enabled).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("  conditions: ").append(conditions).append("\n");
    sb.append("  limit: ").append(limit).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
