package org.wso2.carbon.throttle.service.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;



@ApiModel(description = "")
public class ThrottledEventDTO  {
  
  
  
  private String throttleKey = null;
  
  
  private String lastUpdatedTime = null;
  
  
  private String throttleState = null;

  
  /**
   * throttle key.
   **/
  @ApiModelProperty(value = "throttle key.")
  @JsonProperty("throttleKey")
  public String getThrottleKey() {
    return throttleKey;
  }
  public void setThrottleKey(String throttleKey) {
    this.throttleKey = throttleKey;
  }

  
  /**
   * Last time decision updated.
   **/
  @ApiModelProperty(value = "Last time decision updated.")
  @JsonProperty("lastUpdatedTime")
  public String getLastUpdatedTime() {
    return lastUpdatedTime;
  }
  public void setLastUpdatedTime(String lastUpdatedTime) {
    this.lastUpdatedTime = lastUpdatedTime;
  }

  
  /**
   * throttle state.
   **/
  @ApiModelProperty(value = "throttle state.")
  @JsonProperty("throttleState")
  public String getThrottleState() {
    return throttleState;
  }
  public void setThrottleState(String throttleState) {
    this.throttleState = throttleState;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ThrottledEventDTO {\n");
    
    sb.append("  throttleKey: ").append(throttleKey).append("\n");
    sb.append("  lastUpdatedTime: ").append(lastUpdatedTime).append("\n");
    sb.append("  throttleState: ").append(throttleState).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
