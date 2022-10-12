package org.wso2.apk.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.*;

import java.util.Objects;

import javax.xml.bind.annotation.*;

public class WorkflowResponseDTO   {
  

    @XmlType(name="WorkflowStatusEnum")
    @XmlEnum(String.class)
    public enum WorkflowStatusEnum {
        CREATED("CREATED"),
        APPROVED("APPROVED"),
        REJECTED("REJECTED"),
        REGISTERED("REGISTERED");
        private String value;

        WorkflowStatusEnum (String v) {
            value = v;
        }

        public String value() {
            return value;
        }

        @Override
        public String toString() {
            return String.valueOf(value);
        }

        @JsonCreator
        public static WorkflowStatusEnum fromValue(String v) {
            for (WorkflowStatusEnum b : WorkflowStatusEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private WorkflowStatusEnum workflowStatus = null;
    private String jsonPayload = null;

  /**
   * This attribute declares whether this workflow task is approved or rejected. 
   **/
  public WorkflowResponseDTO workflowStatus(WorkflowStatusEnum workflowStatus) {
    this.workflowStatus = workflowStatus;
    return this;
  }

  
  @ApiModelProperty(example = "APPROVED", required = true, value = "This attribute declares whether this workflow task is approved or rejected. ")
  @JsonProperty("workflowStatus")
  @NotNull
  public WorkflowStatusEnum getWorkflowStatus() {
    return workflowStatus;
  }
  public void setWorkflowStatus(WorkflowStatusEnum workflowStatus) {
    this.workflowStatus = workflowStatus;
  }

  /**
   * Attributes that returned after the workflow execution 
   **/
  public WorkflowResponseDTO jsonPayload(String jsonPayload) {
    this.jsonPayload = jsonPayload;
    return this;
  }

  
  @ApiModelProperty(value = "Attributes that returned after the workflow execution ")
  @JsonProperty("jsonPayload")
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
    return Objects.equals(workflowStatus, workflowResponse.workflowStatus) &&
        Objects.equals(jsonPayload, workflowResponse.jsonPayload);
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

