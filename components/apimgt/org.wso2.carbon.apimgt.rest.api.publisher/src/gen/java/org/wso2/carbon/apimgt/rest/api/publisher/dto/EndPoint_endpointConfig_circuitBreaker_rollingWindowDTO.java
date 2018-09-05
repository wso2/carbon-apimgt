package org.wso2.carbon.apimgt.rest.api.publisher.dto;


import com.google.gson.annotations.SerializedName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.Objects;

/**
 * EndPoint_endpointConfig_circuitBreaker_rollingWindowDTO
 */
public class EndPoint_endpointConfig_circuitBreaker_rollingWindowDTO   {
  @SerializedName("timeWindow")
  private Integer timeWindow = null;

  @SerializedName("bucketSize")
  private Integer bucketSize = null;

  @SerializedName("requestVolumeThreshold")
  private Integer requestVolumeThreshold = null;

  public EndPoint_endpointConfig_circuitBreaker_rollingWindowDTO timeWindow(Integer timeWindow) {
    this.timeWindow = timeWindow;
    return this;
  }

   /**
   * time window in milliseconds
   * @return timeWindow
  **/
  @ApiModelProperty(example = "1000", value = "time window in milliseconds")
  public Integer getTimeWindow() {
    return timeWindow;
  }

  public void setTimeWindow(Integer timeWindow) {
    this.timeWindow = timeWindow;
  }

  public EndPoint_endpointConfig_circuitBreaker_rollingWindowDTO bucketSize(Integer bucketSize) {
    this.bucketSize = bucketSize;
    return this;
  }

   /**
   * bucket size in milliseconds 
   * @return bucketSize
  **/
  @ApiModelProperty(example = "1000", value = "bucket size in milliseconds ")
  public Integer getBucketSize() {
    return bucketSize;
  }

  public void setBucketSize(Integer bucketSize) {
    this.bucketSize = bucketSize;
  }

  public EndPoint_endpointConfig_circuitBreaker_rollingWindowDTO requestVolumeThreshold(Integer requestVolumeThreshold) {
    this.requestVolumeThreshold = requestVolumeThreshold;
    return this;
  }

   /**
   * Minimum number of requests in a rolling window that will trip the circuit. 
   * @return requestVolumeThreshold
  **/
  @ApiModelProperty(example = "2", value = "Minimum number of requests in a rolling window that will trip the circuit. ")
  public Integer getRequestVolumeThreshold() {
    return requestVolumeThreshold;
  }

  public void setRequestVolumeThreshold(Integer requestVolumeThreshold) {
    this.requestVolumeThreshold = requestVolumeThreshold;
  }


  @Override
  public boolean equals(java.lang.Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    EndPoint_endpointConfig_circuitBreaker_rollingWindowDTO endPointEndpointConfigCircuitBreakerRollingWindow = (EndPoint_endpointConfig_circuitBreaker_rollingWindowDTO) o;
    return Objects.equals(this.timeWindow, endPointEndpointConfigCircuitBreakerRollingWindow.timeWindow) &&
        Objects.equals(this.bucketSize, endPointEndpointConfigCircuitBreakerRollingWindow.bucketSize) &&
        Objects.equals(this.requestVolumeThreshold, endPointEndpointConfigCircuitBreakerRollingWindow.requestVolumeThreshold);
  }

  @Override
  public int hashCode() {
    return Objects.hash(timeWindow, bucketSize, requestVolumeThreshold);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("class EndPoint_endpointConfig_circuitBreaker_rollingWindowDTO {\n");
    
    sb.append("    timeWindow: ").append(toIndentedString(timeWindow)).append("\n");
    sb.append("    bucketSize: ").append(toIndentedString(bucketSize)).append("\n");
    sb.append("    requestVolumeThreshold: ").append(toIndentedString(requestVolumeThreshold)).append("\n");
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

