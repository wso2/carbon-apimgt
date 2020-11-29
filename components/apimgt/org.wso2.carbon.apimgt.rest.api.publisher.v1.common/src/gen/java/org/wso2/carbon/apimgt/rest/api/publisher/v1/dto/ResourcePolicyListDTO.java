package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ResourcePolicyInfoDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class ResourcePolicyListDTO   {
  
    private List<ResourcePolicyInfoDTO> list = new ArrayList<ResourcePolicyInfoDTO>();
    private Integer count = null;

  /**
   **/
  public ResourcePolicyListDTO list(List<ResourcePolicyInfoDTO> list) {
    this.list = list;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("list")
  public List<ResourcePolicyInfoDTO> getList() {
    return list;
  }
  public void setList(List<ResourcePolicyInfoDTO> list) {
    this.list = list;
  }

  /**
   * Number of policy resources returned. 
   **/
  public ResourcePolicyListDTO count(Integer count) {
    this.count = count;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "Number of policy resources returned. ")
  @JsonProperty("count")
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    ResourcePolicyListDTO resourcePolicyList = (ResourcePolicyListDTO) o;
    return Objects.equals(list, resourcePolicyList.list) &&
        Objects.equals(count, resourcePolicyList.count);
  }

  @Override
  public int hashCode() {
    return Objects.hash(list, count);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ResourcePolicyListDTO {\n");
    
    sb.append("    list: ").append(toIndentedString(list)).append("\n");
    sb.append("    count: ").append(toIndentedString(count)).append("\n");
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

