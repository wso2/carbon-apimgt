package org.wso2.carbon.apimgt.rest.api.store.dto;

import java.util.Objects;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * AvgRatingDTO
 */
@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-03-24T18:12:27.379+05:30")
public class AvgRatingDTO   {
  @JsonProperty("apiId")
  private String apiId = null;

  @JsonProperty("avgRating")
  private String avgRating = null;

  public AvgRatingDTO apiId(String apiId) {
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

  public AvgRatingDTO avgRating(String avgRating) {
    this.avgRating = avgRating;
    return this;
  }

   /**
   * Average rating of the API 
   * @return avgRating
  **/
  @ApiModelProperty(required = true, value = "Average rating of the API ")
  public String getAvgRating() {
    return avgRating;
  }

  public void setAvgRating(String avgRating) {
    this.avgRating = avgRating;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    AvgRatingDTO avgRating = (AvgRatingDTO) o;
    return Objects.equals(this.apiId, avgRating.apiId) &&
        Objects.equals(this.avgRating, avgRating.avgRating);
  }

  @Override
  public int hashCode() {
    return Objects.hash(apiId, avgRating);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class AvgRatingDTO {\n");
    
    sb.append("    apiId: ").append(toIndentedString(apiId)).append("\n");
    sb.append("    avgRating: ").append(toIndentedString(avgRating)).append("\n");
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

