package org.wso2.carbon.apimgt.rest.api.publisher.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.PaginationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.SearchResultDTO;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;



public class SearchResultListDTO   {
  
    private Integer count = null;
    private String next = null;
    private String previous = null;
    private List<SearchResultDTO> list = new ArrayList<>();
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
   * Link to the next subset of resources qualified. Empty if no more resources are to be returned. 
   **/
  public SearchResultListDTO next(String next) {
    this.next = next;
    return this;
  }

  
  @ApiModelProperty(example = "/apis?limit=1&offset=2&query=", value = "Link to the next subset of resources qualified. Empty if no more resources are to be returned. ")
  @JsonProperty("next")
  public String getNext() {
    return next;
  }
  public void setNext(String next) {
    this.next = next;
  }

  /**
   * Link to the previous subset of resources qualified. Empty if current subset is the first subset returned. 
   **/
  public SearchResultListDTO previous(String previous) {
    this.previous = previous;
    return this;
  }

  
  @ApiModelProperty(example = "/apis?limit=1&offset=0&query=", value = "Link to the previous subset of resources qualified. Empty if current subset is the first subset returned. ")
  @JsonProperty("previous")
  public String getPrevious() {
    return previous;
  }
  public void setPrevious(String previous) {
    this.previous = previous;
  }

  /**
   **/
  public SearchResultListDTO list(List<SearchResultDTO> list) {
    this.list = list;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("list")
  public List<SearchResultDTO> getList() {
    return list;
  }
  public void setList(List<SearchResultDTO> list) {
    this.list = list;
  }

  /**
   **/
  public SearchResultListDTO pagination(PaginationDTO pagination) {
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
    SearchResultListDTO searchResultList = (SearchResultListDTO) o;
    return Objects.equals(count, searchResultList.count) &&
        Objects.equals(next, searchResultList.next) &&
        Objects.equals(previous, searchResultList.previous) &&
        Objects.equals(list, searchResultList.list) &&
        Objects.equals(pagination, searchResultList.pagination);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, next, previous, list, pagination);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SearchResultListDTO {\n");
    
    sb.append("    count: ").append(toIndentedString(count)).append("\n");
    sb.append("    next: ").append(toIndentedString(next)).append("\n");
    sb.append("    previous: ").append(toIndentedString(previous)).append("\n");
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

