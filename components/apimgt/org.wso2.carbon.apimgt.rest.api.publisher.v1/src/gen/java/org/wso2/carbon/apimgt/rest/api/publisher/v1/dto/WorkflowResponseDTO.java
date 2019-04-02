package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class WorkflowResponseDTO  {
  
  
  public enum WorkflowStatusEnum {
     CREATED,  APPROVED,  REJECTED,  REGISTERED, 
  };
  @NotNull
  private WorkflowStatusEnum workflowStatus = null;
  
  
  private String jsonPayload = null;

  
  /**
   * This attribute declares whether this workflow task is approved or rejected.\n
   **/
  @ApiModelProperty(required = true, value = "This attribute declares whether this workflow task is approved or rejected.\n")
  @JsonProperty("workflowStatus")
  public WorkflowStatusEnum getWorkflowStatus() {
    return workflowStatus;
  }
  public void setWorkflowStatus(WorkflowStatusEnum workflowStatus) {
    this.workflowStatus = workflowStatus;
  }

  
  /**
   * Attributes that returned after the workflow execution\n
   **/
  @ApiModelProperty(value = "Attributes that returned after the workflow execution\n")
  @JsonProperty("jsonPayload")
  public String getJsonPayload() {
    return jsonPayload;
  }
  public void setJsonPayload(String jsonPayload) {
    this.jsonPayload = jsonPayload;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class WorkflowResponseDTO {\n");
    
    sb.append("  workflowStatus: ").append(workflowStatus).append("\n");
    sb.append("  jsonPayload: ").append(jsonPayload).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
