package org.wso2.apk.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;

import java.util.Objects;

import javax.validation.Valid;



public class TopicListDTO   {
  
    private Integer count = null;
    private List<TopicDTO> list = new ArrayList<TopicDTO>();
    private PaginationDTO pagination = null;

  /**
   * Number of Topics returned. 
   **/
  public TopicListDTO count(Integer count) {
    this.count = count;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "Number of Topics returned. ")
  @JsonProperty("count")
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }

  /**
   **/
  public TopicListDTO list(List<TopicDTO> list) {
    this.list = list;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("list")
  public List<TopicDTO> getList() {
    return list;
  }
  public void setList(List<TopicDTO> list) {
    this.list = list;
  }

  /**
   **/
  public TopicListDTO pagination(PaginationDTO pagination) {
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
    TopicListDTO topicList = (TopicListDTO) o;
    return Objects.equals(count, topicList.count) &&
        Objects.equals(list, topicList.list) &&
        Objects.equals(pagination, topicList.pagination);
  }

  @Override
  public int hashCode() {
    return Objects.hash(count, list, pagination);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class TopicListDTO {\n");
    
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

