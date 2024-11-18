package org.wso2.carbon.apimgt.governance.rest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.governance.rest.api.dto.GovernancePolicyInfoDTO;
import javax.validation.constraints.*;

/**
 * A list of governance policies.
 **/

import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;

@ApiModel(description = "A list of governance policies.")

public class GovernancePolicyListDTO   {
  
    private Integer count = null;
    private List<GovernancePolicyInfoDTO> list = new ArrayList<GovernancePolicyInfoDTO>();

  /**
   * Number of governance policies returned.
   **/
  public GovernancePolicyListDTO count(Integer count) {
    this.count = count;
    return this;
  }

  
  @ApiModelProperty(example = "10", value = "Number of governance policies returned.")
  @JsonProperty("count")
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }

  /**
   * List of governance policies.
   **/
  public GovernancePolicyListDTO list(List<GovernancePolicyInfoDTO> list) {
    this.list = list;
    return this;
  }

  
  @ApiModelProperty(value = "List of governance policies.")
      @Valid
  @JsonProperty("list")
  public List<GovernancePolicyInfoDTO> getList() {
    return list;
  }
  public void setList(List<GovernancePolicyInfoDTO> list) {
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
    GovernancePolicyListDTO governancePolicyList = (GovernancePolicyListDTO) o;
    return Objects.equals(count, governancePolicyList.count) &&
        Objects.equals(list, governancePolicyList.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class GovernancePolicyListDTO {\n");
    
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

