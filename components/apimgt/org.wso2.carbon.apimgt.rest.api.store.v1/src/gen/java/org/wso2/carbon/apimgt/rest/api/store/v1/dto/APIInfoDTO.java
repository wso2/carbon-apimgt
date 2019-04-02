package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.BaseAPIInfoDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class APIInfoDTO extends BaseAPIInfoDTO {
  
  
  
  private String lifeCycleStatus = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("lifeCycleStatus")
  public String getLifeCycleStatus() {
    return lifeCycleStatus;
  }
  public void setLifeCycleStatus(String lifeCycleStatus) {
    this.lifeCycleStatus = lifeCycleStatus;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class APIInfoDTO {\n");
    sb.append("  " + super.toString()).append("\n");
    sb.append("  lifeCycleStatus: ").append(lifeCycleStatus).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
