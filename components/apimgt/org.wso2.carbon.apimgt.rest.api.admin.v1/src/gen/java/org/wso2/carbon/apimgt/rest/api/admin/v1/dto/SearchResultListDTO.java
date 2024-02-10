package org.wso2.carbon.apimgt.rest.api.admin.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.ApiResultDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.PaginationDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class SearchResultListDTO   {
  
    private List<ApiResultDTO> apis = new ArrayList<ApiResultDTO>();
    private Integer count = null;
    private PaginationDTO pagination = null;

  /**
   **/
  public SearchResultListDTO apis(List<ApiResultDTO> apis) {
    this.apis = apis;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("apis")
  public List<ApiResultDTO> getApis() {
    return apis;
  }
  public void setApis(List<ApiResultDTO> apis) {
    this.apis = apis;
  }

  /**
   * Number of results returned. 
   **/
  public SearchResultListDTO count(Integer count) {
    this.count = count;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "Number of results returned. ")
  @JsonProperty("count")
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }

  /**
   **/
  public SearchResultListDTO pagination(PaginationDTO pagination) {
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
    SearchResultListDTO searchResultList = (SearchResultListDTO) o;
    return Objects.equals(apis, searchResultList.apis) &&
        Objects.equals(count, searchResultList.count) &&
        Objects.equals(pagination, searchResultList.pagination);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apis, count, pagination);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SearchResultListDTO {\n");
    
    sb.append("    apis: ").append(toIndentedString(apis)).append("\n");
    sb.append("    count: ").append(toIndentedString(count)).append("\n");
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

