package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.PaginationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ServiceDiscoveriesInfoDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;



public class ServiceDiscoveriesInfoListDTO   {
  
    private Integer count = null;
    private String type = null;
    private List<ServiceDiscoveriesInfoDTO> list = new ArrayList<>();
    private PaginationDTO pagination = null;

  /**
   * Number of services returned. 
   **/
  public ServiceDiscoveriesInfoListDTO count(Integer count) {
    this.count = count;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "Number of services returned. ")
  @JsonProperty("count")
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }

  /**
   * Type of the service discovery system 
   **/
  public ServiceDiscoveriesInfoListDTO type(String type) {
    this.type = type;
    return this;
  }

  
  @ApiModelProperty(example = "Kubernetes", value = "Type of the service discovery system ")
  @JsonProperty("type")
  public String getType() {
    return type;
  }
  public void setType(String type) {
    this.type = type;
  }

  /**
   **/
  public ServiceDiscoveriesInfoListDTO list(List<ServiceDiscoveriesInfoDTO> list) {
    this.list = list;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("list")
  public List<ServiceDiscoveriesInfoDTO> getList() {
    return list;
  }
  public void setList(List<ServiceDiscoveriesInfoDTO> list) {
    this.list = list;
  }

  /**
   **/
  public ServiceDiscoveriesInfoListDTO pagination(PaginationDTO pagination) {
    this.pagination = pagination;
    return this;
  }

  
  @ApiModelProperty(value = "")
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
    ServiceDiscoveriesInfoListDTO serviceDiscoveriesInfoList = (ServiceDiscoveriesInfoListDTO) o;
    return Objects.equals(count, serviceDiscoveriesInfoList.count) &&
        Objects.equals(type, serviceDiscoveriesInfoList.type) &&
        Objects.equals(list, serviceDiscoveriesInfoList.list) &&
        Objects.equals(pagination, serviceDiscoveriesInfoList.pagination);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, type, list, pagination);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ServiceDiscoveriesInfoListDTO {\n");
    
    sb.append("    count: ").append(toIndentedString(count)).append("\n");
    sb.append("    type: ").append(toIndentedString(type)).append("\n");
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

