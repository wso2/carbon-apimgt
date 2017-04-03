package org.wso2.carbon.apimgt.rest.api.store.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * WorkflowResponseDTO
 */
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-03-31T11:27:01.517+05:30")
public class WorkflowResponseDTO   {
  /**
   * This attribute declares whether this workflow task is approved or rejected. 
   */
  public enum WorkflowStatusEnum {
    CREATED("CREATED"),
    
    APPROVED("APPROVED"),
    
    REJECTED("REJECTED"),
    
    REGISTERED("REGISTERED");

    private String value;

    WorkflowStatusEnum(String value) {
      this.value = value;
    }

    @Override
    @JsonValue
    public String toString() {
      return String.valueOf(value);
    }

    @JsonCreator
    public static WorkflowStatusEnum fromValue(String text) {
      for (WorkflowStatusEnum b : WorkflowStatusEnum.values()) {
        if (String.valueOf(b.value).equals(text)) {
          return b;
        }
      }
      return null;
    }
  }

  @JsonProperty("workflowStatus")
  private WorkflowStatusEnum workflowStatus = null;

  @JsonProperty("jsonPayload")
  private String jsonPayload = null;

  public WorkflowResponseDTO workflowStatus(WorkflowStatusEnum workflowStatus) {
    this.workflowStatus = workflowStatus;
    return this;
  }

   /**
   * This attribute declares whether this workflow task is approved or rejected. 
   * @return workflowStatus
  **/
  @ApiModelProperty(example = "APPROVED", required = true, value = "This attribute declares whether this workflow task is approved or rejected. ")
  public WorkflowStatusEnum getWorkflowStatus() {
    return workflowStatus;
  }

  public void setWorkflowStatus(WorkflowStatusEnum workflowStatus) {
    this.workflowStatus = workflowStatus;
  }

  public WorkflowResponseDTO jsonPayload(String jsonPayload) {
    this.jsonPayload = jsonPayload;
    return this;
  }

   /**
   * Attributes that returned after the workflow execution 
   * @return jsonPayload
  **/
  @ApiModelProperty(value = "Attributes that returned after the workflow execution ")
  public String getJsonPayload() {
    return jsonPayload;
  }

  public void setJsonPayload(String jsonPayload) {
    this.jsonPayload = jsonPayload;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WorkflowResponseDTO workflowResponse = (WorkflowResponseDTO) o;
    return Objects.equals(this.workflowStatus, workflowResponse.workflowStatus) &&
        Objects.equals(this.jsonPayload, workflowResponse.jsonPayload);
  }

  @Override
  public int hashCode() {
    return Objects.hash(workflowStatus, jsonPayload);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WorkflowResponseDTO {\n");
    
    sb.append("    workflowStatus: ").append(toIndentedString(workflowStatus)).append("\n");
    sb.append("    jsonPayload: ").append(toIndentedString(jsonPayload)).append("\n");
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

