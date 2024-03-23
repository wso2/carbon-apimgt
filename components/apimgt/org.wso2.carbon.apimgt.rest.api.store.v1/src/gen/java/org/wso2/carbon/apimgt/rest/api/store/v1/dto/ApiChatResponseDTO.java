package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ApiChatResponseResourceDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.EnrichedAPISpecDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.SampleQueryDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class ApiChatResponseDTO   {
  
    private EnrichedAPISpecDTO apiSpec = null;

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
    private ApiChatResponseResourceDTO resource = null;
    private String result = null;
    private List<SampleQueryDTO> queries = new ArrayList<SampleQueryDTO>();

  /**
   **/
  public ApiChatResponseDTO apiSpec(EnrichedAPISpecDTO apiSpec) {
    this.apiSpec = apiSpec;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("apiSpec")
  public EnrichedAPISpecDTO getApiSpec() {
    return apiSpec;
  }
  public void setApiSpec(EnrichedAPISpecDTO apiSpec) {
    this.apiSpec = apiSpec;
  }

  /**
   * Task status (IN_PROGRESS, TERMINATED or COMPLETED)
   **/
  public ApiChatResponseDTO taskStatus(TaskStatusEnum taskStatus) {
    this.taskStatus = taskStatus;
    return this;
  }

  
  @ApiModelProperty(example = "COMPLETED", value = "Task status (IN_PROGRESS, TERMINATED or COMPLETED)")
  @JsonProperty("taskStatus")
  public TaskStatusEnum getTaskStatus() {
    return taskStatus;
  }
  public void setTaskStatus(TaskStatusEnum taskStatus) {
    this.taskStatus = taskStatus;
  }

  /**
   **/
  public ApiChatResponseDTO resource(ApiChatResponseResourceDTO resource) {
    this.resource = resource;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("resource")
  public ApiChatResponseResourceDTO getResource() {
    return resource;
  }
  public void setResource(ApiChatResponseResourceDTO resource) {
    this.resource = resource;
  }

  /**
   * completion result
   **/
  public ApiChatResponseDTO result(String result) {
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

  /**
   * list of sample queries
   **/
  public ApiChatResponseDTO queries(List<SampleQueryDTO> queries) {
    this.queries = queries;
    return this;
  }

  
  @ApiModelProperty(value = "list of sample queries")
      @Valid
  @JsonProperty("queries")
  public List<SampleQueryDTO> getQueries() {
    return queries;
  }
  public void setQueries(List<SampleQueryDTO> queries) {
    this.queries = queries;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ApiChatResponseDTO apiChatResponse = (ApiChatResponseDTO) o;
    return Objects.equals(apiSpec, apiChatResponse.apiSpec) &&
        Objects.equals(taskStatus, apiChatResponse.taskStatus) &&
        Objects.equals(resource, apiChatResponse.resource) &&
        Objects.equals(result, apiChatResponse.result) &&
        Objects.equals(queries, apiChatResponse.queries);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiSpec, taskStatus, resource, result, queries);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApiChatResponseDTO {\n");
    
    sb.append("    apiSpec: ").append(toIndentedString(apiSpec)).append("\n");
    sb.append("    taskStatus: ").append(toIndentedString(taskStatus)).append("\n");
    sb.append("    resource: ").append(toIndentedString(resource)).append("\n");
    sb.append("    result: ").append(toIndentedString(result)).append("\n");
    sb.append("    queries: ").append(toIndentedString(queries)).append("\n");
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

