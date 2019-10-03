package org.wso2.carbon.apimgt.rest.api.store.v1.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import javax.validation.constraints.*;


import io.swagger.annotations.*;
import java.util.Objects;

import javax.xml.bind.annotation.*;
import org.wso2.carbon.apimgt.rest.api.util.annotations.Scope;
import org.hibernate.validator.constraints.NotEmpty;



public class RatingDTO   {
  
    private String ratingId = null;
    private String apiId = null;
    private String ratedBy = null;
    private Integer rating = null;

  /**
   **/
  public RatingDTO ratingId(String ratingId) {
    this.ratingId = ratingId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("ratingId")
  public String getRatingId() {
    return ratingId;
  }
  public void setRatingId(String ratingId) {
    this.ratingId = ratingId;
  }

  /**
   **/
  public RatingDTO apiId(String apiId) {
    this.apiId = apiId;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("apiId")
  public String getApiId() {
    return apiId;
  }
  public void setApiId(String apiId) {
    this.apiId = apiId;
  }

  /**
   **/
  public RatingDTO ratedBy(String ratedBy) {
    this.ratedBy = ratedBy;
    return this;
  }

  
  @ApiModelProperty(value = "")
  @JsonProperty("ratedBy")
  public String getRatedBy() {
    return ratedBy;
  }
  public void setRatedBy(String ratedBy) {
    this.ratedBy = ratedBy;
  }

  /**
   **/
  public RatingDTO rating(Integer rating) {
    this.rating = rating;
    return this;
  }

  
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("rating")
  @NotNull
  @NotEmpty
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
    return Objects.equals(ratingId, rating.ratingId) &&
        Objects.equals(apiId, rating.apiId) &&
        Objects.equals(ratedBy, rating.ratedBy) &&
        Objects.equals(rating, rating.rating);
  }

  @Override
  public int hashCode() {
    return Objects.hash(ratingId, apiId, ratedBy, rating);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class RatingDTO {\n");
    
    sb.append("    ratingId: ").append(toIndentedString(ratingId)).append("\n");
    sb.append("    apiId: ").append(toIndentedString(apiId)).append("\n");
    sb.append("    ratedBy: ").append(toIndentedString(ratedBy)).append("\n");
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

