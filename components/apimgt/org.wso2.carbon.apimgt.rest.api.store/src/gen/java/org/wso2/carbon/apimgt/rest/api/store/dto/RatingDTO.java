package org.wso2.carbon.apimgt.rest.api.store.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * RatingDTO
 */
public class RatingDTO   {
  @SerializedName("ratingId")
  private String ratingId = null;

  @SerializedName("apiId")
  private String apiId = null;

  @SerializedName("username")
  private String username = null;

  @SerializedName("rating")
  private Integer rating = null;

  public RatingDTO ratingId(String ratingId) {
    this.ratingId = ratingId;
    return this;
  }

   /**
   * Get ratingId
   * @return ratingId
  **/
  @ApiModelProperty(required = true, value = "")
  public String getRatingId() {
    return ratingId;
  }

  public void setRatingId(String ratingId) {
    this.ratingId = ratingId;
  }

  public RatingDTO apiId(String apiId) {
    this.apiId = apiId;
    return this;
  }

   /**
   * Get apiId
   * @return apiId
  **/
  @ApiModelProperty(required = true, value = "")
  public String getApiId() {
    return apiId;
  }

  public void setApiId(String apiId) {
    this.apiId = apiId;
  }

  public RatingDTO username(String username) {
    this.username = username;
    return this;
  }

   /**
   * If username is not given user invoking the API will be taken as the username. 
   * @return username
  **/
  @ApiModelProperty(required = true, value = "If username is not given user invoking the API will be taken as the username. ")
  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public RatingDTO rating(Integer rating) {
    this.rating = rating;
    return this;
  }

   /**
   * Get rating
   * @return rating
  **/
  @ApiModelProperty(required = true, value = "")
  public Integer getRating() {
    return rating;
  }

  public void setRating(Integer rating) {
    this.rating = rating;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    RatingDTO rating = (RatingDTO) o;
    return Objects.equals(this.ratingId, rating.ratingId) &&
        Objects.equals(this.apiId, rating.apiId) &&
        Objects.equals(this.username, rating.username) &&
        Objects.equals(this.rating, rating.rating);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ratingId, apiId, username, rating);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RatingDTO {\n");
    
    sb.append("    ratingId: ").append(toIndentedString(ratingId)).append("\n");
    sb.append("    apiId: ").append(toIndentedString(apiId)).append("\n");
    sb.append("    username: ").append(toIndentedString(username)).append("\n");
    sb.append("    rating: ").append(toIndentedString(rating)).append("\n");
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

