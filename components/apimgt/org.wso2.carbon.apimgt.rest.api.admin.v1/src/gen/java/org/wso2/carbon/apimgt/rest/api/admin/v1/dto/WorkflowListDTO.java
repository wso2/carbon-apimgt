package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.WorkflowInfoDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class WorkflowListDTO   {
  
    private Integer count = null;
    private String next = null;
    private String previous = null;
    private List<WorkflowInfoDTO> list = new ArrayList<>();

  /**
   * Number of workflow processes returned. 
   **/
  public WorkflowListDTO count(Integer count) {
    this.count = count;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "Number of workflow processes returned. ")
  @JsonProperty("count")
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }

  /**
   * Link to the next subset of resources qualified. Empty if no more resources are to be returned. 
   **/
  public WorkflowListDTO next(String next) {
    this.next = next;
    return this;
  }

  
  @ApiModelProperty(example = "/workflows?limit=1&offset=2&user=", value = "Link to the next subset of resources qualified. Empty if no more resources are to be returned. ")
  @JsonProperty("next")
  public String getNext() {
    return next;
  }
  public void setNext(String next) {
    this.next = next;
  }

  /**
   * Link to the previous subset of resources qualified. Empty if current subset is the first subset returned. 
   **/
  public WorkflowListDTO previous(String previous) {
    this.previous = previous;
    return this;
  }

  
  @ApiModelProperty(example = "/workflows?limit=1&offset=0&user=", value = "Link to the previous subset of resources qualified. Empty if current subset is the first subset returned. ")
  @JsonProperty("previous")
  public String getPrevious() {
    return previous;
  }
  public void setPrevious(String previous) {
    this.previous = previous;
  }

  /**
   **/
  public WorkflowListDTO list(List<WorkflowInfoDTO> list) {
    this.list = list;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("list")
  public List<WorkflowInfoDTO> getList() {
    return list;
  }
  public void setList(List<WorkflowInfoDTO> list) {
    this.list = list;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    WorkflowListDTO workflowList = (WorkflowListDTO) o;
    return Objects.equals(count, workflowList.count) &&
        Objects.equals(next, workflowList.next) &&
        Objects.equals(previous, workflowList.previous) &&
        Objects.equals(list, workflowList.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, next, previous, list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WorkflowListDTO {\n");
    
    sb.append("    count: ").append(toIndentedString(count)).append("\n");
    sb.append("    next: ").append(toIndentedString(next)).append("\n");
    sb.append("    previous: ").append(toIndentedString(previous)).append("\n");
    sb.append("    list: ").append(toIndentedString(list)).append("\n");
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

