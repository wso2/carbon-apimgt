package org.wso2.carbon.apimgt.micro.gateway.api.synchronizer.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


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
