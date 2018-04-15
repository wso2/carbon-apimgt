package org.wso2.carbon.apimgt.rest.api.analytics.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * SubscriptionCountDTO
 */
public class SubscriptionCountDTO   {
  @SerializedName("time")
  private String time = null;

  @SerializedName("count")
  private Long count = null;

  public SubscriptionCountDTO time(String time) {
    this.time = time;
    return this;
  }

   /**
   * Timestamp in UTC 
   * @return time
  **/
  @ApiModelProperty(value = "Timestamp in UTC ")
  public String getTime() {
    return time;
  }

  public void setTime(String time) {
    this.time = time;
  }

  public SubscriptionCountDTO count(Long count) {
    this.count = count;
    return this;
  }

   /**
   * Number of subscriptions created 
   * @return count
  **/
  @ApiModelProperty(value = "Number of subscriptions created ")
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
    SubscriptionCountDTO subscriptionCount = (SubscriptionCountDTO) o;
    return Objects.equals(this.time, subscriptionCount.time) &&
        Objects.equals(this.count, subscriptionCount.count);
  }

  @Override
  public int hashCode() {
    return Objects.hash(time, count);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class SubscriptionCountDTO {\n");
    
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

