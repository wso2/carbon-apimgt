package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApiChatExecuteResponseResourceDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class ApiChatExecuteResponseDTO   {
  

    @XmlType(name="TaskStatusEnum")
    @XmlEnum(String.class)
    public enum TaskStatusEnum {
        IN_PROGRESS("IN_PROGRESS"),
        TERMINATED("TERMINATED"),
        COMPLETED("COMPLETED");
        private String value;

        TaskStatusEnum (String v) {
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
        public static TaskStatusEnum fromValue(String v) {
            for (TaskStatusEnum b : TaskStatusEnum.values()) {
                if (String.valueOf(b.value).equals(v)) {
                    return b;
                }
            }
return null;
        }
    }
    private TaskStatusEnum taskStatus = null;
    private ApiChatExecuteResponseResourceDTO resource = null;
    private String result = null;

  /**
   * Task status (IN_PROGRESS, TERMINATED or COMPLETED)
   **/
  public ApiChatExecuteResponseDTO taskStatus(TaskStatusEnum taskStatus) {
    this.taskStatus = taskStatus;
    return this;
  }

  
  @ApiModelProperty(example = "COMPLETED", required = true, value = "Task status (IN_PROGRESS, TERMINATED or COMPLETED)")
  @JsonProperty("taskStatus")
  @NotNull
  public TaskStatusEnum getTaskStatus() {
    return taskStatus;
  }
  public void setTaskStatus(TaskStatusEnum taskStatus) {
    this.taskStatus = taskStatus;
  }

  /**
   **/
  public ApiChatExecuteResponseDTO resource(ApiChatExecuteResponseResourceDTO resource) {
    this.resource = resource;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("resource")
  public ApiChatExecuteResponseResourceDTO getResource() {
    return resource;
  }
  public void setResource(ApiChatExecuteResponseResourceDTO resource) {
    this.resource = resource;
  }

  /**
   * completion result
   **/
  public ApiChatExecuteResponseDTO result(String result) {
    this.result = result;
    return this;
  }

  
  @ApiModelProperty(example = "The pet with ID 123 is available. Here are the details: - ID: 123 - Name: asd - Status: available", value = "completion result")
  @JsonProperty("result")
  public String getResult() {
    return result;
  }
  public void setResult(String result) {
    this.result = result;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiChatExecuteResponseDTO apiChatExecuteResponse = (ApiChatExecuteResponseDTO) o;
    return Objects.equals(taskStatus, apiChatExecuteResponse.taskStatus) &&
        Objects.equals(resource, apiChatExecuteResponse.resource) &&
        Objects.equals(result, apiChatExecuteResponse.result);
  }

  @Override
  public int hashCode() {
    return Objects.hash(taskStatus, resource, result);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiChatExecuteResponseDTO {\n");
    
    sb.append("    taskStatus: ").append(toIndentedString(taskStatus)).append("\n");
    sb.append("    resource: ").append(toIndentedString(resource)).append("\n");
    sb.append("    result: ").append(toIndentedString(result)).append("\n");
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

