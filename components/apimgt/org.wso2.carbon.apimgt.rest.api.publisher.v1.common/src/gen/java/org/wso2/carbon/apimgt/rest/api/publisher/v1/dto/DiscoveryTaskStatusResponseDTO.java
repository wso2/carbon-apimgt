package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.*;

/**
 * Current status and optional result of a discovery task.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;
@ApiModel(description = "Current status and optional result of a discovery task.")


public class DiscoveryTaskStatusResponseDTO   {
  
    private String taskId = null;

    @XmlType(name="StatusEnum")
    @XmlEnum(String.class)
    public enum StatusEnum {
        PENDING("PENDING"),
        COMPLETED("COMPLETED"),
        FAILED("FAILED");
        private String value;

        StatusEnum (String v) {
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
        public static StatusEnum fromValue(String v) {
            for (StatusEnum b : StatusEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private StatusEnum status = null;
    private List<Object> result = new ArrayList<Object>();
    private String error = null;

  /**
   * Unique identifier of the discovery task.
   **/
  public DiscoveryTaskStatusResponseDTO taskId(String taskId) {
    this.taskId = taskId;
    return this;
  }

  
  @ApiModelProperty(value = "Unique identifier of the discovery task.")
  @JsonProperty("taskId")
  public String getTaskId() {
    return taskId;
  }
  public void setTaskId(String taskId) {
    this.taskId = taskId;
  }

  /**
   * Current status of the discovery task.
   **/
  public DiscoveryTaskStatusResponseDTO status(StatusEnum status) {
    this.status = status;
    return this;
  }

  
  @ApiModelProperty(value = "Current status of the discovery task.")
  @JsonProperty("status")
  public StatusEnum getStatus() {
    return status;
  }
  public void setStatus(StatusEnum status) {
    this.status = status;
  }

  /**
   * List of discovered APIs (populated when status is COMPLETED).
   **/
  public DiscoveryTaskStatusResponseDTO result(List<Object> result) {
    this.result = result;
    return this;
  }

  
  @ApiModelProperty(value = "List of discovered APIs (populated when status is COMPLETED).")
  @JsonProperty("result")
  public List<Object> getResult() {
    return result;
  }
  public void setResult(List<Object> result) {
    this.result = result;
  }

  /**
   * Error message if the task failed.
   **/
  public DiscoveryTaskStatusResponseDTO error(String error) {
    this.error = error;
    return this;
  }

  
  @ApiModelProperty(value = "Error message if the task failed.")
  @JsonProperty("error")
  public String getError() {
    return error;
  }
  public void setError(String error) {
    this.error = error;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    DiscoveryTaskStatusResponseDTO discoveryTaskStatusResponse = (DiscoveryTaskStatusResponseDTO) o;
    return Objects.equals(taskId, discoveryTaskStatusResponse.taskId) &&
        Objects.equals(status, discoveryTaskStatusResponse.status) &&
        Objects.equals(result, discoveryTaskStatusResponse.result) &&
        Objects.equals(error, discoveryTaskStatusResponse.error);
  }

  @Override
  public int hashCode() {
    return Objects.hash(taskId, status, result, error);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class DiscoveryTaskStatusResponseDTO {\n");
    
    sb.append("    taskId: ").append(toIndentedString(taskId)).append("\n");
    sb.append("    status: ").append(toIndentedString(status)).append("\n");
    sb.append("    result: ").append(toIndentedString(result)).append("\n");
    sb.append("    error: ").append(toIndentedString(error)).append("\n");
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

