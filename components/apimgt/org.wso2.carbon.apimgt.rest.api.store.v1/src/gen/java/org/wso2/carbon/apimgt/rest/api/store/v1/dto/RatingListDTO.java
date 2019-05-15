package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.PaginationDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.RatingDTO;

import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class RatingListDTO  {
  
  
  
  private String avgRating = null;
  
  
  private String userRating = null;
  
  
  private Integer count = null;
  
  
  private List<RatingDTO> list = new ArrayList<RatingDTO>();
  
  
  private PaginationDTO pagination = null;

  
  /**
   * Average Rating of the API\n
   **/
  @ApiModelProperty(value = "Average Rating of the API\n")
  @JsonProperty("avgRating")
  public String getAvgRating() {
    return avgRating;
  }
  public void setAvgRating(String avgRating) {
    this.avgRating = avgRating;
  }

  
  /**
   * Rating given by the user\n
   **/
  @ApiModelProperty(value = "Rating given by the user\n")
  @JsonProperty("userRating")
  public String getUserRating() {
    return userRating;
  }
  public void setUserRating(String userRating) {
    this.userRating = userRating;
  }

  
  /**
   * Number of Subscriber Ratings returned.\n
   **/
  @ApiModelProperty(value = "Number of Subscriber Ratings returned.\n")
  @JsonProperty("count")
  public Integer getCount() {
    return count;
  }
  public void setCount(Integer count) {
    this.count = count;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("list")
  public List<RatingDTO> getList() {
    return list;
  }
  public void setList(List<RatingDTO> list) {
    this.list = list;
  }

  
  /**
   **/
  @ApiModelProperty(value = "")
  @JsonProperty("pagination")
  public PaginationDTO getPagination() {
    return pagination;
  }
  public void setPagination(PaginationDTO pagination) {
    this.pagination = pagination;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class RatingListDTO {\n");
    
    sb.append("  avgRating: ").append(avgRating).append("\n");
    sb.append("  userRating: ").append(userRating).append("\n");
    sb.append("  count: ").append(count).append("\n");
    sb.append("  list: ").append(list).append("\n");
    sb.append("  pagination: ").append(pagination).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
