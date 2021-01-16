package org.wso2.carbon.apimgt.rest.api.service.catalog.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.PaginationDTO;
import org.wso2.carbon.apimgt.rest.api.service.catalog.dto.ServiceDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class ServiceListDTO   {
  
    private Integer limit = null;
    private Integer offset = null;
    private Integer total = null;
    private List<ServiceDTO> list = new ArrayList<ServiceDTO>();
    private PaginationDTO pagination = null;

  /**
   **/
  public ServiceListDTO limit(Integer limit) {
    this.limit = limit;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("limit")
  public Integer getLimit() {
    return limit;
  }
  public void setLimit(Integer limit) {
    this.limit = limit;
  }

  /**
   **/
  public ServiceListDTO offset(Integer offset) {
    this.offset = offset;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("offset")
  public Integer getOffset() {
    return offset;
  }
  public void setOffset(Integer offset) {
    this.offset = offset;
  }

  /**
   **/
  public ServiceListDTO total(Integer total) {
    this.total = total;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("total")
  public Integer getTotal() {
    return total;
  }
  public void setTotal(Integer total) {
    this.total = total;
  }

  /**
   **/
  public ServiceListDTO list(List<ServiceDTO> list) {
    this.list = list;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("list")
  public List<ServiceDTO> getList() {
    return list;
  }
  public void setList(List<ServiceDTO> list) {
    this.list = list;
  }

  /**
   **/
  public ServiceListDTO pagination(PaginationDTO pagination) {
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
    ServiceListDTO serviceList = (ServiceListDTO) o;
    return Objects.equals(limit, serviceList.limit) &&
        Objects.equals(offset, serviceList.offset) &&
        Objects.equals(total, serviceList.total) &&
        Objects.equals(list, serviceList.list) &&
        Objects.equals(pagination, serviceList.pagination);
  }

  @Override
  public int hashCode() {
    return Objects.hash(limit, offset, total, list, pagination);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ServiceListDTO {\n");
    
    sb.append("    limit: ").append(toIndentedString(limit)).append("\n");
    sb.append("    offset: ").append(toIndentedString(offset)).append("\n");
    sb.append("    total: ").append(toIndentedString(total)).append("\n");
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

