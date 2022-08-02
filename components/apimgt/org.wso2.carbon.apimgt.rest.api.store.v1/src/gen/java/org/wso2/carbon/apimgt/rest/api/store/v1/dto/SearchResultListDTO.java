package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PaginationDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.common.annotations.Scope;
import com.fasterxml.jackson.annotation.JsonCreator;

import javax.validation.Valid;



public class SearchResultListDTO   {
  
    private Integer count = null;
    private List<Object> list = new ArrayList<Object>();
    private PaginationDTO pagination = null;

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
  public SearchResultListDTO list(List<Object> list) {
    this.list = list;
    return this;
  }

  
  @ApiModelProperty(example = "[{\"id\":\"abcff4cf-24c5-4298-a7b4-39a1fbd34693\",\"name\":\"PizzaShackAPI\",\"type\":\"API\",\"transportType\":null,\"description\":null,\"context\":\"/pizzashack\",\"version\":\"1.0.0\",\"provider\":\"admin\",\"status\":\"PUBLISHED\",\"thumbnailUri\":null,\"businessInformation\":{\"businessOwner\":\"Jane Roe\",\"businessOwnerEmail\":\"businessowner@wso2.com\",\"technicalOwner\":\"John Doe\",\"technicalOwnerEmail\":\"technicalowner@wso2.com\"},\"avgRating\":\"4.0\"}]", value = "")
  @JsonProperty("list")
  public List<Object> getList() {
    return list;
  }
  public void setList(List<Object> list) {
    this.list = list;
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
    return Objects.equals(count, searchResultList.count) &&
        Objects.equals(list, searchResultList.list) &&
        Objects.equals(pagination, searchResultList.pagination);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, list, pagination);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SearchResultListDTO {\n");
    
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

