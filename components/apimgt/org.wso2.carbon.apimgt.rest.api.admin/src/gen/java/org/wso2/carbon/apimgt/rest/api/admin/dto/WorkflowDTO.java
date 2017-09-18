package org.wso2.carbon.apimgt.rest.api.admin.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * WorkflowDTO
 */
public class WorkflowDTO   {
  @JsonProperty("referenceId")
  private String referenceId = null;

  @JsonProperty("type")
  private String type = null;

  @JsonProperty("description")
  private String description = null;

  @JsonProperty("createdTime")
  private String createdTime = null;

  @JsonProperty("workflowStatus")
  private String workflowStatus = null;

  public WorkflowDTO referenceId(String referenceId) {
    this.referenceId = referenceId;
    return this;
  }

   /**
   * Get referenceId
   * @return referenceId
  **/
  @ApiModelProperty(example = "12225615-0c99-43db-b691-c7e1bc23e149", value = "")
  public String getReferenceId() {
    return referenceId;
  }

  public void setReferenceId(String referenceId) {
    this.referenceId = referenceId;
  }

  public WorkflowDTO type(String type) {
    this.type = type;
    return this;
  }

   /**
   * Get type
   * @return type
  **/
  @ApiModelProperty(example = "AM_APPLICATION_CREATION", value = "")
  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public WorkflowDTO description(String description) {
    this.description = description;
    return this;
  }

   /**
   * Get description
   * @return description
  **/
  @ApiModelProperty(example = "Application created by foo", value = "")
  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public WorkflowDTO createdTime(String createdTime) {
    this.createdTime = createdTime;
    return this;
  }

   /**
   * Get createdTime
   * @return createdTime
  **/
  @ApiModelProperty(example = "CREATED", value = "")
  public String getCreatedTime() {
    return createdTime;
  }

  public void setCreatedTime(String createdTime) {
    this.createdTime = createdTime;
  }

  public WorkflowDTO workflowStatus(String workflowStatus) {
    this.workflowStatus = workflowStatus;
    return this;
  }

   /**
   * Get workflowStatus
   * @return workflowStatus
  **/
  @ApiModelProperty(example = "APPROVED", value = "")
  public String getWorkflowStatus() {
    return workflowStatus;
  }

  public void setWorkflowStatus(String workflowStatus) {
    this.workflowStatus = workflowStatus;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WorkflowDTO workflow = (WorkflowDTO) o;
    return Objects.equals(this.referenceId, workflow.referenceId) &&
        Objects.equals(this.type, workflow.type) &&
        Objects.equals(this.description, workflow.description) &&
        Objects.equals(this.createdTime, workflow.createdTime) &&
        Objects.equals(this.workflowStatus, workflow.workflowStatus);
  }

  @Override
  public int hashCode() {
    return Objects.hash(referenceId, type, description, createdTime, workflowStatus);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WorkflowDTO {\n");
    
    sb.append("    referenceId: ").append(toIndentedString(referenceId)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
    sb.append("    description: ").append(toIndentedString(description)).append("\n");
    sb.append("    createdTime: ").append(toIndentedString(createdTime)).append("\n");
    sb.append("    workflowStatus: ").append(toIndentedString(workflowStatus)).append("\n");
    sb.append("}");
    return sb.toString();
  }

  /**
   * Convert the given object to string with each line indented by 4 spaces
   * (except the first line).
   */
  private String toIndentedString(java.lang.Object o) {
    if (o == null) {
      return "null";
    }
    return o.toString().replace("\n", "\n    ");
  }
}

