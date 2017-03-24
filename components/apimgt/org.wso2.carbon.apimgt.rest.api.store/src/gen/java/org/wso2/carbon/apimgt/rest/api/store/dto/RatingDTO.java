package org.wso2.carbon.apimgt.rest.api.store.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModelProperty;

import java.util.Objects;

/**
 * RatingDTO
 */
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-03-22T12:22:55.771+05:30")
public class RatingDTO   {
  @JsonProperty("ratingId")
  private String ratingId = null;

  @JsonProperty("apiId")
  private String apiId = null;

  @JsonProperty("subscriber")
  private String subscriber = null;

  @JsonProperty("rating")
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

  public RatingDTO subscriber(String subscriber) {
    this.subscriber = subscriber;
    return this;
  }

   /**
   * If subscriber is not given user invoking the API will be taken as the subscriber. 
   * @return subscriber
  **/
  @ApiModelProperty(required = true, value = "If subscriber is not given user invoking the API will be taken as the subscriber. ")
  public String getSubscriber() {
    return subscriber;
  }

  public void setSubscriber(String subscriber) {
    this.subscriber = subscriber;
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
        Objects.equals(this.subscriber, rating.subscriber) &&
        Objects.equals(this.rating, rating.rating);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ratingId, apiId, subscriber, rating);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RatingDTO {\n");
    
    sb.append("    ratingId: ").append(toIndentedString(ratingId)).append("\n");
    sb.append("    apiId: ").append(toIndentedString(apiId)).append("\n");
    sb.append("    subscriber: ").append(toIndentedString(subscriber)).append("\n");
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

