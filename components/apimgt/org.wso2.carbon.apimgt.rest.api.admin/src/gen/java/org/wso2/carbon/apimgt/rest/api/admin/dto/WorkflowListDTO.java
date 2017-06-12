package org.wso2.carbon.apimgt.rest.api.admin.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.dto.WorkflowDTO;
import java.util.Objects;

/**
 * WorkflowListDTO
 */
public class WorkflowListDTO   {
  @JsonProperty("count")
  private Integer count = null;

  @JsonProperty("list")
  private List<WorkflowDTO> list = new ArrayList<WorkflowDTO>();

  public WorkflowListDTO count(Integer count) {
    this.count = count;
    return this;
  }

   /**
   * Number of workflow entries returned. 
   * @return count
  **/
  @ApiModelProperty(example = "1", value = "Number of workflow entries returned. ")
  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public WorkflowListDTO list(List<WorkflowDTO> list) {
    this.list = list;
    return this;
  }

  public WorkflowListDTO addListItem(WorkflowDTO listItem) {
    this.list.add(listItem);
    return this;
  }

   /**
   * Get list
   * @return list
  **/
  @ApiModelProperty(value = "")
  public List<WorkflowDTO> getList() {
    return list;
  }

  public void setList(List<WorkflowDTO> list) {
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
    return Objects.equals(this.count, workflowList.count) &&
        Objects.equals(this.list, workflowList.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class WorkflowListDTO {\n");
    
    sb.append("    count: ").append(toIndentedString(count)).append("\n");
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

