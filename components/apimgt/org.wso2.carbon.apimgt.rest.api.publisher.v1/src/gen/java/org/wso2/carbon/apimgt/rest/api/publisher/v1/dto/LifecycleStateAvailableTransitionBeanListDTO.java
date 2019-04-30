package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class LifecycleStateAvailableTransitionBeanListDTO  {
  
  
  
  private String targetState = null;
  
  
  private String event = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("targetState")
  public String getTargetState() {
    return targetState;
  }
  public void setTargetState(String targetState) {
    this.targetState = targetState;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("event")
  public String getEvent() {
    return event;
  }
  public void setEvent(String event) {
    this.event = event;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class LifecycleStateAvailableTransitionBeanListDTO {\n");
    
    sb.append("  targetState: ").append(targetState).append("\n");
    sb.append("  event: ").append(event).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
