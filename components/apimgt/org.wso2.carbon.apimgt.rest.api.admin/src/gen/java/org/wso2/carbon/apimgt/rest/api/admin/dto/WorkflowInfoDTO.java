package org.wso2.carbon.apimgt.rest.api.admin.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;


@ApiModel(description = "")
public class WorkflowInfoDTO  {
  
  
  public enum WorkflowTypeEnum {
     AM_APPLICATION_CREATION,  AM_SUBSCRIPTION_CREATION,  AM_USER_SIGNUP,  AM_APPLICATION_REGISTRATION_PRODUCTION,  AM_APPLICATION_REGISTRATION_SANDBOX,  AM_APPLICATION_DELETION,  AM_API_STATE,  AM_SUBSCRIPTION_DELETION, 
  };
  
  private WorkflowTypeEnum workflowType = null;
  
  public enum WorkflowStatusEnum {
     APPROVED,  CREATED, 
  };
  
  private WorkflowStatusEnum workflowStatus = null;
  
  
  private String createdTime = null;
  
  
  private String updatedTime = null;
  
  
  private String referenceId = null;
  
  
  private Object properties = null;
  
  
  private String description = null;

  
  /**
   * Type of the Workflow Request. It shows which type of request is it.\n
   **/
  @ApiModelProperty(value = "Type of the Workflow Request. It shows which type of request is it.\n")
  @JsonProperty("workflowType")
  public WorkflowTypeEnum getWorkflowType() {
    return workflowType;
  }
  public void setWorkflowType(WorkflowTypeEnum workflowType) {
    this.workflowType = workflowType;
  }

  
  /**
   * Show the Status of the the workflow request whether it is approved or created.\n
   **/
  @ApiModelProperty(value = "Show the Status of the the workflow request whether it is approved or created.\n")
  @JsonProperty("workflowStatus")
  public WorkflowStatusEnum getWorkflowStatus() {
    return workflowStatus;
  }
  public void setWorkflowStatus(WorkflowStatusEnum workflowStatus) {
    this.workflowStatus = workflowStatus;
  }

  
  /**
   * Time of the the workflow request created.\n
   **/
  @ApiModelProperty(value = "Time of the the workflow request created.\n")
  @JsonProperty("createdTime")
  public String getCreatedTime() {
    return createdTime;
  }
  public void setCreatedTime(String createdTime) {
    this.createdTime = createdTime;
  }

  
  /**
   * Time of the the workflow request updated.\n
   **/
  @ApiModelProperty(value = "Time of the the workflow request updated.\n")
  @JsonProperty("updatedTime")
  public String getUpdatedTime() {
    return updatedTime;
  }
  public void setUpdatedTime(String updatedTime) {
    this.updatedTime = updatedTime;
  }

  
  /**
   * Workflow external reference is used to identify the workflow requests uniquely.\n
   **/
  @ApiModelProperty(value = "Workflow external reference is used to identify the workflow requests uniquely.\n")
  @JsonProperty("referenceId")
  public String getReferenceId() {
    return referenceId;
  }
  public void setReferenceId(String referenceId) {
    this.referenceId = referenceId;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("properties")
  public Object getProperties() {
    return properties;
  }
  public void setProperties(Object properties) {
    this.properties = properties;
  }

  
  /**
   * description is a message with basic details about the workflow request.\n
   **/
  @ApiModelProperty(value = "description is a message with basic details about the workflow request.\n")
  @JsonProperty("description")
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class WorkflowInfoDTO {\n");
    
    sb.append("  workflowType: ").append(workflowType).append("\n");
    sb.append("  workflowStatus: ").append(workflowStatus).append("\n");
    sb.append("  createdTime: ").append(createdTime).append("\n");
    sb.append("  updatedTime: ").append(updatedTime).append("\n");
    sb.append("  referenceId: ").append(referenceId).append("\n");
    sb.append("  properties: ").append(properties).append("\n");
    sb.append("  description: ").append(description).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
