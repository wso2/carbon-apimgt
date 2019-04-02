package org.wso2.carbon.apimgt.rest.api.store.v1.dto;


import io.swagger.annotations.*;
import com.fasterxml.jackson.annotation.*;

import javax.validation.constraints.NotNull;





@ApiModel(description = "")
public class RatingDTO  {
  
  
  @NotNull
  private String ratingId = null;
  
  @NotNull
  private String apiId = null;
  
  @NotNull
  private String username = null;
  
  @NotNull
  private Integer rating = null;

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("ratingId")
  public String getRatingId() {
    return ratingId;
  }
  public void setRatingId(String ratingId) {
    this.ratingId = ratingId;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("apiId")
  public String getApiId() {
    return apiId;
  }
  public void setApiId(String apiId) {
    this.apiId = apiId;
  }

  
  /**
   * If username is not given user invoking the API will be taken as the username.\n
   **/
  @ApiModelProperty(required = true, value = "If username is not given user invoking the API will be taken as the username.\n")
  @JsonProperty("username")
  public String getUsername() {
    return username;
  }
  public void setUsername(String username) {
    this.username = username;
  }

  
  /**
   **/
  @ApiModelProperty(required = true, value = "")
  @JsonProperty("rating")
  public Integer getRating() {
    return rating;
  }
  public void setRating(Integer rating) {
    this.rating = rating;
  }

  

  @Override
  public String toString()  {
    StringBuilder sb = new StringBuilder();
    sb.append("class RatingDTO {\n");
    
    sb.append("  ratingId: ").append(ratingId).append("\n");
    sb.append("  apiId: ").append(apiId).append("\n");
    sb.append("  username: ").append(username).append("\n");
    sb.append("  rating: ").append(rating).append("\n");
    sb.append("}\n");
    return sb.toString();
  }
}
