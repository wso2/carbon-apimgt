package org.wso2.apk.apimgt.rest.api.backoffice.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.PaginationDTO;
import org.wso2.apk.apimgt.rest.api.backoffice.v1.dto.UsagePlanDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.apk.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class UsagePlanListDTO   {
  
    private Integer count = null;
    private List<UsagePlanDTO> list = new ArrayList<UsagePlanDTO>();
    private PaginationDTO pagination = null;

  /**
   * Number of Usage Plans returned. 
   **/
  public UsagePlanListDTO count(Integer count) {
    this.count = count;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "Number of Usage Plans returned. ")
  @JsonProperty("count")
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }

  /**
   * Array of Usage Policies 
   **/
  public UsagePlanListDTO list(List<UsagePlanDTO> list) {
    this.list = list;
    return this;
  }

  
  @ApiModelProperty(value = "Array of Usage Policies ")
      @Valid
  @JsonProperty("list")
  public List<UsagePlanDTO> getList() {
    return list;
  }
  public void setList(List<UsagePlanDTO> list) {
    this.list = list;
  }

  /**
   **/
  public UsagePlanListDTO pagination(PaginationDTO pagination) {
    this.pagination = pagination;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("pagination")
  public PaginationDTO getPagination() {
    return pagination;
  }
  public void setPagination(PaginationDTO pagination) {
    this.pagination = pagination;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    UsagePlanListDTO usagePlanList = (UsagePlanListDTO) o;
    return Objects.equals(count, usagePlanList.count) &&
        Objects.equals(list, usagePlanList.list) &&
        Objects.equals(pagination, usagePlanList.pagination);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, list, pagination);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class UsagePlanListDTO {\n");
    
    sb.append("    count: ").append(toIndentedString(count)).append("\n");
    sb.append("    list: ").append(toIndentedString(list)).append("\n");
    sb.append("    pagination: ").append(toIndentedString(pagination)).append("\n");
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

