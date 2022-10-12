package org.wso2.apk.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;

import java.util.Objects;

import javax.validation.Valid;



public class RatingListDTO   {
  
    private String avgRating = null;
    private Integer userRating = null;
    private Integer count = null;
    private List<RatingDTO> list = new ArrayList<RatingDTO>();
    private PaginationDTO pagination = null;

  /**
   * Average Rating of the API 
   **/
  public RatingListDTO avgRating(String avgRating) {
    this.avgRating = avgRating;
    return this;
  }

  
  @ApiModelProperty(example = "4", value = "Average Rating of the API ")
  @JsonProperty("avgRating")
  public String getAvgRating() {
    return avgRating;
  }
  public void setAvgRating(String avgRating) {
    this.avgRating = avgRating;
  }

  /**
   * Rating given by the user 
   **/
  public RatingListDTO userRating(Integer userRating) {
    this.userRating = userRating;
    return this;
  }

  
  @ApiModelProperty(example = "4", value = "Rating given by the user ")
  @JsonProperty("userRating")
  public Integer getUserRating() {
    return userRating;
  }
  public void setUserRating(Integer userRating) {
    this.userRating = userRating;
  }

  /**
   * Number of Subscriber Ratings returned. 
   **/
  public RatingListDTO count(Integer count) {
    this.count = count;
    return this;
  }

  
  @ApiModelProperty(example = "1", value = "Number of Subscriber Ratings returned. ")
  @JsonProperty("count")
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }

  /**
   **/
  public RatingListDTO list(List<RatingDTO> list) {
    this.list = list;
    return this;
  }

  
  @ApiModelProperty(value = "")
      @Valid
  @JsonProperty("list")
  public List<RatingDTO> getList() {
    return list;
  }
  public void setList(List<RatingDTO> list) {
    this.list = list;
  }

  /**
   **/
  public RatingListDTO pagination(PaginationDTO pagination) {
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
    RatingListDTO ratingList = (RatingListDTO) o;
    return Objects.equals(avgRating, ratingList.avgRating) &&
        Objects.equals(userRating, ratingList.userRating) &&
        Objects.equals(count, ratingList.count) &&
        Objects.equals(list, ratingList.list) &&
        Objects.equals(pagination, ratingList.pagination);
  }

  @Override
  public int hashCode() {
    return Objects.hash(avgRating, userRating, count, list, pagination);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RatingListDTO {\n");
    
    sb.append("    avgRating: ").append(toIndentedString(avgRating)).append("\n");
    sb.append("    userRating: ").append(toIndentedString(userRating)).append("\n");
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

