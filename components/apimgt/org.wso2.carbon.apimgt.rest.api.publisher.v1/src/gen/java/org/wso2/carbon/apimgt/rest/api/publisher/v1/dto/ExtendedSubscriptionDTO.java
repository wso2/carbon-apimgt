package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SubscriptionDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class ExtendedSubscriptionDTO extends SubscriptionDTO {
  
  
  
  private String workflowId = null;

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("workflowId")
  public String getWorkflowId() {
    return workflowId;
  }
  public void setWorkflowId(String workflowId) {
    this.workflowId = workflowId;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class ExtendedSubscriptionDTO {\n");
    sb.append("  " + super.toString()).append("\n");
    sb.append("  workflowId: ").append(workflowId).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
