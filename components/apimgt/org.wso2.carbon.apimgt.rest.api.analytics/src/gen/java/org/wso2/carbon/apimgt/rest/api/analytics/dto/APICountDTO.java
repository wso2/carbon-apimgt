package org.wso2.carbon.apimgt.rest.api.analytics.dto;


import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * APICountDTO
 */
public class APICountDTO   {
  @JsonProperty("time")
  private Long time = null;

  @JsonProperty("count")
  private Long count = null;

  public APICountDTO time(Long time) {
    this.time = time;
    return this;
  }

   /**
   * Timestamp 
   * @return time
  **/
  @ApiModelProperty(value = "Timestamp ")
  public Long getTime() {
    return time;
  }

  public void setTime(Long time) {
    this.time = time;
  }

  public APICountDTO count(Long count) {
    this.count = count;
    return this;
  }

   /**
   * Number of APIs created 
   * @return count
  **/
  @ApiModelProperty(value = "Number of APIs created ")
  public Long getCount() {
    return count;
  }

  public void setCount(Long count) {
    this.count = count;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    APICountDTO apICount = (APICountDTO) o;
    return Objects.equals(this.time, apICount.time) &&
        Objects.equals(this.count, apICount.count);
  }

  @Override
  public int hashCode() {
    return Objects.hash(time, count);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class APICountDTO {\n");
    
    sb.append("    time: ").append(toIndentedString(time)).append("\n");
    sb.append("    count: ").append(toIndentedString(count)).append("\n");
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

