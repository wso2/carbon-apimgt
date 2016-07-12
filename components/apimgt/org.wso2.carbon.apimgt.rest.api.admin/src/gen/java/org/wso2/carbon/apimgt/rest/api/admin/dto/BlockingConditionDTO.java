package org.wso2.carbon.apimgt.rest.api.admin.dto;

import io.swagger.annotations.ApiModel;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;



/**
 * Blocking Conditions
 **/


@ApiModel(description = "Blocking Conditions")
public class BlockingConditionDTO  {
  
  
  
  private String conditionId = null;
  
  @NotNull
  private String conditionType = null;
  
  @NotNull
  private String conditionValue = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("conditionId")
  public String getConditionId() {
    return conditionId;
  }
  public void setConditionId(String conditionId) {
    this.conditionId = conditionId;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("conditionType")
  public String getConditionType() {
    return conditionType;
  }
  public void setConditionType(String conditionType) {
    this.conditionType = conditionType;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("conditionValue")
  public String getConditionValue() {
    return conditionValue;
  }
  public void setConditionValue(String conditionValue) {
    this.conditionValue = conditionValue;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class BlockingConditionDTO {\n");
    
    sb.append("  conditionId: ").append(conditionId).append("\n");
    sb.append("  conditionType: ").append(conditionType).append("\n");
    sb.append("  conditionValue: ").append(conditionValue).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
