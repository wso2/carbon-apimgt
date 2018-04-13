package org.wso2.carbon.apimgt.rest.api.analytics.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * ApplicationCountDTO
 */
public class ApplicationCountDTO   {
  @SerializedName("time")
  private String time = null;

  @SerializedName("count")
  private Long count = null;

  public ApplicationCountDTO time(String time) {
    this.time = time;
    return this;
  }

   /**
   * Timestamp 
   * @return time
  **/
  @ApiModelProperty(value = "Timestamp ")
  public String getTime() {
    return time;
  }

  public void setTime(String time) {
    this.time = time;
  }

  public ApplicationCountDTO count(Long count) {
    this.count = count;
    return this;
  }

   /**
   * Number of application registrations 
   * @return count
  **/
  @ApiModelProperty(value = "Number of application registrations ")
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
    ApplicationCountDTO applicationCount = (ApplicationCountDTO) o;
    return Objects.equals(this.time, applicationCount.time) &&
        Objects.equals(this.count, applicationCount.count);
  }

  @Override
  public int hashCode() {
    return Objects.hash(time, count);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class ApplicationCountDTO {\n");
    
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

