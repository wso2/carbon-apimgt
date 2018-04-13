package org.wso2.carbon.apimgt.rest.api.store.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.dto.RatingDTO;
import java.util.Objects;

/**
 * RatingListDTO
 */
public class RatingListDTO   {
  @SerializedName("avgRating")
  private String avgRating = null;

  @SerializedName("userRating")
  private String userRating = null;

  @SerializedName("count")
  private Integer count = null;

  @SerializedName("next")
  private String next = null;

  @SerializedName("previous")
  private String previous = null;

  @SerializedName("list")
  private List<RatingDTO> list = new ArrayList<RatingDTO>();

  public RatingListDTO avgRating(String avgRating) {
    this.avgRating = avgRating;
    return this;
  }

   /**
   * Average Rating of the API 
   * @return avgRating
  **/
  @ApiModelProperty(value = "Average Rating of the API ")
  public String getAvgRating() {
    return avgRating;
  }

  public void setAvgRating(String avgRating) {
    this.avgRating = avgRating;
  }

  public RatingListDTO userRating(String userRating) {
    this.userRating = userRating;
    return this;
  }

   /**
   * Rating given by the user 
   * @return userRating
  **/
  @ApiModelProperty(value = "Rating given by the user ")
  public String getUserRating() {
    return userRating;
  }

  public void setUserRating(String userRating) {
    this.userRating = userRating;
  }

  public RatingListDTO count(Integer count) {
    this.count = count;
    return this;
  }

   /**
   * Number of Subscriber Ratings returned. 
   * @return count
  **/
  @ApiModelProperty(value = "Number of Subscriber Ratings returned. ")
  public Integer getCount() {
    return count;
  }

  public void setCount(Integer count) {
    this.count = count;
  }

  public RatingListDTO next(String next) {
    this.next = next;
    return this;
  }

   /**
   * Link to the next subset of resources qualified. Empty if no more resources are to be returned. 
   * @return next
  **/
  @ApiModelProperty(value = "Link to the next subset of resources qualified. Empty if no more resources are to be returned. ")
  public String getNext() {
    return next;
  }

  public void setNext(String next) {
    this.next = next;
  }

  public RatingListDTO previous(String previous) {
    this.previous = previous;
    return this;
  }

   /**
   * Link to the previous subset of resources qualified. Empty if current subset is the first subset returned. 
   * @return previous
  **/
  @ApiModelProperty(value = "Link to the previous subset of resources qualified. Empty if current subset is the first subset returned. ")
  public String getPrevious() {
    return previous;
  }

  public void setPrevious(String previous) {
    this.previous = previous;
  }

  public RatingListDTO list(List<RatingDTO> list) {
    this.list = list;
    return this;
  }

  public RatingListDTO addListItem(RatingDTO listItem) {
    this.list.add(listItem);
    return this;
  }

   /**
   * Get list
   * @return list
  **/
  @ApiModelProperty(value = "")
  public List<RatingDTO> getList() {
    return list;
  }

  public void setList(List<RatingDTO> list) {
    this.list = list;
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
    return Objects.equals(this.avgRating, ratingList.avgRating) &&
        Objects.equals(this.userRating, ratingList.userRating) &&
        Objects.equals(this.count, ratingList.count) &&
        Objects.equals(this.next, ratingList.next) &&
        Objects.equals(this.previous, ratingList.previous) &&
        Objects.equals(this.list, ratingList.list);
  }

  @Override
  public int hashCode() {
    return Objects.hash(avgRating, userRating, count, next, previous, list);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RatingListDTO {\n");
    
    sb.append("    avgRating: ").append(toIndentedString(avgRating)).append("\n");
    sb.append("    userRating: ").append(toIndentedString(userRating)).append("\n");
    sb.append("    count: ").append(toIndentedString(count)).append("\n");
    sb.append("    next: ").append(toIndentedString(next)).append("\n");
    sb.append("    previous: ").append(toIndentedString(previous)).append("\n");
    sb.append("    list: ").append(toIndentedString(list)).append("\n");
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

