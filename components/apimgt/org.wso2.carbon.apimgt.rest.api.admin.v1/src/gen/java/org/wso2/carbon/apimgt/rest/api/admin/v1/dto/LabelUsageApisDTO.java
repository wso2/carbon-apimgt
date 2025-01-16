package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ApiResultDTO;
import javax.validation.constraints.*;

/**
 * List of APIs associated with the label. 
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "List of APIs associated with the label. ")

public class LabelUsageApisDTO   {
  
    private Integer count = null;
    private List<ApiResultDTO> list = new ArrayList<ApiResultDTO>();

  /**
   * Number of APIs associated with the label.
   **/
  public LabelUsageApisDTO count(Integer count) {
    this.count = count;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "Number of APIs associated with the label.")
  @JsonProperty("count")
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }

  /**
   * List of APIs associated with the label.
   **/
  public LabelUsageApisDTO list(List<ApiResultDTO> list) {
    this.list = list;
    return this;
  }

  
  @ApiModelProperty(value = "List of APIs associated with the label.")
      @Valid
  @JsonProperty("list")
  public List<ApiResultDTO> getList() {
    return list;
  }
  public void setList(List<ApiResultDTO> list) {
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
    LabelUsageApisDTO labelUsageApis = (LabelUsageApisDTO) o;
    return Objects.equals(count, labelUsageApis.count) &&
        Objects.equals(list, labelUsageApis.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class LabelUsageApisDTO {\n");
    
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

