package org.wso2.carbon.apimgt.rest.api.analytics.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * APICountDTO
 */
public class APICountDTO   {
  @SerializedName("time")
  private String time = null;

  @SerializedName("count")
  private Long count = null;

  public APICountDTO time(String time) {
    this.time = time;
    return this;
  }

   /**
   * Timestamps of created APIs 
   * @return time
  **/
  @ApiModelProperty(value = "Timestamps of created APIs ")
  public String getTime() {
    return time;
  }

  public void setTime(String time) {
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

