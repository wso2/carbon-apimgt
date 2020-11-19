package org.wso2.carbon.apimgt.internal.service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.internal.service.dto.ApplicationPolicyDTO;
import org.wso2.carbon.apimgt.internal.service.dto.PaginationDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;



public class ApplicationPolicyListDTO   {
  
    private Integer count = null;
    private List<ApplicationPolicyDTO> list = new ArrayList<>();
    private PaginationDTO pagination = null;

  /**
   * Number of ApplicationPolicies returned. 
   **/
  public ApplicationPolicyListDTO count(Integer count) {
    this.count = count;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "Number of ApplicationPolicies returned. ")
  @JsonProperty("count")
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }

  /**
   **/
  public ApplicationPolicyListDTO list(List<ApplicationPolicyDTO> list) {
    this.list = list;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("list")
  public List<ApplicationPolicyDTO> getList() {
    return list;
  }
  public void setList(List<ApplicationPolicyDTO> list) {
    this.list = list;
  }

  /**
   **/
  public ApplicationPolicyListDTO pagination(PaginationDTO pagination) {
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
    ApplicationPolicyListDTO applicationPolicyList = (ApplicationPolicyListDTO) o;
    return Objects.equals(count, applicationPolicyList.count) &&
        Objects.equals(list, applicationPolicyList.list) &&
        Objects.equals(pagination, applicationPolicyList.pagination);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, list, pagination);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationPolicyListDTO {\n");
    
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

