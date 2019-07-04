package org.wso2.carbon.apimgt.rest.api.admin.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class MonetizationUsagePublishInfoDTO  {
  
  
  
  private String state = null;
  
  
  private String status = null;
  
  
  private String startedTime = null;
  
  
  private String lastPublsihedTime = null;

  
  /**
   * State of usage publish job
   **/
  @ApiModelProperty(value = "State of usage publish job")
  @JsonProperty("state")
  public String getState() {
    return state;
  }
  public void setState(String state) {
    this.state = state;
  }

  
  /**
   * Status of usage publish job
   **/
  @ApiModelProperty(value = "Status of usage publish job")
  @JsonProperty("status")
  public String getStatus() {
    return status;
  }
  public void setStatus(String status) {
    this.status = status;
  }

  
  /**
   * Timestamp of the started time of the Job
   **/
  @ApiModelProperty(value = "Timestamp of the started time of the Job")
  @JsonProperty("startedTime")
  public String getStartedTime() {
    return startedTime;
  }
  public void setStartedTime(String startedTime) {
    this.startedTime = startedTime;
  }

  
  /**
   * Timestamp of the last published time
   **/
  @ApiModelProperty(value = "Timestamp of the last published time")
  @JsonProperty("lastPublsihedTime")
  public String getLastPublsihedTime() {
    return lastPublsihedTime;
  }
  public void setLastPublsihedTime(String lastPublsihedTime) {
    this.lastPublsihedTime = lastPublsihedTime;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class MonetizationUsagePublishInfoDTO {\n");
    
    sb.append("  state: ").append(state).append("\n");
    sb.append("  status: ").append(status).append("\n");
    sb.append("  startedTime: ").append(startedTime).append("\n");
    sb.append("  lastPublsihedTime: ").append(lastPublsihedTime).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
